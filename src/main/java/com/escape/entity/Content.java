package com.escape.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 内容实体类（视频、图文等）
 *
 * @author escape
 * @since 2025-06-10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("contents")
public class Content {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("content_type")
    private String contentType;

    @TableField("title")
    private String title;

    @TableField("description")
    private String description;

    @TableField("cover_image")
    private String coverImage;

    @TableField("author_id")
    private Long authorId;

    // 关联信息
    @TableField("position_id")
    private Long positionId;

    @TableField("hero_id")
    private Long heroId;

    @TableField("map_id")
    private Long mapId;

    @TableField("weapon_id")
    private Long weaponId;

    // 视频相关字段
    @TableField("video_url")
    private String videoUrl;

    @TableField("video_duration")
    private Integer videoDuration;

    @TableField("video_size")
    private Long videoSize;

    // 图文相关字段
    @TableField("content_body")
    private String contentBody;

    // 统计信息
    @TableField("view_count")
    private Integer viewCount;

    @TableField("like_count")
    private Integer likeCount;

    @TableField("collect_count")
    private Integer collectCount;

    // 状态信息
    @TableField("is_featured")
    private Integer isFeatured;

    @TableField("is_official")
    private Integer isOfficial;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("status")
    private Integer status;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    @TableField("publish_time")
    private LocalDateTime publishTime;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}