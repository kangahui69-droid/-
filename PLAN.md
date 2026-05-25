# TechForge 文章功能实施计划

## 需求概述

1. 首页"最新文章"只显示最新的6篇文章
2. 超过6个时标题右侧显示"查看更多"按钮，点击跳转文章列表页
3. 导航栏"文章"点击后跳转专门的文章列表页面
4. 文章列表页显示所有已发布文章，卡片形式展示
5. 点击文章卡片跳转到详情页
6. 详情页显示：标题、内容、浏览量、发布时间
7. 详情页底部可评论（无需登录，访客可直接留言）

---

## 阶段 1：首页文章数据对接

### 可平衡的终态
- 首页"最新文章"区域从后端 `/api/articles` 动态获取 6 条数据（按发布时间倒序）
- 标题右侧显示"查看更多"链接（当文章数 > 6 时显示）
- 点击"查看更多"跳转到 `/articles.html`
- 导航栏"文章"链接到 `/articles.html`

### 明确的验证方式
1. 重启后端服务，刷新首页
2. 首页"最新文章"区域显示数据库中最新的 6 篇文章
3. 当文章数 > 6 时，标题右侧显示"查看更多"按钮
4. 点击"查看更多"或导航栏"文章"跳转到文章列表页
5. 后端日志无 500/404 错误

### 关键约束
- 不修改后端实体结构，使用现有 Article 字段
- 沿用现有的 CSS 卡片样式（下同）
- 加载失败时显示静态占位（向后兼容）

---

## 阶段 2：文章列表页

### 可平衡的终态
- 新建 `articles.html` 页面
- 页面展示所有已发布（PUBLISHED）的文章列表
- 每个文章以卡片形式显示（标题、摘要、标签）
- 点击卡片跳转到详情页 `/article.html?id=X`

### 明确的验证方式
1. 访问 `/articles.html`
2. 页面显示所有已发布文章（卡片网格布局）
3. 点击任意文章卡片跳转到对应详情页
4. URL 参数正确传递文章 ID

### 关键约束
- 与首页卡片保持一致的设计风格
- 支持分页（每页 12 条）或滚动加载

---

## 阶段 3：文章详情页

### 可平衡的终态
- 访问 `/article.html?id=X` 显示文章详情
- 页面显示：标题、发布时间、浏览量、完整内容
- 浏览量 +1（调用后端 API 更新）

### 明确的验证方式
1. 点击文章卡片进入详情页
2. URL 参数正确传递文章 ID
3. 内容从 `/api/articles/{id}` 获取
4. 刷新页面浏览量增加（验证 +1）

### 关键约束
- 内容以 Markdown 渲染（或简化文本）
- 文章不存在时显示 404 提示

---

## 阶段 4：评论功能

### 可平衡的终态
- 数据库新增 `comment` 表（文章评论）
- 评论 API 接口（CRUD）
- 文章详情页底部显示评论列表
- 访客可直接发表评论（无需登录）

### 明确的验证方式
1. 在文章下方可见评论输入框
2. 提交评论后实时显示在列表中
3. 刷新页面评论仍然存在
4. 评论按时间倒序排列

### 关键约束
- 访客可自由发表评论（无需登录）
- 敏感词过滤（同 XSS 防护）
- 分页显示（每页 20 条）

---

## 需修改的文件

### 后端 (新增)
| 文件 | 说明 |
|------|------|
| `entity/Comment.java` | 评论实体 |
| `repository/CommentRepository.java` | 评论数据访问 |
| `controller/CommentController.java` | 评论 CRUD 接口 |
| `Article.java` | 添加 comments 关系 |

### 前端 (新增/修改)
| 文件 | 说明 |
|------|------|
| `static/index.html` | 修改加载 API 数据 |
| `static/articles.html` | 新建文章列表页 |
| `static/article.html` | 新建文章详情页 |

---

## 实施顺序

1. 阶段 1 → 首页文章数据对接（最简单）
2. 阶段 2 → 文章列表页
3. 阶段 3 → 文章详情页
4. 阶段 4 → 评论功能

---

## 完成状态 ✅

- [x] SQL文件同步（comment表已添加）
- [x] Comment实体类已存在
- [x] CommentRepository已存在
- [x] CommentController已存在
- [x] SecurityConfig已添加白名单
- [x] index.html首页文章加载
- [x] articles.html列表页已存在
- [x] article.html详情页已存在（含评论）

---

## 管理端页面目标

### 需求概述

在浏览端登录成功后，显示管理端页面（暂不开发具体管理功能）。

### 可平衡的终态

- 新建管理端页面 `admin.html`
- 页面结构：左侧固定侧边栏（220px）+ 右侧主内容区
- 侧边栏包含导航菜单：文章管理、项目管理、技能管理、个人资料、退出登录
- 登录成功后从 `/` 跳转到 `/admin.html`
- 当前登录状态通过 localStorage 中的 token 判断
- 已登录用户直接显示管理端页面，未登录显示登录表单

### 明确的验证方式

1. 访问首页点击"后台"链接，跳转到 `/admin.html`（未登录时显示登录表单）
2. 输入正确用户名密码，点击登录，成功后跳转到管理端页面
3. 管理端页面显示左侧导航栏和右侧内容区
4. 刷新页面保持登录状态（localStorage 有 token）
5. 点击"退出登录"清除 token 并跳转回浏览端首页

### 关键约束

- 使用现有 SecurityConfig 白名单规则，后端接口不变
- 管理端页面添加 `/admin.html` 到 permitAll 白名单
- 复用 index.html 中已定义的 CSS 样式（如 admin-layout, admin-sidebar）
- 只实现页面框架和登录逻辑，不开发具体管理功能
- 使用 Token 存于 localStorage 进行状态判断

---

## 文章管理功能目标

### 需求概述

在管理端实现文章的增删改查功能，包括：发布新文章、查看文章列表、编辑文章、删除文章、管理评论。

### 可平衡的终态

1. **文章列表页** (`data-page="articles"`)
   - 显示所有文章（包括 DRAFT 和 PUBLISHED）
   - 每条显示：标题、状态、浏览量、创建时间
   - 支持搜索/筛选

2. **文章编辑/创建**
   - 弹窗或内嵌表单编辑文章
   - 字段：标题、别名(slug)、内容(Markdown)、摘要、封面图、状态
   - 发布/保存草稿

3. **文章删除**
   - 列表页支持删除操作

4. **评论管理**（在该文章详情中）
   - 查看文章的所有评论
   - 删除不良评论

### 明确的验证方式

1. 进入管理端 → 点击"文章管理" → 显示文章列表
2. 点击"新建文章" → 填写表单 → 保存 → 列表显示新文章
3. 点击文章"编辑" → 修改内容 → 保存 → 刷新列表
4. 点击"删除" → 确认 → 文章从列表移除
5. 在文章编辑页面点击"评论" → 显示评论列表 → 删除不良评论

### 关键约束

- 后端使用已有 `/api/admin/articles` 接口
- 评论使用 `/api/comments?articleId=X` 接口
- 使用 token 认证（Header 添加 `Authorization: Bearer <token>`）
- 复用现有 admin.html 布局和样式
- 状态 DRAFT/PUBLISHED 使用下拉选择

---

## 管理端首页导航目标

### 需求概述

在管理端侧边栏上方添加"首页"链接，点击后跳转到浏览端首页，方便在其它管理页面快速返回。

### 可衡量的终态

1. 侧边栏顶部添加"首页"菜单项
2. 点击后跳转至浏览端首页 `/`
3. 其它页面保持现有布局不变

### 明确的验证方式

1. 进入管理端 `/admin.html`
2. 侧边栏顶部可见"首页"链接
3. 点击"首页"返回浏览端首页
4. 登录状态下从首页点击"后台"可返回管理端

### 关键约束

- 添加到现有 admin.html 的侧边栏菜单
- 不修改后端接口
- 样式与现有菜单保持一致
- 使用 href="/" 跳转

### 完成状态

- [x] 侧边栏顶部添加"首页"链接

---

## 浏览端首页导航目标

### 需求概述

在浏览端的导航栏中添加"首页"链接，便于从其它页面快速返回首页。

### 可衡量的终态

1. 导航栏添加"首页"链接
2. 点击可跳转至首页 `/`
3. 导航链接顺序：首页、文章、项目、技能、AI对话、后台

### 明确的验证方式

1. 访问 `/articles.html` 或 `/article.html`
2. 导航栏可见"首页"链接
3. 点击"首页"返回浏览端首页

### 关键约束

- 添加到 index.html、articles.html、article.html 的导航栏
- 不修改后端接口
- 样式与现有 nav-link 保持一致

### 完成状态

- [x] 导航栏添加"首页"链接 (index.html)
- [x] 导航栏添加"首页"链接 (articles.html)
- [x] 导航栏添加"首页"链接 (article.html)

---

## 项目展示功能完善目标

### 需求概述

1. 首页"项目展示"最多显示3个
2. 超过3个时标题右侧显示"查看更多"按钮，点击跳转项目列表页
3. 导航栏"项目"点击后跳转专门的项目列表页面
4. 项目列表页显示所有已发布项目，卡片形式展示
5. 点击项目卡片跳转到详情页
6. 详情页显示：项目名称、项目类型、项目介绍、技术栈、功能模块、项目结构、项目总结、GitHub路径

---

### 阶段 1：首页项目数据对接

#### 可平衡的终态
- 首页"项目展示"区域从 `/api/projects` 动态获取 3 条数据（按 sortOrder 排序）
- 标题右侧显示"查看更多"链接（当项目数 > 3 时显示）
- 点击"查看更多"跳转到 `/projects.html`
- 导航栏"项目"链接到 `/projects.html`

#### 明确的验证方式
1. 重启后端服务，刷新首页
2. 首页"项目展示"区域显示数据库中最新的 3 个项目
3. 当项目数 > 3 时，标题右侧显示"查看更多"按钮
4. 点击"查看更多"或导航栏"项目"跳转到项目列表页
5. 后端日志无 500/404 错误

#### 关键约束
- 不修改后端实体结构，使用现有 Project 字段
- 沿用现有的 CSS 卡片样式（下同）
- 加载失败时显示静态占位（向后兼容）

---

### 阶段 2：项目列表页

#### 可平衡的终态
- 新建 `projects.html` 页面
- 页面展示所有已发布（PUBLISHED）的项目列表
- 每个项目以卡片形式显示（名称、简介、技术栈）
- 点击卡片跳转到详情页 `/project.html?id=X`

#### 明确的验证方式
1. 访问 `/projects.html`
2. 页面显示所有已发布项目（卡片网格布局）
3. 点击任意项目卡片跳转到对应详情页
4. URL 参数正确传递项目 ID

#### 关键约束
- 与首页卡片保持一致的设计风格
- 支持分页（每页 12 条）或滚动加载

---

### 阶段 3：项目详情页

#### 可平衡的终态
- 访问 `/project.html?id=X` 显示项目详情
- 页面显示：项目名称、项目类型、项目介绍、技术栈、功能模块、项目结构、项目总结、GitHub路径

#### 明确的验证方式
1. 点击项目卡片进入详情页
2. URL 参数正确传递项目 ID
3. 内容从 `/api/projects/{id}` 获取
4. 页面显示所有必要字段信息

#### 关键约束
- 项目不存在时显示 404 提示
- GitHub 链接可点击跳转

---

### 阶段 4：管理端项目字段扩展（可选）

#### 可平衡的终态
- 后端 Project 实体新增字段：`projectType`、`features`、`structure`、`summary`
- 管理端创建/编辑项目时可填写新增字段
- 保存后字段正确存储到数据库

#### 明确的验证方式
1. 管理端新建项目时可填写项目类型、功能模块等新字段
2. 编辑已存在的项目时可修改这些字段
3. 数据库正确存储新增字段的值
4. 前端详情页正确展示新增字段内容

#### 关键约束
- 新增字段采用 JSON 格式存储（features）
- 避免频繁修改表结构

---

## 需修改的文件

### 后端 (扩展，可选)
| 文件 | 说明 |
|------|------|
| `entity/Project.java` | 新增字段：projectType、features、structure、summary |

### 前端 (新增/修改)
| 文件 | 说明 |
|------|------|
| `static/index.html` | 修改项目区域，加载 API 数据 |
| `static/projects.html` | 新建项目列表页 |
| `static/project.html` | 新建项目详情页 |

---

## 实施顺序

1. 阶段 1 → 首页项目数据对接（最简单）
2. 阶段 2 → 项目列表页
3. 阶段 3 → 项目详情页
4. 阶段 4 → 管理端项目字段扩展（可选）

---

## 完成状态

- [x] 首页项目数据对接
- [x] 项目列表页
- [x] 项目详情页
- [x] 管理端项目字段扩展

---

## 管理端项目管理功能目标

### 需求概述

在管理端实现项目的增删改查功能，包括：发布新项目、查看项目列表、编辑项目、删除项目。

### 可衡量的终态

1. **项目列表页** (`data-page="projects"`)
   - 显示所有项目（包括 DRAFT 和 PUBLISHED）
   - 每条显示：名称、状态、技术栈、操作按钮
   - 支持搜索/筛选

2. **项目创建**
   - 点击"新建项目"弹出表单
   - 字段：名称、介绍、技术栈、项目类型、功能模块(JSON)、项目结构、总结、封面图、Demo URL、GitHub URL、排序、状态

3. **项目编辑**
   - 点击"编辑"打开表单
   - 预填充所有字段
   - 保存后更新列表

4. **项目删除**
   - 列表页支持删除操作

### 明确的验证方式

1. 进入管理端 → 点击"项目管理" → 显示项目列表
2. 点击"新建项目" → 填写表单 → 保存 → 列表显示新项目
3. 点击"编辑" → 修改字段 → 保存 → 刷新后字段更新
4. 点击"删除" → 确认 → 项目从列表移除

### 关键约束

- 调用 `/api/admin/projects` 接口（已存在）
- 使用 `Authorization: Bearer <token>` 认证
- 复用现有 admin.html 布局和样式
- 状态 DRAFT/PUBLISHED 使用下拉选择

### 完成状态

- [x] 项目列表页
- [x] 项目创建
- [x] 项目编辑
- [x] 项目删除

---

## AI对话功能目标（技术顾问助手）

### 需求概述

将 AI 对话定位为「技术顾问」助手，专注于回答技术相关问题，帮助访客了解站主的技术能力、项目经验和技术栈。

### 一、可衡量的终态

#### 1. 基础对话功能
- 回答技术相关问题（"你会什么技术？"、"你做过什么项目？"）
- 项目介绍（"介绍一下你的项目"）
- 技术栈查询（"你用过哪些框架？"）

#### 2. 非技术问题处理
- 对于非技术问题，礼貌拒绝并引导回到技术话题

#### 3. 对话界面
- 独立的 AI 对话页面（`chat.html`）
- 左右聊天气泡区分
- 3-4个快捷问题按钮

#### 4. 后端接口
- 优化系统 Prompt
- 新增快捷问题配置接口

---

### 二、明确的验证方式

| 验证点 | 预期结果 |
|--------|----------|
| 问"你会什么？" | 返回技术栈列表（Java、Python、RISC-V等） |
| 问"介绍一下你的项目" | 返回项目列表及简介 |
| 问"今天天气怎么样" | 回复"我只回答技术问题哦~" |
| 点击快捷问题"你擅长什么" | 自动发送并得到正确回答 |
| 刷新页面 | 对话内容清除 |
| 无网络/API超时 | 显示预设回复或错误提示 |

---

### 三、关键约束

- **Prompt 设计**：限定回答范围为技术相关问题
- **知识库**：复用现有项目+文章内容（RAG）
- **界面风格**：与现有 TechForge 风格一致
- **对话历史**：仅当前会话，刷新清除
- **响应时间**：≤ 5秒（包含打字机效果等待）
- **敏感词过滤**：保持现有 XSS 防护

---

### 四、需修改的文件

#### 后端
| 文件 | 说明 |
|------|------|
| `ChatController.java` | 优化 Prompt，快捷问题配置 |

#### 前端
| 文件 | 说明 |
|------|------|
| `index.html` | 对话区域改造或新建 `chat.html` |
| `chat.css`（可选） | 对话页面样式 |

---

### 五、实施顺序

1. **阶段1** → 后端 Prompt 优化（最简单）
2. **阶段2** → 对话前端改造（现有页面增强）
3. **阶段3** → 快捷问题功能
4. **阶段4** → 体验优化（打字机效果、加载状态）

---

### 六、完成状态

- [ ] 后端 Prompt 优化
- [ ] 对话前端改造
- [ ] 快捷问题功能
- [ ] 体验优化

### 一、可衡量的终态

| 指标 | 目标值 |
|------|--------|
| 支持技能分类数量 | ≥ 4个大类（前端/后端/AI/硬件） |
| 单个技能属性 | 具备名称、分类、熟练度、图标 |
| API响应时间 | ≤ 200ms |
| 前端渲染帧率 | ≥ 30fps |

---

### 二、明确的验证方式

#### 1. API验证

```bash
# 请求技能树数据
GET /api/skills/tree

# 预期响应示例
{
  "code": 200,
  "data": {
    "categories": [
      {
        "id": "frontend",
        "name": "前端开发",
        "skills": [
          {"id": 1, "name": "JavaScript", "level": 4, "icon": "js"},
          {"id": 2, "name": "Vue.js", "level": 3, "icon": "vue"}
        ]
      }
    ]
  }
}
```

#### 2. 前端验证

- [ ] 技能按分类分组展示，默认收起/展开
- [ ] 点击技能显示详情弹窗（熟练度说明、学习时间等）
- [ ] 熟练度用颜色区分：1-2级蓝色、3级黄色、4-5级红色
- [ ] 移动端自适应（折叠为列表）

#### 3. 数据验证

- [ ] 查询数据库，skill表新增字段：`category`、`level`、`icon_url`
- [ ] 数据完整性检查：无null分类、无null名称

---

### 三、关键约束

| 约束类型 | 具体内容 |
|----------|----------|
| **兼容性** | 不影响现有 `/api/skills` 旧接口，保持向后兼容 |
| **性能** | 技能数据采用缓存，首次加载后不频繁查询数据库 |
| **数据迁移** | 原有技能数据需平滑迁移，新字段有默认值 |
| **前端无框架依赖** | 使用原生JS/CSS实现，不引入第三方可视化库（除非必要） |

---

### 四、数据库改动

#### skill表新增字段

| 字段名 | 类型 | 说明 | 默认值 |
|---------|------|------|--------|
| category | VARCHAR(50) | 分类（如frontend/backend/ai/hardware） | 'other' |
| level | INT | 熟练度（1-5） | 1 |
| icon_url | VARCHAR(255) | 图标URL | null |

---

### 五、API规划

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/skills | GET | 原有接口，保持不变 |
| /api/skills/tree | GET | 新增，返回树形结构的技能数据 |
| /api/admin/skills | POST | 原有接口，支持新字段 |
| /api/admin/skills/{id} | PUT | 支持修改分类/熟练度 |

---

### 六、完成状态

- [x] Skill实体新增字段（category, level, icon_url）
- [x] /api/skills/tree接口
- [x] skills.html技能树前端页面
- [x] SecurityConfig白名单

---

## 项目关联技能功能目标

### 需求概述

在管理端创建/编辑项目时，可选择该项目使用了哪些技能；在项目详情页展示该项目所关联的技能标签，点击可跳转到技能树页面。

### 可衡量的终态

#### 1. 数据库层
- 创建 `project_skill` 中间表，记录项目与技能的多对多关系
- 无数据冗余，关联查询通过中间表完成

#### 2. 管理端项目表单
- 项目创建/编辑表单增加"关联技能"多选字段
- 以下拉框形式展示可选择的技能（按分类分组）
- 已选技能以标签形式显示，支持移除

#### 3. 项目列表页
- 每个项目卡片显示关联技能的简短标签（最多显示3个，超出显示"+N"）

#### 4. 项目详情页
- "技术栈"区域显示关联的技能标签（非字符串）
- 点击技能标签跳转到 `/skills.html?category=X` 或高亮对应技能

### 明确的验证方式

| 验证点 | 预期结果 |
|--------|----------|
| 进入管理端 → 项目管理 → 新建项目 | 表单包含"关联技能"下拉选项 |
| 选择3个技能并保存 | 数据库 project_skill 表有3条记录 |
| 编辑项目 → 修改关联技能 | 增删操作正常，标签实时更新 |
| 访问项目详情页 | 显示技能标签而非纯文本技术栈 |
| 点击技能标签 | 跳转技能树页面并高亮对应分类 |

### 关键约束

- 保留现有 `techStack` 字段（不删除，兼容旧数据）
- 技能多选，按 category 显示分组
- 仅显示已发布的技能供选择
- 已有项目可通过编辑补充关联技能
- 前端样式与现有技能卡片一致（按熟练度显色）

---

### 一、数据库改动

#### project_skill 中间表

| 字段名 | 类型 | 说明 | 约束 |
|---------|------|------|------|
| id | INT | 主键 | AUTO_INCREMENT |
| project_id | INT | 关联项目ID | NOT NULL, FK |
| skill_id | INT | 关联技能ID | NOT NULL, FK |
| created_at | DATETIME | 创建时间 | DEFAULT NOW() |

```sql
CREATE TABLE project_skill (
    id INT AUTO_INCREMENT PRIMARY KEY,
    project_id INT NOT NULL,
    skill_id INT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE,
    FOREIGN KEY (skill_id) REFERENCES skill(id) ON DELETE CASCADE,
    UNIQUE KEY uk_project_skill (project_id, skill_id)
);
```

---

### 二、后端改动

#### 1. 新增实体类 ProjectSkill.java

```java
@Entity
@Table(name = "project_skill")
public class ProjectSkill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "project_id", nullable = false)
    private Integer projectId;

    @Column(name = "skill_id", nullable = false)
    private Integer skillId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
```

#### 2. 新增 Repository

```java
public interface ProjectSkillRepository extends JpaRepository<ProjectSkill, Integer> {
    List<ProjectSkill> findByProjectId(Integer projectId);
    List<ProjectSkill> findBySkillId(Integer skillId);
    void deleteByProjectId(Integer projectId);
}
```

#### 3. 扩展 Project 实体

```java
@OneToMany(mappedBy = "projectId")
private List<ProjectSkill> projectSkills;
```

#### 4. 扩展 ProjectController

- GET `/api/projects/{id}` 返回时同时返回关联的技能详情
- 管理端保存项目时处理关联技能的增删

---

### 三、前端改动

#### 1. admin.html 项目表单
- 增加"关联技能"多选下拉组件
- 支持搜索过滤、按分类分组

#### 2. project.html 项目详情页
- 将 techStack 文本改为技能标签展示
- 点击跳转技能树

#### 3. projects.html 项目列表
- 显示关联技能简短标签

---

### 四、API规划

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/projects/{id} | GET | 返回项目详情时包含关联技能 |
| /api/admin/projects | POST | 保存时处理关联技能IDs |
| /api/admin/projects/{id} | PUT | 更新时处理关联技能IDs |

---

### 五、需修改的文件

#### 后端 (新增/修改)
| 文件 | 说明 |
|------|------|
| `entity/ProjectSkill.java` | 中间表实体 |
| `repository/ProjectSkillRepository.java` | 数据访问 |
| `entity/Project.java` | 添加 skills 关系 |
| `controller/ProjectController.java` | 处理关联技能 |
| 新增 SQL 脚本 | 建表语句 |

#### 前端 (修改)
| 文件 | 说明 |
|------|------|
| `static/admin.html` | 项目表单增加技能选择 |
| `static/project.html` | 技能标签展示 |
| `static/projects.html` | 列表显示技能标签 |

---

### 六、完成状态

- [x] 创建 project_skill 中间表和实体
- [x] ProjectController 返回关联技能
- [x] admin.html 技能多选组件
- [x] project.html 技能标签展示
- [x] projects.html 列表技能标签

---

## 管理端技能管理功能目标

### 需求概述

在管理端实现技能的增删改查功能，包括：添加新技能、修改已有技能、删除已有技能、设置技能分类和熟练度。

### 可衡量的终态

#### 1. 技能列表页 (data-page="skills")
- 显示所有技能卡片，每个卡片显示：名称、分类、熟练度、排序
- 支持搜索/筛选
- 显示"新建技能"按钮

#### 2. 技能创建
- 点击"新建技能"弹出/显示表单
- 字段：名称、分类（下拉选择）、熟练度（1-5级）、图标URL、排序
- 提交后保存到数据库

#### 3. 技能编辑
- 点击技能卡片的"编辑"按钮
- 预填充所有字段
- 保存后更新列表

#### 4. 技能删除
- 点击"删除"按钮
- 确认后从数据库删除
- 刷新列表

### 明确的验证方式

| 验证点 | 预期结果 |
|--------|----------|
| 进入管理端 → 点击"技能管理" | 显示技能列表（按分类分组） |
| 点击"新建技能" → 填写表单 → 保存 | 列表显示新技能 |
| 点击"编辑" → 修改名称 → 保存 | 列表显示更新后的名称 |
| 点击"删除" → 确认 | 技能从列表移除 |
| 刷新页面 | 技能数据持久化（数据库查询验证） |

### 关键约束

- 使用现有 `/api/admin/skills` 接口（POST 创建、DELETE 删除）
- 需新增 PUT 接口用于编辑（当前后端只有 POST/DELETE）
- 使用 `Authorization: Bearer <token>` 认证
- 复用 admin.html 布局和样式
- 分类选择：frontend、backend、ai、hardware、other
- 熟练度：1-5 级选择器

---

### 一、后端改动

#### 1. 扩展 SkillController

```java
@PutMapping("/admin/skills/{id}")
@PreAuthorize("isAuthenticated()")
public Result<?> updateSkill(@PathVariable Integer id, @RequestBody SkillRequest request) {
    // 更新技能
}
```

#### 2. 扩展 SkillRequest

```java
public static class SkillRequest {
    private String name;
    private Integer sortOrder;
    private String category;      // 新增
    private Integer level;        // 新增
    private String iconUrl;       // 新增
    // getters & setters
}
```

---

### 二、前端改动

#### admin.html 新增技能管理页面

- 加载技能列表函数 `loadSkills()`
- 技能列表渲染函数 `renderSkillList(skills)`
- 技能表单函数 `showSkillForm(skill?)`
- 保存技能函数 `saveSkill()`
- 删除技能函数 `deleteSkill(id)`
- 技能卡片 CSS 样式

---

### 三、API规划

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/admin/skills | POST | 创建技能 |
| /api/admin/skills/{id} | PUT | 更新技能 |
| /api/admin/skills/{id} | DELETE | 删除技能 |
| /api/skills | GET | 获取所有技能（管理端也用此接口） |

---

### 四、完成状态

- [x] 后端 PUT 接口
- [x] SkillRequest 扩展字段
- [x] admin.html 技能列表页
- [x] 技能创建/编辑表单
- [x] 技能删除功能