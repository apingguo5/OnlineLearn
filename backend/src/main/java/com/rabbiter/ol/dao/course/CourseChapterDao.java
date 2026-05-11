package com.rabbiter.ol.dao.course;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rabbiter.ol.entity.course.CourseChapterEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.HashMap;
import java.util.List;

@Mapper
public interface CourseChapterDao extends BaseMapper<CourseChapterEntity> {

    List<HashMap> queryListByClassId(Integer classId);

    List<HashMap> queryTreeByClassId(Integer classId);
}