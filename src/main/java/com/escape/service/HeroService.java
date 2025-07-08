package com.escape.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.escape.entity.Hero;
import com.escape.entity.HeroSkill;

import java.util.List;
import java.util.Map;

/**
 * 英雄服务接口
 *
 * @author escape
 * @since 2025-06-11
 */
public interface HeroService extends IService<Hero> {

    /**
     * 获取英雄详情（包含技能）
     * @param heroId 英雄ID
     * @return 英雄详情
     */
    Map<String, Object> getHeroDetail(Long heroId);

    /**
     * 根据英雄标识获取英雄
     * @param heroKey 英雄标识
     * @return 英雄信息
     */
    Hero getByHeroKey(String heroKey);

    /**
     * 获取英雄技能列表
     * @param heroId 英雄ID
     * @return 技能列表
     */
    List<HeroSkill> getHeroSkills(Long heroId);

    /**
     * 按类型获取英雄列表
     * @param heroType 英雄类型
     * @return 英雄列表
     */
    List<Hero> getHeroesByType(String heroType);

    /**
     * 获取所有启用的英雄（带缓存）
     * @return 英雄列表
     */
    List<Hero> getAllEnabledHeroes();

    /**
     * 获取英雄类型统计
     * @return 统计结果
     */
    Map<String, Integer> getHeroTypeStatistics();

    /**
     * 按难度获取英雄
     * @param difficulty 难度等级(1-5)
     * @return 英雄列表
     */
    List<Hero> getHeroesByDifficulty(Integer difficulty);

    /**
     * 分页查询英雄
     * @param page 分页参数
     * @param heroType 英雄类型（可选）
     * @param difficulty 难度等级（可选）
     * @return 分页结果
     */
    IPage<Hero> getHeroPage(Page<Hero> page, String heroType, Integer difficulty);

    /**
     * 创建英雄（包含技能）
     * @param hero 英雄信息
     * @param skills 技能列表
     * @return 是否成功
     */
    boolean createHeroWithSkills(Hero hero, List<HeroSkill> skills);

    /**
     * 更新英雄状态
     * @param heroId 英雄ID
     * @param status 状态
     * @return 是否成功
     */
    boolean updateHeroStatus(Long heroId, Integer status);

    /**
     * 批量导入英雄数据
     * @param heroes 英雄列表
     * @return 导入结果
     */
    Map<String, Object> batchImportHeroes(List<Hero> heroes);

    /**
     * 获取英雄选项列表（用于下拉框）
     * @return 英雄简要信息
     */
    List<Map<String, Object>> getHeroOptions();

    /**
     * 刷新英雄缓存
     */
    void refreshHeroCache();


    /**
     * 更新英雄信息
     * @param hero 英雄信息
     * @param token 令牌
     */
    void updateHero(Hero hero, String token);
}