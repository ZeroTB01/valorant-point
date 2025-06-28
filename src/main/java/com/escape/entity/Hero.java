package com.escape.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 英雄实体类
 *
 * @author escape
 * @since 2025-06-10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("heroes")
public class Hero {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("hero_key")
    private String heroKey;

    @TableField("hero_name")
    private String heroName;

    @TableField("hero_type")
    private String heroType;

    @TableField("avatar")
    private String avatar;

    @TableField("description")
    private String description;

    @TableField("difficulty")
    private Integer difficulty;

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