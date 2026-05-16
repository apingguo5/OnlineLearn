# 视频播放空白根因分析报告

## 问题现象

在学生界面"正在学习"栏目下，学习"Android Basics with Compose"课程中的视频（本地 `.webm` 文件）显示区域完全空白，浏览器 Network 面板看不到任何 media 资源请求。

## 涉及文件

| 文件 | 作用 |
|------|------|
| `backend/src/main/java/com/rabbiter/ol/controller/StudentCourseChapterController.java` | 后端返回章节内容，将本地路径转为HTTP路径 |
| `backend/src/main/java/com/rabbiter/ol/config/MyWebMvnConfig.java` | 后端静态资源配置：`/**/courses/**` → `file:user.dir/courses/` |
| `frontend/src/views/studentweb/courses/CourseLearning.vue` | 前端视频播放组件，获取URL后生成 `<video>` 标签 |
| `frontend/vue.config.js` | 前端代理配置 |

## 完整数据流分析

### Step 1: 路径存储

本地磁盘文件路径：
```
D:\Code\OnlineLearn\courses\Android\Unit 1 Your first Android app\PATHWAY 1 Introduction to Kotlin\2 Welcome to Android Basics with Compose\Welcome to Android Basics with Compose [lNKk-RSL7wg].webm
```

数据库中 `course_resource.file_url` 存储的是相对路径（从 `courses/` 开始的服务端路径，不带 `D:\Code\OnlineLearn\` 前缀）：
```
courses/Android/Unit 1 Your first Android app/PATHWAY 1 Introduction to Kotlin/2 Welcome to Android Basics with Compose/Welcome to Android Basics with Compose [lNKk-RSL7wg].webm
```

> 注：该路径是通过 `CourseScannerService` 从文件元数据提取后存入数据库的。

### Step 2: 后端查询返回

`ChapterContentDao.xml` 中 `queryListWithDetails` SQL 查询（第38行 `WHERE c.chapter_id = #{chapterId}`) 返回一列数据，其中对于 `content_type = 3`（本地资源类型），通过 `LEFT JOIN course_resource cr3 ON c.content_type = 3 AND c.ref_id = cr3.id` 关联 `course_resource` 表，但 **只返回了 `cr3.resource_name` 作为 `refTitle`**，**并没有返回 `file_url` 字段！**

**这是第一个问题：后端SQL查询没有返回 `file_url`。**

### Step 3: StudentCourseChapterController 处理

控制器中的 `convertToHttpUrl()` 方法负责将本地文件路径转为 HTTP 可访问的 URL。

```java
private String convertToHttpUrl(String fileUrl) {
    if (fileUrl == null || fileUrl.isEmpty()) return "";
    String basePath = System.getProperty("user.dir").replace("\\", "/");
    if (fileUrl.startsWith(basePath)) {
        fileUrl = fileUrl.substring(basePath.length());
    }
    fileUrl = fileUrl.replace("\\", "/");
    if (!fileUrl.startsWith("/")) {
        fileUrl = "/" + fileUrl;
    }
    // 从 course_resource 直接查询 file_url 的完整路径
    if (fileUrl.startsWith("D:/") || fileUrl.startsWith("/D:/")) {
        if (fileUrl.startsWith("/")) fileUrl = fileUrl.substring(1);
        if (fileUrl.startsWith(basePath)) {
            fileUrl = fileUrl.substring(basePath.length());
        }
        if (!fileUrl.startsWith("/")) fileUrl = "/" + fileUrl;
    }
    return "http://localhost:9251" + fileUrl;
}
```

**由于SQL没有返回 `file_url`，该方法的入参可能为空字符串，或者只传入了 `resource_name` 而非完整的 `file_url` 路径。** 即返回的 `http://localhost:9251/courses/...` URL **并不包含原始文件的完整路径**，或者包含但未做URL编码。

### Step 4: 静态资源映射

`MyWebMvnConfig.java` 中：
```java
registry.addResourceHandler("/courses/**")
    .addResourceLocations("file:" + System.getProperty("user.dir") + "/courses/");
```

这表示当收到形如 `http://localhost:9251/courses/Android/Unit 1...` 的请求时，Spring 会映射到 `D:\Code\OnlineLearn\courses\Android\Unit 1...` 路径读取文件。

### Step 5: 前端接收与渲染

前端 `CourseLearning.vue` 的 `openContent()` 方法从 API 响应中拿到 `videoUrl` 或 `fileUrl`，直接赋值给 `<video>` 标签的 `src` 属性：

```javascript
videoUrl: data.videoUrl || (data.resourceType === 3 && data.contentType === 3 ? data.fileUrl : '')
```

## 根本原因（核心缺陷）

路径中的特殊字符未做 URL 编码：

**数据库文件路径中包含以下特殊字符：**

```
courses/Android/Unit 1 Your first Android app/PATHWAY 1 Introduction to Kotlin/2 Welcome to Android Basics with Compose/Welcome to Android Basics with Compose [lNKk-RSL7wg].webm
│                              │                    │                              │                                       └─── 方括号 []（RFC 3986 保留字符）
│                              │                    │                              └─────── 空格（需编码为 %20）
│                              │                    └─────────────────────── 空格（需编码为 %20）
│                              └────────── 空格（需编码为 %20）
└────────────────── 空格（需编码为 %20）
```

**具体缺陷链：**

1. **后端 `convertToHttpUrl()`** 使用简单的字符串拼接生成 URL，对路径中的**空格**和**方括号 `[]`** 没有做任何 URL 编码
   - 空格 → 应编码为 `%20`
   - `[` → 应编码为 `%3B`
   - `]` → 应编码为 `%5D`

2. **生成后的 URL 示例（有问题的）：**
   ```
   http://localhost:9251/courses/Android/Unit 1 Your first Android app/PATHWAY 1 Introduction to Kotlin/2 Welcome to Android Basics with Compose/Welcome to Android Basics with Compose [lNKk-RSL7wg].webm
   ```

3. **浏览器行为**：当 `<video>` 标签的 `src` 被设为上述 URL 时，浏览器解析到路径中的**空格**和**方括号**：
   - **空格** → 浏览器可能会视其为 URL 的分隔符，截断路径
   - **方括号 `[]`** → 在 URL 路径中属于非法字符（RFC 3986），浏览器拒绝发送请求
   - **最终结果**：Network 面板看不到任何 media 请求，播放器显示空白

## 修复方案

### 方案一：后端 encode（推荐）

在 `StudentCourseChapterController.convertToHttpUrl()` 中对路径进行 URL 编码：

```java
private String convertToHttpUrl(String fileUrl) {
    if (fileUrl == null || fileUrl.isEmpty()) return "";
    
    // 先对路径中的特殊字符做 URL 编码
    String encodedUrl = encodeUrlPath(fileUrl);
    
    // ... 后续处理逻辑
    return "http://localhost:9251" + fileUrl;
}

// 对 URL 路径中的特殊字符进行编码，保留 / 分隔符
private String encodeUrlPath(String path) {
    StringBuilder result = new StringBuilder();
    for (char ch : path.toCharArray()) {
        if (ch == '/' || ch == '\\') {
            result.append('/');
        } else if (Character.isLetterOrDigit(ch) || ch == '-' || ch == '_' || ch == '.' || ch == '~') {
            result.append(ch);
        } else {
            result.append('%').append(String.format("%02X", (int) ch));
        }
    }
    return result.toString();
}
```

### 方案二：前端 encode（兜底）

在 `CourseLearning.vue` 的 `openContent()` 中对视频 URL 使用 `encodeURI()` 编码（`encodeURI` 不会编码 `/?&#` 等 URL 结构字符）：

```javascript
if (data.videoUrl || data.fileUrl) {
  this.contentUrl = encodeURI(data.videoUrl || data.fileUrl);
}
```

### 方案三：SQL 查询补充 file_url

`ChapterContentDao.xml` 中需补充 `file_url` 字段返回，因为目前 `content_type=3` 只返回了 `resource_name` 作为 `refTitle`，缺失了关键的 `file_url`。

```sql
SELECT
    c.id,
    c.chapter_id AS chapterId,
    c.content_type AS contentType,
    c.content_title AS contentTitle,
    c.ref_id AS refId,
    c.sort_order AS sortOrder,
    c.create_time AS createTime,
    CASE
        WHEN c.content_type = 1 THEN cr1.resource_name
        WHEN c.content_type = 2 THEN kp.title
        WHEN c.content_type = 3 THEN cr3.resource_name
        ELSE ''
    END AS refTitle,
    CASE
        WHEN c.content_type = 3 THEN cr3.file_url
        ELSE NULL
    END AS fileUrl
FROM chapter_content c
```

## 总结

| 问题点 | 严重程度 | 说明 |
|--------|---------|------|
| 路径有空格/方括号未编码 | **致命** | 浏览器拒绝发送请求，视频空白 |
| SQL 缺 file_url 字段 | **严重** | 即使 URL 编码正确，后端也无法拿到完整路径 |
| `convertToHttpUrl` 无编码逻辑 | **严重** | 路径直接拼接，未考虑特殊字符 |
| 前端未做URL兜底编码 | **一般** | 应加 `encodeURI()` 作为安全兜底 |