package com.rabbiter.ol.service;

import com.rabbiter.ol.vo.CourseRecommendVo;
import com.rabbiter.ol.vo.ExerciseRecommendVo;
import com.rabbiter.ol.vo.LearningPathVo;

import java.util.List;

/**
 * 推荐系统服务接口
 * 覆盖三类场景：
 * 1) 课程推荐：基于协同过滤（User-Based + Item-Based 混合）
 * 2) 练习推荐：基于答题正确率与薄弱知识点
 * 3) 学习路径推荐：基于章节前置依赖与掌握度
 */
public interface RecommendService {

    /**
     * 课程推荐：混合协同过滤（User-Based + Item-Based）
     * @param userId  学生用户ID
     * @param topN    返回 Top-N 推荐
     * @return 推荐课程列表（按推荐分排序）
     */
    List<CourseRecommendVo> recommendCourses(Integer userId, int topN);

    /**
     * 课程推荐 - 仅 User-Based 协同过滤
     */
    List<CourseRecommendVo> recommendCoursesByUserCF(Integer userId, int topN);

    /**
     * 课程推荐 - 仅 Item-Based 协同过滤
     */
    List<CourseRecommendVo> recommendCoursesByItemCF(Integer userId, int topN);

    /**
     * 练习推荐：针对薄弱知识点
     * @param userId  学生用户ID
     * @param classId 课程（班级）ID，若为 null 则跨课程推荐
     * @param topN    Top-N
     */
    List<ExerciseRecommendVo> recommendExercises(Integer userId, Integer classId, int topN);

    /**
     * 学习路径推荐：基于知识图谱（章节前置依赖）
     * @param userId   学生用户ID
     * @param courseId 课程ID
     */
    LearningPathVo recommendLearningPath(Integer userId, Integer courseId);
}
