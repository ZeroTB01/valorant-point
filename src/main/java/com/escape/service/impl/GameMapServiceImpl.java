package com.escape.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.escape.common.ResultCode;
import com.escape.common.exception.BusinessException;
import com.escape.entity.GameMap;
import com.escape.mapper.GameMapMapper;
import com.escape.mapper.PositionMapper;
import com.escape.service.GameMapService;
import com.escape.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 游戏地图服务实现类
 *
 * @author escape
 * @since 2025-06-14
 */
@Slf4j
@Service
public class GameMapServiceImpl extends ServiceImpl<GameMapMapper, GameMap> implements GameMapService {

    @Autowired
    private GameMapMapper gameMapMapper;

    @Autowired
    private PositionMapper positionMapper;

    @Autowired
    private RedisUtils redisUtils;

    private static final String MAP_CACHE_PREFIX = "map:";
    private static final String MAP_LIST_CACHE_KEY = "map:list:enabled";
    private static final String MAP_OPTIONS_CACHE_KEY = "map:options";
    private static final long CACHE_EXPIRE_HOURS = 2;

    @Override
    public Map<String, Object> getMapDetail(Long mapId) {
        if (mapId == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "地图ID不能为空");
        }

        // 先从缓存获取
        String cacheKey = MAP_CACHE_PREFIX + "detail:" + mapId;
        String cachedData = redisUtils.get(cacheKey);
        if (StringUtils.hasText(cachedData)) {
            log.debug("从缓存获取地图详情: {}", mapId);
        }

        // 查询地图基本信息
        GameMap gameMap = gameMapMapper.selectById(mapId);
        if (gameMap == null || gameMap.getDeleted() == 1) {
            throw new BusinessException(ResultCode.DATA_NOT_EXISTS.getCode(), "地图不存在");
        }

        // 查询地图的点位数量统计
        List<Map<String, Object>> positionStats = positionMapper.countByMap();
        Integer positionCount = positionStats.stream()
                .filter(stat -> mapId.equals(((Number) stat.get("map_id")).longValue()))
                .map(stat -> ((Number) stat.get("count")).intValue())
                .findFirst()
                .orElse(0);

        // 组装结果
        Map<String, Object> result = new HashMap<>();
        result.put("map", gameMap);
        result.put("positionCount", positionCount);

        // 缓存结果
        redisUtils.set(cacheKey, result.toString(), CACHE_EXPIRE_HOURS, TimeUnit.HOURS);

        return result;
    }

    @Override
    public GameMap getByMapKey(String mapKey) {
        if (!StringUtils.hasText(mapKey)) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "地图标识不能为空");
        }

        return gameMapMapper.findByMapKey(mapKey);
    }

    @Override
    public List<GameMap> getMapsByType(String mapType) {
        if (!StringUtils.hasText(mapType)) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "地图类型不能为空");
        }

        // 验证类型是否合法
        if (!isValidMapType(mapType)) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "无效的地图类型");
        }

        return gameMapMapper.findByMapType(mapType);
    }

    @Override
    public List<GameMap> getMapsBySiteCount(Integer siteCount) {
        if (siteCount == null || siteCount < 1 || siteCount > 3) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "站点数量必须在1-3之间");
        }

        return gameMapMapper.findBySiteCount(siteCount);
    }

    @Override
    public List<GameMap> getAllEnabledMaps() {
        // 先从缓存获取
        String cachedData = redisUtils.get(MAP_LIST_CACHE_KEY);
        if (StringUtils.hasText(cachedData)) {
            log.debug("从缓存获取地图列表");
        }

        List<GameMap> maps = gameMapMapper.findAllEnabled();

        // 缓存结果
        redisUtils.set(MAP_LIST_CACHE_KEY, maps.toString(), CACHE_EXPIRE_HOURS, TimeUnit.HOURS);

        return maps;
    }

    @Override
    public List<Map<String, Object>> getMapOptions() {
        // 先从缓存获取
        String cachedData = redisUtils.get(MAP_OPTIONS_CACHE_KEY);
        if (StringUtils.hasText(cachedData)) {
            log.debug("从缓存获取地图选项");
        }

        List<GameMap> maps = gameMapMapper.findMapOptions();

        List<Map<String, Object>> options = maps.stream()
                .map(map -> {
                    Map<String, Object> option = new HashMap<>();
                    option.put("id", map.getId());
                    option.put("mapKey", map.getMapKey());
                    option.put("mapName", map.getMapName());
                    return option;
                })
                .collect(Collectors.toList());

        // 缓存结果
        redisUtils.set(MAP_OPTIONS_CACHE_KEY, options.toString(), CACHE_EXPIRE_HOURS, TimeUnit.HOURS);

        return options;
    }

    @Override
    public IPage<GameMap> getMapPage(Page<GameMap> page, String mapType) {
        QueryWrapper<GameMap> wrapper = new QueryWrapper<>();
        wrapper.eq("deleted", 0)
                .eq("status", 1);

        if (StringUtils.hasText(mapType)) {
            wrapper.eq("map_type", mapType);
        }

        wrapper.orderByAsc("sort_order");

        return gameMapMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createMap(GameMap gameMap) {
        // 验证地图信息
        validateMap(gameMap);

        // 检查地图标识是否已存在
        GameMap existMap = gameMapMapper.findByMapKey(gameMap.getMapKey());
        if (existMap != null) {
            throw new BusinessException(ResultCode.DATA_ALREADY_EXISTS.getCode(), "地图标识已存在");
        }

        // 保存地图
        int result = gameMapMapper.insert(gameMap);
        if (result <= 0) {
            throw new BusinessException(ResultCode.DATABASE_ERROR.getCode(), "保存地图失败");
        }

        // 清除缓存
        refreshMapCache();

        return true;
    }

    @Override
    public boolean updateMapStatus(Long mapId, Integer status) {
        if (mapId == null || status == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "参数不能为空");
        }

        if (status != 0 && status != 1) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "状态值无效");
        }

        int result = gameMapMapper.updateStatus(mapId, status);

        if (result > 0) {
            // 清除相关缓存
            redisUtils.delete(MAP_CACHE_PREFIX + "detail:" + mapId);
            refreshMapCache();
        }

        return result > 0;
    }

    @Override
    public Map<String, Object> getMapStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // 总地图数
        QueryWrapper<GameMap> totalWrapper = new QueryWrapper<>();
        totalWrapper.eq("deleted", 0);
        long totalMaps = gameMapMapper.selectCount(totalWrapper);
        stats.put("totalMaps", totalMaps);

        // 启用的地图数
        QueryWrapper<GameMap> enabledWrapper = new QueryWrapper<>();
        enabledWrapper.eq("deleted", 0).eq("status", 1);
        long enabledMaps = gameMapMapper.selectCount(enabledWrapper);
        stats.put("enabledMaps", enabledMaps);

        // 各类型地图数量
        QueryWrapper<GameMap> typeWrapper = new QueryWrapper<>();
        typeWrapper.eq("deleted", 0).eq("status", 1)
                .select("map_type", "COUNT(*) as count")
                .groupBy("map_type");
        List<Map<String, Object>> typeStats = gameMapMapper.selectMaps(typeWrapper);
        stats.put("typeStatistics", typeStats);

        // 各地图的点位数量
        List<Map<String, Object>> positionStats = positionMapper.countByMap();
        stats.put("positionStatistics", positionStats);

        return stats;
    }

    @Override
    public void refreshMapCache() {
        log.info("刷新地图缓存");
        // 删除列表缓存
        redisUtils.delete(MAP_LIST_CACHE_KEY);
        // 删除选项缓存
        redisUtils.delete(MAP_OPTIONS_CACHE_KEY);
        // 删除所有地图详情缓存
        redisUtils.delete(redisUtils.keys(MAP_CACHE_PREFIX + "*"));
    }

    /**
     * 验证地图类型是否合法
     */
    private boolean isValidMapType(String mapType) {
        return "defuse".equals(mapType) || "deathmatch".equals(mapType);
    }

    /**
     * 验证地图信息
     */
    private void validateMap(GameMap gameMap) {
        if (gameMap == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "地图信息不能为空");
        }
        if (!StringUtils.hasText(gameMap.getMapKey())) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "地图标识不能为空");
        }
        if (!StringUtils.hasText(gameMap.getMapName())) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "地图名称不能为空");
        }
        if (!StringUtils.hasText(gameMap.getMapType())) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "地图类型不能为空");
        }
        if (!isValidMapType(gameMap.getMapType())) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "无效的地图类型");
        }
        if (gameMap.getSiteCount() == null || gameMap.getSiteCount() < 1 || gameMap.getSiteCount() > 3) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "站点数量必须在1-3之间");
        }
    }
}