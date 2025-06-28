package com.escape.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 文件工具类
 * 用于文件上传、下载等操作
 *
 * @author escape
 * @since 2025-06-02
 */
@Slf4j
public class FileUtils {

    // 允许的图片格式
    private static final List<String> ALLOWED_IMAGE_EXTENSIONS =
            Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "webp");

    // 允许的视频格式
    private static final List<String> ALLOWED_VIDEO_EXTENSIONS =
            Arrays.asList("mp4", "avi", "mov", "wmv", "flv", "mkv");

    /**
     * 获取文件扩展名
     */
    public static String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf('.') == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }

    /**
     * 生成唯一文件名
     */
    public static String generateFileName(String originalFileName) {
        String extension = getFileExtension(originalFileName);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 8);
        return timestamp + "_" + uuid + (extension.isEmpty() ? "" : "." + extension);
    }

    /**
     * 检查是否为图片文件
     */
    public static boolean isImageFile(String fileName) {
        String extension = getFileExtension(fileName);
        return ALLOWED_IMAGE_EXTENSIONS.contains(extension);
    }

    /**
     * 检查是否为视频文件
     */
    public static boolean isVideoFile(String fileName) {
        String extension = getFileExtension(fileName);
        return ALLOWED_VIDEO_EXTENSIONS.contains(extension);
    }

    /**
     * 检查文件大小是否合法
     */
    public static boolean isFileSizeValid(MultipartFile file, long maxSizeInBytes) {
        return file.getSize() <= maxSizeInBytes;
    }

    /**
     * 创建目录（如果不存在）
     */
    public static boolean createDirectoryIfNotExists(String path) {
        File directory = new File(path);
        if (!directory.exists()) {
            return directory.mkdirs();
        }
        return true;
    }

    /**
     * 保存上传的文件
     */
    public static String saveUploadedFile(MultipartFile file, String uploadPath) {
        try {
            // 创建上传目录
            createDirectoryIfNotExists(uploadPath);

            // 生成新文件名
            String newFileName = generateFileName(file.getOriginalFilename());
            String filePath = uploadPath + File.separator + newFileName;

            // 保存文件
            File destFile = new File(filePath);
            file.transferTo(destFile);

            log.info("文件上传成功: {}", filePath);
            return newFileName;
        } catch (IOException e) {
            log.error("文件上传失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 删除文件
     */
    public static boolean deleteFile(String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                boolean deleted = file.delete();
                if (deleted) {
                    log.info("文件删除成功: {}", filePath);
                } else {
                    log.warn("文件删除失败: {}", filePath);
                }
                return deleted;
            }
            return true;
        } catch (Exception e) {
            log.error("文件删除异常: {}, 错误: {}", filePath, e.getMessage());
            return false;
        }
    }

    /**
     * 格式化文件大小
     */
    public static String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.1f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", size / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", size / (1024.0 * 1024.0 * 1024.0));
        }
    }
}