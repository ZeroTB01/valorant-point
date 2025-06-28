package com.escape.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云OSS配置类
 * 配置OSS客户端（仅在配置了OSS时启用）
 *
 * @author escape
 * @since 2025-06-02
 */
@Configuration
@ConditionalOnProperty(prefix = "aliyun.oss", name = "enabled", havingValue = "true", matchIfMissing = false)
public class OssConfig {

    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    @Value("${aliyun.oss.access-key-id}")
    private String accessKeyId;

    @Value("${aliyun.oss.access-key-secret}")
    private String accessKeySecret;

    /**
     * 创建OSS客户端Bean
     */
    @Bean
    public OSS ossClient() {
        return new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    }
}