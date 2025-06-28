package com.escape.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 地图实体类
 * 注意：类名使用GameMap避免与java.util.Map冲突
 *
 * @author escape
 * @since 2025-06-10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("maps")
public class GameMap {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("map_key")
    private String mapKey;

    @TableField("map_name")
    private String mapName;

    @TableField("map_type")
    private String mapType;

    @TableField("minimap_url")
    private String minimapUrl;

    @TableField("overview_url")
    private String overviewUrl;

    @TableField("description")
    private String description;

    @TableField("site_count")
    private Integer siteCount;

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