package com.rabbiter.ol.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rabbiter.ol.dao.ChapterResourceRefDao;
import com.rabbiter.ol.entity.ChapterResourceRefEntity;
import com.rabbiter.ol.service.ChapterResourceRefService;
import org.springframework.stereotype.Service;

@Service("chapterResourceRefService")
public class ChapterResourceRefServiceImpl extends ServiceImpl<ChapterResourceRefDao, ChapterResourceRefEntity> implements ChapterResourceRefService {
}