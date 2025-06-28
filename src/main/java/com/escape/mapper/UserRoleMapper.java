package com.escape.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.escape.entity.UserRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户角色关联数据访问层
 *
 * @author escape
 * @since 2025-06-02
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {
}
