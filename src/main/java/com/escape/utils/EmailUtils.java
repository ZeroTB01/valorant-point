package com.escape.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * 邮件工具类
 * 用于发送各种邮件
 *
 * @author escape
 * @since 2025-06-02
 */
@Slf4j
@Component
public class EmailUtils {

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * 生成随机验证码
     */
    public String generateVerificationCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    /**
     * 发送注册验证码邮件
     */
    public boolean sendRegistrationCode(String toEmail, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("【Valorant攻略平台】注册验证码");
            message.setText(buildRegistrationEmailContent(code));

            javaMailSender.send(message);
            log.info("注册验证码邮件发送成功: {}", toEmail);
            return true;
        } catch (Exception e) {
            log.error("注册验证码邮件发送失败: {}, 错误: {}", toEmail, e.getMessage());
            return false;
        }
    }

    /**
     * 发送密码重置验证码邮件
     */
    public boolean sendPasswordResetCode(String toEmail, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("【Valorant攻略平台】密码重置验证码");
            message.setText(buildPasswordResetEmailContent(code));

            javaMailSender.send(message);
            log.info("密码重置验证码邮件发送成功: {}", toEmail);
            return true;
        } catch (Exception e) {
            log.error("密码重置验证码邮件发送失败: {}, 错误: {}", toEmail, e.getMessage());
            return false;
        }
    }

    /**
     * 构建注册邮件内容
     */
    private String buildRegistrationEmailContent(String code) {
        return String.format(
                "欢迎注册Valorant攻略平台！\n\n" +
                        "您的注册验证码是：%s\n\n" +
                        "验证码有效期为10分钟，请及时使用。\n" +
                        "如果您没有进行注册操作，请忽略此邮件。\n\n" +
                        "感谢您的使用！\n" +
                        "Valorant攻略平台团队",
                code
        );
    }

    /**
     * 构建密码重置邮件内容
     */
    private String buildPasswordResetEmailContent(String code) {
        return String.format(
                "您好！\n\n" +
                        "您正在重置Valorant攻略平台的登录密码。\n\n" +
                        "您的验证码是：%s\n\n" +
                        "验证码有效期为10分钟，请及时使用。\n" +
                        "如果您没有进行密码重置操作，请忽略此邮件。\n\n" +
                        "Valorant攻略平台团队",
                code
        );
    }

    /**
     * 发送欢迎邮件
     */
    public boolean sendWelcomeEmail(String toEmail, String username) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("【Valorant攻略平台】欢迎加入！");
            message.setText(buildWelcomeEmailContent(username));

            javaMailSender.send(message);
            log.info("欢迎邮件发送成功: {}", toEmail);
            return true;
        } catch (Exception e) {
            log.error("欢迎邮件发送失败: {}, 错误: {}", toEmail, e.getMessage());
            return false;
        }
    }

    /**
     * 构建欢迎邮件内容
     */
    private String buildWelcomeEmailContent(String username) {
        return String.format(
                "亲爱的 %s，\n\n" +
                        "欢迎加入Valorant攻略平台！\n\n" +
                        "在这里，您可以：\n" +
                        "• 学习各种英雄的技能和战术\n" +
                        "• 掌握地图点位和策略\n" +
                        "• 了解武器数据和特点\n" +
                        "• 观看高质量的教学视频\n\n" +
                        "开始您的Valorant进阶之旅吧！\n\n" +
                        "祝您游戏愉快！\n" +
                        "Valorant攻略平台团队",
                username
        );
    }
}