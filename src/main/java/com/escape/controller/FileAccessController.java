package com.escape.controller;

import com.escape.storage.impl.LocalFileStorageStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 本地文件访问控制器
 * 仅在使用本地存储时启用
 *
 * @author escape
 * @since 2025-06-02
 */
@Slf4j
@RestController
@RequestMapping("/files")
@ConditionalOnProperty(name = "app.storage.type", havingValue = "local")
public class FileAccessController {

    @Autowired
    private LocalFileStorageStrategy localFileStorage;

    /**
     * 访问文件 - 使用路径变量替代通配符
     * 例如: /files/image/20250602/xxx.jpg
     */
    @GetMapping("/{fileType}/{date}/{fileName:.+}")
    public ResponseEntity<InputStreamResource> getFile(
            @PathVariable String fileType,
            @PathVariable String date,
            @PathVariable String fileName,
            @RequestParam(required = false) String token,
            @RequestParam(required = false) Long expire,
            @RequestParam(required = false) String download) {

        try {
            // 构建完整的文件路径
            String fullFileName = fileType + "/" + date + "/" + fileName;

            // 检查文件是否存在
            if (!localFileStorage.exists(fullFileName)) {
                log.warn("文件不存在: {}", fullFileName);
                return ResponseEntity.notFound().build();
            }

            // 如果有token，验证token
            if (token != null && expire != null) {
                if (!localFileStorage.validateToken(fullFileName, token, expire)) {
                    log.warn("Token验证失败: {}", fullFileName);
                    return ResponseEntity.status(403).build(); // 访问被拒绝
                }
            }

            // 获取文件输入流
            InputStream inputStream = localFileStorage.getFileInputStream(fullFileName);
            String contentType = localFileStorage.getContentType(fullFileName);

            // 构建响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));

            // 如果是下载请求，添加 Content-Disposition 头
            if ("true".equals(download)) {
                String encodedName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
                headers.add(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + encodedName);
            }

            log.info("文件访问成功: {}", fullFileName);
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(new InputStreamResource(inputStream));

        } catch (IOException e) {
            log.error("文件访问失败: fileType={}, date={}, fileName={}, error={}",
                    fileType, date, fileName, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 访问根目录下的文件
     * 例如: /files/direct/avatar.jpg
     */
    @GetMapping("/direct/{fileName:.+}")
    public ResponseEntity<InputStreamResource> getDirectFile(
            @PathVariable String fileName,
            @RequestParam(required = false) String token,
            @RequestParam(required = false) Long expire,
            @RequestParam(required = false) String download) {

        try {
            // 检查文件是否存在
            if (!localFileStorage.exists(fileName)) {
                log.warn("直接文件不存在: {}", fileName);
                return ResponseEntity.notFound().build();
            }

            // Token验证
            if (token != null && expire != null) {
                if (!localFileStorage.validateToken(fileName, token, expire)) {
                    log.warn("直接文件Token验证失败: {}", fileName);
                    return ResponseEntity.status(403).build();
                }
            }

            // 获取文件输入流
            InputStream inputStream = localFileStorage.getFileInputStream(fileName);
            String contentType = localFileStorage.getContentType(fileName);

            // 构建响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));

            // 下载处理
            if ("true".equals(download)) {
                String encodedName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
                headers.add(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + encodedName);
            }

            log.info("直接文件访问成功: {}", fileName);
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(new InputStreamResource(inputStream));

        } catch (IOException e) {
            log.error("直接文件访问失败: fileName={}, error={}", fileName, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 获取文件信息
     */
    @GetMapping("/info/{fileType}/{date}/{fileName:.+}")
    public ResponseEntity<FileInfo> getFileInfo(
            @PathVariable String fileType,
            @PathVariable String date,
            @PathVariable String fileName) {
        try {
            String fullFileName = fileType + "/" + date + "/" + fileName;

            if (!localFileStorage.exists(fullFileName)) {
                return ResponseEntity.notFound().build();
            }

            FileInfo info = new FileInfo();
            info.setFileName(fileName);
            info.setFullPath(fullFileName);
            info.setContentType(localFileStorage.getContentType(fullFileName));
            info.setExists(true);

            return ResponseEntity.ok(info);

        } catch (Exception e) {
            log.error("获取文件信息失败: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 健康检查 - 检查存储系统状态
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        try {
            String storageType = localFileStorage.getStorageType();
            return ResponseEntity.ok("Local file storage is healthy: " + storageType);
        } catch (Exception e) {
            log.error("存储健康检查失败: {}", e.getMessage());
            return ResponseEntity.status(500).body("Storage unhealthy: " + e.getMessage());
        }
    }

    /**
     * 文件信息DTO
     */
    public static class FileInfo {
        private String fileName;
        private String fullPath;
        private String contentType;
        private boolean exists;

        // getters and setters
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        public String getFullPath() { return fullPath; }
        public void setFullPath(String fullPath) { this.fullPath = fullPath; }
        public String getContentType() { return contentType; }
        public void setContentType(String contentType) { this.contentType = contentType; }
        public boolean isExists() { return exists; }
        public void setExists(boolean exists) { this.exists = exists; }
    }
}