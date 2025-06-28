package com.escape.common;

import lombok.Getter;

/**
 * 返回状态码枚举
 * 定义系统中使用的所有状态码
 *
 * @author escape
 * @since 2025-06-02
 */
@Getter
public enum ResultCode {

    // ==================== 成功状态码 ====================
    SUCCESS(200, "操作成功"),

    // ==================== 客户端错误 4xx ====================
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权访问"),
    FORBIDDEN(403, "权限不足"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),
    CONFLICT(409, "数据冲突"),
    UNPROCESSABLE_ENTITY(422, "请求参数验证失败"),

    // ==================== 服务器错误 5xx ====================
    INTERNAL_SERVER_ERROR(500, "系统内部错误"),
    BAD_GATEWAY(502, "网关错误"),
    SERVICE_UNAVAILABLE(503, "服务不可用"),

    // ==================== 用户相关错误 1xxx ====================
    USER_NOT_EXISTS(1001, "用户不存在"),
    USER_ALREADY_EXISTS(1002, "用户已存在"),
    INVALID_PASSWORD(1003, "密码错误"),
    USER_DISABLED(1004, "用户已被禁用"),
    EMAIL_NOT_VERIFIED(1005, "邮箱未验证"),
    USER_PASSWORD_ERROR(1006, "用户名或密码错误"),
    USER_ACCOUNT_DISABLED(1007, "账号已被禁用"),
    USER_PERMISSION_DENIED(1008, "用户权限不足"),

    // ==================== 认证错误 2xxx ====================
    TOKEN_EXPIRED(2001, "Token已过期"),
    TOKEN_INVALID(2002, "Token无效"),
    LOGIN_REQUIRED(2003, "请先登录"),
    PERMISSION_DENIED(2004, "权限不足"),

    // ==================== 验证码错误 3xxx ====================
    VERIFICATION_CODE_EXPIRED(3001, "验证码已过期"),
    VERIFICATION_CODE_INVALID(3002, "验证码错误"),
    VERIFICATION_CODE_SEND_FAILED(3003, "验证码发送失败"),

    // ==================== 文件相关错误 4xxx ====================
    FILE_UPLOAD_FAILED(4001, "文件上传失败"),
    FILE_SIZE_EXCEEDED(4002, "文件大小超出限制"),
    FILE_TYPE_NOT_SUPPORTED(4003, "不支持的文件类型"),
    FILE_TYPE_NOT_ALLOWED(4004, "不允许的文件类型"),
    FILE_NOT_FOUND(4005, "文件不存在"),
    FILE_DELETE_FAILED(4006, "文件删除失败"),
    FILE_DOWNLOAD_FAILED(4007, "文件下载失败"),

    // ==================== 数据库错误 5xxx ====================
    DATABASE_ERROR(5001, "数据库操作失败"),
    DATA_NOT_EXISTS(5002, "数据不存在"),
    DATA_ALREADY_EXISTS(5003, "数据已存在"),
    DATA_INSERT_FAILED(5004, "数据插入失败"),
    DATA_UPDATE_FAILED(5005, "数据更新失败"),
    DATA_DELETE_FAILED(5006, "数据删除失败"),

    // ==================== 缓存错误 6xxx ====================
    CACHE_ERROR(6001, "缓存操作失败"),
    CACHE_EXPIRED(6002, "缓存已过期"),
    CACHE_NOT_FOUND(6003, "缓存不存在"),

    // ==================== 第三方服务错误 7xxx ====================
    EMAIL_SEND_FAILED(7001, "邮件发送失败"),
    OSS_UPLOAD_FAILED(7002, "文件上传到OSS失败"),
    OSS_DELETE_FAILED(7003, "OSS文件删除失败"),
    THIRD_PARTY_ERROR(7004, "第三方服务异常"),

    // ==================== 业务操作错误 8xxx ====================
    OPERATION_FAILED(8001, "操作失败"),
    OPERATION_NOT_ALLOWED(8002, "操作不允许"),
    OPERATION_TIMEOUT(8003, "操作超时"),

    // ==================== 系统级错误 9xxx ====================
    SYSTEM_BUSY(9001, "系统繁忙，请稍后重试"),
    SYSTEM_MAINTENANCE(9002, "系统维护中"),

    // ==================== 兜底错误 ====================
    INTERNAL_ERROR(9999, "系统内部错误");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 根据错误码获取ResultCode
     */
    public static ResultCode getByCode(Integer code) {
        if (code == null) {
            return INTERNAL_ERROR;
        }

        for (ResultCode resultCode : ResultCode.values()) {
            if (resultCode.getCode().equals(code)) {
                return resultCode;
            }
        }

        return INTERNAL_ERROR;
    }

    /**
     * 判断是否为成功状态
     */
    public boolean isSuccess() {
        return SUCCESS.getCode().equals(this.code);
    }

    /**
     * 判断是否为客户端错误（4xx）
     */
    public boolean isClientError() {
        return this.code >= 400 && this.code < 500;
    }

    /**
     * 判断是否为服务器错误（5xx及以上）
     */
    public boolean isServerError() {
        return this.code >= 500;
    }
}