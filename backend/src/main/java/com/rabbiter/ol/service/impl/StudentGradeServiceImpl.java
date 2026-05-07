package com.rabbiter.ol.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rabbiter.ol.dao.StudentGradeDao;
import com.rabbiter.ol.entity.StudentGradeEntity;
import com.rabbiter.ol.service.StudentGradeService;
import org.springframework.stereotype.Service;

@Service("studentGradeService")
public class StudentGradeServiceImpl extends ServiceImpl<StudentGradeDao, StudentGradeEntity> implements StudentGradeService {
}