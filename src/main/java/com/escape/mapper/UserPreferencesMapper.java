package com.escape.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.escape.entity.UserPreferences;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户偏好设置数据访问层
 *
 * @author escape
 * @since 2025-06-02
 */
@Mapper
public interface UserPreferencesMapper extends BaseMapper<UserPreferences> {

    /**
     * 根据用户ID查询偏好设置
     */
    @Select("SELECT * FROM user_preferences WHERE user_id = #{userId}")
    UserPreferences findByUserId(@Param("userId") Long userId);
}