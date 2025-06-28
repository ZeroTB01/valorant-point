package com.escape.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * 文件存储管理器
 * 根据配置选择合适的存储策略
 *
 * @author escape
 * @since 2025-06-02
 */
@Slf4j
@Component
public class FileStorageManager {

    @Autowired
    private Map<String, FileStorageStrategy> storageStrategies;

    @Value("${app.storage.type:local}")
    private String storageType;

    private FileStorageStrategy currentStrategy;

    @PostConstruct
    public void init() {
        selectStrategy(storageType);
    }

    /**
     * 获取当前存储策略
     */
    public FileStorageStrategy getStrategy() {
        return currentStrategy;
    }

    /**
     * 切换存储策略
     */
    public void selectStrategy(String type) {
        String strategyName = type.toLowerCase() + "FileStorage";
        FileStorageStrategy strategy = storageStrategies.get(strategyName);

        if (strategy == null) {
            throw new IllegalArgumentException("不支持的存储类型: " + type);
        }

        this.currentStrategy = strategy;
        log.info("切换文件存储策略: {} -> {}", storageType, type);
        this.storageType = type;
    }

    /**
     * 获取当前存储类型
     */
    public String getCurrentStorageType() {
        return storageType;
    }
}