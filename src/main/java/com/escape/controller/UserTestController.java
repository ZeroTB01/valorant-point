package com.escape.controller;

import com.escape.common.Result;
import com.escape.dto.response.UserInfoResponse;
import com.escape.dto.response.UserPreferencesResponse;
import com.escape.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户服务测试控制器
 * 临时用于测试UserService功能
 *
 * @author escape
 * @since 2025-06-13
 */
@Slf4j
@RestController
@RequestMapping("/test/user")
public class UserTestController {

    @Autowired
    private UserService userService;

    /**
     * 测试根据ID获取用户信息
     */
    @GetMapping("/info/{userId}")
    public Result<UserInfoResponse> getUserInfo(@PathVariable Long userId) {
        try {
            UserInfoResponse userInfo = userService.getUserInfo(userId);
            return Result.success(userInfo);
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 测试检查邮箱是否存在
     */
    @GetMapping("/check-email")
    public Result<Boolean> checkEmail(@RequestParam String email) {
        try {
            boolean exists = userService.checkEmailExists(email);
            return Result.success(exists);
        } catch (Exception e) {
            log.error("检查邮箱失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 测试检查用户名是否存在
     */
    @GetMapping("/check-username")
    public Result<Boolean> checkUsername(@RequestParam String username) {
        try {
            boolean exists = userService.checkUsernameExists(username);
            return Result.success(exists);
        } catch (Exception e) {
            log.error("检查用户名失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 测试获取用户角色
     */
    @GetMapping("/roles/{userId}")
    public Result<List<String>> getUserRoles(@PathVariable Long userId) {
        try {
            List<String> roles = userService.getUserRoles(userId);
            return Result.success(roles);
        } catch (Exception e) {
            log.error("获取用户角色失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 测试获取用户偏好
     */
    @GetMapping("/preferences/{userId}")
    public Result<UserPreferencesResponse> getUserPreferences(@PathVariable Long userId) {
        try {
            UserPreferencesResponse preferences = userService.getUserPreferences(userId);
            return Result.success(preferences);
        } catch (Exception e) {
            log.error("获取用户偏好失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 测试游客用户信息
     */
    @GetMapping("/guest")
    public Result<UserInfoResponse> getGuestInfo() {
        try {
            UserInfoResponse guestInfo = userService.getUserInfo(-1L);
            return Result.success(guestInfo);
        } catch (Exception e) {
            log.error("获取游客信息失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 测试用户统计信息
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getUserStatistics() {
        try {
            Map<String, Object> stats = userService.getUserStatistics();
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取用户统计失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 测试刷新用户缓存
     */
    @PostMapping("/refresh-cache/{userId}")
    public Result<String> refreshCache(@PathVariable Long userId) {
        try {
            userService.refreshUserCache(userId);
            return Result.success("缓存刷新成功");
        } catch (Exception e) {
            log.error("刷新缓存失败", e);
            return Result.error(e.getMessage());
        }
    }
}