package com.escape.dto.response;

import lombok.Data;

/**
 * 用户偏好响应DTO
 *
 * @author escape
 * @since 2025-06-02
 */
@Data
public class UserPreferencesResponse {

    private String themeMode;
    private String videoQuality;
    private String language;
    private Boolean notificationEmail;
    private Boolean autoPlayVideo;
}