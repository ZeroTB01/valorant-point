package com.escape.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * 日期时间工具类
 * 用于日期时间的格式化和计算
 *
 * @author escape
 * @since 2025-06-02
 */
public class DateUtils {

    public static final String DEFAULT_DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd";
    public static final String DEFAULT_TIME_PATTERN = "HH:mm:ss";

    /**
     * 获取当前时间字符串
     */
    public static String getCurrentDateTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(DEFAULT_DATETIME_PATTERN));
    }

    /**
     * 获取当前日期字符串
     */
    public static String getCurrentDate() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(DEFAULT_DATE_PATTERN));
    }

    /**
     * 格式化时间
     */
    public static String formatDateTime(LocalDateTime dateTime, String pattern) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 计算两个时间之间的差值（分钟）
     */
    public static long getMinutesBetween(LocalDateTime start, LocalDateTime end) {
        return ChronoUnit.MINUTES.between(start, end);
    }

    /**
     * 检查时间是否已过期
     */
    public static boolean isExpired(LocalDateTime expireTime) {
        return LocalDateTime.now().isAfter(expireTime);
    }

    /**
     * 获取N分钟后的时间
     */
    public static LocalDateTime getTimeAfterMinutes(int minutes) {
        return LocalDateTime.now().plusMinutes(minutes);
    }
}