package com.escape.controller;

import com.escape.common.Result;
import com.escape.dto.FileUploadRecordDTO;
import com.escape.dto.StorageStats;
import com.escape.entity.FileUploadRecord;
import com.escape.service.FileUploadService;
import com.escape.service.FileUploadService.ImageUploadResult;
import com.escape.service.FileUploadService.VideoUploadResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件上传控制器
 *
 * @author escape
 * @since 2025-06-02
 */
@Slf4j
@RestController
@RequestMapping("/file")
public class FileUploadController {

    @Autowired
    private FileUploadService fileUploadService;

    /**
     * 上传单个文件
     */
    @PostMapping("/upload")
    public Result<String> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "document") String fileType,
            Authentication authentication) {

        try {
            Long userId = getUserId(authentication);
            String fileUrl = fileUploadService.uploadFile(file, userId, fileType);
            log.info("文件上传成功: userId={}, fileName={}, fileUrl={}",
                    userId, file.getOriginalFilename(), fileUrl);
            return Result.success(fileUrl);
        } catch (Exception e) {
            log.error("文件上传失败: {}", e.getMessage(), e);
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 批量上传文件
     */
    @PostMapping("/upload-batch")
    public Result<List<String>> uploadFiles(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(defaultValue = "document") String fileType,
            Authentication authentication) {

        try {
            Long userId = getUserId(authentication);
            List<String> fileUrls = fileUploadService.uploadFiles(files, userId, fileType);
            log.info("批量文件上传成功: userId={}, count={}", userId, fileUrls.size());
            return Result.success(fileUrls);
        } catch (Exception e) {
            log.error("批量文件上传失败: {}", e.getMessage(), e);
            return Result.error("批量文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 上传图片
     */
    @PostMapping("/upload-image")
    public Result<ImageUploadResult> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "true") boolean generateThumbnail,
            Authentication authentication) {

        try {
            Long userId = getUserId(authentication);
            ImageUploadResult result = fileUploadService.uploadImage(file, userId, generateThumbnail);
            log.info("图片上传成功: userId={}, fileName={}", userId, file.getOriginalFilename());
            return Result.success(result);
        } catch (Exception e) {
            log.error("图片上传失败: {}", e.getMessage(), e);
            return Result.error("图片上传失败: " + e.getMessage());
        }
    }

    /**
     * 上传视频
     */
    @PostMapping("/upload-video")
    public Result<VideoUploadResult> uploadVideo(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        try {
            Long userId = getUserId(authentication);
            VideoUploadResult result = fileUploadService.uploadVideo(file, userId);
            log.info("视频上传成功: userId={}, fileName={}", userId, file.getOriginalFilename());
            return Result.success(result);
        } catch (Exception e) {
            log.error("视频上传失败: {}", e.getMessage(), e);
            return Result.error("视频上传失败: " + e.getMessage());
        }
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/delete")
    public Result<Void> deleteFile(
            @RequestParam String fileUrl,
            Authentication authentication) {

        try {
            Long userId = getUserId(authentication);
            boolean success = fileUploadService.deleteFile(fileUrl, userId);
            if (success) {
                log.info("文件删除成功: userId={}, fileUrl={}", userId, fileUrl);
                return Result.success();
            } else {
                log.warn("文件删除失败: userId={}, fileUrl={}", userId, fileUrl);
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            log.error("文件删除异常: {}", e.getMessage(), e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除文件
     */
    @DeleteMapping("/delete-batch")
    public Result<Integer> deleteFiles(
            @RequestBody List<String> fileUrls,
            Authentication authentication) {

        try {
            Long userId = getUserId(authentication);
            int count = fileUploadService.deleteFiles(fileUrls, userId);
            log.info("批量文件删除成功: userId={}, count={}", userId, count);
            return Result.success(count);
        } catch (Exception e) {
            log.error("批量文件删除失败: {}", e.getMessage(), e);
            return Result.error("批量删除失败: " + e.getMessage());
        }
    }

    /**
     * 获取上传记录
     */
    @GetMapping("/records")
    public Result<List<FileUploadRecordDTO>> getUploadRecords(
            @RequestParam(required = false) String fileType,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {

        try {
            Long userId = getUserId(authentication);
            List<FileUploadRecordDTO> records = fileUploadService.getUploadRecords(userId, fileType, page, size);
            return Result.success(records);
        } catch (Exception e) {
            log.error("获取上传记录失败: {}", e.getMessage(), e);
            return Result.error("获取上传记录失败: " + e.getMessage());
        }
    }

    /**
     * 获取临时访问URL
     */
    @GetMapping("/temp-url")
    public Result<String> getTemporaryUrl(
            @RequestParam String fileKey,
            @RequestParam(defaultValue = "60") int expireMinutes) {

        try {
            String tempUrl = fileUploadService.getTemporaryUrl(fileKey, expireMinutes);
            return Result.success(tempUrl);
        } catch (Exception e) {
            log.error("获取临时URL失败: {}", e.getMessage(), e);
            return Result.error("获取临时URL失败: " + e.getMessage());
        }
    }

    /**
     * 检查文件是否存在
     */
    @GetMapping("/exists")
    public Result<Boolean> checkFileExists(@RequestParam String fileUrl) {
        try {
            boolean exists = fileUploadService.checkFileExists(fileUrl);
            return Result.success(exists);
        } catch (Exception e) {
            log.error("检查文件存在失败: {}", e.getMessage(), e);
            return Result.error("检查文件存在失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户存储统计
     */
    @GetMapping("/storage-stats")
    public Result<StorageStats> getUserStorageStats(Authentication authentication) {
        try {
            Long userId = getUserId(authentication);
            StorageStats stats = fileUploadService.getUserStorageStats(userId);
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取存储统计失败: {}", e.getMessage(), e);
            return Result.error("获取存储统计失败: " + e.getMessage());
        }
    }

    /**
     * 文件上传健康检查
     */
    @GetMapping("/health")
    public Result<String> healthCheck() {
        try {
            // 简单的健康检查
            return Result.success("File upload service is healthy");
        } catch (Exception e) {
            log.error("文件上传服务健康检查失败: {}", e.getMessage(), e);
            return Result.error("File upload service unhealthy: " + e.getMessage());
        }
    }

    /**
     * 获取用户ID
     */
    private Long getUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            // 游客用户
            log.debug("游客用户访问文件上传服务");
            return -1L;
        }

        // 从认证信息中获取用户ID
        Object principal = authentication.getPrincipal();
        if (principal instanceof Long) {
            return (Long) principal;
        }

        // 如果存储的是用户ID字符串
        if (principal instanceof String) {
            try {
                return Long.parseLong(principal.toString());
            } catch (NumberFormatException e) {
                log.warn("无法解析用户ID: {}", principal);
                return -1L;
            }
        }

        // 如果是自定义用户对象，可能需要调用 getId() 方法
        // 这里需要根据您的 UserDetails 实现来调整
        log.warn("未知的认证主体类型: {}", principal.getClass());
        return -1L;
    }
}