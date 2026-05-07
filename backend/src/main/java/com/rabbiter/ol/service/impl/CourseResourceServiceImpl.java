package com.rabbiter.ol.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rabbiter.ol.dao.CourseResourceDao;
import com.rabbiter.ol.entity.CourseResourceEntity;
import com.rabbiter.ol.service.CourseResourceService;
import org.springframework.stereotype.Service;

@Service("courseResourceService")
public class CourseResourceServiceImpl extends ServiceImpl<CourseResourceDao, CourseResourceEntity> implements CourseResourceService {
}