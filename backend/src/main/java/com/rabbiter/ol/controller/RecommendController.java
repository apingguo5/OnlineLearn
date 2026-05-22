package com.rabbiter.ol.controller;

import com.rabbiter.ol.common.Result;
import com.rabbiter.ol.service.RecommendService;
import com.rabbiter.ol.vo.CourseRecommendVo;
import com.rabbiter.ol.vo.ExerciseRecommendVo;
import com.rabbiter.ol.vo.LearningPathVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 个性化推荐接口
 * 三类推荐场景：课程 / 练习 / 学习路径
 */
@RestController
@RequestMapping("/study/recommend")
@CrossOrigin
public class RecommendController {

    private static final Logger log = LoggerFactory.getLogger(RecommendController.class);

    @Autowired
    private RecommendService recommendService;

    /**
     * 课程推荐（混合协同过滤）
     * POST /study/recommend/courses
     * @param params { userId, topN? }
     */
    @PostMapping("/courses")
    public Result recommendCourses(@RequestBody Map<String, Object> params) {
        try {
            Integer userId = parseInt(params.get("userId"));
            int topN = params.get("topN") != null ? parseInt(params.get("topN")) : 10;
            if (userId == null) {
                return Result.failure("userId 不能为空");
            }
            List<CourseRecommendVo> list = recommendService.recommendCourses(userId, topN);
            return Result.success(list);
        } catch (Exception e) {
            log.error("课程推荐失败", e);
            return Result.failure("课程推荐失败: " + e.getMessage());
        }
    }

    /**
     * 课程推荐 - User-Based CF
     * POST /study/recommend/courses/user-cf
     */
    @PostMapping("/courses/user-cf")
    public Result recommendByUserCF(@RequestBody Map<String, Object> params) {
        try {
            Integer userId = parseInt(params.get("userId"));
            int topN = params.get("topN") != null ? parseInt(params.get("topN")) : 10;
            if (userId == null) {
                return Result.failure("userId 不能为空");
            }
            return Result.success(recommendService.recommendCoursesByUserCF(userId, topN));
        } catch (Exception e) {
            log.error("User-CF 推荐失败", e);
            return Result.failure("User-CF 推荐失败: " + e.getMessage());
        }
    }

    /**
     * 课程推荐 - Item-Based CF
     * POST /study/recommend/courses/item-cf
     */
    @PostMapping("/courses/item-cf")
    public Result recommendByItemCF(@RequestBody Map<String, Object> params) {
        try {
            Integer userId = parseInt(params.get("userId"));
            int topN = params.get("topN") != null ? parseInt(params.get("topN")) : 10;
            if (userId == null) {
                return Result.failure("userId 不能为空");
            }
            return Result.success(recommendService.recommendCoursesByItemCF(userId, topN));
        } catch (Exception e) {
            log.error("Item-CF 推荐失败", e);
            return Result.failure("Item-CF 推荐失败: " + e.getMessage());
        }
    }

    /**
     * 练习推荐（基于薄弱知识点）
     * POST /study/recommend/exercises
     * @param params { userId, classId?, topN? }
     */
    @PostMapping("/exercises")
    public Result recommendExercises(@RequestBody Map<String, Object> params) {
        try {
            Integer userId = parseInt(params.get("userId"));
            Integer classId = parseInt(params.get("classId"));
            int topN = params.get("topN") != null ? parseInt(params.get("topN")) : 10;
            if (userId == null) {
                return Result.failure("userId 不能为空");
            }
            List<ExerciseRecommendVo> list = recommendService.recommendExercises(userId, classId, topN);
            return Result.success(list);
        } catch (Exception e) {
            log.error("练习推荐失败", e);
            return Result.failure("练习推荐失败: " + e.getMessage());
        }
    }

    /**
     * 学习路径推荐（基于知识图谱）
     * POST /study/recommend/learning-path
     * @param params { userId, courseId }
     */
    @PostMapping("/learning-path")
    public Result recommendLearningPath(@RequestBody Map<String, Object> params) {
        try {
            Integer userId = parseInt(params.get("userId"));
            Integer courseId = parseInt(params.get("courseId"));
            if (userId == null || courseId == null) {
                return Result.failure("userId 与 courseId 均不能为空");
            }
            LearningPathVo vo = recommendService.recommendLearningPath(userId, courseId);
            return Result.success(vo);
        } catch (Exception e) {
            log.error("学习路径推荐失败", e);
            return Result.failure("学习路径推荐失败: " + e.getMessage());
        }
    }

    private Integer parseInt(Object obj) {
        if (obj == null) return null;
        try {
            return Integer.valueOf(obj.toString());
        } catch (Exception e) {
            return null;
        }
    }
}
