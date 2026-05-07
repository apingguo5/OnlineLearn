package com.rabbiter.ol.dao.course;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rabbiter.ol.entity.course.ChapterContentEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.HashMap;
import java.util.List;

/**
 * 章节内容 DAO
 */
@Mapper
public interface ChapterContentDao extends BaseMapper<ChapterContentEntity> {

    List<HashMap> queryListWithDetails(Integer chapterId);
}