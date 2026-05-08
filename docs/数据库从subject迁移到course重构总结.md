# 数据库从 subject 迁移到 course 重构总结

> 修改日期：2026-05-08
> 对应需求：全面移除旧表 subject 相关内容，基于新数据库结构重构后端代码

---

## 一、重构背景

原数据库设计中，课程体系使用 `subject`（科目）表作为顶层实体，`class`（班级）通过 `subject_id` 关联科目，教学内容通过 `video_total`（视频总集）和 `videos`（视频分集）组织。该设计存在以下问题：

1. **语义不清晰**：`subject` 无法准确表达"课程"概念，与业务实际不符
2. **资源管理碎片化**：视频、文档、习题等资源没有统一管理表
3. **章节结构缺失**：缺少通用的章节目录结构，仅支持视频的二级分层
4. **用户-科目关联冗余**：`user_subject` 表在实际业务中无对应场景

本次重构将 `subject` 全面替换为 `course`（课程），并引入 `course_chapter`（课程章节）、`course_resource`（课程资源）表，统一管理所有教学资源。

---

## 二、数据库表变更

### 新增表（3张）

| 表名 | 说明 | 关键字段 |
|------|------|----------|
| `course` | 课程表（替代 subject） | id, course_name, description, creator_id, cover_url, status, create_time, update_time |
| `course_chapter` | 课程章节表（替代 video_total） | id, course_id, chapter_name, parent_id, sort_order, creator_id, create_time |
| `course_resource` | 课程资源表（替代 videos，统一管理视频/文档/习题等） | id, course_id, resource_name, resource_type(1视频/2PDF/3PPT/4习题/5其他), file_url, storage_bucket, object_key, file_hash, file_size, duration, chapter_id, sort_order, uploader_id, is_public, create_time |

### 删除表（3张）

| 表名 | 原因 |
|------|------|
| `subject` | 由 `course` 替代 |
| `user_subject` | 业务中无实际使用场景，移除冗余关联 |
| `video_total` / `videos` | 由 `course_chapter` + `course_resource` 替代 |

### 修改表（2张）

| 表名 | 变更内容 |
|------|----------|
| `class` | 新增 `course_id` 外键（关联 course.id），移除 `subject_id` 字段，新增 `academic_year`、`semester`、`max_students` 字段 |
| `ask_questions` | `video_id` → `resource_id`，外键从 `videos` 改为关联 `course_resource` |
| `video_watch_record` | `video_id` → `resource_id`，外键从 `videos` 改为关联 `course_resource` |

---

## 三、后端代码变更

### 新增文件（0个）- 复用/重写现有 Entity

| 变更文件 | 变更内容 |
|----------|----------|
| `SubjectEntity.java` | 表映射从 `subject` 改为 `course`；字段重构：`subjectName` → `courseName`，`userId` → `creatorId`；新增 `description`、`coverUrl`、`status`、`createTime`、`updateTime` |
| `ClassEntity.java` | `subjectId` → `courseId`；新增 `@TableField("course_id")`、`@TableField("create_time")` 注解 |
| `SubjectDao.xml` | 更新 SQL 查询映射，适配 `course` 表字段名 |
| `SubjectVo.java` | 更新 VO 字段，适配新的 Entity 结构 |
| `ClassDao.xml` | 更新 SQL 查询映射，引用 `course_id` 替代 `subject_id` |
| `TeacherDashboardController.java` | 全面重写：移除所有 subject 相关代码，改用 course 查询；替换视频资源查询逻辑；适配新 chapter/resource 结构 |
| `UserController.java` | 移除 subject 相关接口调用 |

### 删除文件（7个）

| 删除文件 | 说明 |
|----------|------|
| `UserSubjectController.java` | 用户-科目关联控制器，已无对应表 |
| `UserSubjectDao.java` | DAO 层 |
| `UserSubjectEntity.java` | 实体类 |
| `UserSubjectService.java` | 服务接口 |
| `UserSubjectServiceImpl.java` | 服务实现 |
| `UserSubjectVo.java` | VO 类 |
| `UserSubjectDao.xml` | MyBatis XML 映射 |

### 删除 SQL 脚本（3个）

| 删除文件 | 说明 |
|----------|------|
| `create_video_watch_record.sql` | 已合并入 `online_learn.sql` |
| `migration_v2.sql` | 已整合进基线与新设计冲突 |
| `migration_v3.sql` | 已整合进基线与新设计冲突 |

---

## 四、前端代码变更

| 变更文件 | 变更内容 |
|----------|----------|
| `teacherApi.js` | 所有 API 调用中 `subjectId` → `courseId`；更新 `saveCourse` 接口字段名；移除 subject 相关接口 |
| `TeacherCourseManagement.vue` | 适配新 course 结构，创建课程时使用 `course_name`、`description`、`cover_url`、`creator_id` 等新字段；章节树改用 `course_chapter`；资源管理改用 `course_resource` |
| `TeacherDashboard.vue` | 统计分析改用 course 数据源；移除 subject 相关统计 |
| `TeacherClassManagement.vue` | 班级管理改用 `course_id` 关联课程 |
| `Login.vue` | 适配后端返回数据结构的变更 |
| `router/index.js` | 路由配置调整 |
| `request.js` | 请求拦截器调整 |
| `vue.config.js` | 代理配置调整 |

---

## 五、实体关系变更对照

### 旧关系结构
```
subject (科目)
  ├── user_subject (用户-科目关联) [已移除]
  ├── video_total (视频总集) [已移除]
  │     └── videos (视频分集) [已移除]
  └── class (班级) [subject_id 关联]
```

### 新关系结构
```
course (课程)
  ├── course_chapter (章节，支持无限层树状结构)
  │     └── course_resource (资源，通过 chapter_id 关联)
  ├── course_resource (资源，通过 course_id 直接关联)
  └── class (班级，通过 course_id 关联)
        └── user_class (班级成员)
```

---

## 六、关键设计决策

1. **`SubjectEntity.java` 保留文件名，改表映射**：为避免大规模文件重命名造成 git 历史混乱，保留 `SubjectEntity.java` 文件名，通过 `@TableName("course")` 注解映射到 `course` 表
2. **`course_resource` 统一资源管理**：用 `resource_type` 字段区分视频/PDF/PPT/习题集等，替代旧设计中 `videos` 表仅支持视频的局限
3. **`course_chapter` 树状结构**：通过 `parent_id` + `sort_order` 支持无限层级树状章节，替代旧设计中 `video_total` → `videos` 的固定二级结构
4. **外键完整性**：所有新表均设置外键约束和索引，保证数据完整性
5. **兼容性**：`ask_questions` 和 `video_watch_record` 表的 `video_id` 统一改为 `resource_id`，语义更清晰且支持所有资源类型

---

## 七、后续待办

- [ ] 验证旧数据迁移脚本：将现有 subject、video_total、videos 数据迁移到 course、course_chapter、course_resource
- [ ] 学生端适配：`StudentCourses.vue` 等学生端页面需更新为新的表结构
- [ ] 删除废弃代码：全面搜索项目中所有引用旧字段名（`subjectId`、`video_id`、`subject_name` 等）的代码并清理
- [ ] 单元测试：为新的 Controller/Service 层编写/更新测试用例