package com.rabbiter.ol.dao.course;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rabbiter.ol.entity.course.CourseChapterEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.HashMap;
import java.util.List;

@Mapper
public interface CourseChapterDao extends BaseMapper<CourseChapterEntity> {

    List<HashMap> queryListByClassId(Integer classId);

    List<HashMap> queryTreeByClassId(Integer classId);

    List<HashMap> queryListByCourseId(Integer courseId);

    /**
     * 根据章节ID获取课程ID（通过class表JOIN）
     */
    @Select("SELECT c.course_id FROM class c JOIN course_chapter cc ON c.id = cc.class_id WHERE cc.id = #{chapterId}")
    Integer getCourseIdByChapterId(@Param("chapterId") Integer chapterId);
}