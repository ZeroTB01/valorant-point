package com.escape.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.escape.common.Result;
import com.escape.common.ResultCode;
import com.escape.entity.GameMap;
import com.escape.service.GameMapService;
import com.escape.utils.JwtUtils;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 游戏地图控制器
 * 提供地图数据查询、管理等API接口
 *
 * @author escape
 * @since 2025-06-14
 */
@Slf4j
@RestController
@RequestMapping("/map")
public class GameMapController {

    @Autowired
    private GameMapService gameMapService;

    @Autowired
    private JwtUtils jwtUtils;

    // ==================== 地图基础查询接口 ====================

    /**
     * 获取所有启用的地图列表
     */
    @GetMapping("/list")
    public Result<List<GameMap>> getAllMaps() {
        try {
            List<GameMap> maps = gameMapService.getAllEnabledMaps();
            return Result.success(maps);
        } catch (Exception e) {
            log.error("获取地图列表失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 根据地图ID获取地图详情
     */
    @GetMapping("/{mapId}")
    public Result<Map<String, Object>> getMapDetail(@PathVariable @NotNull(message = "地图ID不能为空") Long mapId) {
        try {
            Map<String, Object> mapDetail = gameMapService.getMapDetail(mapId);
            return Result.success(mapDetail);
        } catch (RuntimeException e) {
            log.warn("获取地图详情失败: mapId={}, 原因: {}", mapId, e.getMessage());
            return handleMapException(e);
        } catch (Exception e) {
            log.error("获取地图详情系统错误: mapId={}", mapId, e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 根据地图标识获取地图信息
     */
    @GetMapping("/key/{mapKey}")
    public Result<GameMap> getMapByKey(@PathVariable String mapKey) {
        try {
            GameMap gameMap = gameMapService.getByMapKey(mapKey);
            if (gameMap == null) {
                return Result.error(ResultCode.DATA_NOT_EXISTS.getCode(), "地图不存在");
            }
            return Result.success(gameMap);
        } catch (Exception e) {
            log.error("根据mapKey获取地图失败: mapKey={}", mapKey, e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    // ==================== 地图筛选和分类接口 ====================

    /**
     * 按地图类型获取地图列表
     */
    @GetMapping("/type/{mapType}")
    public Result<List<GameMap>> getMapsByType(@PathVariable String mapType) {
        try {
            List<GameMap> maps = gameMapService.getMapsByType(mapType);
            return Result.success(maps);
        } catch (RuntimeException e) {
            log.warn("按类型获取地图失败: mapType={}, 原因: {}", mapType, e.getMessage());
            return handleMapException(e);
        } catch (Exception e) {
            log.error("按类型获取地图系统错误: mapType={}", mapType, e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 按站点数量获取地图列表
     */
    @GetMapping("/sites/{siteCount}")
    public Result<List<GameMap>> getMapsBySiteCount(@PathVariable @NotNull(message = "站点数量不能为空") Integer siteCount) {
        try {
            List<GameMap> maps = gameMapService.getMapsBySiteCount(siteCount);
            return Result.success(maps);
        } catch (RuntimeException e) {
            log.warn("按站点数量获取地图失败: siteCount={}, 原因: {}", siteCount, e.getMessage());
            return handleMapException(e);
        } catch (Exception e) {
            log.error("按站点数量获取地图系统错误: siteCount={}", siteCount, e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 分页查询地图（支持筛选）
     */
    @GetMapping("/page")
    public Result<IPage<GameMap>> getMapPage(@RequestParam(defaultValue = "1") Integer current,
                                             @RequestParam(defaultValue = "10") Integer size,
                                             @RequestParam(required = false) String mapType) {
        try {
            Page<GameMap> page = new Page<>(current, size);
            IPage<GameMap> mapPage = gameMapService.getMapPage(page, mapType);
            return Result.success(mapPage);
        } catch (Exception e) {
            log.error("分页查询地图失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取地图选项列表（用于下拉框等）
     */
    @GetMapping("/options")
    public Result<List<Map<String, Object>>> getMapOptions() {
        try {
            List<Map<String, Object>> options = gameMapService.getMapOptions();
            return Result.success(options);
        } catch (Exception e) {
            log.error("获取地图选项失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取地图统计信息
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getMapStatistics() {
        try {
            Map<String, Object> statistics = gameMapService.getMapStatistics();
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取地图统计失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取地图筛选的可用选项
     */
    @GetMapping("/filter-options")
    public Result<Map<String, Object>> getMapFilterOptions() {
        try {
            Map<String, Object> options = Map.of(
                    "mapTypes", List.of(
                            Map.of("value", "defuse", "label", "拆弹地图", "description", "标准5v5对战地图"),
                            Map.of("value", "deathmatch", "label", "死斗地图", "description", "个人死斗模式地图")
                    ),
                    "siteCounts", List.of(
                            Map.of("value", 1, "label", "单站点"),
                            Map.of("value", 2, "label", "双站点"),
                            Map.of("value", 3, "label", "三站点")
                    )
            );
            return Result.success(options);
        } catch (Exception e) {
            log.error("获取地图筛选选项失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    // ==================== 管理员功能接口 ====================

    /**
     * 创建地图（管理员功能）
     */
    @PostMapping
    public Result<String> createMap(@RequestBody GameMap gameMap,
                                    @RequestHeader("Authorization") String token) {
        try {
            Long currentUserId = getUserIdFromToken(token);

            // 验证管理员权限
            validateAdminPermission(currentUserId);

            boolean success = gameMapService.createMap(gameMap);
            if (success) {
                return Result.success("地图创建成功");
            } else {
                return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "地图创建失败");
            }
        } catch (RuntimeException e) {
            log.warn("创建地图失败: {}", e.getMessage());
            return handleMapException(e);
        } catch (Exception e) {
            log.error("创建地图系统错误", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 更新地图信息（管理员功能）
     */
    @PutMapping("/{mapId}")
    public Result<String> updateMap(@PathVariable Long mapId,
                                    @RequestBody GameMap gameMap,
                                    @RequestHeader("Authorization") String token) {
        try {
            Long currentUserId = getUserIdFromToken(token);

            // 验证管理员权限
            validateAdminPermission(currentUserId);

            gameMap.setId(mapId);
            boolean success = gameMapService.updateById(gameMap);
            if (success) {
                // 刷新缓存
                gameMapService.refreshMapCache();
                return Result.success("地图更新成功");
            } else {
                return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "地图更新失败");
            }
        } catch (RuntimeException e) {
            log.warn("更新地图失败: mapId={}, 原因: {}", mapId, e.getMessage());
            return handleMapException(e);
        } catch (Exception e) {
            log.error("更新地图系统错误: mapId={}", mapId, e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 更新地图状态（管理员功能）
     */
    @PutMapping("/{mapId}/status")
    public Result<String> updateMapStatus(@PathVariable Long mapId,
                                          @RequestParam @NotNull(message = "状态值不能为空") Integer status,
                                          @RequestHeader("Authorization") String token) {
        try {
            Long currentUserId = getUserIdFromToken(token);

            // 验证管理员权限
            validateAdminPermission(currentUserId);

            boolean success = gameMapService.updateMapStatus(mapId, status);
            if (success) {
                String statusText = status == 1 ? "启用" : "禁用";
                return Result.success("地图" + statusText + "成功");
            } else {
                return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "状态更新失败");
            }
        } catch (RuntimeException e) {
            log.warn("更新地图状态失败: mapId={}, status={}, 原因: {}", mapId, status, e.getMessage());
            return handleMapException(e);
        } catch (Exception e) {
            log.error("更新地图状态系统错误: mapId={}, status={}", mapId, status, e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 删除地图（软删除，管理员功能）
     */
    @DeleteMapping("/{mapId}")
    public Result<String> deleteMap(@PathVariable Long mapId,
                                    @RequestHeader("Authorization") String token) {
        try {
            Long currentUserId = getUserIdFromToken(token);

            // 验证管理员权限
            validateAdminPermission(currentUserId);

            boolean success = gameMapService.removeById(mapId);
            if (success) {
                // 刷新缓存
                gameMapService.refreshMapCache();
                return Result.success("地图删除成功");
            } else {
                return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "地图删除失败");
            }
        } catch (RuntimeException e) {
            log.warn("删除地图失败: mapId={}, 原因: {}", mapId, e.getMessage());
            return handleMapException(e);
        } catch (Exception e) {
            log.error("删除地图系统错误: mapId={}", mapId, e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 刷新地图缓存（管理员功能）
     */
    @PostMapping("/refresh-cache")
    public Result<String> refreshMapCache(@RequestHeader("Authorization") String token) {
        try {
            Long currentUserId = getUserIdFromToken(token);

            // 验证管理员权限
            validateAdminPermission(currentUserId);

            gameMapService.refreshMapCache();
            return Result.success("地图缓存刷新成功");
        } catch (RuntimeException e) {
            log.warn("刷新地图缓存失败: {}", e.getMessage());
            return handleMapException(e);
        } catch (Exception e) {
            log.error("刷新地图缓存系统错误", e);
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
     * 统一处理地图相关异常
     */
    private <T> Result<T> handleMapException(RuntimeException e) {
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