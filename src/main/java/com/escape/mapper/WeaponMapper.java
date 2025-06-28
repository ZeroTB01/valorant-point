package com.escape.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.escape.entity.Weapon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * 武器数据访问层
 *
 * @author escape
 * @since 2025-06-11
 */
@Mapper
public interface WeaponMapper extends BaseMapper<Weapon> {

    /**
     * 根据武器标识查询武器
     */
    @Select("SELECT * FROM weapons WHERE weapon_key = #{weaponKey} AND deleted = 0")
    Weapon findByWeaponKey(@Param("weaponKey") String weaponKey);

    /**
     * 根据武器类型查询武器列表
     */
    @Select("SELECT * FROM weapons WHERE weapon_type = #{weaponType} " +
            "AND status = 1 AND deleted = 0 ORDER BY price, sort_order")
    List<Weapon> findByWeaponType(@Param("weaponType") String weaponType);

    /**
     * 根据价格范围查询武器
     */
    @Select("SELECT * FROM weapons WHERE price BETWEEN #{minPrice} AND #{maxPrice} " +
            "AND status = 1 AND deleted = 0 ORDER BY price")
    List<Weapon> findByPriceRange(@Param("minPrice") Integer minPrice,
                                  @Param("maxPrice") Integer maxPrice);

    /**
     * 查询所有启用的武器
     */
    @Select("SELECT * FROM weapons WHERE status = 1 AND deleted = 0 " +
            "ORDER BY weapon_type, price, sort_order")
    List<Weapon> findAllEnabled();

    /**
     * 按武器类型统计数量和平均价格
     */
    @Select("SELECT weapon_type, COUNT(*) as count, AVG(price) as avg_price " +
            "FROM weapons WHERE deleted = 0 AND status = 1 " +
            "GROUP BY weapon_type")
    List<Map<String, Object>> statisticsByType();

    /**
     * 根据穿透等级查询武器
     */
    @Select("SELECT * FROM weapons WHERE wall_penetration = #{penetration} " +
            "AND status = 1 AND deleted = 0 ORDER BY damage_body DESC")
    List<Weapon> findByPenetration(@Param("penetration") String penetration);

    /**
     * 更新武器价格
     */
    @Update("UPDATE weapons SET price = #{price}, update_time = NOW() WHERE id = #{id}")
    int updatePrice(@Param("id") Long id, @Param("price") Integer price);

    /**
     * 查询武器简要信息（用于下拉选择等场景）
     */
    @Select("SELECT id, weapon_key, weapon_name, weapon_type, price " +
            "FROM weapons WHERE status = 1 AND deleted = 0 " +
            "ORDER BY weapon_type, price")
    List<Map<String, Object>> findWeaponOptions();
}