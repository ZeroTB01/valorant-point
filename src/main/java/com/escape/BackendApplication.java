package com.escape;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Valorant 点位攻略平台后端主启动类
 *
 * @author escape
 * @since 2025-06-02
 */
@EnableAsync
@EnableTransactionManagement
@MapperScan("com.escape.mapper")

@SpringBootApplication
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
        System.out.println("\n========================================");
        System.out.println("🎮 游戏内容平台启动成功！");
        System.out.println("📍 检测试运行: http://localhost:8080/api/auth/health");
        System.out.println("📚 API文档: http://localhost:8080/API.html");
        System.out.println("========================================\n");
    }

}
