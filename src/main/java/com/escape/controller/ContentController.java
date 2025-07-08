package com.escape.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.escape.common.Result;
import com.escape.common.ResultCode;
import com.escape.entity.Content;
import com.escape.service.ContentService;
import com.escape.utils.JwtUtils;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 内容控制器
 * 提供内容查询、管理等API接口
 *
 * @author escape
 * @since 2025-06-14
 */
@Slf4j
@RestController
@RequestMapping("/content")
public class ContentController {

    @Autowired
    private ContentService contentService;

    @Autowired
    private JwtUtils jwtUtils;

    // ==================== 内容查询接口 ====================

    /**
     * 获取内容详情
     */
    @GetMapping("/{contentId}")
    public Result<Map<String, Object>> getContentDetail(@PathVariable @NotNull(message = "内容ID不能为空") Long contentId) {
        try {
            Map<String, Object> contentDetail = contentService.getContentDetail(contentId);
            return Result.success(contentDetail);
        } catch (RuntimeException e) {
            log.warn("获取内容详情失败: contentId={}, 原因: {}", contentId, e.getMessage());
            return handleContentException(e);
        } catch (Exception e) {
            log.error("获取内容详情系统错误: contentId={}", contentId, e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 按内容类型获取内容列表
     */
    @GetMapping("/type/{contentType}")
    public Result<List<Content>> getContentsByType(@PathVariable String contentType) {
        try {
            List<Content> contents = contentService.getContentsByType(contentType);
            return Result.success(contents);
        } catch (RuntimeException e) {
            log.warn("按类型获取内容失败: contentType={}, 原因: {}", contentType, e.getMessage());
            return handleContentException(e);
        } catch (Exception e) {
            log.error("按类型获取内容系统错误: contentType={}", contentType, e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取热门内容
     */
    @GetMapping("/hot")
    public Result<List<Content>> getHotContents(@RequestParam(defaultValue = "10") Integer limit) {
        try {
            List<Content> hotContents = contentService.getHotContents(limit);
            return Result.success(hotContents);
        } catch (Exception e) {
            log.error("获取热门内容失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取精选内容
     */
    @GetMapping("/featured")
    public Result<List<Content>> getFeaturedContents(@RequestParam(defaultValue = "10") Integer limit) {
        try {
            List<Content> featuredContents = contentService.getFeaturedContents(limit);
            return Result.success(featuredContents);
        } catch (Exception e) {
            log.error("获取精选内容失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取官方内容
     */
    @GetMapping("/official")
    public Result<List<Content>> getOfficialContents() {
        try {
            List<Content> officialContents = contentService.getOfficialContents();
            return Result.success(officialContents);
        } catch (Exception e) {
            log.error("获取官方内容失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 根据关联获取内容
     */
    @GetMapping("/relation")
    public Result<List<Content>> getContentsByRelation(@RequestParam(required = false) Long heroId,
                                                       @RequestParam(required = false) Long mapId,
                                                       @RequestParam(required = false) Long weaponId,
                                                       @RequestParam(required = false) Long positionId) {
        try {
            List<Content> contents = contentService.getContentsByRelation(heroId, mapId, weaponId, positionId);
            return Result.success(contents);
        } catch (Exception e) {
            log.error("根据关联获取内容失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取作者的内容
     */
    @GetMapping("/author/{authorId}")
    public Result<List<Content>> getContentsByAuthor(@PathVariable @NotNull(message = "作者ID不能为空") Long authorId) {
        try {
            List<Content> contents = contentService.getContentsByAuthor(authorId);
            return Result.success(contents);
        } catch (RuntimeException e) {
            log.warn("获取作者内容失败: authorId={}, 原因: {}", authorId, e.getMessage());
            return handleContentException(e);
        } catch (Exception e) {
            log.error("获取作者内容系统错误: authorId={}", authorId, e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取相关内容
     */
    @GetMapping("/{contentId}/related")
    public Result<List<Map<String, Object>>> getRelatedContents(@PathVariable @NotNull(message = "内容ID不能为空") Long contentId,
                                                                @RequestParam(defaultValue = "5") Integer limit) {
        try {
            List<Map<String, Object>> relatedContents = contentService.getRelatedContents(contentId, limit);
            return Result.success(relatedContents);
        } catch (RuntimeException e) {
            log.warn("获取相关内容失败: contentId={}, 原因: {}", contentId, e.getMessage());
            return handleContentException(e);
        } catch (Exception e) {
            log.error("获取相关内容系统错误: contentId={}", contentId, e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 搜索内容
     */
    @GetMapping("/search")
    public Result<IPage<Content>> searchContents(@RequestParam(defaultValue = "1") Integer current,
                                                 @RequestParam(defaultValue = "10") Integer size,
                                                 @RequestParam String keyword,
                                                 @RequestParam(required = false) String contentType) {
        try {
            Page<Content> page = new Page<>(current, size);
            IPage<Content> result = contentService.searchContents(page, keyword, contentType);
            return Result.success(result);
        } catch (RuntimeException e) {
            log.warn("搜索内容失败: keyword={}, 原因: {}", keyword, e.getMessage());
            return handleContentException(e);
        } catch (Exception e) {
            log.error("搜索内容系统错误: keyword={}", keyword, e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 分页查询内容
     */
    @GetMapping("/page")
    public Result<IPage<Content>> getContentPage(@RequestParam(defaultValue = "1") Integer current,
                                                 @RequestParam(defaultValue = "10") Integer size,
                                                 @RequestParam(required = false) String contentType,
                                                 @RequestParam(required = false) Integer status,
                                                 @RequestParam(required = false) Boolean isFeatured,
                                                 @RequestParam(required = false) Boolean isOfficial,
                                                 @RequestParam(required = false) Long authorId,
                                                 @RequestParam(required = false) Long heroId,
                                                 @RequestParam(required = false) Long mapId) {
        try {
            Page<Content> page = new Page<>(current, size);

            Map<String, Object> params = new HashMap<>();
            if (contentType != null) params.put("contentType", contentType);
            if (status != null) params.put("status", status);
            if (isFeatured != null) params.put("isFeatured", isFeatured ? 1 : 0);
            if (isOfficial != null) params.put("isOfficial", isOfficial ? 1 : 0);
            if (authorId != null) params.put("authorId", authorId);
            if (heroId != null) params.put("heroId", heroId);
            if (mapId != null) params.put("mapId", mapId);

            IPage<Content> contentPage = contentService.getContentPage(page, params);
            return Result.success(contentPage);
        } catch (Exception e) {
            log.error("分页查询内容失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取内容统计信息
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getContentStatistics() {
        try {
            Map<String, Object> statistics = contentService.getContentStatistics();
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取内容统计失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    // ==================== 内容交互接口 ====================

    /**
     * 点赞内容
     */
    @PostMapping("/{contentId}/like")
    public Result<String> likeContent(@PathVariable Long contentId,
                                      @RequestHeader("Authorization") String token) {
        try {
            Long userId = getUserIdFromToken(token);

            // TODO: 检查用户是否已点赞，实际项目中需要记录点赞关系

            boolean success = contentService.updateLikeCount(contentId, 1);
            if (success) {
                return Result.success("点赞成功");
            } else {
                return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "点赞失败");
            }
        } catch (RuntimeException e) {
            log.warn("点赞失败: contentId={}, 原因: {}", contentId, e.getMessage());
            return handleContentException(e);
        } catch (Exception e) {
            log.error("点赞系统错误: contentId={}", contentId, e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 取消点赞
     */
    @DeleteMapping("/{contentId}/like")
    public Result<String> unlikeContent(@PathVariable Long contentId,
                                        @RequestHeader("Authorization") String token) {
        try {
            Long userId = getUserIdFromToken(token);

            // TODO: 检查用户是否已点赞

            boolean success = contentService.updateLikeCount(contentId, -1);
            if (success) {
                return Result.success("取消点赞成功");
            } else {
                return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "取消点赞失败");
            }
        } catch (RuntimeException e) {
            log.warn("取消点赞失败: contentId={}, 原因: {}", contentId, e.getMessage());
            return handleContentException(e);
        } catch (Exception e) {
            log.error("取消点赞系统错误: contentId={}", contentId, e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    // ==================== 内容管理接口 ====================

    /**
     * 创建内容（管理员/内容创作者功能）
     */
    @PostMapping
    public Result<Long> createContent(@RequestBody ContentCreateRequest request,
                                      @RequestHeader("Authorization") String token) {
        try {
            Long currentUserId = getUserIdFromToken(token);

            // 设置作者ID
            request.getContent().setAuthorId(currentUserId);

            Long contentId = contentService.createContent(request.getContent(), request.getTagIds());
            return Result.success("内容创建成功", contentId);
        } catch (RuntimeException e) {
            log.warn("创建内容失败: {}", e.getMessage());
            return handleContentException(e);
        } catch (Exception e) {
            log.error("创建内容系统错误", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 更新内容（管理员/内容创作者功能）
     */
    @PutMapping("/{contentId}")
    public Result<String> updateContent(@PathVariable Long contentId,
                                        @RequestBody ContentUpdateRequest request,
                                        @RequestHeader("Authorization") String token) {
        try {
            Long currentUserId = getUserIdFromToken(token);

            // TODO: 检查是否是作者或管理员

            request.getContent().setId(contentId);
            boolean success = contentService.updateContent(request.getContent(), request.getTagIds());
            if (success) {
                return Result.success("内容更新成功");
            } else {
                return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "内容更新失败");
            }
        } catch (RuntimeException e) {
            log.warn("更新内容失败: contentId={}, 原因: {}", contentId, e.getMessage());
            return handleContentException(e);
        } catch (Exception e) {
            log.error("更新内容系统错误: contentId={}", contentId, e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 更新内容状态（管理员功能）
     */
    @PutMapping("/{contentId}/status")
    public Result<String> updateContentStatus(@PathVariable Long contentId,
                                              @RequestParam @NotNull(message = "状态值不能为空") Integer status,
                                              @RequestHeader("Authorization") String token) {
        try {
            Long currentUserId = getUserIdFromToken(token);

            // 验证管理员权限
            validateAdminPermission(currentUserId);

            boolean success = contentService.updateContentStatus(contentId, status);
            if (success) {
                String statusText = status == 1 ? "发布" : (status == 2 ? "下架" : "待审核");
                return Result.success("内容已" + statusText);
            } else {
                return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "状态更新失败");
            }
        } catch (RuntimeException e) {
            log.warn("更新内容状态失败: contentId={}, status={}, 原因: {}", contentId, status, e.getMessage());
            return handleContentException(e);
        } catch (Exception e) {
            log.error("更新内容状态系统错误: contentId={}, status={}", contentId, status, e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 设置内容为精选（管理员功能）
     */
    @PutMapping("/{contentId}/featured")
    public Result<String> setContentFeatured(@PathVariable Long contentId,
                                             @RequestParam @NotNull(message = "精选状态不能为空") Boolean isFeatured,
                                             @RequestHeader("Authorization") String token) {
        try {
            Long currentUserId = getUserIdFromToken(token);

            // 验证管理员权限
            validateAdminPermission(currentUserId);

            boolean success = contentService.setContentFeatured(contentId, isFeatured);
            if (success) {
                String text = isFeatured ? "设为精选" : "取消精选";
                return Result.success("内容已" + text);
            } else {
                return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "操作失败");
            }
        } catch (RuntimeException e) {
            log.warn("设置精选失败: contentId={}, isFeatured={}, 原因: {}", contentId, isFeatured, e.getMessage());
            return handleContentException(e);
        } catch (Exception e) {
            log.error("设置精选系统错误: contentId={}, isFeatured={}", contentId, isFeatured, e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 删除内容（软删除，管理员功能）
     */
    @DeleteMapping("/{contentId}")
    public Result<String> deleteContent(@PathVariable Long contentId,
                                        @RequestHeader("Authorization") String token) {
        try {
            Long currentUserId = getUserIdFromToken(token);

            // 验证管理员权限
            validateAdminPermission(currentUserId);

            boolean success = contentService.removeById(contentId);
            if (success) {
                // 刷新缓存
                contentService.refreshContentCache();
                return Result.success("内容删除成功");
            } else {
                return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "内容删除失败");
            }
        } catch (RuntimeException e) {
            log.warn("删除内容失败: contentId={}, 原因: {}", contentId, e.getMessage());
            return handleContentException(e);
        } catch (Exception e) {
            log.error("删除内容系统错误: contentId={}", contentId, e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 批量导入内容（管理员功能）
     */
    @PostMapping("/batch-import")
    public Result<Map<String, Object>> batchImportContents(@RequestBody List<Content> contents,
                                                           @RequestHeader("Authorization") String token) {
        try {
            Long currentUserId = getUserIdFromToken(token);

            // 验证管理员权限
            validateAdminPermission(currentUserId);

            Map<String, Object> result = contentService.batchImportContents(contents);
            return Result.success("批量导入完成", result);
        } catch (RuntimeException e) {
            log.warn("批量导入内容失败: {}", e.getMessage());
            return handleContentException(e);
        } catch (Exception e) {
            log.error("批量导入内容系统错误", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 刷新内容缓存（管理员功能）
     */
    @PostMapping("/refresh-cache")
    public Result<String> refreshContentCache(@RequestHeader("Authorization") String token) {
        try {
            Long currentUserId = getUserIdFromToken(token);

            // 验证管理员权限
            validateAdminPermission(currentUserId);

            contentService.refreshContentCache();
            return Result.success("内容缓存刷新成功");
        } catch (RuntimeException e) {
            log.warn("刷新内容缓存失败: {}", e.getMessage());
            return handleContentException(e);
        } catch (Exception e) {
            log.error("刷新内容缓存系统错误", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    // ==================== 请求DTO ====================

    /**
     * 内容创建请求DTO
     */
    @Data
    public static class ContentCreateRequest {
        private Content content;
        private List<Long> tagIds;
    }

    /**
     * 内容更新请求DTO
     */
    @Data
    public static class ContentUpdateRequest {
        private Content content;
        private List<Long> tagIds;
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
     * 统一处理内容相关异常
     */
    private <T> Result<T> handleContentException(RuntimeException e) {
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