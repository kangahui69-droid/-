# TechForge · 技术熔炉 - 开发流程

> 前后端分离的个人作品集网站

---

## 技术架构总览

```
┌─────────────────────────────────────────────────────────────┐
│                    访问者                                  │
│  (浏览器打开 http://localhost:8080/public/index.html)    │
└─────────────────────────────────────────────────────────────┘
                              ↓ ↑
┌─────────────────────────────────────────────────────────────┐
│                  后端 (Spring Boot)                       │
│                                                          │
│   Controllers ──→ Services ──→ Repositories ──→ DB      │
│        ↓                                                   │
│   JWT 认证                                               │
└─────────────────────────────────────────────────────────────┘
                              ↓ ↑
┌─────────────────────────────────────────────────────────────┐
│                   数据库 (MySQL)                          │
│   user / profile / article / tag / project / skill       │
│   article_tag / chat_history                             │
└─────────────────────────────────────────────────────────────┘
```

---

## Phase 1: 项目初始化

### 1.1 创建后端项目

```bash
# 目录结构
E:/java/portfolio/
├── portfolio-web/           # Spring Boot 后端
│   ├── pom.xml
│   └── src/
│       └── main/
│           ├── java/com/techforge/
│           │   └── TechForgeApplication.java
│           └── resources/
│               └── application.yml
├── public/                   # 前端静态文件
│   ├── index.html           # 首页
│   ├── admin.html         # 后台管理
│   └── css/
│       └── style.css
└── README.md
```

### 1.2 pom.xml 依赖

```xml
<dependencies>
    <!-- Spring Boot -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    
    <!-- JWT -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.12.3</version>
    </dependency>
    
    <!-- MySQL -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <scope>runtime</scope>
    </dependency>
    
    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
</dependencies>
```

### 1.3 application.yml 配置

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/techforge?useUnicode=true&characterEncoding=utf8
    username: root
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect

jwt:
  secret: ${JWT_SECRET}
  expiration: 86400000

minimax:
  api-key: ${MINIMAX_API_KEY}
```

---

## Phase 2: 实体类 (Entity)

### 2.1 User.java

```java
@Entity
@Table(name = "user")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(nullable = false, unique = true)
    private String username;
    
    @Column(nullable = false)
    private String password;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
}
```

### 2.2 Profile.java

```java
@Entity
@Table(name = "profile")
@Data
public class Profile {
    @Id
    private Integer id;  // 仅一条，id=1
    
    private String name;
    private String avatar;
    private String bio;
    @Column(columnDefinition = "TEXT")
    private String about;
    private String socialGithub;
    private String socialEmail;
    private String socialX;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### 2.3 Article.java

```java
@Entity
@Table(name = "article")
@Data
public class Article {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(nullable = false, unique = true)
    private String slug;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    
    private String excerpt;
    private String coverImage;
    
    @Enumerated(EnumType.STRING)
    private Status status = Status.DRAFT;
    
    private Integer views = 0;
    private Integer likes = 0;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    
    @ManyToMany
    @JoinTable(
        name = "article_tag",
        joinColumns = @JoinColumn(name = "article_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags;
    
    public enum Status {
        DRAFT, PUBLISHED
    }
}
```

---

## Phase 3: Repository

```java
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);
}

public interface ProfileRepository extends JpaRepository<Profile, Integer> {
}

public interface ArticleRepository extends JpaRepository<Article, Integer> {
    Page<Article> findByStatusAndDeletedAtIsNull(Status status, Pageable pageable);
    Page<Article> findByStatusAndDeletedAtIsNullAndTagsName(Status status, String tagName, Pageable pageable);
    Optional<Article> findByIdAndDeletedAtIsNull(Integer id);
    Optional<Article> findBySlugAndDeletedAtIsNull(String slug);
}

public interface ProjectRepository extends JpaRepository<Project, Integer> {
    List<Project> findByStatusOrderBySortOrderAsc(Status status);
}

public interface SkillRepository extends JpaRepository<Skill, Integer> {
    List<Skill> findByOrderBySortOrderAsc();
}
```

---

## Phase 4: Controller & API

### 4.1 公开接口

```java
@RestController
@RequestMapping("/api")
public class PublicController {
    
    @GetMapping("/profile")
    public Result<ProfileDTO> getProfile() { ... }
    
    @GetMapping("/articles")
    public Result<Page<ArticleDTO>> getArticles(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String tag
    ) { ... }
    
    @GetMapping("/articles/{id}")
    public Result<ArticleDTO> getArticle(@PathVariable Integer id) { ... }
    
    @GetMapping("/projects")
    public Result<List<ProjectDTO>> getProjects() { ... }
    
    @GetMapping("/skills")
    public Result<List<SkillDTO>> getSkills() { ... }
}
```

### 4.2 管理接口

```java
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("isAuthenticated()")
public class AdminController {
    
    @PutMapping("/profile")
    public Result<?> updateProfile(@RequestBody ProfileDTO dto) { ... }
    
    @PostMapping("/articles")
    public Result<?> createArticle(@RequestBody ArticleDTO dto) { ... }
    
    @PutMapping("/articles/{id}")
    public Result<?> updateArticle(@PathVariable Integer id, @RequestBody ArticleDTO dto) { ... }
    
    @DeleteMapping("/articles/{id}")
    public Result<?> deleteArticle(@PathVariable Integer id) { ... }
    
    @PostMapping("/projects")
    public Result<?> createProject(@RequestBody ProjectDTO dto) { ... }
    
    @DeleteMapping("/projects/{id}")
    public Result<?> deleteProject(@PathVariable Integer id) { ... }
    
    @PostMapping("/skills")
    public Result<?> createSkill(@RequestBody SkillDTO dto) { ... }
    
    @DeleteMapping("/skills/{id}")
    public Result<?> deleteSkill(@PathVariable Integer id) { ... }
}
```

### 4.3 认证接口

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        // 验证用户名密码
        // 生成 JWT Token
        // 返回 Token 和过期时间
    }
}
```

---

## Phase 5: 前端页面

### 5.1 文件结构

```
public/
├── index.html          # 首页 (访客)
├── admin.html         # 后台管理
├── article.html      # 文章详情
├── css/
│   └── style.css
└── js/
    ├── api.js        # API 调用
    └── app.js       # 页面逻辑
```

### 5.2 API 调用示例 (api.js)

```javascript
const API_BASE = '/api';

// 公开接口
export const getProfile = () => fetch(`${API_BASE}/profile`).then(r => r.json());
export const getArticles = (page, size, tag) => 
    fetch(`${API_BASE}/articles?page=${page}&size=${size}${tag ? `&tag=${tag}` : ''}`)
    .then(r => r.json());
export const getProjects = () => fetch(`${API_BASE}/projects`).then(r => r.json());
export const getSkills = () => fetch(`${API_BASE}/skills`).then(r => r.json());

// 管理接口 (需 Token)
export const login = (username, password) =>
    fetch(`${API_BASE}/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password })
    }).then(r => r.json());

export const createArticle = (data, token) =>
    fetch(`${API_BASE}/admin/articles`, {
        method: 'POST',
        headers: { 
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(data)
    }).then(r => r.json());

export const updateArticle = (id, data, token) => 
    fetch(`${API_BASE}/admin/articles/${id}`, {
        method: 'PUT',
        headers: { 
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(data)
    }).then(r => r.json());
```

---

## Phase 6: 数据库初始化

### 6.1 建表 SQL

```sql
CREATE DATABASE techforge CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE techforge;

-- 执行 entity 类会自动建表，或手动执行：
CREATE TABLE `user` (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE `profile` (
    id INT PRIMARY KEY DEFAULT 1,
    name VARCHAR(50),
    avatar VARCHAR(255),
    bio VARCHAR(200),
    about TEXT,
    social_github VARCHAR(100),
    social_email VARCHAR(100),
    social_x VARCHAR(100)
);

-- 插入管理员 (密码需 BCrypt 加密)
-- 插入初始 profile
```

---

## Phase 7: 启动验证

### 7.1 启动命令

```bash
# 方式1: 命令行
cd E:/java/portfolio/portfolio-web
./mvnw spring-boot:run

# 方式2: IDE 运行
# 运行 TechForgeApplication.main()
```

### 7.2 验证清单

| 检查项 | 预期结果 |
|--------|----------|
| 访问 `http://localhost:8080` | 404 (静态资源在 /public) |
| 访问 `http://localhost:8080/public/index.html` | 显示首页 |
| 访问 `http://localhost:8080/api/profile` | 返回 JSON 个人简介 |
| POST `/api/auth/login` | 登录成功返回 Token |

---

## 文件清单

```
E:/java/portfolio/
├── portfolio-web/
│   ├── pom.xml
│   ├── mvnw / mvnw.cmd
│   └── src/main/
│       ├── java/com/techforge/
│       │   ├── TechForgeApplication.java
│       │   ├── config/
│       │   │   ├── SecurityConfig.java
│       │   │   ├── JwtConfig.java
│       │   │   └── CorsConfig.java
│       │   ├── entity/
│       │   │   ├── User.java
│       │   │   ├── Profile.java
│       │   │   ├── Article.java
│       │   │   ├── Tag.java
│       │   │   ├── Project.java
│       │   │   ├── Skill.java
│       │   │   └── ChatHistory.java
│       │   ├── repository/
│       │   ├── service/
│       │   ├── controller/
│       │   │   ├── PublicController.java
│       │   │   ├── AdminController.java
│       │   │   └── AuthController.java
│       │   └── dto/
│       └── resources/
│           └── application.yml
├── public/
│   ├── index.html
│   ├── admin.html
│   ├── article.html
│   └── css/
│       └── style.css
├── sql/
│   └── init.sql
└── README.md
```

---

## 接下来的步骤

1. **[ ]** 创建 portfolio-web 项目结构
2. **[ ]** 配置 pom.xml 依赖
3. **[ ]** 实现 Entity 实体类
4. **[ ]** 实现 Repository
5. **[ ]** 实现 Controller API
6. **[ ]** 初始化数据库
7. **[ ]** 创建前端页面
8. **[ ]** 启动测试