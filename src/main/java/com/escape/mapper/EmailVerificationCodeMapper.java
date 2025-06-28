package com.escape.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.escape.entity.EmailVerificationCode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 邮箱验证码数据访问层
 *
 * @author escape
 * @since 2025-06-02
 */
@Mapper
public interface EmailVerificationCodeMapper extends BaseMapper<EmailVerificationCode> {

    /**
     * 查询有效的验证码
     */
    @Select("SELECT * FROM email_verification_codes " +
            "WHERE email = #{email} AND type = #{type} AND used = 0 " +
            "AND expire_time > NOW() ORDER BY create_time DESC LIMIT 1")
    EmailVerificationCode findValidCode(@Param("email") String email, @Param("type") String type);

    /**
     * 标记验证码为已使用
     */
    @Update("UPDATE email_verification_codes SET used = 1 WHERE id = #{id}")
    int markAsUsed(@Param("id") Long id);
}