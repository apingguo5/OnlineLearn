# 课程资源存储链路打通总结

## 问题背景

需要将本地磁盘上的课程章节资源（视频文件）存储到后端的 `course_resource` 表中，实现完整的增删改查链路。

## 数据库表结构

`course_resource` 表定义（DDL）：

```sql
CREATE TABLE `course_resource` (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '资源ID',
  `course_id` int UNSIGNED NOT NULL COMMENT '所属课程ID',
  `resource_name` varchar(255) NOT NULL COMMENT '资源名称',
  `resource_type` tinyint UNSIGNED NOT NULL COMMENT '资源类型（1: 视频, 2: PDF文档, 3: PPT, 4: 习题集, 5: 其他）',
  `file_url` varchar(500) DEFAULT NULL COMMENT '文件访问URL（对象存储路径）',
  `storage_bucket` varchar(255) DEFAULT NULL COMMENT '存储桶名称',
  `object_key` varchar(500) DEFAULT NULL COMMENT '对象键（文件路径）',
  `file_hash` varchar(64) DEFAULT NULL COMMENT '文件哈希值（用于去重、校验）',
  `file_size` bigint UNSIGNED DEFAULT NULL COMMENT '文件大小（字节）',
  `duration` int UNSIGNED DEFAULT NULL COMMENT '视频/音频时长（秒）',
  `chapter_id` int UNSIGNED DEFAULT NULL COMMENT '所属章节ID',
  `sort_order` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '同一章节内的排序序号',
  `uploader_id` int UNSIGNED DEFAULT NULL COMMENT '上传者ID',
  `is_public` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否公开（0: 仅班级学生, 1: 公开）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  PRIMARY KEY (`id`),
  KEY `idx_course_id` (`course_id`),
  KEY `idx_resource_type` (`resource_type`),
  KEY `idx_chapter_id` (`chapter_id`),
  KEY `idx_sort_order` (`sort_order`),
  KEY `idx_chapter_sort` (`chapter_id`, `sort_order`),
  KEY `idx_uploader_id` (`uploader_id`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_resource_course` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_resource_chapter` FOREIGN KEY (`chapter_id`) REFERENCES `course_chapter` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_resource_uploader` FOREIGN KEY (`uploader_id`) REFERENCES `user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程资源表';
```

## 核心问题

关键问题在于 `course_resource` 需要通过 `course_id` 关联到课程，但 `course_chapter` 表只有 `class_id` 字段（并没有直接存储 `course_id`）。因此需要通过一个3表 JOIN 来获取章节对应的课程ID：

```
course_chapter.class_id → class.id → class.course_id
```

## 修改文件清单

### 1. `CourseResourceEntity.java` — 实体类对齐

**文件路径**: `backend/src/main/java/com/rabbiter/ol/entity/CourseResourceEntity.java`

- 使用 `@TableName("course_resource")` 注解映射到新表名
- 字段对齐 DDL 中的所有列：
  - `id`, `courseId`, `resourceName`, `resourceType`, `fileUrl`
  - `storageBucket`, `objectKey`, `fileHash`, `fileSize`, `duration`
  - `chapterId`, `sortOrder`, `uploaderId`, `isPublic`, `createTime`, `updateTime`
- 所有字段提供 getter/setter

### 2. `CourseResourceDao.java` — 数据访问层

**文件路径**: `backend/src/main/java/com/rabbiter/ol/dao/CourseResourceDao.java`

新增以下方法：

| 方法 | 功能 |
|------|------|
| `listByChapterId(Integer chapterId)` | 根据章节ID查询资源列表 |
| `listByCourseId(Integer courseId)` | 根据课程ID查询资源列表 |
| `getMaxSortOrder(Integer chapterId)` | 获取章节内的最大排序值 |
| `updateSortOrder(Integer id, Integer sortOrder)` | 更新排序 |
| `existsByFilePath(String filePath)` | 检查文件路径是否已存在（用于去重） |

### 3. `CourseResourceDao.xml` — MyBatis Mapper

**文件路径**: `backend/src/main/resources/mapper/CourseResourceDao.xml`

- 定义完整的 `<resultMap>` 映射所有字段到实体属性
- 实现 `listByChapterId` — 按 `sort_order, id` 升序排列
- 实现 `listByCourseId` — 按 `create_time` 降序排列
- 实现 `getMaxSortOrder` — `COALESCE(MAX(sort_order), -1)` 防止空值
- 实现 `updateSortOrder` — 同时更新 `update_time = NOW()`
- 实现 `existsByFilePath` — `COUNT(1) > 0` 返回布尔值

### 4. `CourseResourceService.java` — 服务接口

**文件路径**: `backend/src/main/java/com/rabbiter/ol/service/CourseResourceService.java`

提供完整 CRUD 接口：
- `listByChapterId`, `listByCourseId` — 查询
- `addLocalResource` — 创建资源
- `deleteResource`, `batchDeleteResources` — 删除
- `updateResource` — 更新
- `sortResources` — 重新排序
- `getCourseIdByChapterId` — **核心**：通过章节ID获取课程ID
- `scanAndImportLocalResources` — 批量扫描导入
- `searchResources` — 条件检索
- `existsByFilePath` — 检查文件是否已导入

### 5. `CourseResourceServiceImpl.java` — 服务实现（核心修复）

**文件路径**: `backend/src/main/java/com/rabbiter/ol/service/impl/CourseResourceServiceImpl.java`

**关键修复 — `getCourseIdByChapterId` 方法**：

```java
@Override
public Integer getCourseIdByChapterId(Integer chapterId) {
    try {
        return jdbcTemplate.queryForObject(
            "SELECT c.course_id FROM course_chapter cc " +
            "JOIN `class` c ON cc.class_id = c.id " +
            "WHERE cc.id = ?",
            Integer.class,
            chapterId
        );
    } catch (Exception e) {
        throw new RuntimeException("无法获取章节所属课程ID：章节(chapterId=" + chapterId + ")不存在或关联数据异常", e);
    }
}
```

- 使用 `JdbcTemplate` 执行 3表 JOIN 查询
- 通过 `course_chapter.class_id → class.id → class.course_id` 链路获取课程ID
- 异常时抛出带具体参数的运行时异常，便于排查

**其他方法实现**：
- `addLocalResource` — 新增资源，自动设置 `courseId`, `sortOrder`（最大值+1）, `createTime`
- `updateResource` — 仅更新提供的非空字段（名称、类型、路径、排序、公开、章节、时长、大小）
- `sortResources` — 按传入ID列表顺序重新编号 `sortOrder`
- `scanAndImportLocalResources` — 扫描目录，按扩展名识别类型（视频/PDF/PPT等），跳过已导入文件
- `searchResources` — 按章节/课程/资源类型条件过滤

### 6. `CourseResourceController.java` — REST API 控制器

**文件路径**: `backend/src/main/java/com/rabbiter/ol/controller/CourseResourceController.java`

| API 端点 | 方法 | 功能 |
|----------|------|------|
| `/study/teacher/course/resource/listByChapter` | POST | 获取章节资源列表 |
| `/study/teacher/course/resource/listByCourse` | POST | 获取课程资源列表 |
| `/study/teacher/course/resource/addLocal` | POST | 添加本地路径资源（测试用，自动提取名称） |
| `/study/teacher/course/resource/add` | POST | 添加资源（通用，完整字段） |
| `/study/teacher/course/resource/update` | POST | 更新资源 |
| `/study/teacher/course/resource/delete` | POST | 删除资源 |
| `/study/teacher/course/resource/batchDelete` | POST | 批量删除资源 |
| `/study/teacher/course/resource/sort` | POST | 更新资源排序 |
| `/study/teacher/course/resource/search` | POST | 按条件检索资源 |

`/addLocal` 端点中注入了 `CourseResourceDao` 用于获取排序值，并通过 `courseResourceService.getCourseIdByChapterId()` 获取课程ID。

### 7. `CourseChapterController.java` — 章节控制器（已有方法复用）

**文件路径**: `backend/src/main/java/com/rabbiter/ol/controller/CourseChapterController.java`

原有 `/addLocalResource` 端点已正确使用 `courseResourceService.getCourseIdByChapterId()`，该方法自动创建 `chapter_content` 记录关联资源与章节。

## 测试验证结果

### 插入测试

请求：
```json
POST /study/teacher/course/addLocalResource
{
  "chapterId": 3,
  "resourceName": "Welcome to Android Basics with Compose",
  "localPath": "D:/Code/OnlineLearn/courses/Android/Unit 1 Your first Android app/PATHWAY 1 Introduction to Kotlin/2 Welcome to Android Basics with Compose/Welcome to Android Basics with Compose [lNKk-RSL7wg].webm",
  "uploaderId": 1
}
```

SQL 执行日志：
```
INSERT INTO course_resource (course_id, resource_name, resource_type, file_url, chapter_id, is_public, create_time)
VALUES (3, 'Welcome to Android Basics with Compose', 5, 'D:/Code/...webm', 3, 0, '2026-05-13 19:06:51')
```

响应成功：
```json
{
  "resultData": {
    "resourceId": 4,
    "contentId": 1,
    "resourceName": "Welcome to Android Basics with Compose",
    "fileUrl": "D:/Code/OnlineLearn/..."
  },
  "code": 200
}
```

同时自动在 `chapter_content` 表中创建了关联记录（`content_type=3` 本地路径类型）。

## API 完整使用示例

### 插入资源
```bash
curl -X POST http://localhost:9251/study/teacher/course/addLocalResource \
  -H "Content-Type: application/json" \
  -d '{"chapterId":3,"resourceName":"视频标题","localPath":"D:/path/to/file.webm","uploaderId":1}'
```

### 按章节查询资源
```bash
curl -X POST http://localhost:9251/study/teacher/course/resource/search \
  -H "Content-Type: application/json" \
  -d '{"chapterId":3}'
```

### 按类型过滤查询
```bash
curl -X POST http://localhost:9251/study/teacher/course/resource/search \
  -H "Content-Type: application/json" \
  -d '{"chapterId":3, "resourceType":1}'
```

### 更新资源（仅更新指定字段）
```bash
curl -X POST http://localhost:9251/study/teacher/course/resource/update \
  -H "Content-Type: application/json" \
  -d '{"id":4,"resourceName":"新名称","resourceType":1}'
```

### 删除资源
```bash
curl -X POST http://localhost:9251/study/teacher/course/resource/delete \
  -H "Content-Type: application/json" \
  -d '{"id":4}'
```

## 注意事项

1. **表关联路径**：`course_chapter` 没有直接存 `course_id`，需要通过 `class` 表中转
2. **资源类型**: 视频=1, PDF=2, PPT=3, 习题集=4, 其他=5
3. **排序值**：新资源自动取当前章节最大排序值+1
4. **去重**：`existsByFilePath` 检查同一路径是否已导入
5. **事务**：写入操作均标注 `@Transactional(rollbackFor = Exception.class)`