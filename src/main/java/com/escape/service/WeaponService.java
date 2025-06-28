package com.escape.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.escape.entity.Weapon;

import java.util.List;
import java.util.Map;

/**
 * 武器服务接口
 *
 * @author escape
 * @since 2025-06-14
 */
public interface WeaponService extends IService<Weapon> {

    /**
     * 获取武器详情
     * @param weaponId 武器ID
     * @return 武器详情
     */
    Map<String, Object> getWeaponDetail(Long weaponId);

    /**
     * 根据武器标识获取武器
     * @param weaponKey 武器标识
     * @return 武器信息
     */
    Weapon getByWeaponKey(String weaponKey);

    /**
     * 根据武器类型获取武器列表
     * @param weaponType 武器类型
     * @return 武器列表
     */
    List<Weapon> getWeaponsByType(String weaponType);

    /**
     * 根据价格范围获取武器
     * @param minPrice 最低价格
     * @param maxPrice 最高价格
     * @return 武器列表
     */
    List<Weapon> getWeaponsByPriceRange(Integer minPrice, Integer maxPrice);

    /**
     * 根据穿透等级获取武器
     * @param penetration 穿透等级
     * @return 武器列表
     */
    List<Weapon> getWeaponsByPenetration(String penetration);

    /**
     * 获取所有启用的武器（带缓存）
     * @return 武器列表
     */
    List<Weapon> getAllEnabledWeapons();

    /**
     * 获取武器类型统计
     * @return 统计结果
     */
    List<Map<String, Object>> getWeaponTypeStatistics();

    /**
     * 获取武器选项列表（用于下拉框）
     * @return 武器简要信息
     */
    List<Map<String, Object>> getWeaponOptions();

    /**
     * 分页查询武器
     * @param page 分页参数
     * @param weaponType 武器类型（可选）
     * @param minPrice 最低价格（可选）
     * @param maxPrice 最高价格（可选）
     * @return 分页结果
     */
    IPage<Weapon> getWeaponPage(Page<Weapon> page, String weaponType, Integer minPrice, Integer maxPrice);

    /**
     * 创建武器
     * @param weapon 武器信息
     * @return 是否成功
     */
    boolean createWeapon(Weapon weapon);

    /**
     * 更新武器价格
     * @param weaponId 武器ID
     * @param price 新价格
     * @return 是否成功
     */
    boolean updateWeaponPrice(Long weaponId, Integer price);

    /**
     * 更新武器状态
     * @param weaponId 武器ID
     * @param status 状态
     * @return 是否成功
     */
    boolean updateWeaponStatus(Long weaponId, Integer status);

    /**
     * 批量导入武器数据
     * @param weapons 武器列表
     * @return 导入结果
     */
    Map<String, Object> batchImportWeapons(List<Weapon> weapons);

    /**
     * 获取武器对比数据
     * @param weaponIds 要对比的武器ID列表
     * @return 对比数据
     */
    List<Map<String, Object>> compareWeapons(List<Long> weaponIds);

    /**
     * 刷新武器缓存
     */
    void refreshWeaponCache();
}