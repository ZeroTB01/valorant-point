package com.escape.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.escape.entity.Content;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * 内容数据访问层
 *
 * @author escape
 * @since 2025-06-11
 */
@Mapper
public interface ContentMapper extends BaseMapper<Content> {

    /**
     * 根据内容类型查询列表
     */
    @Select("SELECT * FROM contents WHERE content_type = #{contentType} " +
            "AND status = 1 AND deleted = 0 ORDER BY sort_order, publish_time DESC")
    List<Content> findByContentType(@Param("contentType") String contentType);

    /**
     * 查询热门内容（按浏览量）
     */
    @Select("SELECT * FROM contents WHERE status = 1 AND deleted = 0 " +
            "ORDER BY view_count DESC LIMIT #{limit}")
    List<Content> findHotContents(@Param("limit") Integer limit);

    /**
     * 查询精选内容
     */
    @Select("SELECT * FROM contents WHERE is_featured = 1 " +
            "AND status = 1 AND deleted = 0 " +
            "ORDER BY sort_order, publish_time DESC LIMIT #{limit}")
    List<Content> findFeaturedContents(@Param("limit") Integer limit);

    /**
     * 查询官方内容
     */
    @Select("SELECT * FROM contents WHERE is_official = 1 " +
            "AND status = 1 AND deleted = 0 " +
            "ORDER BY publish_time DESC")
    List<Content> findOfficialContents();

    /**
     * 根据关联查询内容（英雄/地图/武器/点位）
     */
    @Select("<script>" +
            "SELECT * FROM contents WHERE status = 1 AND deleted = 0 " +
            "<if test='heroId != null'>AND hero_id = #{heroId}</if> " +
            "<if test='mapId != null'>AND map_id = #{mapId}</if> " +
            "<if test='weaponId != null'>AND weapon_id = #{weaponId}</if> " +
            "<if test='positionId != null'>AND position_id = #{positionId}</if> " +
            "ORDER BY view_count DESC" +
            "</script>")
    List<Content> findByRelation(@Param("heroId") Long heroId,
                                 @Param("mapId") Long mapId,
                                 @Param("weaponId") Long weaponId,
                                 @Param("positionId") Long positionId);

    /**
     * 更新浏览次数
     */
    @Update("UPDATE contents SET view_count = view_count + 1 WHERE id = #{id}")
    int incrementViewCount(@Param("id") Long id);

    /**
     * 更新点赞次数
     */
    @Update("UPDATE contents SET like_count = like_count + #{delta} WHERE id = #{id}")
    int updateLikeCount(@Param("id") Long id, @Param("delta") Integer delta);

    /**
     * 更新收藏次数
     */
    @Update("UPDATE contents SET collect_count = collect_count + #{delta} WHERE id = #{id}")
    int updateCollectCount(@Param("id") Long id, @Param("delta") Integer delta);

    /**
     * 查询作者的内容
     */
    @Select("SELECT * FROM contents WHERE author_id = #{authorId} " +
            "AND status = 1 AND deleted = 0 ORDER BY publish_time DESC")
    List<Content> findByAuthor(@Param("authorId") Long authorId);

    /**
     * 统计内容数据
     */
    @Select("SELECT content_type, COUNT(*) as count, " +
            "SUM(view_count) as total_views, " +
            "AVG(view_count) as avg_views " +
            "FROM contents WHERE status = 1 AND deleted = 0 " +
            "GROUP BY content_type")
    List<Map<String, Object>> statisticsByType();

    /**
     * 查询相关内容（基于标签，建议在XML实现）
     */
    List<Map<String, Object>> findRelatedContentsByTags(@Param("contentId") Long contentId,
                                                        @Param("limit") Integer limit);

    /**
     * 搜索内容（标题和描述，建议在XML实现）
     */
    IPage<Content> searchContents(Page<Content> page,
                                  @Param("keyword") String keyword,
                                  @Param("contentType") String contentType);

    /**
     * 查询内容详情（包含关联信息）
     */
    Map<String, Object> findContentDetail(@Param("id") Long id);
}