package com.escape.controller;

import com.escape.common.Result;
import com.escape.common.ResultCode;
import com.escape.dto.request.LoginRequest;
import com.escape.dto.request.RegisterRequest;
import com.escape.dto.response.LoginResponse;
import com.escape.dto.response.UserInfoResponse;
import com.escape.dto.response.UserPreferencesResponse;
import com.escape.service.AuthService;
import com.escape.service.impl.AuthServiceImpl;
import com.escape.utils.IpUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 用户认证控制器
 * 处理登录、注册、邮箱验证等认证相关操作
 *
 * @author escape
 * @since 2025-06-02
 */
@Slf4j
@RestController
@RequestMapping("/auth")
//@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;


    @GetMapping("/test-date")
    public UserInfoResponse testDate() {
        UserInfoResponse response = new UserInfoResponse();
        response.setId(1L);
        response.setUsername("escape");
        response.setEmail("898381075@qq.com");
        response.setNickname("测试用户");
        response.setStatus(1);
        response.setEmailVerified(1);

        // 时间字段转换为字符串
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String currentTime = LocalDateTime.now().format(formatter);
        response.setLastLoginTime(currentTime);
        response.setCreateTime(currentTime);

        // 设置角色和偏好（可选）
        response.setRoles(List.of("USER"));

        UserPreferencesResponse preferences = new UserPreferencesResponse();
        preferences.setThemeMode("dark");
        preferences.setVideoQuality("720p");
        preferences.setLanguage("zh-CN");
        preferences.setNotificationEmail(true);
        preferences.setAutoPlayVideo(true);
        response.setPreferences(preferences);

        return response;
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Result<String> healthCheck() {
        return Result.success("认证服务运行正常");
    }

    /**
     * 发送邮箱验证码
     */
    @PostMapping("/send-code")
    public Result<String> sendVerificationCode(@RequestParam String email,
                                               @RequestParam String type) {
        try {
            log.info("发送验证码请求，邮箱: {}, 类型: {}", email, type);
            boolean success = authService.sendVerificationCode(email, type);
            if (success) {
                return Result.success("验证码发送成功");
            } else {
                return Result.error(ResultCode.VERIFICATION_CODE_SEND_FAILED);
            }
        } catch (RuntimeException e) {
            log.warn("发送验证码失败，邮箱: {}, 类型: {}, 原因: {}", email, type, e.getMessage());
            if (e.getMessage().contains("邮箱已经被注册")) {
                return Result.error(ResultCode.USER_ALREADY_EXISTS.getCode(), e.getMessage());
            } else if (e.getMessage().contains("发送太频繁")) {
                return Result.error(ResultCode.BAD_REQUEST.getCode(), e.getMessage());
            } else if (e.getMessage().contains("邮箱格式")) {
                return Result.error(ResultCode.BAD_REQUEST.getCode(), e.getMessage());
            } else if (e.getMessage().contains("不支持的验证码类型")) {
                return Result.error(ResultCode.BAD_REQUEST.getCode(), e.getMessage());
            } else {
                return Result.error(ResultCode.VERIFICATION_CODE_SEND_FAILED.getCode(), e.getMessage());
            }
        } catch (Exception e) {
            log.error("发送验证码系统错误，邮箱: {}, 类型: {}", email, type, e);
            return Result.error(ResultCode.VERIFICATION_CODE_SEND_FAILED);
        }
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<String> register(@Valid @RequestBody RegisterRequest request,
                                   HttpServletRequest httpRequest) {
        try {
            String clientIp = IpUtils.getClientIp(httpRequest);
            log.info("用户注册请求，邮箱: {}, IP: {}", request.getEmail(), clientIp);

            boolean success = authService.register(request, clientIp);
            if (success) {
                return Result.success("注册成功");
            } else {
                return Result.error(ResultCode.USER_ALREADY_EXISTS);
            }
        } catch (RuntimeException e) {
            log.warn("用户注册失败，邮箱: {}, 原因: {}", request.getEmail(), e.getMessage());
            if (e.getMessage().contains("验证码")) {
                return Result.error(ResultCode.VERIFICATION_CODE_INVALID);
            } else if (e.getMessage().contains("用户名已存在")) {
                return Result.error(ResultCode.USER_ALREADY_EXISTS.getCode(), "用户名已存在");
            } else if (e.getMessage().contains("邮箱已经被注册")) {
                return Result.error(ResultCode.USER_ALREADY_EXISTS);
            } else {
                return Result.error(ResultCode.BAD_REQUEST.getCode(), e.getMessage());
            }
        } catch (Exception e) {
            log.error("用户注册失败，邮箱: {}", request.getEmail(), e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), e.getMessage());
        }
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                       HttpServletRequest httpRequest) {
        try {
            String clientIp = IpUtils.getClientIp(httpRequest);
            log.info("用户登录请求，邮箱: {}, IP: {}", request.getEmail(), clientIp);

            LoginResponse response = authService.login(request, clientIp);
            return Result.success("登录成功", response);
        } catch (RuntimeException e) {
            log.warn("用户登录失败，邮箱: {}, 原因: {}", request.getEmail(), e.getMessage());
            if (e.getMessage().contains("用户不存在")) {
                return Result.error(ResultCode.USER_NOT_EXISTS);
            } else if (e.getMessage().contains("密码错误")) {
                return Result.error(ResultCode.INVALID_PASSWORD);
            } else if (e.getMessage().contains("账户已被禁用")) {
                return Result.error(ResultCode.USER_DISABLED);
            } else if (e.getMessage().contains("邮箱未验证")) {
                return Result.error(ResultCode.EMAIL_NOT_VERIFIED);
            } else {
                return Result.error(ResultCode.BAD_REQUEST.getCode(), e.getMessage());
            }
        } catch (Exception e) {
            log.error("用户登录系统错误，邮箱: {}", request.getEmail(), e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 游客登录
     */
    @PostMapping("/guest-login")
    public Result<LoginResponse> guestLogin(HttpServletRequest httpRequest) {
        try {
            String clientIp = IpUtils.getClientIp(httpRequest);
            log.info("游客登录请求，IP: {}", clientIp);

            LoginResponse response = authService.guestLogin(clientIp);
            return Result.success("游客登录成功", response);
        } catch (Exception e) {
            log.error("游客登录失败，IP: {}", IpUtils.getClientIp(httpRequest), e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 刷新Token
     */
    @PostMapping("/refresh")
    public Result<LoginResponse> refreshToken(@RequestParam String refreshToken) {
        try {
            log.info("Token刷新请求");
            LoginResponse response = authService.refreshToken(refreshToken);
            return Result.success("Token刷新成功", response);
        } catch (RuntimeException e) {
            log.warn("Token刷新失败: {}", e.getMessage());
            if (e.getMessage().contains("过期")) {
                return Result.error(ResultCode.TOKEN_EXPIRED);
            } else if (e.getMessage().contains("无效") || e.getMessage().contains("Token")) {
                return Result.error(ResultCode.TOKEN_INVALID);
            } else {
                return Result.error(ResultCode.TOKEN_INVALID);
            }
        } catch (Exception e) {
            log.error("Token刷新系统错误", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 发送密码重置验证码
     */
    @PostMapping("/forgot-password")
    public Result<String> forgotPassword(@RequestParam String email) {
        try {
            log.info("发送密码重置验证码，邮箱: {}", email);
            boolean success = authService.sendPasswordResetCode(email);
            if (success) {
                return Result.success("密码重置验证码已发送到您的邮箱");
            } else {
                return Result.error(ResultCode.EMAIL_SEND_FAILED);
            }
        } catch (RuntimeException e) {
            log.warn("发送密码重置验证码失败，邮箱: {}, 原因: {}", email, e.getMessage());
            if (e.getMessage().contains("未注册")) {
                return Result.error(ResultCode.USER_NOT_EXISTS);
            } else if (e.getMessage().contains("发送太频繁")) {
                return Result.error(ResultCode.BAD_REQUEST.getCode(), e.getMessage());
            } else {
                return Result.error(ResultCode.EMAIL_SEND_FAILED.getCode(), e.getMessage());
            }
        } catch (Exception e) {
            log.error("发送密码重置验证码系统错误，邮箱: {}", email, e);
            return Result.error(ResultCode.EMAIL_SEND_FAILED);
        }
    }

    /**
     * 重置密码
     */
    @PostMapping("/reset-password")
    public Result<String> resetPassword(@RequestParam String email,
                                        @RequestParam String code,
                                        @RequestParam String newPassword) {
        try {
            log.info("重置密码请求，邮箱: {}", email);
            boolean success = authService.resetPassword(email, code, newPassword);
            if (success) {
                return Result.success("密码重置成功");
            } else {
                return Result.error(ResultCode.VERIFICATION_CODE_INVALID);
            }
        } catch (RuntimeException e) {
            log.warn("密码重置失败，邮箱: {}, 原因: {}", email, e.getMessage());
            if (e.getMessage().contains("用户不存在")) {
                return Result.error(ResultCode.USER_NOT_EXISTS);
            } else if (e.getMessage().contains("验证码错误或已过期")) {
                return Result.error(ResultCode.VERIFICATION_CODE_INVALID);
            } else if (e.getMessage().contains("验证码")) {
                return Result.error(ResultCode.VERIFICATION_CODE_INVALID);
            } else {
                return Result.error(ResultCode.BAD_REQUEST.getCode(), e.getMessage());
            }
        } catch (Exception e) {
            log.error("密码重置系统错误，邮箱: {}", email, e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public Result<String> logout(@RequestHeader("Authorization") String token) {
        try {
            log.info("用户登出请求");
            authService.logout(token);
            return Result.success("登出成功");
        } catch (RuntimeException e) {
            log.warn("用户登出失败: {}", e.getMessage());
            return Result.error(ResultCode.TOKEN_INVALID);
        } catch (Exception e) {
            log.error("用户登出系统错误", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 验证Token有效性
     */
    @GetMapping("/validate")
    public Result<String> validateToken(@RequestHeader("Authorization") String token) {
        try {
            boolean isValid = authService.validateToken(token);
            if (isValid) {
                return Result.success("Token有效");
            } else {
                return Result.error(ResultCode.TOKEN_INVALID);
            }
        } catch (Exception e) {
            log.error("Token验证系统错误", e);
            return Result.error(ResultCode.TOKEN_INVALID);
        }
    }



    /**
     * 检查邮箱是否已注册
     */
    @GetMapping("/check-email")
    public Result<Boolean> checkEmailExists(@RequestParam String email) {
        try {
            // 这个方法需要在AuthService中实现
            // boolean exists = authService.checkEmailExists(email);
            // return Result.success(exists);

            // 临时实现，建议在AuthService中添加此方法
            log.info("检查邮箱是否存在: {}", email);
            return Result.success("邮箱检查功能需要在AuthService中实现", false);
        } catch (Exception e) {
            log.error("检查邮箱存在性失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/profile")
    public Result<UserInfoResponse> getUserProfile(@RequestHeader("Authorization") String token) {
        try {
            UserInfoResponse userProfile = authService.getUserProfile(token);
            return Result.success(userProfile);
        } catch (RuntimeException e) {
            log.warn("获取用户信息失败: {}", e.getMessage());
            if (e.getMessage().contains("Token") || e.getMessage().contains("无效") || e.getMessage().contains("过期")) {
                return Result.error(ResultCode.TOKEN_INVALID);
            } else if (e.getMessage().contains("用户不存在")) {
                return Result.error(ResultCode.USER_NOT_EXISTS);
            } else if (e.getMessage().contains("禁用")) {
                return Result.error(ResultCode.USER_DISABLED);
            } else {
                return Result.error(ResultCode.BAD_REQUEST.getCode(), e.getMessage());
            }
        } catch (Exception e) {
            log.error("获取用户信息系统错误", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 更改密码
     */
    @PutMapping("/change-password")
    public Result<String> changePassword(@RequestHeader("Authorization") String token,
                                         @RequestParam String oldPassword,
                                         @RequestParam String newPassword) {
        try {
            log.info("更改密码请求");
            boolean success = authService.changePassword(token, oldPassword, newPassword);
            if (success) {
                return Result.success("密码修改成功，请重新登录");
            } else {
                return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
            }
        } catch (RuntimeException e) {
            log.warn("更改密码失败: {}", e.getMessage());
            if (e.getMessage().contains("Token") || e.getMessage().contains("无效") || e.getMessage().contains("过期")) {
                return Result.error(ResultCode.TOKEN_INVALID);
            } else if (e.getMessage().contains("原密码错误")) {
                return Result.error(ResultCode.INVALID_PASSWORD);
            } else if (e.getMessage().contains("用户不存在")) {
                return Result.error(ResultCode.USER_NOT_EXISTS);
            } else if (e.getMessage().contains("禁用")) {
                return Result.error(ResultCode.USER_DISABLED);
            } else if (e.getMessage().contains("游客")) {
                return Result.error(ResultCode.PERMISSION_DENIED.getCode(), e.getMessage());
            } else {
                return Result.error(ResultCode.BAD_REQUEST.getCode(), e.getMessage());
            }
        } catch (Exception e) {
            log.error("更改密码系统错误", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }
}