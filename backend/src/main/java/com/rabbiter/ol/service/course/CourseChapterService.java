package com.rabbiter.ol.service.course;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rabbiter.ol.entity.course.CourseChapterEntity;

import java.util.HashMap;
import java.util.List;

public interface CourseChapterService extends IService<CourseChapterEntity> {

    List<HashMap> getChaptersByClassId(Integer classId);
}