package com.escape.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * 系统健康检查控制器
 * 用于验证数据库、Redis等基础服务连接状态
 *
 * @author escape
 * @since 2025-06-02
 */
@RestController
@RequestMapping("/health")
public class HealthController {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 系统健康检查
     */
    @GetMapping("/check")
    public Map<String, Object> healthCheck() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("message", "Valorant Platform Backend is running!");
        result.put("timestamp", System.currentTimeMillis());

        // 检查数据库连接
        try (Connection connection = dataSource.getConnection()) {
            result.put("database", "connected");
            result.put("databaseUrl", connection.getMetaData().getURL());
        } catch (Exception e) {
            result.put("database", "disconnected");
            result.put("databaseError", e.getMessage());
        }

        // 检查Redis连接
        try {
            stringRedisTemplate.opsForValue().set("health:check", "test");
            String testValue = stringRedisTemplate.opsForValue().get("health:check");
            if ("test".equals(testValue)) {
                result.put("redis", "connected");
            } else {
                result.put("redis", "test failed");
            }
        } catch (Exception e) {
            result.put("redis", "disconnected");
            result.put("redisError", e.getMessage());
        }

        return result;
    }

    /**
     * 简单的欢迎接口
     */
    @GetMapping("/welcome")
    public Map<String, String> welcome() {
        Map<String, String> result = new HashMap<>();
        result.put("message", "Welcome to Valorant Strategy Platform!");
        result.put("version", "1.0.0");
        return result;
    }
}