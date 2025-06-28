package com.escape.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

/**
 * IP地址工具类
 *
 * @author escape
 * @since 2025-06-02
 */
public class IpUtils {

    private static final String UNKNOWN = "unknown";
    private static final String LOCALHOST_IPV4 = "127.0.0.1";
    private static final String LOCALHOST_IPV6 = "0:0:0:0:0:0:0:1";
    private static final String LOCALHOST_IPV6_SHORT = "::1";
    private static final String SEPARATOR = ",";

    /**
     * 获取客户端真实IP地址
     *
     * @param request HttpServletRequest对象
     * @return 客户端IP地址
     */
    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN;
        }

        // 1. 检查 X-Forwarded-For 头部
        String ip = request.getHeader("x-forwarded-for");
        if (isValidIp(ip)) {
            // X-Forwarded-For可能包含多个IP，取第一个非unknown的有效IP
            return getFirstValidIp(ip);
        }

        // 2. 检查 Proxy-Client-IP 头部
        ip = request.getHeader("Proxy-Client-IP");
        if (isValidIp(ip)) {
            return ip;
        }

        // 3. 检查 WL-Proxy-Client-IP 头部 (WebLogic服务器)
        ip = request.getHeader("WL-Proxy-Client-IP");
        if (isValidIp(ip)) {
            return ip;
        }

        // 4. 检查 HTTP_CLIENT_IP 头部
        ip = request.getHeader("HTTP_CLIENT_IP");
        if (isValidIp(ip)) {
            return ip;
        }

        // 5. 检查 HTTP_X_FORWARDED_FOR 头部
        ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        if (isValidIp(ip)) {
            return getFirstValidIp(ip);
        }

        // 6. 检查 X-Real-IP 头部 (Nginx代理)
        ip = request.getHeader("X-Real-IP");
        if (isValidIp(ip)) {
            return ip;
        }

        // 7. 最后获取remoteAddr
        ip = request.getRemoteAddr();

        // 处理本地回环地址
        if (LOCALHOST_IPV6.equals(ip) || LOCALHOST_IPV6_SHORT.equals(ip)) {
            ip = LOCALHOST_IPV4;
        }

        return ip;
    }

    /**
     * 检查IP是否有效
     *
     * @param ip IP地址
     * @return 是否有效
     */
    private static boolean isValidIp(String ip) {
        return StringUtils.hasText(ip) && !UNKNOWN.equalsIgnoreCase(ip);
    }

    /**
     * 从逗号分隔的IP列表中获取第一个有效IP
     *
     * @param ips 逗号分隔的IP列表
     * @return 第一个有效IP
     */
    private static String getFirstValidIp(String ips) {
        if (!StringUtils.hasText(ips)) {
            return UNKNOWN;
        }

        String[] ipArray = ips.split(SEPARATOR);
        for (String ip : ipArray) {
            String trimmedIp = ip.trim();
            if (isValidIp(trimmedIp) && !isInternalIp(trimmedIp)) {
                return trimmedIp;
            }
        }

        // 如果没有找到有效的外部IP，返回第一个非unknown的IP
        for (String ip : ipArray) {
            String trimmedIp = ip.trim();
            if (isValidIp(trimmedIp)) {
                return trimmedIp;
            }
        }

        return UNKNOWN;
    }

    /**
     * 判断是否为内网IP
     *
     * @param ip IP地址
     * @return 是否为内网IP
     */
    public static boolean isInternalIp(String ip) {
        if (!StringUtils.hasText(ip)) {
            return false;
        }

        // 本地回环地址
        if (LOCALHOST_IPV4.equals(ip) || LOCALHOST_IPV6.equals(ip) || LOCALHOST_IPV6_SHORT.equals(ip)) {
            return true;
        }

        try {
            String[] parts = ip.split("\\.");
            if (parts.length != 4) {
                return false;
            }

            int firstOctet = Integer.parseInt(parts[0]);
            int secondOctet = Integer.parseInt(parts[1]);

            // A类私有地址：10.0.0.0 - 10.255.255.255
            if (firstOctet == 10) {
                return true;
            }

            // B类私有地址：172.16.0.0 - 172.31.255.255
            if (firstOctet == 172 && secondOctet >= 16 && secondOctet <= 31) {
                return true;
            }

            // C类私有地址：192.168.0.0 - 192.168.255.255
            if (firstOctet == 192 && secondOctet == 168) {
                return true;
            }

            // 链路本地地址：169.254.0.0 - 169.254.255.255
            if (firstOctet == 169 && secondOctet == 254) {
                return true;
            }

        } catch (NumberFormatException e) {
            // 如果解析失败，认为不是内网IP
            return false;
        }

        return false;
    }

    /**
     * 验证IP地址格式是否正确
     *
     * @param ip IP地址
     * @return 是否为有效的IP地址格式
     */
    public static boolean isValidIpAddress(String ip) {
        if (!StringUtils.hasText(ip)) {
            return false;
        }

        // IPv4地址验证
        if (isValidIpv4(ip)) {
            return true;
        }

        // IPv6地址验证
        if (isValidIpv6(ip)) {
            return true;
        }

        return false;
    }

    /**
     * 验证IPv4地址格式
     */
    private static boolean isValidIpv4(String ip) {
        String[] parts = ip.split("\\.");
        if (parts.length != 4) {
            return false;
        }

        try {
            for (String part : parts) {
                int num = Integer.parseInt(part);
                if (num < 0 || num > 255) {
                    return false;
                }
                // 检查是否有前导零（除了"0"本身）
                if (part.length() > 1 && part.startsWith("0")) {
                    return false;
                }
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 验证IPv6地址格式（简单验证）
     */
    private static boolean isValidIpv6(String ip) {
        // 简单的IPv6格式检查
        if (ip.contains("::")) {
            // 包含双冒号的简化格式
            String[] parts = ip.split("::");
            if (parts.length > 2) {
                return false;
            }
        }

        // 基本的十六进制字符检查
        String cleanIp = ip.replace("::", ":").replace(":", "");
        return cleanIp.matches("^[0-9a-fA-F]*$");
    }

    /**
     * 获取本机IP地址（用于服务器端）
     *
     * @return 本机IP地址
     */
    public static String getLocalIpAddress() {
        try {
            java.net.InetAddress addr = java.net.InetAddress.getLocalHost();
            return addr.getHostAddress();
        } catch (Exception e) {
            return LOCALHOST_IPV4;
        }
    }
}