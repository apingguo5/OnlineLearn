package com.rabbiter.ol.controller;

import com.rabbiter.ol.common.Result;
import com.rabbiter.ol.entity.course.ChapterContentEntity;
import com.rabbiter.ol.entity.course.CourseChapterEntity;
import com.rabbiter.ol.service.course.CourseChapterService;
import com.rabbiter.ol.service.course.ChapterContentService;
import com.rabbiter.ol.service.VideosService;
import com.rabbiter.ol.service.KnowledgePointService;
import com.rabbiter.ol.service.CourseResourceService;
import com.rabbiter.ol.dao.CourseResourceDao;
import com.rabbiter.ol.entity.VideosEntity;
import com.rabbiter.ol.entity.KnowledgePointEntity;
import com.rabbiter.ol.entity.CourseResourceEntity;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 学生端课程学习 - 章节与内容查看
 */
@RestController
@RequestMapping("study/student/course")
@CrossOrigin
public class StudentCourseChapterController {

    private static final Logger log = LoggerFactory.getLogger(StudentCourseChapterController.class);

    @Autowired
    private CourseChapterService courseChapterService;

    @Autowired
    private ChapterContentService chapterContentService;

    @Autowired
    private VideosService videosService;

    @Autowired
    private KnowledgePointService knowledgePointService;

    @Autowired
    private CourseResourceService courseResourceService;

    @Autowired
    private CourseResourceDao courseResourceDao;

    /**
     * 获取课程的学习章节树形结构（按班级）
     * POST /study/student/course/learningChapters
     * @param params { classId: 班级ID }
     */
    @RequestMapping("/learningChapters")
    public Result getLearningChapters(@RequestBody Map<String, Object> params) {
        Object classIdObj = params.get("classId");
        if (classIdObj == null) {
            return Result.failure("班级ID不能为空");
        }
        Integer classId = ((Number) classIdObj).intValue();
        List<HashMap> chapters = courseChapterService.getChapterTreeByClassId(classId);
        return Result.success(chapters);
    }

    /**
     * 递归收集指定章节的所有子章节ID（含自身）
     */
    private List<Integer> collectChildChapterIds(Integer chapterId) {
        List<Integer> ids = new ArrayList<>();
        ids.add(chapterId);
        // 查找所有 parent_id = chapterId 的子章节
        List<CourseChapterEntity> children = courseChapterService.list(
            new QueryWrapper<CourseChapterEntity>().eq("parent_id", chapterId)
        );
        for (CourseChapterEntity child : children) {
            ids.addAll(collectChildChapterIds(child.getId()));
        }
        return ids;
    }

    /**
     * 获取章节内容列表（含资源详细信息）
     * 先从 chapter_content 表查询；如果为空或查询失败，
     * 则降级从 course_resource 表直接查询该章节（及其子章节）下的资源。
     * POST /study/student/course/chapterContents
     * @param params { chapterId: 章节ID }
     */
    @RequestMapping("/chapterContents")
    public Result getChapterContents(@RequestBody Map<String, Object> params) {
        Object chapterIdObj = params.get("chapterId");
        if (chapterIdObj == null) {
            return Result.failure("章节ID不能为空");
        }
        Integer chapterId = ((Number) chapterIdObj).intValue();

        // 1️⃣ 先尝试从 chapter_content 表获取（标准路径），包裹 try-catch 防止异常导致 500
        try {
            List<HashMap> contents = chapterContentService.getContentsWithDetails(chapterId);
            if (contents != null && !contents.isEmpty()) {
                // ✅ chapter_content 有数据，按标准路径处理
                List<Map<String, Object>> resultList = new ArrayList<>();
                for (HashMap content : contents) {
                    Map<String, Object> item = new HashMap<>(content);
                    Object ctObj = content.get("contentType");
                    Integer contentType = ctObj != null ? ((Number) ctObj).intValue() : null;
                    Object refIdObj = content.get("refId");

                    if (contentType != null && refIdObj != null) {
                        Integer refId = ((Number) refIdObj).intValue();
                        if (contentType == 1) {
                            // 视频类型 - 获取视频详情
                            VideosEntity video = videosService.getById(refId);
                            if (video != null) {
                                item.put("videoUrl", convertToHttpUrl(video.getVideoUrl()));
                                item.put("videoName", video.getTopic());
                            }
                        } else if (contentType == 2) {
                            // 知识点/阅读类型 - 获取阅读内容
                            KnowledgePointEntity kp = knowledgePointService.getById(refId);
                            if (kp != null) {
                                item.put("content", kp.getContent());
                                item.put("htmlContent", kp.getContent());
                            }
                        } else if (contentType == 3) {
                            // 本地资源类型
                            CourseResourceEntity resource = courseResourceService.getById(refId);
                            if (resource != null) {
                                String fileUrl = resource.getFileUrl();
                                String httpUrl = convertToHttpUrl(fileUrl);
                                item.put("fileUrl", httpUrl);
                                item.put("resourceType", resource.getResourceType());

                                // 自动识别视频文件（.webm/.mp4/.mkv 等），设置为视频播放类型
                                if (fileUrl != null && (
                                    fileUrl.toLowerCase().endsWith(".webm") ||
                                    fileUrl.toLowerCase().endsWith(".mp4") ||
                                    fileUrl.toLowerCase().endsWith(".mkv") ||
                                    fileUrl.toLowerCase().endsWith(".avi") ||
                                    fileUrl.toLowerCase().endsWith(".mov")
                                )) {
                                    item.put("contentType", 1);
                                    item.put("videoUrl", httpUrl);
                                    item.put("videoName", resource.getResourceName());
                                }
                            }
                        }
                    }
                    resultList.add(item);
                }
                return Result.success(resultList);
            }
        } catch (Exception e) {
            // chapter_content 查询异常（例如表不存在、列缺失等），降级到 course_resource 查询
            log.warn("查询 chapter_content 失败，降级到 course_resource 查询, chapterId={}, error={}", chapterId, e.getMessage());
        }

        // 2️⃣ 降级路径：从 course_resource 表直接查询
        //    先判断该章节是否为父章节（有子章节），如果是则递归收集所有子章节的资源
        try {
            List<CourseChapterEntity> children = courseChapterService.list(
                new QueryWrapper<CourseChapterEntity>().eq("parent_id", chapterId)
            );

            List<CourseResourceEntity> resources;
            if (children != null && !children.isEmpty()) {
                // 该章节有子章节 → 递归收集所有子章节（含自身）的 chapterId
                List<Integer> allChapterIds = collectChildChapterIds(chapterId);
                resources = courseResourceDao.listByChapterIds(allChapterIds);
                log.info("父章节 chapterId={} 有 {} 个子章节，聚合资源共 {} 条",
                    chapterId, children.size(), resources != null ? resources.size() : 0);
            } else {
                // 叶子章节 → 直接查询该章节下的资源
                resources = courseResourceDao.listByChapterId(chapterId);
            }

            if (resources != null && !resources.isEmpty()) {
                List<Map<String, Object>> resultList = new ArrayList<>();
                for (CourseResourceEntity resource : resources) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", resource.getId());
                    item.put("chapterId", resource.getChapterId());
                    item.put("contentTitle", resource.getResourceName());
                    item.put("sortOrder", resource.getSortOrder());
                    // 🔄 将本地路径转换为 HTTP 可访问 URL
                    String httpUrl = convertToHttpUrl(resource.getFileUrl());
                    item.put("fileUrl", httpUrl);
                    item.put("resourceType", resource.getResourceType());

                    // 判断资源类型并填充详细信息
                    String localFileUrl = resource.getFileUrl();
                    Integer resourceType = resource.getResourceType();
                    item.put("contentType", resourceType);
                    if (resourceType != null) {
                        if (resourceType == 1) {
                            // 视频类型：直接使用 course_resource 中的 file_url 作为视频地址（已转 HTTP URL）
                            item.put("videoUrl", httpUrl);
                            item.put("videoName", resource.getResourceName());
                        } else if (resourceType == 2) {
                            // 阅读/知识点类型：使用 file_url 作为内容路径
                            item.put("content", httpUrl);
                            item.put("htmlContent", httpUrl);
                        }

                        // 自动识别视频文件（.webm/.mp4/.mkv 等），覆盖设置为视频播放类型
                        if (localFileUrl != null && (
                            localFileUrl.toLowerCase().endsWith(".webm") ||
                            localFileUrl.toLowerCase().endsWith(".mp4") ||
                            localFileUrl.toLowerCase().endsWith(".mkv") ||
                            localFileUrl.toLowerCase().endsWith(".avi") ||
                            localFileUrl.toLowerCase().endsWith(".mov")
                        )) {
                            item.put("contentType", 1);
                            item.put("videoUrl", httpUrl);
                            item.put("videoName", resource.getResourceName());
                        }
                    }

                    resultList.add(item);
                }
                return Result.success(resultList);
            }
        } catch (Exception e) {
            log.warn("降级查询 course_resource 也失败, chapterId={}, error={}", chapterId, e.getMessage());
        }

        // 3️⃣ 没有任何资源，返回空列表
        return Result.success(new ArrayList<>());
    }

    /**
     * 将本地文件路径转换为 HTTP 可访问的相对URL路径（对特殊字符进行单次URL编码）
     * 例如：D:\Code\OnlineLearn\courses\Android\Unit 1... → /courses/Android/Unit%201...
     *
     * 编码策略：
     * 1. 提取从 /courses/ 开始的相对路径
     * 2. 对路径中每个段进行URL编码（空格→%20，方括号→%5B/%5D等）
     * 3. 保留 / 分隔符不变
     * 4. 前端不应对返回值再次编码，否则会造成双重编码（%20→%2520）
     */
    private String convertToHttpUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return fileUrl;
        }
        try {
            // 统一正斜杠
            String normalizedUrl = fileUrl.replace("\\", "/");
            String lowerUrl = normalizedUrl.toLowerCase();

            // 在路径中定位 /courses/ (大小写不敏感)
            int coursesIndex = lowerUrl.indexOf("/courses/");

            String relativePath;
            if (coursesIndex >= 0) {
                // 提取从 /courses/ 开始的部分（保留原始大小写）
                relativePath = normalizedUrl.substring(coursesIndex);
            } else if (lowerUrl.startsWith("courses/")) {
                // fallback: 没有前导斜杠
                relativePath = "/" + normalizedUrl;
            } else {
                log.debug("convertToHttpUrl(no match): {} → (unchanged)", fileUrl);
                return fileUrl;
            }

            // 对路径进行URL编码，但保留 / 分隔符
            // 注意：前端不应再调用 encodeURI()，否则会导致双重编码
            String encodedPath = encodeUrlPath(relativePath);
            log.debug("convertToHttpUrl: {} → {}", fileUrl, encodedPath);
            return encodedPath;
        } catch (Exception e) {
            log.warn("convertToHttpUrl error for url={}: {}", fileUrl, e.getMessage());
            return fileUrl;
        }
    }

    /**
     * 对URL路径进行单次URL编码，保留 / 分隔符不变
     * 使用 URLEncoder.encode + 替换 + 为 %20（路径中空格只能用%20）
     */
    private String encodeUrlPath(String path) {
        if (path == null || path.isEmpty()) {
            return path;
        }
        String[] segments = path.split("/", -1);
        StringBuilder encoded = new StringBuilder();
        for (int i = 0; i < segments.length; i++) {
            if (i > 0) {
                encoded.append("/");
            }
            String segment = URLEncoder.encode(segments[i], StandardCharsets.UTF_8)
                .replace("+", "%20");
            encoded.append(segment);
        }
        return encoded.toString();
    }
}
