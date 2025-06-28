package com.escape.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 武器实体类
 *
 * @author escape
 * @since 2025-06-10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("weapons")
public class Weapon {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("weapon_key")
    private String weaponKey;

    @TableField("weapon_name")
    private String weaponName;

    @TableField("weapon_type")
    private String weaponType;

    @TableField("price")
    private Integer price;

    @TableField("damage_head")
    private Integer damageHead;

    @TableField("damage_body")
    private Integer damageBody;

    @TableField("damage_leg")
    private Integer damageLeg;

    @TableField("fire_rate")
    private BigDecimal fireRate;

    @TableField("magazine_size")
    private Integer magazineSize;

    @TableField("wall_penetration")
    private String wallPenetration;

    @TableField("image_url")
    private String imageUrl;

    @TableField("description")
    private String description;

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