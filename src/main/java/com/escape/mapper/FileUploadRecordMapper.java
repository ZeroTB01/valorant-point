package com.escape.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.escape.entity.FileUploadRecord;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

/**
 * 文件上传记录Mapper接口
 *
 * @author escape
 * @since 2025-06-15
 */
@Mapper
public interface FileUploadRecordMapper extends BaseMapper<FileUploadRecord> {

    /**
     * 根据文件URL查询记录
     */
    @Select("SELECT * FROM file_upload_records WHERE file_url = #{fileUrl} AND deleted = 0 LIMIT 1")
    FileUploadRecord findByFileUrl(@Param("fileUrl") String fileUrl);

    /**
     * 根据对象键更新状态
     */
    @Update("UPDATE file_upload_records SET status = #{status}, update_time = NOW() " +
            "WHERE object_key = #{objectKey} AND deleted = 0")
    int updateStatusByObjectKey(@Param("objectKey") String objectKey, @Param("status") Integer status);

    /**
     * 查询用户的文件列表
     */
    @Select("<script>" +
            "SELECT * FROM file_upload_records " +
            "WHERE user_id = #{userId} AND deleted = 0 AND status = 1 " +
            "<if test='fileType != null'>" +
            "AND file_type = #{fileType} " +
            "</if>" +
            "ORDER BY create_time DESC " +
            "LIMIT #{offset}, #{limit}" +
            "</script>")
    List<FileUploadRecord> findUserFiles(@Param("userId") Long userId,
                                         @Param("fileType") String fileType,
                                         @Param("offset") Integer offset,
                                         @Param("limit") Integer limit);

    /**
     * 统计用户各类型文件数量
     */
    @Select("SELECT file_type, COUNT(*) as count " +
            "FROM file_upload_records " +
            "WHERE user_id = #{userId} AND deleted = 0 AND status = 1 " +
            "GROUP BY file_type")
    List<Map<String, Object>> countByUserAndType(@Param("userId") Long userId);

    /**
     * 统计用户文件总大小
     */
    @Select("SELECT SUM(file_size) FROM file_upload_records " +
            "WHERE user_id = #{userId} AND deleted = 0 AND status = 1")
    Long sumFileSizeByUser(@Param("userId") Long userId);

    /**
     * 统计用户文件总数
     */
    @Select("SELECT COUNT(*) FROM file_upload_records " +
            "WHERE user_id = #{userId} AND deleted = 0 AND status = 1")
    Integer countByUser(@Param("userId") Long userId);

    /**
     * 查询用户最近的上传记录
     */
    @Select("SELECT * FROM file_upload_records " +
            "WHERE user_id = #{userId} AND deleted = 0 AND status = 1 " +
            "ORDER BY create_time DESC " +
            "LIMIT #{limit}")
    List<FileUploadRecord> findRecentUploads(@Param("userId") Long userId, @Param("limit") Integer limit);

    /**
     * 批量查询文件记录
     */
    @Select("<script>" +
            "SELECT * FROM file_upload_records " +
            "WHERE deleted = 0 AND file_url IN " +
            "<foreach collection='fileUrls' item='url' open='(' separator=',' close=')'>" +
            "#{url}" +
            "</foreach>" +
            "</script>")
    List<FileUploadRecord> findByFileUrls(@Param("fileUrls") List<String> fileUrls);

    /**
     * 统计用户文件信息
     * @param userId 用户ID
     * @return 统计结果
     */
    @Select("SELECT file_type, COUNT(*) as count, SUM(file_size) as total_size " +
            "FROM file_upload_records " +
            "WHERE user_id = #{userId} AND deleted = 0 " +
            "GROUP BY file_type")
    List<Map<String, Object>> statisticsByUserAndType(@Param("userId") Long userId);
}
