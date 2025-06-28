package com.escape.storage;

import java.io.InputStream;
import java.util.Map;

/**
 * 文件存储策略接口
 * 支持多种存储方式（本地、OSS、七牛云等）
 *
 * @author escape
 * @since 2025-06-02
 */
public interface FileStorageStrategy {

    /**
     * 上传文件
     * @param inputStream 文件输入流
     * @param fileName 文件名（包含路径）
     * @param contentType 文件类型
     * @param metadata 文件元数据
     * @return 文件访问URL
     */
    String upload(InputStream inputStream, String fileName, String contentType, Map<String, String> metadata);

    /**
     * 删除文件
     * @param fileName 文件名（包含路径）
     * @return 是否删除成功
     */
    boolean delete(String fileName);

    /**
     * 检查文件是否存在
     * @param fileName 文件名（包含路径）
     * @return 是否存在
     */
    boolean exists(String fileName);

    /**
     * 获取文件访问URL
     * @param fileName 文件名（包含路径）
     * @return 访问URL
     */
    String getAccessUrl(String fileName);

    /**
     * 获取文件临时访问URL（用于私有文件）
     * @param fileName 文件名（包含路径）
     * @param expireMinutes 过期时间（分钟）
     * @return 临时访问URL
     */
    String getTemporaryUrl(String fileName, int expireMinutes);

    /**
     * 获取存储类型
     * @return 存储类型名称
     */
    String getStorageType();
}