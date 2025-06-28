package com.escape.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 标签实体类
 *
 * @author escape
 * @since 2025-06-10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("tags")
public class Tag {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("tag_name")
    private String tagName;

    @TableField("tag_type")
    private String tagType;

    @TableField("description")
    private String description;

    @TableField("color")
    private String color;

    @TableField("hot_score")
    private Integer hotScore;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("status")
    private Integer status;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}