# 学生课程路由冲突修复总结

## 问题描述

首页课程广场 (`home.vue`) 的课程卡片点击后使用 `name: 'Courses'` 跳转课程详情/选课页，但该路由名 `Courses` 同时被侧边栏"正在学习"的路由 `/courses` 占用，导致路由指向冲突——点击课程卡片实际跳转到了 `StudentCourses.vue`（正在学习页），而不是 `Courses.vue`（课程详情+选课页）。

## 解决方案

将课程详情/选课功能独立为一个新路由 `/course-detail`（路由名 `CourseDetail`），与"正在学习"的路由 `/courses` 分离，避免路由名冲突。

## 修改文件清单

### 1. `frontend/src/router/index.js`
- 在 `StudentContainWeb` 的子路由中新增 `/course-detail` 路由，路由名 `CourseDetail`，组件指向 `Courses.vue`（懒加载）
- `/courses` 路由保持不变，继续指向 `StudentCourses.vue`

### 2. `frontend/src/views/studentweb/contain/home.vue`
- 课程广场卡片点击方法 `goToCourse` 中的路由名从 `'Courses'` 改为 `'CourseDetail'`

### 3. `frontend/src/views/studentweb/courses/Courses.vue`
- 内部 `openCourseDetail` 方法中的路由名从 `'Courses'` 改为 `'CourseDetail'`

## 路由映射关系（修复后）

| 功能入口 | 路由路径 | 路由名 | 组件 | 用途 |
|---------|---------|-------|------|------|
| 侧边栏"正在学习" | `/courses` | `Courses` | `StudentCourses.vue` | 展示该学生已选课程班级 |
| 首页课程广场卡片 | `/course-detail` | `CourseDetail` | `Courses.vue` | 课程详情展示 + 选课/退选操作 |