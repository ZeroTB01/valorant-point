package com.escape.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.escape.entity.User;
import com.escape.entity.UserPreferences;
import com.escape.dto.response.UserInfoResponse;
import com.escape.dto.response.UserPreferencesResponse;

import java.util.List;
import java.util.Map;

/**
 * 用户服务接口
 * 负责用户信息管理、偏好设置、状态管理等功能
 *
 * @author escape
 * @since 2025-06-13
 */
public interface UserService extends IService<User> {

    // ==================== 用户基本信息管理 ====================

    /**
     * 根据用户ID获取用户信息
     * @param userId 用户ID
     * @return 用户信息响应DTO
     */
    UserInfoResponse getUserInfo(Long userId);

    /**
     * 根据邮箱获取用户
     * @param email 邮箱
     * @return 用户实体
     */
    User getUserByEmail(String email);

    /**
     * 根据用户名获取用户
     * @param username 用户名
     * @return 用户实体
     */
    User getUserByUsername(String username);

    /**
     * 检查邮箱是否已存在
     * @param email 邮箱
     * @return 是否存在
     */
    boolean checkEmailExists(String email);

    /**
     * 检查用户名是否已存在
     * @param username 用户名
     * @return 是否存在
     */
    boolean checkUsernameExists(String username);

    /**
     * 更新用户基本信息
     * @param userId 用户ID
     * @param nickname 昵称
     * @param avatar 头像URL
     * @param phone 手机号
     * @return 是否成功
     */
    boolean updateUserProfile(Long userId, String nickname, String avatar, String phone);

    /**
     * 更新用户头像
     * @param userId 用户ID
     * @param avatarUrl 头像URL
     * @return 是否成功
     */
    boolean updateUserAvatar(Long userId, String avatarUrl);

    // ==================== 用户状态管理 ====================

    /**
     * 更新用户状态
     * @param userId 用户ID
     * @param status 状态（0-禁用，1-正常）
     * @return 是否成功
     */
    boolean updateUserStatus(Long userId, Integer status);

    /**
     * 更新用户最后登录信息
     * @param userId 用户ID
     * @param loginIp 登录IP
     * @return 是否成功
     */
    boolean updateLastLoginInfo(Long userId, String loginIp);

    /**
     * 验证用户邮箱
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean verifyUserEmail(Long userId);

    // ==================== 用户偏好设置管理 ====================

    /**
     * 获取用户偏好设置
     * @param userId 用户ID
     * @return 用户偏好响应DTO
     */
    UserPreferencesResponse getUserPreferences(Long userId);

    /**
     * 更新用户偏好设置
     * @param userId 用户ID
     * @param preferences 偏好设置
     * @return 是否成功
     */
    boolean updateUserPreferences(Long userId, UserPreferencesResponse preferences);

    /**
     * 创建默认用户偏好设置
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean createDefaultUserPreferences(Long userId);

    // ==================== 用户角色管理 ====================

    /**
     * 获取用户角色列表
     * @param userId 用户ID
     * @return 角色标识列表
     */
    List<String> getUserRoles(Long userId);

    /**
     * 为用户分配角色
     * @param userId 用户ID
     * @param roleKey 角色标识
     * @return 是否成功
     */
    boolean assignUserRole(Long userId, String roleKey);

    /**
     * 移除用户角色
     * @param userId 用户ID
     * @param roleKey 角色标识
     * @return 是否成功
     */
    boolean removeUserRole(Long userId, String roleKey);

    // ==================== 用户查询和统计 ====================

    /**
     * 分页查询用户列表
     * @param page 分页参数
     * @param keyword 搜索关键词（用户名、邮箱、昵称）
     * @param status 用户状态（可选）
     * @return 分页结果
     */
    IPage<User> getUserPage(Page<User> page, String keyword, Integer status);

    /**
     * 获取用户统计信息
     * @return 统计数据（总数、活跃数、今日注册数等）
     */
    Map<String, Object> getUserStatistics();

    /**
     * 获取用户的活动摘要
     * @param userId 用户ID
     * @return 活动摘要（收藏数、浏览数、学习进度等）
     */
    Map<String, Object> getUserActivitySummary(Long userId);

    // ==================== 用户数据导出和批量操作 ====================

    /**
     * 批量更新用户状态
     * @param userIds 用户ID列表
     * @param status 新状态
     * @return 更新成功的数量
     */
    int batchUpdateUserStatus(List<Long> userIds, Integer status);

    /**
     * 删除用户（软删除）
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean deleteUser(Long userId);

    /**
     * 批量删除用户（软删除）
     * @param userIds 用户ID列表
     * @return 删除成功的数量
     */
    int batchDeleteUsers(List<Long> userIds);

    // ==================== 缓存管理 ====================

    /**
     * 刷新用户缓存
     * @param userId 用户ID
     */
    void refreshUserCache(Long userId);

    /**
     * 清除用户会话缓存
     * @param userId 用户ID
     */
    void clearUserSession(Long userId);
}