package com.rabbiter.ol.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.rabbiter.ol.common.Result;
import com.rabbiter.ol.entity.course.CourseChapterEntity;
import com.rabbiter.ol.entity.course.ChapterContentEntity;
import com.rabbiter.ol.service.course.CourseChapterService;
import com.rabbiter.ol.service.course.ChapterContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.rabbiter.ol.service.VideosService;
import com.rabbiter.ol.service.KnowledgePointService;
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

    /**
     * 获取班级的章节列表
     */
    @RequestMapping("/chapters")
    public Result getChapters(@RequestBody Map<String, Object> params) {
        Integer classId = (Integer) params.get("classId");
        if (classId == null) {
            return Result.failure("班级ID不能为空");
        }
        List<HashMap> chapters = courseChapterService.getChaptersByClassId(classId);
        return Result.success(chapters);
    }

    /**
     * 添加章节
     */
    @RequestMapping("/addChapter")
    public Result addChapter(@RequestBody Map<String, Object> params) {
        Integer classId = (Integer) params.get("classId");
        String chapterName = (String) params.get("chapterName");

        if (classId == null || chapterName == null || chapterName.trim().isEmpty()) {
            return Result.failure("参数错误");
        }

        // 获取当前最大排序值
        int maxSort = courseChapterService.count(new QueryWrapper<CourseChapterEntity>().eq("class_id", classId));

        CourseChapterEntity entity = new CourseChapterEntity();
        entity.setClassId(classId);
        entity.setChapterName(chapterName.trim());
        entity.setSortOrder(maxSort);
        entity.setCreateTime(new Date());
        courseChapterService.save(entity);

        return Result.success(entity);
    }

    /**
     * 更新章节名称
     */
    @RequestMapping("/updateChapter")
    public Result updateChapter(@RequestBody Map<String, Object> params) {
        Object idObj = params.get("id") != null ? params.get("id") : params.get("chapterId");
        Integer id = idObj instanceof Integer ? (Integer) idObj : (idObj != null ? Integer.parseInt(idObj.toString()) : null);
        String chapterName = (String) params.get("chapterName");

        if (id == null || chapterName == null || chapterName.trim().isEmpty()) {
            return Result.failure("参数错误");
        }

        CourseChapterEntity entity = courseChapterService.getById(id);
        if (entity == null) {
            return Result.failure("章节不存在");
        }
        entity.setChapterName(chapterName.trim());
        courseChapterService.updateById(entity);

        return Result.successCode();
    }

    /**
     * 删除章节
     */
    @RequestMapping("/deleteChapter")
    public Result deleteChapter(@RequestBody Map<String, Object> params) {
        Object idObj = params.get("id") != null ? params.get("id") : params.get("chapterId");
        Integer id = idObj instanceof Integer ? (Integer) idObj : (idObj != null ? Integer.parseInt(idObj.toString()) : null);
        if (id == null) {
            return Result.failure("章节ID不能为空");
        }
        // 删除章节下的所有内容
        chapterContentService.remove(new QueryWrapper<ChapterContentEntity>().eq("chapter_id", id));
        courseChapterService.removeById(id);
        return Result.successCode();
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
     * 获取章节内容列表
     */
    @RequestMapping("/contents")
    public Result getContents(@RequestBody Map<String, Object> params) {
        Integer chapterId = (Integer) params.get("chapterId");
        if (chapterId == null) {
            return Result.failure("章节ID不能为空");
        }
        List<HashMap> contents = chapterContentService.getContentsWithDetails(chapterId);
        return Result.success(contents);
    }

    /**
     * 添加章节内容（视频或文字阅读）
     */
    @RequestMapping("/addContent")
    public Result addContent(@RequestBody Map<String, Object> params) {
        Integer chapterId = (Integer) params.get("chapterId");
        Integer contentType = (Integer) params.get("contentType");
        Object contentDataObj = params.get("contentData");

        if (chapterId == null || contentType == null || contentDataObj == null) {
            return Result.failure("参数错误");
        }

        Integer refId = contentDataObj instanceof Integer ? (Integer) contentDataObj : Integer.parseInt(contentDataObj.toString());

        // 自动解析标题
        String contentTitle = "";
        if (contentType == 1) {
            // 视频
            com.rabbiter.ol.entity.VideosEntity video = videosService.getById(refId);
            if (video != null) {
                contentTitle = video.getTopic();
            }
        } else if (contentType == 2) {
            // 文字阅读（知识点）
            com.rabbiter.ol.entity.KnowledgePointEntity kp = knowledgePointService.getById(refId);
            if (kp != null) {
                contentTitle = kp.getTitle();
            }
        }

        // 获取当前最大排序值
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