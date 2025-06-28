package com.escape.dto.response;

import lombok.Data;
import java.util.List;

/**
 * 用户信息响应DTO
 * 临时将时间字段改为String解决序列化问题
 *
 * @author escape
 * @since 2025-06-02
 */
@Data
public class UserInfoResponse {

    private Long id;
    private String username;
    private String email;
    private String nickname;
    private String avatar;
    private Integer status;
    private Integer emailVerified;

    // 改为String类型
    private String lastLoginTime;
    private String createTime;

    private List<String> roles;
    private UserPreferencesResponse preferences;
}