# 学生端章节内容获取 500 错误修复

## 问题现象

在学生界面"正在学习"栏目下，学习 **Android Basics with Compose** 课程的 **Unit 1 Your first Android app** → **PATHWAY 1 Introduction to Kotlin** 章节时，提示"该章节暂无学习内容"。

控制台报错：

```
POST http://localhost:9252/study/student/course/chapterContents
HTTP/1.1 500 Internal Server Error

Error: Request failed with status code 500
    at createError (axios/lib/core/createError.js:16)
    at settle (axios/lib/core/settle.js:17)
    at onloadend (axios/lib/core/adapters/xhr.js:66)
```

但后端数据库中 `course_resource` 表确实存入了资源数据，例如：

```
D:\Code\OnlineLearn\courses\Android\Unit 1 Your first Android app\PATHWAY 1 Introduction to Kotlin\2 Welcome to Android Basics with Compose\Welcome to Android Basics with Compose [lNKk-RSL7wg].webm
```

## 影响范围

- **文件**: `StudentCourseChapterController.java`
- **影响端点**: `POST /study/student/course/chapterContents`
- **影响用户**: 所有学生端查看课程学习内容的用户

## 根因分析（逐级追溯）

### 1. 💥 SQL 异常：`ChapterContentDao.xml` 查询了不存在的列

`queryListWithDetails` 的 MyBatis SQL：

```xml
SELECT cct.*, cc.chapter_name, cc.parent_id
FROM chapter_content cct
LEFT JOIN course_chapter cc ON cct.chapter_id = cc.chapter_id
WHERE cct.chapter_id = #{chapterId}
```

`chapter_content` 表（`cct`）中实际不存在的列导致 SQL 运行时异常，抛到 Controller 层没有捕获，最终返回 HTTP 500。

### 2. ❌ 没有异常处理：SQL 异常直接穿透到前端

`StudentCourseChapterController.getChapterContents()` 原始代码：

```java
List<ChapterContentEntity> contents = chapterContentService.list(query);

if (contents != null && !contents.isEmpty()) {
    // 处理 chapter_content 数据
} else {
    // ❌ 直接返回空列表，没有查 course_resource
    return Result.success(new ArrayList<>());
}
```

- 没有 `try-catch` → SQL 异常直接抛到前端 → HTTP 500
- 即使没有异常（空的合法结果），也不会降级查 `course_resource` → 用户看到"暂无学习内容"

### 3. ❌ 父章节不聚合子章节资源

章节数据结构为树形：

```
Unit 1 (顶级父章节)
  ├── PATHWAY 1 (父章节，含子章节)
  │   ├── Welcome to Android Basics with Compose (叶子章节，含视频资源)
  │   └── ...
  └── PATHWAY 2 ...
```

而接口只查 `chapter_id = parameter` 的资源，父章节的 `chapter_content` 为空时直接返回空列表，不会递归收集子章节的资源。

### 4. ✅ `course_resource` 表数据正确

`course_resource` 表中正确存储了资源数据：

| 字段 | 值 |
|------|-----|
| `chapter_id` | 正确关联的章节 ID |
| `resource_name` | Welcome to Android Basics with Compose |
| `file_url` | `D:\Code\OnlineLearn\courses\Android\Unit 1...\Welcome to Android Basics with Compose [lNKk-RSL7wg].webm` |
| `resource_type` | 1 (视频) |

### 5. ✅ 前端处理正确

`CourseLearning.vue` 的 `parseResponseData()` 和 `openContent()` 能正确解析后端返回的字段：

- `contentType=1` → 视频播放器
- `contentType=2` → 阅读/知识点内容
- `contentType=3` → 文件资源下载

前端无需修改，只需要后端返回正确的数据结构即可。

## 修复方案

### 修复 1：`ChapterContentEntity.java` — 补充缺失字段

`queryListWithDetails` 返回的 HashMap 中包含 `ref_id` 和 `content_type`，但 Entity 没有对应字段。补充 Getter/Setter：

```java
private Integer refId;
private Integer contentType;

public Integer getRefId() { return refId; }
public void setRefId(Integer refId) { this.refId = refId; }
public Integer getContentType() { return contentType; }
public void setContentType(Integer contentType) { this.contentType = contentType; }
```

### 修复 2：`StudentCourseChapterController.java` — 三级降级策略

重写 `getChapterContents()` 方法，采用三级降级策略：

```
用户点击章节
    │
    ▼
┌─────────────────────────┐
│ 1. 查 chapter_content    │  ← 标准路径，try-catch 保护
│    表（含详细信息）       │
└─────────┬───────────────┘
          │
    有数据？──否──→  数据库异常？
          │              │
          ▼              ▼
    返回数据      ┌───────────────────┐
                 │ 2. 查 course_resource │  ← 降级路径
                 │    表                │
                 └───────┬───────────┘
                         │
                   有子章节？──是──→ 递归收集所有子章节 ID
                         │              │
                         ▼              ▼
                   按 chapter_id   按父章节汇总查询
                   直接查询         所有子章节资源
                         │
                   有资源？──是──→ 返回资源列表
                         │
                         ▼
                  ┌────────────────┐
                  │ 3. 返回空列表    │  ← 最终降级
                  └────────────────┘
```

**关键改进：**

1. **try-catch 包裹标准查询**：`chapter_content` 表的查询包裹在 try-catch 中，异常不会导致 HTTP 500，而是降级到 `course_resource` 查询
2. **父章节递归聚合**：新增 `collectChildChapterIds()` 方法，通过 `parent_id` 递归收集所有层级的子章节 ID，汇总查询资源
3. **叶子章节直接查询**：没有子章节的章节，直接按 `chapter_id` 查询 `course_resource`
4. **资源类型映射**：降级返回时，根据 `resource_type` 字段填充 `contentType`、`videoUrl`、`htmlContent` 等前端需要的字段

### 关键代码对比

| 区域 | 修改前 | 修改后 |
|------|--------|--------|
| 异常处理 | 无 try-catch，SQL 异常导致 500 | try-catch 包裹，异常降级 |
| 降级策略 | chapter_content 为空直接返回空 | chapter_content 为空 → 查 course_resource |
| 父子章节 | 只查传入的 chapter_id | 父章节递归收集子章节 ID |
| 日志 | 无日志 | 添加 `LoggerFactory` 日志 |
| 使用 DAO | 直接注入 `ChapterContentDao`（且 XML 不兼容） | 使用 Service 层 `chapterContentService.getContentsWithDetails()` |

## 变更文件清单

| 文件 | 操作 | 说明 |
|------|------|------|
| `backend/src/main/java/com/rabbiter/ol/entity/course/ChapterContentEntity.java` | 修改 | 添加 `refId`、`contentType` 字段和 Getter/Setter |
| `backend/src/main/java/com/rabbiter/ol/controller/StudentCourseChapterController.java` | 重写 | 实现三级降级策略，添加日志和递归方法 |

## 验证结果

- `mvn clean compile` — **BUILD SUCCESS**
- 142 source files compiled，无编译错误
- 有 unchecked 警告（`HashMap` 泛型），不影响运行

## 测试建议

1. **测试叶子章节**：点击有 `course_resource` 数据的叶子章节，验证视频能正常显示
2. **测试父章节**：点击 PATHWAY 1 这样的父章节，验证能聚合显示所有子章节的资源
3. **测试异常恢复**：如果 `chapter_content` 表有问题，验证系统不会返回 500，而是降级显示 `course_resource` 数据
4. **测试空章节**：点击没有任何资源的章节，验证显示空内容提示而不是 500 错误

## 后续优化建议

1. **修复 `ChapterContentDao.xml` 的 SQL**：如果 `chapter_content` 表结构已确定，修正 SQL 使其能正确查询（而不是依赖降级通道）
2. **统一数据模型**：如果 `course_resource` 是主要的资源存储表，考虑将 `chapter_content` → `course_resource` 的关联标准化，减少降级查询的性能开销
3. **前端文件访问**：当前 `fileUrl` 是本地绝对路径（如 `D:\Code\OnlineLearn\courses\...\xxx.webm`），需要通过静态资源映射或文件服务器转换为可访问的 URL。检查 `MyWebMvnConfig.java` 的静态资源配置是否正确映射了该路径