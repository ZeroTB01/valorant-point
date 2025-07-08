# Valorant点位攻略平台 API接口文档

## 1. 概述

### 1.1 接口规范说明

#### 1.1.1 基础信息

- **接口基础路径**: `http://localhost:8080/api`
- **接口协议**: HTTP/HTTPS
- **接口风格**: RESTful
- **数据格式**: JSON
- **字符编码**: UTF-8
- **时间格式**: `yyyy-MM-dd HH:mm:ss`（Asia/Shanghai时区）

#### 1.1.2 请求规范

- **GET请求**: 参数通过URL Query传递
- **POST/PUT/DELETE请求**: 参数通过Request Body传递（Content-Type: application/json）
- **文件上传**: 使用multipart/form-data

#### 1.1.3 接口版本

- 当前版本：v1.0
- 版本控制：通过URL路径控制（预留）

### 1.2 认证机制说明

#### 1.2.1 认证方式

本系统采用 **JWT (JSON Web Token)** 认证方式。

#### 1.2.2 Token获取

1. 通过 `/auth/login` 接口登录获取
2. 通过 `/auth/guest-login` 接口获取游客Token
3. 通过 `/auth/refresh` 接口刷新Token

#### 1.2.3 Token使用

需要认证的接口，请在请求头中携带Token：

```
Authorization: Bearer {your_token}
```

#### 1.2.4 Token有效期

- **Access Token**: 24小时
- **Refresh Token**: 7天
- **游客Token**: 24小时（不可刷新）

#### 1.2.5 权限级别

1. **游客用户**: 可访问公开数据，不能收藏、发布内容
2. **普通用户(USER)**: 完整的用户功能
3. **内容管理员(CONTENT_ADMIN)**: 内容审核和管理
4. **超级管理员(SUPER_ADMIN)**: 系统所有权限

### 1.3 统一响应格式

#### 1.3.1 成功响应

```json
{
    "code": 200,
    "success": true,
    "message": "操作成功",
    "data": {
        // 实际返回的数据
    },
    "timestamp": 1719500000000
}
```

#### 1.3.2 失败响应

```json
{
    "code": 400,
    "success": false,
    "message": "参数错误",
    "data": null,
    "timestamp": 1719500000000
}
```

#### 1.3.3 分页数据响应

```json
{
    "code": 200,
    "success": true,
    "message": "查询成功",
    "data": {
        "records": [],      // 数据列表
        "total": 100,       // 总记录数
        "size": 10,         // 每页大小
        "current": 1,       // 当前页码
        "pages": 10         // 总页数
    },
    "timestamp": 1719500000000
}
```

### 1.4 通用错误码

|错误码|说明|HTTP状态码|处理建议|
|---|---|---|---|
|200|操作成功|200|-|
|400|请求参数错误|400|检查请求参数|
|401|未授权|401|需要登录或Token无效|
|403|无权限|403|权限不足|
|404|资源不存在|404|请求的资源未找到|
|500|服务器内部错误|500|联系后端开发|
|1001|用户不存在|200|提示用户注册|
|1002|密码错误|200|提示重新输入|
|1003|用户已存在|200|提示更换用户名|
|1004|验证码错误|200|重新获取验证码|
|1005|Token无效|200|重新登录|
|1006|Token过期|200|刷新Token或重新登录|
|1007|邮箱未验证|200|提示验证邮箱|
|1008|用户已禁用|200|联系管理员|
|2001|数据不存在|200|检查请求ID|
|2002|数据已存在|200|避免重复操作|
|3001|文件上传失败|200|检查文件大小和格式|
|3002|文件类型不支持|200|查看支持的文件类型|
|3003|文件大小超限|200|压缩文件或分片上传|
|4001|发送频率限制|200|等待后重试|
|5001|需要登录|200|引导用户登录|

#### 错误响应示例

```json
{
    "code": 1005,
    "success": false,
    "message": "Token无效或已过期",
    "data": null,
    "timestamp": 1719500000000
}
```

### 1.5 其他说明

#### 1.5.1 调用频率限制

- 验证码发送：同一邮箱1分钟内只能发送1次
- 文件上传：需登录用户，单文件最大100MB
- 其他接口：暂无限制

#### 1.5.2 跨域支持

开发环境已配置CORS，支持以下来源：

- http://localhost:3000
- http://127.0.0.1:3000

#### 1.5.3 数据格式约定

- 时间字段：统一使用字符串格式 "yyyy-MM-dd HH:mm:ss"
- 布尔值：使用 true/false
- 空值：使用 null，不使用空字符串
- 金额：使用整数分为单位

---



## 2. 认证模块（Auth）

### 2.1 用户注册 🔓

#### 接口描述

用户通过邮箱注册新账号，需要先获取邮箱验证码。

#### 请求信息

- **URL**: `/auth/register`
- **Method**: `POST`
- **需要认证**: 否
- **权限要求**: 无

#### 请求参数

|参数名|类型|必填|说明|示例|
|---|---|---|---|---|
|username|String|是|用户名，3-20位字母数字下划线|escape|
|email|String|是|邮箱地址|user@example.com|
|password|String|是|密码，6-20位|Pass123456|
|verificationCode|String|是|邮箱验证码，6位数字|123456|
|nickname|String|否|昵称，不填则使用用户名|逃逸者|

#### 请求示例

```json
{
    "username": "escape",
    "email": "user@example.com",
    "password": "Pass123456",
    "verificationCode": "123456",
    "nickname": "逃逸者"
}
```

#### 响应示例

```json
{
    "code": 200,
    "success": true,
    "message": "注册成功",
    "data": null,
    "timestamp": 1719500000000
}
```

#### 错误码

|错误码|说明|处理建议|
|---|---|---|
|1003|用户名已存在|更换用户名|
|1003|邮箱已被注册|使用其他邮箱或找回密码|
|1004|验证码错误或已过期|重新获取验证码|
|400|参数格式错误|检查参数格式|

---

### 2.2 用户登录 🔓

#### 接口描述

用户通过邮箱和密码登录系统，获取访问Token。

#### 请求信息

- **URL**: `/auth/login`
- **Method**: `POST`
- **需要认证**: 否
- **权限要求**: 无

#### 请求参数

|参数名|类型|必填|说明|示例|
|---|---|---|---|---|
|email|String|是|邮箱地址|user@example.com|
|password|String|是|密码|Pass123456|

#### 请求示例

```json
{
    "email": "user@example.com",
    "password": "Pass123456"
}
```

#### 响应示例

```json
{
    "code": 200,
    "success": true,
    "message": "登录成功",
    "data": {
        "userId": 1,
        "username": "escape",
        "email": "user@example.com",
        "nickname": "逃逸者",
        "avatar": "http://example.com/avatar.jpg",
        "roles": ["USER"],
        "accessToken": "eyJhbGciOiJIUzI1NiIs...",
        "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
        "expiresIn": 86400,
        "tokenType": "Bearer"
    },
    "timestamp": 1719500000000
}
```

#### 错误码

|错误码|说明|处理建议|
|---|---|---|
|1001|用户不存在|检查邮箱或先注册|
|1002|密码错误|重新输入密码|
|1007|邮箱未验证|先验证邮箱|
|1008|账户已被禁用|联系管理员|

---

### 2.3 游客登录 🔓

#### 接口描述

无需注册，快速获取游客身份Token，可以浏览公开内容但无法收藏等。

#### 请求信息

- **URL**: `/auth/guest-login`
- **Method**: `POST`
- **需要认证**: 否
- **权限要求**: 无

#### 请求参数

无

#### 响应示例

```json
{
    "code": 200,
    "success": true,
    "message": "游客登录成功",
    "data": {
        "userId": -1,
        "username": "guest_1719500000000",
        "email": "guest@temp.com",
        "nickname": "游客用户",
        "avatar": null,
        "roles": ["GUEST"],
        "accessToken": "eyJhbGciOiJIUzI1NiIs...",
        "refreshToken": null,
        "expiresIn": 86400,
        "tokenType": "Bearer"
    },
    "timestamp": 1719500000000
}
```

#### 业务说明

- 游客Token有效期24小时
- 游客无法使用刷新Token功能
- 游客userId固定为-1

---

### 2.4 发送验证码 🔓

#### 接口描述

发送邮箱验证码，用于注册或密码重置。

#### 请求信息

- **URL**: `/auth/send-code`
- **Method**: `POST`
- **需要认证**: 否
- **权限要求**: 无

#### 请求参数

|参数名|类型|必填|说明|示例|
|---|---|---|---|---|
|email|String|是|邮箱地址|user@example.com|
|type|String|是|验证码类型：register-注册，reset-重置密码|register|

#### 请求示例

```http
POST /api/auth/send-code?email=user@example.com&type=register
```

#### 响应示例

```json
{
    "code": 200,
    "success": true,
    "message": "验证码发送成功",
    "data": null,
    "timestamp": 1719500000000
}
```

#### 错误码

|错误码|说明|处理建议|
|---|---|---|
|1003|该邮箱已被注册（type=register时）|直接登录或找回密码|
|1001|邮箱未注册（type=reset时）|先注册账号|
|4001|发送太频繁，请稍后再试|等待1分钟后重试|
|500|邮件发送失败|稍后重试或联系管理员|

#### 业务说明

- 验证码有效期10分钟
- 同一邮箱1分钟内只能发送1次
- 验证码为6位数字

---

### 2.5 刷新Token 🔐

#### 接口描述

使用Refresh Token获取新的Access Token。

#### 请求信息

- **URL**: `/auth/refresh`
- **Method**: `POST`
- **需要认证**: 否（但需要有效的Refresh Token）
- **权限要求**: 无

#### 请求参数

|参数名|类型|必填|说明|示例|
|---|---|---|---|---|
|refreshToken|String|是|刷新Token|eyJhbGciOiJIUzI1NiIs...|

#### 请求示例

```http
POST /api/auth/refresh?refreshToken=eyJhbGciOiJIUzI1NiIs...
```

#### 响应示例

```json
{
    "code": 200,
    "success": true,
    "message": "Token刷新成功",
    "data": {
        "userId": 1,
        "username": "escape",
        "email": "user@example.com",
        "nickname": "逃逸者",
        "avatar": "http://example.com/avatar.jpg",
        "roles": ["USER"],
        "accessToken": "eyJhbGciOiJIUzI1NiIs...",
        "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
        "expiresIn": 86400,
        "tokenType": "Bearer"
    },
    "timestamp": 1719500000000
}
```

#### 错误码

|错误码|说明|处理建议|
|---|---|---|
|1005|Refresh Token无效|重新登录|
|1006|Refresh Token过期|重新登录|

---

### 2.6 忘记密码 🔓

#### 接口描述

发送密码重置验证码到用户邮箱。

#### 请求信息

- **URL**: `/auth/forgot-password`
- **Method**: `POST`
- **需要认证**: 否
- **权限要求**: 无

#### 请求参数

|参数名|类型|必填|说明|示例|
|---|---|---|---|---|
|email|String|是|注册邮箱|user@example.com|

#### 请求示例

```http
POST /api/auth/forgot-password?email=user@example.com
```

#### 响应示例

```json
{
    "code": 200,
    "success": true,
    "message": "密码重置验证码已发送到您的邮箱",
    "data": null,
    "timestamp": 1719500000000
}
```

#### 错误码

|错误码|说明|处理建议|
|---|---|---|
|1001|用户不存在|检查邮箱地址|
|4001|发送太频繁|等待1分钟后重试|

---

### 2.7 重置密码 🔓

#### 接口描述

使用验证码重置密码。

#### 请求信息

- **URL**: `/auth/reset-password`
- **Method**: `POST`
- **需要认证**: 否
- **权限要求**: 无

#### 请求参数

|参数名|类型|必填|说明|示例|
|---|---|---|---|---|
|email|String|是|邮箱地址|user@example.com|
|code|String|是|验证码|123456|
|newPassword|String|是|新密码，6-20位|NewPass123|

#### 请求示例

```http
POST /api/auth/reset-password?email=user@example.com&code=123456&newPassword=NewPass123
```

#### 响应示例

```json
{
    "code": 200,
    "success": true,
    "message": "密码重置成功",
    "data": null,
    "timestamp": 1719500000000
}
```

#### 错误码

|错误码|说明|处理建议|
|---|---|---|
|1001|用户不存在|检查邮箱地址|
|1004|验证码错误或已过期|重新获取验证码|
|400|密码格式不符合要求|检查密码格式|

---

### 2.8 用户登出 🔐

#### 接口描述

退出登录，服务端会将Token加入黑名单。

#### 请求信息

- **URL**: `/auth/logout`
- **Method**: `POST`
- **需要认证**: 是
- **权限要求**: 无

#### 请求头

```
Authorization: Bearer {your_token}
```

#### 请求参数

无

#### 响应示例

```json
{
    "code": 200,
    "success": true,
    "message": "登出成功",
    "data": null,
    "timestamp": 1719500000000
}
```

#### 业务说明

- 登出后原Token立即失效
- 客户端应清除本地存储的Token

---


## 3. 用户模块（User）

### 3.1 获取当前用户信息 🔐

#### 接口描述

获取当前登录用户的详细信息，包括基本信息和偏好设置。

#### 请求信息

- **URL**: `/user/profile`
- **Method**: `GET`
- **需要认证**: 是
- **权限要求**: 登录用户

#### 请求头

```
Authorization: Bearer {your_token}
```

#### 请求参数

无

#### 响应示例

```json
{
    "code": 200,
    "success": true,
    "message": "操作成功",
    "data": {
        "id": 1,
        "username": "escape",
        "email": "user@example.com",
        "nickname": "逃逸者",
        "avatar": "http://example.com/avatar.jpg",
        "phone": "138****8000",
        "status": 1,
        "emailVerified": 1,
        "roles": ["USER"],
        "lastLoginTime": "2025-06-27 10:00:00",
        "createTime": "2025-06-01 10:00:00",
        "preferences": {
            "themeMode": "dark",
            "videoQuality": "1080p",
            "language": "zh-CN",
            "notificationEmail": true,
            "notificationPush": false,
            "autoPlayVideo": true,
            "videoVolume": 80,
            "backgroundImage": "http://example.com/bg.jpg"
        },
        "statistics": {
            "favoriteCount": 25,
            "historyCount": 150,
            "uploadCount": 5
        }
    },
    "timestamp": 1719500000000
}
```

#### 错误码

|错误码|说明|处理建议|
|---|---|---|
|401|Token无效或过期|重新登录|
|1001|用户不存在|重新登录|

---

### 3.2 更新用户信息 🔐

#### 接口描述

更新用户基本信息，如昵称、手机号等。

#### 请求信息

- **URL**: `/user/profile`
- **Method**: `PUT`
- **需要认证**: 是
- **权限要求**: 登录用户

#### 请求头

```
Authorization: Bearer {your_token}
```

#### 请求参数

|参数名|类型|必填|说明|示例|
|---|---|---|---|---|
|nickname|String|否|昵称，2-20个字符|新昵称|
|phone|String|否|手机号|13800138000|
|backgroundImage|String|否|背景图URL|http://example.com/bg.jpg|

#### 请求示例

```json
{
    "nickname": "新昵称",
    "phone": "13800138000",
    "backgroundImage": "http://example.com/bg.jpg"
}
```

#### 响应示例

```json
{
    "code": 200,
    "success": true,
    "message": "更新成功",
    "data": {
        "id": 1,
        "username": "escape",
        "email": "user@example.com",
        "nickname": "新昵称",
        "avatar": "http://example.com/avatar.jpg",
        "phone": "138****8000",
        "status": 1,
        "emailVerified": 1,
        "roles": ["USER"],
        "updateTime": "2025-06-27 10:30:00"
    },
    "timestamp": 1719500000000
}
```

#### 错误码

|错误码|说明|处理建议|
|---|---|---|
|400|参数格式错误|检查参数格式|
|401|Token无效或过期|重新登录|

#### 业务说明

- 用户名和邮箱不可修改
- 手机号会自动脱敏显示

---

### 3.3 更新用户偏好 🔐

#### 接口描述

更新用户偏好设置，如主题、视频质量等。

#### 请求信息

- **URL**: `/user/preferences`
- **Method**: `PUT`
- **需要认证**: 是
- **权限要求**: 登录用户

#### 请求头

```
Authorization: Bearer {your_token}
```

#### 请求参数

|参数名|类型|必填|说明|示例|
|---|---|---|---|---|
|themeMode|String|否|主题模式：light-浅色，dark-深色|dark|
|videoQuality|String|否|视频质量：auto,720p,1080p,4k|1080p|
|language|String|否|语言：zh-CN,zh-TW,en-US|zh-CN|
|notificationEmail|Boolean|否|是否接收邮件通知|true|
|notificationPush|Boolean|否|是否接收推送通知|false|
|autoPlayVideo|Boolean|否|是否自动播放视频|true|
|videoVolume|Integer|否|视频音量：0-100|80|

#### 请求示例

```json
{
    "themeMode": "dark",
    "videoQuality": "1080p",
    "language": "zh-CN",
    "notificationEmail": true,
    "autoPlayVideo": true,
    "videoVolume": 80
}
```

#### 响应示例

```json
{
    "code": 200,
    "success": true,
    "message": "偏好设置更新成功",
    "data": {
        "themeMode": "dark",
        "videoQuality": "1080p",
        "language": "zh-CN",
        "notificationEmail": true,
        "notificationPush": false,
        "autoPlayVideo": true,
        "videoVolume": 80,
        "backgroundImage": "http://example.com/bg.jpg",
        "updateTime": "2025-06-27 10:35:00"
    },
    "timestamp": 1719500000000
}
```

#### 错误码

|错误码|说明|处理建议|
|---|---|---|
|400|参数值无效|检查参数值范围|
|401|Token无效或过期|重新登录|

#### 业务说明

- 所有参数都是可选的，只更新传递的字段
- 前端应该同步更新本地存储的偏好设置

---

### 3.4 上传头像 🔐

#### 接口描述

上传用户头像图片。

#### 请求信息

- **URL**: `/user/avatar`
- **Method**: `POST`
- **需要认证**: 是
- **权限要求**: 登录用户
- **Content-Type**: `multipart/form-data`

#### 请求头

```
Authorization: Bearer {your_token}
Content-Type: multipart/form-data
```

#### 请求参数

|参数名|类型|必填|说明|示例|
|---|---|---|---|---|
|file|File|是|头像图片文件|avatar.jpg|

#### 请求示例（Form Data）

```
file: [图片文件]
```

#### 响应示例

```json
{
    "code": 200,
    "success": true,
    "message": "头像上传成功",
    "data": {
        "avatarUrl": "http://example.com/uploads/avatar/2025/06/xxx.jpg",
        "thumbnailUrl": "http://example.com/uploads/avatar/2025/06/xxx_thumb.jpg"
    },
    "timestamp": 1719500000000
}
```

#### 错误码

|错误码|说明|处理建议|
|---|---|---|
|3001|文件上传失败|重试或检查网络|
|3002|文件类型不支持|仅支持jpg,png,gif|
|3003|文件大小超限|图片不能超过5MB|

#### 业务说明

- 支持的图片格式：jpg, jpeg, png, gif
- 最大文件大小：5MB
- 系统会自动生成缩略图
- 上传成功后会自动更新用户头像

---

### 3.5 更新密码 🔐

#### 接口描述

修改当前用户的登录密码。

#### 请求信息

- **URL**: `/user/password`
- **Method**: `PUT`
- **需要认证**: 是
- **权限要求**: 登录用户

#### 请求头

```
Authorization: Bearer {your_token}
```

#### 请求参数

|参数名|类型|必填|说明|示例|
|---|---|---|---|---|
|oldPassword|String|是|原密码|OldPass123|
|newPassword|String|是|新密码，6-20位|NewPass456|

#### 请求示例

```json
{
    "oldPassword": "OldPass123",
    "newPassword": "NewPass456"
}
```

#### 响应示例

```json
{
    "code": 200,
    "success": true,
    "message": "密码修改成功",
    "data": null,
    "timestamp": 1719500000000
}
```

#### 错误码

|错误码|说明|处理建议|
|---|---|---|
|1002|原密码错误|检查原密码|
|400|新密码格式不符合要求|6-20位密码|
|401|Token无效或过期|重新登录|

#### 业务说明

- 修改密码后，当前Token仍然有效
- 建议前端提示用户记住新密码

---

### 3.6 获取用户角色 🔐

#### 接口描述

获取当前用户的角色列表。

#### 请求信息

- **URL**: `/user/roles`
- **Method**: `GET`
- **需要认证**: 是
- **权限要求**: 登录用户

#### 请求头

```
Authorization: Bearer {your_token}
```

#### 请求参数

无

#### 响应示例

```json
{
    "code": 200,
    "success": true,
    "message": "操作成功",
    "data": [
        "USER",
        "CONTENT_ADMIN"
    ],
    "timestamp": 1719500000000
}
```

#### 角色说明

|角色标识|角色名称|权限说明|
|---|---|---|
|USER|普通用户|基本功能权限|
|CONTENT_ADMIN|内容管理员|内容审核和管理|
|SUPER_ADMIN|超级管理员|系统所有权限|

---

### 补充说明

#### 游客用户限制

游客用户（userId = -1）调用以上接口时：

- 3.1 获取用户信息：返回游客默认信息
- 3.2-3.6：返回错误码 5001（需要登录）

#### 偏好设置选项

获取偏好设置可选值：

- **GET** `/user/preferences/options`

响应示例：

```json
{
    "code": 200,
    "success": true,
    "message": "操作成功",
    "data": {
        "themeModeOptions": [
            {"value": "light", "label": "浅色主题"},
            {"value": "dark", "label": "深色主题"}
        ],
        "videoQualityOptions": [
            {"value": "auto", "label": "自动"},
            {"value": "720p", "label": "720P"},
            {"value": "1080p", "label": "1080P"},
            {"value": "4k", "label": "4K"}
        ],
        "languageOptions": [
            {"value": "zh-CN", "label": "简体中文"},
            {"value": "zh-TW", "label": "繁体中文"},
            {"value": "en-US", "label": "English"}
        ]
    },
    "timestamp": 1719500000000
}
```

---


## 4. 基础数据查询模块（公开接口）

### 4.1 英雄（Hero）

#### 4.1.1 获取英雄列表 🔓

##### 接口描述

获取所有启用的英雄列表，包含基本信息。

##### 请求信息

- **URL**: `/hero/list`
- **Method**: `GET`
- **需要认证**: 否
- **权限要求**: 无

##### 请求参数

无

##### 响应示例

```json
{
    "code": 200,
    "success": true,
    "message": "操作成功",
    "data": [
        {
            "id": 1,
            "heroKey": "jett",
            "heroName": "杰特",
            "heroType": "duelist",
            "avatar": "http://example.com/heroes/jett.png",
            "description": "来自韩国的杰特是一位注重灵活性的特工，她拥有无与伦比的速度和敏捷性",
            "difficulty": 3,
            "sortOrder": 1,
            "status": 1
        },
        {
            "id": 2,
            "heroKey": "phoenix",
            "heroName": "菲尼克斯",
            "heroType": "duelist",
            "avatar": "http://example.com/heroes/phoenix.png",
            "description": "来自英国的菲尼克斯，拥有火焰技能，善于制造战斗机会",
            "difficulty": 2,
            "sortOrder": 2,
            "status": 1
        }
    ],
    "timestamp": 1719500000000
}
```

##### 字段说明

|字段|类型|说明|
|---|---|---|
|heroKey|String|英雄标识（英文）|
|heroName|String|英雄名称（中文）|
|heroType|String|英雄类型：duelist-决斗者，sentinel-哨卫，controller-控场，initiator-先锋|
|difficulty|Integer|难度等级：1-5，数字越大越难|

---

#### 4.1.2 获取英雄详情 🔓

##### 接口描述

获取指定英雄的详细信息，包含技能列表。

##### 请求信息

- **URL**: `/hero/{heroId}`
- **Method**: `GET`
- **需要认证**: 否
- **权限要求**: 无

##### 路径参数

|参数名|类型|必填|说明|示例|
|---|---|---|---|---|
|heroId|Long|是|英雄ID|1|

##### 响应示例

```json
{
    "code": 200,
    "success": true,
    "message": "操作成功",
    "data": {
        "hero": {
            "id": 1,
            "heroKey": "jett",
            "heroName": "杰特",
            "heroType": "duelist",
            "avatar": "http://example.com/heroes/jett.png",
            "description": "来自韩国的杰特是一位注重灵活性的特工，她拥有无与伦比的速度和敏捷性",
            "difficulty": 3,
            "sortOrder": 1,
            "status": 1,
            "createTime": "2025-06-01 10:00:00",
            "updateTime": "2025-06-26 15:00:00"
        },
        "skills": [
            {
                "id": 1,
                "skillKey": "C",
                "skillName": "浮空",
                "skillIcon": "http://example.com/skills/jett_c.png",
                "description": "按住跳跃键在空中浮空",
                "tips": "可以在空中开枪，适合打出意想不到的角度",
                "cooldown": "2次充能",
                "cost": "200"
            },
            {
                "id": 2,
                "skillKey": "Q",
                "skillName": "逆风",
                "skillIcon": "http://example.com/skills/jett_q.png",
                "description": "立即向移动方向冲刺一小段距离",
                "tips": "可以快速撤退或进入战斗位置",
                "cooldown": "2次充能",
                "cost": "150"
            },
            {
                "id": 3,
                "skillKey": "E",
                "skillName": "顺风",
                "skillIcon": "http://example.com/skills/jett_e.png",
                "description": "激活后立即向前冲刺",
                "tips": "主要技能，每回合免费一次",
                "cooldown": "每回合1次",
                "cost": "免费"
            },
            {
                "id": 4,
                "skillKey": "X",
                "skillName": "刀锋风暴",
                "skillIcon": "http://example.com/skills/jett_x.png",
                "description": "装备一套高精度飞刀",
                "tips": "左键单发，右键连发，击杀会刷新飞刀",
                "cooldown": "7个充能点",
                "cost": "终极技能"
            }
        ],
        "positionCount": 25,
        "contentCount": 18
    },
    "timestamp": 1719500000000
}
```

##### 错误码

|错误码|说明|处理建议|
|---|---|---|
|2001|英雄不存在|检查英雄ID|

---

#### 4.1.3 获取英雄技能 🔓

##### 接口描述

获取指定英雄的技能列表。

##### 请求信息

- **URL**: `/hero/{heroId}/skills`
- **Method**: `GET`
- **需要认证**: 否
- **权限要求**: 无

##### 路径参数

|参数名|类型|必填|说明|示例|
|---|---|---|---|---|
|heroId|Long|是|英雄ID|1|

##### 响应示例

```json
{
    "code": 200,
    "success": true,
    "message": "操作成功",
    "data": [
        {
            "id": 1,
            "heroId": 1,
            "skillKey": "C",
            "skillName": "浮空",
            "skillIcon": "http://example.com/skills/jett_c.png",
            "description": "按住跳跃键在空中浮空",
            "tips": "可以在空中开枪，适合打出意想不到的角度",
            "cooldown": "2次充能",
            "cost": "200",
            "sortOrder": 1
        }
    ],
    "timestamp": 1719500000000
}
```

---

#### 4.1.4 按类型筛选英雄 🔓

##### 接口描述

根据英雄类型获取英雄列表。

##### 请求信息

- **URL**: `/hero/type/{heroType}`
- **Method**: `GET`
- **需要认证**: 否
- **权限要求**: 无

##### 路径参数

|参数名|类型|必填|说明|示例|
|---|---|---|---|---|
|heroType|String|是|英雄类型|duelist|

##### 英雄类型枚举

|值|说明|
|---|---|
|duelist|决斗者|
|sentinel|哨卫|
|controller|控场|
|initiator|先锋|

##### 响应示例

```json
{
    "code": 200,
    "success": true,
    "message": "操作成功",
    "data": [
        {
            "id": 1,
            "heroKey": "jett",
            "heroName": "杰特",
            "heroType": "duelist",
            "avatar": "http://example.com/heroes/jett.png",
            "description": "来自韩国的杰特是一位注重灵活性的特工",
            "difficulty": 3,
            "sortOrder": 1,
            "status": 1
        }
    ],
    "timestamp": 1719500000000
}
```

---

#### 4.1.5 获取英雄选项列表 🔓

##### 接口描述

获取简化的英雄选项列表，用于下拉框选择。

##### 请求信息

- **URL**: `/hero/options`
- **Method**: `GET`
- **需要认证**: 否
- **权限要求**: 无

##### 请求参数

无

##### 响应示例

```json
{
    "code": 200,
    "success": true,
    "message": "操作成功",
    "data": [
        {
            "value": 1,
            "label": "杰特",
            "heroType": "duelist",
            "avatar": "http://example.com/heroes/jett_small.png"
        },
        {
            "value": 2,
            "label": "菲尼克斯",
            "heroType": "duelist",
            "avatar": "http://example.com/heroes/phoenix_small.png"
        }
    ],
    "timestamp": 1719500000000
}
```

---

### 4.2 地图（Map）

#### 4.2.1 获取地图列表 🔓

##### 接口描述

获取所有启用的地图列表。

##### 请求信息

- **URL**: `/map/list`
- **Method**: `GET`
- **需要认证**: 否
- **权限要求**: 无

##### 请求参数

无

##### 响应示例

```json
{
    "code": 200,
    "success": true,
    "message": "操作成功",
    "data": [
        {
            "id": 1,
            "mapKey": "bind",
            "mapName": "炼狱镇",
            "mapType": "standard",
            "minimap": "http://example.com/maps/bind_mini.jpg",
            "overview": "http://example.com/maps/bind_overview.jpg",
            "description": "位于摩洛哥的双点地图，具有传送门机制",
            "sites": "A,B",
            "sortOrder": 1,
            "status": 1
        },
        {
            "id": 2,
            "mapKey": "haven",
            "mapName": "隐世修所",
            "mapType": "standard",
            "minimap": "http://example.com/maps/haven_mini.jpg",
            "overview": "http://example.com/maps/haven_overview.jpg",
            "description": "位于不丹的三点地图，唯一拥有三个炸弹点的地图",
            "sites": "A,B,C",
            "sortOrder": 2,
            "status": 1
        }
    ],
    "timestamp": 1719500000000
}
```

##### 字段说明

|字段|类型|说明|
|---|---|---|
|mapKey|String|地图标识（英文）|
|mapName|String|地图名称（中文）|
|mapType|String|地图类型：standard-标准，deathmatch-死斗|
|sites|String|炸弹点位：A,B或A,B,C|

---

#### 4.2.2 获取地图详情 🔓

##### 接口描述

获取指定地图的详细信息，包含点位统计。

##### 请求信息

- **URL**: `/map/{mapId}`
- **Method**: `GET`
- **需要认证**: 否
- **权限要求**: 无

##### 路径参数

|参数名|类型|必填|说明|示例|
|---|---|---|---|---|
|mapId|Long|是|地图ID|1|

##### 响应示例

```json
{
    "code": 200,
    "success": true,
    "message": "操作成功",
    "data": {
        "map": {
            "id": 1,
            "mapKey": "bind",
            "mapName": "炼狱镇",
            "mapType": "standard",
            "minimap": "http://example.com/maps/bind_mini.jpg",
            "overview": "http://example.com/maps/bind_overview.jpg",
            "description": "位于摩洛哥的双点地图，具有传送门机制",
            "sites": "A,B",
            "sortOrder": 1,
            "status": 1,
            "createTime": "2025-06-01 10:00:00",
            "updateTime": "2025-06-26 15:00:00"
        },
        "positionStats": {
            "total": 45,
            "attack": 25,
            "defense": 20,
            "bySite": {
                "A": 22,
                "B": 23
            }
        },
        "popularHeroes": [
            {
                "heroId": 1,
                "heroName": "杰特",
                "positionCount": 8
            },
            {
                "heroId": 5,
                "heroName": "贤者",
                "positionCount": 6
            }
        ]
    },
    "timestamp": 1719500000000
}
```

##### 错误码

|错误码|说明|处理建议|
|---|---|---|
|2001|地图不存在|检查地图ID|

---

#### 4.2.3 获取地图选项列表 🔓

##### 接口描述

获取简化的地图选项列表，用于筛选器。

##### 请求信息

- **URL**: `/map/options`
- **Method**: `GET`
- **需要认证**: 否
- **权限要求**: 无

##### 请求参数

无

##### 响应示例

```json
{
    "code": 200,
    "success": true,
    "message": "操作成功",
    "data": [
        {
            "value": 1,
            "label": "炼狱镇",
            "sites": ["A", "B"],
            "minimap": "http://example.com/maps/bind_tiny.jpg"
        },
        {
            "value": 2,
            "label": "隐世修所",
            "sites": ["A", "B", "C"],
            "minimap": "http://example.com/maps/haven_tiny.jpg"
        }
    ],
    "timestamp": 1719500000000
}
```

---

### 4.3 武器（Weapon）

#### 4.3.1 获取武器列表 🔓

##### 接口描述

获取所有启用的武器列表。

##### 请求信息

- **URL**: `/weapon/list`
- **Method**: `GET`
- **需要认证**: 否
- **权限要求**: 无

##### 请求参数

无

##### 响应示例

```json
{
    "code": 200,
    "success": true,
    "message": "操作成功",
    "data": [
        {
            "id": 1,
            "weaponKey": "vandal",
            "weaponName": "暴徒",
            "weaponType": "rifle",
            "price": 2900,
            "damageHead": 160,
            "damageBody": 40,
            "damageLeg": 34,
            "fireRate": 9.75,
            "magazineSize": 25,
            "wallPenetration": "high",
            "imageUrl": "http://example.com/weapons/vandal.png",
            "description": "高伤害突击步枪，一枪爆头",
            "sortOrder": 1,
            "status": 1
        },
        {
            "id": 2,
            "weaponKey": "phantom",
            "weaponName": "幻影",
            "weaponType": "rifle",
            "price": 2900,
            "damageHead": 140,
            "damageBody": 35,
            "damageLeg": 30,
            "fireRate": 11,
            "magazineSize": 30,
            "wallPenetration": "medium",
            "imageUrl": "http://example.com/weapons/phantom.png",
            "description": "稳定性高的突击步枪，带消音器",
            "sortOrder": 2,
            "status": 1
        }
    ],
    "timestamp": 1719500000000
}
```

##### 武器类型说明

|类型|说明|
|---|---|
|sidearm|手枪|
|smg|冲锋枪|
|rifle|步枪|
|sniper|狙击枪|
|heavy|重武器|
|melee|近战|

---

#### 4.3.2 获取武器详情 🔓

##### 接口描述

获取指定武器的详细信息。

##### 请求信息

- **URL**: `/weapon/{weaponId}`
- **Method**: `GET`
- **需要认证**: 否
- **权限要求**: 无

##### 路径参数

|参数名|类型|必填|说明|示例|
|---|---|---|---|---|
|weaponId|Long|是|武器ID|1|

##### 响应示例

```json
{
    "code": 200,
    "success": true,
    "message": "操作成功",
    "data": {
        "weapon": {
            "id": 1,
            "weaponKey": "vandal",
            "weaponName": "暴徒",
            "weaponType": "rifle",
            "price": 2900,
            "damageHead": 160,
            "damageBody": 40,
            "damageLeg": 34,
            "fireRate": 9.75,
            "magazineSize": 25,
            "wallPenetration": "high",
            "imageUrl": "http://example.com/weapons/vandal.png",
            "description": "高伤害突击步枪，一枪爆头",
            "sortOrder": 1,
            "status": 1,
            "createTime": "2025-06-01 10:00:00",
            "updateTime": "2025-06-26 15:00:00"
        },
        "damageRanges": [
            {
                "range": "0-50m",
                "head": 160,
                "body": 40,
                "leg": 34
            }
        ],
        "relatedContent": 15
    },
    "timestamp": 1719500000000
}
```

---

#### 4.3.3 按类型筛选武器 🔓

##### 接口描述

根据武器类型获取武器列表。

##### 请求信息

- **URL**: `/weapon/type/{weaponType}`
- **Method**: `GET`
- **需要认证**: 否
- **权限要求**: 无

##### 路径参数

|参数名|类型|必填|说明|示例|
|---|---|---|---|---|
|weaponType|String|是|武器类型|rifle|

##### 响应示例

```json
{
    "code": 200,
    "success": true,
    "message": "操作成功",
    "data": [
        {
            "id": 1,
            "weaponKey": "vandal",
            "weaponName": "暴徒",
            "weaponType": "rifle",
            "price": 2900,
            "imageUrl": "http://example.com/weapons/vandal.png",
            "description": "高伤害突击步枪"
        }
    ],
    "timestamp": 1719500000000
}
```

---

## 5. 核心业务模块

### 5.1 点位（Position）- 核心功能

#### 5.1.1 三级筛选点位 🔓 ⭐

##### 接口描述

项目核心功能，通过地图→英雄→攻防方的三级筛选获取匹配的点位列表。

##### 请求信息

- **URL**: `/position/filter`
- **Method**: `GET`
- **需要认证**: 否
- **权限要求**: 无

##### 请求参数

|参数名|类型|必填|说明|示例|
|---|---|---|---|---|
|mapId|Long|是|地图ID|1|
|heroId|Long|否|英雄ID，不传则返回该地图所有英雄的点位|1|
|side|String|是|攻防方：attack-进攻，defense-防守|attack|

##### 请求示例

```
GET /api/position/filter?mapId=1&heroId=1&side=attack
```

##### 响应示例

```json
{
    "code": 200,
    "success": true,
    "message": "操作成功",
    "data": [
        {
            "id": 1,
            "mapId": 1,
            "heroId": 1,
            "positionName": "A点长烟雾",
            "positionType": "smoke",
            "side": "attack",
            "site": "A",
            "difficulty": 2,
            "description": "封锁A长通道，方便队友推进",
            "setupImage": "http://example.com/positions/bind_jett_a_smoke_setup.jpg",
            "throwImage": "http://example.com/positions/bind_jett_a_smoke_throw.jpg",
            "landingImage": "http://example.com/positions/bind_jett_a_smoke_landing.jpg",
            "viewCount": 1520,
            "sortOrder": 1,
            "status": 1,
            "tags": ["常用", "A点", "进攻烟雾"],
            "heroInfo": {
                "heroName": "杰特",
                "heroAvatar": "http://example.com/heroes/jett_small.png"
            },
            "mapInfo": {
                "mapName": "炼狱镇",
                "site": "A"
            }
        },
        {
            "id": 2,
            "mapId": 1,
            "heroId": 1,
            "positionName": "A点高台闪光",
            "positionType": "flash",
            "side": "attack",
            "site": "A",
            "difficulty": 3,
            "description": "闪光弹清理A点高台防守位",
            "setupImage": "http://example.com/positions/bind_jett_a_flash_setup.jpg",
            "throwImage": "http://example.com/positions/bind_jett_a_flash_throw.jpg",
            "landingImage": "http://example.com/positions/bind_jett_a_flash_landing.jpg",
            "viewCount": 890,
            "sortOrder": 2,
            "status": 1,
            "tags": ["进阶", "A点", "闪光弹"]
        }
    ],
    "timestamp": 1719500000000
}
```

##### 点位类型说明

|类型|说明|
|---|---|
|smoke|烟雾弹|
|flash|闪光弹|
|molly|燃烧弹|
|wall|墙/屏障|
|orb|球（毒球等）|
|trap|陷阱|
|general|通用|

##### 错误码

|错误码|说明|处理建议|
|---|---|---|
|400|参数错误|检查必填参数|
|2001|地图不存在|检查地图ID|

##### 业务说明

- 这是项目最核心的功能接口
- 支持只选择地图查看所有英雄的点位
- 结果按照viewCount和sortOrder排序
- 返回数据包含关联的英雄和地图基本信息

---

#### 5.1.2 获取筛选选项 🔓

##### 接口描述

获取点位筛选器的可用选项，包括地图、英雄、攻防方选项。

##### 请求信息

- **URL**: `/position/filter-options`
- **Method**: `GET`
- **需要认证**: 否
- **权限要求**: 无

##### 请求参数

无

##### 响应示例

```json
{
    "code": 200,
    "success": true,
    "message": "操作成功",
    "data": {
        "maps": [
            {
                "value": 1,
                "label": "炼狱镇",
                "sites": ["A", "B"]
            },
            {
                "value": 2,
                "label": "隐世修所",
                "sites": ["A", "B", "C"]
            }
        ],
        "heroes": [
            {
                "value": 0,
                "label": "全部英雄"
            },
            {
                "value": 1,
                "label": "杰特",
                "type": "duelist"
            },
            {
                "value": 2,
                "label": "菲尼克斯",
                "type": "duelist"
            }
        ],
        "sides": [
            {
                "value": "attack",
                "label": "进攻方",
                "icon": "⚔️"
            },
            {
                "value": "defense",
                "label": "防守方",
                "icon": "🛡️"
            },
            {
                "value": "both",
                "label": "通用",
                "icon": "🔄"
            }
        ],
        "positionTypes": [
            {
                "value": "smoke",
                "label": "烟雾弹",
                "color": "#9E9E9E"
            },
            {
                "value": "flash",
                "label": "闪光弹",
                "color": "#FFC107"
            },
            {
                "value": "molly",
                "label": "燃烧弹",
                "color": "#FF5722"
            }
        ]
    },
    "timestamp": 1719500000000
}
```

---

#### 5.1.3 获取点位详情 🔓

##### 接口描述

获取单个点位的详细信息，包含完整的图片和说明。

##### 请求信息

- **URL**: `/position/{positionId}`
- **Method**: `GET`
- **需要认证**: 否
- **权限要求**: 无

##### 路径参数

|参数名|类型|必填|说明|示例|
|---|---|---|---|---|
|positionId|Long|是|点位ID|1|

##### 响应示例

```json
{
    "code": 200,
    "success": true,
    "message": "操作成功",
    "data": {
        "position": {
            "id": 1,
            "mapId": 1,
            "heroId": 1,
            "positionName": "A点长烟雾",
            "positionType": "smoke",
            "side": "attack",
            "site": "A",
            "difficulty": 2,
            "description": "封锁A长通道，方便队友推进。这个烟雾弹可以完全覆盖A长入口，阻挡防守方视线。",
            "setupImage": "http://example.com/positions/bind_jett_a_smoke_setup.jpg",
            "throwImage": "http://example.com/positions/bind_jett_a_smoke_throw.jpg",
            "landingImage": "http://example.com/positions/bind_jett_a_smoke_landing.jpg",
            "viewCount": 1521,
            "sortOrder": 1,
            "status": 1,
            "createTime": "2025-06-01 10:00:00",
            "updateTime": "2025-06-26 15:00:00"
        },
        "map": {
            "id": 1,
            "mapName": "炼狱镇",
            "mapKey": "bind",
            "minimap": "http://example.com/maps/bind_mini.jpg"
        },
        "hero": {
            "id": 1,
            "heroName": "杰特",
            "heroKey": "jett",
            "avatar": "http://example.com/heroes/jett.png"
        },
        "relatedPositions": [
            {
                "id": 2,
                "positionName": "A点高台闪光",
                "positionType": "flash",
                "difficulty": 3
            }
        ],
        "tags": ["常用", "A点", "进攻烟雾"]
    },
    "timestamp": 1719500000000
}
```

##### 业务说明

- 每次访问会增加viewCount
- 返回相关推荐点位

---

#### 5.1.4 获取热门点位 🔓

##### 接口描述

获取浏览量最高的热门点位。

##### 请求信息

- **URL**: `/position/hot`
- **Method**: `GET`
- **需要认证**: 否
- **权限要求**: 无

##### 请求参数

|参数名|类型|必填|说明|示例|
|---|---|---|---|---|
|limit|Integer|否|返回数量，默认10|10|
|mapId|Long|否|筛选特定地图|1|
|heroId|Long|否|筛选特定英雄|1|

##### 响应示例

```json
{
    "code": 200,
    "success": true,
    "message": "操作成功",
    "data": [
        {
            "positionId": 1,
            "positionName": "A点长烟雾",
            "mapName": "炼狱镇",
            "heroName": "杰特",
            "viewCount": 1521,
            "thumbnail": "http://example.com/positions/thumbs/1.jpg"
        }
    ],
    "timestamp": 1719500000000
}
```

---

### 5.2 内容（Content）

#### 5.2.1 获取内容详情 🔓

##### 接口描述

获取视频或图文内容的详细信息。

##### 请求信息

- **URL**: `/content/{contentId}`
- **Method**: `GET`
- **需要认证**: 否
- **权限要求**: 无

##### 路径参数

|参数名|类型|必填|说明|示例|
|---|---|---|---|---|
|contentId|Long|是|内容ID|1|

##### 响应示例

```json
{
    "code": 200,
    "success": true,
    "message": "操作成功",
    "data": {
        "content": {
            "id": 1,
            "contentType": "video",
            "title": "炼狱镇杰特进攻技巧合集",
            "description": "包含炼狱镇杰特的多个实用进攻点位和技巧",
            "coverImage": "http://example.com/contents/covers/1.jpg",
            "authorId": 1,
            "positionId": 1,
            "heroId": 1,
            "mapId": 1,
            "videoUrl": "http://example.com/videos/1.mp4",
            "videoDuration": 180,
            "videoSize": 52428800,
            "contentBody": null,
            "viewCount": 2580,
            "likeCount": 158,
            "collectCount": 89,
            "isFeatured": 1,
            "isOfficial": 0,
            "status": 1,
            "publishTime": "2025-06-20 10:00:00",
            "createTime": "2025-06-20 09:00:00"
        },
        "author": {
            "userId": 1,
            "username": "escape",
            "nickname": "逃逸者",
            "avatar": "http://example.com/avatars/1.jpg"
        },
        "relatedData": {
            "hero": {
                "heroName": "杰特",
                "heroKey": "jett"
            },
            "map": {
                "mapName": "炼狱镇",
                "mapKey": "bind"
            },
            "position": {
                "positionName": "A点长烟雾"
            }
        },
        "tags": ["进攻技巧", "杰特", "炼狱镇", "教学"],
        "relatedContents": [
            {
                "id": 2,
                "title": "炼狱镇防守要点",
                "coverImage": "http://example.com/contents/covers/2.jpg",
                "contentType": "video",
                "viewCount": 1890
            }
        ]
    },
    "timestamp": 1719500000000
}
```

##### 内容类型说明

|类型|说明|
|---|---|
|video|视频内容|
|article|图文内容|
|mixed|混合内容|

##### 业务说明

- 每次访问会增加viewCount
- 视频内容返回videoUrl
- 图文内容返回contentBody（Markdown格式）

---

#### 5.2.2 按类型获取内容 🔓

##### 接口描述

根据内容类型获取内容列表。

##### 请求信息

- **URL**: `/content/type/{contentType}`
- **Method**: `GET`
- **需要认证**: 否
- **权限要求**: 无

##### 路径参数

|参数名|类型|必填|说明|示例|
|---|---|---|---|---|
|contentType|String|是|内容类型|video|

##### 响应示例

```json
{
    "code": 200,
    "success": true,
    "message": "操作成功",
    "data": [
        {
            "id": 1,
            "contentType": "video",
            "title": "炼狱镇杰特进攻技巧合集",
            "description": "包含炼狱镇杰特的多个实用进攻点位和技巧",
            "coverImage": "http://example.com/contents/covers/1.jpg",
            "videoDuration": 180,
            "viewCount": 2580,
            "likeCount": 158,
            "publishTime": "2025-06-20 10:00:00"
        }
    ],
    "timestamp": 1719500000000
}
```

---

#### 5.2.3 获取热门内容 🔓

##### 接口描述

获取浏览量最高的热门内容。

##### 请求信息

- **URL**: `/content/hot`
- **Method**: `GET`
- **需要认证**: 否
- **权限要求**: 无

##### 请求参数

|参数名|类型|必填|说明|示例|
|---|---|---|---|---|
|limit|Integer|否|返回数量，默认10|10|
|contentType|String|否|内容类型筛选|video|
|days|Integer|否|最近N天，默认7|7|

##### 响应示例

```json
{
    "code": 200,
    "success": true,
    "message": "操作成功",
    "data": [
        {
            "id": 1,
            "title": "炼狱镇杰特进攻技巧合集",
            "coverImage": "http://example.com/contents/covers/1.jpg",
            "contentType": "video",
            "viewCount": 2580,
            "likeCount": 158,
            "author": "逃逸者",
            "publishTime": "2025-06-20 10:00:00"
        }
    ],
    "timestamp": 1719500000000
}
```

---

#### 5.2.4 获取推荐内容 🔓

##### 接口描述

根据用户偏好或当前浏览内容获取推荐。

##### 请求信息

- **URL**: `/content/recommend`
- **Method**: `GET`
- **需要认证**: 否
- **权限要求**: 无

##### 请求参数

|参数名|类型|必填|说明|示例|
|---|---|---|---|---|
|contentId|Long|否|基于此内容推荐|1|
|heroId|Long|否|基于英雄推荐|1|
|mapId|Long|否|基于地图推荐|1|
|limit|Integer|否|返回数量，默认5|5|

##### 响应示例

```json
{
    "code": 200,
    "success": true,
    "message": "操作成功",
    "data": [
        {
            "id": 2,
            "title": "炼狱镇防守要点解析",
            "coverImage": "http://example.com/contents/covers/2.jpg",
            "contentType": "video",
            "reason": "相同地图",
            "score": 0.85
        }
    ],
    "timestamp": 1719500000000
}
```

---

#### 5.2.5 分页查询内容 🔓

##### 接口描述

分页查询内容列表，支持多种筛选条件。

##### 请求信息

- **URL**: `/content/page`
- **Method**: `GET`
- **需要认证**: 否
- **权限要求**: 无

##### 请求参数

|参数名|类型|必填|说明|示例|
|---|---|---|---|---|
|current|Integer|否|当前页，默认1|1|
|size|Integer|否|每页大小，默认10|10|
|contentType|String|否|内容类型|video|
|heroId|Long|否|英雄ID|1|
|mapId|Long|否|地图ID|1|
|keyword|String|否|搜索关键词|技巧|
|orderBy|String|否|排序字段：time,view,like|view|

##### 响应示例

```json
{
    "code": 200,
    "success": true,
    "message": "操作成功",
    "data": {
        "records": [
            {
                "id": 1,
                "title": "炼狱镇杰特进攻技巧合集",
                "coverImage": "http://example.com/contents/covers/1.jpg",
                "contentType": "video",
                "viewCount": 2580,
                "publishTime": "2025-06-20 10:00:00"
            }
        ],
        "total": 50,
        "size": 10,
        "current": 1,
        "pages": 5
    },
    "timestamp": 1719500000000
}
```

---

### 5.3 标签（Tag）

#### 5.3.1 获取热门标签 🔓

##### 接口描述

获取使用次数最多的热门标签。

##### 请求信息

- **URL**: `/tag/hot`
- **Method**: `GET`
- **需要认证**: 否
- **权限要求**: 无

##### 请求参数

|参数名|类型|必填|说明|示例|
|---|---|---|---|---|
|limit|Integer|否|返回数量，默认10|10|

##### 响应示例

```json
{
    "code": 200,
    "success": true,
    "message": "操作成功",
    "data": [
        {
            "id": 1,
            "tagName": "进攻技巧",
            "tagType": "skill",
            "hotScore": 890,
            "color": "#FF5722"
        },
        {
            "id": 2,
            "tagName": "新手推荐",
            "tagType": "level",
            "hotScore": 756,
            "color": "#4CAF50"
        }
    ],
    "timestamp": 1719500000000
}
```

---

#### 5.3.2 按类型获取标签 🔓

##### 接口描述

根据标签类型获取标签列表。

##### 请求信息

- **URL**: `/tag/type/{tagType}`
- **Method**: `GET`
- **需要认证**: 否
- **权限要求**: 无

##### 路径参数

|参数名|类型|必填|说明|示例|
|---|---|---|---|---|
|tagType|String|是|标签类型|skill|

##### 标签类型说明

|类型|说明|
|---|---|
|skill|技巧类|
|level|难度类|
|scene|场景类|
|style|风格类|

##### 响应示例

```json
{
    "code": 200,
    "success": true,
    "message": "操作成功",
    "data": [
        {
            "id": 1,
            "tagName": "进攻技巧",
            "tagType": "skill",
            "hotScore": 890,
            "status": 1
        }
    ],
    "timestamp": 1719500000000
}
```

---

#### 5.3.3 搜索标签 🔓

##### 接口描述

根据关键词搜索标签。

##### 请求信息

- **URL**: `/tag/search`
- **Method**: `GET`
- **需要认证**: 否
- **权限要求**: 无

##### 请求参数

|参数名|类型|必填|说明|示例|
|---|---|---|---|---|
|keyword|String|是|搜索关键词|技巧|

##### 响应示例

```json
{
    "code": 200,
    "success": true,
    "message": "操作成功",
    "data": [
        {
            "id": 1,
            "tagName": "进攻技巧",
            "tagType": "skill",
            "matchScore": 1.0
        }
    ],
    "timestamp": 1719500000000
}
```

---

#### 5.3.4 获取内容标签 🔓

##### 接口描述

获取指定内容的所有标签。

##### 请求信息

- **URL**: `/tag/content/{contentId}`
- **Method**: `GET`
- **需要认证**: 否
- **权限要求**: 无

##### 路径参数

|参数名|类型|必填|说明|示例|
|---|---|---|---|---|
|contentId|Long|是|内容ID|1|

##### 响应示例

```json
{
    "code": 200,
    "success": true,
    "message": "操作成功",
    "data": [
        {
            "id": 1,
            "tagName": "进攻技巧",
            "tagType": "skill",
            "color": "#FF5722"
        },
        {
            "id": 5,
            "tagName": "炼狱镇",
            "tagType": "scene",
            "color": "#2196F3"
        }
    ],
    "timestamp": 1719500000000
}
```

---


## 6. 用户交互模块

### 6.1 收藏（Favorite）

#### 6.1.1 添加收藏 🔐

##### 接口描述

将内容、点位、英雄等添加到收藏夹。

##### 请求信息

- **URL**: `/favorite/add`
- **Method**: `POST`
- **需要认证**: 是
- **权限要求**: 登录用户

##### 请求头

```
Authorization: Bearer {your_token}
```

##### 请求参数

|参数名|类型|必填|说明|示例|
|---|---|---|---|---|
|targetType|String|是|收藏目标类型：content,position,hero,map,weapon|content|
|targetId|Long|是|收藏目标ID|1|
|folderName|String|否|收藏夹名称，默认"默认收藏夹"|我的视频|

##### 请求示例

```json
{
    "targetType": "content",
    "targetId": 1,
    "folderName": "教学视频"
}
```

##### 响应示例

```json
{
    "code": 200,
    "success": true,
    "message": "收藏成功",
    "data": {
        "favoriteId": 101,
        "createTime": "2025-06-27 10:00:00"
    },
    "timestamp": 1719500000000
}
```

##### 错误码

|错误码|说明|处理建议|
|---|---|---|
|5001|需要登录|引导用户登录|
|2002|已经收藏过了|提示用户|
|2001|收藏目标不存在|检查目标ID|

##### 业务说明

- 游客用户无法使用收藏功能
- 同一目标只能收藏一次
- 收藏成功后会更新目标的收藏数

---

#### 6.1.2 取消收藏 🔐

##### 接口描述

从收藏夹中移除指定内容。

##### 请求信息

- **URL**: `/favorite/remove`
- **Method**: `DELETE`
- **需要认证**: 是
- **权限要求**: 登录用户

##### 请求头

```
Authorization: Bearer {your_token}
```

##### 请求参数

|参数名|类型|必填|说明|示例|
|---|---|---|---|---|
|targetType|String|是|收藏目标类型|content|
|targetId|Long|是|收藏目标ID|1|

##### 请求示例

```json
{
    "targetType": "content",
    "targetId": 1
}
```

##### 响应示例

```json
{
    "code": 200,
    "success": true,
    "message": "取消收藏成功",
    "data": null,
    "timestamp": 1719500000000
}
```

##### 错误码

|错误码|说明|处理建议|
|---|---|---|
|5001|需要登录|引导用户登录|
|2001|未收藏该内容|刷新收藏状态|

---

#### 6.1.3 检查收藏状态 🔐

##### 接口描述

检查当前用户是否已收藏指定内容。

##### 请求信息

- **URL**: `/favorite/check`
- **Method**: `GET`
- **需要认证**: 是
- **权限要求**: 登录用户

##### 请求头

```
Authorization: Bearer {your_token}
```

##### 请求参数

|参数名|类型|必填|说明|示例|
|---|---|---|---|---|
|targetType|String|是|收藏目标类型|content|
|targetId|Long|是|收藏目标ID|1|

##### 请求示例

```
GET /api/favorite/check?targetType=content&targetId=1
```

##### 响应示例

```json
{
    "code": 200,
    "success": true,
    "message": "操作成功",
    "data": true,
    "timestamp": 1719500000000
}
```

##### 业务说明

- 游客用户始终返回false
- 可用于页面收藏按钮状态显示

---

#### 6.1.4 获取收藏列表 🔐

##### 接口描述

获取用户的收藏列表，支持按类型筛选。

##### 请求信息

- **URL**: `/favorite/list`
- **Method**: `GET`
- **需要认证**: 是
- **权限要求**: 登录用户

##### 请求头

```
Authorization: Bearer {your_token}
```

##### 请求参数

|参数名|类型|必填|说明|示例|
|---|---|---|---|---|
|targetType|String|是|收藏类型|content|

##### 请求示例

```
GET /api/favorite/list?targetType=content
```

##### 响应示例

```json
{
    "code": 200,
    "success": true,
    "message": "操作成功",
    "data": [
        {
            "id": 101,
            "userId": 1,
            "targetType": "content",
            "targetId": 1,
            "folderName": "教学视频",
            "createTime": "2025-06-27 10:00:00",
            "targetInfo": {
                "id": 1,
                "title": "炼狱镇杰特进攻技巧合集",
                "coverImage": "http://example.com/contents/covers/1.jpg",
                "contentType": "video",
                "viewCount": 2580,
                "author": "逃逸者"
            }
        },
        {
            "id": 102,
            "userId": 1,
            "targetType": "content",
            "targetId": 2,
            "folderName": "教学视频",
            "createTime": "2025-06-26 15:30:00",
            "targetInfo": {
                "id": 2,
                "title": "隐世修所防守要点",
                "coverImage": "http://example.com/contents/covers/2.jpg",
                "contentType": "article",
                "viewCount": 1890,
                "author": "资深玩家"
            }
        }
    ],
    "timestamp": 1719500000000
}
```

---

#### 6.1.5 获取收藏夹列表 🔐

##### 接口描述

获取用户创建的所有收藏夹。

##### 请求信息

- **URL**: `/favorite/folders`
- **Method**: `GET`
- **需要认证**: 是
- **权限要求**: 登录用户

##### 请求头

```
Authorization: Bearer {your_token}
```

##### 请求参数

无

##### 响应示例

```json
{
    "code": 200,
    "success": true,
    "message": "操作成功",
    "data": [
        {
            "folderName": "默认收藏夹",
            "count": 15,
            "createTime": "2025-06-01 10:00:00",
            "lastUpdateTime": "2025-06-27 10:00:00"
        },
        {
            "folderName": "教学视频",
            "count": 8,
            "createTime": "2025-06-15 14:00:00",
            "lastUpdateTime": "2025-06-26 15:30:00"
        },
        {
            "folderName": "点位收藏",
            "count": 25,
            "createTime": "2025-06-10 09:00:00",
            "lastUpdateTime": "2025-06-25 20:00:00"
        }
    ],
    "timestamp": 1719500000000
}
```

---

#### 6.1.6 移动收藏 🔐

##### 接口描述

将收藏项移动到其他收藏夹。

##### 请求信息

- **URL**: `/favorite/move`
- **Method**: `PUT`
- **需要认证**: 是
- **权限要求**: 登录用户

##### 请求头

```
Authorization: Bearer {your_token}
```

##### 请求参数

|参数名|类型|必填|说明|示例|
|---|---|---|---|---|
|favoriteId|Long|是|收藏记录ID|101|
|newFolder|String|是|新的收藏夹名称|精选内容|

##### 请求示例

```json
{
    "favoriteId": 101,
    "newFolder": "精选内容"
}
```

##### 响应示例

```json
{
    "code": 200,
    "success": true,
    "message": "移动成功",
    "data": null,
    "timestamp": 1719500000000
}
```

##### 业务说明

- 如果目标收藏夹不存在，会自动创建

---

### 补充接口

#### 批量操作

##### 批量添加收藏 🔐

- **URL**: `/favorite/batch-add`
- **Method**: `POST`

请求示例：

```json
{
    "targetType": "position",
    "targetIds": [1, 2, 3, 4, 5],
    "folderName": "常用点位"
}
```

##### 批量删除收藏 🔐

- **URL**: `/favorite/batch-remove`
- **Method**: `DELETE`

请求示例：

```json
{
    "favoriteIds": [101, 102, 103]
}
```

#### 收藏统计

##### 获取收藏统计 🔐

- **URL**: `/favorite/statistics`
- **Method**: `GET`

响应示例：

```json
{
    "code": 200,
    "success": true,
    "message": "操作成功",
    "data": {
        "totalCount": 48,
        "byType": {
            "content": 20,
            "position": 25,
            "hero": 3
        },
        "byFolder": {
            "默认收藏夹": 15,
            "教学视频": 8,
            "点位收藏": 25
        },
        "recentCount": 5
    },
    "timestamp": 1719500000000
}
```

---

### 6.2 浏览历史（History）

#### 6.2.1 添加浏览记录 🔐

##### 接口描述

记录用户的浏览历史（通常由前端在访问内容时自动调用）。

##### 请求信息

- **URL**: `/history/add`
- **Method**: `POST`
- **需要认证**: 是
- **权限要求**: 登录用户

##### 请求头

```
Authorization: Bearer {your_token}
```

##### 请求参数

|参数名|类型|必填|说明|示例|
|---|---|---|---|---|
|targetType|String|是|浏览目标类型|content|
|targetId|Long|是|浏览目标ID|1|
|duration|Integer|否|浏览时长（秒）|180|
|progress|Integer|否|进度百分比（视频类）|85|

##### 请求示例

```json
{
    "targetType": "content",
    "targetId": 1,
    "duration": 180,
    "progress": 85
}
```

##### 响应示例

```json
{
    "code": 200,
    "success": true,
    "message": "记录成功",
    "data": {
        "historyId": 201
    },
    "timestamp": 1719500000000
}
```

##### 业务说明

- 游客用户的浏览历史不会被记录
- 重复浏览会更新时间和进度
- 用于个性化推荐和继续观看功能

---

#### 6.2.2 获取浏览历史 🔐

##### 接口描述

分页获取用户的浏览历史记录。

##### 请求信息

- **URL**: `/history/list`
- **Method**: `GET`
- **需要认证**: 是
- **权限要求**: 登录用户

##### 请求头

```
Authorization: Bearer {your_token}
```

##### 请求参数

|参数名|类型|必填|说明|示例|
|---|---|---|---|---|
|current|Integer|否|当前页，默认1|1|
|size|Integer|否|每页大小，默认20|20|
|targetType|String|否|筛选类型|content|

##### 请求示例

```
GET /api/history/list?current=1&size=20&targetType=content
```

##### 响应示例

```json
{
    "code": 200,
    "success": true,
    "message": "操作成功",
    "data": {
        "records": [
            {
                "id": 201,
                "userId": 1,
                "targetType": "content",
                "targetId": 1,
                "viewDuration": 180,
                "progress": 85,
                "createTime": "2025-06-27 10:00:00",
                "updateTime": "2025-06-27 10:03:00",
                "targetInfo": {
                    "id": 1,
                    "title": "炼狱镇杰特进攻技巧合集",
                    "coverImage": "http://example.com/contents/covers/1.jpg",
                    "contentType": "video",
                    "duration": 210
                }
            },
            {
                "id": 200,
                "userId": 1,
                "targetType": "position",
                "targetId": 5,
                "viewDuration": 30,
                "progress": null,
                "createTime": "2025-06-27 09:45:00",
                "updateTime": "2025-06-27 09:45:30",
                "targetInfo": {
                    "id": 5,
                    "positionName": "隐世修所C点烟雾",
                    "mapName": "隐世修所",
                    "heroName": "烟雾"
                }
            }
        ],
        "total": 150,
        "size": 20,
        "current": 1,
        "pages": 8
    },
    "timestamp": 1719500000000
}
```

---

#### 6.2.3 删除浏览记录 🔐

##### 接口描述

删除指定的浏览历史记录。

##### 请求信息

- **URL**: `/history/delete`
- **Method**: `DELETE`
- **需要认证**: 是
- **权限要求**: 登录用户

##### 请求头

```
Authorization: Bearer {your_token}
```

##### 请求参数

|参数名|类型|必填|说明|示例|
|---|---|---|---|---|
|historyId|Long|是|历史记录ID|201|

##### 请求示例

```json
{
    "historyId": 201
}
```

##### 响应示例

```json
{
    "code": 200,
    "success": true,
    "message": "删除成功",
    "data": null,
    "timestamp": 1719500000000
}
```

---

#### 6.2.4 清空浏览历史 🔐

##### 接口描述

清空当前用户的所有浏览历史。

##### 请求信息

- **URL**: `/history/clear`
- **Method**: `DELETE`
- **需要认证**: 是
- **权限要求**: 登录用户

##### 请求头

```
Authorization: Bearer {your_token}
```

##### 请求参数

无

##### 响应示例

```json
{
    "code": 200,
    "success": true,
    "message": "清空成功",
    "data": {
        "deletedCount": 150
    },
    "timestamp": 1719500000000
}
```

##### 业务说明

- 此操作不可恢复
- 建议前端添加确认提示

---

### 补充接口

#### 历史记录高级功能

##### 获取今日历史 🔐

- **URL**: `/history/today`
- **Method**: `GET`

响应示例：

```json
{
    "code": 200,
    "success": true,
    "message": "操作成功",
    "data": [
        {
            "hour": "10:00",
            "items": [
                {
                    "id": 201,
                    "title": "炼狱镇杰特进攻技巧合集",
                    "targetType": "content",
                    "viewTime": "10:00:00"
                }
            ]
        }
    ],
    "timestamp": 1719500000000
}
```

##### 获取历史统计 🔐

- **URL**: `/history/statistics`
- **Method**: `GET`

响应示例：

```json
{
    "code": 200,
    "success": true,
    "message": "操作成功",
    "data": {
        "totalCount": 150,
        "todayCount": 12,
        "weekCount": 45,
        "byType": {
            "content": 80,
            "position": 60,
            "hero": 10
        },
        "totalDuration": 18500,
        "averageDuration": 123
    },
    "timestamp": 1719500000000
}
```

##### 批量删除历史 🔐

- **URL**: `/history/batch`
- **Method**: `DELETE`

请求示例：

```json
{
    "historyIds": [201, 202, 203]
}
```

---

## 7. 文件管理模块

### 7.1 上传文件 🔐

#### 接口描述

上传单个文件到服务器，支持本地存储和OSS存储。

#### 请求信息

- **URL**: `/file/upload`
- **Method**: `POST`
- **需要认证**: 是
- **权限要求**: 登录用户
- **Content-Type**: `multipart/form-data`

#### 请求头

```
Authorization: Bearer {your_token}
Content-Type: multipart/form-data
```

#### 请求参数

|参数名|类型|必填|说明|示例|
|---|---|---|---|---|
|file|File|是|文件对象|file.pdf|
|fileType|String|否|文件类型，默认document|document|

#### 文件类型说明

|类型|说明|支持格式|大小限制|
|---|---|---|---|
|image|图片|jpg,jpeg,png,gif,webp|10MB|
|video|视频|mp4,avi,mov,flv|100MB|
|document|文档|pdf,doc,docx,xls,xlsx|20MB|

#### 请求示例（Form Data）

```
file: [文件对象]
fileType: image
```

#### 响应示例

```json
{
    "code": 200,
    "success": true,
    "message": "上传成功",
    "data": "http://example.com/uploads/2025/06/27/xxx.jpg",
    "timestamp": 1719500000000
}
```

#### 错误码

|错误码|说明|处理建议|
|---|---|---|
|3001|文件上传失败|重试或检查网络|
|3002|文件类型不支持|查看支持的文件类型|
|3003|文件大小超限|压缩文件或分片上传|
|5001|需要登录|引导用户登录|

#### 业务说明

- 游客用户无法上传文件
- 文件名会自动重命名以避免冲突
- 返回的URL可直接用于访问文件

---

### 7.2 批量上传文件 🔐

#### 接口描述

一次上传多个文件。

#### 请求信息

- **URL**: `/file/upload-batch`
- **Method**: `POST`
- **需要认证**: 是
- **权限要求**: 登录用户
- **Content-Type**: `multipart/form-data`

#### 请求头

```
Authorization: Bearer {your_token}
Content-Type: multipart/form-data
```

#### 请求参数

|参数名|类型|必填|说明|示例|
|---|---|---|---|---|
|files|File[]|是|文件数组|[file1.jpg, file2.jpg]|
|fileType|String|否|文件类型，默认document|image|

#### 请求示例（Form Data）

```
files: [文件1]
files: [文件2]
files: [文件3]
fileType: image
```

#### 响应示例

```json
{
    "code": 200,
    "success": true,
    "message": "上传成功",
    "data": [
        "http://example.com/uploads/2025/06/27/xxx1.jpg",
        "http://example.com/uploads/2025/06/27/xxx2.jpg",
        "http://example.com/uploads/2025/06/27/xxx3.jpg"
    ],
    "timestamp": 1719500000000
}
```

#### 错误码

|错误码|说明|处理建议|
|---|---|---|
|3001|部分文件上传失败|查看具体失败原因|
|3004|批量上传数量超限|最多同时上传10个文件|

#### 业务说明

- 单次最多上传10个文件
- 如果部分文件失败，会返回成功的文件URL列表

---

### 7.3 上传图片 🔐

#### 接口描述

专门的图片上传接口，支持自动生成缩略图。

#### 请求信息

- **URL**: `/file/upload-image`
- **Method**: `POST`
- **需要认证**: 是
- **权限要求**: 登录用户
- **Content-Type**: `multipart/form-data`

#### 请求头

```
Authorization: Bearer {your_token}
Content-Type: multipart/form-data
```

#### 请求参数

|参数名|类型|必填|说明|示例|
|---|---|---|---|---|
|file|File|是|图片文件|image.jpg|
|generateThumbnail|Boolean|否|是否生成缩略图，默认true|true|

#### 请求示例（Form Data）

```
file: [图片文件]
generateThumbnail: true
```

#### 响应示例

```json
{
    "code": 200,
    "success": true,
    "message": "图片上传成功",
    "data": {
        "originalUrl": "http://example.com/uploads/images/2025/06/27/xxx.jpg",
        "thumbnailUrl": "http://example.com/uploads/images/2025/06/27/xxx_thumb.jpg",
        "width": 1920,
        "height": 1080,
        "fileSize": 2048576,
        "format": "JPEG"
    },
    "timestamp": 1719500000000
}
```

#### 错误码

|错误码|说明|处理建议|
|---|---|---|
|3002|不是有效的图片文件|仅支持jpg,png,gif,webp|
|3003|图片大小超限|图片不能超过10MB|
|3005|图片尺寸过大|最大支持4K分辨率|

#### 业务说明

- 支持的图片格式：jpg, jpeg, png, gif, webp
- 缩略图默认尺寸：300x300（保持比例）
- 会自动压缩过大的图片

---

### 7.4 上传视频 🔐

#### 接口描述

上传视频文件，支持大文件和断点续传。

#### 请求信息

- **URL**: `/file/upload-video`
- **Method**: `POST`
- **需要认证**: 是
- **权限要求**: 登录用户（内容管理员及以上）
- **Content-Type**: `multipart/form-data`

#### 请求头

```
Authorization: Bearer {your_token}
Content-Type: multipart/form-data
```

#### 请求参数

|参数名|类型|必填|说明|示例|
|---|---|---|---|---|
|file|File|是|视频文件|video.mp4|

#### 请求示例（Form Data）

```
file: [视频文件]
```

#### 响应示例

```json
{
    "code": 200,
    "success": true,
    "message": "视频上传成功",
    "data": {
        "videoUrl": "http://example.com/uploads/videos/2025/06/27/xxx.mp4",
        "coverImage": "http://example.com/uploads/videos/2025/06/27/xxx_cover.jpg",
        "duration": 180,
        "fileSize": 52428800,
        "resolution": "1920x1080",
        "format": "MP4",
        "bitrate": 2500
    },
    "timestamp": 1719500000000
}
```

#### 错误码

|错误码|说明|处理建议|
|---|---|---|
|3002|视频格式不支持|仅支持mp4,avi,mov,flv|
|3003|视频大小超限|视频不能超过100MB|
|403|权限不足|需要内容管理员权限|

#### 业务说明

- 仅内容管理员及以上权限可上传视频
- 自动生成视频封面图
- 支持的视频格式：mp4, avi, mov, flv

---

### 7.5 删除文件 🔐

#### 接口描述

删除已上传的文件。

#### 请求信息

- **URL**: `/file/delete`
- **Method**: `DELETE`
- **需要认证**: 是
- **权限要求**: 文件所有者或管理员

#### 请求头

```
Authorization: Bearer {your_token}
```

#### 请求参数

|参数名|类型|必填|说明|示例|
|---|---|---|---|---|
|fileUrl|String|是|文件URL|http://example.com/uploads/xxx.jpg|

#### 请求示例

```
DELETE /api/file/delete?fileUrl=http://example.com/uploads/xxx.jpg
```

#### 响应示例

```json
{
    "code": 200,
    "success": true,
    "message": "删除成功",
    "data": null,
    "timestamp": 1719500000000
}
```

#### 错误码

|错误码|说明|处理建议|
|---|---|---|
|403|无权删除该文件|只能删除自己的文件|
|2001|文件不存在|文件可能已被删除|

#### 业务说明

- 只能删除自己上传的文件
- 管理员可以删除任何文件
- 删除后文件URL将无法访问

---

### 补充接口

#### 文件管理高级功能

##### 获取用户文件列表 🔐

- **URL**: `/file/user-files`
- **Method**: `GET`

请求参数：

|参数名|类型|必填|说明|示例|
|---|---|---|---|---|
|fileType|String|否|文件类型筛选|image|
|current|Integer|否|当前页，默认1|1|
|size|Integer|否|每页大小，默认10|10|

响应示例：

```json
{
    "code": 200,
    "success": true,
    "message": "操作成功",
    "data": {
        "records": [
            {
                "id": 1,
                "fileName": "教学截图.jpg",
                "fileUrl": "http://example.com/uploads/images/xxx.jpg",
                "fileType": "image",
                "fileSize": 1048576,
                "uploadTime": "2025-06-27 10:00:00",
                "usageCount": 3
            }
        ],
        "total": 25,
        "size": 10,
        "current": 1,
        "pages": 3
    },
    "timestamp": 1719500000000
}
```

##### 获取存储统计 🔐

- **URL**: `/file/storage-stats`
- **Method**: `GET`

响应示例：

```json
{
    "code": 200,
    "success": true,
    "message": "操作成功",
    "data": {
        "totalSize": 524288000,
        "usedSize": 104857600,
        "remainSize": 419430400,
        "fileCount": {
            "total": 50,
            "image": 35,
            "video": 10,
            "document": 5
        },
        "sizeByType": {
            "image": 52428800,
            "video": 41943040,
            "document": 10485760
        },
        "storageLimit": 524288000
    },
    "timestamp": 1719500000000
}
```

##### 批量删除文件 🔐

- **URL**: `/file/batch-delete`
- **Method**: `DELETE`

请求示例：

```json
{
    "fileUrls": [
        "http://example.com/uploads/xxx1.jpg",
        "http://example.com/uploads/xxx2.jpg"
    ]
}
```

#### 文件访问控制

##### 生成临时访问链接 🔐

- **URL**: `/file/temp-url`
- **Method**: `POST`

请求参数：

|参数名|类型|必填|说明|示例|
|---|---|---|---|---|
|fileUrl|String|是|原始文件URL|http://example.com/uploads/xxx.jpg|
|expireMinutes|Integer|否|过期时间（分钟），默认60|60|

响应示例：

```json
{
    "code": 200,
    "success": true,
    "message": "操作成功",
    "data": {
        "tempUrl": "http://example.com/uploads/xxx.jpg?token=xxx&expire=1719503600000",
        "expireTime": "2025-06-27 11:00:00"
    },
    "timestamp": 1719500000000
}
```

---

### 文件上传最佳实践

#### 前端上传流程建议

1. **上传前验证**
    
    - 检查文件类型
    - 检查文件大小
    - 图片预览
2. **上传中处理**
    
    - 显示上传进度
    - 支持取消上传
    - 错误重试机制
3. **上传后处理**
    
    - 显示上传结果
    - 自动填充URL到表单
    - 支持删除已上传文件

#### 大文件上传说明

对于超过50MB的视频文件，建议：

1. 使用分片上传（暂未实现）
2. 显示详细进度条
3. 支持断点续传
4. 上传前压缩处理

---
好的，我来编写第8章节「管理员模块」的详细接口文档。

## 8. 管理员模块 👑

> 注意：本章节所有接口都需要管理员权限（CONTENT_ADMIN、ADMIN或SUPER_ADMIN）

### 8.1 用户管理

#### 8.1.1 分页查询用户列表 👑

##### 接口描述

管理员查询系统中的用户列表，支持搜索和筛选。

##### 请求信息

- **URL**: `/user/list`
- **Method**: `GET`
- **需要认证**: 是
- **权限要求**: 管理员

##### 请求头

```
Authorization: Bearer {your_token}
```

##### 请求参数

|参数名|类型|必填|说明|示例|
|---|---|---|---|---|
|current|Integer|否|当前页，默认1|1|
|size|Integer|否|每页大小，默认10|10|
|keyword|String|否|搜索关键词（用户名/邮箱/昵称）|escape|
|status|Integer|否|用户状态：0-禁用，1-正常|1|

##### 请求示例

```
GET /api/user/list?current=1&size=10&keyword=escape&status=1
```

##### 响应示例

```json
{
    "code": 200,
    "success": true,
    "message": "操作成功",
    "data": {
        "records": [
            {
                "id": 1,
                "username": "escape",
                "email": "user@example.com",
                "nickname": "逃逸者",
                "avatar": "http://example.com/avatars/1.jpg",
                "phone": "138****8000",
                "status": 1,
                "emailVerified": 1,
                "roles": ["USER", "CONTENT_ADMIN"],
                "lastLoginTime": "2025-06-27 10:00:00",
                "lastLoginIp": "127.0.0.1",
                "createTime": "2025-06-01 10:00:00"
            }
        ],
        "total": 150,
        "size": 10,
        "current": 1,
        "pages": 15
    },
    "timestamp": 1719500000000
}
```

##### 权限说明

- CONTENT_ADMIN：只能查看普通用户
- ADMIN：可以查看所有用户
- SUPER_ADMIN：可以查看所有用户

---

#### 8.1.2 更新用户状态 👑

##### 接口描述

启用或禁用用户账号。

##### 请求信息

- **URL**: `/user/{userId}/status`
- **Method**: `PUT`
- **需要认证**: 是
- **权限要求**: ADMIN及以上

##### 请求头

```
Authorization: Bearer {your_token}
```

##### 路径参数

|参数名|类型|必填|说明|示例|
|---|---|---|---|---|
|userId|Long|是|用户ID|1|

##### 请求参数

|参数名|类型|必填|说明|示例|
|---|---|---|---|---|
|status|Integer|是|状态：0-禁用，1-启用|0|

##### 请求示例

```json
{
    "status": 0
}
```

##### 响应示例

```json
{
    "code": 200,
    "success": true,
    "message": "用户已禁用",
    "data": null,
    "timestamp": 1719500000000
}
```

##### 业务说明

- 禁用的用户无法登录系统
- 不能禁用自己的账号
- 不能禁用超级管理员

---

#### 8.1.3 分配用户角色 👑

##### 接口描述

为用户分配角色权限。

##### 请求信息

- **URL**: `/user/{userId}/roles`
- **Method**: `POST`
- **需要认证**: 是
- **权限要求**: SUPER_ADMIN

##### 请求头

```
Authorization: Bearer {your_token}
```

##### 路径参数

|参数名|类型|必填|说明|示例|
|---|---|---|---|---|
|userId|Long|是|用户ID|1|

##### 请求参数

|参数名|类型|必填|说明|示例|
|---|---|---|---|---|
|roleKey|String|是|角色标识|CONTENT_ADMIN|

##### 角色标识说明

|标识|名称|说明|
|---|---|---|
|USER|普通用户|基本权限|
|CONTENT_ADMIN|内容管理员|内容管理权限|
|ADMIN|管理员|用户管理权限|
|SUPER_ADMIN|超级管理员|所有权限|

##### 请求示例

```
POST /api/user/1/roles?roleKey=CONTENT_ADMIN
```

##### 响应示例

```json
{
    "code": 200,
    "success": true,
    "message": "角色分配成功",
    "data": null,
    "timestamp": 1719500000000
}
```

---

#### 8.1.4 获取用户统计 👑

##### 接口描述

获取用户相关的统计数据。

##### 请求信息

- **URL**: `/user/statistics`
- **Method**: `GET`
- **需要认证**: 是
- **权限要求**: ADMIN及以上

##### 请求头

```
Authorization: Bearer {your_token}
```

##### 请求参数

无

##### 响应示例

```json
{
    "code": 200,
    "success": true,
    "message": "操作成功",
    "data": {
        "totalUsers": 1500,
        "activeUsers": 1350,
        "disabledUsers": 150,
        "todayNewUsers": 25,
        "weekNewUsers": 180,
        "monthNewUsers": 650,
        "usersByRole": {
            "USER": 1400,
            "CONTENT_ADMIN": 80,
            "ADMIN": 15,
            "SUPER_ADMIN": 5
        },
        "onlineUsers": 125,
        "userGrowthTrend": [
            {
                "date": "2025-06-01",
                "count": 20
            },
            {
                "date": "2025-06-02",
                "count": 35
            }
        ]
    },
    "timestamp": 1719500000000
}
```

---

### 8.2 内容管理

#### 8.2.1 创建英雄 👑

##### 接口描述

添加新的英雄角色。

##### 请求信息

- **URL**: `/hero`
- **Method**: `POST`
- **需要认证**: 是
- **权限要求**: CONTENT_ADMIN及以上

##### 请求头

```
Authorization: Bearer {your_token}
```

##### 请求参数

|参数名|类型|必填|说明|示例|
|---|---|---|---|---|
|heroKey|String|是|英雄标识（英文）|gekko|
|heroName|String|是|英雄名称（中文）|盖柯|
|heroType|String|是|英雄类型|initiator|
|avatar|String|是|头像URL|http://example.com/heroes/gekko.png|
|description|String|是|英雄描述|来自洛杉矶的盖柯...|
|difficulty|Integer|是|难度等级1-5|2|
|sortOrder|Integer|否|排序顺序|20|

##### 请求示例

```json
{
    "heroKey": "gekko",
    "heroName": "盖柯",
    "heroType": "initiator",
    "avatar": "http://example.com/heroes/gekko.png",
    "description": "来自洛杉矶的盖柯带领着一支由紧密相连的生物组成的小队",
    "difficulty": 2,
    "sortOrder": 20
}
```

##### 响应示例

```json
{
    "code": 200,
    "success": true,
    "message": "英雄创建成功",
    "data": {
        "heroId": 25
    },
    "timestamp": 1719500000000
}
```

##### 错误码

|错误码|说明|处理建议|
|---|---|---|
|2002|英雄标识已存在|更换标识|
|400|参数格式错误|检查参数|

---

#### 8.2.2 更新英雄状态 👑

##### 接口描述

启用或禁用英雄。

##### 请求信息

- **URL**: `/hero/{heroId}/status`
- **Method**: `PUT`
- **需要认证**: 是
- **权限要求**: CONTENT_ADMIN及以上

##### 请求头

```
Authorization: Bearer {your_token}
```

##### 路径参数

|参数名|类型|必填|说明|示例|
|---|---|---|---|---|
|heroId|Long|是|英雄ID|1|

##### 请求参数

|参数名|类型|必填|说明|示例|
|---|---|---|---|---|
|status|Integer|是|状态：0-禁用，1-启用|1|

##### 请求示例

```
PUT /api/hero/1/status?status=1
```

##### 响应示例

```json
{
    "code": 200,
    "success": true,
    "message": "英雄已启用",
    "data": null,
    "timestamp": 1719500000000
}
```

---

#### 8.2.3 创建地图 👑

##### 接口描述

添加新的游戏地图。

##### 请求信息

- **URL**: `/map`
- **Method**: `POST`
- **需要认证**: 是
- **权限要求**: CONTENT_ADMIN及以上

##### 请求头

```
Authorization: Bearer {your_token}
```

##### 请求参数

|参数名|类型|必填|说明|示例|
|---|---|---|---|---|
|mapKey|String|是|地图标识（英文）|sunset|
|mapName|String|是|地图名称（中文）|日落之城|
|mapType|String|是|地图类型|standard|
|minimap|String|是|小地图URL|http://example.com/maps/sunset_mini.jpg|
|overview|String|是|俯视图URL|http://example.com/maps/sunset_overview.jpg|
|description|String|是|地图描述|位于洛杉矶的地图...|
|sites|String|是|炸弹点位|A,B|
|sortOrder|Integer|否|排序顺序|10|

##### 请求示例

```json
{
    "mapKey": "sunset",
    "mapName": "日落之城",
    "mapType": "standard",
    "minimap": "http://example.com/maps/sunset_mini.jpg",
    "overview": "http://example.com/maps/sunset_overview.jpg",
    "description": "位于洛杉矶的标准地图，拥有独特的中路设计",
    "sites": "A,B",
    "sortOrder": 10
}
```

##### 响应示例

```json
{
    "code": 200,
    "success": true,
    "message": "地图创建成功",
    "data": {
        "mapId": 8
    },
    "timestamp": 1719500000000
}
```

---

#### 8.2.4 创建点位 👑

##### 接口描述

添加新的战术点位。

##### 请求信息

- **URL**: `/position`
- **Method**: `POST`
- **需要认证**: 是
- **权限要求**: CONTENT_ADMIN及以上

##### 请求头

```
Authorization: Bearer {your_token}
```

##### 请求参数

|参数名|类型|必填|说明|示例|
|---|---|---|---|---|
|mapId|Long|是|地图ID|1|
|heroId|Long|否|英雄ID（通用点位可不填）|1|
|positionName|String|是|点位名称|A点天堂烟雾|
|positionType|String|是|点位类型|smoke|
|side|String|是|攻防方|attack|
|site|String|是|站点|A|
|difficulty|Integer|是|难度1-5|2|
|description|String|是|点位描述|这个烟雾可以...|
|setupImage|String|是|准备位置图|http://example.com/setup.jpg|
|throwImage|String|是|投掷位置图|http://example.com/throw.jpg|
|landingImage|String|是|落点位置图|http://example.com/landing.jpg|

##### 请求示例

```json
{
    "mapId": 1,
    "heroId": 1,
    "positionName": "A点天堂烟雾",
    "positionType": "smoke",
    "side": "attack",
    "site": "A",
    "difficulty": 2,
    "description": "这个烟雾可以完美封锁A点天堂位置的视野",
    "setupImage": "http://example.com/positions/setup.jpg",
    "throwImage": "http://example.com/positions/throw.jpg",
    "landingImage": "http://example.com/positions/landing.jpg"
}
```

##### 响应示例

```json
{
    "code": 200,
    "success": true,
    "message": "点位创建成功",
    "data": {
        "positionId": 100
    },
    "timestamp": 1719500000000
}
```

---

#### 8.2.5 发布内容 👑

##### 接口描述

发布新的教学内容（视频或图文）。

##### 请求信息

- **URL**: `/content/publish`
- **Method**: `POST`
- **需要认证**: 是
- **权限要求**: CONTENT_ADMIN及以上

##### 请求头

```
Authorization: Bearer {your_token}
```

##### 请求参数

|参数名|类型|必填|说明|示例|
|---|---|---|---|---|
|contentType|String|是|内容类型：video,article,mixed|video|
|title|String|是|标题|炼狱镇进攻教学|
|description|String|是|描述|详细讲解炼狱镇...|
|coverImage|String|是|封面图URL|http://example.com/cover.jpg|
|positionId|Long|否|关联点位ID|1|
|heroId|Long|否|关联英雄ID|1|
|mapId|Long|否|关联地图ID|1|
|videoUrl|String|视频必填|视频URL|http://example.com/video.mp4|
|contentBody|String|图文必填|图文内容（Markdown）|# 标题...|
|tags|String[]|否|标签列表|["教学","进攻"]|
|isFeatured|Integer|否|是否精选|1|

##### 请求示例（视频）

```json
{
    "contentType": "video",
    "title": "炼狱镇杰特进攻完整教学",
    "description": "从基础到进阶，全面讲解炼狱镇杰特的进攻打法",
    "coverImage": "http://example.com/covers/content.jpg",
    "positionId": 1,
    "heroId": 1,
    "mapId": 1,
    "videoUrl": "http://example.com/videos/tutorial.mp4",
    "tags": ["教学", "进攻", "杰特", "炼狱镇"],
    "isFeatured": 1
}
```

##### 响应示例

```json
{
    "code": 200,
    "success": true,
    "message": "内容发布成功",
    "data": {
        "contentId": 50
    },
    "timestamp": 1719500000000
}
```

---

#### 8.2.6 内容审核 👑

##### 接口描述

审核用户投稿的内容。

##### 请求信息

- **URL**: `/content/{contentId}/review`
- **Method**: `PUT`
- **需要认证**: 是
- **权限要求**: CONTENT_ADMIN及以上

##### 请求头

```
Authorization: Bearer {your_token}
```

##### 路径参数

|参数名|类型|必填|说明|示例|
|---|---|---|---|---|
|contentId|Long|是|内容ID|50|

##### 请求参数

|参数名|类型|必填|说明|示例|
|---|---|---|---|---|
|status|Integer|是|审核状态：1-通过，2-拒绝|1|
|reason|String|拒绝时必填|拒绝原因|内容质量不符合要求|

##### 请求示例

```json
{
    "status": 1,
    "reason": ""
}
```

##### 响应示例

```json
{
    "code": 200,
    "success": true,
    "message": "审核完成",
    "data": null,
    "timestamp": 1719500000000
}
```

---

### 8.3 标签管理

#### 8.3.1 创建标签 👑

##### 接口描述

创建新的内容标签。

##### 请求信息

- **URL**: `/tag`
- **Method**: `POST`
- **需要认证**: 是
- **权限要求**: CONTENT_ADMIN及以上

##### 请求头

```
Authorization: Bearer {your_token}
```

##### 请求参数

|参数名|类型|必填|说明|示例|
|---|---|---|---|---|
|tagName|String|是|标签名称|新手教学|
|tagType|String|是|标签类型|skill|
|color|String|否|标签颜色|#4CAF50|
|sortOrder|Integer|否|排序顺序|1|

##### 请求示例

```json
{
    "tagName": "新手教学",
    "tagType": "skill",
    "color": "#4CAF50",
    "sortOrder": 1
}
```

##### 响应示例

```json
{
    "code": 200,
    "success": true,
    "message": "标签创建成功",
    "data": {
        "tagId": 30
    },
    "timestamp": 1719500000000
}
```

---

#### 8.3.2 更新标签 👑

##### 接口描述

更新标签信息。

##### 请求信息

- **URL**: `/tag/{tagId}`
- **Method**: `PUT`
- **需要认证**: 是
- **权限要求**: CONTENT_ADMIN及以上

##### 请求头

```
Authorization: Bearer {your_token}
```

##### 路径参数

|参数名|类型|必填|说明|示例|
|---|---|---|---|---|
|tagId|Long|是|标签ID|30|

##### 请求参数

同创建标签

##### 响应示例

```json
{
    "code": 200,
    "success": true,
    "message": "标签更新成功",
    "data": null,
    "timestamp": 1719500000000
}
```

---

#### 8.3.3 删除标签 👑

##### 接口描述

删除指定标签。

##### 请求信息

- **URL**: `/tag/{tagId}`
- **Method**: `DELETE`
- **需要认证**: 是
- **权限要求**: CONTENT_ADMIN及以上

##### 请求头

```
Authorization: Bearer {your_token}
```

##### 路径参数

|参数名|类型|必填|说明|示例|
|---|---|---|---|---|
|tagId|Long|是|标签ID|30|

##### 响应示例

```json
{
    "code": 200,
    "success": true,
    "message": "标签删除成功",
    "data": null,
    "timestamp": 1719500000000
}
```

##### 业务说明

- 删除标签会同时解除所有内容的标签关联

---

#### 8.3.4 设置内容标签 👑

##### 接口描述

为内容设置标签。

##### 请求信息

- **URL**: `/tag/content/{contentId}/tags`
- **Method**: `POST`
- **需要认证**: 是
- **权限要求**: CONTENT_ADMIN及以上

##### 请求头

```
Authorization: Bearer {your_token}
```

##### 路径参数

|参数名|类型|必填|说明|示例|
|---|---|---|---|---|
|contentId|Long|是|内容ID|50|

##### 请求参数

|参数名|类型|必填|说明|示例|
|---|---|---|---|---|
|tagIds|Long[]|是|标签ID列表|[1,2,3]|

##### 请求示例

```json
{
    "tagIds": [1, 2, 3, 5, 8]
}
```

##### 响应示例

```json
{
    "code": 200,
    "success": true,
    "message": "标签设置成功",
    "data": null,
    "timestamp": 1719500000000
}
```

##### 业务说明

- 会覆盖原有的所有标签
- 传空数组可清除所有标签

---

### 补充接口

#### 数据统计看板 👑

##### 获取管理员仪表盘数据

- **URL**: `/admin/dashboard`
- **Method**: `GET`

响应示例：

```json
{
    "code": 200,
    "success": true,
    "message": "操作成功",
    "data": {
        "overview": {
            "totalUsers": 1500,
            "todayActiveUsers": 350,
            "totalContents": 680,
            "todayNewContents": 15,
            "totalPositions": 450,
            "totalViews": 158000
        },
        "recentData": {
            "newUsers": 25,
            "newContents": 8,
            "newFavorites": 125,
            "newComments": 0
        },
        "charts": {
            "userGrowth": [...],
            "contentTrend": [...],
            "popularContent": [...],
            "activeHours": [...]
        }
    },
    "timestamp": 1719500000000
}
```

#### 批量操作接口 👑

##### 批量更新内容状态

- **URL**: `/content/batch-status`
- **Method**: `PUT`

请求示例：

```json
{
    "contentIds": [50, 51, 52],
    "status": 1
}
```

##### 批量删除用户

- **URL**: `/user/batch-delete`
- **Method**: `DELETE`
- **权限要求**: SUPER_ADMIN

请求示例：

```json
{
    "userIds": [100, 101, 102]
}
```

---

## 9. 搜索模块

### 9.1 全局搜索 🔓

#### 接口描述

全站内容搜索，支持搜索英雄、地图、武器、点位、内容等。

#### 请求信息

- **URL**: `/search/global`
- **Method**: `GET`
- **需要认证**: 否
- **权限要求**: 无

#### 请求参数

|参数名|类型|必填|说明|示例|
|---|---|---|---|---|
|keyword|String|是|搜索关键词|杰特|
|current|Integer|否|当前页，默认1|1|
|size|Integer|否|每页大小，默认10|10|

#### 请求示例

```
GET /api/search/global?keyword=杰特&current=1&size=10
```

#### 响应示例

```json
{
    "code": 200,
    "success": true,
    "message": "操作成功",
    "data": {
        "keyword": "杰特",
        "searchTime": 35,
        "totalResults": 42,
        "results": {
            "heroes": {
                "count": 1,
                "items": [
                    {
                        "id": 1,
                        "heroKey": "jett",
                        "heroName": "杰特",
                        "heroType": "duelist",
                        "avatar": "http://example.com/heroes/jett.png",
                        "matchScore": 1.0,
                        "highlight": "<em>杰特</em>"
                    }
                ]
            },
            "positions": {
                "count": 15,
                "items": [
                    {
                        "id": 1,
                        "positionName": "炼狱镇杰特A点烟雾",
                        "mapName": "炼狱镇",
                        "heroName": "杰特",
                        "positionType": "smoke",
                        "matchScore": 0.95,
                        "highlight": "炼狱镇<em>杰特</em>A点烟雾"
                    }
                ]
            },
            "contents": {
                "count": 20,
                "items": [
                    {
                        "id": 1,
                        "title": "杰特进阶技巧教学",
                        "contentType": "video",
                        "coverImage": "http://example.com/covers/1.jpg",
                        "author": "逃逸者",
                        "viewCount": 2580,
                        "matchScore": 0.88,
                        "highlight": "<em>杰特</em>进阶技巧教学"
                    }
                ]
            },
            "maps": {
                "count": 0,
                "items": []
            },
            "weapons": {
                "count": 0,
                "items": []
            },
            "tags": {
                "count": 5,
                "items": [
                    {
                        "id": 10,
                        "tagName": "杰特技巧",
                        "tagType": "skill",
                        "hotScore": 156,
                        "matchScore": 0.9,
                        "highlight": "<em>杰特</em>技巧"
                    }
                ]
            }
        },
        "pagination": {
            "current": 1,
            "size": 10,
            "total": 42,
            "pages": 5
        }
    },
    "timestamp": 1719500000000
}
```

#### 业务说明

- 搜索结果按类型分组展示
- 支持中文分词搜索
- 搜索结果带有匹配度评分
- 关键词高亮显示（用`<em>`标签包裹）
- 空结果的类型不返回

---

### 9.2 获取搜索建议 🔓

#### 接口描述

根据用户输入实时返回搜索建议，用于搜索框自动完成。

#### 请求信息

- **URL**: `/search/suggestions`
- **Method**: `GET`
- **需要认证**: 否
- **权限要求**: 无

#### 请求参数

|参数名|类型|必填|说明|示例|
|---|---|---|---|---|
|keyword|String|是|搜索关键词（至少1个字符）|杰|

#### 请求示例

```
GET /api/search/suggestions?keyword=杰
```

#### 响应示例

```json
{
    "code": 200,
    "success": true,
    "message": "操作成功",
    "data": [
        {
            "text": "杰特",
            "type": "hero",
            "icon": "👤",
            "description": "决斗者英雄",
            "searchCount": 1250
        },
        {
            "text": "杰特进攻技巧",
            "type": "content",
            "icon": "📹",
            "description": "热门视频",
            "searchCount": 890
        },
        {
            "text": "杰特飞刀",
            "type": "keyword",
            "icon": "🔍",
            "description": "热门搜索",
            "searchCount": 650
        },
        {
            "text": "杰特烟雾点位",
            "type": "position",
            "icon": "📍",
            "description": "点位搜索",
            "searchCount": 450
        },
        {
            "text": "杰特大招使用",
            "type": "tag",
            "icon": "🏷️",
            "description": "相关标签",
            "searchCount": 320
        }
    ],
    "timestamp": 1719500000000
}
```

#### 业务说明

- 最多返回10条建议
- 按搜索热度排序
- 实时响应，建议做防抖处理
- 支持拼音搜索（如"jt"可以搜到"杰特"）

---

### 9.3 获取热门搜索 🔓

#### 接口描述

获取当前热门搜索关键词列表。

#### 请求信息

- **URL**: `/search/hot`
- **Method**: `GET`
- **需要认证**: 否
- **权限要求**: 无

#### 请求参数

|参数名|类型|必填|说明|示例|
|---|---|---|---|---|
|limit|Integer|否|返回数量，默认10，最大20|10|

#### 请求示例

```
GET /api/search/hot?limit=10
```

#### 响应示例

```json
{
    "code": 200,
    "success": true,
    "message": "操作成功",
    "data": [
        {
            "rank": 1,
            "keyword": "新英雄盖柯",
            "searchCount": 5680,
            "trend": "up",
            "trendPercent": 125.5,
            "tag": "🔥"
        },
        {
            "rank": 2,
            "keyword": "炼狱镇点位",
            "searchCount": 4520,
            "trend": "up",
            "trendPercent": 35.2,
            "tag": "HOT"
        },
        {
            "rank": 3,
            "keyword": "杰特技巧",
            "searchCount": 3890,
            "trend": "stable",
            "trendPercent": 2.1,
            "tag": null
        },
        {
            "rank": 4,
            "keyword": "隐世修所防守",
            "searchCount": 3200,
            "trend": "down",
            "trendPercent": -15.3,
            "tag": null
        },
        {
            "rank": 5,
            "keyword": "新手教学",
            "searchCount": 2950,
            "trend": "up",
            "trendPercent": 18.6,
            "tag": "新"
        },
        {
            "rank": 6,
            "keyword": "暴徒皮肤",
            "searchCount": 2800,
            "trend": "stable",
            "trendPercent": 0.5,
            "tag": null
        },
        {
            "rank": 7,
            "keyword": "贤者墙位置",
            "searchCount": 2650,
            "trend": "up",
            "trendPercent": 22.3,
            "tag": null
        },
        {
            "rank": 8,
            "keyword": "蝰蛇阵容",
            "searchCount": 2500,
            "trend": "up",
            "trendPercent": 45.8,
            "tag": null
        },
        {
            "rank": 9,
            "keyword": "经济局打法",
            "searchCount": 2350,
            "trend": "stable",
            "trendPercent": -3.2,
            "tag": null
        },
        {
            "rank": 10,
            "keyword": "雷兹大招",
            "searchCount": 2200,
            "trend": "down",
            "trendPercent": -28.5,
            "tag": null
        }
    ],
    "timestamp": 1719500000000
}
```

#### 字段说明

|字段|说明|
|---|---|
|trend|趋势：up-上升，down-下降，stable-平稳|
|trendPercent|相比昨日变化百分比|
|tag|特殊标签：🔥-爆热，HOT-热门，新-新内容|

#### 业务说明

- 基于最近24小时的搜索数据
- 每小时更新一次
- 可用于首页热搜榜展示

---

### 9.4 获取搜索历史 🔐

#### 接口描述

获取当前用户的搜索历史记录。

#### 请求信息

- **URL**: `/search/history`
- **Method**: `GET`
- **需要认证**: 是
- **权限要求**: 登录用户

#### 请求头

```
Authorization: Bearer {your_token}
```

#### 请求参数

|参数名|类型|必填|说明|示例|
|---|---|---|---|---|
|limit|Integer|否|返回数量，默认10，最大50|10|

#### 请求示例

```
GET /api/search/history?limit=10
```

#### 响应示例

```json
{
    "code": 200,
    "success": true,
    "message": "操作成功",
    "data": [
        {
            "id": 101,
            "keyword": "杰特技巧",
            "searchTime": "2025-06-27 10:30:15",
            "resultCount": 42
        },
        {
            "id": 100,
            "keyword": "炼狱镇点位",
            "searchTime": "2025-06-27 10:15:30",
            "resultCount": 28
        },
        {
            "id": 99,
            "keyword": "新手教学",
            "searchTime": "2025-06-27 09:45:00",
            "resultCount": 65
        },
        {
            "id": 98,
            "keyword": "贤者墙",
            "searchTime": "2025-06-26 22:30:00",
            "resultCount": 15
        },
        {
            "id": 97,
            "keyword": "雷兹大招技巧",
            "searchTime": "2025-06-26 20:15:45",
            "resultCount": 8
        }
    ],
    "timestamp": 1719500000000
}
```

#### 业务说明

- 游客用户返回空列表
- 按时间倒序排列
- 自动去重，相同关键词只保留最新一条

---

### 9.5 清空搜索历史 🔐

#### 接口描述

清空当前用户的所有搜索历史记录。

#### 请求信息

- **URL**: `/search/history`
- **Method**: `DELETE`
- **需要认证**: 是
- **权限要求**: 登录用户

#### 请求头

```
Authorization: Bearer {your_token}
```

#### 请求参数

无

#### 响应示例

```json
{
    "code": 200,
    "success": true,
    "message": "搜索历史已清空",
    "data": {
        "deletedCount": 25
    },
    "timestamp": 1719500000000
}
```

---

### 补充接口

#### 高级搜索功能

##### 分类搜索

除了全局搜索，还支持在特定类型内搜索：

- **搜索英雄**: `/search/heroes?keyword=杰特`
- **搜索地图**: `/search/maps?keyword=炼狱`
- **搜索武器**: `/search/weapons?keyword=暴徒`
- **搜索点位**: `/search/positions?keyword=烟雾`
- **搜索内容**: `/search/contents?keyword=教学`

每个接口的响应格式类似，但只返回对应类型的结果。

##### 高级筛选搜索

- **URL**: `/search/advanced`
- **Method**: `POST`

请求示例：

```json
{
    "keyword": "烟雾",
    "filters": {
        "type": ["position", "content"],
        "heroId": 1,
        "mapId": 1,
        "tags": ["进攻", "常用"],
        "dateRange": {
            "start": "2025-06-01",
            "end": "2025-06-30"
        }
    },
    "sort": "relevance",
    "current": 1,
    "size": 20
}
```

##### 相关搜索推荐

- **URL**: `/search/related`
- **Method**: `GET`

请求参数：

|参数名|类型|必填|说明|示例|
|---|---|---|---|---|
|keyword|String|是|原始搜索词|杰特|

响应示例：

```json
{
    "code": 200,
    "success": true,
    "message": "操作成功",
    "data": [
        "杰特技巧",
        "杰特大招",
        "杰特飞刀",
        "杰特烟雾点位",
        "杰特进攻路线",
        "杰特对枪技巧"
    ],
    "timestamp": 1719500000000
}
```

#### 搜索统计

##### 搜索数据统计（管理员）👑

- **URL**: `/search/statistics`
- **Method**: `GET`
- **权限要求**: ADMIN及以上

响应示例：

```json
{
    "code": 200,
    "success": true,
    "message": "操作成功",
    "data": {
        "todaySearchCount": 15680,
        "yesterdaySearchCount": 14250,
        "weekSearchCount": 98500,
        "avgSearchPerUser": 6.5,
        "searchSuccessRate": 0.82,
        "topKeywords": [
            {
                "keyword": "新英雄盖柯",
                "count": 5680,
                "userCount": 2150
            }
        ],
        "noResultKeywords": [
            {
                "keyword": "夜市皮肤",
                "count": 125
            }
        ],
        "searchSources": {
            "searchBox": 0.75,
            "hotSearch": 0.15,
            "relatedSearch": 0.1
        }
    },
    "timestamp": 1719500000000
}
```

---

### 搜索优化建议

#### 前端实现建议

1. **搜索框优化**
    
    - 输入防抖（建议300ms）
    - 显示搜索建议下拉框
    - 支持键盘上下选择
    - 回车直接搜索
2. **搜索结果页**
    
    - 分类标签页展示
    - 支持二次筛选
    - 搜索结果高亮
    - 无结果时推荐相关内容
3. **搜索历史管理**
    
    - 本地缓存最近5条
    - 点击可快速搜索
    - 支持删除单条历史

#### 性能优化

- 搜索结果缓存15分钟
- 热门搜索缓存1小时
- 搜索建议缓存5分钟
- 支持搜索结果分页加载

