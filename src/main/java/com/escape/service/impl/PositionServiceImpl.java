package com.escape.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.escape.common.ResultCode;
import com.escape.common.exception.BusinessException;
import com.escape.entity.*;
import com.escape.mapper.*;
import com.escape.service.PositionService;
import com.escape.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 点位服务实现类
 *
 * @author escape
 * @since 2025-06-11
 */
@Slf4j
@Service
public class PositionServiceImpl extends ServiceImpl<PositionMapper, Position> implements PositionService {

    @Autowired
    private PositionMapper positionMapper;

    @Autowired
    private GameMapMapper gameMapMapper;

    @Autowired
    private HeroMapper heroMapper;

    @Autowired
    private ViewHistoryMapper viewHistoryMapper;

    @Autowired
    private RedisUtils redisUtils;

    private static final String POSITION_CACHE_PREFIX = "position:";
    private static final String HOT_POSITIONS_KEY = "position:hot:";
    private static final String FILTER_OPTIONS_KEY = "position:filter:options";
    private static final long CACHE_EXPIRE_HOURS = 2;

    @Override
    public List<Position> filterPositions(Long mapId, Long heroId, String side) {
        // 参数验证
        if (mapId == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "地图ID不能为空");
        }
        if (!StringUtils.hasText(side)) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "攻防方不能为空");
        }
        if (!isValidSide(side)) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "无效的攻防方参数");
        }

        // 构建缓存键
        String cacheKey = String.format("%sfilter:%d:%s:%s",
                POSITION_CACHE_PREFIX, mapId, heroId != null ? heroId : "null", side);

        // 尝试从缓存获取
        String cachedData = redisUtils.get(cacheKey);
        if (StringUtils.hasText(cachedData)) {
            log.debug("从缓存获取筛选结果");
            // 实际项目中这里需要反序列化
        }

        // 执行查询
        List<Position> positions = positionMapper.findByThreeFilter(mapId, heroId, side);

        // 缓存结果
        if (!positions.isEmpty()) {
            redisUtils.set(cacheKey, positions.toString(), 1, TimeUnit.HOURS);
        }

        return positions;
    }

    @Override
    public Map<String, Object> getPositionDetail(Long positionId) {
        if (positionId == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "点位ID不能为空");
        }

        // 查询点位基本信息
        Position position = positionMapper.selectById(positionId);
        if (position == null || position.getDeleted() == 1) {
            throw new BusinessException(ResultCode.DATA_NOT_EXISTS.getCode(), "点位不存在");
        }

        // 查询关联的地图信息
        GameMap gameMap = gameMapMapper.selectById(position.getMapId());

        // 查询关联的英雄信息（如果有）
        Hero hero = null;
        if (position.getHeroId() != null) {
            hero = heroMapper.selectById(position.getHeroId());
        }

        // 组装结果
        Map<String, Object> result = new HashMap<>();
        result.put("position", position);
        result.put("map", gameMap);
        result.put("hero", hero);

        // 增加浏览次数（异步执行）
        incrementViewCount(positionId);

        return result;
    }

    @Override
    public List<Position> getPositionsByMapAndSite(Long mapId, String site) {
        if (mapId == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "地图ID不能为空");
        }
        if (!StringUtils.hasText(site)) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "站点不能为空");
        }

        return positionMapper.findByMapAndSite(mapId, site);
    }

    @Override
    public List<Map<String, Object>> getHotPositions(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10;
        }

        // 缓存键
        String cacheKey = HOT_POSITIONS_KEY + limit;

        // 尝试从缓存获取
        String cachedData = redisUtils.get(cacheKey);
        if (StringUtils.hasText(cachedData)) {
            log.debug("从缓存获取热门点位");
        }

        List<Map<String, Object>> hotPositions = positionMapper.findHotPositions(limit);

        // 缓存30分钟
        redisUtils.set(cacheKey, hotPositions.toString(), 30, TimeUnit.MINUTES);

        return hotPositions;
    }

    @Override
    public List<Position> getRelatedPositions(Long positionId, Integer limit) {
        if (positionId == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "点位ID不能为空");
        }
        if (limit == null || limit <= 0) {
            limit = 5;
        }

        // 获取当前点位信息
        Position currentPosition = positionMapper.selectById(positionId);
        if (currentPosition == null) {
            return new ArrayList<>();
        }

        // 查询相关点位（同地图、同类型）
        return positionMapper.findRelatedPositions(
                currentPosition.getMapId(),
                currentPosition.getPositionType(),
                positionId,
                limit
        );
    }

    @Override
    public List<Position> getPositionsByType(String positionType, Integer limit) {
        if (!StringUtils.hasText(positionType)) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "点位类型不能为空");
        }
        if (!isValidPositionType(positionType)) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "无效的点位类型");
        }
        if (limit == null || limit <= 0) {
            limit = 20;
        }

        return positionMapper.findByPositionType(positionType, limit);
    }

    @Override
    public List<Position> getPositionsByDifficulty(Integer difficulty) {
        if (difficulty == null || difficulty < 1 || difficulty > 5) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "难度等级必须在1-5之间");
        }

        return positionMapper.findByDifficulty(difficulty);
    }

    @Override
    public IPage<Position> getPositionPage(Page<Position> page, Map<String, Object> params) {
        QueryWrapper<Position> wrapper = new QueryWrapper<>();
        wrapper.eq("deleted", 0).eq("status", 1);

        // 处理查询参数
        if (params != null) {
            Object mapId = params.get("mapId");
            if (mapId != null) {
                wrapper.eq("map_id", mapId);
            }

            Object heroId = params.get("heroId");
            if (heroId != null) {
                wrapper.eq("hero_id", heroId);
            }

            Object side = params.get("side");
            if (side != null && StringUtils.hasText(side.toString())) {
                wrapper.eq("side", side);
            }

            Object positionType = params.get("positionType");
            if (positionType != null && StringUtils.hasText(positionType.toString())) {
                wrapper.eq("position_type", positionType);
            }

            Object difficulty = params.get("difficulty");
            if (difficulty != null) {
                wrapper.eq("difficulty", difficulty);
            }
        }

        wrapper.orderByAsc("difficulty", "sort_order");

        return positionMapper.selectPage(page, wrapper);
    }

    @Override
    public boolean incrementViewCount(Long positionId) {
        try {
            // 使用Redis实现防刷机制
            String viewKey = "position:view:" + positionId + ":" + getCurrentHour();
            Long count = redisUtils.increment(viewKey);

            // 设置过期时间为1小时
            if (count == 1) {
                redisUtils.expire(viewKey, 1, TimeUnit.HOURS);
            }

            // 每10次浏览更新一次数据库
            if (count % 10 == 0) {
                positionMapper.incrementViewCount(positionId);
            }

            return true;
        } catch (Exception e) {
            log.error("增加浏览次数失败", e);
            return false;
        }
    }

    @Override
    public Map<Long, Integer> getMapPositionStatistics() {
        List<Map<String, Object>> stats = positionMapper.countByMap();

        return stats.stream()
                .collect(Collectors.toMap(
                        m -> ((Number) m.get("map_id")).longValue(),
                        m -> ((Number) m.get("count")).intValue()
                ));
    }

    @Override
    public Map<Long, Integer> getHeroPositionStatistics() {
        List<Map<String, Object>> stats = positionMapper.countByHero();

        return stats.stream()
                .collect(Collectors.toMap(
                        m -> ((Number) m.get("hero_id")).longValue(),
                        m -> ((Number) m.get("count")).intValue()
                ));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createPosition(Position position) {
        // 验证点位信息
        validatePosition(position);

        // 检查地图是否存在
        GameMap gameMap = gameMapMapper.selectById(position.getMapId());
        if (gameMap == null || gameMap.getDeleted() == 1) {
            throw new BusinessException(ResultCode.DATA_NOT_EXISTS.getCode(), "地图不存在");
        }

        // 检查英雄是否存在（如果指定了英雄）
        if (position.getHeroId() != null) {
            Hero hero = heroMapper.selectById(position.getHeroId());
            if (hero == null || hero.getDeleted() == 1) {
                throw new BusinessException(ResultCode.DATA_NOT_EXISTS.getCode(), "英雄不存在");
            }
        }

        // 保存点位
        int result = positionMapper.insert(position);

        if (result > 0) {
            // 清除相关缓存
            refreshPositionCache();
        }

        return result > 0;
    }

    @Override
    public boolean updatePositionStatus(Long positionId, Integer status) {
        if (positionId == null || status == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "参数不能为空");
        }

        if (status != 0 && status != 1) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "状态值无效");
        }

        Position position = new Position();
        position.setId(positionId);
        position.setStatus(status);

        int result = positionMapper.updateById(position);

        if (result > 0) {
            refreshPositionCache();
        }

        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> batchImportPositions(List<Position> positions) {
        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int failCount = 0;
        List<String> errors = new ArrayList<>();

        for (int i = 0; i < positions.size(); i++) {
            Position position = positions.get(i);
            try {
                validatePosition(position);
                positionMapper.insert(position);
                successCount++;
            } catch (Exception e) {
                failCount++;
                errors.add(String.format("第%d条数据导入失败：%s", i + 1, e.getMessage()));
                log.error("导入点位失败", e);
            }
        }

        result.put("totalCount", positions.size());
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("errors", errors);

        if (successCount > 0) {
            refreshPositionCache();
        }

        return result;
    }

    @Override
    public Map<String, List<?>> getFilterOptions() {
        // 从缓存获取
        String cachedData = redisUtils.get(FILTER_OPTIONS_KEY);
        if (StringUtils.hasText(cachedData)) {
            log.debug("从缓存获取筛选选项");
        }

        Map<String, List<?>> options = new HashMap<>();

        // 获取所有启用的地图
        List<GameMap> maps = gameMapMapper.findAllEnabled();
        options.put("maps", maps);

        // 获取所有启用的英雄
        List<Hero> heroes = heroMapper.findAllEnabled();
        options.put("heroes", heroes);

        // 点位类型选项
        List<Map<String, String>> positionTypes = Arrays.asList(
                Map.of("value", "smoke", "label", "烟雾"),
                Map.of("value", "flash", "label", "闪光"),
                Map.of("value", "molly", "label", "燃烧"),
                Map.of("value", "wall", "label", "墙"),
                Map.of("value", "orb", "label", "球"),
                Map.of("value", "trap", "label", "陷阱"),
                Map.of("value", "general", "label", "通用")
        );
        options.put("positionTypes", positionTypes);

        // 攻防方选项
        List<Map<String, String>> sides = Arrays.asList(
                Map.of("value", "attack", "label", "进攻方"),
                Map.of("value", "defense", "label", "防守方"),
                Map.of("value", "both", "label", "通用")
        );
        options.put("sides", sides);

        // 难度选项
        List<Map<String, Object>> difficulties = Arrays.asList(
                Map.of("value", 1, "label", "入门"),
                Map.of("value", 2, "label", "简单"),
                Map.of("value", 3, "label", "中等"),
                Map.of("value", 4, "label", "困难"),
                Map.of("value", 5, "label", "大师")
        );
        options.put("difficulties", difficulties);

        // 缓存结果
        redisUtils.set(FILTER_OPTIONS_KEY, options.toString(), CACHE_EXPIRE_HOURS, TimeUnit.HOURS);

        return options;
    }

    @Override
    public void refreshPositionCache() {
        log.info("刷新点位缓存");
        // 删除筛选缓存
        redisUtils.delete(redisUtils.keys(POSITION_CACHE_PREFIX + "filter:*"));
        // 删除热门点位缓存
        redisUtils.delete(redisUtils.keys(HOT_POSITIONS_KEY + "*"));
        // 删除筛选选项缓存
        redisUtils.delete(FILTER_OPTIONS_KEY);
    }

    @Override
    public Map<String, Object> getUserPositionProgress(Long userId, Long mapId) {
        if (userId == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "用户ID不能为空");
        }

        Map<String, Object> progress = new HashMap<>();

        // 查询总点位数
        QueryWrapper<Position> totalWrapper = new QueryWrapper<>();
        totalWrapper.eq("status", 1).eq("deleted", 0);
        if (mapId != null) {
            totalWrapper.eq("map_id", mapId);
        }
        long totalCount = positionMapper.selectCount(totalWrapper);

        // 查询用户已学习的点位数（通过浏览历史）
        List<ViewHistory> histories = viewHistoryMapper.findByUserAndType(userId, "position");
        Set<Long> learnedPositionIds = histories.stream()
                .map(ViewHistory::getTargetId)
                .collect(Collectors.toSet());

        // 如果指定了地图，需要过滤
        if (mapId != null && !learnedPositionIds.isEmpty()) {
            QueryWrapper<Position> filterWrapper = new QueryWrapper<>();
            filterWrapper.in("id", learnedPositionIds)
                    .eq("map_id", mapId);
            learnedPositionIds = positionMapper.selectList(filterWrapper).stream()
                    .map(Position::getId)
                    .collect(Collectors.toSet());
        }

        long learnedCount = learnedPositionIds.size();

        progress.put("totalCount", totalCount);
        progress.put("learnedCount", learnedCount);
        progress.put("progressRate", totalCount > 0 ? (learnedCount * 100.0 / totalCount) : 0.0);
        progress.put("mapId", mapId);

        return progress;
    }

    /**
     * 验证攻防方参数
     */
    private boolean isValidSide(String side) {
        return "attack".equals(side) || "defense".equals(side) || "both".equals(side);
    }

    /**
     * 验证点位类型
     */
    private boolean isValidPositionType(String type) {
        return Arrays.asList("smoke", "flash", "molly", "wall", "orb", "trap", "general")
                .contains(type);
    }

    /**
     * 验证点位信息
     */
    private void validatePosition(Position position) {
        if (position == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "点位信息不能为空");
        }
        if (position.getMapId() == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "地图ID不能为空");
        }
        if (!StringUtils.hasText(position.getPositionName())) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "点位名称不能为空");
        }
        if (!StringUtils.hasText(position.getPositionType())) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "点位类型不能为空");
        }
        if (!isValidPositionType(position.getPositionType())) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "无效的点位类型");
        }
        if (!StringUtils.hasText(position.getSide())) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "攻防方不能为空");
        }
        if (!isValidSide(position.getSide())) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "无效的攻防方");
        }
    }

    /**
     * 获取当前小时（用于浏览计数）
     */
    private String getCurrentHour() {
        return String.valueOf(System.currentTimeMillis() / (1000 * 60 * 60));
    }
}