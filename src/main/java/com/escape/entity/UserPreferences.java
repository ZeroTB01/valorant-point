package com.escape.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户偏好设置实体类
 *
 * @author escape
 * @since 2025-06-02
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("user_preferences")
public class UserPreferences {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("theme_mode")
    private String themeMode;

    @TableField("video_quality")
    private String videoQuality;

    @TableField("language")
    private String language;

    @TableField("notification_email")
    private Integer notificationEmail;

    @TableField("auto_play_video")
    private Integer autoPlayVideo;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}