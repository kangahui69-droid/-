# AGENTS.md

This file provides guidance to Codex (Codex.ai/code) when working with code in this repository.

---

## 项目概述

**TechForge** 是前后端分离的个人作品集网站，后端 Spring Boot 3.x + MySQL，前端为静态 HTML/CSS/JS。

## 常用命令

### 启动后端 (Windows CMD)

```cmd
cd portfolio-web
set DB_PASSWORD=你的数据库密码
.\mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

> Windows PowerShell 用引号包裹: `./mvnw "spring-boot:run" "-Dspring-boot.run.profiles=local"`

### 构建打包

```bash
./mvnw package -DskipTests
java -jar target/techforge-backend-1.0.0-SNAPSHOT.jar --spring.profiles=local
```

### 运行测试

```bash
./mvnw test
./mvnw test -Dtest=SomeTestClass
```

## 环境配置

| 配置 | 值 |
|------|-----|
| JDK | 17 |
| 数据库 | MySQL 8.x (localhost:3306/techforge) |
| 用户/密码 | root / <你的数据库密码> |

## 架构

```
com.techforge/
├── config/       # Security/JWT/CORS 配置
├── controller/   # REST API
├── entity/      # JPA 实体
├── repository/  # 数据访问
├── service/     # 业务逻辑
└── dto/       # 数据传输对象
```

### 核心表

- `user` - 管理员 (BCrypt密码)
- `profile` - 个人简介
- `article` / `tag` - 文章 + 标签
- `project` - 项目展示
- `skill` - 技能
- `chat_history` - AI对话历史

### 安全

- JWT 认证 + Spring Security `@PreAuthorize`
- 管理接口需 `Authorization: Bearer <token>`
- CORS 白名单配置

### AI 对话

- DeepSeek API (环境变量 `DEEPSEEK_API_KEY`)
- RAG: 从 Project 表构建上下文

## 前端静态文件

位于 `portfolio-web/src/main/resources/static/`:

- `index.html` - 首页
- `projects.html` - 项目列表
- `project.html` - 项目详情
- `articles.html` / `article.html` - 文章列表/详情
- `admin.html` - 后台管理
- `skills.html` - 技能页

## 重要文件

- [application.yml](portfolio-web/src/main/resources/application.yml) - 主配置
- [application-local.yml](portfolio-web/src/main/resources/application-local.yml) - 本地配置
- [SecurityConfig.java](portfolio-web/src/main/java/com/techforge/config/SecurityConfig.java) - 权限配置