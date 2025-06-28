package com.escape.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.escape.common.ResultCode;
import com.escape.common.exception.BusinessException;
import com.escape.entity.Weapon;
import com.escape.mapper.WeaponMapper;
import com.escape.service.WeaponService;
import com.escape.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 武器服务实现类
 *
 * @author escape
 * @since 2025-06-14
 */
@Slf4j
@Service
public class WeaponServiceImpl extends ServiceImpl<WeaponMapper, Weapon> implements WeaponService {

    @Autowired
    private WeaponMapper weaponMapper;

    @Autowired
    private RedisUtils redisUtils;

    private static final String WEAPON_CACHE_PREFIX = "weapon:";
    private static final String WEAPON_LIST_CACHE_KEY = "weapon:list:enabled";
    private static final String WEAPON_TYPE_STATS_KEY = "weapon:stats:type";
    private static final long CACHE_EXPIRE_HOURS = 2;

    @Override
    public Map<String, Object> getWeaponDetail(Long weaponId) {
        if (weaponId == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "武器ID不能为空");
        }

        // 先从缓存获取
        String cacheKey = WEAPON_CACHE_PREFIX + "detail:" + weaponId;
        String cachedData = redisUtils.get(cacheKey);
        if (StringUtils.hasText(cachedData)) {
            log.debug("从缓存获取武器详情: {}", weaponId);
        }

        // 查询武器基本信息
        Weapon weapon = weaponMapper.selectById(weaponId);
        if (weapon == null || weapon.getDeleted() == 1) {
            throw new BusinessException(ResultCode.DATA_NOT_EXISTS.getCode(), "武器不存在");
        }

        // 组装结果
        Map<String, Object> result = new HashMap<>();
        result.put("weapon", weapon);

        // 添加伤害统计
        Map<String, Object> damageStats = new HashMap<>();
        damageStats.put("head", weapon.getDamageHead());
        damageStats.put("body", weapon.getDamageBody());
        damageStats.put("leg", weapon.getDamageLeg());
        result.put("damageStats", damageStats);

        // 缓存结果
        redisUtils.set(cacheKey, result.toString(), CACHE_EXPIRE_HOURS, TimeUnit.HOURS);

        return result;
    }

    @Override
    public Weapon getByWeaponKey(String weaponKey) {
        if (!StringUtils.hasText(weaponKey)) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "武器标识不能为空");
        }

        return weaponMapper.findByWeaponKey(weaponKey);
    }

    @Override
    public List<Weapon> getWeaponsByType(String weaponType) {
        if (!StringUtils.hasText(weaponType)) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "武器类型不能为空");
        }

        // 验证类型是否合法
        if (!isValidWeaponType(weaponType)) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "无效的武器类型");
        }

        return weaponMapper.findByWeaponType(weaponType);
    }

    @Override
    public List<Weapon> getWeaponsByPriceRange(Integer minPrice, Integer maxPrice) {
        if (minPrice == null || maxPrice == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "价格范围不能为空");
        }

        if (minPrice < 0 || maxPrice < minPrice) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "价格范围无效");
        }

        return weaponMapper.findByPriceRange(minPrice, maxPrice);
    }

    @Override
    public List<Weapon> getWeaponsByPenetration(String penetration) {
        if (!StringUtils.hasText(penetration)) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "穿透等级不能为空");
        }

        // 验证穿透等级是否合法
        if (!isValidPenetration(penetration)) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "无效的穿透等级");
        }

        return weaponMapper.findByPenetration(penetration);
    }

    @Override
    public List<Weapon> getAllEnabledWeapons() {
        // 先从缓存获取
        String cachedData = redisUtils.get(WEAPON_LIST_CACHE_KEY);
        if (StringUtils.hasText(cachedData)) {
            log.debug("从缓存获取武器列表");
        }

        List<Weapon> weapons = weaponMapper.findAllEnabled();

        // 缓存结果
        redisUtils.set(WEAPON_LIST_CACHE_KEY, weapons.toString(), CACHE_EXPIRE_HOURS, TimeUnit.HOURS);

        return weapons;
    }

    @Override
    public List<Map<String, Object>> getWeaponTypeStatistics() {
        // 从缓存获取
        String cachedData = redisUtils.get(WEAPON_TYPE_STATS_KEY);
        if (StringUtils.hasText(cachedData)) {
            log.debug("从缓存获取武器类型统计");
        }

        List<Map<String, Object>> stats = weaponMapper.statisticsByType();

        // 缓存结果
        redisUtils.set(WEAPON_TYPE_STATS_KEY, stats.toString(), CACHE_EXPIRE_HOURS, TimeUnit.HOURS);

        return stats;
    }

    @Override
    public List<Map<String, Object>> getWeaponOptions() {
        List<Map<String, Object>> options = weaponMapper.findWeaponOptions();
        return options;
    }

    @Override
    public IPage<Weapon> getWeaponPage(Page<Weapon> page, String weaponType, Integer minPrice, Integer maxPrice) {
        QueryWrapper<Weapon> wrapper = new QueryWrapper<>();
        wrapper.eq("deleted", 0)
                .eq("status", 1);

        if (StringUtils.hasText(weaponType)) {
            wrapper.eq("weapon_type", weaponType);
        }
        if (minPrice != null) {
            wrapper.ge("price", minPrice);
        }
        if (maxPrice != null) {
            wrapper.le("price", maxPrice);
        }

        wrapper.orderByAsc("weapon_type", "price", "sort_order");

        return weaponMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createWeapon(Weapon weapon) {
        // 验证武器信息
        validateWeapon(weapon);

        // 检查武器标识是否已存在
        Weapon existWeapon = weaponMapper.findByWeaponKey(weapon.getWeaponKey());
        if (existWeapon != null) {
            throw new BusinessException(ResultCode.DATA_ALREADY_EXISTS.getCode(), "武器标识已存在");
        }

        // 保存武器
        int result = weaponMapper.insert(weapon);
        if (result <= 0) {
            throw new BusinessException(ResultCode.DATABASE_ERROR.getCode(), "保存武器失败");
        }

        // 清除缓存
        refreshWeaponCache();

        return true;
    }

    @Override
    public boolean updateWeaponPrice(Long weaponId, Integer price) {
        if (weaponId == null || price == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "参数不能为空");
        }

        if (price < 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "价格不能为负数");
        }

        int result = weaponMapper.updatePrice(weaponId, price);

        if (result > 0) {
            // 清除相关缓存
            redisUtils.delete(WEAPON_CACHE_PREFIX + "detail:" + weaponId);
            refreshWeaponCache();
        }

        return result > 0;
    }

    @Override
    public boolean updateWeaponStatus(Long weaponId, Integer status) {
        if (weaponId == null || status == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "参数不能为空");
        }

        if (status != 0 && status != 1) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "状态值无效");
        }

        Weapon weapon = new Weapon();
        weapon.setId(weaponId);
        weapon.setStatus(status);

        int result = weaponMapper.updateById(weapon);

        if (result > 0) {
            refreshWeaponCache();
        }

        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> batchImportWeapons(List<Weapon> weapons) {
        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int failCount = 0;

        for (Weapon weapon : weapons) {
            try {
                validateWeapon(weapon);

                // 检查是否已存在
                Weapon existWeapon = weaponMapper.findByWeaponKey(weapon.getWeaponKey());
                if (existWeapon != null) {
                    failCount++;
                    log.warn("武器已存在，跳过: {}", weapon.getWeaponKey());
                    continue;
                }

                weaponMapper.insert(weapon);
                successCount++;
            } catch (Exception e) {
                failCount++;
                log.error("导入武器失败: {}", weapon.getWeaponKey(), e);
            }
        }

        result.put("totalCount", weapons.size());
        result.put("successCount", successCount);
        result.put("failCount", failCount);

        // 刷新缓存
        if (successCount > 0) {
            refreshWeaponCache();
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> compareWeapons(List<Long> weaponIds) {
        if (weaponIds == null || weaponIds.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "武器ID列表不能为空");
        }

        if (weaponIds.size() > 5) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "最多只能对比5把武器");
        }

        List<Map<String, Object>> compareData = new ArrayList<>();

        for (Long weaponId : weaponIds) {
            Weapon weapon = weaponMapper.selectById(weaponId);
            if (weapon != null && weapon.getDeleted() == 0) {
                Map<String, Object> weaponData = new HashMap<>();
                weaponData.put("id", weapon.getId());
                weaponData.put("name", weapon.getWeaponName());
                weaponData.put("type", weapon.getWeaponType());
                weaponData.put("price", weapon.getPrice());
                weaponData.put("damageHead", weapon.getDamageHead());
                weaponData.put("damageBody", weapon.getDamageBody());
                weaponData.put("damageLeg", weapon.getDamageLeg());
                weaponData.put("fireRate", weapon.getFireRate());
                weaponData.put("magazineSize", weapon.getMagazineSize());
                weaponData.put("penetration", weapon.getWallPenetration());
                compareData.add(weaponData);
            }
        }

        return compareData;
    }

    @Override
    public void refreshWeaponCache() {
        log.info("刷新武器缓存");
        // 删除列表缓存
        redisUtils.delete(WEAPON_LIST_CACHE_KEY);
        // 删除统计缓存
        redisUtils.delete(WEAPON_TYPE_STATS_KEY);
        // 删除所有武器详情缓存
        redisUtils.delete(redisUtils.keys(WEAPON_CACHE_PREFIX + "*"));
    }

    /**
     * 验证武器类型是否合法
     */
    private boolean isValidWeaponType(String weaponType) {
        return "sidearm".equals(weaponType) || "smg".equals(weaponType)
                || "rifle".equals(weaponType) || "sniper".equals(weaponType)
                || "heavy".equals(weaponType) || "melee".equals(weaponType);
    }

    /**
     * 验证穿透等级是否合法
     */
    private boolean isValidPenetration(String penetration) {
        return "low".equals(penetration) || "medium".equals(penetration) || "high".equals(penetration);
    }

    /**
     * 验证武器信息
     */
    private void validateWeapon(Weapon weapon) {
        if (weapon == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "武器信息不能为空");
        }
        if (!StringUtils.hasText(weapon.getWeaponKey())) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "武器标识不能为空");
        }
        if (!StringUtils.hasText(weapon.getWeaponName())) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "武器名称不能为空");
        }
        if (!StringUtils.hasText(weapon.getWeaponType())) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "武器类型不能为空");
        }
        if (!isValidWeaponType(weapon.getWeaponType())) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "无效的武器类型");
        }
        if (weapon.getPrice() == null || weapon.getPrice() < 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "武器价格不能为负数");
        }
    }
}