package com.escape.utils;

import com.escape.common.Result;

/**
 * 响应结果工具类
 * 统一封装API响应结果
 *
 * @author escape
 * @since 2025-06-02
 */
public class ResponseUtils {

    /**
     * 成功响应
     */
    public static <T> Result<T> success() {
        return Result.success();
    }

    /**
     * 成功响应带数据
     */
    public static <T> Result<T> success(T data) {
        return Result.success(data);
    }

    /**
     * 成功响应带消息和数据
     */
    public static <T> Result<T> success(String message, T data) {
        return Result.success(message, data);
    }

    /**
     * 失败响应
     */
    public static <T> Result<T> error(String message) {
        return Result.error(message);
    }

    /**
     * 失败响应带错误码
     */
    public static <T> Result<T> error(int code, String message) {
        return Result.error(code, message);
    }
}