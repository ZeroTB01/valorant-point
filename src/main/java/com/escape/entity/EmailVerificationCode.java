package com.escape.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 邮箱验证码实体类
 *
 * @author escape
 * @since 2025-06-02
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("email_verification_codes")
public class EmailVerificationCode {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("email")
    private String email;

    @TableField("code")
    private String code;

    @TableField("type")
    private String type;

    @TableField("used")
    private Integer used;

    @TableField("expire_time")
    private LocalDateTime expireTime;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;


}