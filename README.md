# TechForge · 技术熔炉

> 技术沉淀与作品展示平台

---

## 项目概述

- **项目名称**: TechForge (技术熔炉)
- **标语**: 用代码创造价值，用技术解决问题
- **类型**: 前后端分离个人网站
- **核心功能**: 公开浏览 + 后台管理 + AI对话助手

---

## 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 后端 | Spring Boot | 3.x |
| 数据库 | MySQL | 8.x |
| ORM | Spring Data JPA | - |
| 认证 | JWT | - |
| AI | Minimax API | - |
| 前端 | 原生 HTML/CSS/JS | - |
| 部署 (可选) | Railway / Render | - |

---

## 功能模块

### 公开模块 (游客)

| 模块 | 功能 |
|------|------|
| 首页 | 个人简介 + 项目卡片 + 博客列表 |
| 博客 | 文章列表(分页) → 文章详情(Markdown渲染) |
| 项目 | 项目卡片 → 详情弹窗 |
| 技能 | 技能标签云 |
| AI对话 | 在线问答 (基于知识库的RAG) |

### 管理模块 (管理员)

| 模块 | 功能 |
|------|------|
| 登录 | JWT认证 |
| 内容管理 | 文章/项目/技能的 CRUD |
| 个人设置 | 简介编辑 |

---

## 数据库设计

### ER 图关系

```
user ←─────── profile (1:1)
      │
      ├─── article (1:n)
      │
      ├─── project (1:n)
      │
      └─── skill (1:n)
            │
            └─── article_tag (n:n) ──── article
```

### 表结构

```sql
-- ============================================
-- 用户表 (仅管理员一人)
-- ============================================
CREATE TABLE `user` (
    id          INT PRIMARY KEY AUTO_INCREMENT,
    username   VARCHAR(50) NOT NULL UNIQUE COMMENT '管理员用户名',
    password   VARCHAR(100) NOT NULL COMMENT 'BCrypt加密后的密码',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- 个人简介
-- ============================================
CREATE TABLE `profile` (
    id              INT PRIMARY KEY DEFAULT 1,
    name            VARCHAR(50) NOT NULL COMMENT '显示名称',
    avatar          VARCHAR(255) COMMENT '头像URL',
    bio             VARCHAR(200) COMMENT '一句话简介',
    about           TEXT COMMENT '关于我详细内容',
    social_github   VARCHAR(100) COMMENT 'GitHub链接',
    social_email   VARCHAR(100) COMMENT '邮箱',
    social_x       VARCHAR(100) COMMENT 'Twitter/X',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- 博客文章
-- ============================================
CREATE TABLE `article` (
    id            INT PRIMARY KEY AUTO_INCREMENT,
    title         VARCHAR(200) NOT NULL COMMENT '文章标题',
    slug          VARCHAR(200) NOT NULL UNIQUE COMMENT 'SEO友好链接',
    content       TEXT NOT NULL COMMENT 'Markdown内容',
    excerpt       VARCHAR(300) COMMENT '摘要',
    cover_image   VARCHAR(255) COMMENT '封面图',
    status       ENUM('draft','published') DEFAULT 'draft' COMMENT '状态',
    views         INT DEFAULT 0 COMMENT '阅读量',
    likes        INT DEFAULT 0 COMMENT '点赞数',
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at    DATETIME NULL COMMENT '软删除',

    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_slug (slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- 标签 (文章标签)
-- ============================================
CREATE TABLE `tag` (
    id           INT PRIMARY KEY AUTO_INCREMENT,
    name         VARCHAR(50) NOT NULL UNIQUE,
    created_at   DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- 文章-标签关联
-- ============================================
CREATE TABLE `article_tag` (
    article_id  INT NOT NULL,
    tag_id      INT NOT NULL,
    PRIMARY KEY (article_id, tag_id),

    FOREIGN KEY (article_id) REFERENCES article(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tag(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- 项目展示
-- ============================================
CREATE TABLE `project` (
    id            INT PRIMARY KEY AUTO_INCREMENT,
    name          VARCHAR(100) NOT NULL,
    description   TEXT NOT NULL,
    tech_stack   VARCHAR(300) COMMENT '技术栈(逗号分隔)',
    cover_image  VARCHAR(255),
    demo_url     VARCHAR(255),
    github_url  VARCHAR(255),
    sort_order   INT DEFAULT 0 COMMENT '排序',
    status      ENUM('draft','published') DEFAULT 'draft',
    created_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at   DATETIME NULL,

    INDEX idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- 技能标签
-- ============================================
CREATE TABLE `skill` (
    id           INT PRIMARY KEY AUTO_INCREMENT,
    name         VARCHAR(50) NOT NULL,
    sort_order   INT DEFAULT 0,
    created_at   DATETIME DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- AI对话历史 (用于分析，不公开)
-- ============================================
CREATE TABLE `chat_history` (
    id           INT PRIMARY KEY AUTO_INCREMENT,
    session_id   VARCHAR(50) COMMENT '会话ID',
    question    TEXT NOT NULL,
    answer      TEXT,
    tokens      INT DEFAULT 0 COMMENT '消耗token数',
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_session_id (session_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

## API 接口文档

### 公共约定

```
请求头: (无)
响应格式: { "code": 0, "message": "success", "data": {...} }
错误码:
  0    - 成功
  400  - 请求参数错误
  401  - 未授权
  403  - 禁止访问
  404  - 资源不存在
  500  - 服务器错误
```

### 公开接口

#### 1. 获取个人简介

```
GET /api/profile

响应: {
  "data": {
    "name": "小辉",
    "bio": "RISC-V物联网开发者",
    "about": "...",
    "social": { "github": "...", "email": "...", "x": "..." }
  }
}
```

#### 2. 文章列表

```
GET /api/articles?page=1&size=10&status=published

响应: {
  "data": {
    "list": [
      { "id": 1, "title": "...", "excerpt": "...", "tags": [...], "views": 100, "created_at": "2026-01-01" }
    ],
    "total": 10,
    "page": 1,
    "size": 10
  }
}
```

#### 3. 文章详情

```
GET /api/articles/{id}

响应: {
  "data": {
    "id": 1,
    "title": "...",
    "content": "Markdown内容",
    "cover_image": "...",
    "tags": ["RISC-V"],
    "views": 101,
    "created_at": "2026-01-01"
  }
}
```

#### 4. 项目列表

```
GET /api/projects

响应: {
  "data": [{ "id": 1, "name": "...", "description": "...", "tech_stack": [...] }]
}
```

#### 5. 技能列表

```
GET /api/skills

响应: { "data": [{ "id": 1, "name": "Python" }] }
```

#### 6. AI对话

```
POST /api/chat
Header: (无)
Body: { "question": "你用过哪些技术?" }

响应: {
  "data": { "answer": "我主要使用..." }
}
```

### 管理接口 (需JWT认证)

```
Header: Authorization: Bearer <token>
```

#### 7. 登录

```
POST /api/auth/login
Body: { "username": "ADMIN_USERNAME", "password": "ADMIN_PASSWORD" }

响应: { "data": { "token": "eyJ...", "expiresIn": 86400 } }
```

#### 8. 更新个人简介

```
PUT /api/profile (需认证)
Body: { "name": "...", "bio": "...", "about": "...", "social": {...} }
```

#### 9-11. 文章管理 (CRUD)

```
POST   /api/articles       (需认证) - 创建文章
PUT    /api/articles/{id}  (需认证) - 更新文章
DELETE /api/articles/{id}  (需认证) - 删除文章(软删除)
```

#### 12-14. 项目管理 (CRUD)

```
POST   /api/projects      (需认证)
PUT    /api/projects/{id} (需认证)
DELETE /api/projects/{id}(需认证)
```

#### 15-16. 技能管理

```
POST   /api/skills      (需认证)
DELETE /api/skills/{id}(需认证)
```

---

## 安全设计

### 1. JWT

- 过期时间: 24小时 (86400秒)
- 刷新策略: 重新登录获取新Token

### 2. CORS

- 仅允许受信任域名
- 生产环境配置具体域名

### 3. 密码

- BCrypt 加密存储
- 不接受明文密码

### 4. Markdown

- 使用 commons-text 过滤 HTML 标签
- 防止 XSS

### 5. 接口限流

- AI对话接口: 每个 IP 每分钟 10 次
- 登录接口: 每个 IP 每分钟 5 次

### 6. 敏感信息

- API Key 放环境变量，不提交到代码仓库

---

## RAG 实现方案

### 文档处理流程

```
1. 收集: 获取所有已发布的文章、项目文档
2. 切片: 按标题段落切分，每个 chunk ≈ 500字
3. 向量化: 调用 Embedding 模型
4. 存储: MySQL TEXT字段 或 Chroma/Qdrant
5. 检索: TopK=3, 相似度阈值=0.7
6. 回答: 构建 Prompt + 调用 LLM
```

### 兜底策略

- 知识库无相关内容时返回: "抱歉，这个问题超出了我的知识范围"

### 成本控制

- 每次对话最大 tokens: 2000
- 每日免费限额: 100次/天

---

## 环境变量

```bash
# 数据库
DB_HOST=localhost
DB_PORT=3306
DB_NAME=techforge
DB_USER=root
DB_PASSWORD=your_password

# JWT
JWT_SECRET=your_jwt_secret_min_256_bits

# Minimax AI
MINIMAX_API_KEY=your_api_key
```

---

## 本地启动

### 1. 创建数据库

```sql
CREATE DATABASE techforge CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 初始化数据

```sql
-- 插入管理员 (密码: BCrypt(ADMIN_PASSWORD))
INSERT INTO user (username, password) VALUES ('ADMIN_USERNAME', '$2a$10$...');

-- 插入初始简介
INSERT INTO profile (id, name, bio) VALUES ('小辉', '技术爱好者');
```

### 3. 启动后端

```bash
cd portfolio-web
./mvnw spring-boot:run
# 访问 http://localhost:8080
```

### 4. 启动前端

```bash
# 直接打开 public/index.html
# 或使用静态服务器
cd public && npx serve .
```

---

## 项目结构

```
E:/java/portfolio/
├── portfolio-web/                    # 后端项目
│   ├── src/main/java/com/techforge/
│   │   ├── TechForgeApplication.java   # 启动类
│   │   ├── config/                  # 配置
│   │   │   ├── SecurityConfig.java
│   │   │   ├── CorsConfig.java
│   │   │   └── JwtConfig.java
│   │   ├── controller/             # 控制器
│   │   ├── entity/                # 实体
│   │   ├── repository/             # 数据访问
│   │   ├── service/               # 业务逻辑
│   │   └── dto/                   # 数据传输对象
│   └── resources/
│       ├── application.yml
│       └── application-local.yml
├── public/                          # 前端静态文件
│   ├── index.html                  # 首页
│   ├── css/style.css
│   └── js/app.js
├── sql/                            # SQL脚本
│   └── init.sql
└── README.md
```

---

## MVP 范围

| 阶段 | 功能 | 优先级 |
|------|------|--------|
| v1.0 | 首页+项目+博客+技能展示 | P0 |
| v1.1 | 后台管理+登录 | P0 |
| v1.2 | 文章详情+Markdown渲染 | P1 |
| v1.3 | AI对话 (无RAG) | P2 |
| v1.4 | RAG知识库 | P3 |

---

## 验收标准

- [ ] 后端启动无报错
- [ ] 首页显示初始数据
- [ ] 管理员登录获取 Token
- [ ] 创建/编辑/删除文章成功
- [ ] 发布文章在首页显示
- [ ] AI对话接口返回回答

---

## 部署方案 (可选)

### Railway 部署

1. 连接到 GitHub 仓库
2. 设置环境变量
3. 配置 Build Command: `./mvnw package -DskipTests`
4. 配置 Start Command: `java -jar target/portfolio-web-0.0.1-SNAPSHOT.jar`

### Nginx 配置

```nginx
server {
    listen 80;
    server_name your-domain.com;

    location / {
        root /var/www/public;
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://localhost:8080;
    }
}
```

---

## 文档更新日志

| 日期 | 内容 |
|------|------|
| 2026-05-22 | 初始版本 |