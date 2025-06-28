package com.escape.storage.impl;

import com.escape.storage.FileStorageStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.beans.factory.InitializingBean; // ✅ 使用 Spring 接口替代 @PostConstruct

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Map;

/**
 * 本地文件存储策略实现
 * 用于开发环境的文件存储
 *
 * @author escape
 * @since 2025-06-02
 */
@Slf4j
@Component("localFileStorage")
public class LocalFileStorageStrategy implements FileStorageStrategy, InitializingBean {

    @Value("${app.storage.local.base-path:./uploads}")
    private String basePath;

    @Value("${app.storage.local.base-url:http://localhost:8080/api/files}")
    private String baseUrl;

    @Value("${app.storage.local.temp-token-secret:valorant-local-secret}")
    private String tempTokenSecret;

    // ✅ 使用 Spring InitializingBean 接口替代 @PostConstruct
    @Override
    public void afterPropertiesSet() throws Exception {
        init();
    }

    public void init() {
        // 创建上传目录
        try {
            Path uploadPath = Paths.get(basePath);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("创建上传目录: {}", uploadPath.toAbsolutePath());
            } else {
                log.info("上传目录已存在: {}", uploadPath.toAbsolutePath());
            }
        } catch (IOException e) {
            log.error("创建上传目录失败: {}", e.getMessage());
            throw new RuntimeException("无法创建上传目录", e);
        }
    }

    @Override
    public String upload(InputStream inputStream, String fileName, String contentType, Map<String, String> metadata) {
        try {
            // 构建完整的文件路径
            Path filePath = Paths.get(basePath, fileName);

            // 创建目录（如果不存在）
            Files.createDirectories(filePath.getParent());

            // 保存文件
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);

            // 保存元数据（可选）
            if (metadata != null && !metadata.isEmpty()) {
                saveMetadata(fileName, metadata);
            }

            log.info("文件保存成功: {}", filePath.toAbsolutePath());

            // 返回访问URL
            return getAccessUrl(fileName);

        } catch (IOException e) {
            log.error("文件保存失败: {}", e.getMessage());
            throw new RuntimeException("文件保存失败", e);
        }
    }

    @Override
    public boolean delete(String fileName) {
        try {
            Path filePath = Paths.get(basePath, fileName);
            boolean deleted = Files.deleteIfExists(filePath);

            // 删除元数据
            deleteMetadata(fileName);

            if (deleted) {
                log.info("文件删除成功: {}", filePath.toAbsolutePath());
            } else {
                log.warn("文件不存在，无需删除: {}", filePath.toAbsolutePath());
            }

            return deleted;
        } catch (IOException e) {
            log.error("文件删除失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean exists(String fileName) {
        try {
            Path filePath = Paths.get(basePath, fileName);
            return Files.exists(filePath);
        } catch (Exception e) {
            log.error("检查文件存在失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String getAccessUrl(String fileName) {
        // 返回公开访问URL
        return baseUrl + "/" + fileName.replace("\\", "/");
    }

    @Override
    public String getTemporaryUrl(String fileName, int expireMinutes) {
        // 生成带签名的临时URL
        long expireTime = System.currentTimeMillis() + (expireMinutes * 60 * 1000L);
        String token = generateToken(fileName, expireTime);

        return baseUrl + "/" + fileName.replace("\\", "/") +
                "?token=" + token + "&expire=" + expireTime;
    }

    @Override
    public String getStorageType() {
        return "LOCAL";
    }

    /**
     * 获取文件输入流（供Controller使用）
     */
    public InputStream getFileInputStream(String fileName) throws IOException {
        Path filePath = Paths.get(basePath, fileName);
        if (!Files.exists(filePath)) {
            throw new FileNotFoundException("文件不存在: " + fileName);
        }
        return Files.newInputStream(filePath);
    }

    /**
     * 获取文件内容类型
     */
    public String getContentType(String fileName) {
        try {
            Path filePath = Paths.get(basePath, fileName);
            String contentType = Files.probeContentType(filePath);
            return contentType != null ? contentType : "application/octet-stream";
        } catch (IOException e) {
            log.warn("获取文件类型失败: {}", e.getMessage());
            return "application/octet-stream";
        }
    }

    /**
     * 验证临时访问Token
     */
    public boolean validateToken(String fileName, String token, long expireTime) {
        // 检查是否过期
        if (System.currentTimeMillis() > expireTime) {
            log.warn("Token已过期: {}", fileName);
            return false;
        }

        // 验证Token
        String expectedToken = generateToken(fileName, expireTime);
        boolean valid = expectedToken.equals(token);

        if (!valid) {
            log.warn("Token验证失败: {}", fileName);
        }

        return valid;
    }

    // ==================== 私有方法 ====================

    /**
     * 生成访问Token
     */
    private String generateToken(String fileName, long expireTime) {
        try {
            String data = fileName + ":" + expireTime + ":" + tempTokenSecret;
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(data.getBytes());
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("生成Token失败", e);
        }
    }

    /**
     * 保存文件元数据
     */
    private void saveMetadata(String fileName, Map<String, String> metadata) {
        try {
            Path metadataPath = Paths.get(basePath, fileName + ".metadata");

            // 确保元数据目录存在
            Files.createDirectories(metadataPath.getParent());

            StringBuilder content = new StringBuilder();
            for (Map.Entry<String, String> entry : metadata.entrySet()) {
                content.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
            }

            Files.writeString(metadataPath, content.toString());
            log.debug("元数据保存成功: {}", metadataPath);
        } catch (IOException e) {
            log.warn("保存元数据失败: {}", e.getMessage());
        }
    }

    /**
     * 删除文件元数据
     */
    private void deleteMetadata(String fileName) {
        try {
            Path metadataPath = Paths.get(basePath, fileName + ".metadata");
            Files.deleteIfExists(metadataPath);
            log.debug("元数据删除成功: {}", metadataPath);
        } catch (IOException e) {
            log.warn("删除元数据失败: {}", e.getMessage());
        }
    }

    /**
     * 获取基础路径（用于调试）
     */
    public String getBasePath() {
        return basePath;
    }

    /**
     * 获取基础URL（用于调试）
     */
    public String getBaseUrl() {
        return baseUrl;
    }
}