package com.escape.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.escape.common.Result;
import com.escape.common.ResultCode;
import com.escape.entity.Weapon;
import com.escape.service.WeaponService;
import com.escape.utils.JwtUtils;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 武器控制器
 * 提供武器数据查询、管理等API接口
 *
 * @author escape
 * @since 2025-06-14
 */
@Slf4j
@RestController
@RequestMapping("/weapon")
public class WeaponController {

    @Autowired
    private WeaponService weaponService;

    @Autowired
    private JwtUtils jwtUtils;

    // ==================== 武器基础查询接口 ====================

    /**
     * 获取所有启用的武器列表
     */
    @GetMapping("/list")
    public Result<List<Weapon>> getAllWeapons() {
        try {
            List<Weapon> weapons = weaponService.getAllEnabledWeapons();
            return Result.success(weapons);
        } catch (Exception e) {
            log.error("获取武器列表失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 根据武器ID获取武器详情
     */
    @GetMapping("/{weaponId}")
    public Result<Map<String, Object>> getWeaponDetail(@PathVariable @NotNull(message = "武器ID不能为空") Long weaponId) {
        try {
            Map<String, Object> weaponDetail = weaponService.getWeaponDetail(weaponId);
            return Result.success(weaponDetail);
        } catch (RuntimeException e) {
            log.warn("获取武器详情失败: weaponId={}, 原因: {}", weaponId, e.getMessage());
            return handleWeaponException(e);
        } catch (Exception e) {
            log.error("获取武器详情系统错误: weaponId={}", weaponId, e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 根据武器标识获取武器信息
     */
    @GetMapping("/key/{weaponKey}")
    public Result<Weapon> getWeaponByKey(@PathVariable String weaponKey) {
        try {
            Weapon weapon = weaponService.getByWeaponKey(weaponKey);
            if (weapon == null) {
                return Result.error(ResultCode.DATA_NOT_EXISTS.getCode(), "武器不存在");
            }
            return Result.success(weapon);
        } catch (Exception e) {
            log.error("根据weaponKey获取武器失败: weaponKey={}", weaponKey, e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    // ==================== 武器筛选和分类接口 ====================

    /**
     * 按武器类型获取武器列表
     */
    @GetMapping("/type/{weaponType}")
    public Result<List<Weapon>> getWeaponsByType(@PathVariable String weaponType) {
        try {
            List<Weapon> weapons = weaponService.getWeaponsByType(weaponType);
            return Result.success(weapons);
        } catch (RuntimeException e) {
            log.warn("按类型获取武器失败: weaponType={}, 原因: {}", weaponType, e.getMessage());
            return handleWeaponException(e);
        } catch (Exception e) {
            log.error("按类型获取武器系统错误: weaponType={}", weaponType, e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 按价格范围获取武器列表
     */
    @GetMapping("/price-range")
    public Result<List<Weapon>> getWeaponsByPriceRange(@RequestParam @NotNull(message = "最低价格不能为空") Integer minPrice,
                                                       @RequestParam @NotNull(message = "最高价格不能为空") Integer maxPrice) {
        try {
            List<Weapon> weapons = weaponService.getWeaponsByPriceRange(minPrice, maxPrice);
            return Result.success(weapons);
        } catch (RuntimeException e) {
            log.warn("按价格范围获取武器失败: minPrice={}, maxPrice={}, 原因: {}", minPrice, maxPrice, e.getMessage());
            return handleWeaponException(e);
        } catch (Exception e) {
            log.error("按价格范围获取武器系统错误: minPrice={}, maxPrice={}", minPrice, maxPrice, e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 按穿透等级获取武器列表
     */
    @GetMapping("/penetration/{penetration}")
    public Result<List<Weapon>> getWeaponsByPenetration(@PathVariable String penetration) {
        try {
            List<Weapon> weapons = weaponService.getWeaponsByPenetration(penetration);
            return Result.success(weapons);
        } catch (RuntimeException e) {
            log.warn("按穿透等级获取武器失败: penetration={}, 原因: {}", penetration, e.getMessage());
            return handleWeaponException(e);
        } catch (Exception e) {
            log.error("按穿透等级获取武器系统错误: penetration={}", penetration, e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 分页查询武器（支持筛选）
     */
    @GetMapping("/page")
    public Result<IPage<Weapon>> getWeaponPage(@RequestParam(defaultValue = "1") Integer current,
                                               @RequestParam(defaultValue = "10") Integer size,
                                               @RequestParam(required = false) String weaponType,
                                               @RequestParam(required = false) Integer minPrice,
                                               @RequestParam(required = false) Integer maxPrice) {
        try {
            Page<Weapon> page = new Page<>(current, size);
            IPage<Weapon> weaponPage = weaponService.getWeaponPage(page, weaponType, minPrice, maxPrice);
            return Result.success(weaponPage);
        } catch (Exception e) {
            log.error("分页查询武器失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取武器类型统计
     */
    @GetMapping("/statistics/types")
    public Result<List<Map<String, Object>>> getWeaponTypeStatistics() {
        try {
            List<Map<String, Object>> statistics = weaponService.getWeaponTypeStatistics();
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取武器类型统计失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取武器选项列表（用于下拉框等）
     */
    @GetMapping("/options")
    public Result<List<Map<String, Object>>> getWeaponOptions() {
        try {
            List<Map<String, Object>> options = weaponService.getWeaponOptions();
            return Result.success(options);
        } catch (Exception e) {
            log.error("获取武器选项失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 武器对比
     */
    @PostMapping("/compare")
    public Result<List<Map<String, Object>>> compareWeapons(@RequestBody List<Long> weaponIds) {
        try {
            List<Map<String, Object>> compareData = weaponService.compareWeapons(weaponIds);
            return Result.success(compareData);
        } catch (RuntimeException e) {
            log.warn("武器对比失败: {}", e.getMessage());
            return handleWeaponException(e);
        } catch (Exception e) {
            log.error("武器对比系统错误", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取武器筛选的可用选项
     */
    @GetMapping("/filter-options")
    public Result<Map<String, Object>> getWeaponFilterOptions() {
        try {
            Map<String, Object> options = Map.of(
                    "weaponTypes", List.of(
                            Map.of("value", "sidearm", "label", "手枪", "icon", "pistol"),
                            Map.of("value", "smg", "label", "冲锋枪", "icon", "smg"),
                            Map.of("value", "rifle", "label", "步枪", "icon", "rifle"),
                            Map.of("value", "sniper", "label", "狙击枪", "icon", "sniper"),
                            Map.of("value", "heavy", "label", "重武器", "icon", "heavy"),
                            Map.of("value", "melee", "label", "近战武器", "icon", "melee")
                    ),
                    "priceRanges", List.of(
                            Map.of("label", "免费", "min", 0, "max", 0),
                            Map.of("label", "0-500", "min", 0, "max", 500),
                            Map.of("label", "500-1000", "min", 500, "max", 1000),
                            Map.of("label", "1000-2000", "min", 1000, "max", 2000),
                            Map.of("label", "2000-3000", "min", 2000, "max", 3000),
                            Map.of("label", "3000+", "min", 3000, "max", 99999)
                    ),
                    "penetrationLevels", List.of(
                            Map.of("value", "low", "label", "低穿透", "color", "#4CAF50"),
                            Map.of("value", "medium", "label", "中穿透", "color", "#FFC107"),
                            Map.of("value", "high", "label", "高穿透", "color", "#F44336")
                    )
            );
            return Result.success(options);
        } catch (Exception e) {
            log.error("获取武器筛选选项失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    // ==================== 管理员功能接口 ====================

    /**
     * 创建武器（管理员功能）
     */
    @PostMapping
    public Result<String> createWeapon(@RequestBody Weapon weapon,
                                       @RequestHeader("Authorization") String token) {
        try {
            Long currentUserId = getUserIdFromToken(token);

            // 验证管理员权限
            validateAdminPermission(currentUserId);

            boolean success = weaponService.createWeapon(weapon);
            if (success) {
                return Result.success("武器创建成功");
            } else {
                return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "武器创建失败");
            }
        } catch (RuntimeException e) {
            log.warn("创建武器失败: {}", e.getMessage());
            return handleWeaponException(e);
        } catch (Exception e) {
            log.error("创建武器系统错误", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 更新武器信息（管理员功能）
     */
    @PutMapping("/{weaponId}")
    public Result<String> updateWeapon(@PathVariable Long weaponId,
                                       @RequestBody Weapon weapon,
                                       @RequestHeader("Authorization") String token) {
        try {
            Long currentUserId = getUserIdFromToken(token);

            // 验证管理员权限
            validateAdminPermission(currentUserId);

            weapon.setId(weaponId);
            boolean success = weaponService.updateById(weapon);
            if (success) {
                // 刷新缓存
                weaponService.refreshWeaponCache();
                return Result.success("武器更新成功");
            } else {
                return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "武器更新失败");
            }
        } catch (RuntimeException e) {
            log.warn("更新武器失败: weaponId={}, 原因: {}", weaponId, e.getMessage());
            return handleWeaponException(e);
        } catch (Exception e) {
            log.error("更新武器系统错误: weaponId={}", weaponId, e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 更新武器价格（管理员功能）
     */
    @PutMapping("/{weaponId}/price")
    public Result<String> updateWeaponPrice(@PathVariable Long weaponId,
                                            @RequestParam @NotNull(message = "价格不能为空") Integer price,
                                            @RequestHeader("Authorization") String token) {
        try {
            Long currentUserId = getUserIdFromToken(token);

            // 验证管理员权限
            validateAdminPermission(currentUserId);

            boolean success = weaponService.updateWeaponPrice(weaponId, price);
            if (success) {
                return Result.success("武器价格更新成功");
            } else {
                return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "价格更新失败");
            }
        } catch (RuntimeException e) {
            log.warn("更新武器价格失败: weaponId={}, price={}, 原因: {}", weaponId, price, e.getMessage());
            return handleWeaponException(e);
        } catch (Exception e) {
            log.error("更新武器价格系统错误: weaponId={}, price={}", weaponId, price, e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 更新武器状态（管理员功能）
     */
    @PutMapping("/{weaponId}/status")
    public Result<String> updateWeaponStatus(@PathVariable Long weaponId,
                                             @RequestParam @NotNull(message = "状态值不能为空") Integer status,
                                             @RequestHeader("Authorization") String token) {
        try {
            Long currentUserId = getUserIdFromToken(token);

            // 验证管理员权限
            validateAdminPermission(currentUserId);

            boolean success = weaponService.updateWeaponStatus(weaponId, status);
            if (success) {
                String statusText = status == 1 ? "启用" : "禁用";
                return Result.success("武器" + statusText + "成功");
            } else {
                return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "状态更新失败");
            }
        } catch (RuntimeException e) {
            log.warn("更新武器状态失败: weaponId={}, status={}, 原因: {}", weaponId, status, e.getMessage());
            return handleWeaponException(e);
        } catch (Exception e) {
            log.error("更新武器状态系统错误: weaponId={}, status={}", weaponId, status, e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 批量导入武器（管理员功能）
     */
    @PostMapping("/batch-import")
    public Result<Map<String, Object>> batchImportWeapons(@RequestBody List<Weapon> weapons,
                                                          @RequestHeader("Authorization") String token) {
        try {
            Long currentUserId = getUserIdFromToken(token);

            // 验证管理员权限
            validateAdminPermission(currentUserId);

            Map<String, Object> result = weaponService.batchImportWeapons(weapons);
            return Result.success("批量导入完成", result);
        } catch (RuntimeException e) {
            log.warn("批量导入武器失败: {}", e.getMessage());
            return handleWeaponException(e);
        } catch (Exception e) {
            log.error("批量导入武器系统错误", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 删除武器（软删除，管理员功能）
     */
    @DeleteMapping("/{weaponId}")
    public Result<String> deleteWeapon(@PathVariable Long weaponId,
                                       @RequestHeader("Authorization") String token) {
        try {
            Long currentUserId = getUserIdFromToken(token);

            // 验证管理员权限
            validateAdminPermission(currentUserId);

            boolean success = weaponService.removeById(weaponId);
            if (success) {
                // 刷新缓存
                weaponService.refreshWeaponCache();
                return Result.success("武器删除成功");
            } else {
                return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "武器删除失败");
            }
        } catch (RuntimeException e) {
            log.warn("删除武器失败: weaponId={}, 原因: {}", weaponId, e.getMessage());
            return handleWeaponException(e);
        } catch (Exception e) {
            log.error("删除武器系统错误: weaponId={}", weaponId, e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 刷新武器缓存（管理员功能）
     */
    @PostMapping("/refresh-cache")
    public Result<String> refreshWeaponCache(@RequestHeader("Authorization") String token) {
        try {
            Long currentUserId = getUserIdFromToken(token);

            // 验证管理员权限
            validateAdminPermission(currentUserId);

            weaponService.refreshWeaponCache();
            return Result.success("武器缓存刷新成功");
        } catch (RuntimeException e) {
            log.warn("刷新武器缓存失败: {}", e.getMessage());
            return handleWeaponException(e);
        } catch (Exception e) {
            log.error("刷新武器缓存系统错误", e);
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
     * 统一处理武器相关异常
     */
    private <T> Result<T> handleWeaponException(RuntimeException e) {
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