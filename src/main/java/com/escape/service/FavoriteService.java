package com.escape.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.escape.entity.Favorite;

import java.util.List;
import java.util.Map;

/**
 * 收藏服务接口
 *
 * @author escape
 * @since 2025-06-14
 */
public interface FavoriteService extends IService<Favorite> {

    /**
     * 添加收藏
     * @param userId 用户ID
     * @param targetType 目标类型：content/position/hero/map/weapon
     * @param targetId 目标ID
     * @param folderName 收藏夹名称（可选）
     * @return 是否成功
     */
    boolean addFavorite(Long userId, String targetType, Long targetId, String folderName);

    /**
     * 取消收藏
     * @param userId 用户ID
     * @param targetType 目标类型
     * @param targetId 目标ID
     * @return 是否成功
     */
    boolean removeFavorite(Long userId, String targetType, Long targetId);

    /**
     * 检查是否已收藏
     * @param userId 用户ID
     * @param targetType 目标类型
     * @param targetId 目标ID
     * @return 是否已收藏
     */
    boolean isFavorited(Long userId, String targetType, Long targetId);

    /**
     * 获取用户的收藏列表（按类型）
     * @param userId 用户ID
     * @param targetType 目标类型
     * @return 收藏列表
     */
    List<Favorite> getUserFavorites(Long userId, String targetType);

    /**
     * 获取用户的收藏夹列表
     * @param userId 用户ID
     * @return 收藏夹名称列表
     */
    List<String> getUserFolders(Long userId);

    /**
     * 获取指定收藏夹的内容
     * @param userId 用户ID
     * @param folderName 收藏夹名称
     * @return 收藏列表
     */
    List<Favorite> getFavoritesByFolder(Long userId, String folderName);

    /**
     * 获取用户各类型收藏数量统计
     * @param userId 用户ID
     * @return 统计结果
     */
    List<Map<String, Object>> getUserFavoriteStatistics(Long userId);

    /**
     * 获取用户各收藏夹的数量统计
     * @param userId 用户ID
     * @return 统计结果
     */
    List<Map<String, Object>> getUserFolderStatistics(Long userId);

    /**
     * 移动收藏到其他收藏夹
     * @param userId 用户ID
     * @param favoriteId 收藏ID
     * @param newFolder 新收藏夹名称
     * @return 是否成功
     */
    boolean moveFavorite(Long userId, Long favoriteId, String newFolder);

    /**
     * 批量删除收藏
     * @param userId 用户ID
     * @param favoriteIds 收藏ID列表
     * @return 删除数量
     */
    int batchRemoveFavorites(Long userId, List<Long> favoriteIds);

    /**
     * 获取收藏的详细信息（分页）
     * @param page 分页参数
     * @param userId 用户ID
     * @param targetType 目标类型（可选）
     * @param folderName 收藏夹名称（可选）
     * @return 分页结果
     */
    IPage<Map<String, Object>> getFavoriteDetails(Page<Favorite> page, Long userId, String targetType, String folderName);

    /**
     * 获取内容被收藏次数
     * @param targetType 目标类型
     * @param targetId 目标ID
     * @return 收藏次数
     */
    int getFavoriteCount(String targetType, Long targetId);

    /**
     * 获取热门收藏内容
     * @param targetType 目标类型
     * @param limit 数量限制
     * @return 热门内容列表
     */
    List<Map<String, Object>> getHotFavorites(String targetType, Integer limit);

    /**
     * 创建收藏夹
     * @param userId 用户ID
     * @param folderName 收藏夹名称
     * @return 是否成功
     */
    boolean createFolder(Long userId, String folderName);

    /**
     * 重命名收藏夹
     * @param userId 用户ID
     * @param oldName 原名称
     * @param newName 新名称
     * @return 是否成功
     */
    boolean renameFolder(Long userId, String oldName, String newName);

    /**
     * 删除收藏夹（收藏移到默认收藏夹）
     * @param userId 用户ID
     * @param folderName 收藏夹名称
     * @return 是否成功
     */
    boolean deleteFolder(Long userId, String folderName);

    /**
     * 获取用户最近的收藏
     * @param userId 用户ID
     * @param limit 数量限制
     * @return 收藏列表
     */
    List<Favorite> getRecentFavorites(Long userId, Integer limit);

    /**
     * 批量检查是否已收藏
     * @param userId 用户ID
     * @param targetType 目标类型
     * @param targetIds 目标ID列表
     * @return 目标ID到是否收藏的映射
     */
    Map<Long, Boolean> batchCheckFavorited(Long userId, String targetType, List<Long> targetIds);
}