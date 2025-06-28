package com.escape.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.escape.entity.Favorite;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

/**
 * 收藏数据访问层
 *
 * @author escape
 * @since 2025-06-11
 */
@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {

    /**
     * 检查是否已收藏
     */
    @Select("SELECT COUNT(*) FROM favorites " +
            "WHERE user_id = #{userId} AND target_type = #{targetType} " +
            "AND target_id = #{targetId}")
    boolean existsByUserAndTarget(@Param("userId") Long userId,
                                  @Param("targetType") String targetType,
                                  @Param("targetId") Long targetId);

    /**
     * 查询用户的收藏列表（按类型）
     */
    @Select("SELECT * FROM favorites WHERE user_id = #{userId} " +
            "AND target_type = #{targetType} " +
            "ORDER BY create_time DESC")
    List<Favorite> findByUserAndType(@Param("userId") Long userId,
                                     @Param("targetType") String targetType);

    /**
     * 查询用户的收藏夹列表
     */
    @Select("SELECT DISTINCT folder_name FROM favorites " +
            "WHERE user_id = #{userId} ORDER BY folder_name")
    List<String> findUserFolders(@Param("userId") Long userId);

    /**
     * 查询指定收藏夹的内容
     */
    @Select("SELECT * FROM favorites WHERE user_id = #{userId} " +
            "AND folder_name = #{folderName} " +
            "ORDER BY create_time DESC")
    List<Favorite> findByUserAndFolder(@Param("userId") Long userId,
                                       @Param("folderName") String folderName);

    /**
     * 统计用户各类型收藏数量
     */
    @Select("SELECT target_type, COUNT(*) as count FROM favorites " +
            "WHERE user_id = #{userId} GROUP BY target_type")
    List<Map<String, Object>> countByUserAndType(@Param("userId") Long userId);

    /**
     * 统计各收藏夹的数量
     */
    @Select("SELECT folder_name, COUNT(*) as count FROM favorites " +
            "WHERE user_id = #{userId} GROUP BY folder_name")
    List<Map<String, Object>> countByUserAndFolder(@Param("userId") Long userId);

    /**
     * 移动收藏到其他收藏夹
     */
    @Update("UPDATE favorites SET folder_name = #{newFolder} " +
            "WHERE id = #{id} AND user_id = #{userId}")
    int moveToFolder(@Param("id") Long id,
                     @Param("userId") Long userId,
                     @Param("newFolder") String newFolder);

    /**
     * 批量删除收藏
     */
    @Delete("<script>" +
            "DELETE FROM favorites WHERE user_id = #{userId} AND id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchDelete(@Param("userId") Long userId,
                    @Param("ids") List<Long> ids);

    /**
     * 查询收藏的内容详情（分页）
     * 建议在XML中实现，根据target_type关联不同的表
     */
    IPage<Map<String, Object>> selectFavoriteDetails(Page<Favorite> page,
                                                     @Param("userId") Long userId,
                                                     @Param("targetType") String targetType,
                                                     @Param("folderName") String folderName);

    /**
     * 查询内容被收藏次数
     */
    @Select("SELECT COUNT(*) FROM favorites " +
            "WHERE target_type = #{targetType} AND target_id = #{targetId}")
    int countByTarget(@Param("targetType") String targetType,
                      @Param("targetId") Long targetId);

    /**
     * 查询热门收藏内容
     */
    @Select("SELECT target_type, target_id, COUNT(*) as collect_count " +
            "FROM favorites " +
            "WHERE target_type = #{targetType} " +
            "GROUP BY target_type, target_id " +
            "ORDER BY collect_count DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> findHotTargets(@Param("targetType") String targetType,
                                             @Param("limit") Integer limit);

    /**
     * 获取用户最近收藏
     */
    @Select("SELECT * FROM favorites WHERE user_id = #{userId} " +
            "ORDER BY create_time DESC LIMIT #{limit}")
    List<Favorite> findRecentFavorites(@Param("userId") Long userId,
                                       @Param("limit") Integer limit);
}