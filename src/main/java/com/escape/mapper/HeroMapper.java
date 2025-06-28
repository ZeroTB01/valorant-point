package com.escape.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.escape.entity.Hero;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * 英雄数据访问层
 *
 * @author escape
 * @since 2025-06-11
 */
@Mapper
public interface HeroMapper extends BaseMapper<Hero> {

    /**
     * 根据英雄标识查询英雄
     */
    @Select("SELECT * FROM heroes WHERE hero_key = #{heroKey} AND deleted = 0")
    Hero findByHeroKey(@Param("heroKey") String heroKey);

    /**
     * 根据英雄类型查询英雄列表
     */
    @Select("SELECT * FROM heroes WHERE hero_type = #{heroType} AND deleted = 0 ORDER BY sort_order")
    List<Hero> findByHeroType(@Param("heroType") String heroType);

    /**
     * 查询所有启用的英雄
     */
    @Select("SELECT * FROM heroes WHERE status = 1 AND deleted = 0 ORDER BY sort_order")
    List<Hero> findAllEnabled();

    /**
     * 统计各类型英雄数量
     */
    @Select("SELECT hero_type, COUNT(*) as count FROM heroes " +
            "WHERE deleted = 0 AND status = 1 " +
            "GROUP BY hero_type")
    List<Map<String, Object>> countByHeroType();

    /**
     * 按难度等级查询英雄
     */
    @Select("SELECT * FROM heroes WHERE difficulty = #{difficulty} " +
            "AND status = 1 AND deleted = 0 ORDER BY sort_order")
    List<Hero> findByDifficulty(@Param("difficulty") Integer difficulty);

    /**
     * 更新英雄状态
     */
    @Update("UPDATE heroes SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 分页查询英雄（带条件）
     * 建议在XML中实现，这里仅作示例
     */
    IPage<Hero> selectPageWithCondition(Page<Hero> page,
                                        @Param("heroType") String heroType,
                                        @Param("difficulty") Integer difficulty);
}