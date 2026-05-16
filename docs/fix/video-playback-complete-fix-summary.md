# 视频播放本地路径修复完整总结

## 问题概述

学生端在学习界面点击章节内容时，本地 WebM/MP4 视频文件（存储在 `D:\Code\OnlineLearn\courses\` 目录下）无法播放，页面显示空白，浏览器控制台 Network 面板未发起任何媒体资源请求。

## 问题表现

1. 视频弹窗打开后 `<video>` 标签的 `src` 为空或无效
2. 浏览器 Network 面板看不到对 `/courses/...` 的任何请求
3. 控制台无 404 错误，后端也无日志
4. 页面 `<video>` 区域显示为黑色空白

## 数据流追踪分析

完整的请求链路如下：

```
用户点击内容卡片
  → openContent(content)
  → this.currentVideoUrl = content.videoUrl
  → <video :src="currentVideoUrl">
  → 浏览器发起 GET /courses/... 请求
  → vue.config.js 代理 /courses → http://localhost:9251
  → CourseFileController.serveCourseFile()
  → 读取本地文件系统并返回文件内容
  → 视频播放
```

## 根因分析（两阶段修复）

### 阶段一：本地路径未转换为 HTTP URL

**问题位置**：`StudentCourseChapterController.getChapterContents()`（第 2️⃣ 降级路径分支）

**问题现象**：从 `course_resource` 表查询到的 `fileUrl` 字段存储的是 Windows 本地绝对路径，例如：
```
D:\Code\OnlineLearn\courses\Android\Unit 1 Your first Android app\... [lNKk-RSL7wg].webm
```

**问题原因**：后端直接将本地路径作为 `videoUrl` 返回给前端。前端 `<video>` 标签尝试加载该路径时，浏览器无法识别 `file://` 协议（或由于安全策略被禁止），导致播放失败。

**修复方案**（已在 `StudentCourseChapterController` 中实现）：
- 新增 `convertToHttpUrl()` 方法，将本地路径转换为 HTTP 可访问的相对路径：
  - 原始：`D:\Code\OnlineLearn\courses\Android\Unit 1...\... [lNKk-RSL7wg].webm`
  - 转换：`/courses/Android/Unit%201.../...%5BlNKk-RSL7wg%5D.webm`
- 新增 `encodeUrlPath()` 方法，对路径中的特殊字符（空格、方括号、括号等）进行单次 URL 编码
- 在 `contentType == 3`（本地资源）分支中调用转换，同时自动识别 `.webm/.mp4/.mkv` 等视频扩展名，将 `contentType` 覆写为 `1`（视频类型）

### 阶段二：前端双重编码导致 URL 损坏

**问题位置**：`CourseLearning.vue` 的 `openContent()` 方法

**问题现象**：修复阶段一后，Netowrk 面板可以看到请求，但请求的 URL 是错误的（如 `/courses/Android/Unit%25201...` 中的 `%2520` 是 `%20` 的双重编码），导致 `CourseFileController` 解码后路径匹配不上实际文件，返回 404。

**问题原因**：后端 `convertToHttpUrl()` 已对路径进行了 `URLEncoder.encode()` 编码（如空格 → `%20`，方括号 → `%5B`、`%5D`），但前端 `openContent()` 中又调用了 `encodeURI()` 对已经编码的 URL 进行二次编码，导致：
```
原始空格 → 后端编码为 %20 → 前端 encodeURI → %2520
原始 [    → 后端编码为 %5B → 前端 encodeURI → %255B
```

传递给 `<video>` 的 `src` 最终为 `/courses/Android/Unit%25201...`，后端 `URLDecoder.decode()` 将其解码为 `/courses/Android/Unit%201...`，与实际文件目录名 `Unit 1...` 不匹配。

**修复方案**（已在 `CourseLearning.vue` 中实现）：
- 在 `openContent()` 中**移除** `encodeURI()` 调用，直接使用后端返回的已编码 URL
- 添加注释说明：后端已对 URL 进行单次编码，前端不应再次编码
- 同时在 `loadContents()` 中保留视频文件后缀自动识别兜底逻辑（防止后端遗漏）

## 涉及文件及修改内容

### 1. 后端：`StudentCourseChapterController.java`

| 方法 | 修改内容 |
|------|---------|
| `convertToHttpUrl()` | 新增。提取本地路径中 `/courses/` 后的相对路径，进行 URL 编码 |
| `encodeUrlPath()` | 新增。对路径分段编码并保留 `/` 分隔符 |
| `getChapterContents()` | 在 `contentType == 3` 分支中调用 `convertToHttpUrl()`；自动识别 `.webm/.mp4/.mkv/.avi/.mov` 为视频类型 |
| `getChapterContents()` | 在降级分支中也同样处理视频文件识别 |

### 2. 前端：`CourseLearning.vue`

| 方法 | 修改内容 |
|------|---------|
| `openContent()` | **移除** `encodeURI()` 调用，直接使用后端返回的 URL |
| `openContent()` | 添加注释明确说明编码策略 |
| `loadContents()` | 保留视频后缀自动识别作为安全兜底 |

## 新增组件

### `CourseFileController.java`（新控制器）

**职责**：处理所有 `/courses/**` 路径的静态文件请求，替代 Spring ResourceHandler。

**为什么不用 Spring ResourceHandler**：
- Spring 内部使用 `java.net.URL` 构造 `file:` URI
- `java.net.URL` 不允许路径中包含 `[ ] ( )` 等字符
- 即使文件真实存在，也会抛出 `MalformedURLException`

**功能特点**：
- 直接使用 `java.io.File` / `java.nio.file.Path` 操作文件系统，绕过 `java.net.URL` 限制
- 支持 HTTP Range 请求（206 Partial Content），实现视频拖拽/快进
- 路径穿越防护（对拼接路径做 `normalize()` 和前缀检查）
- 自动探测 MIME 类型（使用 `Files.probeContentType()`）

## 代理配置（无需修改）

`vue.config.js` 中已有的代理配置：
```javascript
'/courses': {
    target: 'http://localhost:9251/',
    changOrigin: true
}
```

该代理将前端请求 `/courses/Android/Unit%201...` 转发到后端 `http://localhost:9251/courses/Android/Unit%201...`，由 `CourseFileController` 处理。

## 路径处理流程图

```
数据库 file_url
  ↓
D:\Code\OnlineLearn\courses\Android\...\Welcome to Android Basics with Compose [lNKk-RSL7wg].webm
  ↓  StudentCourseChapterController.convertToHttpUrl()
  ↓  ① 提取 /courses/ 之后的部分
  ↓  ② 对路径分段进行 URLEncoder.encode()
  ↓
/courses/Android/Unit%201%20Your%20first%20Android%20app/...%5BlNKk-RSL7wg%5D.webm
  ↓  (JSON 响应返回给前端)
  ↓
前端 <video :src="currentVideoUrl">
  ↓  (浏览器发起 GET 请求)
  ↓
vue.config.js 代理 → http://localhost:9251/courses/...
  ↓
CourseFileController.serveCourseFile()
  ↓  ① URLDecoder.decode() 路径
  ↓  ② Paths.get(COURSES_ROOT, decodedPath)
  ↓  ③ 读取文件并写出
  ↓
视频播放成功 ✅
```

## 编码策略规范

| 环节 | 编码操作 | 说明 |
|------|---------|------|
| 数据库 `file_url` | 无编码 | 存储原始 Windows 路径 |
| 后端 `convertToHttpUrl()` | URLEncoder.encode() 一次 | 空格→%20，方括号→%5B/%5D |
| 后端 `encodeUrlPath()` | 分段编码，保留 `/` | 确保路径分隔符不被编码 |
| 后端 JSON 序列化 | 无额外编码 | `@ResponseBody` 自动处理 |
| 前端赋值 `videoUrl` | **不调用 encodeURI()** | 直接使用后端返回值 |
| 浏览器发起请求 | 自动处理 | 对已编码 URL 不再二次编码 |
| `CourseFileController` | URLDecoder.decode() | 还原为原始文件名 |

## 关键注意事项

1. **禁止双重编码**：后端已编码的 URL（包含 `%20`、`%5B` 等），前端绝不能再调用 `encodeURI()`，否则 `%20` 会变为 `%2520`
2. **视频扩展名识别**：后端自动识别 `.webm`、`.mp4`、`.mkv`、`.avi`、`.mov`，同时前端 `loadContents()` 和 `openContent()` 中也保留兜底识别逻辑
3. **Range 请求支持**：`CourseFileController` 正确处理 HTTP Range 头，支持视频拖拽进度条
4. **路径穿越防护**：`CourseFileController` 对拼接后的路径做 `normalize()` 并检查是否仍以 `COURSES_ROOT` 开头
5. **MIME 类型探测**：使用 JDK 的 `Files.probeContentType()` 自动设置 Content-Type，确保浏览器正确解析视频流
6. **WebM 浏览器兼容性**：WebM 格式在 Chrome/Firefox/Edge 中原生支持，无需额外插件

## 回归测试要点

1. 点击视频内容卡片 → 弹窗 → 视频自动播放（检查 Network 请求 `/courses/...`）
2. 视频拖动进度条 → 检查 206 Partial Content 响应
3. 含有方括号 `[]`、空格、括号的文件名 → 正确播放
4. 知识点/阅读类型内容 → 正常显示
5. 文件资源类型内容 → 正确生成下载链接
6. 章节无内容 → 显示"暂无学习内容"提示

## 相关文档

- [视频播放本地路径修复方案](video-playback-local-path-fix.md) - 阶段一修复详情
- [`convertToHttpUrl` 双重编码根因分析](video-blank-local-path-root-cause.md) - 阶段二根因分析
- [学生端章节内容 500 错误修复](student-chapter-content-500-error-fix.md)
- [课程资源存储链接修复](course-resource-storage-link-fix.md)