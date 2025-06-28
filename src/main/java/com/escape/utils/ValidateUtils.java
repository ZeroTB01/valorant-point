package com.escape.utils;

import java.util.regex.Pattern;

/**
 * 数据验证工具类
 * 用于各种数据格式验证
 *
 * @author escape
 * @since 2025-06-02
 */
public class ValidateUtils {

    // 邮箱正则表达式
    private static final String EMAIL_PATTERN =
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

    // 手机号正则表达式（中国大陆）
    private static final String PHONE_PATTERN =
            "^1[3-9]\\d{9}$";

    // 用户名正则表达式（4-20位字母数字下划线）
    private static final String USERNAME_PATTERN =
            "^[a-zA-Z0-9_]{4,20}$";

    // 密码正则表达式（6-20位，至少包含字母和数字）
    private static final String PASSWORD_PATTERN =
            "^(?=.*[a-zA-Z])(?=.*\\d)[a-zA-Z\\d@$!%*?&]{6,20}$";

    private static final Pattern emailPattern = Pattern.compile(EMAIL_PATTERN);
    private static final Pattern phonePattern = Pattern.compile(PHONE_PATTERN);
    private static final Pattern usernamePattern = Pattern.compile(USERNAME_PATTERN);
    private static final Pattern passwordPattern = Pattern.compile(PASSWORD_PATTERN);

    /**
     * 验证邮箱格式
     */
    public static boolean isValidEmail(String email) {
        return email != null && emailPattern.matcher(email).matches();
    }

    /**
     * 验证手机号格式
     */
    public static boolean isValidPhone(String phone) {
        return phone != null && phonePattern.matcher(phone).matches();
    }

    /**
     * 验证用户名格式
     */
    public static boolean isValidUsername(String username) {
        return username != null && usernamePattern.matcher(username).matches();
    }

    /**
     * 验证密码强度
     */
    public static boolean isValidPassword(String password) {
        return password != null && passwordPattern.matcher(password).matches();
    }

    /**
     * 验证字符串是否为空
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 验证字符串是否不为空
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * 验证字符串长度是否在指定范围内
     */
    public static boolean isLengthValid(String str, int minLength, int maxLength) {
        if (str == null) {
            return false;
        }
        int length = str.length();
        return length >= minLength && length <= maxLength;
    }

    /**
     * 验证是否为数字
     */
    public static boolean isNumeric(String str) {
        if (isEmpty(str)) {
            return false;
        }
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 验证是否为正整数
     */
    public static boolean isPositiveInteger(String str) {
        if (isEmpty(str)) {
            return false;
        }
        try {
            int number = Integer.parseInt(str);
            return number > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 验证验证码格式（6位数字）
     */
    public static boolean isValidVerificationCode(String code) {
        return code != null && code.matches("^\\d{6}$");
    }
}