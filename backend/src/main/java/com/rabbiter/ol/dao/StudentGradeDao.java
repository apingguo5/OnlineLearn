package com.rabbiter.ol.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rabbiter.ol.entity.StudentGradeEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StudentGradeDao extends BaseMapper<StudentGradeEntity> {
}