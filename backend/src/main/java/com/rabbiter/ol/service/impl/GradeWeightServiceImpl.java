package com.rabbiter.ol.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rabbiter.ol.dao.GradeWeightDao;
import com.rabbiter.ol.entity.GradeWeightEntity;
import com.rabbiter.ol.service.GradeWeightService;
import org.springframework.stereotype.Service;

@Service("gradeWeightService")
public class GradeWeightServiceImpl extends ServiceImpl<GradeWeightDao, GradeWeightEntity> implements GradeWeightService {
}