package com.rabbiter.ol.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rabbiter.ol.entity.StudentGradeEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface StudentGradeDao extends BaseMapper<StudentGradeEntity> {

    List<Map<String, Object>> queryClassGrades(@Param("classId") Integer classId);
}
