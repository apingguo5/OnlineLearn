# 学生端侧边栏重构与课程页面优化

## 背景

学生首页侧边栏存在以下问题：
- 课程栏目存在多余二级菜单（课程列表、课程管理、试题练习），实际只需"课程列表（正在学）"
- 试题练习和课程管理应合并为"作业习题"
- 存在不必要的功能入口（群组、收件箱等）
- 代码冗余，部分功能从未实现（如详情页的"返回课程列表"按钮）
- 首页跳转课程详情后渲染崩溃

## 修改内容

### 1. 侧边栏精简 `StudentAside.vue`

| 原菜单 | 新菜单 | 说明 |
|--------|--------|------|
| 首页 | 首页 | 保留 |
| 课程 → 课程列表 / 课程管理 / 试题练习 | 课程（直接进入课程列表） | 合并扁平化 |
| 群组 | 删除 | 未实现功能 |
| 收件箱 | 删除 | 未实现功能 |
| 笔记 | 删除 | 未实现功能 |
| 问答社区 | 问答社区 | 保留 |
| — | 作业习题 | 新增入口 |

### 2. 课程页面重构 `Courses.vue`

- 新建课程列表/详情一体化组件
- 兼容两种模式：`mode='list'`（课程列表）和 `mode='detail'`（单个课程详情）
- 支持 URL 查询参数 `courseId` + `courseName` 从首页直接跳转到指定课程
- 修复渲染崩溃：访问 `courseDetail.courseName` 时因 `courseDetail=null` 报错
- 删除未实现的"返回课程列表"按钮，精简细节视图
- 适配后端 `Result` 响应结构（`code: 200`, `resultData` 字段）

### 3. 路由注册 `router/index.js`

- 添加 `/courses` 路由，映射到 `Courses.vue`

### 4. 首页跳转改造 `home.vue`

- 从字符串路径跳转改为命名路由跳转
- 携带 `courseId` 和 `courseName` 参数

### 5. 清理废弃文件

- 删除 `StudentCourses.vue`、`StudentGroups.vue`、`StudentInbox.vue`、`StudentNotes.vue` 等未使用的视图组件
- 删除对应的 API 文件（`notes.js`、`inbox.js` 等）

## 涉及文件

| 文件 | 操作 |
|------|------|
| `frontend/src/views/studentweb/aside/StudentAside.vue` | 重写 - 精简菜单 |
| `frontend/src/views/studentweb/courses/Courses.vue` | 新建 - 课程列表/详情组件 |
| `frontend/src/router/index.js` | 修改 - 注册 Courses 路由 |
| `frontend/src/views/studentweb/contain/home.vue` | 修改 - 跳转适配命名路由 |
| `frontend/src/views/studentweb/contain/StudentContainWeb.vue` | 修改 - 移除旧视图引用 |
| `frontend/src/api/studentweb/courses.js` | 新建 - 课程 API |
| `frontend/src/views/studentweb/courses/StudentCourses.vue` | 删除 |
| `frontend/src/views/studentweb/groups/StudentGroups.vue` | 删除 |
| `frontend/src/views/studentweb/inbox/StudentInbox.vue` | 删除 |
| `frontend/src/views/studentweb/notes/StudentNotes.vue` | 删除 |
| `frontend/src/api/studentweb/notes.js` | 删除 |
| `frontend/src/api/studentweb/inbox.js` | 删除 |