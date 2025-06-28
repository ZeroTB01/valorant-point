package com.escape.dto;

import lombok.Data;
import java.util.HashMap;
import java.util.Map;

/**
 * 存储统计信息DTO
 *
 * @author escape
 * @since 2025-06-25
 */
@Data
public class StorageStats {

    /**
     * 已使用的存储空间（字节）
     */
    private Long usedSize;

    /**
     * 总存储空间限制（字节）
     */
    private Long totalSize;

    /**
     * 文件总数
     */
    private Integer fileCount;

    /**
     * 图片文件数量
     */
    private Integer imageCount;

    /**
     * 视频文件数量
     */
    private Integer videoCount;

    /**
     * 文档文件数量
     */
    private Integer documentCount;

    /**
     * 各类型文件统计详情
     */
    private Map<String, FileTypeStats> fileTypeStats;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 统计时间戳
     */
    private Long timestamp;

    public StorageStats() {
        this.fileTypeStats = new HashMap<>();
        this.timestamp = System.currentTimeMillis();
        // 默认值
        this.usedSize = 0L;
        this.totalSize = 10L * 1024 * 1024 * 1024; // 10GB 默认限制
        this.fileCount = 0;
        this.imageCount = 0;
        this.videoCount = 0;
        this.documentCount = 0;
    }

    /**
     * 获取使用率百分比
     */
    public Double getUsagePercentage() {
        if (totalSize == null || totalSize == 0) {
            return 0.0;
        }
        return (usedSize.doubleValue() / totalSize.doubleValue()) * 100;
    }

    /**
     * 获取剩余空间（字节）
     */
    public Long getRemainingSize() {
        return totalSize - usedSize;
    }

    /**
     * 获取格式化的已使用空间
     */
    public String getUsedSizeFormatted() {
        return formatFileSize(usedSize);
    }

    /**
     * 获取格式化的总空间
     */
    public String getTotalSizeFormatted() {
        return formatFileSize(totalSize);
    }

    /**
     * 获取格式化的剩余空间
     */
    public String getRemainingSizeFormatted() {
        return formatFileSize(getRemainingSize());
    }

    /**
     * 检查是否接近存储限制
     */
    public Boolean isNearLimit() {
        return getUsagePercentage() > 80.0;
    }

    /**
     * 检查是否达到存储限制
     */
    public Boolean isOverLimit() {
        return getUsagePercentage() >= 100.0;
    }

    /**
     * 添加文件类型统计
     */
    public void addFileTypeStat(String fileType, Integer count, Long size) {
        FileTypeStats stats = new FileTypeStats();
        stats.setFileType(fileType);
        stats.setCount(count);
        stats.setTotalSize(size);
        this.fileTypeStats.put(fileType, stats);
    }

    /**
     * 格式化文件大小
     */
    private String formatFileSize(Long size) {
        if (size == null || size == 0) {
            return "0 B";
        }

        double fileSize = size.doubleValue();
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;

        while (fileSize >= 1024 && unitIndex < units.length - 1) {
            fileSize /= 1024;
            unitIndex++;
        }

        return String.format("%.2f %s", fileSize, units[unitIndex]);
    }

    /**
     * 文件类型统计内部类
     */
    @Data
    public static class FileTypeStats {

        /**
         * 文件类型
         */
        private String fileType;

        /**
         * 文件数量
         */
        private Integer count;

        /**
         * 总大小（字节）
         */
        private Long totalSize;

        /**
         * 获取格式化大小
         */
        public String getTotalSizeFormatted() {
            return formatFileSize(totalSize);
        }

        /**
         * 获取平均文件大小
         */
        public Long getAverageSize() {
            if (count == null || count == 0) {
                return 0L;
            }
            return totalSize / count;
        }

        /**
         * 获取格式化的平均文件大小
         */
        public String getAverageSizeFormatted() {
            return formatFileSize(getAverageSize());
        }

        /**
         * 格式化文件大小
         */
        private String formatFileSize(Long size) {
            if (size == null || size == 0) {
                return "0 B";
            }

            double fileSize = size.doubleValue();
            String[] units = {"B", "KB", "MB", "GB", "TB"};
            int unitIndex = 0;

            while (fileSize >= 1024 && unitIndex < units.length - 1) {
                fileSize /= 1024;
                unitIndex++;
            }

            return String.format("%.2f %s", fileSize, units[unitIndex]);
        }
    }
}