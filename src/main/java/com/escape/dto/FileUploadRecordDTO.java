package com.escape.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 文件上传记录DTO
 *
 * @author escape
 * @since 2025-06-25
 */
@Data
public class FileUploadRecordDTO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 原始文件名
     */
    private String fileName;

    /**
     * 文件URL
     */
    private String fileUrl;

    /**
     * 文件类型：image/video/document
     */
    private String fileType;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 文件大小（格式化显示）
     */
    private String fileSizeFormatted;

    /**
     * OSS对象键
     */
    private String objectKey;

    /**
     * 文件MIME类型
     */
    private String mimeType;

    /**
     * 状态：0-已删除，1-正常
     */
    private Integer status;

    /**
     * 状态描述
     */
    private String statusText;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 是否可以访问
     */
    private Boolean accessible;

    /**
     * 临时访问URL（如果需要）
     */
    private String temporaryUrl;

    /**
     * 文件扩展名
     */
    private String fileExtension;

    /**
     * 上传天数
     */
    private Long daysSinceUpload;

    /**
     * 格式化的上传时间
     */
    private String uploadTime;

    /**
     * 格式化文件大小
     */
    public String getFileSizeFormatted() {
        if (fileSize == null) {
            return "0 B";
        }

        double size = fileSize.doubleValue();
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;

        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }

        return String.format("%.2f %s", size, units[unitIndex]);
    }

    /**
     * 获取状态文本
     */
    public String getStatusText() {
        if (status == null) {
            return "未知";
        }

        return switch (status) {
            case 0 -> "已删除";
            case 1 -> "正常";
            default -> "未知状态";
        };
    }

    /**
     * 获取文件扩展名
     */
    public String getFileExtension() {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * 计算上传天数
     */
    public Long getDaysSinceUpload() {
        if (createTime == null) {
            return 0L;
        }

        return java.time.Duration.between(createTime, LocalDateTime.now()).toDays();
    }

    /**
     * 判断是否为图片
     */
    public boolean isImage() {
        return "image".equals(fileType);
    }

    /**
     * 判断是否为视频
     */
    public boolean isVideo() {
        return "video".equals(fileType);
    }

    /**
     * 判断是否为文档
     */
    public boolean isDocument() {
        return "document".equals(fileType);
    }
}