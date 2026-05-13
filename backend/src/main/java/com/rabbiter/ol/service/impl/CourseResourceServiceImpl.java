package com.rabbiter.ol.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rabbiter.ol.dao.CourseResourceDao;
import com.rabbiter.ol.entity.CourseResourceEntity;
import com.rabbiter.ol.service.CourseResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.Date;
import java.util.List;

@Service("courseResourceService")
public class CourseResourceServiceImpl extends ServiceImpl<CourseResourceDao, CourseResourceEntity> implements CourseResourceService {

    @Autowired
    private CourseResourceDao courseResourceDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<CourseResourceEntity> listByChapterId(Integer chapterId) {
        return courseResourceDao.listByChapterId(chapterId);
    }

    @Override
    public List<CourseResourceEntity> listByCourseId(Integer courseId) {
        return courseResourceDao.listByCourseId(courseId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CourseResourceEntity addLocalResource(Integer chapterId, String resourceName, String localPath, Integer resourceType, Integer uploaderId) {
        // 获取章节对应的课程ID（通过class表JOIN）
        Integer courseId = getCourseIdByChapterId(chapterId);

        CourseResourceEntity resource = new CourseResourceEntity();
        resource.setCourseId(courseId);
        resource.setResourceName(resourceName);
        resource.setFileUrl(localPath);
        resource.setResourceType(resourceType != null ? resourceType : 5);
        resource.setChapterId(chapterId);
        resource.setUploaderId(uploaderId);
        resource.setIsPublic(0); // 默认仅班级学生可见

        // 获取当前章节最大排序值
        Integer maxSort = courseResourceDao.getMaxSortOrder(chapterId);
        resource.setSortOrder(maxSort != null ? maxSort + 1 : 0);

        resource.setCreateTime(new Date());
        this.save(resource);
        return resource;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteResource(Integer resourceId) {
        this.removeById(resourceId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateResource(CourseResourceEntity resource) {
        CourseResourceEntity existing = this.getById(resource.getId());
        if (existing == null) {
            throw new RuntimeException("资源不存在: id=" + resource.getId());
        }
        if (resource.getResourceName() != null) existing.setResourceName(resource.getResourceName());
        if (resource.getResourceType() != null) existing.setResourceType(resource.getResourceType());
        if (resource.getFileUrl() != null) existing.setFileUrl(resource.getFileUrl());
        if (resource.getSortOrder() != null) existing.setSortOrder(resource.getSortOrder());
        if (resource.getIsPublic() != null) existing.setIsPublic(resource.getIsPublic());
        if (resource.getChapterId() != null) existing.setChapterId(resource.getChapterId());
        if (resource.getDuration() != null) existing.setDuration(resource.getDuration());
        if (resource.getFileSize() != null) existing.setFileSize(resource.getFileSize());
        this.updateById(existing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteResources(List<Integer> ids) {
        this.removeByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sortResources(List<Integer> ids) {
        for (int i = 0; i < ids.size(); i++) {
            CourseResourceEntity entity = this.getById(ids.get(i));
            if (entity != null) {
                entity.setSortOrder(i);
                this.updateById(entity);
            }
        }
    }

    @Override
    public boolean existsByFilePath(String filePath) {
        return courseResourceDao.existsByFilePath(filePath);
    }

    @Override
    public Integer getCourseIdByChapterId(Integer chapterId) {
        // course_chapter 表只有 class_id，需要通过 class 表关联到 course_id
        // course_chapter.class_id -> class.id -> class.course_id
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int scanAndImportLocalResources(Integer courseId, Integer chapterId, String localBasePath, Integer uploaderId) {
        File baseDir = new File(localBasePath);
        if (!baseDir.exists() || !baseDir.isDirectory()) {
            return 0;
        }

        int imported = 0;
        File[] files = baseDir.listFiles(f -> f.isFile());
        if (files == null) return 0;

        for (File file : files) {
            String filePath = file.getAbsolutePath().replace("\\", "/");

            // 跳过已导入的文件
            if (existsByFilePath(filePath)) {
                continue;
            }

            // 根据扩展名判断资源类型
            int resourceType = 5; // 默认"其他"
            String name = file.getName().toLowerCase();
            if (name.endsWith(".mp4") || name.endsWith(".webm") || name.endsWith(".avi") || name.endsWith(".mov")) {
                resourceType = 1; // 视频
            } else if (name.endsWith(".pdf")) {
                resourceType = 2; // PDF
            } else if (name.endsWith(".ppt") || name.endsWith(".pptx")) {
                resourceType = 3; // PPT
            } else if (name.endsWith(".doc") || name.endsWith(".docx")) {
                resourceType = 2; // 文档
            }

            CourseResourceEntity resource = new CourseResourceEntity();
            resource.setCourseId(courseId);
            resource.setResourceName(file.getName());
            resource.setFileUrl(filePath);
            resource.setResourceType(resourceType);
            resource.setFileSize(file.length());
            resource.setChapterId(chapterId);
            resource.setUploaderId(uploaderId);
            resource.setIsPublic(0);

            Integer maxSort = courseResourceDao.getMaxSortOrder(chapterId);
            resource.setSortOrder(maxSort != null ? maxSort + 1 : 0);

            resource.setCreateTime(new Date());
            this.save(resource);
            imported++;
        }
        return imported;
    }

    @Override
    public List<CourseResourceEntity> searchResources(Integer courseId, Integer chapterId, Integer resourceType) {
        List<CourseResourceEntity> list;
        if (chapterId != null) {
            list = courseResourceDao.listByChapterId(chapterId);
        } else if (courseId != null) {
            list = courseResourceDao.listByCourseId(courseId);
        } else {
            list = this.list();
        }

        // 按资源类型过滤
        if (resourceType != null) {
            list.removeIf(r -> !resourceType.equals(r.getResourceType()));
        }

        return list;
    }
}