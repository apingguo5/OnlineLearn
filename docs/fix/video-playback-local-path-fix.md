# 视频播放本地路径修复方案

## 问题分析

当学生端访问 `chapterContents` 接口时，`contentType=3`（本地资源类型）的资源返回的 `fileUrl` 是 Windows 本地绝对路径（如 `D:/Code/OnlineLearn/courses/...`），未被转换为 HTTP 可访问的 URL。同时 `.webm` 视频文件被识别为 `resourceType=5`（其他类型）而非视频类型。

## 修复内容

### 1. 后端 `StudentCourseChapterController.java`
- 在 `contentType == 3` 分支中：调用 `convertToHttpUrl()` 将 `fileUrl` 从本地路径转换为 HTTP 路径
- 对于 `.webm/.mp4` 等视频文件：自动添加 `videoUrl` 和 `resourceType: 1` 字段覆盖，使前端能正确渲染为视频播放器

### 2. 前端 `CourseLearning.vue`
- 在处理 `chapterContents` 响应时，增加对 `contentType=3` 但实际是视频文件的识别逻辑
- 将 `fileUrl` 中本地路径转换为带 `http://localhost:9251` 前缀的完整 HTTP URL