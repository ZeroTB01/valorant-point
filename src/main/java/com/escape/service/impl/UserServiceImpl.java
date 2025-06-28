package com.escape.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.escape.common.ResultCode;
import com.escape.common.exception.BusinessException;
import com.escape.dto.response.UserInfoResponse;
import com.escape.dto.response.UserPreferencesResponse;
import com.escape.entity.*;
import com.escape.mapper.*;
import com.escape.service.UserService;
import com.escape.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 * 负责用户信息管理、偏好设置、状态管理等功能
 *
 * @author escape
 * @since 2025-06-13
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private UserPreferencesMapper userPreferencesMapper;

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private ViewHistoryMapper viewHistoryMapper;

    @Autowired
    private RedisUtils redisUtils;

    // 缓存键前缀
    private static final String USER_CACHE_PREFIX = "user:";
    private static final String USER_PREFERENCES_CACHE_PREFIX = "user:preferences:";
    private static final String USER_ROLES_CACHE_PREFIX = "user:roles:";
    private static final String USER_SESSION_PREFIX = "user:session:";
    private static final long CACHE_EXPIRE_MINUTES = 30;

    // ==================== 用户基本信息管理 ====================

    @Override
    public UserInfoResponse getUserInfo(Long userId) {
        if (userId == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "用户ID不能为空");
        }

        // 游客用户特殊处理
        if (userId == -1L) {
            return buildGuestUserInfo();
        }

        // 尝试从缓存获取
        String cacheKey = USER_CACHE_PREFIX + "info:" + userId;
        String cachedData = redisUtils.get(cacheKey);
        if (StringUtils.hasText(cachedData)) {
            log.debug("从缓存获取用户信息: {}", userId);
            // 实际项目中需要反序列化，这里暂时重新查询
        }

        // 查询用户基本信息
        User user = userMapper.selectById(userId);
        if (user == null || user.getDeleted() == 1) {
            throw new BusinessException(ResultCode.USER_NOT_EXISTS);
        }

        // 构建用户信息响应
        UserInfoResponse userInfo = buildUserInfoResponse(user);

        // 缓存用户信息
        redisUtils.set(cacheKey, userInfo.toString(), CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        return userInfo;
    }

    @Override
    public User getUserByEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return null;
        }
        return userMapper.findByEmail(email);
    }

    @Override
    public User getUserByUsername(String username) {
        if (!StringUtils.hasText(username)) {
            return null;
        }
        return userMapper.findByUsername(username);
    }

    @Override
    public boolean checkEmailExists(String email) {
        if (!StringUtils.hasText(email)) {
            return false;
        }
        User user = getUserByEmail(email);
        return user != null;
    }

    @Override
    public boolean checkUsernameExists(String username) {
        if (!StringUtils.hasText(username)) {
            return false;
        }
        User user = getUserByUsername(username);
        return user != null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUserProfile(Long userId, String nickname, String avatar, String phone) {
        if (userId == null || userId == -1L) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "无效的用户ID");
        }

        User user = userMapper.selectById(userId);
        if (user == null || user.getDeleted() == 1) {
            throw new BusinessException(ResultCode.USER_NOT_EXISTS);
        }

        // 更新用户信息
        User updateUser = new User();
        updateUser.setId(userId);
        if (StringUtils.hasText(nickname)) {
            updateUser.setNickname(nickname);
        }
        if (StringUtils.hasText(avatar)) {
            updateUser.setAvatar(avatar);
        }
        if (StringUtils.hasText(phone)) {
            updateUser.setPhone(phone);
        }

        int result = userMapper.updateById(updateUser);
        if (result > 0) {
            // 清除缓存
            refreshUserCache(userId);
            log.info("用户信息更新成功: userId={}", userId);
            return true;
        }
        return false;
    }

    @Override
    public boolean updateUserAvatar(Long userId, String avatarUrl) {
        return updateUserProfile(userId, null, avatarUrl, null);
    }

    // ==================== 用户状态管理 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUserStatus(Long userId, Integer status) {
        if (userId == null || status == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "参数不能为空");
        }

        if (status != 0 && status != 1) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "状态值无效");
        }

        User user = new User();
        user.setId(userId);
        user.setStatus(status);

        int result = userMapper.updateById(user);
        if (result > 0) {
            refreshUserCache(userId);
            log.info("用户状态更新成功: userId={}, status={}", userId, status);
            return true;
        }
        return false;
    }

    @Override
    public boolean updateLastLoginInfo(Long userId, String loginIp) {
        if (userId == null || userId == -1L) {
            return false; // 游客用户不记录登录信息
        }

        int result = userMapper.updateLastLoginInfo(userId, LocalDateTime.now(), loginIp);
        if (result > 0) {
            refreshUserCache(userId);
            return true;
        }
        return false;
    }

    @Override
    public boolean verifyUserEmail(Long userId) {
        if (userId == null || userId == -1L) {
            return false;
        }

        int result = userMapper.verifyEmail(userId);
        if (result > 0) {
            refreshUserCache(userId);
            log.info("用户邮箱验证成功: userId={}", userId);
            return true;
        }
        return false;
    }

    // ==================== 用户偏好设置管理 ====================

    @Override
    public UserPreferencesResponse getUserPreferences(Long userId) {
        if (userId == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "用户ID不能为空");
        }

        // 游客用户返回默认偏好
        if (userId == -1L) {
            return buildDefaultPreferences();
        }

        // 尝试从缓存获取
        String cacheKey = USER_PREFERENCES_CACHE_PREFIX + userId;
        String cachedData = redisUtils.get(cacheKey);
        if (StringUtils.hasText(cachedData)) {
            log.debug("从缓存获取用户偏好: {}", userId);
        }

        // 查询用户偏好
        UserPreferences preferences = userPreferencesMapper.findByUserId(userId);
        UserPreferencesResponse response = new UserPreferencesResponse();

        if (preferences != null) {
            BeanUtils.copyProperties(preferences, response);
            response.setNotificationEmail(preferences.getNotificationEmail() == 1);
            response.setAutoPlayVideo(preferences.getAutoPlayVideo() == 1);
        } else {
            // 返回默认偏好
            response = buildDefaultPreferences();
        }

        // 缓存偏好设置
        redisUtils.set(cacheKey, response.toString(), CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUserPreferences(Long userId, UserPreferencesResponse preferences) {
        if (userId == null || userId == -1L || preferences == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "参数不能为空");
        }

        // 查询是否已存在偏好设置
        UserPreferences existingPreferences = userPreferencesMapper.findByUserId(userId);

        if (existingPreferences != null) {
            // 更新现有偏好
            UserPreferences updatePreferences = new UserPreferences();
            updatePreferences.setId(existingPreferences.getId());
            updatePreferences.setThemeMode(preferences.getThemeMode());
            updatePreferences.setVideoQuality(preferences.getVideoQuality());
            updatePreferences.setLanguage(preferences.getLanguage());
            updatePreferences.setNotificationEmail(Boolean.TRUE.equals(preferences.getNotificationEmail()) ? 1 : 0);
            updatePreferences.setAutoPlayVideo(Boolean.TRUE.equals(preferences.getAutoPlayVideo()) ? 1 : 0);

            int result = userPreferencesMapper.updateById(updatePreferences);
            if (result > 0) {
                // 清除缓存
                redisUtils.delete(USER_PREFERENCES_CACHE_PREFIX + userId);
                log.info("用户偏好更新成功: userId={}", userId);
                return true;
            }
        } else {
            // 创建新的偏好设置
            UserPreferences newPreferences = new UserPreferences();
            newPreferences.setUserId(userId);
            newPreferences.setThemeMode(preferences.getThemeMode());
            newPreferences.setVideoQuality(preferences.getVideoQuality());
            newPreferences.setLanguage(preferences.getLanguage());
            newPreferences.setNotificationEmail(Boolean.TRUE.equals(preferences.getNotificationEmail()) ? 1 : 0);
            newPreferences.setAutoPlayVideo(Boolean.TRUE.equals(preferences.getAutoPlayVideo()) ? 1 : 0);

            int result = userPreferencesMapper.insert(newPreferences);
            if (result > 0) {
                log.info("用户偏好创建成功: userId={}", userId);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean createDefaultUserPreferences(Long userId) {
        if (userId == null || userId == -1L) {
            return false;
        }

        UserPreferences preferences = new UserPreferences();
        preferences.setUserId(userId);
        preferences.setThemeMode("dark");
        preferences.setVideoQuality("720p");
        preferences.setLanguage("zh-CN");
        preferences.setNotificationEmail(1);
        preferences.setAutoPlayVideo(1);

        int result = userPreferencesMapper.insert(preferences);
        if (result > 0) {
            log.info("默认用户偏好创建成功: userId={}", userId);
            return true;
        }
        return false;
    }

    // ==================== 用户角色管理 ====================

    @Override
    public List<String> getUserRoles(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }

        // 游客用户返回游客角色
        if (userId == -1L) {
            return List.of("GUEST");
        }

        // 尝试从缓存获取
        String cacheKey = USER_ROLES_CACHE_PREFIX + userId;
        String cachedData = redisUtils.get(cacheKey);
        if (StringUtils.hasText(cachedData)) {
            log.debug("从缓存获取用户角色: {}", userId);
        }

        List<String> roles = userMapper.findRolesByUserId(userId);
        if (roles.isEmpty()) {
            roles = List.of("USER"); // 默认角色
        }

        // 缓存角色信息
        redisUtils.set(cacheKey, roles.toString(), CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        return roles;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignUserRole(Long userId, String roleKey) {
        if (userId == null || !StringUtils.hasText(roleKey)) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "参数不能为空");
        }

        // 查询角色是否存在
        Role role = roleMapper.findByRoleKey(roleKey);
        if (role == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXISTS.getCode(), "角色不存在");
        }

        // 检查用户是否已有该角色
        QueryWrapper<UserRole> query = new QueryWrapper<>();
        query.eq("user_id", userId).eq("role_id", role.getId());
        UserRole existingUserRole = userRoleMapper.selectOne(query);
        if (existingUserRole != null) {
            return true; // 已存在，返回成功
        }

        // 创建用户角色关联
        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(role.getId());

        int result = userRoleMapper.insert(userRole);
        if (result > 0) {
            // 清除角色缓存
            redisUtils.delete(USER_ROLES_CACHE_PREFIX + userId);
            log.info("用户角色分配成功: userId={}, roleKey={}", userId, roleKey);
            return true;
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeUserRole(Long userId, String roleKey) {
        if (userId == null || !StringUtils.hasText(roleKey)) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "参数不能为空");
        }

        // 查询角色
        Role role = roleMapper.findByRoleKey(roleKey);
        if (role == null) {
            return true; // 角色不存在，返回成功
        }

        // 删除用户角色关联
        QueryWrapper<UserRole> query = new QueryWrapper<>();
        query.eq("user_id", userId).eq("role_id", role.getId());

        int result = userRoleMapper.delete(query);
        if (result > 0) {
            // 清除角色缓存
            redisUtils.delete(USER_ROLES_CACHE_PREFIX + userId);
            log.info("用户角色移除成功: userId={}, roleKey={}", userId, roleKey);
        }
        return true;
    }

    // ==================== 用户查询和统计 ====================

    @Override
    public IPage<User> getUserPage(Page<User> page, String keyword, Integer status) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("deleted", 0);

        if (status != null) {
            wrapper.eq("status", status);
        }

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like("username", keyword)
                    .or().like("email", keyword)
                    .or().like("nickname", keyword));
        }

        wrapper.orderByDesc("create_time");

        return userMapper.selectPage(page, wrapper);
    }

    @Override
    public Map<String, Object> getUserStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // 总用户数
        QueryWrapper<User> totalQuery = new QueryWrapper<>();
        totalQuery.eq("deleted", 0);
        long totalUsers = userMapper.selectCount(totalQuery);
        stats.put("totalUsers", totalUsers);

        // 活跃用户数（状态为1）
        QueryWrapper<User> activeQuery = new QueryWrapper<>();
        activeQuery.eq("deleted", 0).eq("status", 1);
        long activeUsers = userMapper.selectCount(activeQuery);
        stats.put("activeUsers", activeUsers);

        // 今日注册用户数
        QueryWrapper<User> todayQuery = new QueryWrapper<>();
        todayQuery.eq("deleted", 0)
                .ge("create_time", LocalDateTime.now().toLocalDate());
        long todayUsers = userMapper.selectCount(todayQuery);
        stats.put("todayUsers", todayUsers);

        // 邮箱验证用户数
        QueryWrapper<User> verifiedQuery = new QueryWrapper<>();
        verifiedQuery.eq("deleted", 0).eq("email_verified", 1);
        long verifiedUsers = userMapper.selectCount(verifiedQuery);
        stats.put("verifiedUsers", verifiedUsers);

        return stats;
    }

    @Override
    public Map<String, Object> getUserActivitySummary(Long userId) {
        if (userId == null || userId == -1L) {
            return Collections.emptyMap();
        }

        Map<String, Object> summary = new HashMap<>();

        // 收藏数统计
        List<Map<String, Object>> favoriteStats = favoriteMapper.countByUserAndType(userId);
        summary.put("favoriteStats", favoriteStats);

        // 浏览历史统计
        List<Map<String, Object>> viewStats = viewHistoryMapper.statisticsByUser(userId);
        summary.put("viewStats", viewStats);

        // 最近收藏
        List<Favorite> recentFavorites = favoriteMapper.findRecentFavorites(userId, 5);
        summary.put("recentFavorites", recentFavorites);

        return summary;
    }

    // ==================== 用户数据导出和批量操作 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateUserStatus(List<Long> userIds, Integer status) {
        if (userIds == null || userIds.isEmpty() || status == null) {
            return 0;
        }

        if (status != 0 && status != 1) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "状态值无效");
        }

        int successCount = 0;
        for (Long userId : userIds) {
            try {
                if (updateUserStatus(userId, status)) {
                    successCount++;
                }
            } catch (Exception e) {
                log.error("批量更新用户状态失败: userId={}", userId, e);
            }
        }

        return successCount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUser(Long userId) {
        if (userId == null || userId == -1L) {
            return false;
        }

        User user = new User();
        user.setId(userId);
        user.setDeleted(1);

        int result = userMapper.updateById(user);
        if (result > 0) {
            refreshUserCache(userId);
            clearUserSession(userId);
            log.info("用户删除成功: userId={}", userId);
            return true;
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDeleteUsers(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return 0;
        }

        int successCount = 0;
        for (Long userId : userIds) {
            try {
                if (deleteUser(userId)) {
                    successCount++;
                }
            } catch (Exception e) {
                log.error("批量删除用户失败: userId={}", userId, e);
            }
        }

        return successCount;
    }

    // ==================== 缓存管理 ====================

    @Override
    public void refreshUserCache(Long userId) {
        if (userId == null) {
            return;
        }

        log.info("刷新用户缓存: userId={}", userId);

        // 删除用户相关的所有缓存
        redisUtils.delete(USER_CACHE_PREFIX + "info:" + userId);
        redisUtils.delete(USER_PREFERENCES_CACHE_PREFIX + userId);
        redisUtils.delete(USER_ROLES_CACHE_PREFIX + userId);
    }

    @Override
    public void clearUserSession(Long userId) {
        if (userId == null) {
            return;
        }

        String sessionKey = USER_SESSION_PREFIX + userId;
        redisUtils.delete(sessionKey);
        log.info("用户会话清除成功: userId={}", userId);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 构建用户信息响应
     */
    private UserInfoResponse buildUserInfoResponse(User user) {
        UserInfoResponse userInfo = new UserInfoResponse();

        // 手动设置字段，时间字段转换为字符串
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setEmail(user.getEmail());
        userInfo.setNickname(user.getNickname());
        userInfo.setAvatar(user.getAvatar());
        userInfo.setStatus(user.getStatus());
        userInfo.setEmailVerified(user.getEmailVerified());

        // 时间字段转换
        if (user.getLastLoginTime() != null) {
            userInfo.setLastLoginTime(user.getLastLoginTime().format(
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        if (user.getCreateTime() != null) {
            userInfo.setCreateTime(user.getCreateTime().format(
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }

        // 获取用户角色
        List<String> roles = getUserRoles(user.getId());
        userInfo.setRoles(roles);

        // 获取用户偏好
        UserPreferencesResponse preferences = getUserPreferences(user.getId());
        userInfo.setPreferences(preferences);

        return userInfo;
    }

    /**
     * 构建游客用户信息
     */
    private UserInfoResponse buildGuestUserInfo() {
        UserInfoResponse guestInfo = new UserInfoResponse();
        guestInfo.setId(-1L);
        guestInfo.setUsername("guest_" + System.currentTimeMillis());
        guestInfo.setEmail("guest@temp.com");
        guestInfo.setNickname("游客用户");
        guestInfo.setStatus(1);
        guestInfo.setRoles(List.of("GUEST"));
        guestInfo.setPreferences(buildDefaultPreferences());

        // 设置时间字符串
        String currentTime = java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        guestInfo.setCreateTime(currentTime);
        guestInfo.setLastLoginTime(currentTime);

        return guestInfo;
    }

    /**
     * 构建默认偏好设置
     */
    private UserPreferencesResponse buildDefaultPreferences() {
        UserPreferencesResponse preferences = new UserPreferencesResponse();
        preferences.setThemeMode("dark");
        preferences.setVideoQuality("720p");
        preferences.setLanguage("zh-CN");
        preferences.setNotificationEmail(true);
        preferences.setAutoPlayVideo(true);
        return preferences;
    }
}