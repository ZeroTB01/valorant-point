package com.escape.test;

import com.escape.common.Result;
import com.escape.utils.EmailUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class EmailTest {
    @Autowired
    private EmailUtils emailUtils;
    @GetMapping("/test/test-mail")
    public Result<String> testMail() {
        boolean success = emailUtils.sendRegistrationCode("your@qq.com", "123456");
        return success ? Result.success("测试邮件发送成功") : Result.error("发送失败");
    }
}
