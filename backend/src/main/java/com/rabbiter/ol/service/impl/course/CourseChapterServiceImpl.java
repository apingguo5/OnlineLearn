package com.rabbiter.ol.service.impl.course;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rabbiter.ol.dao.course.CourseChapterDao;
import com.rabbiter.ol.entity.course.CourseChapterEntity;
import com.rabbiter.ol.service.course.CourseChapterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Service("courseChapterService")
public class CourseChapterServiceImpl extends ServiceImpl<CourseChapterDao, CourseChapterEntity> implements CourseChapterService {

    @Autowired
    private CourseChapterDao courseChapterDao;

    @Override
    public List<HashMap> getChaptersByClassId(Integer classId) {
        return courseChapterDao.queryListByClassId(classId);
    }
}