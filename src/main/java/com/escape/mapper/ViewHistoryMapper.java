package com.escape.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.escape.entity.ViewHistory;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 浏览历史数据访问层
 *
 * @author escape
 * @since 2025-06-11
 */
@Mapper
public interface ViewHistoryMapper extends BaseMapper<ViewHistory> {

    /**
     * 查询用户的浏览历史（按类型）
     */
    @Select("SELECT * FROM view_history WHERE user_id = #{userId} " +
            "AND target_type = #{targetType} " +
            "ORDER BY update_time DESC")
    List<ViewHistory> findByUserAndType(@Param("userId") Long userId,
                                        @Param("targetType") String targetType);

    /**
     * 查询用户最近的浏览历史
     */
    @Select("SELECT * FROM view_history WHERE user_id = #{userId} " +
            "ORDER BY update_time DESC LIMIT #{limit}")
    List<ViewHistory> findRecentHistory(@Param("userId") Long userId,
                                        @Param("limit") Integer limit);

    /**
     * 查询或创建浏览记录
     */
    @Select("SELECT * FROM view_history WHERE user_id = #{userId} " +
            "AND target_type = #{targetType} AND target_id = #{targetId}")
    ViewHistory findByUserAndTarget(@Param("userId") Long userId,
                                    @Param("targetType") String targetType,
                                    @Param("targetId") Long targetId);

    /**
     * 更新浏览记录（时间、时长、进度）
     */
    @Update("UPDATE view_history SET " +
            "view_duration = view_duration + #{duration}, " +
            "progress = #{progress}, " +
            "update_time = NOW() " +
            "WHERE id = #{id}")
    int updateViewRecord(@Param("id") Long id,
                         @Param("duration") Integer duration,
                         @Param("progress") Integer progress);

    /**
     * 统计用户各类型浏览数量
     */
    @Select("SELECT target_type, COUNT(*) as count, " +
            "SUM(view_duration) as total_duration " +
            "FROM view_history WHERE user_id = #{userId} " +
            "GROUP BY target_type")
    List<Map<String, Object>> statisticsByUser(@Param("userId") Long userId);

    /**
     * 查询用户指定时间段的浏览历史
     */
    @Select("SELECT * FROM view_history WHERE user_id = #{userId} " +
            "AND update_time BETWEEN #{startTime} AND #{endTime} " +
            "ORDER BY update_time DESC")
    List<ViewHistory> findByUserAndTimeRange(@Param("userId") Long userId,
                                             @Param("startTime") LocalDateTime startTime,
                                             @Param("endTime") LocalDateTime endTime);

    /**
     * 删除指定时间之前的历史记录
     */
    @Delete("DELETE FROM view_history WHERE user_id = #{userId} " +
            "AND update_time < #{beforeTime}")
    int deleteOldHistory(@Param("userId") Long userId,
                         @Param("beforeTime") LocalDateTime beforeTime);

    /**
     * 批量删除浏览历史
     */
    @Delete("<script>" +
            "DELETE FROM view_history WHERE user_id = #{userId} AND id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchDelete(@Param("userId") Long userId,
                    @Param("ids") List<Long> ids);

    /**
     * 清空用户的所有浏览历史
     */
    @Delete("DELETE FROM view_history WHERE user_id = #{userId}")
    int clearUserHistory(@Param("userId") Long userId);

    /**
     * 查询浏览历史详情（分页，带内容信息）
     * 建议在XML中实现，根据target_type关联不同的表
     */
    IPage<Map<String, Object>> selectHistoryDetails(Page<ViewHistory> page,
                                                    @Param("userId") Long userId,
                                                    @Param("targetType") String targetType);

    /**
     * 获取用户的观看进度（视频类）
     */
    @Select("SELECT vh.*, c.title, c.video_duration FROM view_history vh " +
            "INNER JOIN contents c ON vh.target_id = c.id " +
            "WHERE vh.user_id = #{userId} " +
            "AND vh.target_type = 'content' " +
            "AND vh.progress > 0 AND vh.progress < 100 " +
            "ORDER BY vh.update_time DESC")
    List<Map<String, Object>> findUnfinishedVideos(@Param("userId") Long userId);

    /**
     * 统计内容的浏览用户数
     */
    @Select("SELECT COUNT(DISTINCT user_id) FROM view_history " +
            "WHERE target_type = #{targetType} AND target_id = #{targetId}")
    int countUniqueViewers(@Param("targetType") String targetType,
                           @Param("targetId") Long targetId);

    /**
     * 插入或更新浏览记录
     */
    @Insert("INSERT INTO view_history(user_id, target_type, target_id, view_duration, progress) " +
            "VALUES(#{userId}, #{targetType}, #{targetId}, #{duration}, #{progress}) " +
            "ON DUPLICATE KEY UPDATE " +
            "view_duration = view_duration + VALUES(view_duration), " +
            "progress = VALUES(progress), " +
            "update_time = NOW()")
    int insertOrUpdate(@Param("userId") Long userId,
                       @Param("targetType") String targetType,
                       @Param("targetId") Long targetId,
                       @Param("duration") Integer duration,
                       @Param("progress") Integer progress);
}