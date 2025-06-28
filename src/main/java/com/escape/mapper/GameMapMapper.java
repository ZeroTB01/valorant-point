package com.escape.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.escape.entity.GameMap;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 游戏地图数据访问层
 *
 * @author escape
 * @since 2025-06-11
 */
@Mapper
public interface GameMapMapper extends BaseMapper<GameMap> {

    /**
     * 根据地图标识查询地图
     */
    @Select("SELECT * FROM maps WHERE map_key = #{mapKey} AND deleted = 0")
    GameMap findByMapKey(@Param("mapKey") String mapKey);

    /**
     * 根据地图类型查询地图列表
     */
    @Select("SELECT * FROM maps WHERE map_type = #{mapType} " +
            "AND status = 1 AND deleted = 0 ORDER BY sort_order")
    List<GameMap> findByMapType(@Param("mapType") String mapType);

    /**
     * 查询所有启用的地图
     */
    @Select("SELECT * FROM maps WHERE status = 1 AND deleted = 0 ORDER BY sort_order")
    List<GameMap> findAllEnabled();

    /**
     * 根据站点数量查询地图
     */
    @Select("SELECT * FROM maps WHERE site_count = #{siteCount} " +
            "AND status = 1 AND deleted = 0 ORDER BY sort_order")
    List<GameMap> findBySiteCount(@Param("siteCount") Integer siteCount);

    /**
     * 更新地图状态
     */
    @Update("UPDATE maps SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 查询地图简要信息（用于下拉选择等场景）
     */
    @Select("SELECT id, map_key, map_name FROM maps " +
            "WHERE status = 1 AND deleted = 0 ORDER BY sort_order")
    List<GameMap> findMapOptions();
}