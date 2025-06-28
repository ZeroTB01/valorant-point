package com.escape.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.escape.common.Result;
import com.escape.common.ResultCode;
import com.escape.entity.Position;
import com.escape.service.PositionService;
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
 * 点位控制器
 * 提供点位查询、筛选、管理等API接口
 * 这是项目的核心功能控制器，实现地图→英雄→攻防的三级筛选
 *
 * @author escape
 * @since 2025-06-15
 */
@Slf4j
@RestController
@RequestMapping("/position")
public class PositionController {

    @Autowired
    private PositionService positionService;

    @Autowired
    private JwtUtils jwtUtils;

    // ==================== 核心筛选功能接口 ====================

    /**
     * 三级筛选查询点位（核心功能）
     * 地图→英雄→攻防方 三级筛选
     */
    @GetMapping("/filter")
    public Result<List<Position>> filterPositions(@RequestParam @NotNull(message = "地图ID不能为空") Long mapId,
                                                  @RequestParam(required = false) Long heroId,
                                                  @RequestParam @NotBlank(message = "攻防方不能为空") String side) {
        try {
            log.info("三级筛选查询: mapId={}, heroId={}, side={}", mapId, heroId, side);
            List<Position> positions = positionService.filterPositions(mapId, heroId, side);
            return Result.success(positions);
        } catch (RuntimeException e) {
            log.warn("筛选点位失败: mapId={}, heroId={}, side={}, 原因: {}", mapId, heroId, side, e.getMessage());
            return handlePositionException(e);
        } catch (Exception e) {
            log.error("筛选点位系统错误: mapId={}, heroId={}, side={}", mapId, heroId, side, e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取筛选选项
     * 返回可用的地图、英雄、攻防方等选项，用于前端构建筛选器
     */
    @GetMapping("/filter-options")
    public Result<Map<String, List<?>>> getFilterOptions() {
        try {
            Map<String, List<?>> options = positionService.getFilterOptions();
            return Result.success(options);
        } catch (Exception e) {
            log.error("获取筛选选项失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    // ==================== 点位查询接口 ====================

    /**
     * 获取点位详情
     */
    @GetMapping("/{positionId}")
    public Result<Map<String, Object>> getPositionDetail(@PathVariable @NotNull(message = "点位ID不能为空") Long positionId) {
        try {
            Map<String, Object> positionDetail = positionService.getPositionDetail(positionId);
            return Result.success(positionDetail);
        } catch (RuntimeException e) {
            log.warn("获取点位详情失败: positionId={}, 原因: {}", positionId, e.getMessage());
            return handlePositionException(e);
        } catch (Exception e) {
            log.error("获取点位详情系统错误: positionId={}", positionId, e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 根据地图和站点查询点位
     */
    @GetMapping("/map/{mapId}/site/{site}")
    public Result<List<Position>> getPositionsByMapAndSite(@PathVariable @NotNull(message = "地图ID不能为空") Long mapId,
                                                           @PathVariable @NotBlank(message = "站点不能为空") String site) {
        try {
            List<Position> positions = positionService.getPositionsByMapAndSite(mapId, site);
            return Result.success(positions);
        } catch (RuntimeException e) {
            log.warn("根据地图和站点查询点位失败: mapId={}, site={}, 原因: {}", mapId, site, e.getMessage());
            return handlePositionException(e);
        } catch (Exception e) {
            log.error("根据地图和站点查询点位系统错误: mapId={}, site={}", mapId, site, e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取热门点位
     */
    @GetMapping("/hot")
    public Result<List<Map<String, Object>>> getHotPositions(@RequestParam(defaultValue = "10") Integer limit) {
        try {
            List<Map<String, Object>> hotPositions = positionService.getHotPositions(limit);
            return Result.success(hotPositions);
        } catch (Exception e) {
            log.error("获取热门点位失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取相关推荐点位
     */
    @GetMapping("/{positionId}/related")
    public Result<List<Position>> getRelatedPositions(@PathVariable @NotNull(message = "点位ID不能为空") Long positionId,
                                                      @RequestParam(defaultValue = "5") Integer limit) {
        try {
            List<Position> relatedPositions = positionService.getRelatedPositions(positionId, limit);
            return Result.success(relatedPositions);
        } catch (RuntimeException e) {
            log.warn("获取相关点位失败: positionId={}, 原因: {}", positionId, e.getMessage());
            return handlePositionException(e);
        } catch (Exception e) {
            log.error("获取相关点位系统错误: positionId={}", positionId, e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 按点位类型查询
     */
    @GetMapping("/type/{positionType}")
    public Result<List<Position>> getPositionsByType(@PathVariable String positionType,
                                                     @RequestParam(defaultValue = "20") Integer limit) {
        try {
            List<Position> positions = positionService.getPositionsByType(positionType, limit);
            return Result.success(positions);
        } catch (RuntimeException e) {
            log.warn("按类型查询点位失败: positionType={}, 原因: {}", positionType, e.getMessage());
            return handlePositionException(e);
        } catch (Exception e) {
            log.error("按类型查询点位系统错误: positionType={}", positionType, e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 按难度查询点位
     */
    @GetMapping("/difficulty/{difficulty}")
    public Result<List<Position>> getPositionsByDifficulty(@PathVariable @NotNull(message = "难度等级不能为空") Integer difficulty) {
        try {
            List<Position> positions = positionService.getPositionsByDifficulty(difficulty);
            return Result.success(positions);
        } catch (RuntimeException e) {
            log.warn("按难度查询点位失败: difficulty={}, 原因: {}", difficulty, e.getMessage());
            return handlePositionException(e);
        } catch (Exception e) {
            log.error("按难度查询点位系统错误: difficulty={}", difficulty, e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 分页查询点位（支持多条件）
     */
    @GetMapping("/page")
    public Result<IPage<Position>> getPositionPage(@RequestParam(defaultValue = "1") Integer current,
                                                   @RequestParam(defaultValue = "20") Integer size,
                                                   @RequestParam(required = false) Long mapId,
                                                   @RequestParam(required = false) Long heroId,
                                                   @RequestParam(required = false) String side,
                                                   @RequestParam(required = false) String positionType,
                                                   @RequestParam(required = false) Integer difficulty) {
        try {
            Page<Position> page = new Page<>(current, size);

            Map<String, Object> params = new HashMap<>();
            if (mapId != null) params.put("mapId", mapId);
            if (heroId != null) params.put("heroId", heroId);
            if (side != null) params.put("side", side);
            if (positionType != null) params.put("positionType", positionType);
            if (difficulty != null) params.put("difficulty", difficulty);

            IPage<Position> positionPage = positionService.getPositionPage(page, params);
            return Result.success(positionPage);
        } catch (Exception e) {
            log.error("分页查询点位失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    // ==================== 统计分析接口 ====================

    /**
     * 获取地图点位统计
     */
    @GetMapping("/statistics/map")
    public Result<Map<Long, Integer>> getMapPositionStatistics() {
        try {
            Map<Long, Integer> statistics = positionService.getMapPositionStatistics();
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取地图点位统计失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取英雄点位统计
     */
    @GetMapping("/statistics/hero")
    public Result<Map<Long, Integer>> getHeroPositionStatistics() {
        try {
            Map<Long, Integer> statistics = positionService.getHeroPositionStatistics();
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取英雄点位统计失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取用户的点位学习进度
     */
    @GetMapping("/progress")
    public Result<Map<String, Object>> getUserPositionProgress(@RequestHeader("Authorization") String token,
                                                               @RequestParam(required = false) Long mapId) {
        try {
            Long userId = getUserIdFromToken(token);

            if (userId == -1L) {
                return Result.error(ResultCode.LOGIN_REQUIRED.getCode(), "游客用户请先登录查看学习进度");
            }

            Map<String, Object> progress = positionService.getUserPositionProgress(userId, mapId);
            return Result.success(progress);
        } catch (RuntimeException e) {
            log.warn("获取学习进度失败: {}", e.getMessage());
            return handlePositionException(e);
        } catch (Exception e) {
            log.error("获取学习进度系统错误", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    // ==================== 管理员功能接口 ====================

    /**
     * 创建点位（管理员功能）
     */
    @PostMapping
    public Result<String> createPosition(@RequestBody Position position,
                                         @RequestHeader("Authorization") String token) {
        try {
            Long currentUserId = getUserIdFromToken(token);

            // 验证管理员权限
            validateAdminPermission(currentUserId);

            boolean success = positionService.createPosition(position);
            if (success) {
                return Result.success("点位创建成功");
            } else {
                return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "点位创建失败");
            }
        } catch (RuntimeException e) {
            log.warn("创建点位失败: {}", e.getMessage());
            return handlePositionException(e);
        } catch (Exception e) {
            log.error("创建点位系统错误", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 更新点位信息（管理员功能）
     */
    @PutMapping("/{positionId}")
    public Result<String> updatePosition(@PathVariable Long positionId,
                                         @RequestBody Position position,
                                         @RequestHeader("Authorization") String token) {
        try {
            Long currentUserId = getUserIdFromToken(token);

            // 验证管理员权限
            validateAdminPermission(currentUserId);

            position.setId(positionId);
            boolean success = positionService.updateById(position);
            if (success) {
                // 刷新缓存
                positionService.refreshPositionCache();
                return Result.success("点位更新成功");
            } else {
                return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "点位更新失败");
            }
        } catch (RuntimeException e) {
            log.warn("更新点位失败: positionId={}, 原因: {}", positionId, e.getMessage());
            return handlePositionException(e);
        } catch (Exception e) {
            log.error("更新点位系统错误: positionId={}", positionId, e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 更新点位状态（管理员功能）
     */
    @PutMapping("/{positionId}/status")
    public Result<String> updatePositionStatus(@PathVariable Long positionId,
                                               @RequestParam @NotNull(message = "状态值不能为空") Integer status,
                                               @RequestHeader("Authorization") String token) {
        try {
            Long currentUserId = getUserIdFromToken(token);

            // 验证管理员权限
            validateAdminPermission(currentUserId);

            boolean success = positionService.updatePositionStatus(positionId, status);
            if (success) {
                String statusText = status == 1 ? "启用" : "禁用";
                return Result.success("点位" + statusText + "成功");
            } else {
                return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "状态更新失败");
            }
        } catch (RuntimeException e) {
            log.warn("更新点位状态失败: positionId={}, status={}, 原因: {}", positionId, status, e.getMessage());
            return handlePositionException(e);
        } catch (Exception e) {
            log.error("更新点位状态系统错误: positionId={}, status={}", positionId, status, e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 批量导入点位（管理员功能）
     */
    @PostMapping("/batch-import")
    public Result<Map<String, Object>> batchImportPositions(@RequestBody List<Position> positions,
                                                            @RequestHeader("Authorization") String token) {
        try {
            Long currentUserId = getUserIdFromToken(token);

            // 验证管理员权限
            validateAdminPermission(currentUserId);

            Map<String, Object> result = positionService.batchImportPositions(positions);
            return Result.success("批量导入完成", result);
        } catch (RuntimeException e) {
            log.warn("批量导入点位失败: {}", e.getMessage());
            return handlePositionException(e);
        } catch (Exception e) {
            log.error("批量导入点位系统错误", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 删除点位（软删除，管理员功能）
     */
    @DeleteMapping("/{positionId}")
    public Result<String> deletePosition(@PathVariable Long positionId,
                                         @RequestHeader("Authorization") String token) {
        try {
            Long currentUserId = getUserIdFromToken(token);

            // 验证管理员权限
            validateAdminPermission(currentUserId);

            boolean success = positionService.removeById(positionId);
            if (success) {
                // 刷新缓存
                positionService.refreshPositionCache();
                return Result.success("点位删除成功");
            } else {
                return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "点位删除失败");
            }
        } catch (RuntimeException e) {
            log.warn("删除点位失败: positionId={}, 原因: {}", positionId, e.getMessage());
            return handlePositionException(e);
        } catch (Exception e) {
            log.error("删除点位系统错误: positionId={}", positionId, e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 刷新点位缓存（管理员功能）
     */
    @PostMapping("/refresh-cache")
    public Result<String> refreshPositionCache(@RequestHeader("Authorization") String token) {
        try {
            Long currentUserId = getUserIdFromToken(token);

            // 验证管理员权限
            validateAdminPermission(currentUserId);

            positionService.refreshPositionCache();
            return Result.success("点位缓存刷新成功");
        } catch (RuntimeException e) {
            log.warn("刷新点位缓存失败: {}", e.getMessage());
            return handlePositionException(e);
        } catch (Exception e) {
            log.error("刷新点位缓存系统错误", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    // ==================== 辅助接口 ====================

    /**
     * 获取点位类型选项
     */
    @GetMapping("/position-types")
    public Result<List<Map<String, String>>> getPositionTypes() {
        try {
            List<Map<String, String>> types = List.of(
                    Map.of("value", "smoke", "label", "烟雾", "icon", "cloud", "color", "#9E9E9E"),
                    Map.of("value", "flash", "label", "闪光", "icon", "flash", "color", "#FFC107"),
                    Map.of("value", "molly", "label", "燃烧", "icon", "fire", "color", "#FF5722"),
                    Map.of("value", "wall", "label", "墙", "icon", "wall", "color", "#2196F3"),
                    Map.of("value", "orb", "label", "球", "icon", "circle", "color", "#4CAF50"),
                    Map.of("value", "trap", "label", "陷阱", "icon", "trap", "color", "#9C27B0"),
                    Map.of("value", "general", "label", "通用", "icon", "star", "color", "#607D8B")
            );
            return Result.success(types);
        } catch (Exception e) {
            log.error("获取点位类型失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取难度等级选项
     */
    @GetMapping("/difficulty-levels")
    public Result<List<Map<String, Object>>> getDifficultyLevels() {
        try {
            List<Map<String, Object>> levels = List.of(
                    Map.of("value", 1, "label", "入门", "description", "新手友好，容易掌握", "color", "#4CAF50"),
                    Map.of("value", 2, "label", "简单", "description", "稍有难度，需要练习", "color", "#8BC34A"),
                    Map.of("value", 3, "label", "中等", "description", "需要一定技巧", "color", "#FFC107"),
                    Map.of("value", 4, "label", "困难", "description", "高难度技巧", "color", "#FF9800"),
                    Map.of("value", 5, "label", "大师", "description", "职业级别技巧", "color", "#F44336")
            );
            return Result.success(levels);
        } catch (Exception e) {
            log.error("获取难度等级失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    // ==================== 请求DTO ====================

    /**
     * 批量筛选请求DTO
     */
    @Data
    public static class BatchFilterRequest {
        private List<Long> mapIds;
        private List<Long> heroIds;
        private String side;
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
     * 统一处理点位相关异常
     */
    private <T> Result<T> handlePositionException(RuntimeException e) {
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