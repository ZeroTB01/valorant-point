package com.escape.storage.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.escape.storage.FileStorageStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.Map;

/**
 * 阿里云OSS文件存储策略实现
 * 用于生产环境的文件存储
 *
 * @author escape
 * @since 2025-06-02
 */
@Slf4j
@Component("ossFileStorage")
public class OssFileStorageStrategy implements FileStorageStrategy {

    @Autowired(required = false)
    private OSS ossClient;

    @Value("${aliyun.oss.bucket-name}")
    private String bucketName;

    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    @Override
    public String upload(InputStream inputStream, String fileName, String contentType, Map<String, String> metadata) {
        if (ossClient == null) {
            throw new RuntimeException("OSS客户端未配置");
        }

        try {
            // 创建上传Object的Metadata
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentType(contentType);

            // 添加自定义元数据
            if (metadata != null && !metadata.isEmpty()) {
                objectMetadata.setUserMetadata(metadata);
            }

            // 上传文件
            PutObjectRequest request = new PutObjectRequest(bucketName, fileName, inputStream, objectMetadata);
            ossClient.putObject(request);

            log.info("OSS文件上传成功: {}", fileName);

            // 返回访问URL
            return getAccessUrl(fileName);

        } catch (Exception e) {
            log.error("OSS文件上传失败: {}", e.getMessage());
            throw new RuntimeException("文件上传失败", e);
        }
    }

    @Override
    public boolean delete(String fileName) {
        if (ossClient == null) {
            return false;
        }

        try {
            ossClient.deleteObject(bucketName, fileName);
            log.info("OSS文件删除成功: {}", fileName);
            return true;
        } catch (Exception e) {
            log.error("OSS文件删除失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean exists(String fileName) {
        if (ossClient == null) {
            return false;
        }

        try {
            return ossClient.doesObjectExist(bucketName, fileName);
        } catch (Exception e) {
            log.error("OSS检查文件存在失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String getAccessUrl(String fileName) {
        // 返回公开访问URL
        return String.format("https://%s.%s/%s", bucketName, endpoint, fileName);
    }

    @Override
    public String getTemporaryUrl(String fileName, int expireMinutes) {
        if (ossClient == null) {
            return getAccessUrl(fileName);
        }

        try {
            Date expiration = new Date(System.currentTimeMillis() + expireMinutes * 60 * 1000);
            URL url = ossClient.generatePresignedUrl(bucketName, fileName, expiration);
            return url.toString();
        } catch (Exception e) {
            log.error("生成OSS临时URL失败: {}", e.getMessage());
            return getAccessUrl(fileName);
        }
    }

    @Override
    public String getStorageType() {
        return "OSS";
    }
}