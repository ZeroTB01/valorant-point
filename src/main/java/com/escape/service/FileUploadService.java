package com.escape.service;

import com.escape.dto.FileUploadRecordDTO;
import com.escape.dto.StorageStats;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件上传服务接口
 * 定义文件上传相关的业务方法
 *
 * @author escape
 * @since 2025-06-02
 */
public interface FileUploadService {

    /**
     * 上传单个文件
     * @param file 文件
     * @param userId 用户ID
     * @param fileType 文件类型（image/video/document）
     * @return 文件访问URL
     */
    String uploadFile(MultipartFile file, Long userId, String fileType);

    /**
     * 批量上传文件
     * @param files 文件列表
     * @param userId 用户ID
     * @param fileType 文件类型
     * @return 文件访问URL列表
     */
    List<String> uploadFiles(List<MultipartFile> files, Long userId, String fileType);

    /**
     * 上传图片（带缩略图）
     * @param file 图片文件
     * @param userId 用户ID
     * @param generateThumbnail 是否生成缩略图
     * @return 图片上传结果
     */
    ImageUploadResult uploadImage(MultipartFile file, Long userId, boolean generateThumbnail);

    /**
     * 上传视频
     * @param file 视频文件
     * @param userId 用户ID
     * @return 视频上传结果
     */
    VideoUploadResult uploadVideo(MultipartFile file, Long userId);

    /**
     * 删除文件
     * @param fileUrl 文件URL
     * @param userId 用户ID
     * @return 是否删除成功
     */
    boolean deleteFile(String fileUrl, Long userId);

    /**
     * 批量删除文件
     * @param fileUrls 文件URL列表
     * @param userId 用户ID
     * @return 成功删除的数量
     */
    int deleteFiles(List<String> fileUrls, Long userId);

    /**
     * 获取文件上传记录
     * @param userId 用户ID
     * @param fileType 文件类型
     * @param page 页码
     * @param size 每页大小
     * @return 上传记录列表
     */
    List<FileUploadRecordDTO> getUploadRecords(Long userId, String fileType, int page, int size);

    /**
     * 获取文件临时访问URL（用于私有文件）
     * @param fileKey 文件Key
     * @param expireMinutes 过期时间（分钟）
     * @return 临时访问URL
     */
    String getTemporaryUrl(String fileKey, int expireMinutes);

    /**
     * 检查文件是否存在
     * @param fileUrl 文件URL
     * @return 是否存在
     */
    boolean checkFileExists(String fileUrl);

    /**
     * 获取用户存储使用情况
     * @param userId 用户ID
     * @return 存储统计信息
     */
    StorageStats getUserStorageStats(Long userId);

    // ==================== 结果类定义 ====================

    /**
     * 图片上传结果
     */
    class ImageUploadResult {
        private String originalUrl;
        private String thumbnailUrl;
        private Long fileSize;
        private Integer width;
        private Integer height;

        // getters and setters
        public String getOriginalUrl() { return originalUrl; }
        public void setOriginalUrl(String originalUrl) { this.originalUrl = originalUrl; }
        public String getThumbnailUrl() { return thumbnailUrl; }
        public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
        public Long getFileSize() { return fileSize; }
        public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
        public Integer getWidth() { return width; }
        public void setWidth(Integer width) { this.width = width; }
        public Integer getHeight() { return height; }
        public void setHeight(Integer height) { this.height = height; }
    }

    /**
     * 视频上传结果
     */
    class VideoUploadResult {
        private String videoUrl;
        private String coverUrl;
        private Long fileSize;
        private Integer duration; // 视频时长（秒）
        private String resolution; // 分辨率

        // getters and setters
        public String getVideoUrl() { return videoUrl; }
        public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
        public String getCoverUrl() { return coverUrl; }
        public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
        public Long getFileSize() { return fileSize; }
        public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
        public Integer getDuration() { return duration; }
        public void setDuration(Integer duration) { this.duration = duration; }
        public String getResolution() { return resolution; }
        public void setResolution(String resolution) { this.resolution = resolution; }
    }
}