package com.escape.service.impl;

import com.escape.dto.request.LoginRequest;
import com.escape.dto.request.RegisterRequest;
import com.escape.dto.response.LoginResponse;
import com.escape.dto.response.UserInfoResponse;
import com.escape.entity.*;
import com.escape.mapper.*;
import com.escape.service.AuthService;
import com.escape.service.UserService;
import com.escape.utils.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 用户认证服务实现类
 * 重构后专注于认证相关功能，用户管理功能委托给UserService
 *
 * @author escape
 * @since 2025-06-05
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private EmailVerificationCodeMapper emailVerificationCodeMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private EmailUtils emailUtils;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String VERIFICATION_CODE_PREFIX = "verification_code:";
    private static final String USER_SESSION_PREFIX = "user_session:";
    private static final String BLACKLIST_TOKEN_PREFIX = "blacklist_token:";
    private static final int VERIFICATION_CODE_EXPIRE_MINUTES = 10;

    /**
     * 发送验证码
     */
    @Override
    public boolean sendVerificationCode(String email, String type) {
        // 参数验证
        if (!StringUtils.hasText(email)) {
            throw new RuntimeException("邮箱不能为空");
        }

        if (!ValidateUtils.isValidEmail(email)) {
            throw new RuntimeException("邮箱格式不正确");
        }

        if (!StringUtils.hasText(type)) {
            throw new RuntimeException("验证码类型不能为空");
        }

        if (!"register".equals(type) && !"reset".equals(type)) {
            throw new RuntimeException("不支持的验证码类型");
        }

        // 检查邮箱是否已注册（注册类型）
        if ("register".equals(type)) {
            if (userService.checkEmailExists(email)) {
                throw new RuntimeException("该邮箱已经被注册");
            }
        }

        // 检查验证码发送频率（1分钟内只能发送一次）
        String rateLimitKey = "rate_limit:" + email;
        if (redisTemplate.hasKey(rateLimitKey)) {
            throw new RuntimeException("发送太频繁，请稍后再试");
        }

        // 生成验证码
        String code = emailUtils.generateVerificationCode();

        // 保存到数据库
        EmailVerificationCode verificationCode = new EmailVerificationCode();
        verificationCode.setEmail(email);
        verificationCode.setCode(code);
        verificationCode.setType(type);
        verificationCode.setUsed(0);
        verificationCode.setExpireTime(LocalDateTime.now().plusMinutes(VERIFICATION_CODE_EXPIRE_MINUTES));
        emailVerificationCodeMapper.insert(verificationCode);

        // 保存到Redis
        String redisKey = VERIFICATION_CODE_PREFIX + type + ":" + email;
        redisTemplate.opsForValue().set(redisKey, code, VERIFICATION_CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        // 设置发送频率限制
        redisTemplate.opsForValue().set(rateLimitKey, "1", 1, TimeUnit.MINUTES);

        // 发送邮件
        boolean emailSent;
        if ("register".equals(type)) {
            emailSent = emailUtils.sendRegistrationCode(email, code);
        } else if ("reset".equals(type)) {
            emailSent = emailUtils.sendPasswordResetCode(email, code);
        } else {
            throw new RuntimeException("不支持的验证码类型");
        }

        if (!emailSent) {
            throw new RuntimeException("邮件发送失败，请稍后重试");
        }

        log.info("验证码发送成功: email={}, type={}", email, type);
        return true;
    }

    /**
     * 用户注册
     */
    @Override
    @Transactional
    public boolean register(RegisterRequest request, String clientIp) {
        // 验证验证码
        if (!verifyCode(request.getEmail(), request.getVerificationCode(), "register")) {
            throw new RuntimeException("验证码错误或已过期");
        }

        // 检查用户名是否已存在
        if (userService.checkUsernameExists(request.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查邮箱是否已注册
        if (userService.checkEmailExists(request.getEmail())) {
            throw new RuntimeException("该邮箱已经被注册");
        }

        // 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(PasswordUtils.encode(request.getPassword()));
        user.setNickname(StringUtils.hasText(request.getNickname()) ?
                request.getNickname() : request.getUsername());
        user.setStatus(1);
        user.setEmailVerified(1);
        user.setDeleted(0);

        int result = userMapper.insert(user);
        if (result <= 0) {
            throw new RuntimeException("用户创建失败");
        }

        // 分配默认角色（普通用户）- 使用UserService
        userService.assignUserRole(user.getId(), "USER");

        // 创建用户偏好设置 - 使用UserService
        userService.createDefaultUserPreferences(user.getId());

        // 标记验证码为已使用
        markVerificationCodeUsed(request.getEmail(), request.getVerificationCode(), "register");

        // 发送欢迎邮件
        emailUtils.sendWelcomeEmail(request.getEmail(), request.getUsername());

        log.info("用户注册成功: username={}, email={}", request.getUsername(), request.getEmail());
        return true;
    }

    /**
     * 用户登录
     */
    @Override
    public LoginResponse login(LoginRequest request, String clientIp) {
        // 查找用户 - 使用UserService
        User user = userService.getUserByEmail(request.getEmail());

        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (user.getStatus() != 1) {
            throw new RuntimeException("账户已被禁用");
        }

        // 验证密码
        if (!PasswordUtils.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        // 更新最后登录信息 - 使用UserService
        userService.updateLastLoginInfo(user.getId(), clientIp);

        // 生成Token
        return generateLoginResponse(user);
    }

    /**
     * 游客登录
     */
    @Override
    public LoginResponse guestLogin(String clientIp) {
        // 创建临时游客用户信息
        User guestUser = new User();
        guestUser.setId(-1L); // 游客使用负数ID
        guestUser.setUsername("guest_" + System.currentTimeMillis());
        guestUser.setEmail("guest@temp.com");
        guestUser.setNickname("游客用户");
        guestUser.setStatus(1);

        return generateLoginResponse(guestUser);
    }

    /**
     * 刷新Token
     */
    @Override
    public LoginResponse refreshToken(String refreshToken) {
        if (!jwtUtils.validateToken(refreshToken)) {
            throw new RuntimeException("刷新Token无效或已过期");
        }

        Long userId = jwtUtils.getUserIdFromToken(refreshToken);
        if (userId == null) {
            throw new RuntimeException("无法从Token中获取用户信息");
        }

        // 游客用户的特殊处理
        if (userId == -1L) {
            User guestUser = new User();
            guestUser.setId(-1L);
            guestUser.setUsername("guest_" + System.currentTimeMillis());
            guestUser.setEmail("guest@temp.com");
            guestUser.setNickname("游客用户");
            guestUser.setStatus(1);
            return generateLoginResponse(guestUser);
        }

        User user = userMapper.selectById(userId);
        if (user == null || user.getDeleted() == 1 || user.getStatus() != 1) {
            throw new RuntimeException("用户不存在或已被禁用");
        }

        return generateLoginResponse(user);
    }

    /**
     * 发送密码重置验证码
     */
    @Override
    public boolean sendPasswordResetCode(String email) {
        // 检查用户是否存在 - 使用UserService
        if (!userService.checkEmailExists(email)) {
            throw new RuntimeException("该邮箱未注册");
        }

        return sendVerificationCode(email, "reset");
    }

    /**
     * 重置密码
     */
    @Override
    @Transactional
    public boolean resetPassword(String email, String code, String newPassword) {
        // 验证验证码
        if (!verifyCode(email, code, "reset")) {
            throw new RuntimeException("验证码错误或已过期");
        }

        // 查找用户 - 使用UserService
        User user = userService.getUserByEmail(email);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 更新密码
        user.setPassword(PasswordUtils.encode(newPassword));
        int result = userMapper.updateById(user);
        if (result <= 0) {
            throw new RuntimeException("密码更新失败");
        }

        // 标记验证码为已使用
        markVerificationCodeUsed(email, code, "reset");

        // 清除该用户的所有会话 - 使用UserService
        userService.clearUserSession(user.getId());

        log.info("密码重置成功: email={}", email);
        return true;
    }

    /**
     * 用户登出
     */
    @Override
    public void logout(String token) {
        if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
            token = token.substring(7);

            // 将Token加入黑名单
            String blacklistKey = BLACKLIST_TOKEN_PREFIX + token;
            Long expiration = jwtUtils.getExpirationDateFromToken(token).getTime();
            long ttl = expiration - System.currentTimeMillis();

            if (ttl > 0) {
                redisTemplate.opsForValue().set(blacklistKey, "1", ttl, TimeUnit.MILLISECONDS);
            }

            // 清除用户会话 - 使用UserService
            Long userId = jwtUtils.getUserIdFromToken(token);
            if (userId != null) {
                userService.clearUserSession(userId);
            }
        }
    }

    /**
     * 验证Token
     */
    @Override
    public boolean validateToken(String token) {
        if (!StringUtils.hasText(token)) {
            return false;
        }

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // 检查是否在黑名单中
        String blacklistKey = BLACKLIST_TOKEN_PREFIX + token;
        if (redisTemplate.hasKey(blacklistKey)) {
            return false;
        }

        return jwtUtils.validateToken(token);
    }

    /**
     * 获取当前用户信息
     */
    @Override
    public UserInfoResponse getUserProfile(String token) {
        if (!StringUtils.hasText(token)) {
            throw new RuntimeException("Token不能为空");
        }

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // 验证Token
        if (!validateToken(token)) {
            throw new RuntimeException("Token无效或已过期");
        }

        // 获取用户ID
        Long userId = jwtUtils.getUserIdFromToken(token);
        if (userId == null) {
            throw new RuntimeException("无法从Token中获取用户信息");
        }

        // 使用UserService获取用户信息
        return userService.getUserInfo(userId);
    }

    /**
     * 更改密码
     */
    @Override
    @Transactional
    public boolean changePassword(String token, String oldPassword, String newPassword) {
        if (!StringUtils.hasText(token)) {
            throw new RuntimeException("Token不能为空");
        }

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        if (!StringUtils.hasText(oldPassword) || !StringUtils.hasText(newPassword)) {
            throw new RuntimeException("密码不能为空");
        }

        if (newPassword.length() < 6) {
            throw new RuntimeException("新密码长度不能少于6位");
        }

        // 验证Token
        if (!validateToken(token)) {
            throw new RuntimeException("Token无效或已过期");
        }

        // 获取用户ID
        Long userId = jwtUtils.getUserIdFromToken(token);
        if (userId == null || userId == -1L) {
            throw new RuntimeException("游客用户不支持修改密码");
        }

        // 查询用户信息
        User user = userMapper.selectById(userId);
        if (user == null || user.getDeleted() == 1) {
            throw new RuntimeException("用户不存在");
        }

        if (user.getStatus() != 1) {
            throw new RuntimeException("用户已被禁用");
        }

        // 验证旧密码
        if (!PasswordUtils.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }

        // 检查新旧密码是否相同
        if (PasswordUtils.matches(newPassword, user.getPassword())) {
            throw new RuntimeException("新密码不能与原密码相同");
        }

        // 更新密码
        user.setPassword(PasswordUtils.encode(newPassword));
        int result = userMapper.updateById(user);
        if (result <= 0) {
            throw new RuntimeException("密码更新失败");
        }

        // 清除该用户的所有会话（强制重新登录）- 使用UserService
        userService.clearUserSession(user.getId());

        log.info("用户密码修改成功: userId={}, email={}", user.getId(), user.getEmail());
        return true;
    }

    // ==================== 私有方法 ====================

    /**
     * 验证验证码
     */
    private boolean verifyCode(String email, String code, String type) {
        // 从Redis验证
        String redisKey = VERIFICATION_CODE_PREFIX + type + ":" + email;
        String cachedCode = (String) redisTemplate.opsForValue().get(redisKey);

        if (cachedCode != null && cachedCode.equals(code)) {
            return true;
        }

        // 从数据库验证
        QueryWrapper<EmailVerificationCode> query = new QueryWrapper<>();
        query.eq("email", email)
                .eq("code", code)
                .eq("type", type)
                .eq("used", 0)
                .gt("expire_time", LocalDateTime.now())
                .orderByDesc("create_time")
                .last("LIMIT 1");

        EmailVerificationCode verificationCode = emailVerificationCodeMapper.selectOne(query);
        return verificationCode != null;
    }

    /**
     * 标记验证码为已使用
     */
    private void markVerificationCodeUsed(String email, String code, String type) {
        // 清除Redis缓存
        String redisKey = VERIFICATION_CODE_PREFIX + type + ":" + email;
        redisTemplate.delete(redisKey);

        // 更新数据库
        QueryWrapper<EmailVerificationCode> query = new QueryWrapper<>();
        query.eq("email", email).eq("code", code).eq("type", type).eq("used", 0);

        EmailVerificationCode verificationCode = new EmailVerificationCode();
        verificationCode.setUsed(1);

        emailVerificationCodeMapper.update(verificationCode, query);
    }

    /**
     * 生成登录响应
     */
    private LoginResponse generateLoginResponse(User user) {
        // 生成Token
        String accessToken = jwtUtils.generateAccessToken(user.getId(), user.getUsername(), user.getEmail());
        String refreshToken = jwtUtils.generateRefreshToken(user.getId());

        // 使用UserService构建用户信息
        UserInfoResponse userInfo;
        if (user.getId() == -1L) {
            // 游客用户特殊处理
            userInfo = userService.getUserInfo(-1L);
        } else {
            userInfo = userService.getUserInfo(user.getId());
        }

        // 缓存用户会话
        String sessionKey = USER_SESSION_PREFIX + user.getId();
        redisTemplate.opsForValue().set(sessionKey, userInfo, 30, TimeUnit.MINUTES);

        LoginResponse response = new LoginResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setExpiresIn(30 * 60 * 1000L); // 30分钟
        response.setUserInfo(userInfo);

        return response;
    }
}