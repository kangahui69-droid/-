# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

---

## 项目概述

**TechForge** 是一个前后端分离的个人作品集网站，后端使用 Spring Boot 3.x + MySQL，前端为原生 HTML/CSS/JS（目前独立于 frontend 项目）。

## 常用命令

### 启动后端

```bash
cd portfolio-web
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
# 访问 http://localhost:8080
```

### 构建打包

```bash
./mvnw package -DskipTests
java -jar target/techforge-backend-1.0.0-SNAPSHOT.jar --spring.profiles=local
```

### 运行测试

```bash
./mvnw test
# 单个测试类
./mvnw test -Dtest=SomeTestClass
```

## 环境配置

| 配置 | 值 |
|------|-----|
| JDK | 17 |
| 数据库 | MySQL 8.x (localhost:3306/techforge) |
| 用户 | root |
| 密码 | kang2005. |

## 架构要点

### 分层结构

```
com.techforge/
├── config/          # 配置类 (Security, JWT, CORS)
├── controller/       # REST API 接口
├── entity/          # JPA 实体
├── repository/      # 数据访问层
├── service/        # 业务逻辑
└── dto/           # 数据传输对象
```

### 数据库表

- `user` - 管理员（仅一人）
- `profile` - 个人简介
- `article` - 博客文章（含 tags 多对多）
- `tag` - 文章标签
- `project` - 项目展示
- `skill` - 技能标签
- `chat_history` - AI 对话历史

### 安全设计

- JWT 认证，过期时间 24 小时
- 管理接口需 `Authorization: Bearer <token>`
- 密码 BCrypt 加密存储
- 使用 Apache Commons Text 过滤 XSS

### AI 对话

- 基于 MiniMax API（环境变量 `MINIMAX_API_KEY`）
- 无 API Key 时返回本地预设回复
- RAG 实现：通过 Project 表构建上下文

## 重要文件

- [application.yml](portfolio-web/src/main/resources/application.yml) - 主配置（包含所有环境变量）
- [application-local.yml](portfolio-web/src/main/resources/application-local.yml) - 本地开发配置
- [SecurityConfig.java](portfolio-web/src/main/java/com/techforge/config/SecurityConfig.java) - 权限配置
- [README.md](README.md) - 完整项目文档

## 已知限制

- 前端静态文件不在此项目中，需从独立 frontend 项目获取
- `/` 根路径无静态页面，后端重启会返回 404