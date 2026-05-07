package com.rabbiter.ol.dao.course;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rabbiter.ol.entity.course.CourseChapterEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.HashMap;
import java.util.List;

/**
 * 课程章节 DAO
 */
@Mapper
public interface CourseChapterDao extends BaseMapper<CourseChapterEntity> {

    List<HashMap> queryListByClassId(Integer classId);
}