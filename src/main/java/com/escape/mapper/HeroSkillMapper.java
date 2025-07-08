package com.escape.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.escape.entity.HeroSkill;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 英雄技能数据访问层
 *
 * @author escape
 * @since 2025-06-11
 */
@Mapper
public interface HeroSkillMapper extends BaseMapper<HeroSkill> {

    /**
     * 根据英雄ID查询技能列表
     */
    @Select("SELECT * FROM hero_skills WHERE hero_id = #{heroId} ORDER BY sort_order")
    List<HeroSkill> findByHeroId(@Param("heroId") Long heroId);

    /**
     * 根据英雄ID和技能键位查询技能
     */
    @Select("SELECT * FROM hero_skills WHERE hero_id = #{heroId} AND skill_key = #{skillKey}")
    HeroSkill findByHeroIdAndSkillKey(@Param("heroId") Long heroId,
                                      @Param("skillKey") String skillKey);

    /**
     * 批量查询多个英雄的技能
     */
    @Select("<script>" +
            "SELECT * FROM hero_skills WHERE hero_id IN " +
            "<foreach collection='heroIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            " ORDER BY hero_id, sort_order" +
            "</script>")
    List<HeroSkill> findByHeroIds(@Param("heroIds") List<Long> heroIds);

    /**
     * 更新技能描述
     */
    @Update("UPDATE hero_skills SET description = #{description}, " +
            "update_time = NOW() WHERE id = #{id}")
    int updateDescription(@Param("id") Long id,
                          @Param("description") String description);

    /**
     * 更新技能使用技巧
     */
    @Update("UPDATE hero_skills SET tips = #{tips}, " +
            "update_time = NOW() WHERE id = #{id}")
    int updateTips(@Param("id") Long id, @Param("tips") String tips);

    /**
     * 删除应英雄技能
     */
    @Delete("DELETE FROM hero_skills WHERE hero_id = #{heroId}")
    int deleteByHeroId(@Param("heroId") Long heroId);
}