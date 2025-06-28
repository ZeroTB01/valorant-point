package com.escape.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;


/**
 * 登录请求DTO
 *
 * @author escape
 * @since 2025-06-02
 */
@Data
public class LoginRequest {

    @NotBlank(message = "邮箱不能为空")
    private String email;

    @NotBlank(message = "密码不能为空")
    private String password;

    private Boolean rememberMe = false;
}