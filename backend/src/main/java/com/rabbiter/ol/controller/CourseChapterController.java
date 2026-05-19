package com.rabbiter.ol.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.rabbiter.ol.common.Result;
import com.rabbiter.ol.entity.course.CourseChapterEntity;
import com.rabbiter.ol.entity.course.ChapterContentEntity;
import com.rabbiter.ol.service.course.CourseChapterService;
import com.rabbiter.ol.service.course.ChapterContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import com.rabbiter.ol.service.VideosService;
import com.rabbiter.ol.service.KnowledgePointService;
import com.rabbiter.ol.service.CourseResourceService;
import com.rabbiter.ol.entity.CourseResourceEntity;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 课程章节管理 - 教师可管理班级课程章节
 */
@RestController
@RequestMapping("study/teacher/course")
@CrossOrigin
public class CourseChapterController {

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
    private JdbcTemplate jdbcTemplate;

    /**
     * 获取班级的章节列表
     * 支持按 classId 或 courseId 查询
     */
    @RequestMapping("/chapters")
    public Result getChapters(@RequestBody Map<String, Object> params) {
        Object classIdObj = params.get("classId");
        Object courseIdObj = params.get("courseId");
        if (classIdObj != null) {
            Integer classId = ((Number) classIdObj).intValue();
            List<HashMap> chapters = courseChapterService.getChaptersByClassId(classId);
            return Result.success(chapters);
        } else if (courseIdObj != null) {
            Integer courseId = ((Number) courseIdObj).intValue();
            List<HashMap> chapters = courseChapterService.getChaptersByCourseId(courseId);
            return Result.success(chapters);
        }
        return Result.failure("参数错误：请提供 classId 或 courseId");
    }

    /**
     * 获取章节树形结构
     */
    @RequestMapping("/chapterTree")
    public Result getChapterTree(@RequestBody Map<String, Object> params) {
        Object classIdObj = params.get("classId");
        if (classIdObj == null) {
            return Result.failure("班级ID不能为空");
        }
        Integer classId = ((Number) classIdObj).intValue();
        List<HashMap> chapters = courseChapterService.getChapterTreeByClassId(classId);
        return Result.success(chapters);
    }

    /**
     * 添加章节
     */
    @RequestMapping("/addChapter")
    public Result addChapter(@RequestBody Map<String, Object> params) {
        Object classIdObj = params.get("classId");
        String chapterName = (String) params.get("chapterName");
        Object parentIdObj = params.get("parentId");

        if (classIdObj == null || chapterName == null || chapterName.trim().isEmpty()) {
            return Result.failure("参数错误");
        }
        Integer classId = ((Number) classIdObj).intValue();

        // 获取当前最大排序值
        int maxSort = courseChapterService.count(new QueryWrapper<CourseChapterEntity>().eq("class_id", classId));

        CourseChapterEntity entity = new CourseChapterEntity();
        entity.setClassId(classId);
        entity.setChapterName(chapterName.trim());
        entity.setSortOrder(maxSort);
        if (parentIdObj != null) {
            entity.setParentId(((Number) parentIdObj).intValue());
        }
        entity.setCreateTime(new Date());
        entity.setUpdateTime(new Date());
        courseChapterService.save(entity);

        Map<String, Object> result = new HashMap<>();
        result.put("id", entity.getId());
        result.put("chapterName", entity.getChapterName());
        result.put("sortOrder", entity.getSortOrder());
        result.put("parentId", entity.getParentId());
        result.put("resourceCount", 0);
        return Result.success(result);
    }

    /**
     * 更新章节
     */
    @RequestMapping("/updateChapter")
    public Result updateChapter(@RequestBody Map<String, Object> params) {
        Object idObj = params.get("id") != null ? params.get("id") : params.get("chapterId");
        Integer id = idObj instanceof Integer ? (Integer) idObj : (idObj != null ? Integer.parseInt(idObj.toString()) : null);

        if (id == null) {
            return Result.failure("章节ID不能为空");
        }

        CourseChapterEntity entity = courseChapterService.getById(id);
        if (entity == null) {
            return Result.failure("章节不存在");
        }

        String chapterName = (String) params.get("chapterName");
        String chapterType = (String) params.get("chapterType");
        String description = (String) params.get("description");
        Integer sortOrder = params.get("sortOrder") != null ? ((Number) params.get("sortOrder")).intValue() : null;
        Integer parentId = params.get("parentId") != null ? ((Number) params.get("parentId")).intValue() : null;
        if (chapterName != null) entity.setChapterName(chapterName.trim());
        if (chapterType != null) entity.setChapterType(chapterType);
        if (description != null) entity.setDescription(description);
        if (sortOrder != null) entity.setSortOrder(sortOrder);
        if (parentId != null) entity.setParentId(parentId);
        entity.setUpdateTime(new Date());

        courseChapterService.updateById(entity);
        return Result.successCode();
    }

    /**
     * 批量更新章节（排序、层级调整、批量操作）
     */
    @RequestMapping("/batchUpdateChapters")
    public Result batchUpdateChapters(@RequestBody Map<String, Object> params) {
        List<Map<String, Object>> chapters = (List<Map<String, Object>>) params.get("chapters");
        if (chapters == null || chapters.isEmpty()) {
            return Result.failure("参数错误");
        }
        Date now = new Date();
        for (Map<String, Object> ch : chapters) {
            Integer id = ((Number) ch.get("id")).intValue();
            CourseChapterEntity entity = courseChapterService.getById(id);
            if (entity == null) continue;
            if (ch.containsKey("sortOrder")) entity.setSortOrder(((Number) ch.get("sortOrder")).intValue());
            if (ch.containsKey("parentId")) {
                Object pid = ch.get("parentId");
                entity.setParentId(pid != null ? ((Number) pid).intValue() : null);
            }
            entity.setUpdateTime(now);
            courseChapterService.updateById(entity);
        }
        return Result.successCode();
    }

    /**
     * 删除章节
     */
    @RequestMapping("/deleteChapter")
    public Result deleteChapter(@RequestBody Map<String, Object> params) {
        Object idObj = params.get("id") != null ? params.get("id") : params.get("chapterId");
        if (idObj == null) {
            return Result.failure("章节ID不能为空");
        }
        Integer id;
        if (idObj instanceof List) {
            List<Integer> ids = (List<Integer>) idObj;
            for (Integer cid : ids) {
                chapterContentService.remove(new QueryWrapper<ChapterContentEntity>().eq("chapter_id", cid));
                courseChapterService.removeById(cid);
            }
        } else {
            id = idObj instanceof Integer ? (Integer) idObj : Integer.parseInt(idObj.toString());
            chapterContentService.remove(new QueryWrapper<ChapterContentEntity>().eq("chapter_id", id));
            courseChapterService.removeById(id);
        }
        return Result.successCode();
    }

    /**
     * 复制章节（包含内容）
     */
    @RequestMapping("/copyChapter")
    public Result copyChapter(@RequestBody Map<String, Object> params) {
        Integer chapterId = ((Number) params.get("chapterId")).intValue();
        CourseChapterEntity source = courseChapterService.getById(chapterId);
        if (source == null) {
            return Result.failure("章节不存在");
        }
        CourseChapterEntity copy = new CourseChapterEntity();
        copy.setClassId(source.getClassId());
        copy.setChapterName(source.getChapterName() + " (副本)");
        copy.setChapterType(source.getChapterType());
        copy.setDescription(source.getDescription());
        copy.setParentId(source.getParentId());
        copy.setSortOrder(source.getSortOrder() + 1);
        copy.setCreateTime(new Date());
        copy.setUpdateTime(new Date());
        courseChapterService.save(copy);

        // 复制章节内容
        List<ChapterContentEntity> contents = chapterContentService.list(
            new QueryWrapper<ChapterContentEntity>().eq("chapter_id", chapterId)
        );
        if (contents != null && !contents.isEmpty()) {
            Date now = new Date();
            for (ChapterContentEntity content : contents) {
                content.setId(null);
                content.setChapterId(copy.getId());
                content.setCreateTime(now);
            }
            chapterContentService.saveBatch(contents);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", copy.getId());
        result.put("chapterName", copy.getChapterName());
        return Result.success(result);
    }

    /**
     * 章节排序
     */
    @RequestMapping("/sortChapters")
    public Result sortChapters(@RequestBody Map<String, Object> params) {
        List<Integer> ids = (List<Integer>) params.get("ids");
        if (ids == null || ids.isEmpty()) {
            return Result.failure("参数错误");
        }
        for (int i = 0; i < ids.size(); i++) {
            CourseChapterEntity entity = courseChapterService.getById(ids.get(i));
            if (entity != null) {
                entity.setSortOrder(i);
                courseChapterService.updateById(entity);
            }
        }
        return Result.successCode();
    }

    /**
     * 添加本地路径资源（测试用）
     * 将本地文件路径保存到 course_resource 表，并关联到章节
     */
    @RequestMapping("/addLocalResource")
    public Result addLocalResource(@RequestBody Map<String, Object> params) {
        Object chapterIdObj = params.get("chapterId");
        String resourceName = (String) params.get("resourceName");
        String localPath = (String) params.get("localPath");

        if (chapterIdObj == null) {
            return Result.failure("参数错误：缺少章节ID");
        }
        if (localPath == null || localPath.trim().isEmpty()) {
            return Result.failure("参数错误：本地路径不能为空");
        }

        if (resourceName == null || resourceName.trim().isEmpty()) {
            // 从本地路径提取文件名作为资源名称
            String path = localPath.trim();
            int lastSep = Math.max(path.lastIndexOf('\\'), path.lastIndexOf('/'));
            resourceName = lastSep >= 0 ? path.substring(lastSep + 1) : "本地资源";
        }

        Integer chapterId = ((Number) chapterIdObj).intValue();

        // 获取章节所属课程ID（通过class表JOIN，因为course_chapter只有class_id字段）
        Integer courseId = courseResourceService.getCourseIdByChapterId(chapterId);

        // 1. 保存到 course_resource 表
        CourseResourceEntity resource = new CourseResourceEntity();
        resource.setCourseId(courseId);
        resource.setResourceName(resourceName.trim());
        resource.setResourceType(5); // 其他类型
        resource.setFileUrl(localPath.trim());
        resource.setChapterId(chapterId);
        resource.setIsPublic(0);
        resource.setCreateTime(new Date());
        courseResourceService.save(resource);

        // 2. 添加到 chapter_content 表（方便章节管理展示）
        int maxSort = chapterContentService.count(new QueryWrapper<ChapterContentEntity>().eq("chapter_id", chapterId));
        ChapterContentEntity content = new ChapterContentEntity();
        content.setChapterId(chapterId);
        content.setContentType(3); // 本地路径类型
        content.setContentTitle(resourceName.trim());
        content.setRefId(resource.getId());
        content.setSortOrder(maxSort);
        content.setCreateTime(new Date());
        chapterContentService.save(content);

        Map<String, Object> result = new HashMap<>();
        result.put("resourceId", resource.getId());
        result.put("resourceName", resourceName.trim());
        result.put("fileUrl", localPath.trim());
        result.put("contentId", content.getId());
        return Result.success(result);
    }

    /**
     * 获取章节内容列表
     */
    @RequestMapping("/contents")
    public Result getContents(@RequestBody Map<String, Object> params) {
        Object chapterIdObj = params.get("chapterId");
        if (chapterIdObj == null) {
            return Result.failure("章节ID不能为空");
        }
        Integer chapterId = ((Number) chapterIdObj).intValue();
        List<HashMap> contents = chapterContentService.getContentsWithDetails(chapterId);
        return Result.success(contents);
    }

    /**
     * 添加章节内容（视频或文字阅读）
     */
    @RequestMapping("/addContent")
    public Result addContent(@RequestBody Map<String, Object> params) {
        Object chapterIdObj = params.get("chapterId");
        Object contentTypeObj = params.get("contentType");
        Object contentDataObj = params.get("contentData");

        if (chapterIdObj == null || contentTypeObj == null || contentDataObj == null) {
            return Result.failure("参数错误");
        }
        Integer chapterId = ((Number) chapterIdObj).intValue();
        Integer contentType = ((Number) contentTypeObj).intValue();

        Integer refId = contentDataObj instanceof Integer ? (Integer) contentDataObj : Integer.parseInt(contentDataObj.toString());

        // 自动解析标题
        String contentTitle = "";
        if (contentType == 1) {
            com.rabbiter.ol.entity.VideosEntity video = videosService.getById(refId);
            if (video != null) {
                contentTitle = video.getTopic();
            }
        } else if (contentType == 2) {
            com.rabbiter.ol.entity.KnowledgePointEntity kp = knowledgePointService.getById(refId);
            if (kp != null) {
                contentTitle = kp.getTitle();
            }
        }

        int maxSort = chapterContentService.count(new QueryWrapper<ChapterContentEntity>().eq("chapter_id", chapterId));

        ChapterContentEntity entity = new ChapterContentEntity();
        entity.setChapterId(chapterId);
        entity.setContentType(contentType);
        entity.setContentTitle(contentTitle);
        entity.setRefId(refId);
        entity.setSortOrder(maxSort);
        entity.setCreateTime(new Date());
        chapterContentService.save(entity);

        return Result.success(entity);
    }

    /**
     * 删除章节内容
     */
    @RequestMapping("/deleteContent")
    public Result deleteContent(@RequestBody Map<String, Object> params) {
        Object idObj = params.get("id") != null ? params.get("id") : params.get("contentId");
        Integer id = idObj instanceof Integer ? (Integer) idObj : (idObj != null ? Integer.parseInt(idObj.toString()) : null);
        if (id == null) {
            return Result.failure("内容ID不能为空");
        }
        chapterContentService.removeById(id);
        return Result.successCode();
    }

    /**
     * 内容排序
     */
    @RequestMapping("/sortContents")
    public Result sortContents(@RequestBody Map<String, Object> params) {
        List<Integer> ids = (List<Integer>) params.get("ids");
        if (ids == null || ids.isEmpty()) {
            return Result.failure("参数错误");
        }
        for (int i = 0; i < ids.size(); i++) {
            ChapterContentEntity entity = chapterContentService.getById(ids.get(i));
            if (entity != null) {
                entity.setSortOrder(i);
                chapterContentService.updateById(entity);
            }
        }
        return Result.successCode();
    }
}