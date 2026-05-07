package com.rabbiter.ol.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rabbiter.ol.dao.ClassNoticeDao;
import com.rabbiter.ol.entity.ClassNoticeEntity;
import com.rabbiter.ol.service.ClassNoticeService;
import org.springframework.stereotype.Service;

@Service("classNoticeService")
public class ClassNoticeServiceImpl extends ServiceImpl<ClassNoticeDao, ClassNoticeEntity> implements ClassNoticeService {
}