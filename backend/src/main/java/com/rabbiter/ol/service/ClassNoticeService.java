package com.rabbiter.ol.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rabbiter.ol.entity.ClassNoticeEntity;
import com.rabbiter.ol.vo.ClassNoticeVo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface ClassNoticeService extends IService<ClassNoticeEntity> {

    Map<String, Object> queryPage(ClassNoticeVo classNoticeVo);

    Map<String, Object> queryByStudentClasses(ClassNoticeVo classNoticeVo);
}
