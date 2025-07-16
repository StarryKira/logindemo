# 用户注册登录系统

这是一个基于Spring Boot的用户注册登录系统，使用MySQL存储用户数据，JPA作为ORM框架，Redis存储Session。

## 技术栈

- **后端框架**: Spring Boot 3.5.3
- **数据库**: MySQL 8.0
- **ORM框架**: Spring Data JPA
- **缓存/Session**: Redis
- **Java版本**: 21

## 功能特性

### 用户认证
- 用户注册
- 用户登录/登出
- Session管理（存储在Redis中）
- 基于Session的认证

### 用户管理
- 获取用户信息
- 更新用户资料
- 修改密码
- 管理员功能（用户列表、删除用户）

## 项目结构

```
src/main/java/org/starrykira/logindemo/
├── entity/              # 实体类
│   └── User.java       # 用户实体
├── dto/                # 数据传输对象
│   ├── LoginRequest.java
│   ├── RegisterRequest.java
│   ├── ApiResponse.java
│   └── UserResponse.java
├── repository/         # 数据访问层
│   └── UserRepository.java
├── service/           # 业务逻辑层
│   └── UserService.java
├── controller/        # 控制器层
│   ├── AuthController.java
│   └── UserController.java
└── config/           # 配置类
    └── WebConfig.java
```

## 环境要求

1. **Java 21**
2. **MySQL 8.0+**
3. **Redis 6.0+**
4. **Maven 3.6+**

## 快速开始

### 1. 配置数据库

#### MySQL设置
创建数据库：
```sql
CREATE DATABASE logindemo CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

#### Redis设置
确保Redis服务运行在本地6379端口

### 2. 配置文件

修改 `src/main/resources/application.properties` 中的数据库连接信息：

```properties
# 数据库配置
spring.datasource.url=jdbc:mysql://localhost:3306/logindemo?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=你的密码
```

### 3. 运行应用

```bash
# 使用Maven运行
./mvnw spring-boot:run

# 或者编译后运行
./mvnw clean package
java -jar target/logindemo-0.0.1-SNAPSHOT.jar
```

## API接口

### 认证相关接口

#### 1. 用户注册
- **URL**: `POST /api/auth/register`
- **请求体**:
```json
{
    "username": "testuser",
    "password": "123456",
    "email": "test@example.com",
    "realName": "测试用户",
    "phoneNumber": "13800138000"
}
```

#### 2. 用户登录
- **URL**: `POST /api/auth/login`
- **请求体**:
```json
{
    "username": "testuser",
    "password": "123456"
}
```

#### 3. 用户登出
- **URL**: `POST /api/auth/logout`

#### 4. 获取当前用户信息
- **URL**: `GET /api/auth/current`

#### 5. 检查登录状态
- **URL**: `GET /api/auth/status`

### 用户管理接口

#### 1. 获取用户列表
- **URL**: `GET /api/users`
- **需要**: 登录

#### 2. 获取用户信息
- **URL**: `GET /api/users/{id}`
- **需要**: 登录

#### 3. 更新用户信息
- **URL**: `PUT /api/users/{id}`
- **需要**: 本人或管理员权限

#### 4. 删除用户
- **URL**: `DELETE /api/users/{id}`
- **需要**: 管理员权限

#### 5. 修改密码
- **URL**: `POST /api/users/{id}/change-password`
- **需要**: 本人
- **请求体**:
```json
{
    "oldPassword": "旧密码",
    "newPassword": "新密码"
}
```

#### 6. 获取个人资料
- **URL**: `GET /api/users/profile`
- **需要**: 登录

## 认证方式

### Session认证
- 登录时会创建Session并存储在Redis中
- Session ID通过Cookie传递
- Session过期时间：30分钟

## 统一响应格式

所有API响应都遵循统一格式：

```json
{
    "success": true/false,
    "message": "响应消息",
    "data": "响应数据（可选）"
}
```

## 注意事项

2. **密码安全**: 当前版本为了演示方便，密码未加密存储，生产环境请使用BCrypt等加密方式
3. **权限控制**: 简单的基于角色的权限控制（USER/ADMIN）
4. **Session存储**: Session存储在Redis中，支持分布式部署

## 开发建议

生产环境建议添加：
- 密码加密（BCrypt）
- 参数验证（Bean Validation）
- 异常统一处理
- 日志记录
- API限流
- 更完善的权限控制
