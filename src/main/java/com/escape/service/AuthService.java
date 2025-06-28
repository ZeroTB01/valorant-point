package com.escape.service;

import com.escape.dto.request.LoginRequest;
import com.escape.dto.request.RegisterRequest;
import com.escape.dto.response.LoginResponse;
import com.escape.dto.response.UserInfoResponse;

/**
 * 认证服务接口
 * 定义用户认证相关的业务方法
 *
 * @author escape
 * @since 2025-06-05
 */
public interface AuthService {

    /**
     * 发送验证码
     * @param email 邮箱
     * @param type 类型（register-注册, reset-重置密码）
     * @return 是否发送成功
     */
    boolean sendVerificationCode(String email, String type);

    /**
     * 用户注册
     * @param request 注册请求
     * @param clientIp 客户端IP
     * @return 是否注册成功
     */
    boolean register(RegisterRequest request, String clientIp);

    /**
     * 用户登录
     * @param request 登录请求
     * @param clientIp 客户端IP
     * @return 登录响应信息
     */
    LoginResponse login(LoginRequest request, String clientIp);

    /**
     * 游客登录
     * @param clientIp 客户端IP
     * @return 登录响应信息
     */
    LoginResponse guestLogin(String clientIp);

    /**
     * 刷新Token
     * @param refreshToken 刷新Token
     * @return 新的登录响应信息
     */
    LoginResponse refreshToken(String refreshToken);

    /**
     * 发送密码重置验证码
     * @param email 邮箱
     * @return 是否发送成功
     */
    boolean sendPasswordResetCode(String email);

    /**
     * 重置密码
     * @param email 邮箱
     * @param code 验证码
     * @param newPassword 新密码
     * @return 是否重置成功
     */
    boolean resetPassword(String email, String code, String newPassword);

    /**
     * 用户登出
     * @param token 访问Token
     */
    void logout(String token);

    /**
     * 验证Token有效性
     * @param token 访问Token
     * @return 是否有效
     */
    boolean validateToken(String token);

    /**
     * 获取用户信息
     * @param token 访问Token
     * @return 用户信息
     */
    UserInfoResponse getUserProfile(String token);

    /**
     * 修改密码
     * @param token 访问Token
     * @param oldPassword 原密码
     * @param newPassword 新密码
     * @return 是否修改成功
     */
    boolean changePassword(String token, String oldPassword, String newPassword);
}