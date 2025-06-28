package com.escape.filter;

import com.escape.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * JWT认证过滤器
 * 负责从请求中提取JWT Token并进行验证
 *
 * @author escape
 * @since 2025-06-02
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String BLACKLIST_TOKEN_PREFIX = "blacklist_token:";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 获取Token
        String authorizationHeader = request.getHeader("Authorization");
        String token = null;

        if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);
        }

        // 如果没有Token，直接放行，让Spring Security处理
        if (!StringUtils.hasText(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 检查Token是否在黑名单中
            String blacklistKey = BLACKLIST_TOKEN_PREFIX + token;
            if (redisTemplate.hasKey(blacklistKey)) {
                log.warn("Token在黑名单中: {}", token);
                filterChain.doFilter(request, response);
                return;
            }

            // 验证Token
            if (jwtUtils.validateToken(token)) {
                // 从Token中获取用户信息
                Long userId = jwtUtils.getUserIdFromToken(token);
                String username = jwtUtils.getUsernameFromToken(token);
                String email = jwtUtils.getEmailFromToken(token);

                // 创建权限列表（这里简化处理，实际应该从数据库或缓存获取）
                List<SimpleGrantedAuthority> authorities = new ArrayList<>();

                // 游客用户
                if (userId == -1L) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_GUEST"));
                } else {
                    // 普通用户（实际应该从数据库查询角色）
                    authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                }

                // 创建认证对象
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userId, null, authorities);

                // 设置认证详情
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 将认证信息存入SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("JWT认证成功: userId={}, username={}", userId, username);
            } else {
                log.warn("JWT Token验证失败");
            }
        } catch (Exception e) {
            log.error("JWT认证过程发生错误: {}", e.getMessage());
        }

        // 继续过滤链
        filterChain.doFilter(request, response);
    }
}