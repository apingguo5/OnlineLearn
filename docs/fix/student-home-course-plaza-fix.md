# 修复：学生首页课程广场 500 错误

## 问题描述

学生首页 `home.vue` 的"课程广场"无法显示课程数据，浏览器控制台报错：

```
GET http://localhost:9252/study/subject/findList 500 (Internal Server Error)
```

## 原因分析

后端 `SubjectEntity.java` 通过 `@TableName("course")` 映射到 `course` 表，但实体字段缺少 `@TableField` 注解，导致 MyBatis-Plus 在自动生成 SQL 时字段映射异常。

### 受影响文件

- `backend/src/main/java/com/rabbiter/ol/entity/SubjectEntity.java`

### 关键事实

| 属性 | 说明 |
|------|------|
| 实体类 | `SubjectEntity`（旧名称，映射到 `course` 表） |
| 数据库表 | `course`（从 `subject` 重构而来） |
| 控制器路径 | `/study/subject/findList` |
| 前端调用 | `home.vue` → `getAllCourses()` → `GET /study/subject/findList` |
| 返回值 | `Result` 对象，数据在 `resultData` 字段中 |

## 修复内容

为 `SubjectEntity.java` 中所有字段添加 `@TableField` 注解，显式指定数据库列名：

| Java 字段 | 数据库列名 | 注解 |
|-----------|-----------|------|
| `courseName` | `course_name` | `@TableField("course_name")` |
| `description` | `description` | `@TableField("description")` |
| `creatorId` | `creator_id` | `@TableField("creator_id")` |
| `coverUrl` | `cover_url` | `@TableField("cover_url")` |
| `status` | `status` | `@TableField("status")` |
| `createTime` | `create_time` | `@TableField("create_time")` |
| `updateTime` | `update_time` | `@TableField("update_time")` |

## 验证结果

修复后 `findList` 接口正确执行 SQL：

```sql
SELECT id, course_name, description, creator_id, cover_url, status, create_time, update_time 
FROM course
```

成功返回 2 条课程记录（状态码 200），前端"课程广场"正常显示课程卡片。

## 前端数据流

```
home.vue (fetchCourses)
  → getAllCourses() [courses.js]
    → GET /study/subject/findList [request.js]
      → SubjectController.findList()
        → subjectService.list() [MyBatis-Plus BaseMapper]
          → SubjectDao (BaseMapper<SubjectEntity>)
            → SELECT FROM course
```

### 响应数据格式

后端 `Result` 对象结构：
```json
{
  "resultData": [ /* 课程列表 */ ],
  "code": 200
}
```

### 前端处理逻辑（`home.vue`）

```javascript
fetchCourses() {
    getAllCourses().then(res => {
        // 优先从 res.data.resultData 取数据
        if (res && res.data && res.data.code === 200 && Array.isArray(res.data.resultData)) {
            this.courses = res.data.resultData;
        }
        // 其次从 res.data.data 取
        else if (...) { ... }
        // 兜底处理...
    });
}