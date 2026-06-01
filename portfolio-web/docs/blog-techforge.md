# 从零构建个人作品集网站：一个AI开发者的技术实践之路

> 本文详细记录了我从零开始构建 TechForge 个人作品集网站的全过程，包括技术选型、核心功能实现、问题排查以及后续优化方向。希望我的经验能对想要搭建个人网站的开发者有所启发。

## 一、项目背景与动机

作为一名专注于边缘AI与工业物联网应用的开发者，我一直希望拥有一个能够**全面展示个人技术能力和项目作品**的个人网站。市场上的CMS系统虽然便捷，但往往过于通用，难以体现技术功底。于是我决定：**自己动手，用纯手写代码的方式打造一个独一无二的个人作品集网站。**

这个网站需要满足以下核心需求：

- **作品展示**：以精美的卡片形式展示参与的竞赛项目和独立开发的作品
- **文章管理**：方便撰写和管理技术博客文章
- **技能展示**：直观呈现自己的技术栈和学习历程
- **AI对话**：集成一个智能助手，让访客可以快速了解我的技术背景
- **后台管理**：提供简易的管理面板，支持文章的增删改查

最终，我用 **Spring Boot + 原生 JavaScript** 构建了这个名为 **TechForge** 的个人作品集网站，整个项目从规划到上线历时约两周。

---

## 二、技术栈选择

### 后端技术

| 技术 | 版本 | 选择理由 |
|------|------|---------|
| Spring Boot | 3.4.0 | 成熟的微服务框架，社区活跃，文档完善 |
| Spring Security | 6.x | 内置安全支持，与 Spring Boot 无缝集成 |
| Spring Data JPA | - | 简化数据库操作，支持自定义查询 |
| MySQL | 8.x | 开源免费，满足数据存储需求 |
| JWT | 0.12.3 | 无状态的轻量级认证方案 |

### 前端技术

| 技术 | 选择理由 |
|------|---------|
| 原生 JavaScript (ES6+) | 无需构建工具，页面直接运行，维护简单 |
| CSS Variables | 实现主题切换和样式复用 |
| Canvas API | 实现粒子背景动画效果 |
| Fetch API | 原生支持异步请求 |

### AI 集成

| 技术 | 说明 |
|------|------|
| MiniMax API | 国内可用的 AI 大模型 API |
| OpenAI 兼容格式 | 便于后续迁移到其他模型 |

**为什么没有使用 Vue/React？**

我相信对于简单的个人网站，原生技术的性能更好、维护更简洁，同时能更好地展现 JavaScript 功底。当然，如果网站功能变得更加复杂（比如评论系统、用户系统），引入框架会更合适。

---

## 三、核心功能与实现

### 1. 作品集展示（动态加载）

```java
@GetMapping("/api/projects")
public Result<List<Project>> getProjects() {
    // 1. 查询所有已发布项目
    List<Project> projects = projectRepository
        .findByStatusAndDeletedAtIsNullOrderBySortOrderAsc(ProjectStatus.PUBLISHED);

    // 2. 批量查询项目-技能关联
    List<Integer> projectIds = projects.stream().map(Project::getId).collect(Collectors.toList());
    List<ProjectSkill> allRelations = projectSkillRepository.findByProjectIds(projectIds);

    // 3. 收集技能ID并查询
    Set<Integer> skillIds = allRelations.stream()
        .map(ProjectSkill::getSkillId).collect(Collectors.toSet());
    Map<Integer, Skill> skillMap = skillRepository.findAllById(skillIds).stream()
        .collect(Collectors.toMap(Skill::getId, s -> s));

    // 4. 组装到项目对象
    for (Project p : projects) {
        List<ProjectSkill> pss = relationsByProject.getOrDefault(p.getId(), Collections.emptyList());
        List<Skill> skills = pss.stream()
            .map(ps -> skillMap.get(ps.getSkillId()))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        p.setSkills(skills);
    }
    return Result.success(projects);
}
```

前端通过 Fetch API 动态加载：

```javascript
async function loadProjects() {
    const res = await fetch('/api/projects');
    const data = await res.json();
    if (data.code === 0 && data.data) {
        const grid = document.getElementById('projectsGrid');
        grid.innerHTML = '';
        projects.slice(0, 3).forEach(project => {
            const card = document.createElement('a');
            card.href = '/project.html?id=' + project.id;
            card.className = 'section-card';
            card.innerHTML = `
                <h3 class="card-title">${project.name}</h3>
                <p class="card-desc">${project.description}</p>
                <div class="card-tags">
                    ${project.techStack.split(',').slice(0, 3)
                      .map(t => `<span class="tag">${t.trim()}</span>`).join('')}
                </div>
            `;
            grid.appendChild(card);
        });
    }
}
```

### 2. JWT 无状态认证

```java
@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    public String generateToken(String username) {
        return Jwts.builder()
            .subject(username)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(Keys.hmacShaKeyFor(secret.getBytes()), Jwts.SIG.HS256)
            .compact();
    }

    public String extractUsername(String token) {
        return Jwts.parser()
            .verifyWith(Keys.hmacShaKeyFor(secret.getBytes()))
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secret.getBytes()))
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}
```

安全过滤器：

```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
            HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtils.validateToken(token)) {
                String username = jwtUtils.extractUsername(token);
                var auth = new UsernamePasswordAuthenticationToken(
                    username, null, 
                    authoritiesService.loadAuthoritiesByUsername(username));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        filterChain.doFilter(request, response);
    }
}
```

### 3. AI 智能对话

```java
@PostMapping("/api/chat")
public Result<?> chat(@RequestBody Map<String, String> request) {
    String question = request.get("question");

    // 读取个人资料构建上下文
    Profile profile = profileRepository.findFirstByDeletedAtIsNull();
    String systemPrompt = buildSystemPrompt(profile);

    String answer = aiApiClient.call(systemPrompt, question);

    // 保存聊天历史
    chatHistoryRepository.save(history);

    return Result.success(Map.of("answer", answer));
}
```

前端对话界面：
```javascript
async function sendMessage(question) {
    appendMessage('user', question);
    aiSendBtn.disabled = true;
    aiSendBtn.textContent = '思考中...';

    const res = await fetch('/api/chat', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ question: question })
    });

    const data = await res.json();
    if (data.code === 0 && data.data.answer) {
        appendMessage('ai', data.data.answer);
    }

    aiSendBtn.disabled = false;
    aiSendBtn.textContent = '发送';
}
```

### 4. 请求日志中间件

```java
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLogFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
            FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        long startTime = System.currentTimeMillis();

        try {
            chain.doFilter(request, response);
        } catch (Exception e) {
            log.error("[{}] {} {} - ERROR - {}",
                httpRequest.getMethod(),
                httpRequest.getRequestURI(),
                getQueryString(httpRequest),
                e.getMessage());
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("[{}] {} {} - {}ms - {}",
                httpRequest.getMethod(),
                httpRequest.getRequestURI(),
                getQueryString(httpRequest),
                duration,
                httpResponse.getStatus());
        }
    }
}
```

---

## 四、项目结构与亮点

### 项目结构

```
techforge-backend/
├── src/main/java/com/techforge/
│   ├── TechForgeApplication.java      # Spring Boot 启动类
│   ├── client/
│   │   └── AiApiClient.java     # AI API 客户端
│   ├── config/
│   │   ├── JwtUtils.java        # JWT 工具
│   │   ├── SecurityConfig.java # 安全配置
│   │   └── CorsConfig.java    # 跨域配置
│   ├── controller/
│   │   ├── ArticleController.java
│   │   ├── ProjectController.java
│   │   ├── SkillController.java
│   │   ├── ChatController.java
│   │   └── AuthController.java
│   ├── dto/
│   │   └── Result.java           # 统一响应格式
│   ├── entity/
│   │   ├── Article.java       # 文章
│   │   ├── Project.java     # 项目
│   │   └── Skill.java      # 技能
│   ├── filter/
│   │   └── RequestLogFilter.java # 日志过滤
│   └── repository/
│       └── *.java          # JPA 仓储
├── src/main/resources/
│   ├── application.yml     # 应用配置
│   └── static/           # 前端页面
│       ├── index.html
│       ├── articles.html
│       ├── projects.html
│       └── admin.html
└── pom.xml              # Maven 依赖
```

### 主要亮点

1. **RESTful API 设计**：统一的响应格式 `{ code, message, data }`，便于前端统一处理

2. **软删除机制**：所有实体使用 `deleted_at` 字段实现软删除，数据可恢复

3. **批量查询优化**：项目列表接口一次性查询所有关联数据，避免 N+1 问题

4. **前端公共函数提取**：将导航加载、日期格式化、XSS 防护等通用逻辑抽取到 `common.js`

```javascript
// static/js/common.js
function formatDate(dateStr) {
    // 将 ISO 时间转为友好格式：刚刚、X分钟前、X小时前...
}

function escapeHtml(text) {
    // XSS 防护：使用 textContent 自动转义
}

async function loadNav() {
    // 动态加载导航栏
}

async function apiGet(url) { /*...*/ }
async function apiPost(url, data) { /*...*/ }
```

5. **视觉设计**：深色主题 + 青色强调色，粒子背景动画，滚动入园效果

---

## 五、遇到的主要问题及解决过程

### 问题一：AI API 响应格式兼容

MiniMax API 的响应格式在不同时期有变化，需要兼容多种结构：

```java
private String parseAnswer(Map<String, Object> respBody) {
    // 结构1: choices[0].delta.content (流式/2.5格式)
    if (choice.containsKey("delta")) { /*...*/ }

    // 结构2: choices[0].message.content (标准格式)
    if (choice.containsKey("message")) { /*...*/ }

    // 结构3: 根级别 content
    if (respBody.containsKey("content")) {
        return (String) respBody.get("content");
    }
    return null;
}
```

**解决**：逐步尝试多种可能的响应结构，直到找到有效内容。

### 问题二：JWT Token 刷新失效

项目初期将 token 存储在 localStorage，关闭浏览器后 token 依然有效，但换设备后无法使用。

**解决**：保持现有的 24 小时过期时间，通过 Spring Security 的 `RememberMe` 功能可以在未来实现"记住我"功能。

### 问题三：静态资源请求日志过多

每次页面加载都会产生大量请求日志，难以定位问题。

**解决**：优化 `RequestLogFilter`，只记录 API 请求，不记录静态资源：

```java
if (path.endsWith(".css") || path.endsWith(".js") || 
    path.endsWith(".png") || path.endsWith(".jpg")) {
    chain.doFilter(request, response);
    return;
}
```

### 问题四：跨域预检请求

前端和后端分离部署时，浏览器的 OPTIONS 预检请求可能导致 CORS 问题。

**解决**：配置 Spring 的 CORS 规则，允许所有 localhost 端口：

```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("http://localhost:*")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600);
    }
}
```

---

## 六、部署与访问地址

### 在线访问

- **网站地址**：[http://techforge.asia](http://techforge.asia)（如未上线则为本地）
- **后台管理**：[http://techforge.asia/admin.html](http://techforge.asia/admin.html)

### 服务器配置

- **系统**：Ubuntu 20.04 LTS
- **JDK**：OpenJDK 23
- **反向代理**：Nginx
- **数据库**：MySQL 8.0（腾讯云 MySQL）

### 部署步骤

```bash
# 1. 拉取代码
git clone https://github.com/kangahui69/portfolio-web.git
cd portfolio-web

# 2. 构建 JAR
./mvnw clean package -DskipTests

# 3. 运行（生产环境建议使用环境变量配置敏感信息）
nohup java -jar target/techforge-backend-1.0.0.jar &
```

---

## 七、后续优化计划

### 短期优化

- [ ] **文章评论区**：参考 Ghost 评论系统方案
- [ ] **网站SEO**：添加 meta 标签、Sitemap
- [ ] **图片CDN**：使用对象存储替代本地存储
- [ ] **性能监控**：接入 Prometheus + Grafana

### 中期规划

- [ ] **搜索功能**：集成 Elasticsearch
- [ ] **评论通知**：接入 Server 酱微信提醒
- [ ] **暗黑模式**：增加亮色主题切换

### 长期愿景

- [ ] **国际化**：英文版网站
- [ ] **PWA 支持**：离线访问、安装到桌面
- [ ] **博客迁移**：从 MySQL 迁移到 Markdown 文件

---

## 八、总结与心得

经过两周的开发和调试，TechForge 网站终于成形。在这个过程中，我深刻体会到：

1. **从实际需求出发**：不要过度设计，前期只需要满足核心功能，后期再逐步迭代。

2. **前后端协同**：统一的 API 响应格式让前后端解耦更加清晰，减少了联调成本。

3. **日志的重要性**：完善的请求日志帮助快速定位问题，特别是排查跨域和认证问题时。

4. **持续学习的价值**：在做网站的过程中学习了很多 Spring Boot 高级特性，比如 Filter、Interceptor、JWT 等，这些都是课堂上无法学到的实战经验。

5. **开源精神**：感谢Spring、Vue、React等开源社区的无私奉献，让个人开发者也能用上企业级的技术。

最后，如果你对这个项目有任何建议或者想交流技术问题，欢迎通过网站上的 AI 对话功能，或者直接在 GitHub 上提 Issue。

---

## 九、GitHub 仓库链接

**GitHub 仓库**：[https://github.com/kangahui69/portfolio-web](https://github.com/kangahui69/portfolio-web)

```
⭐ Stars: ---
🍴 Forks: ---
```

如果这个项目对你有帮助，请 点个 Star 支持一下！

---

## 十、本地运行指南

### 环境要求

- JDK 17 或更高版本
- Maven 3.8+
- MySQL 8.0+

### 运行步骤

```bash
# 1. 克隆项目
git clone https://github.com/kangahui69/portfolio-web.git
cd portfolio-web

# 2. 创建数据库
mysql -u root -p -e "CREATE DATABASE techforge CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 3. 配置数据库密码（编辑 application.yml 修改数据库连接信息）

# 4. 构建
./mvnw clean package -DskipTests

# 5. 运行
./mvnw spring-boot:run

# 6. 访问网站
# http://localhost:8080
```

### 管理后台

1. 首次使用需在数据库中创建管理员账号
2. 访问 [http://localhost:8080/admin.html](http://localhost:8080/admin.html) 登录
3. 可进行文章、项目、技能的管理

### 常见问题

**Q: 启动报数据库连接失败？**
> 检查 MySQL 服务是否启动，检查 `application.yml` 中的数据库配置是否正确。

**Q: 前端页面样式错乱？**
> 确保通过 `./mvnw spring-boot:run` 启动，直接双击 JAR 可能导致静态资源加载失败。

---

> *最后更新时间：2026-05-29*
> *作者：小辉（TechForge）*
> *本文首发于：[TechForge Blog](https://techforge.asia)*