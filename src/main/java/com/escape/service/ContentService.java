package com.escape.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.escape.entity.Content;

import java.util.List;
import java.util.Map;

/**
 * 内容服务接口
 *
 * @author escape
 * @since 2025-06-14
 */
public interface ContentService extends IService<Content> {

    /**
     * 获取内容详情（包含关联信息）
     * @param contentId 内容ID
     * @return 内容详情
     */
    Map<String, Object> getContentDetail(Long contentId);

    /**
     * 根据内容类型获取内容列表
     * @param contentType 内容类型：video/article/mixed
     * @return 内容列表
     */
    List<Content> getContentsByType(String contentType);

    /**
     * 获取热门内容
     * @param limit 数量限制
     * @return 热门内容列表
     */
    List<Content> getHotContents(Integer limit);

    /**
     * 获取精选内容
     * @param limit 数量限制
     * @return 精选内容列表
     */
    List<Content> getFeaturedContents(Integer limit);

    /**
     * 获取官方内容
     * @return 官方内容列表
     */
    List<Content> getOfficialContents();

    /**
     * 根据关联获取内容（英雄/地图/武器/点位）
     * @param heroId 英雄ID（可选）
     * @param mapId 地图ID（可选）
     * @param weaponId 武器ID（可选）
     * @param positionId 点位ID（可选）
     * @return 内容列表
     */
    List<Content> getContentsByRelation(Long heroId, Long mapId, Long weaponId, Long positionId);

    /**
     * 获取作者的内容
     * @param authorId 作者ID
     * @return 内容列表
     */
    List<Content> getContentsByAuthor(Long authorId);

    /**
     * 获取相关内容（基于标签）
     * @param contentId 内容ID
     * @param limit 数量限制
     * @return 相关内容列表
     */
    List<Map<String, Object>> getRelatedContents(Long contentId, Integer limit);

    /**
     * 搜索内容
     * @param page 分页参数
     * @param keyword 搜索关键词
     * @param contentType 内容类型（可选）
     * @return 分页结果
     */
    IPage<Content> searchContents(Page<Content> page, String keyword, String contentType);

    /**
     * 分页查询内容（支持多条件）
     * @param page 分页参数
     * @param params 查询参数
     * @return 分页结果
     */
    IPage<Content> getContentPage(Page<Content> page, Map<String, Object> params);

    /**
     * 增加浏览次数
     * @param contentId 内容ID
     * @return 是否成功
     */
    boolean incrementViewCount(Long contentId);

    /**
     * 更新点赞数
     * @param contentId 内容ID
     * @param delta 增量（1表示点赞，-1表示取消）
     * @return 是否成功
     */
    boolean updateLikeCount(Long contentId, Integer delta);

    /**
     * 更新收藏数
     * @param contentId 内容ID
     * @param delta 增量（1表示收藏，-1表示取消）
     * @return 是否成功
     */
    boolean updateCollectCount(Long contentId, Integer delta);

    /**
     * 创建内容
     * @param content 内容信息
     * @param tagIds 标签ID列表
     * @return 创建的内容ID
     */
    Long createContent(Content content, List<Long> tagIds);

    /**
     * 更新内容
     * @param content 内容信息
     * @param tagIds 标签ID列表
     * @return 是否成功
     */
    boolean updateContent(Content content, List<Long> tagIds);

    /**
     * 更新内容状态
     * @param contentId 内容ID
     * @param status 状态：0-待审核，1-已发布，2-已下架
     * @return 是否成功
     */
    boolean updateContentStatus(Long contentId, Integer status);

    /**
     * 设置内容为精选
     * @param contentId 内容ID
     * @param isFeatured 是否精选
     * @return 是否成功
     */
    boolean setContentFeatured(Long contentId, Boolean isFeatured);

    /**
     * 获取内容统计信息
     * @return 统计结果
     */
    Map<String, Object> getContentStatistics();

    /**
     * 批量导入内容
     * @param contents 内容列表
     * @return 导入结果
     */
    Map<String, Object> batchImportContents(List<Content> contents);

    /**
     * 刷新内容缓存
     */
    void refreshContentCache();
}