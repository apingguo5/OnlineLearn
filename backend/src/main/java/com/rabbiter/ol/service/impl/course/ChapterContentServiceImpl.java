package com.rabbiter.ol.service.impl.course;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rabbiter.ol.dao.course.ChapterContentDao;
import com.rabbiter.ol.entity.course.ChapterContentEntity;
import com.rabbiter.ol.service.course.ChapterContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Service("chapterContentService")
public class ChapterContentServiceImpl extends ServiceImpl<ChapterContentDao, ChapterContentEntity> implements ChapterContentService {

    @Autowired
    private ChapterContentDao chapterContentDao;

    @Override
    public List<HashMap> getContentsWithDetails(Integer chapterId) {
        return chapterContentDao.queryListWithDetails(chapterId);
    }
}