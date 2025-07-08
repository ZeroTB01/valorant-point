package com.escape.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.escape.common.ResultCode;
import com.escape.common.exception.BusinessException;
import com.escape.entity.Hero;
import com.escape.entity.HeroSkill;
import com.escape.mapper.HeroMapper;
import com.escape.mapper.HeroSkillMapper;
import com.escape.service.HeroService;
import com.escape.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 英雄服务实现类
 *
 * @author escape
 * @since 2025-06-11
 */
@Slf4j
@Service
public class HeroServiceImpl extends ServiceImpl<HeroMapper, Hero> implements HeroService {

    @Autowired
    private HeroMapper heroMapper;

    @Autowired
    private HeroSkillMapper heroSkillMapper;

    @Autowired
    private RedisUtils redisUtils;

    private static final String HERO_CACHE_PREFIX = "hero:";
    private static final String HERO_LIST_CACHE_KEY = "hero:list:enabled";
    private static final String HERO_TYPE_STATS_KEY = "hero:stats:type";
    private static final long CACHE_EXPIRE_HOURS = 2;

    @Override
    public Map<String, Object> getHeroDetail(Long heroId) {
        if (heroId == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "英雄ID不能为空");
        }

        // 先从缓存获取
        String cacheKey = HERO_CACHE_PREFIX + "detail:" + heroId;
        String cachedData = redisUtils.get(cacheKey);
        if (StringUtils.hasText(cachedData)) {
            // 这里应该反序列化，简化示例直接返回
            log.debug("从缓存获取英雄详情: {}", heroId);
        }

        // 查询英雄基本信息
        Hero hero = heroMapper.selectById(heroId);
        if (hero == null || hero.getDeleted() == 1) {
            throw new BusinessException(ResultCode.DATA_NOT_EXISTS.getCode(), "英雄不存在");
        }

        // 查询英雄技能
        List<HeroSkill> skills = heroSkillMapper.findByHeroId(heroId);

        // 组装结果
        Map<String, Object> result = new HashMap<>();
        result.put("hero", hero);
        result.put("skills", skills);

        // 缓存结果
        redisUtils.set(cacheKey, result.toString(), CACHE_EXPIRE_HOURS, TimeUnit.HOURS);

        return result;
    }

    @Override
    public Hero getByHeroKey(String heroKey) {
        if (!StringUtils.hasText(heroKey)) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "英雄标识不能为空");
        }

        return heroMapper.findByHeroKey(heroKey);
    }

    @Override
    public List<HeroSkill> getHeroSkills(Long heroId) {
        if (heroId == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "英雄ID不能为空");
        }

        return heroSkillMapper.findByHeroId(heroId);
    }

    @Override
    public List<Hero> getHeroesByType(String heroType) {
        if (!StringUtils.hasText(heroType)) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "英雄类型不能为空");
        }

        // 验证类型是否合法
        if (!isValidHeroType(heroType)) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "无效的英雄类型");
        }

        return heroMapper.findByHeroType(heroType);
    }

    @Override
    public List<Hero> getAllEnabledHeroes() {
        // 先从缓存获取
        String cachedData = redisUtils.get(HERO_LIST_CACHE_KEY);
        if (StringUtils.hasText(cachedData)) {
            log.debug("从缓存获取英雄列表");
            // 这里应该反序列化，简化示例
        }

        List<Hero> heroes = heroMapper.findAllEnabled();

        // 缓存结果
        redisUtils.set(HERO_LIST_CACHE_KEY, heroes.toString(), CACHE_EXPIRE_HOURS, TimeUnit.HOURS);

        return heroes;
    }

    @Override
    public Map<String, Integer> getHeroTypeStatistics() {
        // 从缓存获取
        String cachedData = redisUtils.get(HERO_TYPE_STATS_KEY);
        if (StringUtils.hasText(cachedData)) {
            log.debug("从缓存获取英雄类型统计");
        }

        List<Map<String, Object>> stats = heroMapper.countByHeroType();
        Map<String, Integer> result = stats.stream()
                .collect(Collectors.toMap(
                        m -> (String) m.get("hero_type"),
                        m -> ((Number) m.get("count")).intValue()
                ));

        // 缓存结果
        redisUtils.set(HERO_TYPE_STATS_KEY, result.toString(), CACHE_EXPIRE_HOURS, TimeUnit.HOURS);

        return result;
    }

    @Override
    public List<Hero> getHeroesByDifficulty(Integer difficulty) {
        if (difficulty == null || difficulty < 1 || difficulty > 5) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "难度等级必须在1-5之间");
        }

        return heroMapper.findByDifficulty(difficulty);
    }

    @Override
    public IPage<Hero> getHeroPage(Page<Hero> page, String heroType, Integer difficulty) {
        QueryWrapper<Hero> wrapper = new QueryWrapper<>();
        wrapper.eq("deleted", 0)
                .eq("status", 1);

        if (StringUtils.hasText(heroType)) {
            wrapper.eq("hero_type", heroType);
        }
        if (difficulty != null) {
            wrapper.eq("difficulty", difficulty);
        }

        wrapper.orderByAsc("sort_order");

        return heroMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createHeroWithSkills(Hero hero, List<HeroSkill> skills) {
        // 验证英雄信息
        validateHero(hero);

        // 检查英雄标识是否已存在
        Hero existHero = heroMapper.findByHeroKey(hero.getHeroKey());
        if (existHero != null) {
            throw new BusinessException(ResultCode.DATA_ALREADY_EXISTS.getCode(), "英雄标识已存在");
        }

        // 保存英雄
        int result = heroMapper.insert(hero);
        if (result <= 0) {
            throw new BusinessException(ResultCode.DATABASE_ERROR.getCode(), "保存英雄失败");
        }

        // 保存技能
        if (skills != null && !skills.isEmpty()) {
            for (HeroSkill skill : skills) {
                skill.setHeroId(hero.getId());
                validateHeroSkill(skill);
                heroSkillMapper.insert(skill);
            }
        }

        // 清除缓存
        refreshHeroCache();

        return true;
    }

    @Override
    public boolean updateHeroStatus(Long heroId, Integer status) {
        if (heroId == null || status == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "参数不能为空");
        }

        if (status != 0 && status != 1) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "状态值无效");
        }

        int result = heroMapper.updateStatus(heroId, status);

        if (result > 0) {
            // 清除相关缓存
            redisUtils.delete(HERO_CACHE_PREFIX + "detail:" + heroId);
            refreshHeroCache();
        }

        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> batchImportHeroes(List<Hero> heroes) {
        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int failCount = 0;

        for (Hero hero : heroes) {
            try {
                validateHero(hero);

                // 检查是否已存在
                Hero existHero = heroMapper.findByHeroKey(hero.getHeroKey());
                if (existHero != null) {
                    failCount++;
                    log.warn("英雄已存在，跳过: {}", hero.getHeroKey());
                    continue;
                }

                heroMapper.insert(hero);
                successCount++;
            } catch (Exception e) {
                failCount++;
                log.error("导入英雄失败: {}", hero.getHeroKey(), e);
            }
        }

        result.put("totalCount", heroes.size());
        result.put("successCount", successCount);
        result.put("failCount", failCount);

        // 刷新缓存
        if (successCount > 0) {
            refreshHeroCache();
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> getHeroOptions() {
        List<Hero> heroes = getAllEnabledHeroes();

        return heroes.stream()
                .map(hero -> {
                    Map<String, Object> option = new HashMap<>();
                    option.put("id", hero.getId());
                    option.put("heroKey", hero.getHeroKey());
                    option.put("heroName", hero.getHeroName());
                    option.put("heroType", hero.getHeroType());
                    option.put("avatar", hero.getAvatar());
                    return option;
                })
                .collect(Collectors.toList());
    }

    @Override
    public void refreshHeroCache() {
        log.info("刷新英雄缓存");
        // 删除列表缓存
        redisUtils.delete(HERO_LIST_CACHE_KEY);
        // 删除统计缓存
        redisUtils.delete(HERO_TYPE_STATS_KEY);
        // 删除所有英雄详情缓存
        redisUtils.delete(redisUtils.keys(HERO_CACHE_PREFIX + "*"));
    }


    /**更新英雄字段内容
     *
     * @param hero 英雄信息
     * @param token 令牌
     */
    @Override
    public void updateHero(Hero hero, String token) {
        // 可加权限校验
        if (hero.getId() == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "英雄ID不能为空");
        }
        // 只更新非null字段
        boolean success = this.updateById(hero);
        if (!success) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "英雄不存在");
        }
    }

    /**
     * 验证英雄类型是否合法
     */
    private boolean isValidHeroType(String heroType) {
        return "duelist".equals(heroType) || "sentinel".equals(heroType)
                || "controller".equals(heroType) || "initiator".equals(heroType);
    }

    /**
     * 验证英雄信息
     */
    private void validateHero(Hero hero) {
        if (hero == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "英雄信息不能为空");
        }
        if (!StringUtils.hasText(hero.getHeroKey())) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "英雄标识不能为空");
        }
        if (!StringUtils.hasText(hero.getHeroName())) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "英雄名称不能为空");
        }
        if (!StringUtils.hasText(hero.getHeroType())) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "英雄类型不能为空");
        }
        if (!isValidHeroType(hero.getHeroType())) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "无效的英雄类型");
        }
    }

    /**
     * 验证技能信息
     */
    private void validateHeroSkill(HeroSkill skill) {
        if (skill == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "技能信息不能为空");
        }
        if (!StringUtils.hasText(skill.getSkillKey())) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "技能键位不能为空");
        }
        if (!isValidSkillKey(skill.getSkillKey())) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "无效的技能键位");
        }
    }

    /**
     * 验证技能键位是否合法
     */
    private boolean isValidSkillKey(String skillKey) {
        return "C".equals(skillKey) || "Q".equals(skillKey)
                || "E".equals(skillKey) || "X".equals(skillKey);
    }
}