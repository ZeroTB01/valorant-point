package com.escape.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.escape.entity.Tag;

import java.util.List;
import java.util.Map;

/**
 * 标签服务接口
 *
 * @author escape
 * @since 2025-06-14
 */
public interface TagService extends IService<Tag> {

    /**
     * 根据标签类型获取标签列表
     * @param tagType 标签类型
     * @return 标签列表
     */
    List<Tag> getTagsByType(String tagType);

    /**
     * 获取热门标签
     * @param limit 数量限制
     * @return 热门标签列表
     */
    List<Tag> getHotTags(Integer limit);

    /**
     * 搜索标签（支持模糊查询）
     * @param keyword 关键词
     * @return 标签列表
     */
    List<Tag> searchTags(String keyword);

    /**
     * 获取内容的标签
     * @param contentId 内容ID
     * @return 标签列表
     */
    List<Tag> getContentTags(Long contentId);

    /**
     * 批量获取内容的标签（优化N+1查询）
     * @param contentIds 内容ID列表
     * @return 内容ID到标签列表的映射
     */
    Map<Long, List<Tag>> getContentTagsMap(List<Long> contentIds);

    /**
     * 获取相关标签（基于共同出现）
     * @param tagId 标签ID
     * @param limit 数量限制
     * @return 相关标签列表
     */
    List<Map<String, Object>> getRelatedTags(Long tagId, Integer limit);

    /**
     * 获取标签使用统计
     * @param limit 数量限制
     * @return 统计结果
     */
    List<Map<String, Object>> getTagUsageStatistics(Integer limit);

    /**
     * 分页查询标签
     * @param page 分页参数
     * @param tagType 标签类型（可选）
     * @param keyword 搜索关键词（可选）
     * @return 分页结果
     */
    IPage<Tag> getTagPage(Page<Tag> page, String tagType, String keyword);

    /**
     * 创建标签
     * @param tag 标签信息
     * @return 是否成功
     */
    boolean createTag(Tag tag);

    /**
     * 更新标签热度
     * @param tagId 标签ID
     * @param delta 热度增量（可以为负）
     * @return 是否成功
     */
    boolean updateTagHotScore(Long tagId, Integer delta);

    /**
     * 批量创建标签
     * @param tags 标签列表
     * @return 创建结果
     */
    Map<String, Object> batchCreateTags(List<Tag> tags);

    /**
     * 为内容设置标签
     * @param contentId 内容ID
     * @param tagIds 标签ID列表
     * @return 是否成功
     */
    boolean setContentTags(Long contentId, List<Long> tagIds);

    /**
     * 获取所有标签类型
     * @return 标签类型列表
     */
    List<Map<String, Object>> getTagTypes();

    /**
     * 合并标签（将源标签的所有关联转移到目标标签）
     * @param sourceTagId 源标签ID
     * @param targetTagId 目标标签ID
     * @return 是否成功
     */
    boolean mergeTags(Long sourceTagId, Long targetTagId);

    /**
     * 刷新标签缓存
     */
    void refreshTagCache();
}