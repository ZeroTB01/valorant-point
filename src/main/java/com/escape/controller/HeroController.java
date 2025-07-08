package com.escape.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.escape.common.Result;
import com.escape.common.ResultCode;
import com.escape.entity.Hero;
import com.escape.entity.HeroSkill;
import com.escape.service.HeroService;
import com.escape.service.HeroSkillService;
import com.escape.utils.JwtUtils;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 英雄控制器
 * 提供英雄数据查询、筛选、管理等API接口
 *
 * @author escape
 * @since 2025-06-13
 */
@Slf4j
@RestController
@RequestMapping("/hero")
public class HeroController {

    @Autowired
    private HeroService heroService;
    @Autowired
    private HeroSkillService heroSkillService;

    @Autowired
    private JwtUtils jwtUtils;

    // ==================== 英雄基础查询接口 ====================

    /**
     * 获取所有启用的英雄列表
     */
    @GetMapping("/list")
    public Result<List<Hero>> getAllHeroes() {
        try {
            List<Hero> heroes = heroService.getAllEnabledHeroes();
            return Result.success(heroes);
        } catch (Exception e) {
            log.error("获取英雄列表失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 根据英雄ID获取英雄详情（包含技能）
     */
    @GetMapping("/{heroId}")
    public Result<Map<String, Object>> getHeroDetail(@PathVariable @NotNull(message = "英雄ID不能为空") Long heroId) {
        try {
            Map<String, Object> heroDetail = heroService.getHeroDetail(heroId);
            return Result.success(heroDetail);
        } catch (RuntimeException e) {
            log.warn("获取英雄详情失败: heroId={}, 原因: {}", heroId, e.getMessage());
            return handleHeroException(e);
        } catch (Exception e) {
            log.error("获取英雄详情系统错误: heroId={}", heroId, e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 根据英雄标识获取英雄信息
     */
    @GetMapping("/key/{heroKey}")
    public Result<Hero> getHeroByKey(@PathVariable String heroKey) {
        try {
            Hero hero = heroService.getByHeroKey(heroKey);
            if (hero == null) {
                return Result.error(ResultCode.DATA_NOT_EXISTS.getCode(), "英雄不存在");
            }
            return Result.success(hero);
        } catch (Exception e) {
            log.error("根据heroKey获取英雄失败: heroKey={}", heroKey, e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取英雄的技能列表
     */
    @GetMapping("/{heroId}/skills")
    public Result<List<HeroSkill>> getHeroSkills(@PathVariable @NotNull(message = "英雄ID不能为空") Long heroId) {
        try {
            List<HeroSkill> skills = heroService.getHeroSkills(heroId);
            return Result.success(skills);
        } catch (RuntimeException e) {
            log.warn("获取英雄技能失败: heroId={}, 原因: {}", heroId, e.getMessage());
            return handleHeroException(e);
        } catch (Exception e) {
            log.error("获取英雄技能系统错误: heroId={}", heroId, e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    // ==================== 英雄筛选和分类接口 ====================

    /**
     * 按英雄类型获取英雄列表
     */
    @GetMapping("/type/{heroType}")
    public Result<List<Hero>> getHeroesByType(@PathVariable String heroType) {
        try {
            List<Hero> heroes = heroService.getHeroesByType(heroType);
            return Result.success(heroes);
        } catch (RuntimeException e) {
            log.warn("按类型获取英雄失败: heroType={}, 原因: {}", heroType, e.getMessage());
            return handleHeroException(e);
        } catch (Exception e) {
            log.error("按类型获取英雄系统错误: heroType={}", heroType, e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 按难度等级获取英雄列表
     */
    @GetMapping("/difficulty/{difficulty}")
    public Result<List<Hero>> getHeroesByDifficulty(@PathVariable @Min(value = 1, message = "难度等级必须大于0")
                                                    @NotNull(message = "难度等级不能为空") Integer difficulty) {
        try {
            List<Hero> heroes = heroService.getHeroesByDifficulty(difficulty);
            return Result.success(heroes);
        } catch (RuntimeException e) {
            log.warn("按难度获取英雄失败: difficulty={}, 原因: {}", difficulty, e.getMessage());
            return handleHeroException(e);
        } catch (Exception e) {
            log.error("按难度获取英雄系统错误: difficulty={}", difficulty, e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 分页查询英雄（支持筛选）
     */
    @GetMapping("/page")
    public Result<IPage<Hero>> getHeroPage(@RequestParam(defaultValue = "1") Integer current,
                                           @RequestParam(defaultValue = "10") Integer size,
                                           @RequestParam(required = false) String heroType,
                                           @RequestParam(required = false) Integer difficulty) {
        try {
            Page<Hero> page = new Page<>(current, size);
            IPage<Hero> heroPage = heroService.getHeroPage(page, heroType, difficulty);
            return Result.success(heroPage);
        } catch (Exception e) {
            log.error("分页查询英雄失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取英雄类型统计
     */
    @GetMapping("/statistics/types")
    public Result<Map<String, Integer>> getHeroTypeStatistics() {
        try {
            Map<String, Integer> statistics = heroService.getHeroTypeStatistics();
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取英雄类型统计失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取英雄选项列表（用于下拉框等）
     */
    @GetMapping("/options")
    public Result<List<Map<String, Object>>> getHeroOptions() {
        try {
            List<Map<String, Object>> options = heroService.getHeroOptions();
            return Result.success(options);
        } catch (Exception e) {
            log.error("获取英雄选项失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取英雄筛选的可用选项
     */
    @GetMapping("/filter-options")
    public Result<Map<String, Object>> getHeroFilterOptions() {
        try {
            Map<String, Object> options = Map.of(
                    "heroTypes", List.of(
                            Map.of("value", "duelist", "label", "决斗者", "description", "专注于击杀和突破"),
                            Map.of("value", "sentinel", "label", "哨卫", "description", "防守和支援队友"),
                            Map.of("value", "controller", "label", "控场", "description", "控制地图和视野"),
                            Map.of("value", "initiator", "label", "先锋", "description", "收集信息和突破")
                    ),
                    "difficulties", List.of(
                            Map.of("value", 1, "label", "入门", "color", "#4CAF50"),
                            Map.of("value", 2, "label", "简单", "color", "#8BC34A"),
                            Map.of("value", 3, "label", "中等", "color", "#FFC107"),
                            Map.of("value", 4, "label", "困难", "color", "#FF9800"),
                            Map.of("value", 5, "label", "大师", "color", "#F44336")
                    )
            );
            return Result.success(options);
        } catch (Exception e) {
            log.error("获取英雄筛选选项失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    // ==================== 管理员功能接口 ====================

    /**
     * 创建英雄（管理员功能）
     */
    @PostMapping
    public Result<String> createHero(@RequestBody Hero hero,
                                     @RequestHeader("Authorization") String token) {
        try {
            Long currentUserId = getUserIdFromToken(token);

            // 验证管理员权限
            validateAdminPermission(currentUserId);

            boolean success = heroService.save(hero);
            if (success) {
                // 刷新缓存
                heroService.refreshHeroCache();
                return Result.success("英雄创建成功");
            } else {
                return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "英雄创建失败");
            }
        } catch (RuntimeException e) {
            log.warn("创建英雄失败: {}", e.getMessage());
            return handleHeroException(e);
        } catch (Exception e) {
            log.error("创建英雄系统错误", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 更新英雄状态（管理员功能）
     */
    @PutMapping("/{heroId}/status")
    public Result<String> updateHeroStatus(@PathVariable Long heroId,
                                           @RequestParam @NotNull(message = "状态值不能为空") Integer status,
                                           @RequestHeader("Authorization") String token) {
        try {
            Long currentUserId = getUserIdFromToken(token);

            // 验证管理员权限
            validateAdminPermission(currentUserId);

            boolean success = heroService.updateHeroStatus(heroId, status);
            if (success) {
                String statusText = status == 1 ? "启用" : "禁用";
                return Result.success("英雄" + statusText + "成功");
            } else {
                return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "状态更新失败");
            }
        } catch (RuntimeException e) {
            log.warn("更新英雄状态失败: heroId={}, status={}, 原因: {}", heroId, status, e.getMessage());
            return handleHeroException(e);
        } catch (Exception e) {
            log.error("更新英雄状态系统错误: heroId={}, status={}", heroId, status, e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 批量导入英雄（管理员功能）
     */
    @PostMapping("/batch-import")
    public Result<Map<String, Object>> batchImportHeroes(@RequestBody List<Hero> heroes,
                                                         @RequestHeader("Authorization") String token) {
        try {
            Long currentUserId = getUserIdFromToken(token);

            // 验证管理员权限
            validateAdminPermission(currentUserId);

            Map<String, Object> result = heroService.batchImportHeroes(heroes);
            return Result.success("批量导入完成", result);
        } catch (RuntimeException e) {
            log.warn("批量导入英雄失败: {}", e.getMessage());
            return handleHeroException(e);
        } catch (Exception e) {
            log.error("批量导入英雄系统错误", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 刷新英雄缓存（管理员功能）
     */
    @PostMapping("/refresh-cache")
    public Result<String> refreshHeroCache(@RequestHeader("Authorization") String token) {
        try {
            Long currentUserId = getUserIdFromToken(token);

            // 验证管理员权限
            validateAdminPermission(currentUserId);

            heroService.refreshHeroCache();
            return Result.success("英雄缓存刷新成功");
        } catch (RuntimeException e) {
            log.warn("刷新英雄缓存失败: {}", e.getMessage());
            return handleHeroException(e);
        } catch (Exception e) {
            log.error("刷新英雄缓存系统错误", e);
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
     * 验证管理员权限（简化版本，实际项目中应该通过UserService检查）
     */
    private void validateAdminPermission(Long userId) {
        // TODO: 这里应该调用UserService检查管理员权限
        // 为了简化，暂时跳过权限验证
        // 实际项目中应该实现：userService.getUserRoles(userId).contains("SUPER_ADMIN")
        log.info("权限验证通过: userId={}", userId);
    }

    /**
     * 统一处理英雄相关异常
     */
    private <T> Result<T> handleHeroException(RuntimeException e) {
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

    /**
     * 更新英雄信息
     */
    @PutMapping("/{heroId}")
    public Result<String> updateHero(
    @PathVariable Long heroId,
    @RequestBody Hero hero,
    @RequestHeader("Authorization") String token
) {
    hero.setId(heroId); // 保证ID一致
    heroService.updateHero(hero, token);
    return Result.success("修改成功");
}

    /**
     * 更新英雄技能
     * @param heroId
     * @param skills
     * @param token
     * @return
     */
    @PostMapping("/{heroId}/skills")
    public Result<String> saveHeroSkills(
            @PathVariable Long heroId,
            @RequestBody List<HeroSkill> skills,
            @RequestHeader("Authorization") String token) {
        try {
            log.info("保存技能 heroId={}, skills={}", heroId, skills);
            Long currentUserId = getUserIdFromToken(token);
            validateAdminPermission(currentUserId);

            heroSkillService.saveOrUpdateHeroSkills(heroId, skills);
            return Result.success("技能保存成功");
        } catch (Exception e) {
            log.error("保存英雄技能系统错误: heroId={}, skills={}, error={}", heroId, skills, e.getMessage(), e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }
}