package com.escape.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.escape.common.ResultCode;
import com.escape.common.exception.BusinessException;
import com.escape.entity.ViewHistory;
import com.escape.mapper.ViewHistoryMapper;
import com.escape.service.ViewHistoryService;
import com.escape.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 浏览历史服务实现类
 *
 * @author escape
 * @since 2025-06-14
 */
@Slf4j
@Service
public class ViewHistoryServiceImpl extends ServiceImpl<ViewHistoryMapper, ViewHistory> implements ViewHistoryService {

    @Autowired
    private ViewHistoryMapper viewHistoryMapper;

    @Autowired
    private RedisUtils redisUtils;

    private static final String VIEW_HISTORY_CACHE_PREFIX = "view:history:";
    private static final String VIEW_COUNT_CACHE_PREFIX = "view:count:";
    private static final long CACHE_EXPIRE_MINUTES = 30;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean recordView(Long userId, String targetType, Long targetId, Integer duration, Integer progress) {
        // 参数验证
        validateParams(userId, targetType, targetId);

        try {
            // 使用insertOrUpdate，避免重复记录
            int result = viewHistoryMapper.insertOrUpdate(userId, targetType, targetId,
                    duration != null ? duration : 0,
                    progress != null ? progress : 0);

            if (result > 0) {
                // 清除缓存
                clearUserHistoryCache(userId);
                log.debug("记录浏览历史成功: userId={}, targetType={}, targetId={}", userId, targetType, targetId);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("记录浏览历史失败", e);
            throw new BusinessException(ResultCode.DATABASE_ERROR.getCode(), "记录浏览历史失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateViewRecord(Long userId, String targetType, Long targetId, Integer duration, Integer progress) {
        // 参数验证
        validateParams(userId, targetType, targetId);

        // 查找现有记录
        ViewHistory history = viewHistoryMapper.findByUserAndTarget(userId, targetType, targetId);

        if (history != null) {
            int result = viewHistoryMapper.updateViewRecord(history.getId(),
                    duration != null ? duration : 0,
                    progress != null ? progress : 0);

            if (result > 0) {
                clearUserHistoryCache(userId);
                return true;
            }
        } else {
            // 如果不存在，创建新记录
            return recordView(userId, targetType, targetId, duration, progress);
        }

        return false;
    }

    @Override
    public List<ViewHistory> getUserHistory(Long userId, String targetType) {
        if (userId == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "用户ID不能为空");
        }

        if (!StringUtils.hasText(targetType)) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "目标类型不能为空");
        }

        return viewHistoryMapper.findByUserAndType(userId, targetType);
    }

    @Override
    public List<ViewHistory> getRecentHistory(Long userId, Integer limit) {
        if (userId == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "用户ID不能为空");
        }

        if (limit == null || limit <= 0) {
            limit = 20;
        }

        // 缓存键
        String cacheKey = VIEW_HISTORY_CACHE_PREFIX + "recent:" + userId + ":" + limit;

        // 尝试从缓存获取
        String cachedData = redisUtils.get(cacheKey);
        if (StringUtils.hasText(cachedData)) {
            log.debug("从缓存获取最近浏览历史");
        }

        List<ViewHistory> histories = viewHistoryMapper.findRecentHistory(userId, limit);

        // 缓存结果
        redisUtils.set(cacheKey, histories.toString(), CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        return histories;
    }

    @Override
    public List<ViewHistory> getHistoryByTimeRange(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        if (userId == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "用户ID不能为空");
        }

        if (startTime == null || endTime == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "时间范围不能为空");
        }

        if (startTime.isAfter(endTime)) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "开始时间不能晚于结束时间");
        }

        return viewHistoryMapper.findByUserAndTimeRange(userId, startTime, endTime);
    }

    @Override
    public IPage<Map<String, Object>> getHistoryDetails(Page<ViewHistory> page, Long userId, String targetType) {
        if (userId == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "用户ID不能为空");
        }

        return viewHistoryMapper.selectHistoryDetails(page, userId, targetType);
    }

    @Override
    public List<Map<String, Object>> getUserViewStatistics(Long userId) {
        if (userId == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "用户ID不能为空");
        }

        return viewHistoryMapper.statisticsByUser(userId);
    }

    @Override
    public List<Map<String, Object>> getUnfinishedVideos(Long userId) {
        if (userId == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "用户ID不能为空");
        }

        return viewHistoryMapper.findUnfinishedVideos(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteOldHistory(Long userId, LocalDateTime beforeTime) {
        if (userId == null || beforeTime == null) {
            return 0;
        }

        int result = viewHistoryMapper.deleteOldHistory(userId, beforeTime);

        if (result > 0) {
            clearUserHistoryCache(userId);
            log.info("删除旧浏览历史成功: userId={}, beforeTime={}, count={}", userId, beforeTime, result);
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDeleteHistory(Long userId, List<Long> historyIds) {
        if (userId == null || historyIds == null || historyIds.isEmpty()) {
            return 0;
        }

        int result = viewHistoryMapper.batchDelete(userId, historyIds);

        if (result > 0) {
            clearUserHistoryCache(userId);
            log.info("批量删除浏览历史成功: userId={}, count={}", userId, result);
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int clearAllHistory(Long userId) {
        if (userId == null) {
            return 0;
        }

        int result = viewHistoryMapper.clearUserHistory(userId);

        if (result > 0) {
            clearUserHistoryCache(userId);
            log.info("清空用户浏览历史成功: userId={}, count={}", userId, result);
        }

        return result;
    }

    @Override
    public int getUniqueViewerCount(String targetType, Long targetId) {
        if (!StringUtils.hasText(targetType) || targetId == null) {
            return 0;
        }

        // 缓存键
        String cacheKey = VIEW_COUNT_CACHE_PREFIX + targetType + ":" + targetId;

        // 尝试从缓存获取
        String cached = redisUtils.get(cacheKey);
        if (cached != null) {
            return Integer.parseInt(cached);
        }

        int count = viewHistoryMapper.countUniqueViewers(targetType, targetId);

        // 缓存结果
        redisUtils.set(cacheKey, String.valueOf(count), CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        return count;
    }

    @Override
    public List<ViewHistory> getTodayHistory(Long userId) {
        if (userId == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "用户ID不能为空");
        }

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        return getHistoryByTimeRange(userId, startOfDay, endOfDay);
    }

    @Override
    public Map<String, Object> getViewHeatmap(Long userId, Integer days) {
        if (userId == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "用户ID不能为空");
        }

        if (days == null || days <= 0) {
            days = 30;
        }

        Map<String, Object> heatmap = new HashMap<>();
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(days);

        // 获取时间范围内的浏览历史
        List<ViewHistory> histories = getHistoryByTimeRange(userId, startTime, endTime);

        // 按日期分组统计
        Map<String, Long> dailyCounts = histories.stream()
                .collect(Collectors.groupingBy(
                        h -> h.getUpdateTime().format(DateTimeFormatter.ISO_LOCAL_DATE),
                        Collectors.counting()
                ));

        // 生成完整的日期范围
        List<Map<String, Object>> data = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            LocalDate date = LocalDate.now().minusDays(i);
            String dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE);

            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", dateStr);
            dayData.put("count", dailyCounts.getOrDefault(dateStr, 0L));
            data.add(dayData);
        }

        heatmap.put("data", data);
        heatmap.put("totalDays", days);
        heatmap.put("totalViews", histories.size());

        return heatmap;
    }

    @Override
    public Map<String, Object> getViewDurationStatistics(Long userId) {
        if (userId == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "用户ID不能为空");
        }

        Map<String, Object> statistics = new HashMap<>();

        // 获取用户所有浏览历史
        QueryWrapper<ViewHistory> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        List<ViewHistory> histories = viewHistoryMapper.selectList(wrapper);

        // 统计总时长
        int totalDuration = histories.stream()
                .mapToInt(ViewHistory::getViewDuration)
                .sum();

        // 按类型统计时长
        Map<String, Integer> durationByType = histories.stream()
                .collect(Collectors.groupingBy(
                        ViewHistory::getTargetType,
                        Collectors.summingInt(ViewHistory::getViewDuration)
                ));

        // 计算平均观看时长
        double avgDuration = histories.isEmpty() ? 0 :
                (double) totalDuration / histories.size();

        statistics.put("totalDuration", totalDuration);
        statistics.put("totalCount", histories.size());
        statistics.put("avgDuration", avgDuration);
        statistics.put("durationByType", durationByType);

        return statistics;
    }

    @Override
    public List<Map<String, Object>> getRecommendations(Long userId, Integer limit) {
        if (userId == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "用户ID不能为空");
        }

        if (limit == null || limit <= 0) {
            limit = 10;
        }

        // 获取用户最近的浏览历史
        List<ViewHistory> recentHistories = getRecentHistory(userId, 50);

        // 统计最常浏览的类型和标签
        Map<String, Long> typeFrequency = recentHistories.stream()
                .collect(Collectors.groupingBy(ViewHistory::getTargetType, Collectors.counting()));

        // TODO: 基于浏览历史的智能推荐算法
        // 这里简化处理，实际项目中应该实现更复杂的推荐算法
        List<Map<String, Object>> recommendations = new ArrayList<>();

        return recommendations;
    }

    /**
     * 验证参数
     */
    private void validateParams(Long userId, String targetType, Long targetId) {
        if (userId == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "用户ID不能为空");
        }
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
     * 清除用户浏览历史缓存
     */
    private void clearUserHistoryCache(Long userId) {
        // 清除该用户的所有浏览历史相关缓存
        redisUtils.delete(redisUtils.keys(VIEW_HISTORY_CACHE_PREFIX + "*:" + userId + ":*"));
    }
}