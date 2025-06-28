package com.escape.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.escape.entity.ContentTag;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

/**
 * 内容标签关联数据访问层
 *
 * @author escape
 * @since 2025-06-11
 */
@Mapper
public interface ContentTagMapper extends BaseMapper<ContentTag> {

    /**
     * 批量插入内容标签关联
     */
    @Insert("<script>" +
            "INSERT INTO content_tags(content_id, tag_id) VALUES " +
            "<foreach collection='tagIds' item='tagId' separator=','>" +
            "(#{contentId}, #{tagId})" +
            "</foreach>" +
            "</script>")
    int batchInsert(@Param("contentId") Long contentId,
                    @Param("tagIds") List<Long> tagIds);

    /**
     * 删除内容的所有标签关联
     */
    @Delete("DELETE FROM content_tags WHERE content_id = #{contentId}")
    int deleteByContentId(@Param("contentId") Long contentId);

    /**
     * 删除标签的所有内容关联
     */
    @Delete("DELETE FROM content_tags WHERE tag_id = #{tagId}")
    int deleteByTagId(@Param("tagId") Long tagId);

    /**
     * 查询拥有指定标签的内容ID列表
     */
    @Select("SELECT content_id FROM content_tags WHERE tag_id = #{tagId}")
    List<Long> findContentIdsByTagId(@Param("tagId") Long tagId);

    /**
     * 查询拥有多个标签的内容ID（交集）
     */
    @Select("<script>" +
            "SELECT content_id FROM content_tags " +
            "WHERE tag_id IN " +
            "<foreach collection='tagIds' item='tagId' open='(' separator=',' close=')'>" +
            "#{tagId}" +
            "</foreach>" +
            " GROUP BY content_id " +
            "HAVING COUNT(DISTINCT tag_id) = #{tagCount}" +
            "</script>")
    List<Long> findContentIdsByTagIds(@Param("tagIds") List<Long> tagIds,
                                      @Param("tagCount") Integer tagCount);

    /**
     * 统计内容的标签数量
     */
    @Select("SELECT content_id, COUNT(*) as tag_count FROM content_tags " +
            "GROUP BY content_id")
    List<Map<String, Object>> countTagsByContent();

    /**
     * 检查内容是否有指定标签
     */
    @Select("SELECT COUNT(*) FROM content_tags " +
            "WHERE content_id = #{contentId} AND tag_id = #{tagId}")
    boolean existsByContentIdAndTagId(@Param("contentId") Long contentId,
                                      @Param("tagId") Long tagId);

    /**
     * 更新内容的标签（先删除后插入）
     */
    default int updateContentTags(Long contentId, List<Long> tagIds) {
        // 先删除原有标签
        deleteByContentId(contentId);
        // 如果有新标签，批量插入
        if (tagIds != null && !tagIds.isEmpty()) {
            return batchInsert(contentId, tagIds);
        }
        return 0;
    }
}