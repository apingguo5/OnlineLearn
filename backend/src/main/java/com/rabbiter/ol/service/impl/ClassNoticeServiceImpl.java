package com.rabbiter.ol.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rabbiter.ol.dao.ClassNoticeDao;
import com.rabbiter.ol.entity.ClassNoticeEntity;
import com.rabbiter.ol.service.ClassNoticeService;
import com.rabbiter.ol.vo.ClassNoticeVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("classNoticeService")
public class ClassNoticeServiceImpl extends ServiceImpl<ClassNoticeDao, ClassNoticeEntity> implements ClassNoticeService {

    @Autowired
    private ClassNoticeDao classNoticeDao;

    @Override
    public Map<String, Object> queryPage(ClassNoticeVo classNoticeVo) {
        Integer total = classNoticeDao.queryCount(classNoticeVo);
        List<HashMap> data = classNoticeDao.queryData(classNoticeVo);
        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("data", data);
        return result;
    }

    @Override
    public Map<String, Object> queryByStudentClasses(ClassNoticeVo classNoticeVo) {
        Integer total = classNoticeDao.countByStudentClasses(classNoticeVo);
        List<HashMap> data = classNoticeDao.queryByStudentClasses(classNoticeVo);
        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("data", data);
        return result;
    }
}
