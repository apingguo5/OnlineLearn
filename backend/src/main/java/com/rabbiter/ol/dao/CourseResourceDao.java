package com.rabbiter.ol.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rabbiter.ol.entity.CourseResourceEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CourseResourceDao extends BaseMapper<CourseResourceEntity> {

    /**
     * 根据章节ID查询资源列表
     */
    List<CourseResourceEntity> listByChapterId(@Param("chapterId") Integer chapterId);

    /**
     * 根据课程ID查询资源列表
     */
    List<CourseResourceEntity> listByCourseId(@Param("courseId") Integer courseId);

    /**
     * 获取章节内的最大排序值
     */
    Integer getMaxSortOrder(@Param("chapterId") Integer chapterId);

    /**
     * 更新排序
     */
    void updateSortOrder(@Param("id") Integer id, @Param("sortOrder") Integer sortOrder);

    /**
     * 检查文件路径是否已存在
     */
    boolean existsByFilePath(@Param("filePath") String filePath);
}
