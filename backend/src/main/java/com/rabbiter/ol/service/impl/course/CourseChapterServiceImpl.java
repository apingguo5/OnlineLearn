package com.rabbiter.ol.service.impl.course;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rabbiter.ol.dao.course.CourseChapterDao;
import com.rabbiter.ol.entity.course.CourseChapterEntity;
import com.rabbiter.ol.service.course.CourseChapterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service("courseChapterService")
public class CourseChapterServiceImpl extends ServiceImpl<CourseChapterDao, CourseChapterEntity> implements CourseChapterService {

    @Autowired
    private CourseChapterDao courseChapterDao;

    @Override
    public List<HashMap> getChaptersByClassId(Integer classId) {
        return courseChapterDao.queryListByClassId(classId);
    }

    @Override
    public List<HashMap> getChapterTreeByClassId(Integer classId) {
        return courseChapterDao.queryTreeByClassId(classId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSave(List<CourseChapterEntity> chapters) {
        if (chapters != null && !chapters.isEmpty()) {
            Date now = new Date();
            for (CourseChapterEntity chapter : chapters) {
                chapter.setCreateTime(now);
                chapter.setUpdateTime(now);
                if (chapter.getPublishStatus() == null) {
                    chapter.setPublishStatus(0);
                }
            }
            this.saveBatch(chapters);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdate(List<CourseChapterEntity> chapters) {
        if (chapters != null && !chapters.isEmpty()) {
            Date now = new Date();
            for (CourseChapterEntity chapter : chapters) {
                chapter.setUpdateTime(now);
            }
            this.updateBatchById(chapters);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Integer> ids) {
        if (ids != null && !ids.isEmpty()) {
            // 找到所有子章节一并删除
            List<Integer> allIds = ids.stream().flatMap(id -> {
                List<CourseChapterEntity> children = this.list(
                    new QueryWrapper<CourseChapterEntity>().eq("parent_id", id)
                );
                List<Integer> childIds = children.stream().map(CourseChapterEntity::getId).collect(Collectors.toList());
                childIds.add(id);
                return childIds.stream();
            }).distinct().collect(Collectors.toList());
            this.removeByIds(allIds);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<CourseChapterEntity> copyChapters(Integer sourceClassId, Integer targetClassId) {
        List<CourseChapterEntity> sourceChapters = this.list(
            new QueryWrapper<CourseChapterEntity>().eq("class_id", sourceClassId)
        );
        if (sourceChapters.isEmpty()) {
            return sourceChapters;
        }

        Date now = new Date();
        for (CourseChapterEntity chapter : sourceChapters) {
            chapter.setId(null);
            chapter.setClassId(targetClassId);
            chapter.setCreateTime(now);
            chapter.setUpdateTime(now);
            chapter.setPublishStatus(0);
        }
        this.saveBatch(sourceChapters);
        return sourceChapters;
    }
}