package com.escape.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.escape.entity.ViewHistory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 浏览历史服务接口
 *
 * @author escape
 * @since 2025-06-14
 */
public interface ViewHistoryService extends IService<ViewHistory> {

    /**
     * 记录浏览历史
     * @param userId 用户ID
     * @param targetType 目标类型：content/position/hero/map/weapon
     * @param targetId 目标ID
     * @param duration 浏览时长（秒）
     * @param progress 进度百分比（视频类）
     * @return 是否成功
     */
    boolean recordView(Long userId, String targetType, Long targetId, Integer duration, Integer progress);

    /**
     * 更新浏览记录（时长和进度）
     * @param userId 用户ID
     * @param targetType 目标类型
     * @param targetId 目标ID
     * @param duration 新增时长（秒）
     * @param progress 当前进度
     * @return 是否成功
     */
    boolean updateViewRecord(Long userId, String targetType, Long targetId, Integer duration, Integer progress);

    /**
     * 获取用户的浏览历史（按类型）
     * @param userId 用户ID
     * @param targetType 目标类型
     * @return 浏览历史列表
     */
    List<ViewHistory> getUserHistory(Long userId, String targetType);

    /**
     * 获取用户最近的浏览历史
     * @param userId 用户ID
     * @param limit 数量限制
     * @return 浏览历史列表
     */
    List<ViewHistory> getRecentHistory(Long userId, Integer limit);

    /**
     * 获取用户指定时间段的浏览历史
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 浏览历史列表
     */
    List<ViewHistory> getHistoryByTimeRange(Long userId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取浏览历史详情（分页）
     * @param page 分页参数
     * @param userId 用户ID
     * @param targetType 目标类型（可选）
     * @return 分页结果
     */
    IPage<Map<String, Object>> getHistoryDetails(Page<ViewHistory> page, Long userId, String targetType);

    /**
     * 获取用户浏览统计
     * @param userId 用户ID
     * @return 统计结果
     */
    List<Map<String, Object>> getUserViewStatistics(Long userId);

    /**
     * 获取未完成的视频
     * @param userId 用户ID
     * @return 未完成视频列表
     */
    List<Map<String, Object>> getUnfinishedVideos(Long userId);

    /**
     * 删除指定时间之前的历史记录
     * @param userId 用户ID
     * @param beforeTime 时间点
     * @return 删除数量
     */
    int deleteOldHistory(Long userId, LocalDateTime beforeTime);

    /**
     * 批量删除浏览历史
     * @param userId 用户ID
     * @param historyIds 历史记录ID列表
     * @return 删除数量
     */
    int batchDeleteHistory(Long userId, List<Long> historyIds);

    /**
     * 清空用户的所有浏览历史
     * @param userId 用户ID
     * @return 删除数量
     */
    int clearAllHistory(Long userId);

    /**
     * 获取内容的浏览用户数
     * @param targetType 目标类型
     * @param targetId 目标ID
     * @return 用户数
     */
    int getUniqueViewerCount(String targetType, Long targetId);

    /**
     * 获取今日浏览历史
     * @param userId 用户ID
     * @return 浏览历史列表
     */
    List<ViewHistory> getTodayHistory(Long userId);

    /**
     * 获取浏览历史热力图数据
     * @param userId 用户ID
     * @param days 最近天数
     * @return 热力图数据
     */
    Map<String, Object> getViewHeatmap(Long userId, Integer days);

    /**
     * 获取观看时长统计
     * @param userId 用户ID
     * @return 统计数据
     */
    Map<String, Object> getViewDurationStatistics(Long userId);

    /**
     * 智能推荐（基于浏览历史）
     * @param userId 用户ID
     * @param limit 推荐数量
     * @return 推荐内容
     */
    List<Map<String, Object>> getRecommendations(Long userId, Integer limit);
}