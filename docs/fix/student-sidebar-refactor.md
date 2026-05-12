# 学生端侧边栏及首页重构总结

## 修改目标

优化学生首页界面侧边栏，删除不必要的功能，重构冗余代码，使菜单结构更清晰合理。

## 修改内容

### 1. 侧边栏重构（StudentAside.vue）

**原结构（8项，含二级菜单）：**
- 首页
- 课程（含二级：课程广场、课程列表）
- 试题练习
- 课程管理
- 问答社区
- 群组
- 我的笔记
- 收件箱

**新结构（7项，扁平化）：**
- 🏠 首页
- 📖 课程列表（正在学的）
- ✏️ 作业习题（原试题练习 + 课程管理整合）
- 💬 问答社区（保留）
- 📓 我的笔记
- 📩 收件箱
- 📊 学习统计（原个人信息改名并保留入口）

**删除了：**
- 课程下的二级栏目（课程广场菜单移除，功能移至首页展示）
- 群组功能

### 2. 首页课程广场重建（home.vue）

- 轮播图下方新增**课程广场**区域
- 调用后端 `/study/class/findList` 接口获取班级列表（含关联课程名和教师名）
- 按课程名称分组展示班级卡片
- 每张卡片显示：班级名、教师、学年学期、容量
- 卡片悬停有上浮阴影动效

### 3. 路由清理（router/index.js）

- 删除 `CoursePlaza` 组件及对应路由注册
- 清理所有不再使用的 import

### 4. 删除无用页面文件

- `StudentGroups.vue` — 群组模块
- `CoursePlaza.vue` — 独立课程广场页面

### 5. 容器组件精简（StudentContainWeb.vue）

- 移除评论和动画等冗余标记
- 只保留 Header + Aside + router-view 核心结构

### 6. API 模块抽取

- 新建 `frontend/src/api/studentweb/studentClass.js`
- 封装 `getAllClasses()` 统一调用 `/study/class/findList`
- 避免在 Vue 组件中硬编码 API 路径

### 7. 修复数据读取BUG

- 后端 Result 类的数据字段为 `resultData`，前端初始写成 `res.data` 导致列表为空
- 修正为 `res.data.resultData`，确保后端class表数据能正确展示

## 涉及文件

| 文件 | 操作 |
|------|------|
| `frontend/src/views/studentweb/aside/StudentAside.vue` | 重构 |
| `frontend/src/views/studentweb/contain/home.vue` | 重构 |
| `frontend/src/views/studentweb/contain/StudentContainWeb.vue` | 精简 |
| `frontend/src/router/index.js` | 清理路由 |
| `frontend/src/api/studentweb/studentClass.js` | 新建 |
| `frontend/src/views/studentweb/courses/StudentCourses.vue` | 修复硬编码API路径 |
| `frontend/src/views/studentweb/groups/StudentGroups.vue` | 删除 |
| `frontend/src/views/studentweb/courses/CoursePlaza.vue` | 删除 |