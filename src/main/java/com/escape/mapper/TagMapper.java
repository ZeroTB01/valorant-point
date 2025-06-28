package com.escape.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.escape.entity.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * 标签数据访问层
 *
 * @author escape
 * @since 2025-06-11
 */
@Mapper
public interface TagMapper extends BaseMapper<Tag> {

    /**
     * 根据标签类型查询标签列表
     */
    @Select("SELECT * FROM tags WHERE tag_type = #{tagType} " +
            "AND status = 1 ORDER BY hot_score DESC, sort_order")
    List<Tag> findByTagType(@Param("tagType") String tagType);

    /**
     * 查询热门标签
     */
    @Select("SELECT * FROM tags WHERE status = 1 " +
            "ORDER BY hot_score DESC LIMIT #{limit}")
    List<Tag> findHotTags(@Param("limit") Integer limit);

    /**
     * 根据标签名称查询（支持模糊查询）
     */
    @Select("SELECT * FROM tags WHERE tag_name LIKE CONCAT('%', #{keyword}, '%') " +
            "AND status = 1 ORDER BY hot_score DESC")
    List<Tag> searchByName(@Param("keyword") String keyword);

    /**
     * 更新标签热度分数
     */
    @Update("UPDATE tags SET hot_score = hot_score + #{delta} WHERE id = #{id}")
    int updateHotScore(@Param("id") Long id, @Param("delta") Integer delta);

    /**
     * 查询内容的标签
     */
    @Select("SELECT t.* FROM tags t " +
            "INNER JOIN content_tags ct ON t.id = ct.tag_id " +
            "WHERE ct.content_id = #{contentId} AND t.status = 1 " +
            "ORDER BY t.sort_order")
    List<Tag> findByContentId(@Param("contentId") Long contentId);

    /**
     * 批量查询内容的标签（优化N+1查询）
     */
    @Select("<script>" +
            "SELECT ct.content_id, t.* FROM tags t " +
            "INNER JOIN content_tags ct ON t.id = ct.tag_id " +
            "WHERE ct.content_id IN " +
            "<foreach collection='contentIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            " AND t.status = 1 ORDER BY ct.content_id, t.sort_order" +
            "</script>")
    List<Map<String, Object>> findByContentIds(@Param("contentIds") List<Long> contentIds);

    /**
     * 统计标签使用次数
     */
    @Select("SELECT t.id, t.tag_name, COUNT(ct.id) as use_count " +
            "FROM tags t " +
            "LEFT JOIN content_tags ct ON t.id = ct.tag_id " +
            "WHERE t.status = 1 " +
            "GROUP BY t.id, t.tag_name " +
            "ORDER BY use_count DESC LIMIT #{limit}")
    List<Map<String, Object>> statisticsTagUsage(@Param("limit") Integer limit);

    /**
     * 查询相关标签（基于共同出现）
     */
    @Select("SELECT t2.*, COUNT(*) as relate_count FROM tags t1 " +
            "INNER JOIN content_tags ct1 ON t1.id = ct1.tag_id " +
            "INNER JOIN content_tags ct2 ON ct1.content_id = ct2.content_id " +
            "INNER JOIN tags t2 ON ct2.tag_id = t2.id " +
            "WHERE t1.id = #{tagId} AND t2.id != #{tagId} AND t2.status = 1 " +
            "GROUP BY t2.id " +
            "ORDER BY relate_count DESC LIMIT #{limit}")
    List<Map<String, Object>> findRelatedTags(@Param("tagId") Long tagId,
                                              @Param("limit") Integer limit);
}