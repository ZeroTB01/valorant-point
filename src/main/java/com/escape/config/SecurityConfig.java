package com.escape.config;

import com.escape.filter.JwtAuthenticationFilter;
import com.escape.handler.JwtAccessDeniedHandler;
import com.escape.handler.JwtAuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Spring Security 配置类
 * 配置安全策略、认证授权规则
 *
 * @author escape
 * @since 2025-06-02
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private CorsConfigurationSource corsConfigurationSource;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private JwtAccessDeniedHandler jwtAccessDeniedHandler;

    /**
     * 密码编码器Bean
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 安全过滤链配置
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 启用CORS支持
                .cors(cors -> cors.configurationSource(corsConfigurationSource))

                // 禁用CSRF保护（因为是前后端分离架构）
                .csrf(csrf -> csrf.disable())

                // 配置会话管理为无状态（使用JWT Token）
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 配置异常处理
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler))

                // 配置请求授权规则
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers("/**").permitAll()  // 临时允许所有请求
                        // ==================== 系统基础接口 ====================
                        // 健康检查接口允许匿名访问
                        .requestMatchers("/health/**").permitAll()

                        // 测试接口允许匿名访问（开发阶段）
                        .requestMatchers("/test/**").permitAll()

                        // ==================== 文件相关接口 ====================
                        // 文件上传接口允许匿名访问（开发测试阶段）
                        .requestMatchers("/file/**").permitAll()

                        // 文件访问接口允许匿名访问
                        .requestMatchers("/files/**").permitAll()

                        // ==================== 认证相关接口 ====================
                        // 认证相关接口允许匿名访问
                        .requestMatchers(
                                "/auth/register",
                                "/auth/login",
                                "/auth/guest-login",
                                "/auth/refresh-token",
                                "/auth/send-verification-code",
                                "/auth/send-password-reset-code",
                                "/auth/reset-password"
                        ).permitAll()

                        // ==================== 公开数据接口 ====================
                        // 用户相关接口允许匿名访问（开发阶段）
                        .requestMatchers("/user/**").permitAll()

                        // 公开的数据接口（只读）
                        .requestMatchers(
                                "/hero/list",
                                "/hero/{id}",
                                "/map/list",
                                "/map/{id}",
                                "/weapon/list",
                                "/weapon/{id}",
                                "/positions/list",
                                "/positions/{id}",
                                "/tags/list"
                        ).permitAll()

                        // ==================== 静态资源 ====================
                        // 公开的静态资源允许访问
                        .requestMatchers("/static/**", "/public/**").permitAll()

                        // 错误页面允许访问
                        .requestMatchers("/error").permitAll()

                        // ==================== 开发工具 ====================
                        // Swagger文档（开发环境）
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // ==================== 管理员接口 ====================
                        // 管理员接口需要管理员角色
                        .requestMatchers("/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN")

                        // 内容管理接口需要内容管理员角色
                        .requestMatchers("/content/manage/**").hasAnyRole("CONTENT_MANAGER", "ADMIN", "SUPER_ADMIN")

                        // ==================== 其他接口 ====================
                        // 其他所有请求都需要认证
                        .anyRequest().authenticated()
                )

                // 禁用默认的登录页面
                .formLogin(form -> form.disable())

                // 禁用默认的登出处理
                .logout(logout -> logout.disable())

                // 配置HTTP Basic认证为禁用状态
                .httpBasic(basic -> basic.disable());

        // 添加JWT过滤器
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}