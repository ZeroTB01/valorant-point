package com.escape.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.escape.common.Result;
import com.escape.common.ResultCode;
import com.escape.entity.ViewHistory;
import com.escape.service.ViewHistoryService;
import com.escape.utils.JwtUtils;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 浏览历史控制器
 * 提供浏览历史管理相关API接口
 *
 * @author escape
 * @since 2025-06-14
 */
@Slf4j
@RestController
@RequestMapping("/history")
public class ViewHistoryController {

    @Autowired
    private ViewHistoryService viewHistoryService;

    @Autowired
    private JwtUtils jwtUtils;

    // ==================== 记录浏览历史接口 ====================

    /**
     * 记录浏览历史
     */
    @PostMapping("/record")
    public Result<String> recordView(@RequestBody ViewRecordRequest request,
                                     @RequestHeader("Authorization") String token) {
        try {
            Long userId = getUserIdFromToken(token);

            if (userId == -1L) {
                return Result.success("游客浏览记录不保存");
            }

            boolean success = viewHistoryService.recordView(
                    userId,
                    request.getTargetType(),
                    request.getTargetId(),
                    request.getDuration(),
                    request.getProgress()
            );

            if (success) {
                return Result.success("浏览记录保存成功");
            } else {
                return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "记录保存失败");
            }
        } catch (RuntimeException e) {
            log.warn("记录浏览历史失败: {}", e.getMessage());
            return handleHistoryException(e);
        } catch (Exception e) {
            log.error("记录浏览历史系统错误", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 更新浏览记录（更新时长和进度）
     */
    @PutMapping("/update")
    public Result<String> updateViewRecord(@RequestBody ViewUpdateRequest request,
                                           @RequestHeader("Authorization") String token) {
        try {
            Long userId = getUserIdFromToken(token);

            if (userId == -1L) {
                return Result.success("游客浏览记录不保存");
            }

            boolean success = viewHistoryService.updateViewRecord(
                    userId,
                    request.getTargetType(),
                    request.getTargetId(),
                    request.getDuration(),
                    request.getProgress()
            );

            if (success) {
                return Result.success("浏览记录更新成功");
            } else {
                return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "记录更新失败");
            }
        } catch (RuntimeException e) {
            log.warn("更新浏览记录失败: {}", e.getMessage());
            return handleHistoryException(e);
        } catch (Exception e) {
            log.error("更新浏览记录系统错误", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    // ==================== 查询浏览历史接口 ====================

    /**
     * 获取用户的浏览历史（按类型）
     */
    @GetMapping("/list")
    public Result<List<ViewHistory>> getUserHistory(@RequestParam @NotBlank(message = "目标类型不能为空") String targetType,
                                                    @RequestHeader("Authorization") String token) {
        try {
            Long userId = getUserIdFromToken(token);

            if (userId == -1L) {
                return Result.error(ResultCode.LOGIN_REQUIRED.getCode(), "游客用户请先登录");
            }

            List<ViewHistory> histories = viewHistoryService.getUserHistory(userId, targetType);
            return Result.success(histories);
        } catch (RuntimeException e) {
            log.warn("获取浏览历史失败: targetType={}, 原因: {}", targetType, e.getMessage());
            return handleHistoryException(e);
        } catch (Exception e) {
            log.error("获取浏览历史系统错误: targetType={}", targetType, e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取最近的浏览历史
     */
    @GetMapping("/recent")
    public Result<List<ViewHistory>> getRecentHistory(@RequestParam(defaultValue = "20") Integer limit,
                                                      @RequestHeader("Authorization") String token) {
        try {
            Long userId = getUserIdFromToken(token);

            if (userId == -1L) {
                return Result.error(ResultCode.LOGIN_REQUIRED.getCode(), "游客用户请先登录");
            }

            List<ViewHistory> histories = viewHistoryService.getRecentHistory(userId, limit);
            return Result.success(histories);
        } catch (Exception e) {
            log.error("获取最近浏览历史失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取今日浏览历史
     */
    @GetMapping("/today")
    public Result<List<ViewHistory>> getTodayHistory(@RequestHeader("Authorization") String token) {
        try {
            Long userId = getUserIdFromToken(token);

            if (userId == -1L) {
                return Result.error(ResultCode.LOGIN_REQUIRED.getCode(), "游客用户请先登录");
            }

            List<ViewHistory> histories = viewHistoryService.getTodayHistory(userId);
            return Result.success(histories);
        } catch (Exception e) {
            log.error("获取今日浏览历史失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取指定时间段的浏览历史
     */
    @GetMapping("/range")
    public Result<List<ViewHistory>> getHistoryByTimeRange(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestHeader("Authorization") String token) {
        try {
            Long userId = getUserIdFromToken(token);

            if (userId == -1L) {
                return Result.error(ResultCode.LOGIN_REQUIRED.getCode(), "游客用户请先登录");
            }

            List<ViewHistory> histories = viewHistoryService.getHistoryByTimeRange(userId, startTime, endTime);
            return Result.success(histories);
        } catch (RuntimeException e) {
            log.warn("获取时间段浏览历史失败: {}", e.getMessage());
            return handleHistoryException(e);
        } catch (Exception e) {
            log.error("获取时间段浏览历史系统错误", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取浏览历史详情（分页）
     */
    @GetMapping("/details")
    public Result<IPage<Map<String, Object>>> getHistoryDetails(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String targetType,
            @RequestHeader("Authorization") String token) {
        try {
            Long userId = getUserIdFromToken(token);

            if (userId == -1L) {
                return Result.error(ResultCode.LOGIN_REQUIRED.getCode(), "游客用户请先登录");
            }

            Page<ViewHistory> page = new Page<>(current, size);
            IPage<Map<String, Object>> details = viewHistoryService.getHistoryDetails(page, userId, targetType);
            return Result.success(details);
        } catch (Exception e) {
            log.error("获取浏览历史详情失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取未完成的视频
     */
    @GetMapping("/unfinished-videos")
    public Result<List<Map<String, Object>>> getUnfinishedVideos(@RequestHeader("Authorization") String token) {
        try {
            Long userId = getUserIdFromToken(token);

            if (userId == -1L) {
                return Result.error(ResultCode.LOGIN_REQUIRED.getCode(), "游客用户请先登录");
            }

            List<Map<String, Object>> videos = viewHistoryService.getUnfinishedVideos(userId);
            return Result.success(videos);
        } catch (Exception e) {
            log.error("获取未完成视频失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    // ==================== 统计分析接口 ====================

    /**
     * 获取用户浏览统计
     */
    @GetMapping("/statistics")
    public Result<List<Map<String, Object>>> getUserViewStatistics(@RequestHeader("Authorization") String token) {
        try {
            Long userId = getUserIdFromToken(token);

            if (userId == -1L) {
                return Result.error(ResultCode.LOGIN_REQUIRED.getCode(), "游客用户请先登录");
            }

            List<Map<String, Object>> statistics = viewHistoryService.getUserViewStatistics(userId);
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取浏览统计失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取观看时长统计
     */
    @GetMapping("/duration-statistics")
    public Result<Map<String, Object>> getViewDurationStatistics(@RequestHeader("Authorization") String token) {
        try {
            Long userId = getUserIdFromToken(token);

            if (userId == -1L) {
                return Result.error(ResultCode.LOGIN_REQUIRED.getCode(), "游客用户请先登录");
            }

            Map<String, Object> statistics = viewHistoryService.getViewDurationStatistics(userId);
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取观看时长统计失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取浏览历史热力图
     */
    @GetMapping("/heatmap")
    public Result<Map<String, Object>> getViewHeatmap(@RequestParam(defaultValue = "30") Integer days,
                                                      @RequestHeader("Authorization") String token) {
        try {
            Long userId = getUserIdFromToken(token);

            if (userId == -1L) {
                return Result.error(ResultCode.LOGIN_REQUIRED.getCode(), "游客用户请先登录");
            }

            Map<String, Object> heatmap = viewHistoryService.getViewHeatmap(userId, days);
            return Result.success(heatmap);
        } catch (Exception e) {
            log.error("获取浏览热力图失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取智能推荐
     */
    @GetMapping("/recommendations")
    public Result<List<Map<String, Object>>> getRecommendations(
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestHeader("Authorization") String token) {
        try {
            Long userId = getUserIdFromToken(token);

            if (userId == -1L) {
                return Result.error(ResultCode.LOGIN_REQUIRED.getCode(), "游客用户请先登录");
            }

            List<Map<String, Object>> recommendations = viewHistoryService.getRecommendations(userId, limit);
            return Result.success(recommendations);
        } catch (Exception e) {
            log.error("获取推荐内容失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    // ==================== 历史管理接口 ====================

    /**
     * 删除指定的浏览历史
     */
    @DeleteMapping("/delete")
    public Result<Integer> deleteHistory(@RequestBody List<Long> historyIds,
                                         @RequestHeader("Authorization") String token) {
        try {
            Long userId = getUserIdFromToken(token);

            if (userId == -1L) {
                return Result.error(ResultCode.LOGIN_REQUIRED.getCode(), "游客用户请先登录");
            }

            int count = viewHistoryService.batchDeleteHistory(userId, historyIds);
            return Result.success("删除成功", count);
        } catch (Exception e) {
            log.error("删除浏览历史失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 清空所有浏览历史
     */
    @DeleteMapping("/clear")
    public Result<Integer> clearAllHistory(@RequestHeader("Authorization") String token) {
        try {
            Long userId = getUserIdFromToken(token);

            if (userId == -1L) {
                return Result.error(ResultCode.LOGIN_REQUIRED.getCode(), "游客用户请先登录");
            }

            int count = viewHistoryService.clearAllHistory(userId);
            return Result.success("清空成功", count);
        } catch (Exception e) {
            log.error("清空浏览历史失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 删除指定时间之前的历史记录
     */
    @DeleteMapping("/clean")
    public Result<Integer> cleanOldHistory(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime beforeTime,
            @RequestHeader("Authorization") String token) {
        try {
            Long userId = getUserIdFromToken(token);

            if (userId == -1L) {
                return Result.error(ResultCode.LOGIN_REQUIRED.getCode(), "游客用户请先登录");
            }

            int count = viewHistoryService.deleteOldHistory(userId, beforeTime);
            return Result.success("清理成功", count);
        } catch (Exception e) {
            log.error("清理旧历史记录失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    // ==================== 公共查询接口 ====================

    /**
     * 获取内容的浏览用户数
     */
    @GetMapping("/viewer-count")
    public Result<Integer> getUniqueViewerCount(
            @RequestParam @NotBlank(message = "目标类型不能为空") String targetType,
            @RequestParam @NotNull(message = "目标ID不能为空") Long targetId) {
        try {
            int count = viewHistoryService.getUniqueViewerCount(targetType, targetId);
            return Result.success(count);
        } catch (Exception e) {
            log.error("获取浏览用户数失败: targetType={}, targetId={}", targetType, targetId, e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    // ==================== 请求DTO ====================

    /**
     * 记录浏览请求DTO
     */
    @Data
    public static class ViewRecordRequest {
        @NotBlank(message = "目标类型不能为空")
        private String targetType;

        @NotNull(message = "目标ID不能为空")
        private Long targetId;

        private Integer duration;
        private Integer progress;
    }

    /**
     * 更新浏览请求DTO
     */
    @Data
    public static class ViewUpdateRequest {
        @NotBlank(message = "目标类型不能为空")
        private String targetType;

        @NotNull(message = "目标ID不能为空")
        private Long targetId;

        private Integer duration;
        private Integer progress;
    }

    // ==================== 工具方法 ====================

    /**
     * 从Token中获取用户ID
     */
    private Long getUserIdFromToken(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new RuntimeException("Token格式错误");
        }

        String actualToken = token.substring(7);
        Long userId = jwtUtils.getUserIdFromToken(actualToken);

        if (userId == null) {
            throw new RuntimeException("无法从Token中获取用户信息");
        }

        return userId;
    }

    /**
     * 统一处理浏览历史相关异常
     */
    private <T> Result<T> handleHistoryException(RuntimeException e) {
        String message = e.getMessage();

        if (message.contains("Token") || message.contains("无效") || message.contains("过期")) {
            return Result.error(ResultCode.TOKEN_INVALID);
        } else if (message.contains("参数") || message.contains("无效")) {
            return Result.error(ResultCode.BAD_REQUEST.getCode(), message);
        } else {
            return Result.error(ResultCode.BAD_REQUEST.getCode(), message);
        }
    }
}