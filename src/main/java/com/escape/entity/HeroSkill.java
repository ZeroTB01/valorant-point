package com.escape.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 英雄技能实体类
 *
 * @author escape
 * @since 2025-06-10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("hero_skills")
public class HeroSkill {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("hero_id")
    private Long heroId;

    @TableField("skill_key")
    private String skillKey;

    @TableField("skill_name")
    private String skillName;

    @TableField("skill_icon")
    private String skillIcon;

    @TableField("description")
    private String description;

    @TableField("tips")
    private String tips;

    @TableField("cooldown")
    private String cooldown;

    @TableField("cost")
    private String cost;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}