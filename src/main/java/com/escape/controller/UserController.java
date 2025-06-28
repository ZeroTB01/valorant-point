package com.escape.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.escape.common.Result;
import com.escape.common.ResultCode;
import com.escape.dto.response.UserInfoResponse;
import com.escape.dto.response.UserPreferencesResponse;
import com.escape.entity.User;
import com.escape.service.UserService;
import com.escape.utils.IpUtils;
import com.escape.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户管理控制器
 * 提供用户信息管理、偏好设置、状态管理等API接口
 *
 * @author escape
 * @since 2025-06-13
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    // ==================== 用户基本信息管理 ====================

    /**
     * 获取当前用户信息
     */
    @GetMapping("/profile")
    public Result<UserInfoResponse> getCurrentUserProfile(@RequestHeader("Authorization") String token) {
        try {
            Long userId = getUserIdFromToken(token);
            UserInfoResponse userInfo = userService.getUserInfo(userId);
            return Result.success(userInfo);
        } catch (RuntimeException e) {
            log.warn("获取用户信息失败: {}", e.getMessage());
            return handleUserException(e);
        } catch (Exception e) {
            log.error("获取用户信息系统错误", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 根据用户ID获取用户信息（管理员功能）
     */
    @GetMapping("/{userId}")
    public Result<UserInfoResponse> getUserById(@PathVariable Long userId,
                                                @RequestHeader("Authorization") String token) {
        try {
            Long currentUserId = getUserIdFromToken(token);

            // 权限验证：只有管理员或用户本人可以查看
            if (!canOperateUser(currentUserId, userId)) {
                return Result.error(ResultCode.PERMISSION_DENIED);
            }

            UserInfoResponse userInfo = userService.getUserInfo(userId);
            return Result.success(userInfo);
        } catch (RuntimeException e) {
            log.warn("获取用户信息失败: userId={}, 原因: {}", userId, e.getMessage());
            return handleUserException(e);
        } catch (Exception e) {
            log.error("获取用户信息系统错误: userId={}", userId, e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 更新用户基本信息
     */
    @PutMapping("/profile")
    public Result<String> updateUserProfile(@RequestHeader("Authorization") String token,
                                            @RequestParam(required = false) String nickname,
                                            @RequestParam(required = false) String avatar,
                                            @RequestParam(required = false) String phone) {
        try {
            Long userId = getUserIdFromToken(token);

            if (userId == -1L) {
                return Result.error(ResultCode.PERMISSION_DENIED.getCode(), "游客用户无法修改信息");
            }

            boolean success = userService.updateUserProfile(userId, nickname, avatar, phone);
            if (success) {
                return Result.success("用户信息更新成功");
            } else {
                return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "用户信息更新失败");
            }
        } catch (RuntimeException e) {
            log.warn("更新用户信息失败: {}", e.getMessage());
            return handleUserException(e);
        } catch (Exception e) {
            log.error("更新用户信息系统错误", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 上传用户头像
     */
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@RequestHeader("Authorization") String token,
                                       @RequestParam("file") MultipartFile file) {
        try {
            Long userId = getUserIdFromToken(token);

            if (userId == -1L) {
                return Result.error(ResultCode.PERMISSION_DENIED.getCode(), "游客用户无法上传头像");
            }

            // TODO: 实现文件上传逻辑（可以上传到本地或OSS）
            // 这里暂时返回一个模拟的头像URL
            String avatarUrl = "https://example.com/avatars/" + userId + "_" + System.currentTimeMillis() + ".jpg";

            boolean success = userService.updateUserAvatar(userId, avatarUrl);
            if (success) {
                return Result.success("头像上传成功", avatarUrl);
            } else {
                return Result.error(ResultCode.FILE_UPLOAD_FAILED);
            }
        } catch (RuntimeException e) {
            log.warn("上传头像失败: {}", e.getMessage());
            return handleUserException(e);
        } catch (Exception e) {
            log.error("上传头像系统错误", e);
            return Result.error(ResultCode.FILE_UPLOAD_FAILED);
        }
    }

    // ==================== 用户偏好设置管理 ====================

    /**
     * 获取用户偏好设置
     */
    @GetMapping("/preferences")
    public Result<UserPreferencesResponse> getUserPreferences(@RequestHeader("Authorization") String token) {
        try {
            Long userId = getUserIdFromToken(token);
            UserPreferencesResponse preferences = userService.getUserPreferences(userId);
            return Result.success(preferences);
        } catch (RuntimeException e) {
            log.warn("获取用户偏好失败: {}", e.getMessage());
            return handleUserException(e);
        } catch (Exception e) {
            log.error("获取用户偏好系统错误", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 更新用户偏好设置
     */
    @PutMapping("/preferences")
    public Result<String> updateUserPreferences(@RequestHeader("Authorization") String token,
                                                @RequestBody UserPreferencesResponse preferences) {
        try {
            Long userId = getUserIdFromToken(token);

            if (userId == -1L) {
                return Result.error(ResultCode.PERMISSION_DENIED.getCode(), "游客用户无法保存偏好设置");
            }

            boolean success = userService.updateUserPreferences(userId, preferences);
            if (success) {
                return Result.success("偏好设置更新成功");
            } else {
                return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "偏好设置更新失败");
            }
        } catch (RuntimeException e) {
            log.warn("更新用户偏好失败: {}", e.getMessage());
            return handleUserException(e);
        } catch (Exception e) {
            log.error("更新用户偏好系统错误", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    // ==================== 便捷的偏好设置管理 ====================

    /**
     * 快速切换主题模式
     */
    @PutMapping("/preferences/theme")
    public Result<String> switchThemeMode(@RequestHeader("Authorization") String token,
                                          @RequestParam @NotBlank(message = "主题模式不能为空") String themeMode) {
        try {
            Long userId = getUserIdFromToken(token);

            if (userId == -1L) {
                return Result.error(ResultCode.PERMISSION_DENIED.getCode(), "游客用户无法保存主题设置");
            }

            // 验证主题模式
            if (!"light".equals(themeMode) && !"dark".equals(themeMode)) {
                return Result.error(ResultCode.BAD_REQUEST.getCode(), "无效的主题模式，只支持 light 或 dark");
            }

            // 获取当前偏好设置
            UserPreferencesResponse currentPreferences = userService.getUserPreferences(userId);
            currentPreferences.setThemeMode(themeMode);

            // 更新偏好设置
            boolean success = userService.updateUserPreferences(userId, currentPreferences);
            if (success) {
                String themeName = "dark".equals(themeMode) ? "深色主题" : "浅色主题";
                return Result.success("已切换至" + themeName);
            } else {
                return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "主题切换失败");
            }
        } catch (RuntimeException e) {
            log.warn("切换主题模式失败: {}", e.getMessage());
            return handleUserException(e);
        } catch (Exception e) {
            log.error("切换主题模式系统错误", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 快速设置视频质量偏好
     */
    @PutMapping("/preferences/video-quality")
    public Result<String> setVideoQuality(@RequestHeader("Authorization") String token,
                                          @RequestParam @NotBlank(message = "视频质量不能为空") String videoQuality) {
        try {
            Long userId = getUserIdFromToken(token);

            if (userId == -1L) {
                return Result.error(ResultCode.PERMISSION_DENIED.getCode(), "游客用户无法保存视频质量设置");
            }

            // 验证视频质量参数
            List<String> validQualities = List.of("auto", "720p", "1080p", "4k");
            if (!validQualities.contains(videoQuality)) {
                return Result.error(ResultCode.BAD_REQUEST.getCode(),
                        "无效的视频质量，支持的选项：" + String.join(", ", validQualities));
            }

            // 获取当前偏好设置
            UserPreferencesResponse currentPreferences = userService.getUserPreferences(userId);
            currentPreferences.setVideoQuality(videoQuality);

            // 更新偏好设置
            boolean success = userService.updateUserPreferences(userId, currentPreferences);
            if (success) {
                return Result.success("视频质量偏好已设置为 " + videoQuality);
            } else {
                return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "视频质量设置失败");
            }
        } catch (RuntimeException e) {
            log.warn("设置视频质量失败: {}", e.getMessage());
            return handleUserException(e);
        } catch (Exception e) {
            log.error("设置视频质量系统错误", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 切换通知设置
     */
    @PutMapping("/preferences/notifications")
    public Result<String> toggleNotifications(@RequestHeader("Authorization") String token,
                                              @RequestParam @NotNull(message = "通知设置不能为空") Boolean emailNotification) {
        try {
            Long userId = getUserIdFromToken(token);

            if (userId == -1L) {
                return Result.error(ResultCode.PERMISSION_DENIED.getCode(), "游客用户无法保存通知设置");
            }

            // 获取当前偏好设置
            UserPreferencesResponse currentPreferences = userService.getUserPreferences(userId);
            currentPreferences.setNotificationEmail(emailNotification);

            // 更新偏好设置
            boolean success = userService.updateUserPreferences(userId, currentPreferences);
            if (success) {
                String status = emailNotification ? "开启" : "关闭";
                return Result.success("邮件通知已" + status);
            } else {
                return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "通知设置失败");
            }
        } catch (RuntimeException e) {
            log.warn("切换通知设置失败: {}", e.getMessage());
            return handleUserException(e);
        } catch (Exception e) {
            log.error("切换通知设置系统错误", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 重置偏好设置为默认值
     */
    @PostMapping("/preferences/reset")
    public Result<String> resetPreferences(@RequestHeader("Authorization") String token) {
        try {
            Long userId = getUserIdFromToken(token);

            if (userId == -1L) {
                return Result.error(ResultCode.PERMISSION_DENIED.getCode(), "游客用户无法重置偏好设置");
            }

            // 创建默认偏好设置
            UserPreferencesResponse defaultPreferences = new UserPreferencesResponse();
            defaultPreferences.setThemeMode("dark");
            defaultPreferences.setVideoQuality("720p");
            defaultPreferences.setLanguage("zh-CN");
            defaultPreferences.setNotificationEmail(true);
            defaultPreferences.setAutoPlayVideo(true);

            // 更新偏好设置
            boolean success = userService.updateUserPreferences(userId, defaultPreferences);
            if (success) {
                return Result.success("偏好设置已重置为默认值");
            } else {
                return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "重置偏好设置失败");
            }
        } catch (RuntimeException e) {
            log.warn("重置偏好设置失败: {}", e.getMessage());
            return handleUserException(e);
        } catch (Exception e) {
            log.error("重置偏好设置系统错误", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取可用的偏好设置选项
     */
    @GetMapping("/preferences/options")
    public Result<Map<String, Object>> getPreferenceOptions() {
        try {
            Map<String, Object> options = new HashMap<>();

            // 主题选项
            options.put("themeOptions", List.of(
                    Map.of("value", "light", "label", "浅色主题", "description", "适合白天使用"),
                    Map.of("value", "dark", "label", "深色主题", "description", "适合夜间使用，保护视力")
            ));

            // 视频质量选项
            options.put("videoQualityOptions", List.of(
                    Map.of("value", "auto", "label", "自动", "description", "根据网络状况自动调整"),
                    Map.of("value", "720p", "label", "720P", "description", "标清画质，节省流量"),
                    Map.of("value", "1080p", "label", "1080P", "description", "高清画质，推荐设置"),
                    Map.of("value", "4k", "label", "4K", "description", "超高清画质，需要高速网络")
            ));

            // 语言选项
            options.put("languageOptions", List.of(
                    Map.of("value", "zh-CN", "label", "简体中文"),
                    Map.of("value", "zh-TW", "label", "繁体中文"),
                    Map.of("value", "en-US", "label", "English")
            ));

            return Result.success(options);
        } catch (Exception e) {
            log.error("获取偏好设置选项失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    // ==================== 用户角色管理 ====================

    /**
     * 获取用户角色列表
     */
    @GetMapping("/roles")
    public Result<List<String>> getUserRoles(@RequestHeader("Authorization") String token) {
        try {
            Long userId = getUserIdFromToken(token);
            List<String> roles = userService.getUserRoles(userId);
            return Result.success(roles);
        } catch (RuntimeException e) {
            log.warn("获取用户角色失败: {}", e.getMessage());
            return handleUserException(e);
        } catch (Exception e) {
            log.error("获取用户角色系统错误", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 为用户分配角色（管理员功能）
     */
    @PostMapping("/{userId}/roles")
    public Result<String> assignUserRole(@PathVariable Long userId,
                                         @RequestParam @NotBlank(message = "角色标识不能为空") String roleKey,
                                         @RequestHeader("Authorization") String token) {
        try {
            Long currentUserId = getUserIdFromToken(token);

            // 验证管理员权限
            validateAdminPermission(currentUserId);

            boolean success = userService.assignUserRole(userId, roleKey);
            if (success) {
                return Result.success("角色分配成功");
            } else {
                return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "角色分配失败");
            }
        } catch (RuntimeException e) {
            log.warn("分配用户角色失败: userId={}, roleKey={}, 原因: {}", userId, roleKey, e.getMessage());
            return handleUserException(e);
        } catch (Exception e) {
            log.error("分配用户角色系统错误: userId={}, roleKey={}", userId, roleKey, e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 移除用户角色（管理员功能）
     */
    @DeleteMapping("/{userId}/roles")
    public Result<String> removeUserRole(@PathVariable Long userId,
                                         @RequestParam @NotBlank(message = "角色标识不能为空") String roleKey,
                                         @RequestHeader("Authorization") String token) {
        try {
            Long currentUserId = getUserIdFromToken(token);

            // 验证管理员权限
            validateAdminPermission(currentUserId);

            boolean success = userService.removeUserRole(userId, roleKey);
            if (success) {
                return Result.success("角色移除成功");
            } else {
                return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "角色移除失败");
            }
        } catch (RuntimeException e) {
            log.warn("移除用户角色失败: userId={}, roleKey={}, 原因: {}", userId, roleKey, e.getMessage());
            return handleUserException(e);
        } catch (Exception e) {
            log.error("移除用户角色系统错误: userId={}, roleKey={}", userId, roleKey, e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取可分配的角色列表（管理员功能）
     */
    @GetMapping("/roles/available")
    public Result<List<Map<String, Object>>> getAvailableRoles(@RequestHeader("Authorization") String token) {
        try {
            Long currentUserId = getUserIdFromToken(token);

            // 验证管理员权限
            validateAdminPermission(currentUserId);

            List<Map<String, Object>> roles = List.of(
                    Map.of("roleKey", "USER", "roleName", "普通用户", "description", "基本用户权限"),
                    Map.of("roleKey", "CONTENT_ADMIN", "roleName", "内容管理员", "description", "管理内容和数据"),
                    Map.of("roleKey", "SUPER_ADMIN", "roleName", "超级管理员", "description", "系统最高权限")
            );

            return Result.success(roles);
        } catch (RuntimeException e) {
            log.warn("获取可分配角色失败: {}", e.getMessage());
            return handleUserException(e);
        } catch (Exception e) {
            log.error("获取可分配角色系统错误", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    // ==================== 用户查询和统计 ====================

    /**
     * 分页查询用户列表（管理员功能）
     */
    @GetMapping("/list")
    public Result<IPage<User>> getUserList(@RequestParam(defaultValue = "1") Integer current,
                                           @RequestParam(defaultValue = "10") Integer size,
                                           @RequestParam(required = false) String keyword,
                                           @RequestParam(required = false) Integer status,
                                           @RequestHeader("Authorization") String token) {
        try {
            Long currentUserId = getUserIdFromToken(token);

            // 验证管理员权限
            validateAdminPermission(currentUserId);

            Page<User> page = new Page<>(current, size);
            IPage<User> userPage = userService.getUserPage(page, keyword, status);
            return Result.success(userPage);
        } catch (RuntimeException e) {
            log.warn("查询用户列表失败: {}", e.getMessage());
            return handleUserException(e);
        } catch (Exception e) {
            log.error("查询用户列表失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取用户统计信息（管理员功能）
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getUserStatistics(@RequestHeader("Authorization") String token) {
        try {
            Long currentUserId = getUserIdFromToken(token);

            // 验证管理员权限
            validateAdminPermission(currentUserId);

            Map<String, Object> statistics = userService.getUserStatistics();
            return Result.success(statistics);
        } catch (RuntimeException e) {
            log.warn("获取用户统计失败: {}", e.getMessage());
            return handleUserException(e);
        } catch (Exception e) {
            log.error("获取用户统计失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取用户活动摘要
     */
    @GetMapping("/activity-summary")
    public Result<Map<String, Object>> getUserActivitySummary(@RequestHeader("Authorization") String token) {
        try {
            Long userId = getUserIdFromToken(token);

            if (userId == -1L) {
                return Result.success("游客用户无活动数据", Map.of());
            }

            Map<String, Object> summary = userService.getUserActivitySummary(userId);
            return Result.success(summary);
        } catch (RuntimeException e) {
            log.warn("获取用户活动摘要失败: {}", e.getMessage());
            return handleUserException(e);
        } catch (Exception e) {
            log.error("获取用户活动摘要系统错误", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    // ==================== 用户状态管理 ====================

    /**
     * 更新用户状态（管理员功能）
     */
    @PutMapping("/{userId}/status")
    public Result<String> updateUserStatus(@PathVariable Long userId,
                                           @RequestParam @NotNull(message = "状态值不能为空") Integer status,
                                           @RequestHeader("Authorization") String token) {
        try {
            Long currentUserId = getUserIdFromToken(token);

            // 验证管理员权限
            validateAdminPermission(currentUserId);

            boolean success = userService.updateUserStatus(userId, status);
            if (success) {
                String statusText = status == 1 ? "启用" : "禁用";
                return Result.success("用户" + statusText + "成功");
            } else {
                return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "状态更新失败");
            }
        } catch (RuntimeException e) {
            log.warn("更新用户状态失败: userId={}, status={}, 原因: {}", userId, status, e.getMessage());
            return handleUserException(e);
        } catch (Exception e) {
            log.error("更新用户状态系统错误: userId={}, status={}", userId, status, e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 删除用户（软删除，管理员功能）
     */
    @DeleteMapping("/{userId}")
    public Result<String> deleteUser(@PathVariable Long userId,
                                     @RequestHeader("Authorization") String token) {
        try {
            Long currentUserId = getUserIdFromToken(token);

            // 验证管理员权限
            validateAdminPermission(currentUserId);

            // 防止删除自己
            if (currentUserId.equals(userId)) {
                return Result.error(ResultCode.BAD_REQUEST.getCode(), "不能删除自己的账户");
            }

            boolean success = userService.deleteUser(userId);
            if (success) {
                return Result.success("用户删除成功");
            } else {
                return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "用户删除失败");
            }
        } catch (RuntimeException e) {
            log.warn("删除用户失败: userId={}, 原因: {}", userId, e.getMessage());
            return handleUserException(e);
        } catch (Exception e) {
            log.error("删除用户系统错误: userId={}", userId, e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    // ==================== 批量操作接口 ====================

    /**
     * 批量更新用户状态（管理员功能）
     */
    @PutMapping("/batch/status")
    public Result<Map<String, Object>> batchUpdateUserStatus(@RequestParam @NotNull(message = "状态值不能为空") Integer status,
                                                             @RequestBody List<Long> userIds,
                                                             @RequestHeader("Authorization") String token) {
        try {
            Long currentUserId = getUserIdFromToken(token);

            // 验证管理员权限
            validateAdminPermission(currentUserId);

            if (userIds == null || userIds.isEmpty()) {
                return Result.error(ResultCode.BAD_REQUEST.getCode(), "用户ID列表不能为空");
            }

            // 防止操作自己
            userIds.remove(currentUserId);

            int successCount = userService.batchUpdateUserStatus(userIds, status);

            Map<String, Object> result = new HashMap<>();
            result.put("totalCount", userIds.size());
            result.put("successCount", successCount);
            result.put("failCount", userIds.size() - successCount);

            String statusText = status == 1 ? "启用" : "禁用";
            return Result.success("批量" + statusText + "完成", result);
        } catch (RuntimeException e) {
            log.warn("批量更新用户状态失败: {}", e.getMessage());
            return handleUserException(e);
        } catch (Exception e) {
            log.error("批量更新用户状态系统错误", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 批量删除用户（管理员功能）
     */
    @DeleteMapping("/batch")
    public Result<Map<String, Object>> batchDeleteUsers(@RequestBody List<Long> userIds,
                                                        @RequestHeader("Authorization") String token) {
        try {
            Long currentUserId = getUserIdFromToken(token);

            // 验证管理员权限
            validateAdminPermission(currentUserId);

            if (userIds == null || userIds.isEmpty()) {
                return Result.error(ResultCode.BAD_REQUEST.getCode(), "用户ID列表不能为空");
            }

            // 防止删除自己
            userIds.remove(currentUserId);

            int successCount = userService.batchDeleteUsers(userIds);

            Map<String, Object> result = new HashMap<>();
            result.put("totalCount", userIds.size());
            result.put("successCount", successCount);
            result.put("failCount", userIds.size() - successCount);

            return Result.success("批量删除完成", result);
        } catch (RuntimeException e) {
            log.warn("批量删除用户失败: {}", e.getMessage());
            return handleUserException(e);
        } catch (Exception e) {
            log.error("批量删除用户系统错误", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    // ==================== 缓存管理 ====================

    /**
     * 刷新用户缓存
     */
    @PostMapping("/refresh-cache")
    public Result<String> refreshUserCache(@RequestHeader("Authorization") String token) {
        try {
            Long userId = getUserIdFromToken(token);
            userService.refreshUserCache(userId);
            return Result.success("缓存刷新成功");
        } catch (RuntimeException e) {
            log.warn("刷新用户缓存失败: {}", e.getMessage());
            return handleUserException(e);
        } catch (Exception e) {
            log.error("刷新用户缓存系统错误", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    // ==================== 权限验证方法 ====================

    /**
     * 检查用户是否有管理员权限
     */
    private boolean hasAdminPermission(Long userId) {
        try {
            List<String> roles = userService.getUserRoles(userId);
            return roles.contains("SUPER_ADMIN") || roles.contains("CONTENT_ADMIN");
        } catch (Exception e) {
            log.error("检查管理员权限失败: userId={}", userId, e);
            return false;
        }
    }

    /**
     * 验证管理员权限，如果没有权限则抛出异常
     */
    private void validateAdminPermission(Long userId) {
        if (!hasAdminPermission(userId)) {
            throw new RuntimeException("权限不足，需要管理员权限");
        }
    }

    /**
     * 检查当前用户是否可以操作目标用户
     */
    private boolean canOperateUser(Long currentUserId, Long targetUserId) {
        // 管理员可以操作任何用户
        if (hasAdminPermission(currentUserId)) {
            return true;
        }
        // 普通用户只能操作自己
        return currentUserId.equals(targetUserId);
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
     * 统一处理用户相关异常
     */
    private <T> Result<T> handleUserException(RuntimeException e) {
        String message = e.getMessage();

        if (message.contains("Token") || message.contains("无效") || message.contains("过期")) {
            return Result.error(ResultCode.TOKEN_INVALID);
        } else if (message.contains("用户不存在")) {
            return Result.error(ResultCode.USER_NOT_EXISTS);
        } else if (message.contains("权限") || message.contains("禁用")) {
            return Result.error(ResultCode.PERMISSION_DENIED);
        } else if (message.contains("参数")) {
            return Result.error(ResultCode.BAD_REQUEST.getCode(), message);
        } else {
            return Result.error(ResultCode.BAD_REQUEST.getCode(), message);
        }
    }
}

