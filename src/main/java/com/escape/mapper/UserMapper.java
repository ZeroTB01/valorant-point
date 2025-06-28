package com.escape.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.escape.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户数据访问层
 *
 * @author escape
 * @since 2025-06-02
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据邮箱查询用户
     */
    @Select("SELECT * FROM users WHERE email = #{email} AND deleted = 0")
    User findByEmail(@Param("email") String email);

    /**
     * 根据用户名查询用户
     */
    @Select("SELECT * FROM users WHERE username = #{username} AND deleted = 0")
    User findByUsername(@Param("username") String username);

    /**
     * 根据用户ID查询用户角色
     */
    @Select("SELECT r.role_key FROM roles r " +
            "INNER JOIN user_roles ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND r.deleted = 0")
    List<String> findRolesByUserId(@Param("userId") Long userId);

    /**
     * 更新用户最后登录信息
     */
    @Update("UPDATE users SET last_login_time = #{loginTime}, last_login_ip = #{loginIp} " +
            "WHERE id = #{userId}")
    int updateLastLoginInfo(@Param("userId") Long userId,
                            @Param("loginTime") LocalDateTime loginTime,
                            @Param("loginIp") String loginIp);

    /**
     * 验证邮箱
     */
    @Update("UPDATE users SET email_verified = 1 WHERE id = #{userId}")
    int verifyEmail(@Param("userId") Long userId);
}