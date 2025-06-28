package com.escape.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.escape.common.exception.BusinessException;
import com.escape.common.ResultCode;
import com.escape.dto.FileUploadRecordDTO;
import com.escape.dto.StorageStats;
import com.escape.entity.FileUploadRecord;
import com.escape.mapper.FileUploadRecordMapper;
import com.escape.service.FileUploadService;
import com.escape.service.FileUploadService.ImageUploadResult;
import com.escape.service.FileUploadService.VideoUploadResult;
import com.escape.storage.FileStorageManager;
import com.escape.storage.FileStorageStrategy;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 文件上传服务实现类
 * 支持多种存储策略（本地、OSS等）
 *
 * @author escape
 * @since 2025-06-02
 */
@Slf4j
@Service
public class FileUploadServiceImpl implements FileUploadService {

    @Autowired
    private FileStorageManager storageManager;

    @Autowired
    private FileUploadRecordMapper fileUploadRecordMapper;

    // 文件大小限制
    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final long MAX_VIDEO_SIZE = 100 * 1024 * 1024; // 100MB
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB

    // 允许的文件类型
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final Set<String> ALLOWED_VIDEO_TYPES = Set.of("mp4", "avi", "mov", "wmv", "flv");
    private static final Set<String> ALLOWED_DOCUMENT_TYPES = Set.of("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String uploadFile(MultipartFile file, Long userId, String fileType) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "文件不能为空");
        }

        // 检查文件大小
        checkFileSize(file, fileType);

        // 检查文件类型
        String fileExtension = getFileExtension(file.getOriginalFilename());
        checkFileType(fileExtension, fileType);

        try {
            // 生成文件名
            String fileName = generateFileName(fileExtension, fileType);

            // 获取存储策略
            FileStorageStrategy strategy = storageManager.getStrategy();

            // 准备元数据
            Map<String, String> metadata = new HashMap<>();
            metadata.put("userId", String.valueOf(userId));
            metadata.put("originalName", file.getOriginalFilename());
            metadata.put("fileType", fileType);

            // 上传文件
            String fileUrl = strategy.upload(
                    file.getInputStream(),
                    fileName,
                    file.getContentType(),
                    metadata
            );

            // 保存上传记录
            saveUploadRecord(userId, file.getOriginalFilename(), fileUrl,
                    fileType, file.getSize(), fileName, file.getContentType());

            log.info("文件上传成功: userId={}, fileName={}, url={}, storage={}",
                    userId, file.getOriginalFilename(), fileUrl, strategy.getStorageType());
            return fileUrl;

        } catch (IOException e) {
            log.error("文件上传失败: {}", e.getMessage());
            throw new BusinessException(ResultCode.FILE_UPLOAD_FAILED);
        }
    }

    @Override
    public List<String> uploadFiles(List<MultipartFile> files, Long userId, String fileType) {
        if (files == null || files.isEmpty()) {
            return Collections.emptyList();
        }

        return files.stream()
                .map(file -> uploadFile(file, userId, fileType))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImageUploadResult uploadImage(MultipartFile file, Long userId, boolean generateThumbnail) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "图片不能为空");
        }

        // 检查文件大小和类型
        checkFileSize(file, "image");
        String fileExtension = getFileExtension(file.getOriginalFilename());
        checkFileType(fileExtension, "image");

        try {
            ImageUploadResult result = new ImageUploadResult();

            // 对于WebP格式，跳过BufferedImage验证，直接上传
            if ("webp".equalsIgnoreCase(fileExtension)) {
                log.info("检测到WebP格式文件，跳过图片内容验证: {}", file.getOriginalFilename());

                // 设置默认尺寸（WebP暂时无法通过ImageIO获取准确尺寸）
                result.setWidth(0);
                result.setHeight(0);
                result.setFileSize(file.getSize());

                // 生成文件名
                String fileName = generateFileName(fileExtension, "image");

                // 获取存储策略
                FileStorageStrategy strategy = storageManager.getStrategy();

                // 准备元数据
                Map<String, String> metadata = new HashMap<>();
                metadata.put("userId", String.valueOf(userId));
                metadata.put("originalName", file.getOriginalFilename());
                metadata.put("fileType", "image");
                metadata.put("isWebP", "true");

                // 直接上传WebP文件
                String originalUrl = strategy.upload(
                        file.getInputStream(),
                        fileName,
                        file.getContentType(),
                        metadata
                );
                result.setOriginalUrl(originalUrl);
                result.setThumbnailUrl(originalUrl); // WebP暂时不生成缩略图，直接使用原图

                // 保存上传记录
                saveUploadRecord(userId, file.getOriginalFilename(), originalUrl,
                        "image", file.getSize(), fileName, file.getContentType());

                log.info("WebP图片上传成功: userId={}, fileName={}, originalUrl={}",
                        userId, file.getOriginalFilename(), originalUrl);

                return result;
            }

            // 其他格式的图片处理（保持原有逻辑）
            BufferedImage originalImage = ImageIO.read(file.getInputStream());
            if (originalImage == null) {
                throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "无效的图片文件");
            }

            // 设置图片尺寸
            result.setWidth(originalImage.getWidth());
            result.setHeight(originalImage.getHeight());
            result.setFileSize(file.getSize());

            // 生成文件名
            String fileName = generateFileName(fileExtension, "image");

            // 获取存储策略
            FileStorageStrategy strategy = storageManager.getStrategy();

            // 准备元数据
            Map<String, String> metadata = new HashMap<>();
            metadata.put("userId", String.valueOf(userId));
            metadata.put("width", String.valueOf(originalImage.getWidth()));
            metadata.put("height", String.valueOf(originalImage.getHeight()));

            // 重新读取文件流（因为前面读取过了）
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(originalImage, fileExtension, baos);
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

            // 上传原图
            String originalUrl = strategy.upload(
                    bais,
                    fileName,
                    file.getContentType(),
                    metadata
            );
            result.setOriginalUrl(originalUrl);

            // 生成缩略图
            if (generateThumbnail) {
                String thumbnailFileName = "thumb_" + fileName;
                ByteArrayOutputStream thumbnailStream = new ByteArrayOutputStream();

                // 使用Thumbnailator生成缩略图
                Thumbnails.of(originalImage)
                        .size(300, 300)
                        .keepAspectRatio(true)
                        .outputFormat(fileExtension)
                        .toOutputStream(thumbnailStream);

                // 上传缩略图
                metadata.put("type", "thumbnail");
                String thumbnailUrl = strategy.upload(
                        new ByteArrayInputStream(thumbnailStream.toByteArray()),
                        thumbnailFileName,
                        file.getContentType(),
                        metadata
                );
                result.setThumbnailUrl(thumbnailUrl);
            }

            // 保存上传记录
            saveUploadRecord(userId, file.getOriginalFilename(), result.getOriginalUrl(),
                    "image", file.getSize(), fileName, file.getContentType());

            log.info("图片上传成功: userId={}, fileName={}, originalUrl={}",
                    userId, file.getOriginalFilename(), result.getOriginalUrl());

            return result;

        } catch (IOException e) {
            log.error("图片上传失败: {}", e.getMessage());
            throw new BusinessException(ResultCode.FILE_UPLOAD_FAILED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VideoUploadResult uploadVideo(MultipartFile file, Long userId) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "视频不能为空");
        }

        // 检查文件大小和类型
        checkFileSize(file, "video");
        String fileExtension = getFileExtension(file.getOriginalFilename());
        checkFileType(fileExtension, "video");

        try {
            VideoUploadResult result = new VideoUploadResult();
            result.setFileSize(file.getSize());

            // 生成文件名
            String fileName = generateFileName(fileExtension, "video");

            // 获取存储策略
            FileStorageStrategy strategy = storageManager.getStrategy();

            // 准备元数据
            Map<String, String> metadata = new HashMap<>();
            metadata.put("userId", String.valueOf(userId));
            metadata.put("type", "video");

            // 上传视频
            String videoUrl = strategy.upload(
                    file.getInputStream(),
                    fileName,
                    file.getContentType(),
                    metadata
            );
            result.setVideoUrl(videoUrl);

            // TODO: 实际项目中应该使用视频处理服务生成封面图
            // 这里暂时返回默认封面
            result.setCoverUrl("/static/default-video-cover.jpg");

            // 保存上传记录
            saveUploadRecord(userId, file.getOriginalFilename(), videoUrl,
                    "video", file.getSize(), fileName, file.getContentType());

            log.info("视频上传成功: userId={}, fileName={}, url={}",
                    userId, file.getOriginalFilename(), videoUrl);

            return result;

        } catch (IOException e) {
            log.error("视频上传失败: {}", e.getMessage());
            throw new BusinessException(ResultCode.FILE_UPLOAD_FAILED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteFile(String fileUrl, Long userId) {
        if (!StringUtils.hasText(fileUrl)) {
            return false;
        }

        try {
            // 检查文件是否属于该用户
            QueryWrapper<FileUploadRecord> query = new QueryWrapper<>();
            query.eq("user_id", userId)
                    .eq("file_url", fileUrl)
                    .eq("deleted", 0);
            FileUploadRecord record = fileUploadRecordMapper.selectOne(query);

            if (record == null) {
                throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权删除该文件");
            }

            // 获取存储策略
            FileStorageStrategy strategy = storageManager.getStrategy();

            // 删除文件
            String fileName = record.getObjectKey();
            boolean deleted = strategy.delete(fileName);

            if (deleted) {
                // 软删除数据库记录
                record.setDeleted(1);
                record.setStatus(0);
                fileUploadRecordMapper.updateById(record);

                log.info("文件删除成功: userId={}, fileUrl={}", userId, fileUrl);
            }

            return deleted;

        } catch (Exception e) {
            log.error("文件删除失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteFiles(List<String> fileUrls, Long userId) {
        if (fileUrls == null || fileUrls.isEmpty()) {
            return 0;
        }

        int successCount = 0;
        for (String fileUrl : fileUrls) {
            if (deleteFile(fileUrl, userId)) {
                successCount++;
            }
        }

        return successCount;
    }

    @Override
    public List<FileUploadRecordDTO> getUploadRecords(Long userId, String fileType, int page, int size) {
        Page<FileUploadRecord> pageParam = new Page<>(page, size);
        QueryWrapper<FileUploadRecord> query = new QueryWrapper<>();
        query.eq("user_id", userId)
                .eq("deleted", 0)
                .eq("status", 1);

        if (StringUtils.hasText(fileType)) {
            query.eq("file_type", fileType);
        }

        query.orderByDesc("create_time");

        Page<FileUploadRecord> resultPage = fileUploadRecordMapper.selectPage(pageParam, query);

        // 转换为DTO响应格式
        return resultPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public String getTemporaryUrl(String fileKey, int expireMinutes) {
        FileStorageStrategy strategy = storageManager.getStrategy();
        return strategy.getTemporaryUrl(fileKey, expireMinutes);
    }

    @Override
    public boolean checkFileExists(String fileUrl) {
        if (!StringUtils.hasText(fileUrl)) {
            return false;
        }

        try {
            // 先从数据库检查
            QueryWrapper<FileUploadRecord> query = new QueryWrapper<>();
            query.eq("file_url", fileUrl)
                    .eq("deleted", 0)
                    .eq("status", 1);
            Long count = fileUploadRecordMapper.selectCount(query);

            if (count == 0) {
                return false;
            }

            // 再从存储系统检查
            FileUploadRecord record = fileUploadRecordMapper.selectOne(query);
            if (record == null) {
                return false;
            }

            FileStorageStrategy strategy = storageManager.getStrategy();
            return strategy.exists(record.getObjectKey());

        } catch (Exception e) {
            log.error("检查文件存在失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public StorageStats getUserStorageStats(Long userId) {
        StorageStats stats = new StorageStats();
        stats.setUserId(userId);

        // 查询用户文件统计
        List<Map<String, Object>> typeStats = fileUploadRecordMapper.statisticsByUserAndType(userId);

        long totalSize = 0;
        int totalCount = 0;
        int imageCount = 0;
        int videoCount = 0;
        int documentCount = 0;

        for (Map<String, Object> typeStat : typeStats) {
            String fileType = (String) typeStat.get("file_type");
            Long count = (Long) typeStat.get("count");
            Long size = (Long) typeStat.get("total_size");

            if (count != null && size != null) {
                totalCount += count.intValue();
                totalSize += size;

                // 添加文件类型统计
                stats.addFileTypeStat(fileType, count.intValue(), size);

                // 分类统计
                switch (fileType) {
                    case "image" -> imageCount = count.intValue();
                    case "video" -> videoCount = count.intValue();
                    case "document" -> documentCount = count.intValue();
                }
            }
        }

        stats.setUsedSize(totalSize);
        stats.setTotalSize(10L * 1024 * 1024 * 1024); // 10GB限制
        stats.setFileCount(totalCount);
        stats.setImageCount(imageCount);
        stats.setVideoCount(videoCount);
        stats.setDocumentCount(documentCount);

        return stats;
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 保存上传记录
     */
    private void saveUploadRecord(Long userId, String originalName, String fileUrl,
                                  String fileType, Long fileSize, String objectKey, String mimeType) {
        FileUploadRecord record = new FileUploadRecord();
        record.setUserId(userId);
        record.setFileName(originalName);
        record.setFileUrl(fileUrl);
        record.setFileType(fileType);
        record.setFileSize(fileSize);
        record.setObjectKey(objectKey);
        record.setMimeType(mimeType);
        record.setStatus(1); // 正常状态
        // deleted、createTime、updateTime 会通过 MyBatis Plus 自动填充

        fileUploadRecordMapper.insert(record);
    }

    /**
     * 检查文件大小
     */
    private void checkFileSize(MultipartFile file, String fileType) {
        long maxSize = switch (fileType) {
            case "image" -> MAX_IMAGE_SIZE;
            case "video" -> MAX_VIDEO_SIZE;
            default -> MAX_FILE_SIZE;
        };

        if (file.getSize() > maxSize) {
            throw new BusinessException(ResultCode.FILE_SIZE_EXCEEDED);
        }
    }

    /**
     * 检查文件类型
     */
    private void checkFileType(String extension, String fileType) {
        Set<String> allowedTypes = switch (fileType) {
            case "image" -> ALLOWED_IMAGE_TYPES;
            case "video" -> ALLOWED_VIDEO_TYPES;
            case "document" -> ALLOWED_DOCUMENT_TYPES;
            default -> throw new BusinessException(ResultCode.FILE_TYPE_NOT_ALLOWED);
        };

        if (!allowedTypes.contains(extension.toLowerCase())) {
            throw new BusinessException(ResultCode.FILE_TYPE_NOT_ALLOWED);
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        if (!StringUtils.hasText(fileName) || !fileName.contains(".")) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "文件名格式错误");
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    /**
     * 生成文件名
     */
    private String generateFileName(String extension, String fileType) {
        LocalDateTime now = LocalDateTime.now();
        String dateStr = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String timeStr = now.format(DateTimeFormatter.ofPattern("HHmmssSSS"));
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);

        return String.format("%s/%s/%s_%s.%s", fileType, dateStr, timeStr, uuid, extension);
    }

    /**
     * 转换 FileUploadRecord 为 DTO
     */
    private FileUploadRecordDTO convertToDTO(FileUploadRecord record) {
        if (record == null) {
            return null;
        }

        FileUploadRecordDTO dto = new FileUploadRecordDTO();
        dto.setId(record.getId());
        dto.setUserId(record.getUserId());
        dto.setFileName(record.getFileName());
        dto.setFileUrl(record.getFileUrl());
        dto.setFileType(record.getFileType());
        dto.setFileSize(record.getFileSize());
        dto.setObjectKey(record.getObjectKey());
        dto.setMimeType(record.getMimeType());
        dto.setStatus(record.getStatus());
        dto.setCreateTime(record.getCreateTime());
        dto.setUpdateTime(record.getUpdateTime());

        // 设置格式化的上传时间
        if (record.getCreateTime() != null) {
            dto.setUploadTime(record.getCreateTime().format(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }

        // 设置计算属性
        dto.setAccessible(record.getStatus() == 1);

        // 如果需要临时URL（私有文件）
        if (record.getObjectKey() != null) {
            try {
                FileStorageStrategy strategy = storageManager.getStrategy();
                dto.setTemporaryUrl(strategy.getTemporaryUrl(record.getObjectKey(), 60));
            } catch (Exception e) {
                log.warn("生成临时URL失败: {}", e.getMessage());
            }
        }

        return dto;
    }
}