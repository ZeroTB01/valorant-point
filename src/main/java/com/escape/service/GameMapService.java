package com.escape.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.escape.entity.GameMap;

import java.util.List;
import java.util.Map;

/**
 * 游戏地图服务接口
 *
 * @author escape
 * @since 2025-06-14
 */
public interface GameMapService extends IService<GameMap> {

    /**
     * 获取地图详情
     * @param mapId 地图ID
     * @return 地图详情
     */
    Map<String, Object> getMapDetail(Long mapId);

    /**
     * 根据地图标识获取地图
     * @param mapKey 地图标识
     * @return 地图信息
     */
    GameMap getByMapKey(String mapKey);

    /**
     * 根据地图类型获取地图列表
     * @param mapType 地图类型
     * @return 地图列表
     */
    List<GameMap> getMapsByType(String mapType);

    /**
     * 根据站点数量获取地图
     * @param siteCount 站点数量
     * @return 地图列表
     */
    List<GameMap> getMapsBySiteCount(Integer siteCount);

    /**
     * 获取所有启用的地图（带缓存）
     * @return 地图列表
     */
    List<GameMap> getAllEnabledMaps();

    /**
     * 获取地图选项列表（用于下拉框）
     * @return 地图简要信息
     */
    List<Map<String, Object>> getMapOptions();

    /**
     * 分页查询地图
     * @param page 分页参数
     * @param mapType 地图类型（可选）
     * @return 分页结果
     */
    IPage<GameMap> getMapPage(Page<GameMap> page, String mapType);

    /**
     * 创建地图
     * @param gameMap 地图信息
     * @return 是否成功
     */
    boolean createMap(GameMap gameMap);

    /**
     * 更新地图状态
     * @param mapId 地图ID
     * @param status 状态
     * @return 是否成功
     */
    boolean updateMapStatus(Long mapId, Integer status);

    /**
     * 获取地图统计信息
     * @return 统计结果（地图数量、点位数量等）
     */
    Map<String, Object> getMapStatistics();

    /**
     * 刷新地图缓存
     */
    void refreshMapCache();
}