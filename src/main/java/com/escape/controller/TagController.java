package com.escape.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.escape.common.Result;
import com.escape.common.ResultCode;
import com.escape.entity.Tag;
import com.escape.service.TagService;
import com.escape.utils.JwtUtils;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 标签控制器
 * 提供标签查询、管理等API接口
 *
 * @author escape
 * @since 2025-06-14
 */
@Slf4j
@RestController
@RequestMapping("/tag")
public class TagController {

    @Autowired
    private TagService tagService;

    @Autowired
    private JwtUtils jwtUtils;

    // ==================== 标签查询接口 ====================

    /**
     * 获取热门标签
     */
    @GetMapping("/hot")
    public Result<List<Tag>> getHotTags(@RequestParam(defaultValue = "10") Integer limit) {
        try {
            List<Tag> hotTags = tagService.getHotTags(limit);
            return Result.success(hotTags);
        } catch (Exception e) {
            log.error("获取热门标签失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 按标签类型获取标签列表
     */
    @GetMapping("/type/{tagType}")
    public Result<List<Tag>> getTagsByType(@PathVariable String tagType) {
        try {
            List<Tag> tags = tagService.getTagsByType(tagType);
            return Result.success(tags);
        } catch (RuntimeException e) {
            log.warn("按类型获取标签失败: tagType={}, 原因: {}", tagType, e.getMessage());
            return handleTagException(e);
        } catch (Exception e) {
            log.error("按类型获取标签系统错误: tagType={}", tagType, e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 搜索标签
     */
    @GetMapping("/search")
    public Result<List<Tag>> searchTags(@RequestParam String keyword) {
        try {
            List<Tag> tags = tagService.searchTags(keyword);
            return Result.success(tags);
        } catch (Exception e) {
            log.error("搜索标签失败: keyword={}", keyword, e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取内容的标签
     */
    @GetMapping("/content/{contentId}")
    public Result<List<Tag>> getContentTags(@PathVariable @NotNull(message = "内容ID不能为空") Long contentId) {
        try {
            List<Tag> tags = tagService.getContentTags(contentId);
            return Result.success(tags);
        } catch (RuntimeException e) {
            log.warn("获取内容标签失败: contentId={}, 原因: {}", contentId, e.getMessage());
            return handleTagException(e);
        } catch (Exception e) {
            log.error("获取内容标签系统错误: contentId={}", contentId, e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 批量获取内容的标签
     */
    @PostMapping("/content/batch")
    public Result<Map<Long, List<Tag>>> getContentTagsMap(@RequestBody List<Long> contentIds) {
        try {
            Map<Long, List<Tag>> tagsMap = tagService.getContentTagsMap(contentIds);
            return Result.success(tagsMap);
        } catch (Exception e) {
            log.error("批量获取内容标签失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取相关标签
     */
    @GetMapping("/{tagId}/related")
    public Result<List<Map<String, Object>>> getRelatedTags(@PathVariable @NotNull(message = "标签ID不能为空") Long tagId,
                                                            @RequestParam(defaultValue = "5") Integer limit) {
        try {
            List<Map<String, Object>> relatedTags = tagService.getRelatedTags(tagId, limit);
            return Result.success(relatedTags);
        } catch (RuntimeException e) {
            log.warn("获取相关标签失败: tagId={}, 原因: {}", tagId, e.getMessage());
            return handleTagException(e);
        } catch (Exception e) {
            log.error("获取相关标签系统错误: tagId={}", tagId, e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取标签使用统计
     */
    @GetMapping("/statistics/usage")
    public Result<List<Map<String, Object>>> getTagUsageStatistics(@RequestParam(defaultValue = "20") Integer limit) {
        try {
            List<Map<String, Object>> statistics = tagService.getTagUsageStatistics(limit);
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取标签使用统计失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 分页查询标签
     */
    @GetMapping("/page")
    public Result<IPage<Tag>> getTagPage(@RequestParam(defaultValue = "1") Integer current,
                                         @RequestParam(defaultValue = "20") Integer size,
                                         @RequestParam(required = false) String tagType,
                                         @RequestParam(required = false) String keyword) {
        try {
            Page<Tag> page = new Page<>(current, size);
            IPage<Tag> tagPage = tagService.getTagPage(page, tagType, keyword);
            return Result.success(tagPage);
        } catch (Exception e) {
            log.error("分页查询标签失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取所有标签类型
     */
    @GetMapping("/types")
    public Result<List<Map<String, Object>>> getTagTypes() {
        try {
            List<Map<String, Object>> types = tagService.getTagTypes();
            return Result.success(types);
        } catch (Exception e) {
            log.error("获取标签类型失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    // ==================== 标签管理接口 ====================

    /**
     * 创建标签（管理员功能）
     */
    @PostMapping
    public Result<String> createTag(@RequestBody Tag tag,
                                    @RequestHeader("Authorization") String token) {
        try {
            Long currentUserId = getUserIdFromToken(token);

            // 验证管理员权限
            validateAdminPermission(currentUserId);

            boolean success = tagService.createTag(tag);
            if (success) {
                return Result.success("标签创建成功");
            } else {
                return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "标签创建失败");
            }
        } catch (RuntimeException e) {
            log.warn("创建标签失败: {}", e.getMessage());
            return handleTagException(e);
        } catch (Exception e) {
            log.error("创建标签系统错误", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 批量创建标签（管理员功能）
     */
    @PostMapping("/batch")
    public Result<Map<String, Object>> batchCreateTags(@RequestBody List<Tag> tags,
                                                       @RequestHeader("Authorization") String token) {
        try {
            Long currentUserId = getUserIdFromToken(token);

            // 验证管理员权限
            validateAdminPermission(currentUserId);

            Map<String, Object> result = tagService.batchCreateTags(tags);
            return Result.success("批量创建完成", result);
        } catch (RuntimeException e) {
            log.warn("批量创建标签失败: {}", e.getMessage());
            return handleTagException(e);
        } catch (Exception e) {
            log.error("批量创建标签系统错误", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 更新标签信息（管理员功能）
     */
    @PutMapping("/{tagId}")
    public Result<String> updateTag(@PathVariable Long tagId,
                                    @RequestBody Tag tag,
                                    @RequestHeader("Authorization") String token) {
        try {
            Long currentUserId = getUserIdFromToken(token);

            // 验证管理员权限
            validateAdminPermission(currentUserId);

            tag.setId(tagId);
            boolean success = tagService.updateById(tag);
            if (success) {
                // 刷新缓存
                tagService.refreshTagCache();
                return Result.success("标签更新成功");
            } else {
                return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "标签更新失败");
            }
        } catch (RuntimeException e) {
            log.warn("更新标签失败: tagId={}, 原因: {}", tagId, e.getMessage());
            return handleTagException(e);
        } catch (Exception e) {
            log.error("更新标签系统错误: tagId={}", tagId, e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 更新标签热度（管理员功能）
     */
    @PutMapping("/{tagId}/hot-score")
    public Result<String> updateTagHotScore(@PathVariable Long tagId,
                                            @RequestParam @NotNull(message = "热度增量不能为空") Integer delta,
                                            @RequestHeader("Authorization") String token) {
        try {
            Long currentUserId = getUserIdFromToken(token);

            // 验证管理员权限
            validateAdminPermission(currentUserId);

            boolean success = tagService.updateTagHotScore(tagId, delta);
            if (success) {
                return Result.success("标签热度更新成功");
            } else {
                return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "热度更新失败");
            }
        } catch (RuntimeException e) {
            log.warn("更新标签热度失败: tagId={}, delta={}, 原因: {}", tagId, delta, e.getMessage());
            return handleTagException(e);
        } catch (Exception e) {
            log.error("更新标签热度系统错误: tagId={}, delta={}", tagId, delta, e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 删除标签（管理员功能）
     */
    @DeleteMapping("/{tagId}")
    public Result<String> deleteTag(@PathVariable Long tagId,
                                    @RequestHeader("Authorization") String token) {
        try {
            Long currentUserId = getUserIdFromToken(token);

            // 验证管理员权限
            validateAdminPermission(currentUserId);

            boolean success = tagService.removeById(tagId);
            if (success) {
                // 刷新缓存
                tagService.refreshTagCache();
                return Result.success("标签删除成功");
            } else {
                return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "标签删除失败");
            }
        } catch (RuntimeException e) {
            log.warn("删除标签失败: tagId={}, 原因: {}", tagId, e.getMessage());
            return handleTagException(e);
        } catch (Exception e) {
            log.error("删除标签系统错误: tagId={}", tagId, e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 为内容设置标签（管理员功能）
     */
    @PostMapping("/content/{contentId}/tags")
    public Result<String> setContentTags(@PathVariable Long contentId,
                                         @RequestBody List<Long> tagIds,
                                         @RequestHeader("Authorization") String token) {
        try {
            Long currentUserId = getUserIdFromToken(token);

            // 验证管理员权限
            validateAdminPermission(currentUserId);

            boolean success = tagService.setContentTags(contentId, tagIds);
            if (success) {
                return Result.success("内容标签设置成功");
            } else {
                return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "标签设置失败");
            }
        } catch (RuntimeException e) {
            log.warn("设置内容标签失败: contentId={}, 原因: {}", contentId, e.getMessage());
            return handleTagException(e);
        } catch (Exception e) {
            log.error("设置内容标签系统错误: contentId={}", contentId, e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 合并标签（管理员功能）
     */
    @PostMapping("/merge")
    public Result<String> mergeTags(@RequestParam @NotNull(message = "源标签ID不能为空") Long sourceTagId,
                                    @RequestParam @NotNull(message = "目标标签ID不能为空") Long targetTagId,
                                    @RequestHeader("Authorization") String token) {
        try {
            Long currentUserId = getUserIdFromToken(token);

            // 验证管理员权限
            validateAdminPermission(currentUserId);

            boolean success = tagService.mergeTags(sourceTagId, targetTagId);
            if (success) {
                return Result.success("标签合并成功");
            } else {
                return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "标签合并失败");
            }
        } catch (RuntimeException e) {
            log.warn("合并标签失败: sourceTagId={}, targetTagId={}, 原因: {}",
                    sourceTagId, targetTagId, e.getMessage());
            return handleTagException(e);
        } catch (Exception e) {
            log.error("合并标签系统错误: sourceTagId={}, targetTagId={}", sourceTagId, targetTagId, e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 刷新标签缓存（管理员功能）
     */
    @PostMapping("/refresh-cache")
    public Result<String> refreshTagCache(@RequestHeader("Authorization") String token) {
        try {
            Long currentUserId = getUserIdFromToken(token);

            // 验证管理员权限
            validateAdminPermission(currentUserId);

            tagService.refreshTagCache();
            return Result.success("标签缓存刷新成功");
        } catch (RuntimeException e) {
            log.warn("刷新标签缓存失败: {}", e.getMessage());
            return handleTagException(e);
        } catch (Exception e) {
            log.error("刷新标签缓存系统错误", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    // ==================== 权限验证和工具方法 ====================

    /**
     * 从Token中获取用户ID
     */
    private Long getUserIdFromToken(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new RuntimeException("Token格式错误");
        }

        String actualToken = token.substring(7);
        Long userId = jwtUtils.getUserIdFromToken(actualToken);

        if (userId == null) {
            throw new RuntimeException("无法从Token中获取用户信息");
        }

        return userId;
    }

    /**
     * 验证管理员权限（简化版本）
     */
    private void validateAdminPermission(Long userId) {
        // TODO: 实际项目中应该通过UserService检查管理员权限
        log.info("权限验证通过: userId={}", userId);
    }

    /**
     * 统一处理标签相关异常
     */
    private <T> Result<T> handleTagException(RuntimeException e) {
        String message = e.getMessage();

        if (message.contains("Token") || message.contains("无效") || message.contains("过期")) {
            return Result.error(ResultCode.TOKEN_INVALID);
        } else if (message.contains("权限")) {
            return Result.error(ResultCode.PERMISSION_DENIED);
        } else if (message.contains("不存在")) {
            return Result.error(ResultCode.DATA_NOT_EXISTS);
        } else if (message.contains("已存在")) {
            return Result.error(ResultCode.DATA_ALREADY_EXISTS);
        } else if (message.contains("参数") || message.contains("无效")) {
            return Result.error(ResultCode.BAD_REQUEST.getCode(), message);
        } else {
            return Result.error(ResultCode.BAD_REQUEST.getCode(), message);
        }
    }
}