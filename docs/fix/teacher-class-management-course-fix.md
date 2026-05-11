# 教师班级管理课程选择功能修复

## 问题描述

教师在创建班级时，"选择课程"下拉列表无法加载课程数据；即使手动加载也无法正确保存班级与课程的关联。

## 根本原因分析

### 1. 后端实体字段与数据库列不匹配

`ClassEntity.java` 中使用了 `subjectId` 字段，但数据库 `class` 表的关联列名为 `course_id`，导致 MyBatis Plus 无法正确映射字段：

| 数据库列 | 原有字段（错误） | 修复后字段（正确） |
|---|---|---|
| `course_id` | `subjectId` | `courseId` |
| `academic_year` | 缺失 | `academicYear` |
| `semester` | 缺失 | `semester` |
| `max_students` | 缺失 | `maxStudents` |

此外，实体中错误地包含了 `status`（班级状态）字段，而数据库 `class` 表并无此列。

### 2. MyBatis XML 配置文件同步错误

`ClassDao.xml` 中 insert/update SQL 语句使用了不存在的列名（`status`、`subject_id`），导致数据库操作报错。

### 3. 前端课程列表响应解析路径错误

前端 `TeacherClassManagement.vue` 的 `loadCourses()` 方法通过 `teacherApi.getCourses()` 调用 `GET /study/teacher/dashboard/subjects`，后端返回格式为：

```json
{ "code": 0, "data": { "list": [...] } }
```

但前端原解析路径与后端实际返回格式不匹配，导致 `courseList` 始终为空数组。

## 修改清单

### 文件 1: `backend/src/main/java/com/rabbiter/ol/entity/ClassEntity.java`

- 替换 `subjectId` → `courseId`（类型 `Integer`，注解 `@TableField("course_id")`）
- 新增 `academicYear`（类型 `String`，注解 `@TableField("academic_year")`）
- 新增 `semester`（类型 `Integer`，注解 `@TableField("semester")`）
- 新增 `maxStudents`（类型 `Integer`，注解 `@TableField("max_students")`）
- 移除不存在的 `status` 字段

### 文件 2: `backend/src/main/resources/mapper/ClassDao.xml`

| SQL 片段 | 修改内容 |
|---|---|
| `resultMap` | 增加 `course_id`/`academic_year`/`semester`/`max_students` 映射，移除 `subject_id`/`status` |
| `baseQuery` SQL 片段 | 增加 `course_id`/`academic_year`/`semester`/`max_students` 查询列，移除 `subject_id`/`status` |
| JOIN 关联 | 改为 LEFT JOIN `course` co ON co.id = c.course_id |

### 文件 3: `frontend/src/views/teacher/TeacherClassManagement.vue`

- 修正 `loadCourses()` 响应解析路径：`res.data.data.list`
- 表单数据默认值与后端字段对齐（`academicYear`、`semester`、`maxStudents`、`courseId`）

### 文件 4: `frontend/src/api/teacher/teacherApi.js`

- 更新 `getCourses()`、`createClass()` 等 API 的 JSDoc 注释，明确标注响应数据格式

## 数据流验证

```
前端 TeacherClassManagement.vue
  ↓ teacherApi.getCourses()
  ↓ GET /study/teacher/dashboard/subjects
后端 TeacherDashboardController.subjects()
  ↓ subjectService.lambdaQuery().list()  ← 查询 course 表
  ↓ 返回 { code: 0, data: { list: [课程列表] } }
前端 loadCourses() 解析 res.data.data.list  ← 正确加载下拉列表
  ↓
用户选择课程，填写班级信息
  ↓ teacherApi.createClass({ courseId, className, userId, ... })
  ↓ POST /study/class/save
后端 ClassController.save()
  ↓ ClassEntity 字段正确映射到数据库 course_id 列
数据库 class 表写入成功
```

## 影响范围

- 仅影响教师端「班级与学生管理」页面的创建班级功能
- 数据库 `class` 表结构未变更，已存在的班级数据不受影响
- 班级列表查询（`findList`）使用的 JOIN 语句兼容新旧数据