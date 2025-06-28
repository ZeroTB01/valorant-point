package com.escape.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 点位实体类
 *
 * @author escape
 * @since 2025-06-10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("positions")
public class Position {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("map_id")
    private Long mapId;

    @TableField("hero_id")
    private Long heroId;

    @TableField("position_name")
    private String positionName;

    @TableField("position_type")
    private String positionType;

    @TableField("side")
    private String side;

    @TableField("site")
    private String site;

    @TableField("difficulty")
    private Integer difficulty;

    @TableField("description")
    private String description;

    @TableField("setup_image")
    private String setupImage;

    @TableField("throw_image")
    private String throwImage;

    @TableField("landing_image")
    private String landingImage;

    @TableField("view_count")
    private Integer viewCount;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("status")
    private Integer status;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}