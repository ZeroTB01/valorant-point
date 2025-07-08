package com.escape.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.escape.common.ResultCode;
import com.escape.common.exception.BusinessException;
import com.escape.entity.Content;
import com.escape.mapper.*;
import com.escape.service.ContentService;
import com.escape.service.TagService;
import com.escape.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 内容服务实现类
 *
 * @author escape
 * @since 2025-06-14
 */
@Slf4j
@Service
public class ContentServiceImpl extends ServiceImpl<ContentMapper, Content> implements ContentService {

    @Autowired
    private ContentMapper contentMapper;

    @Autowired
    private ContentTagMapper contentTagMapper;

    @Autowired
    private TagService tagService;

    @Autowired
    private HeroMapper heroMapper;

    @Autowired
    private GameMapMapper gameMapMapper;

    @Autowired
    private WeaponMapper weaponMapper;

    @Autowired
    private PositionMapper positionMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisUtils redisUtils;

    private static final String CONTENT_CACHE_PREFIX = "content:";
    private static final String HOT_CONTENTS_KEY = "content:hot:";
    private static final String FEATURED_CONTENTS_KEY = "content:featured:";
    private static final long CACHE_EXPIRE_HOURS = 2;

    @Override
    public Map<String, Object> getContentDetail(Long contentId) {
        if (contentId == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "内容ID不能为空");
        }

        // 先从缓存获取
        String cacheKey = CONTENT_CACHE_PREFIX + "detail:" + contentId;
        String cachedData = redisUtils.get(cacheKey);
        if (StringUtils.hasText(cachedData)) {
            log.debug("从缓存获取内容详情: {}", contentId);
        }

        // 查询内容基本信息
        Content content = contentMapper.selectById(contentId);
        if (content == null || content.getDeleted() == 1) {
            throw new BusinessException(ResultCode.DATA_NOT_EXISTS.getCode(), "内容不存在");
        }

        // 增加浏览次数（异步执行）
        incrementViewCount(contentId);

        // 查询关联信息
        Map<String, Object> result = contentMapper.findContentDetail(contentId);
        if (result == null) {
            result = new HashMap<>();
            result.put("content", content);
        }

        // 查询标签
        List<?> tags = tagService.getContentTags(contentId);
        result.put("tags", tags);

        // 查询作者信息
        if (content.getAuthorId() != null) {
            result.put("author", userMapper.selectById(content.getAuthorId()));
        }

        // 缓存结果
        redisUtils.set(cacheKey, result.toString(), CACHE_EXPIRE_HOURS, TimeUnit.HOURS);

        return result;
    }

    @Override
    public List<Content> getContentsByType(String contentType) {
        if (!StringUtils.hasText(contentType)) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "内容类型不能为空");
        }

        // 验证类型是否合法
        if (!isValidContentType(contentType)) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "无效的内容类型");
        }

        return contentMapper.findByContentType(contentType);
    }

    @Override
    public List<Content> getHotContents(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10;
        }

        // 缓存键
        String cacheKey = HOT_CONTENTS_KEY + limit;

        // 尝试从缓存获取
        String cachedData = redisUtils.get(cacheKey);
        if (StringUtils.hasText(cachedData)) {
            log.debug("从缓存获取热门内容");
        }

        List<Content> hotContents = contentMapper.findHotContents(limit);

        // 缓存30分钟
        redisUtils.set(cacheKey, hotContents.toString(), 30, TimeUnit.MINUTES);

        return hotContents;
    }

    @Override
    public List<Content> getFeaturedContents(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10;
        }

        // 缓存键
        String cacheKey = FEATURED_CONTENTS_KEY + limit;

        // 尝试从缓存获取
        String cachedData = redisUtils.get(cacheKey);
        if (StringUtils.hasText(cachedData)) {
            log.debug("从缓存获取精选内容");
        }

        List<Content> featuredContents = contentMapper.findFeaturedContents(limit);

        // 缓存1小时
        redisUtils.set(cacheKey, featuredContents.toString(), 1, TimeUnit.HOURS);

        return featuredContents;
    }

    @Override
    public List<Content> getOfficialContents() {
        return contentMapper.findOfficialContents();
    }

    @Override
    public List<Content> getContentsByRelation(Long heroId, Long mapId, Long weaponId, Long positionId) {
        return contentMapper.findByRelation(heroId, mapId, weaponId, positionId);
    }

    @Override
    public List<Content> getContentsByAuthor(Long authorId) {
        if (authorId == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "作者ID不能为空");
        }

        return contentMapper.findByAuthor(authorId);
    }

    @Override
    public List<Map<String, Object>> getRelatedContents(Long contentId, Integer limit) {
        if (contentId == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "内容ID不能为空");
        }

        if (limit == null || limit <= 0) {
            limit = 5;
        }

        // 基于标签查找相关内容
        return contentMapper.findRelatedContentsByTags(contentId, limit);
    }

    @Override
    public IPage<Content> searchContents(Page<Content> page, String keyword, String contentType) {
        if (!StringUtils.hasText(keyword)) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "搜索关键词不能为空");
        }

        return contentMapper.searchContents(page, keyword.trim(), contentType);
    }

    @Override
    public IPage<Content> getContentPage(Page<Content> page, Map<String, Object> params) {
        QueryWrapper<Content> wrapper = new QueryWrapper<>();
        wrapper.eq("deleted", 0);

        if (params != null) {
            Object status = params.get("status");
            if (status != null) {
                wrapper.eq("status", status);
            }
            Object contentType = params.get("contentType");
            if (contentType != null && StringUtils.hasText(contentType.toString())) {
                wrapper.eq("content_type", contentType);
            }
            Object isFeatured = params.get("isFeatured");
            if (isFeatured != null) {
                wrapper.eq("is_featured", isFeatured);
            }

            Object isOfficial = params.get("isOfficial");
            if (isOfficial != null) {
                wrapper.eq("is_official", isOfficial);
            }

            Object authorId = params.get("authorId");
            if (authorId != null) {
                wrapper.eq("author_id", authorId);
            }

            Object heroId = params.get("heroId");
            if (heroId != null) {
                wrapper.eq("hero_id", heroId);
            }

            Object mapId = params.get("mapId");
            if (mapId != null) {
                wrapper.eq("map_id", mapId);
            }
        }

        wrapper.orderByDesc("is_featured", "view_count", "publish_time");

        return contentMapper.selectPage(page, wrapper);
    }

    @Override
    public boolean incrementViewCount(Long contentId) {
        try {
            // 使用Redis实现防刷机制
            String viewKey = "content:view:" + contentId + ":" + getCurrentHour();
            Long count = redisUtils.increment(viewKey);

            // 设置过期时间为1小时
            if (count == 1) {
                redisUtils.expire(viewKey, 1, TimeUnit.HOURS);
            }

            // 每10次浏览更新一次数据库
            if (count % 10 == 0) {
                contentMapper.incrementViewCount(contentId);
            }

            return true;
        } catch (Exception e) {
            log.error("增加浏览次数失败", e);
            return false;
        }
    }

    @Override
    public boolean updateLikeCount(Long contentId, Integer delta) {
        if (contentId == null || delta == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "参数不能为空");
        }

        int result = contentMapper.updateLikeCount(contentId, delta);

        if (result > 0) {
            // 清除相关缓存
            clearContentCache(contentId);
        }

        return result > 0;
    }

    @Override
    public boolean updateCollectCount(Long contentId, Integer delta) {
        if (contentId == null || delta == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "参数不能为空");
        }

        int result = contentMapper.updateCollectCount(contentId, delta);

        if (result > 0) {
            // 清除相关缓存
            clearContentCache(contentId);
        }

        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createContent(Content content, List<Long> tagIds) {
        // 验证内容信息
        validateContent(content);

        // 设置发布时间
        if (content.getStatus() == 1) {
            content.setPublishTime(LocalDateTime.now());
        }

        // 保存内容
        int result = contentMapper.insert(content);
        if (result <= 0) {
            throw new BusinessException(ResultCode.DATABASE_ERROR.getCode(), "保存内容失败");
        }

        // 设置标签
        if (tagIds != null && !tagIds.isEmpty()) {
            tagService.setContentTags(content.getId(), tagIds);
        }

        // 清除缓存
        refreshContentCache();

        return content.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateContent(Content content, List<Long> tagIds) {
        if (content == null || content.getId() == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "内容信息不能为空");
        }

        // 更新内容
        int result = contentMapper.updateById(content);
        if (result <= 0) {
            return false;
        }

        // 更新标签
        if (tagIds != null) {
            tagService.setContentTags(content.getId(), tagIds);
        }

        // 清除缓存
        clearContentCache(content.getId());

        return true;
    }

    @Override
    public boolean updateContentStatus(Long contentId, Integer status) {
        if (contentId == null || status == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "参数不能为空");
        }

        if (status < 0 || status > 2) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "状态值无效");
        }

        Content content = new Content();
        content.setId(contentId);
        content.setStatus(status);

        // 如果是发布状态，设置发布时间
        if (status == 1) {
            content.setPublishTime(LocalDateTime.now());
        }

        int result = contentMapper.updateById(content);

        if (result > 0) {
            clearContentCache(contentId);
        }

        return result > 0;
    }

    @Override
    public boolean setContentFeatured(Long contentId, Boolean isFeatured) {
        if (contentId == null || isFeatured == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "参数不能为空");
        }

        Content content = new Content();
        content.setId(contentId);
        content.setIsFeatured(isFeatured ? 1 : 0);

        int result = contentMapper.updateById(content);

        if (result > 0) {
            // 清除精选内容缓存
            redisUtils.delete(redisUtils.keys(FEATURED_CONTENTS_KEY + "*"));
            clearContentCache(contentId);
        }

        return result > 0;
    }

    @Override
    public Map<String, Object> getContentStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // 内容类型统计
        List<Map<String, Object>> typeStats = contentMapper.statisticsByType();
        stats.put("typeStatistics", typeStats);

        // 总内容数
        QueryWrapper<Content> totalWrapper = new QueryWrapper<>();
        totalWrapper.eq("deleted", 0);
        long totalContents = contentMapper.selectCount(totalWrapper);
        stats.put("totalContents", totalContents);

        // 已发布内容数
        QueryWrapper<Content> publishedWrapper = new QueryWrapper<>();
        publishedWrapper.eq("deleted", 0).eq("status", 1);
        long publishedContents = contentMapper.selectCount(publishedWrapper);
        stats.put("publishedContents", publishedContents);

        // 精选内容数
        QueryWrapper<Content> featuredWrapper = new QueryWrapper<>();
        featuredWrapper.eq("deleted", 0).eq("is_featured", 1);
        long featuredContents = contentMapper.selectCount(featuredWrapper);
        stats.put("featuredContents", featuredContents);

        return stats;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> batchImportContents(List<Content> contents) {
        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int failCount = 0;
        List<String> errors = new ArrayList<>();

        for (int i = 0; i < contents.size(); i++) {
            Content content = contents.get(i);
            try {
                validateContent(content);
                contentMapper.insert(content);
                successCount++;
            } catch (Exception e) {
                failCount++;
                errors.add(String.format("第%d条数据导入失败：%s", i + 1, e.getMessage()));
                log.error("导入内容失败", e);
            }
        }

        result.put("totalCount", contents.size());
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("errors", errors);

        if (successCount > 0) {
            refreshContentCache();
        }

        return result;
    }

    @Override
    public void refreshContentCache() {
        log.info("刷新内容缓存");
        // 删除热门内容缓存
        redisUtils.delete(redisUtils.keys(HOT_CONTENTS_KEY + "*"));
        // 删除精选内容缓存
        redisUtils.delete(redisUtils.keys(FEATURED_CONTENTS_KEY + "*"));
        // 删除所有内容详情缓存
        redisUtils.delete(redisUtils.keys(CONTENT_CACHE_PREFIX + "*"));
    }

    /**
     * 清除单个内容的缓存
     */
    private void clearContentCache(Long contentId) {
        redisUtils.delete(CONTENT_CACHE_PREFIX + "detail:" + contentId);
        // 清除热门和精选缓存
        redisUtils.delete(redisUtils.keys(HOT_CONTENTS_KEY + "*"));
        redisUtils.delete(redisUtils.keys(FEATURED_CONTENTS_KEY + "*"));
    }

    /**
     * 验证内容类型是否合法
     */
    private boolean isValidContentType(String contentType) {
        return "video".equals(contentType) || "article".equals(contentType) || "mixed".equals(contentType);
    }

    /**
     * 验证内容信息
     */
    private void validateContent(Content content) {
        if (content == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "内容信息不能为空");
        }
        if (!StringUtils.hasText(content.getContentType())) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "内容类型不能为空");
        }
        if (!isValidContentType(content.getContentType())) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "无效的内容类型");
        }
        if (!StringUtils.hasText(content.getTitle())) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "标题不能为空");
        }
        if (content.getTitle().length() > 200) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "标题不能超过200个字符");
        }
    }

    /**
     * 获取当前小时（用于浏览计数）
     */
    private String getCurrentHour() {
        return String.valueOf(System.currentTimeMillis() / (1000 * 60 * 60));
    }
}