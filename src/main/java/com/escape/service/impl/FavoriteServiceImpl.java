package com.escape.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.escape.common.ResultCode;
import com.escape.common.exception.BusinessException;
import com.escape.entity.Favorite;
import com.escape.mapper.FavoriteMapper;
import com.escape.service.ContentService;
import com.escape.service.FavoriteService;
import com.escape.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 收藏服务实现类
 *
 * @author escape
 * @since 2025-06-14
 */
@Slf4j
@Service
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite> implements FavoriteService {

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private ContentService contentService;

    @Autowired
    private RedisUtils redisUtils;

    private static final String FAVORITE_CACHE_PREFIX = "favorite:";
    private static final String HOT_FAVORITES_KEY = "favorite:hot:";
    private static final String DEFAULT_FOLDER = "默认收藏夹";
    private static final long CACHE_EXPIRE_MINUTES = 30;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addFavorite(Long userId, String targetType, Long targetId, String folderName) {
        // 参数验证
        validateParams(userId, targetType, targetId);

        // 检查是否已收藏
        if (favoriteMapper.existsByUserAndTarget(userId, targetType, targetId)) {
            throw new BusinessException(ResultCode.DATA_ALREADY_EXISTS.getCode(), "已经收藏过了");
        }

        // 创建收藏记录
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setTargetType(targetType);
        favorite.setTargetId(targetId);
        favorite.setFolderName(StringUtils.hasText(folderName) ? folderName : DEFAULT_FOLDER);

        int result = favoriteMapper.insert(favorite);

        if (result > 0) {
            // 更新目标的收藏数（如果是内容）
            if ("content".equals(targetType)) {
                contentService.updateCollectCount(targetId, 1);
            }

            // 清除缓存
            clearUserFavoriteCache(userId);

            log.info("添加收藏成功: userId={}, targetType={}, targetId={}", userId, targetType, targetId);
            return true;
        }

        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeFavorite(Long userId, String targetType, Long targetId) {
        // 参数验证
        validateParams(userId, targetType, targetId);

        // 删除收藏记录
        QueryWrapper<Favorite> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
                .eq("target_type", targetType)
                .eq("target_id", targetId);

        int result = favoriteMapper.delete(wrapper);

        if (result > 0) {
            // 更新目标的收藏数（如果是内容）
            if ("content".equals(targetType)) {
                contentService.updateCollectCount(targetId, -1);
            }

            // 清除缓存
            clearUserFavoriteCache(userId);

            log.info("取消收藏成功: userId={}, targetType={}, targetId={}", userId, targetType, targetId);
            return true;
        }

        return false;
    }

    @Override
    public boolean isFavorited(Long userId, String targetType, Long targetId) {
        // 参数验证
        validateParams(userId, targetType, targetId);

        // 构建缓存键
        String cacheKey = FAVORITE_CACHE_PREFIX + "check:" + userId + ":" + targetType + ":" + targetId;

        // 尝试从缓存获取
        String cached = redisUtils.get(cacheKey);
        if (cached != null) {
            return "1".equals(cached);
        }

        // 查询数据库
        boolean exists = favoriteMapper.existsByUserAndTarget(userId, targetType, targetId);

        // 缓存结果
        redisUtils.set(cacheKey, exists ? "1" : "0", CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        return exists;
    }

    @Override
    public List<Favorite> getUserFavorites(Long userId, String targetType) {
        if (userId == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "用户ID不能为空");
        }

        if (!StringUtils.hasText(targetType)) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "目标类型不能为空");
        }

        return favoriteMapper.findByUserAndType(userId, targetType);
    }

    @Override
    public List<String> getUserFolders(Long userId) {
        if (userId == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "用户ID不能为空");
        }

        return favoriteMapper.findUserFolders(userId);
    }

    @Override
    public List<Favorite> getFavoritesByFolder(Long userId, String folderName) {
        if (userId == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "用户ID不能为空");
        }

        if (!StringUtils.hasText(folderName)) {
            folderName = DEFAULT_FOLDER;
        }

        return favoriteMapper.findByUserAndFolder(userId, folderName);
    }

    @Override
    public List<Map<String, Object>> getUserFavoriteStatistics(Long userId) {
        if (userId == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "用户ID不能为空");
        }

        return favoriteMapper.countByUserAndType(userId);
    }

    @Override
    public List<Map<String, Object>> getUserFolderStatistics(Long userId) {
        if (userId == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "用户ID不能为空");
        }

        return favoriteMapper.countByUserAndFolder(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean moveFavorite(Long userId, Long favoriteId, String newFolder) {
        if (userId == null || favoriteId == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "参数不能为空");
        }

        if (!StringUtils.hasText(newFolder)) {
            newFolder = DEFAULT_FOLDER;
        }

        int result = favoriteMapper.moveToFolder(favoriteId, userId, newFolder);

        if (result > 0) {
            clearUserFavoriteCache(userId);
            return true;
        }

        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchRemoveFavorites(Long userId, List<Long> favoriteIds) {
        if (userId == null || favoriteIds == null || favoriteIds.isEmpty()) {
            return 0;
        }

        // 获取要删除的收藏信息，用于更新收藏数
        QueryWrapper<Favorite> query = new QueryWrapper<>();
        query.eq("user_id", userId).in("id", favoriteIds);
        List<Favorite> favorites = favoriteMapper.selectList(query);

        // 执行批量删除
        int result = favoriteMapper.batchDelete(userId, favoriteIds);

        if (result > 0) {
            // 更新内容的收藏数
            Map<Long, Long> contentCounts = favorites.stream()
                    .filter(f -> "content".equals(f.getTargetType()))
                    .collect(Collectors.groupingBy(Favorite::getTargetId, Collectors.counting()));

            for (Map.Entry<Long, Long> entry : contentCounts.entrySet()) {
                contentService.updateCollectCount(entry.getKey(), -entry.getValue().intValue());
            }

            // 清除缓存
            clearUserFavoriteCache(userId);
        }

        return result;
    }

    @Override
    public IPage<Map<String, Object>> getFavoriteDetails(Page<Favorite> page, Long userId, String targetType, String folderName) {
        if (userId == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "用户ID不能为空");
        }

        return favoriteMapper.selectFavoriteDetails(page, userId, targetType, folderName);
    }

    @Override
    public int getFavoriteCount(String targetType, Long targetId) {
        validateTargetParams(targetType, targetId);

        // 构建缓存键
        String cacheKey = FAVORITE_CACHE_PREFIX + "count:" + targetType + ":" + targetId;

        // 尝试从缓存获取
        String cached = redisUtils.get(cacheKey);
        if (cached != null) {
            return Integer.parseInt(cached);
        }

        // 查询数据库
        int count = favoriteMapper.countByTarget(targetType, targetId);

        // 缓存结果
        redisUtils.set(cacheKey, String.valueOf(count), CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        return count;
    }

    @Override
    public List<Map<String, Object>> getHotFavorites(String targetType, Integer limit) {
        if (!StringUtils.hasText(targetType)) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "目标类型不能为空");
        }

        if (limit == null || limit <= 0) {
            limit = 10;
        }

        // 缓存键
        String cacheKey = HOT_FAVORITES_KEY + targetType + ":" + limit;

        // 尝试从缓存获取
        String cachedData = redisUtils.get(cacheKey);
        if (StringUtils.hasText(cachedData)) {
            log.debug("从缓存获取热门收藏");
        }

        List<Map<String, Object>> hotFavorites = favoriteMapper.findHotTargets(targetType, limit);

        // 缓存30分钟
        redisUtils.set(cacheKey, hotFavorites.toString(), 30, TimeUnit.MINUTES);

        return hotFavorites;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createFolder(Long userId, String folderName) {
        if (userId == null || !StringUtils.hasText(folderName)) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "参数不能为空");
        }

        // 检查收藏夹是否已存在
        List<String> folders = getUserFolders(userId);
        if (folders.contains(folderName)) {
            throw new BusinessException(ResultCode.DATA_ALREADY_EXISTS.getCode(), "收藏夹已存在");
        }

        // 这里暂时不需要创建实际记录，当用户添加收藏时会自动创建
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean renameFolder(Long userId, String oldName, String newName) {
        if (userId == null || !StringUtils.hasText(oldName) || !StringUtils.hasText(newName)) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "参数不能为空");
        }

        // 更新所有该收藏夹下的收藏
        UpdateWrapper<Favorite> wrapper = new UpdateWrapper<>();
        wrapper.eq("user_id", userId)
                .eq("folder_name", oldName)
                .set("folder_name", newName);

        int result = favoriteMapper.update(null, wrapper);

        if (result > 0) {
            clearUserFavoriteCache(userId);
            log.info("重命名收藏夹成功: userId={}, {} -> {}", userId, oldName, newName);
            return true;
        }

        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteFolder(Long userId, String folderName) {
        if (userId == null || !StringUtils.hasText(folderName)) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "参数不能为空");
        }

        if (DEFAULT_FOLDER.equals(folderName)) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "默认收藏夹不能删除");
        }

        // 将该收藏夹下的所有收藏移到默认收藏夹
        UpdateWrapper<Favorite> wrapper = new UpdateWrapper<>();
        wrapper.eq("user_id", userId)
                .eq("folder_name", folderName)
                .set("folder_name", DEFAULT_FOLDER);

        int result = favoriteMapper.update(null, wrapper);

        if (result > 0) {
            clearUserFavoriteCache(userId);
            log.info("删除收藏夹成功: userId={}, folderName={}", userId, folderName);
            return true;
        }

        return true; // 即使没有数据也返回成功
    }

    @Override
    public List<Favorite> getRecentFavorites(Long userId, Integer limit) {
        if (userId == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "用户ID不能为空");
        }

        if (limit == null || limit <= 0) {
            limit = 10;
        }

        return favoriteMapper.findRecentFavorites(userId, limit);
    }

    @Override
    public Map<Long, Boolean> batchCheckFavorited(Long userId, String targetType, List<Long> targetIds) {
        Map<Long, Boolean> result = new HashMap<>();

        if (userId == null || !StringUtils.hasText(targetType) || targetIds == null || targetIds.isEmpty()) {
            return result;
        }

        // 查询已收藏的目标
        QueryWrapper<Favorite> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
                .eq("target_type", targetType)
                .in("target_id", targetIds);

        List<Favorite> favorites = favoriteMapper.selectList(wrapper);

        // 构建结果
        for (Long targetId : targetIds) {
            result.put(targetId, favorites.stream().anyMatch(f -> f.getTargetId().equals(targetId)));
        }

        return result;
    }

    /**
     * 验证参数
     */
    private void validateParams(Long userId, String targetType, Long targetId) {
        if (userId == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "用户ID不能为空");
        }
        validateTargetParams(targetType, targetId);
    }

    /**
     * 验证目标参数
     */
    private void validateTargetParams(String targetType, Long targetId) {
        if (!StringUtils.hasText(targetType)) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "目标类型不能为空");
        }
        if (!isValidTargetType(targetType)) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "无效的目标类型");
        }
        if (targetId == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "目标ID不能为空");
        }
    }

    /**
     * 验证目标类型是否合法
     */
    private boolean isValidTargetType(String targetType) {
        return "content".equals(targetType) || "position".equals(targetType)
                || "hero".equals(targetType) || "map".equals(targetType)
                || "weapon".equals(targetType);
    }

    /**
     * 清除用户收藏缓存
     */
    private void clearUserFavoriteCache(Long userId) {
        // 清除该用户的所有收藏相关缓存
        redisUtils.delete(redisUtils.keys(FAVORITE_CACHE_PREFIX + "check:" + userId + ":*"));
        // 清除热门收藏缓存
        redisUtils.delete(redisUtils.keys(HOT_FAVORITES_KEY + "*"));
    }
}