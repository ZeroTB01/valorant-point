package com.escape.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.escape.common.ResultCode;
import com.escape.common.exception.BusinessException;
import com.escape.entity.ContentTag;
import com.escape.entity.Tag;
import com.escape.mapper.ContentTagMapper;
import com.escape.mapper.TagMapper;
import com.escape.service.TagService;
import com.escape.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 标签服务实现类
 *
 * @author escape
 * @since 2025-06-14
 */
@Slf4j
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

    @Autowired
    private TagMapper tagMapper;

    @Autowired
    private ContentTagMapper contentTagMapper;

    @Autowired
    private RedisUtils redisUtils;

    private static final String TAG_CACHE_PREFIX = "tag:";
    private static final String HOT_TAGS_CACHE_KEY = "tag:hot:";
    private static final String TAG_TYPES_CACHE_KEY = "tag:types";
    private static final long CACHE_EXPIRE_HOURS = 2;

    @Override
    public List<Tag> getTagsByType(String tagType) {
        if (!StringUtils.hasText(tagType)) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "标签类型不能为空");
        }

        return tagMapper.findByTagType(tagType);
    }

    @Override
    public List<Tag> getHotTags(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10;
        }

        // 尝试从缓存获取
        String cacheKey = HOT_TAGS_CACHE_KEY + limit;
        String cachedData = redisUtils.get(cacheKey);
        if (StringUtils.hasText(cachedData)) {
            log.debug("从缓存获取热门标签");
        }

        List<Tag> hotTags = tagMapper.findHotTags(limit);

        // 缓存结果
        redisUtils.set(cacheKey, hotTags.toString(), 30, TimeUnit.MINUTES);

        return hotTags;
    }

    @Override
    public List<Tag> searchTags(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return new ArrayList<>();
        }

        return tagMapper.searchByName(keyword.trim());
    }

    @Override
    public List<Tag> getContentTags(Long contentId) {
        if (contentId == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "内容ID不能为空");
        }

        return tagMapper.findByContentId(contentId);
    }

    @Override
    public Map<Long, List<Tag>> getContentTagsMap(List<Long> contentIds) {
        if (contentIds == null || contentIds.isEmpty()) {
            return new HashMap<>();
        }

        // 批量查询
        List<Map<String, Object>> results = tagMapper.findByContentIds(contentIds);

        // 按内容ID分组
        Map<Long, List<Tag>> contentTagsMap = new HashMap<>();
        for (Map<String, Object> result : results) {
            Long contentId = ((Number) result.get("content_id")).longValue();

            Tag tag = new Tag();
            tag.setId(((Number) result.get("id")).longValue());
            tag.setTagName((String) result.get("tag_name"));
            tag.setTagType((String) result.get("tag_type"));
            tag.setDescription((String) result.get("description"));
            tag.setColor((String) result.get("color"));
            tag.setHotScore(((Number) result.get("hot_score")).intValue());

            contentTagsMap.computeIfAbsent(contentId, k -> new ArrayList<>()).add(tag);
        }

        return contentTagsMap;
    }

    @Override
    public List<Map<String, Object>> getRelatedTags(Long tagId, Integer limit) {
        if (tagId == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "标签ID不能为空");
        }

        if (limit == null || limit <= 0) {
            limit = 5;
        }

        return tagMapper.findRelatedTags(tagId, limit);
    }

    @Override
    public List<Map<String, Object>> getTagUsageStatistics(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 20;
        }

        return tagMapper.statisticsTagUsage(limit);
    }

    @Override
    public IPage<Tag> getTagPage(Page<Tag> page, String tagType, String keyword) {
        QueryWrapper<Tag> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1);

        if (StringUtils.hasText(tagType)) {
            wrapper.eq("tag_type", tagType);
        }

        if (StringUtils.hasText(keyword)) {
            wrapper.like("tag_name", keyword);
        }

        wrapper.orderByDesc("hot_score", "sort_order");

        return tagMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createTag(Tag tag) {
        // 验证标签信息
        validateTag(tag);

        // 检查标签名是否已存在
        QueryWrapper<Tag> wrapper = new QueryWrapper<>();
        wrapper.eq("tag_name", tag.getTagName());
        Tag existTag = tagMapper.selectOne(wrapper);
        if (existTag != null) {
            throw new BusinessException(ResultCode.DATA_ALREADY_EXISTS.getCode(), "标签名已存在");
        }

        // 保存标签
        int result = tagMapper.insert(tag);
        if (result <= 0) {
            throw new BusinessException(ResultCode.DATABASE_ERROR.getCode(), "保存标签失败");
        }

        // 清除缓存
        refreshTagCache();

        return true;
    }

    @Override
    public boolean updateTagHotScore(Long tagId, Integer delta) {
        if (tagId == null || delta == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "参数不能为空");
        }

        int result = tagMapper.updateHotScore(tagId, delta);

        if (result > 0) {
            // 清除热门标签缓存
            redisUtils.delete(redisUtils.keys(HOT_TAGS_CACHE_KEY + "*"));
        }

        return result > 0;
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> batchCreateTags(List<Tag> tags) {
        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int failCount = 0;
        List<String> errors = new ArrayList<>();

        for (Tag tag : tags) {
            try {
                validateTag(tag);

                // 检查是否已存在
                QueryWrapper<Tag> wrapper = new QueryWrapper<>();
                wrapper.eq("tag_name", tag.getTagName());
                Tag existTag = tagMapper.selectOne(wrapper);
                if (existTag != null) {
                    failCount++;
                    errors.add("标签已存在: " + tag.getTagName());
                    continue;
                }

                int insertResult = tagMapper.insert(tag);
                if (insertResult > 0) {
                    successCount++;
                } else {
                    failCount++;
                    errors.add("标签插入失败: " + tag.getTagName());
                }
            } catch (Exception e) {
                failCount++;
                errors.add("标签创建失败: " + tag.getTagName() + ", 原因: " + e.getMessage());
                log.error("创建标签失败: {}", tag.getTagName(), e);
            }
        }

        result.put("totalCount", tags.size());
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("errors", errors);

        // 刷新缓存
        if (successCount > 0) {
            refreshTagCache();
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setContentTags(Long contentId, List<Long> tagIds) {
        if (contentId == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "内容ID不能为空");
        }

        // 更新内容标签（先删除后插入）
        int result = contentTagMapper.updateContentTags(contentId, tagIds);

        // 更新标签热度
        if (tagIds != null && !tagIds.isEmpty()) {
            for (Long tagId : tagIds) {
                updateTagHotScore(tagId, 1);
            }
        }

        return result >= 0;
    }

    @Override
    public List<Map<String, Object>> getTagTypes() {
        // 从缓存获取
        String cachedData = redisUtils.get(TAG_TYPES_CACHE_KEY);
        if (StringUtils.hasText(cachedData)) {
            log.debug("从缓存获取标签类型");
        }

        List<Map<String, Object>> types = Arrays.asList(
                Map.of("value", "hero", "label", "英雄相关", "color", "#1890ff"),
                Map.of("value", "map", "label", "地图相关", "color", "#52c41a"),
                Map.of("value", "skill", "label", "技能技巧", "color", "#faad14"),
                Map.of("value", "strategy", "label", "战术策略", "color", "#722ed1"),
                Map.of("value", "difficulty", "label", "难度等级", "color", "#f5222d")
        );

        // 缓存结果
        redisUtils.set(TAG_TYPES_CACHE_KEY, types.toString(), CACHE_EXPIRE_HOURS, TimeUnit.HOURS);

        return types;
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean mergeTags(Long sourceTagId, Long targetTagId) {
        if (sourceTagId == null || targetTagId == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "标签ID不能为空");
        }

        if (sourceTagId.equals(targetTagId)) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "源标签和目标标签不能相同");
        }

        // 查询源标签和目标标签
        Tag sourceTag = tagMapper.selectById(sourceTagId);
        Tag targetTag = tagMapper.selectById(targetTagId);

        if (sourceTag == null || targetTag == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXISTS.getCode(), "标签不存在");
        }

        // 获取源标签的所有内容
        List<Long> contentIds = contentTagMapper.findContentIdsByTagId(sourceTagId);

        // 为每个内容添加目标标签（如果还没有）
        for (Long contentId : contentIds) {
            if (!contentTagMapper.existsByContentIdAndTagId(contentId, targetTagId)) {
                // ✅ 修复：创建 ContentTag 对象而不是传递 null
                ContentTag contentTag = new ContentTag();
                contentTag.setContentId(contentId);
                contentTag.setTagId(targetTagId);
                contentTagMapper.insert(contentTag);
            }
        }

        // 删除源标签的所有关联
        contentTagMapper.deleteByTagId(sourceTagId);

        // 更新目标标签的热度
        updateTagHotScore(targetTagId, sourceTag.getHotScore());

        // 删除源标签
        tagMapper.deleteById(sourceTagId);

        // 刷新缓存
        refreshTagCache();

        log.info("标签合并成功: {} -> {}", sourceTag.getTagName(), targetTag.getTagName());
        return true;
    }

    @Override
    public void refreshTagCache() {
        log.info("刷新标签缓存");
        // 删除热门标签缓存
        redisUtils.delete(redisUtils.keys(HOT_TAGS_CACHE_KEY + "*"));
        // 删除标签类型缓存
        redisUtils.delete(TAG_TYPES_CACHE_KEY);
        // 删除其他标签相关缓存
        redisUtils.delete(redisUtils.keys(TAG_CACHE_PREFIX + "*"));
    }

    /**
     * 验证标签信息
     */
    private void validateTag(Tag tag) {
        if (tag == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "标签信息不能为空");
        }
        if (!StringUtils.hasText(tag.getTagName())) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "标签名称不能为空");
        }
        if (tag.getTagName().length() > 20) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "标签名称不能超过20个字符");
        }
    }
}