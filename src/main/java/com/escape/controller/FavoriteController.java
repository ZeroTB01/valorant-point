package com.escape.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.escape.common.Result;
import com.escape.common.ResultCode;
import com.escape.entity.Favorite;
import com.escape.service.FavoriteService;
import com.escape.utils.JwtUtils;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 收藏控制器
 * 提供收藏管理相关API接口
 * 支持内容、点位、英雄、地图、武器等多种类型的收藏
 *
 * @author escape
 * @since 2025-06-15
 */
@Slf4j
@RestController
@RequestMapping("/favorite")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private JwtUtils jwtUtils;

    // ==================== 收藏操作接口 ====================

    /**
     * 添加收藏
     */
    @PostMapping("/add")
    public Result<String> addFavorite(@RequestBody AddFavoriteRequest request,
                                      @RequestHeader("Authorization") String token) {
        try {
            Long userId = getUserIdFromToken(token);

            if (userId == -1L) {
                return Result.error(ResultCode.LOGIN_REQUIRED.getCode(), "游客用户请先登录");
            }

            boolean success = favoriteService.addFavorite(
                    userId,
                    request.getTargetType(),
                    request.getTargetId(),
                    request.getFolderName()
            );

            if (success) {
                return Result.success("收藏成功");
            } else {
                return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "收藏失败");
            }
        } catch (RuntimeException e) {
            log.warn("添加收藏失败: {}", e.getMessage());
            return handleFavoriteException(e);
        } catch (Exception e) {
            log.error("添加收藏系统错误", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 取消收藏
     */
    @DeleteMapping("/remove")
    public Result<String> removeFavorite(@RequestParam @NotBlank(message = "目标类型不能为空") String targetType,
                                         @RequestParam @NotNull(message = "目标ID不能为空") Long targetId,
                                         @RequestHeader("Authorization") String token) {
        try {
            Long userId = getUserIdFromToken(token);

            if (userId == -1L) {
                return Result.error(ResultCode.LOGIN_REQUIRED.getCode(), "游客用户请先登录");
            }

            boolean success = favoriteService.removeFavorite(userId, targetType, targetId);

            if (success) {
                return Result.success("取消收藏成功");
            } else {
                return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "取消收藏失败");
            }
        } catch (RuntimeException e) {
            log.warn("取消收藏失败: targetType={}, targetId={}, 原因: {}", targetType, targetId, e.getMessage());
            return handleFavoriteException(e);
        } catch (Exception e) {
            log.error("取消收藏系统错误: targetType={}, targetId={}", targetType, targetId, e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 检查是否已收藏
     */
    @GetMapping("/check")
    public Result<Boolean> isFavorited(@RequestParam @NotBlank(message = "目标类型不能为空") String targetType,
                                       @RequestParam @NotNull(message = "目标ID不能为空") Long targetId,
                                       @RequestHeader("Authorization") String token) {
        try {
            Long userId = getUserIdFromToken(token);

            if (userId == -1L) {
                return Result.success(false); // 游客用户默认未收藏
            }

            boolean isFavorited = favoriteService.isFavorited(userId, targetType, targetId);
            return Result.success(isFavorited);
        } catch (Exception e) {
            log.error("检查收藏状态失败: targetType={}, targetId={}", targetType, targetId, e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 批量检查是否已收藏
     */
    @PostMapping("/check-batch")
    public Result<Map<Long, Boolean>> batchCheckFavorited(@RequestBody BatchCheckRequest request,
                                                          @RequestHeader("Authorization") String token) {
        try {
            Long userId = getUserIdFromToken(token);

            if (userId == -1L) {
                // 游客用户返回全部未收藏
                Map<Long, Boolean> result = new HashMap<>();
                for (Long targetId : request.getTargetIds()) {
                    result.put(targetId, false);
                }
                return Result.success(result);
            }

            Map<Long, Boolean> result = favoriteService.batchCheckFavorited(
                    userId,
                    request.getTargetType(),
                    request.getTargetIds()
            );
            return Result.success(result);
        } catch (Exception e) {
            log.error("批量检查收藏状态失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    // ==================== 收藏查询接口 ====================

    /**
     * 获取用户的收藏列表（按类型）
     */
    @GetMapping("/list")
    public Result<List<Favorite>> getUserFavorites(@RequestParam @NotBlank(message = "目标类型不能为空") String targetType,
                                                   @RequestHeader("Authorization") String token) {
        try {
            Long userId = getUserIdFromToken(token);

            if (userId == -1L) {
                return Result.error(ResultCode.LOGIN_REQUIRED.getCode(), "游客用户请先登录");
            }

            List<Favorite> favorites = favoriteService.getUserFavorites(userId, targetType);
            return Result.success(favorites);
        } catch (RuntimeException e) {
            log.warn("获取收藏列表失败: targetType={}, 原因: {}", targetType, e.getMessage());
            return handleFavoriteException(e);
        } catch (Exception e) {
            log.error("获取收藏列表系统错误: targetType={}", targetType, e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取最近的收藏
     */
    @GetMapping("/recent")
    public Result<List<Favorite>> getRecentFavorites(@RequestParam(defaultValue = "10") Integer limit,
                                                     @RequestHeader("Authorization") String token) {
        try {
            Long userId = getUserIdFromToken(token);

            if (userId == -1L) {
                return Result.error(ResultCode.LOGIN_REQUIRED.getCode(), "游客用户请先登录");
            }

            List<Favorite> favorites = favoriteService.getRecentFavorites(userId, limit);
            return Result.success(favorites);
        } catch (Exception e) {
            log.error("获取最近收藏失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取收藏详情（分页，包含关联信息）
     */
    @GetMapping("/details")
    public Result<IPage<Map<String, Object>>> getFavoriteDetails(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) String folderName,
            @RequestHeader("Authorization") String token) {
        try {
            Long userId = getUserIdFromToken(token);

            if (userId == -1L) {
                return Result.error(ResultCode.LOGIN_REQUIRED.getCode(), "游客用户请先登录");
            }

            Page<Favorite> page = new Page<>(current, size);
            IPage<Map<String, Object>> details = favoriteService.getFavoriteDetails(page, userId, targetType, folderName);
            return Result.success(details);
        } catch (Exception e) {
            log.error("获取收藏详情失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    // ==================== 收藏夹管理接口 ====================

    /**
     * 获取用户的收藏夹列表
     */
    @GetMapping("/folders")
    public Result<List<String>> getUserFolders(@RequestHeader("Authorization") String token) {
        try {
            Long userId = getUserIdFromToken(token);

            if (userId == -1L) {
                return Result.error(ResultCode.LOGIN_REQUIRED.getCode(), "游客用户请先登录");
            }

            List<String> folders = favoriteService.getUserFolders(userId);
            return Result.success(folders);
        } catch (Exception e) {
            log.error("获取收藏夹列表失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取指定收藏夹的内容
     */
    @GetMapping("/folder/{folderName}")
    public Result<List<Favorite>> getFavoritesByFolder(@PathVariable String folderName,
                                                       @RequestHeader("Authorization") String token) {
        try {
            Long userId = getUserIdFromToken(token);

            if (userId == -1L) {
                return Result.error(ResultCode.LOGIN_REQUIRED.getCode(), "游客用户请先登录");
            }

            List<Favorite> favorites = favoriteService.getFavoritesByFolder(userId, folderName);
            return Result.success(favorites);
        } catch (Exception e) {
            log.error("获取收藏夹内容失败: folderName={}", folderName, e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 创建收藏夹
     */
    @PostMapping("/folder")
    public Result<String> createFolder(@RequestParam @NotBlank(message = "收藏夹名称不能为空") String folderName,
                                       @RequestHeader("Authorization") String token) {
        try {
            Long userId = getUserIdFromToken(token);

            if (userId == -1L) {
                return Result.error(ResultCode.LOGIN_REQUIRED.getCode(), "游客用户请先登录");
            }

            boolean success = favoriteService.createFolder(userId, folderName);
            if (success) {
                return Result.success("收藏夹创建成功");
            } else {
                return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "收藏夹创建失败");
            }
        } catch (RuntimeException e) {
            log.warn("创建收藏夹失败: folderName={}, 原因: {}", folderName, e.getMessage());
            return handleFavoriteException(e);
        } catch (Exception e) {
            log.error("创建收藏夹系统错误: folderName={}", folderName, e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 重命名收藏夹
     */
    @PutMapping("/folder/rename")
    public Result<String> renameFolder(@RequestParam @NotBlank(message = "原名称不能为空") String oldName,
                                       @RequestParam @NotBlank(message = "新名称不能为空") String newName,
                                       @RequestHeader("Authorization") String token) {
        try {
            Long userId = getUserIdFromToken(token);

            if (userId == -1L) {
                return Result.error(ResultCode.LOGIN_REQUIRED.getCode(), "游客用户请先登录");
            }

            boolean success = favoriteService.renameFolder(userId, oldName, newName);
            if (success) {
                return Result.success("收藏夹重命名成功");
            } else {
                return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "收藏夹重命名失败");
            }
        } catch (RuntimeException e) {
            log.warn("重命名收藏夹失败: {} -> {}, 原因: {}", oldName, newName, e.getMessage());
            return handleFavoriteException(e);
        } catch (Exception e) {
            log.error("重命名收藏夹系统错误: {} -> {}", oldName, newName, e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 删除收藏夹
     */
    @DeleteMapping("/folder/{folderName}")
    public Result<String> deleteFolder(@PathVariable String folderName,
                                       @RequestHeader("Authorization") String token) {
        try {
            Long userId = getUserIdFromToken(token);

            if (userId == -1L) {
                return Result.error(ResultCode.LOGIN_REQUIRED.getCode(), "游客用户请先登录");
            }

            boolean success = favoriteService.deleteFolder(userId, folderName);
            if (success) {
                return Result.success("收藏夹删除成功，收藏已移至默认收藏夹");
            } else {
                return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "收藏夹删除失败");
            }
        } catch (RuntimeException e) {
            log.warn("删除收藏夹失败: folderName={}, 原因: {}", folderName, e.getMessage());
            return handleFavoriteException(e);
        } catch (Exception e) {
            log.error("删除收藏夹系统错误: folderName={}", folderName, e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 移动收藏到其他收藏夹
     */
    @PutMapping("/move")
    public Result<String> moveFavorite(@RequestBody MoveFavoriteRequest request,
                                       @RequestHeader("Authorization") String token) {
        try {
            Long userId = getUserIdFromToken(token);

            if (userId == -1L) {
                return Result.error(ResultCode.LOGIN_REQUIRED.getCode(), "游客用户请先登录");
            }

            boolean success = favoriteService.moveFavorite(userId, request.getFavoriteId(), request.getNewFolder());
            if (success) {
                return Result.success("收藏移动成功");
            } else {
                return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "收藏移动失败");
            }
        } catch (RuntimeException e) {
            log.warn("移动收藏失败: {}", e.getMessage());
            return handleFavoriteException(e);
        } catch (Exception e) {
            log.error("移动收藏系统错误", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    // ==================== 统计分析接口 ====================

    /**
     * 获取用户各类型收藏数量统计
     */
    @GetMapping("/statistics/types")
    public Result<List<Map<String, Object>>> getUserFavoriteStatistics(@RequestHeader("Authorization") String token) {
        try {
            Long userId = getUserIdFromToken(token);

            if (userId == -1L) {
                return Result.error(ResultCode.LOGIN_REQUIRED.getCode(), "游客用户请先登录");
            }

            List<Map<String, Object>> statistics = favoriteService.getUserFavoriteStatistics(userId);
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取收藏统计失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取用户各收藏夹的数量统计
     */
    @GetMapping("/statistics/folders")
    public Result<List<Map<String, Object>>> getUserFolderStatistics(@RequestHeader("Authorization") String token) {
        try {
            Long userId = getUserIdFromToken(token);

            if (userId == -1L) {
                return Result.error(ResultCode.LOGIN_REQUIRED.getCode(), "游客用户请先登录");
            }

            List<Map<String, Object>> statistics = favoriteService.getUserFolderStatistics(userId);
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取收藏夹统计失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取内容被收藏次数
     */
    @GetMapping("/count")
    public Result<Integer> getFavoriteCount(@RequestParam @NotBlank(message = "目标类型不能为空") String targetType,
                                            @RequestParam @NotNull(message = "目标ID不能为空") Long targetId) {
        try {
            int count = favoriteService.getFavoriteCount(targetType, targetId);
            return Result.success(count);
        } catch (Exception e) {
            log.error("获取收藏数失败: targetType={}, targetId={}", targetType, targetId, e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取热门收藏内容
     */
    @GetMapping("/hot")
    public Result<List<Map<String, Object>>> getHotFavorites(
            @RequestParam @NotBlank(message = "目标类型不能为空") String targetType,
            @RequestParam(defaultValue = "10") Integer limit) {
        try {
            List<Map<String, Object>> hotFavorites = favoriteService.getHotFavorites(targetType, limit);
            return Result.success(hotFavorites);
        } catch (Exception e) {
            log.error("获取热门收藏失败: targetType={}", targetType, e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    // ==================== 批量操作接口 ====================

    /**
     * 批量删除收藏
     */
    @DeleteMapping("/batch")
    public Result<Integer> batchRemoveFavorites(@RequestBody List<Long> favoriteIds,
                                                @RequestHeader("Authorization") String token) {
        try {
            Long userId = getUserIdFromToken(token);

            if (userId == -1L) {
                return Result.error(ResultCode.LOGIN_REQUIRED.getCode(), "游客用户请先登录");
            }

            int count = favoriteService.batchRemoveFavorites(userId, favoriteIds);
            return Result.success("批量删除成功", count);
        } catch (Exception e) {
            log.error("批量删除收藏失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    // ==================== 辅助接口 ====================

    /**
     * 获取收藏类型选项
     */
    @GetMapping("/target-types")
    public Result<List<Map<String, String>>> getTargetTypes() {
        try {
            List<Map<String, String>> types = List.of(
                    Map.of("value", "content", "label", "内容", "icon", "document", "color", "#2196F3"),
                    Map.of("value", "position", "label", "点位", "icon", "location", "color", "#4CAF50"),
                    Map.of("value", "hero", "label", "英雄", "icon", "person", "color", "#FF9800"),
                    Map.of("value", "map", "label", "地图", "icon", "map", "color", "#9C27B0"),
                    Map.of("value", "weapon", "label", "武器", "icon", "gun", "color", "#F44336")
            );
            return Result.success(types);
        } catch (Exception e) {
            log.error("获取收藏类型失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    // ==================== 请求DTO ====================

    /**
     * 添加收藏请求DTO
     */
    @Data
    public static class AddFavoriteRequest {
        @NotBlank(message = "目标类型不能为空")
        private String targetType;

        @NotNull(message = "目标ID不能为空")
        private Long targetId;

        private String folderName; // 可选，默认为"默认收藏夹"
    }

    /**
     * 批量检查请求DTO
     */
    @Data
    public static class BatchCheckRequest {
        @NotBlank(message = "目标类型不能为空")
        private String targetType;

        @NotNull(message = "目标ID列表不能为空")
        private List<Long> targetIds;
    }

    /**
     * 移动收藏请求DTO
     */
    @Data
    public static class MoveFavoriteRequest {
        @NotNull(message = "收藏ID不能为空")
        private Long favoriteId;

        @NotBlank(message = "新收藏夹名称不能为空")
        private String newFolder;
    }

    // ==================== 工具方法 ====================

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
     * 统一处理收藏相关异常
     */
    private <T> Result<T> handleFavoriteException(RuntimeException e) {
        String message = e.getMessage();

        if (message.contains("Token") || message.contains("无效") || message.contains("过期")) {
            return Result.error(ResultCode.TOKEN_INVALID);
        } else if (message.contains("已经收藏") || message.contains("已存在")) {
            return Result.error(ResultCode.DATA_ALREADY_EXISTS);
        } else if (message.contains("不存在")) {
            return Result.error(ResultCode.DATA_NOT_EXISTS);
        } else if (message.contains("参数") || message.contains("无效")) {
            return Result.error(ResultCode.BAD_REQUEST.getCode(), message);
        } else if (message.contains("默认收藏夹")) {
            return Result.error(ResultCode.BAD_REQUEST.getCode(), message);
        } else {
            return Result.error(ResultCode.BAD_REQUEST.getCode(), message);
        }
    }
}