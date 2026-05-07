package com.rabbiter.ol.service.course;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rabbiter.ol.entity.course.ChapterContentEntity;

import java.util.HashMap;
import java.util.List;

public interface ChapterContentService extends IService<ChapterContentEntity> {

    List<HashMap> getContentsWithDetails(Integer chapterId);
}