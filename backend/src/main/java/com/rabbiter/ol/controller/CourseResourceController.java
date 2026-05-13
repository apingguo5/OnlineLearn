package com.rabbiter.ol.controller;

import com.rabbiter.ol.common.Result;
import com.rabbiter.ol.dao.CourseResourceDao;
import com.rabbiter.ol.entity.CourseResourceEntity;
import com.rabbiter.ol.service.CourseResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 课程资源管理控制器
 * 负责 course_resource 表的增删改查
 */
@RestController
@RequestMapping("study/teacher/course/resource")
@CrossOrigin
public class CourseResourceController {

    @Autowired
    private CourseResourceService courseResourceService;

    @Autowired
    private CourseResourceDao courseResourceDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 获取章节的资源列表
     */
    @RequestMapping("/listByChapter")
    public Result listByChapter(@RequestBody Map<String, Object> params) {
        Object chapterIdObj = params.get("chapterId");
        if (chapterIdObj == null) {
            return Result.failure("章节ID不能为空");
        }
        Integer chapterId = ((Number) chapterIdObj).intValue();
        List<CourseResourceEntity> list = courseResourceService.listByChapterId(chapterId);
        return Result.success(list);
    }

    /**
     * 获取课程的资源列表
     */
    @RequestMapping("/listByCourse")
    public Result listByCourse(@RequestBody Map<String, Object> params) {
        Object courseIdObj = params.get("courseId");
        if (courseIdObj == null) {
            return Result.failure("课程ID不能为空");
        }
        Integer courseId = ((Number) courseIdObj).intValue();
        List<CourseResourceEntity> list = courseResourceService.listByCourseId(courseId);
        return Result.success(list);
    }

    /**
     * 添加本地路径资源（测试用）
     * 将本地文件路径保存到 course_resource 表，并关联到章节
     */
    @RequestMapping("/addLocal")
    public Result addLocalResource(@RequestBody Map<String, Object> params) {
        Object chapterIdObj = params.get("chapterId");
        String resourceName = (String) params.get("resourceName");
        String localPath = (String) params.get("localPath");
        Object resourceTypeObj = params.get("resourceType");
        Object uploaderIdObj = params.get("uploaderId");

        if (chapterIdObj == null) {
            return Result.failure("参数错误：缺少章节ID");
        }
        if (localPath == null || localPath.trim().isEmpty()) {
            return Result.failure("参数错误：本地路径不能为空");
        }

        // 自动提取资源名称
        if (resourceName == null || resourceName.trim().isEmpty()) {
            String path = localPath.trim();
            int lastSep = Math.max(path.lastIndexOf('\\'), path.lastIndexOf('/'));
            resourceName = lastSep >= 0 ? path.substring(lastSep + 1) : "本地资源";
        }

        Integer chapterId = ((Number) chapterIdObj).intValue();
        Integer resourceType = resourceTypeObj != null ? ((Number) resourceTypeObj).intValue() : 1; // 默认视频
        Integer uploaderId = uploaderIdObj != null ? ((Number) uploaderIdObj).intValue() : null;

        // 获取章节所属课程ID（通过class表JOIN，因为course_chapter只有class_id字段）
        Integer courseId = courseResourceService.getCourseIdByChapterId(chapterId);

        // 1. 保存到 course_resource 表
        CourseResourceEntity resource = new CourseResourceEntity();
        resource.setCourseId(courseId);
        resource.setResourceName(resourceName.trim());
        resource.setResourceType(resourceType);
        resource.setFileUrl(localPath.trim());
        resource.setChapterId(chapterId);
        resource.setUploaderId(uploaderId);
        resource.setIsPublic(0); // 默认仅班级学生可见

        // 获取当前章节最大排序值
        Integer maxSort = courseResourceDao.getMaxSortOrder(chapterId);
        resource.setSortOrder(maxSort != null ? maxSort + 1 : 0);

        resource.setCreateTime(new Date());
        courseResourceService.save(resource);

        Map<String, Object> result = new HashMap<>();
        result.put("resourceId", resource.getId());
        result.put("resourceName", resource.getResourceName());
        result.put("fileUrl", resource.getFileUrl());
        result.put("resourceType", resource.getResourceType());
        result.put("chapterId", resource.getChapterId());
        result.put("courseId", resource.getCourseId());
        return Result.success(result);
    }

    /**
     * 添加资源（通用 - 可设置完整字段）
     */
    @RequestMapping("/add")
    public Result addResource(@RequestBody CourseResourceEntity resource) {
        if (resource.getResourceName() == null || resource.getResourceName().trim().isEmpty()) {
            return Result.failure("资源名称不能为空");
        }
        if (resource.getCourseId() == null) {
            return Result.failure("所属课程ID不能为空");
        }
        resource.setCreateTime(new Date());
        courseResourceService.save(resource);

        Map<String, Object> result = new HashMap<>();
        result.put("id", resource.getId());
        return Result.success(result);
    }

    /**
     * 更新资源
     */
    @RequestMapping("/update")
    public Result updateResource(@RequestBody CourseResourceEntity resource) {
        if (resource.getId() == null) {
            return Result.failure("资源ID不能为空");
        }
        CourseResourceEntity existing = courseResourceService.getById(resource.getId());
        if (existing == null) {
            return Result.failure("资源不存在");
        }

        if (resource.getResourceName() != null) existing.setResourceName(resource.getResourceName());
        if (resource.getResourceType() != null) existing.setResourceType(resource.getResourceType());
        if (resource.getFileUrl() != null) existing.setFileUrl(resource.getFileUrl());
        if (resource.getSortOrder() != null) existing.setSortOrder(resource.getSortOrder());
        if (resource.getIsPublic() != null) existing.setIsPublic(resource.getIsPublic());
        if (resource.getChapterId() != null) existing.setChapterId(resource.getChapterId());
        if (resource.getDuration() != null) existing.setDuration(resource.getDuration());

        courseResourceService.updateById(existing);
        return Result.successCode();
    }

    /**
     * 删除资源
     */
    @RequestMapping("/delete")
    public Result deleteResource(@RequestBody Map<String, Object> params) {
        Object idObj = params.get("id") != null ? params.get("id") : params.get("resourceId");
        if (idObj == null) {
            return Result.failure("资源ID不能为空");
        }
        Integer id = idObj instanceof Integer ? (Integer) idObj : Integer.parseInt(idObj.toString());
        courseResourceService.removeById(id);
        return Result.successCode();
    }

    /**
     * 批量删除资源
     */
    @RequestMapping("/batchDelete")
    public Result batchDeleteResource(@RequestBody Map<String, Object> params) {
        List<Integer> ids = (List<Integer>) params.get("ids");
        if (ids == null || ids.isEmpty()) {
            return Result.failure("资源ID列表不能为空");
        }
        courseResourceService.removeByIds(ids);
        return Result.successCode();
    }

    /**
     * 更新资源排序
     */
    @RequestMapping("/sort")
    public Result sortResources(@RequestBody Map<String, Object> params) {
        List<Integer> ids = (List<Integer>) params.get("ids");
        if (ids == null || ids.isEmpty()) {
            return Result.failure("参数错误");
        }
        for (int i = 0; i < ids.size(); i++) {
            CourseResourceEntity entity = courseResourceService.getById(ids.get(i));
            if (entity != null) {
                entity.setSortOrder(i);
                courseResourceService.updateById(entity);
            }
        }
        return Result.successCode();
    }

    /**
     * 按条件检索资源
     */
    @RequestMapping("/search")
    public Result searchResources(@RequestBody Map<String, Object> params) {
        Integer courseId = params.get("courseId") != null ? ((Number) params.get("courseId")).intValue() : null;
        Integer chapterId = params.get("chapterId") != null ? ((Number) params.get("chapterId")).intValue() : null;
        Integer resourceType = params.get("resourceType") != null ? ((Number) params.get("resourceType")).intValue() : null;

        List<CourseResourceEntity> list;
        if (chapterId != null) {
            list = courseResourceService.listByChapterId(chapterId);
        } else if (courseId != null) {
            list = courseResourceService.listByCourseId(courseId);
        } else {
            list = courseResourceService.list();
        }

        // 按资源类型过滤
        if (resourceType != null) {
            List<CourseResourceEntity> filtered = new ArrayList<>();
            for (CourseResourceEntity r : list) {
                if (resourceType.equals(r.getResourceType())) {
                    filtered.add(r);
                }
            }
            list = filtered;
        }

        return Result.success(list);
    }
}