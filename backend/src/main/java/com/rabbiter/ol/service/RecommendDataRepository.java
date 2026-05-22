package com.rabbiter.ol.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 推荐系统数据仓库层
 * 统一聚合学习行为数据：视频观看、作业、答题、练习、课程进度
 * 通过 JdbcTemplate 直接查询，避免侵入既有 DAO/XML 体系
 */
@Repository
public class RecommendDataRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 获取学生已学课程ID集合（基于 user_class 关联）
     */
    public List<Integer> getLearnedCourseIds(Integer userId) {
        try {
            String sql = "SELECT DISTINCT uc.class_id AS courseId " +
                    "FROM user_class uc " +
                    "WHERE uc.user_id = ?";
            return jdbcTemplate.queryForList(sql, Integer.class, userId);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    /**
     * 获取学生所有评分行为（user_id -> course_id -> rating）
     * 评分由：视频观看时长归一化 + 作业平均分 + 练习平均分 综合得到（0-5）
     */
    public List<Map<String, Object>> getAllUserRatings() {
        try {
            String sql =
                "SELECT t.user_id AS userId, t.course_id AS courseId, " +
                "       AVG(t.rating) AS rating " +
                "FROM ( " +
                "    SELECT vw.user_id, vt.id AS course_id, " +
                "           LEAST(5.0, SUM(vw.watch_time) / 600.0) AS rating " +
                "    FROM video_watch_record vw " +
                "    JOIN video_total vt ON vw.video_total_id = vt.id " +
                "    GROUP BY vw.user_id, vt.id " +
                "    UNION ALL " +
                "    SELECT udh.user_id, h.class_id AS course_id, " +
                "           AVG(IFNULL(udh.score, 0)) / 20.0 AS rating " +
                "    FROM user_do_homework udh " +
                "    JOIN homework h ON udh.homework_id = h.id " +
                "    WHERE h.class_id IS NOT NULL " +
                "    GROUP BY udh.user_id, h.class_id " +
                "    UNION ALL " +
                "    SELECT ude.user_id, e.class_id AS course_id, " +
                "           AVG(IFNULL(ude.score, 0)) / 20.0 AS rating " +
                "    FROM user_do_exercise ude " +
                "    JOIN exercises e ON ude.exercise_id = e.id " +
                "    WHERE e.class_id IS NOT NULL " +
                "    GROUP BY ude.user_id, e.class_id " +
                ") t " +
                "WHERE t.course_id IS NOT NULL " +
                "GROUP BY t.user_id, t.course_id";
            return jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    /**
     * 获取所有可推荐的课程基础信息
     */
    public List<Map<String, Object>> getAllCourses() {
        try {
            String sql = "SELECT id, course_name AS courseName, description, cover_url AS coverUrl " +
                    "FROM course WHERE status = 1";
            return jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    /**
     * 获取学生在某课程（班级）下的作业平均得分
     */
    public Double getHomeworkAvgScore(Integer userId, Integer classId) {
        try {
            String sql = "SELECT AVG(IFNULL(udh.score, 0)) " +
                    "FROM user_do_homework udh " +
                    "JOIN homework h ON udh.homework_id = h.id " +
                    "WHERE udh.user_id = ? AND h.class_id = ?";
            Double v = jdbcTemplate.queryForObject(sql, Double.class, userId, classId);
            return v == null ? 0.0 : v;
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * 获取学生在某课程（班级）下的练习平均得分
     */
    public Double getExerciseAvgScore(Integer userId, Integer classId) {
        try {
            String sql = "SELECT AVG(IFNULL(ude.score, 0)) " +
                    "FROM user_do_exercise ude " +
                    "JOIN exercises e ON ude.exercise_id = e.id " +
                    "WHERE ude.user_id = ? AND e.class_id = ?";
            Double v = jdbcTemplate.queryForObject(sql, Double.class, userId, classId);
            return v == null ? 0.0 : v;
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * 获取学生已完成的练习ID集合
     */
    public List<Integer> getDoneExerciseIds(Integer userId) {
        try {
            String sql = "SELECT DISTINCT exercise_id FROM user_do_exercise WHERE user_id = ?";
            return jdbcTemplate.queryForList(sql, Integer.class, userId);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    /**
     * 获取某课程（班级）下所有的练习题
     */
    public List<Map<String, Object>> getExercisesByClassId(Integer classId) {
        try {
            String sql = "SELECT id, title, content, class_id AS classId, creator " +
                    "FROM exercises WHERE class_id = ?";
            return jdbcTemplate.queryForList(sql, classId);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    /**
     * 获取某课程（班级）下所有知识点
     */
    public List<Map<String, Object>> getKnowledgePointsByClassId(Integer classId) {
        try {
            String sql = "SELECT id, title, content FROM knowledge_point WHERE class_id = ?";
            return jdbcTemplate.queryForList(sql, classId);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    /**
     * 获取学生答题正确率（答题记录）
     * 返回：[{questionId, correctRate, totalCount}]
     */
    public List<Map<String, Object>> getStudentAnswerStats(Integer userId) {
        try {
            String sql = "SELECT question_id AS questionId, " +
                    "       AVG(CASE WHEN status = 1 THEN 1.0 ELSE 0.0 END) AS correctRate, " +
                    "       COUNT(*) AS totalCount " +
                    "FROM student_answer_record " +
                    "WHERE student_id = ? " +
                    "GROUP BY question_id";
            return jdbcTemplate.queryForList(sql, userId);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    /**
     * 获取某课程下的章节列表（按 sort_order 排序）
     */
    public List<Map<String, Object>> getChaptersByCourseId(Integer courseId) {
        try {
            String sql = "SELECT id, course_id AS courseId, chapter_name AS chapterName, " +
                    "       chapter_type AS chapterType, description, parent_id AS parentId, " +
                    "       sort_order AS sortOrder " +
                    "FROM course_chapter " +
                    "WHERE course_id = ? " +
                    "ORDER BY IFNULL(parent_id, 0), IFNULL(sort_order, 0), id";
            return jdbcTemplate.queryForList(sql, courseId);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    /**
     * 获取学生在某课程的章节学习状态（视频观看 / 作业 / 练习的聚合）
     * 这里通过粗粒度统计：每个章节关联资源数据，估算 mastery 0-1
     */
    public Map<Integer, Double> getChapterMastery(Integer userId, Integer courseId) {
        Map<Integer, Double> result = new HashMap<>();
        try {
            String sql = "SELECT cc.id AS chapterId, " +
                    "       AVG(CASE WHEN vw.watch_time IS NULL THEN 0 " +
                    "                WHEN vw.watch_time >= 300 THEN 1.0 " +
                    "                ELSE vw.watch_time / 300.0 END) AS mastery " +
                    "FROM course_chapter cc " +
                    "LEFT JOIN chapter_resource_ref crr ON crr.chapter_id = cc.id " +
                    "LEFT JOIN videos v ON crr.resource_type = 'video' AND crr.resource_id = v.id " +
                    "LEFT JOIN video_watch_record vw ON vw.video_id = v.id AND vw.user_id = ? " +
                    "WHERE cc.course_id = ? " +
                    "GROUP BY cc.id";
            List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, userId, courseId);
            for (Map<String, Object> row : list) {
                Integer chapterId = ((Number) row.get("chapterId")).intValue();
                Object mObj = row.get("mastery");
                Double mastery = mObj == null ? 0.0 : ((Number) mObj).doubleValue();
                result.put(chapterId, mastery);
            }
        } catch (Exception e) {
            // 表结构缺失时返回空映射
        }
        return result;
    }

    /**
     * 获取课程名称
     */
    public String getCourseName(Integer courseId) {
        try {
            String sql = "SELECT course_name FROM course WHERE id = ?";
            return jdbcTemplate.queryForObject(sql, String.class, courseId);
        } catch (Exception e) {
            return null;
        }
    }
}
