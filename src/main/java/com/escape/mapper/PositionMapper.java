package com.escape.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.escape.entity.Position;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * 点位数据访问层
 *
 * @author escape
 * @since 2025-06-11
 */
@Mapper
public interface PositionMapper extends BaseMapper<Position> {

    /**
     * 三级筛选查询（地图 + 英雄 + 攻防）
     * heroId为null时查询通用点位
     */
    @Select("<script>" +
            "SELECT * FROM positions WHERE map_id = #{mapId} " +
            "<if test='heroId != null'>" +
            "AND hero_id = #{heroId} " +
            "</if>" +
            "<if test='heroId == null'>" +
            "AND hero_id IS NULL " +
            "</if>" +
            "AND side = #{side} AND status = 1 AND deleted = 0 " +
            "ORDER BY difficulty, sort_order" +
            "</script>")
    List<Position> findByThreeFilter(@Param("mapId") Long mapId,
                                     @Param("heroId") Long heroId,
                                     @Param("side") String side);

    /**
     * 根据地图和站点查询点位
     */
    @Select("SELECT * FROM positions WHERE map_id = #{mapId} " +
            "AND site = #{site} AND status = 1 AND deleted = 0 " +
            "ORDER BY position_type, sort_order")
    List<Position> findByMapAndSite(@Param("mapId") Long mapId,
                                    @Param("site") String site);

    /**
     * 根据点位类型查询
     */
    @Select("SELECT * FROM positions WHERE position_type = #{positionType} " +
            "AND status = 1 AND deleted = 0 ORDER BY view_count DESC LIMIT #{limit}")
    List<Position> findByPositionType(@Param("positionType") String positionType,
                                      @Param("limit") Integer limit);

    /**
     * 查询热门点位（按浏览量）
     */
    @Select("SELECT p.*, m.map_name, h.hero_name FROM positions p " +
            "LEFT JOIN maps m ON p.map_id = m.id " +
            "LEFT JOIN heroes h ON p.hero_id = h.id " +
            "WHERE p.status = 1 AND p.deleted = 0 " +
            "ORDER BY p.view_count DESC LIMIT #{limit}")
    List<Map<String, Object>> findHotPositions(@Param("limit") Integer limit);

    /**
     * 根据难度查询点位
     */
    @Select("SELECT * FROM positions WHERE difficulty = #{difficulty} " +
            "AND status = 1 AND deleted = 0 ORDER BY view_count DESC")
    List<Position> findByDifficulty(@Param("difficulty") Integer difficulty);

    /**
     * 更新浏览次数
     */
    @Update("UPDATE positions SET view_count = view_count + 1 WHERE id = #{id}")
    int incrementViewCount(@Param("id") Long id);

    /**
     * 统计各地图的点位数量
     */
    @Select("SELECT map_id, COUNT(*) as count FROM positions " +
            "WHERE status = 1 AND deleted = 0 GROUP BY map_id")
    List<Map<String, Object>> countByMap();

    /**
     * 统计各英雄的点位数量
     */
    @Select("SELECT hero_id, COUNT(*) as count FROM positions " +
            "WHERE hero_id IS NOT NULL AND status = 1 AND deleted = 0 " +
            "GROUP BY hero_id")
    List<Map<String, Object>> countByHero();

    /**
     * 相关推荐点位（同地图同类型）
     */
    @Select("SELECT * FROM positions WHERE map_id = #{mapId} " +
            "AND position_type = #{positionType} AND id != #{excludeId} " +
            "AND status = 1 AND deleted = 0 " +
            "ORDER BY view_count DESC LIMIT #{limit}")
    List<Position> findRelatedPositions(@Param("mapId") Long mapId,
                                        @Param("positionType") String positionType,
                                        @Param("excludeId") Long excludeId,
                                        @Param("limit") Integer limit);

    /**
     * 分页查询点位（复杂条件建议在XML中实现）
     */
    IPage<Position> selectPageWithCondition(Page<Position> page,
                                            @Param("mapId") Long mapId,
                                            @Param("heroId") Long heroId,
                                            @Param("side") String side,
                                            @Param("positionType") String positionType,
                                            @Param("difficulty") Integer difficulty);
}