package com.escape.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.escape.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 角色数据访问层
 *
 * @author escape
 * @since 2025-06-02
 */
@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    /**
     * 根据角色标识查询角色
     */
    @Select("SELECT * FROM roles WHERE role_key = #{roleKey} AND deleted = 0")
    Role findByRoleKey(@Param("roleKey") String roleKey);
}