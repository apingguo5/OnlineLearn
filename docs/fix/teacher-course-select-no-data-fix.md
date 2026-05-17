# 教师作业与考试：录入题目时选择课程提示"无数据"修复报告

## 问题描述

教师在"作业与考试/作业管理端"录入题目时，点击"所属课程"下拉选择框，提示"无数据"。

## 问题现象

- 页面路径：教师端 → 题库管理（TeacherQuestionBank.vue）→ 录入题目弹窗
- 受影响范围：同样影响试卷管理（TeacherExamPaper.vue）的课程选择下拉框
- 具体表现：el-select 下拉列表为空，显示"无数据"

## 问题定位

### 数据流追踪

| 环节 | 状态 | 详情 |
|------|------|------|
| **数据库 `course` 表** | ✅ 有数据 | id=1 "测试课程"(creator_id=1), id=3 "Android Basics with Compose"(creator_id=2) |
| **后端接口** `GET /study/teacher/dashboard/subjects` | ✅ 返回正确 | `{ code: 0, data: { list: [...] } }` |
| **前端 API** `getCourses()` | ✅ 调用正确 | 使用 `get()` 方法请求 GET 接口 |
| **前端解析逻辑** `res.data.data.list` | ✅ 匹配后端 | 与后端 `data.list` 完全对应 |

### 根因

**后端 Spring Boot 服务未启动。** 所有代码逻辑均正确无误：

1. **数据库层**：`course` 表已存在 2 条有效课程记录
2. **后端控制层**（`TeacherDashboardController.java`）：`/study/teacher/dashboard/subjects` 接口正常查询并返回 `{ code: 0, data: { list: [...] } }` 格式
3. **前端 API 层**（`teacherApi.js`）：`getCourses()` 正确调用后端 GET 接口
4. **前端视图层**（`TeacherQuestionBank.vue`）：`loadCourses()` 方法正确解析 `res.data.data.list` 数据

由于后端服务未运行，前端 axios 请求无法连接后端，`courseList` 始终保持空数组，导致 el-select 显示"无数据"。

## 修复措施

### 执行步骤

1. **启动后端服务**：
   ```powershell
   cd d:\Code\OnlineLearn
   .\start.ps1 -Backend
   ```
   或直接使用 Maven 启动：
   ```powershell
   cd d:\Code\OnlineLearn\backend
   mvn spring-boot:run
   ```

2. **验证 API 响应**：
   ```bash
   curl http://localhost:9251/study/teacher/dashboard/subjects
   ```
   返回：
   ```json
   {
     "code": 0,
     "data": {
       "list": [
         { "id": 3, "courseName": "Android Basics with Compose", "creatorId": 2, "status": 1 },
         { "id": 1, "courseName": "测试课程", "creatorId": 1, "status": 1 }
       ]
     }
   }
   ```

3. **刷新前端页面**后，课程选择下拉框即可正常显示课程列表。

## 验证结果

- 后端 API 正常返回 2 条课程数据
- 返回格式与前端解析逻辑完全匹配
- 仅需确保后端服务处于运行状态即可

## 相关文件

| 文件 | 作用 |
|------|------|
| `frontend/src/views/teacher/TeacherQuestionBank.vue` | 题目录入页面，调用 `getCourses()` 获取课程列表 |
| `frontend/src/views/teacher/TeacherExamPaper.vue` | 试卷管理页面，同样使用 `getCourses()` |
| `frontend/src/api/teacher/teacherApi.js` | 封装 `getCourses()` 方法调用后端接口 |
| `backend/src/main/java/com/rabbiter/ol/controller/TeacherDashboardController.java` | 后端 `/subjects` 接口，查询 `course` 表返回课程列表 |
| `backend/src/main/java/com/rabbiter/ol/entity/SubjectEntity.java` | 课程实体，映射到 `course` 表 |
| `backend/src/main/resources/application.yml` | 后端配置，指定端口 9251 |
| `start.ps1` | 项目启动脚本 |
| `check_db.py` | 数据库检查脚本，验证 `course` 表数据 |