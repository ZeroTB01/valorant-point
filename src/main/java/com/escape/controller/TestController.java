package com.escape.controller;

import com.escape.common.Result;
import com.escape.common.ResultCode;
import com.escape.common.exception.BusinessException;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 测试控制器
 * 用于测试异常处理和基础功能
 *
 * @author escape
 * @since 2025-06-05
 */
@RestController
@RequestMapping("/test")
public class TestController {

    /**
     * 测试正常响应
     */
    @GetMapping("/success")
    public Result<String> testSuccess() {
        return Result.success("测试成功");
    }

    /**
     * 测试业务异常
     */
    @GetMapping("/business-exception")
    public Result<?> testBusinessException() {
        throw new BusinessException(ResultCode.USER_NOT_EXISTS);
    }

    /**
     * 测试自定义消息的业务异常
     */
    @GetMapping("/custom-exception")
    public Result<?> testCustomException() {
        throw new BusinessException("这是一个自定义的错误消息");
    }

    /**
     * 测试参数验证异常
     */
    @PostMapping("/validation")
    public Result<TestRequest> testValidation(@Valid @RequestBody TestRequest request) {
        return Result.success(request);
    }

    /**
     * 测试运行时异常
     */
    @GetMapping("/runtime-exception")
    public Result<?> testRuntimeException() {
        int result = 10 / 0; // 故意制造异常
        return Result.success(result);
    }

    /**
     * 测试请求DTO
     */
    @Data
    public static class TestRequest {
        @NotBlank(message = "名称不能为空")
        private String name;

        @NotBlank(message = "邮箱不能为空")
        private String email;
    }


}