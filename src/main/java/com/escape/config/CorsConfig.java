package com.escape.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

/**
 * 跨域配置类
 * 解决前后端分离项目的跨域问题
 *
 * @author escape
 * @since 2025-06-02
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    /**
     * 跨域配置
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // 允许的前端域名（开发环境）
                .allowedOrigins("http://localhost:3000", "http://127.0.0.1:3000")
                // 允许的HTTP方法
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // 允许的请求头
                .allowedHeaders("*")
                // 允许携带认证信息
                .allowCredentials(true)
                // 预检请求的缓存时间
                .maxAge(3600);
    }

    /**
     * CORS配置源Bean（用于Spring Security）
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 临时允许所有源（开发环境）
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        // 或者具体添加cpolar域名
        // configuration.setAllowedOrigins(Arrays.asList(
        //     "http://localhost:3000",
        //     "http://127.0.0.1:3000",
        //     "http://4af08d69.r32.cpolar.top",
        //     "https://servicewechat.com"  // 微信小程序域名
        // ));

        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS"
        ));

        // 允许的头部
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // 允许携带认证信息
        configuration.setAllowCredentials(true);

        // 预检请求缓存时间
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}