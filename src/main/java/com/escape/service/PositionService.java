package com.escape.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.escape.entity.Position;

import java.util.List;
import java.util.Map;

/**
 * 点位服务接口
 *
 * @author escape
 * @since 2025-06-11
 */
public interface PositionService extends IService<Position> {

    /**
     * 三级筛选查询点位（核心功能）
     * @param mapId 地图ID（必填）
     * @param heroId 英雄ID（可选，null表示通用点位）
     * @param side 攻防方：attack/defense/both（必填）
     * @return 点位列表
     */
    List<Position> filterPositions(Long mapId, Long heroId, String side);

    /**
     * 获取点位详情（包含关联信息）
     * @param positionId 点位ID
     * @return 点位详情
     */
    Map<String, Object> getPositionDetail(Long positionId);

    /**
     * 根据地图和站点查询点位
     * @param mapId 地图ID
     * @param site 站点：A/B/C/MID
     * @return 点位列表
     */
    List<Position> getPositionsByMapAndSite(Long mapId, String site);

    /**
     * 获取热门点位
     * @param limit 数量限制
     * @return 热门点位列表（包含地图和英雄信息）
     */
    List<Map<String, Object>> getHotPositions(Integer limit);

    /**
     * 获取相关推荐点位
     * @param positionId 当前点位ID
     * @param limit 推荐数量
     * @return 推荐点位列表
     */
    List<Position> getRelatedPositions(Long positionId, Integer limit);

    /**
     * 按点位类型查询
     * @param positionType 点位类型：smoke/flash/molly/wall/orb/trap/general
     * @param limit 数量限制
     * @return 点位列表
     */
    List<Position> getPositionsByType(String positionType, Integer limit);

    /**
     * 按难度查询点位
     * @param difficulty 难度等级(1-5)
     * @return 点位列表
     */
    List<Position> getPositionsByDifficulty(Integer difficulty);

    /**
     * 分页查询点位（支持多条件）
     * @param page 分页参数
     * @param params 查询参数（mapId, heroId, side, positionType, difficulty）
     * @return 分页结果
     */
    IPage<Position> getPositionPage(Page<Position> page, Map<String, Object> params);

    /**
     * 增加浏览次数
     * @param positionId 点位ID
     * @return 是否成功
     */
    boolean incrementViewCount(Long positionId);

    /**
     * 获取地图点位统计
     * @return 各地图的点位数量
     */
    Map<Long, Integer> getMapPositionStatistics();

    /**
     * 获取英雄点位统计
     * @return 各英雄的点位数量
     */
    Map<Long, Integer> getHeroPositionStatistics();

    /**
     * 创建点位（包含图片处理）
     * @param position 点位信息
     * @return 是否成功
     */
    boolean createPosition(Position position);

    /**
     * 更新点位状态
     * @param positionId 点位ID
     * @param status 状态
     * @return 是否成功
     */
    boolean updatePositionStatus(Long positionId, Integer status);

    /**
     * 批量导入点位
     * @param positions 点位列表
     * @return 导入结果
     */
    Map<String, Object> batchImportPositions(List<Position> positions);

    /**
     * 获取点位筛选选项（用于前端筛选器）
     * @return 筛选选项（地图列表、英雄列表、类型列表等）
     */
    Map<String, List<?>> getFilterOptions();

    /**
     * 刷新点位缓存
     */
    void refreshPositionCache();

    /**
     * 获取用户的点位学习进度
     * @param userId 用户ID
     * @param mapId 地图ID（可选）
     * @return 学习进度统计
     */
    Map<String, Object> getUserPositionProgress(Long userId, Long mapId);
}