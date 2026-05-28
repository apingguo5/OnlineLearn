package com.rabbiter.ol.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class AssessmentRecommendDataRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Map<String, Object>> getStudentWrongAnswers(Integer userId, Integer courseId) {
        try {
            String sql =
                "SELECT sar.id, sar.question_id AS questionId, sar.paper_id AS paperId, " +
                "       sar.answer AS studentAnswer, sar.score AS studentScore, sar.submit_time AS submitTime, " +
                "       q.stem, q.options, q.answer AS correctAnswer, q.analysis, " +
                "       q.question_type AS questionType, q.difficulty, " +
                "       q.course_id AS courseId, q.chapter_id AS chapterId, q.score AS fullScore, " +
                "       cc.chapter_name AS chapterName " +
                "FROM student_answer_record sar " +
                "JOIN question q ON sar.question_id = q.id " +
                "LEFT JOIN course_chapter cc ON q.chapter_id = cc.id " +
                "WHERE sar.student_id = ? AND sar.status = 2 " +
                "  AND (sar.score IS NULL OR sar.score < q.score * 0.6) " +
                (courseId != null ? " AND q.course_id = " + courseId : "") +
                " ORDER BY sar.submit_time DESC";
            return jdbcTemplate.queryForList(sql, userId);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public List<Map<String, Object>> getAllQuestionsByCourse(Integer courseId) {
        try {
            String sql =
                "SELECT q.id, q.stem, q.options, q.answer, q.analysis, " +
                "       q.question_type AS questionType, q.difficulty, " +
                "       q.course_id AS courseId, q.chapter_id AS chapterId, q.score, " +
                "       cc.chapter_name AS chapterName " +
                "FROM question q " +
                "LEFT JOIN course_chapter cc ON q.chapter_id = cc.id " +
                "WHERE q.status = 1 AND q.course_id = ?";
            return jdbcTemplate.queryForList(sql, courseId);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public List<Map<String, Object>> getDoneQuestionIds(Integer userId) {
        try {
            String sql = "SELECT DISTINCT question_id FROM student_answer_record WHERE student_id = ? AND status = 2";
            return jdbcTemplate.queryForList(sql, userId);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public List<Map<String, Object>> getPublishedPapers(Integer courseId, Integer classId) {
        try {
            StringBuilder sql = new StringBuilder(
                "SELECT ep.id, ep.title, ep.description, ep.paper_type AS paperType, " +
                "       ep.total_score AS totalScore, ep.duration, " +
                "       ep.course_id AS courseId, c.course_name AS courseName, " +
                "       (SELECT COUNT(*) FROM exam_paper_question epq WHERE epq.paper_id = ep.id) AS questionCount " +
                "FROM exam_paper ep " +
                "JOIN course c ON ep.course_id = c.id " +
                "WHERE ep.status = 1 ");
            if (courseId != null) sql.append(" AND ep.course_id = ").append(courseId);
            if (classId != null) sql.append(" AND (ep.class_id IS NULL OR ep.class_id = ").append(classId).append(")");
            sql.append(" ORDER BY ep.create_time DESC");
            return jdbcTemplate.queryForList(sql.toString());
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public Double getPaperAvgDifficulty(Integer paperId) {
        try {
            String sql =
                "SELECT AVG(COALESCE(q.difficulty, 1)) " +
                "FROM exam_paper_question epq " +
                "JOIN question q ON epq.question_id = q.id " +
                "WHERE epq.paper_id = ?";
            Double v = jdbcTemplate.queryForObject(sql, Double.class, paperId);
            return v == null ? 1.0 : v;
        } catch (Exception e) {
            return 1.0;
        }
    }

    public List<Map<String, Object>> getDonePaperIds(Integer userId) {
        try {
            String sql = "SELECT DISTINCT paper_id FROM student_answer_record WHERE student_id = ? AND status = 2";
            return jdbcTemplate.queryForList(sql, userId);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public List<Map<String, Object>> getStudentAnswerHistory(Integer userId, Integer courseId) {
        try {
            String sql =
                "SELECT sar.score AS studentScore, sar.submit_time AS submitTime, " +
                "       ep.total_score AS paperTotalScore, ep.paper_type AS paperType, ep.title AS paperTitle " +
                "FROM student_answer_record sar " +
                "JOIN exam_paper ep ON sar.paper_id = ep.id " +
                "WHERE sar.student_id = ? AND sar.status = 2 " +
                (courseId != null ? " AND ep.course_id = " + courseId : "") +
                " ORDER BY sar.submit_time ASC";
            return jdbcTemplate.queryForList(sql, userId);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public List<Map<String, Object>> getStudentRecentScores(Integer userId) {
        try {
            String sql =
                "SELECT sar.score AS studentScore, sar.submit_time AS submitTime, " +
                "       q.score AS fullScore, q.difficulty " +
                "FROM student_answer_record sar " +
                "JOIN question q ON sar.question_id = q.id " +
                "WHERE sar.student_id = ? AND sar.status = 2 AND sar.score IS NOT NULL " +
                "ORDER BY sar.submit_time DESC LIMIT 50";
            return jdbcTemplate.queryForList(sql, userId);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public List<Map<String, Object>> getClassStudentsWithFeatures(Integer classId) {
        try {
            String sql =
                "SELECT u.id AS userId, u.user_name AS userName, " +
                "       COALESCE(sg.video_score, 0) AS videoScore, " +
                "       COALESCE(sg.homework_score, 0) AS homeworkScore, " +
                "       COALESCE(sg.exam_score, 0) AS examScore, " +
                "       COALESCE(sg.total_score, 0) AS totalScore " +
                "FROM user_class uc " +
                "JOIN user u ON uc.user_id = u.id " +
                "JOIN user_role ur ON ur.user_id = u.id AND ur.role_id = 3 " +
                "LEFT JOIN student_grade sg ON sg.user_id = uc.user_id AND sg.class_id = uc.class_id " +
                "WHERE uc.class_id = ?";
            return jdbcTemplate.queryForList(sql, classId);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public List<Map<String, Object>> getStudentChapterStats(Integer userId, Integer courseId) {
        try {
            String sql =
                "SELECT cc.id AS chapterId, cc.chapter_name AS chapterName, " +
                "       COUNT(sar.id) AS totalCount, " +
                "       SUM(CASE WHEN sar.score IS NOT NULL AND q.score IS NOT NULL " +
                "               AND sar.score >= q.score * 0.6 THEN 1 ELSE 0 END) AS correctCount " +
                "FROM course_chapter cc " +
                "LEFT JOIN question q ON q.chapter_id = cc.id AND q.status = 1 " +
                "LEFT JOIN student_answer_record sar ON sar.question_id = q.id " +
                "    AND sar.student_id = ? AND sar.status = 2 " +
                "WHERE cc.course_id = ? " +
                "GROUP BY cc.id, cc.chapter_name " +
                "ORDER BY cc.sort_order, cc.id";
            return jdbcTemplate.queryForList(sql, userId, courseId);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public List<Map<String, Object>> getChapterQuestions(Integer chapterId) {
        try {
            String sql =
                "SELECT q.id, q.stem, q.question_type AS questionType, q.difficulty, q.score " +
                "FROM question q " +
                "WHERE q.chapter_id = ? AND q.status = 1 " +
                "ORDER BY q.difficulty, q.id " +
                "LIMIT 10";
            return jdbcTemplate.queryForList(sql, chapterId);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public List<Map<String, Object>> getChapterVideos(Integer chapterId) {
        try {
            String sql =
                "SELECT id, resource_name AS title, description " +
                "FROM course_resource " +
                "WHERE chapter_id = ? AND resource_type = 1 " +
                "LIMIT 5";
            return jdbcTemplate.queryForList(sql, chapterId);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public String getCourseName(Integer courseId) {
        try {
            String sql = "SELECT course_name FROM course WHERE id = ?";
            return jdbcTemplate.queryForObject(sql, String.class, courseId);
        } catch (Exception e) {
            return null;
        }
    }

    public Map<Integer, Double> getStudentQuestionScores(Integer userId) {
        Map<Integer, Double> result = new HashMap<>();
        try {
            String sql =
                "SELECT question_id AS questionId, score " +
                "FROM student_answer_record " +
                "WHERE student_id = ? AND status = 2 AND score IS NOT NULL";
            List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, userId);
            for (Map<String, Object> row : list) {
                Integer qid = ((Number) row.get("questionId")).intValue();
                Double score = ((Number) row.get("score")).doubleValue();
                result.put(qid, score);
            }
        } catch (Exception e) {
            // ignore
        }
        return result;
    }

    public List<Map<String, Object>> getStudentGradesForTrend(Integer userId, Integer classId) {
        try {
            String sql =
                "SELECT video_score AS videoScore, homework_score AS homeworkScore, " +
                "       exam_score AS examScore, total_score AS totalScore, " +
                "       update_time AS recordTime " +
                "FROM student_grade " +
                "WHERE user_id = ? AND class_id = ? " +
                "ORDER BY update_time ASC";
            return jdbcTemplate.queryForList(sql, userId, classId);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public Double getClassAverageScore(Integer classId) {
        try {
            String sql =
                "SELECT AVG(COALESCE(sg.total_score, 0)) " +
                "FROM user_class uc " +
                "JOIN user_role ur ON ur.user_id = uc.user_id AND ur.role_id = 3 " +
                "LEFT JOIN student_grade sg ON sg.user_id = uc.user_id AND sg.class_id = uc.class_id " +
                "WHERE uc.class_id = ?";
            Double v = jdbcTemplate.queryForObject(sql, Double.class, classId);
            return v == null ? 0.0 : v;
        } catch (Exception e) {
            return 0.0;
        }
    }

    public Integer getClassStudentCount(Integer classId) {
        try {
            String sql =
                "SELECT COUNT(*) FROM user_class uc " +
                "JOIN user_role ur ON ur.user_id = uc.user_id AND ur.role_id = 3 " +
                "WHERE uc.class_id = ?";
            return jdbcTemplate.queryForObject(sql, Integer.class, classId);
        } catch (Exception e) {
            return 0;
        }
    }

    public Integer getStudentRankInClass(Integer userId, Integer classId) {
        try {
            String sql =
                "SELECT COUNT(*) + 1 FROM student_grade sg " +
                "JOIN user_class uc ON uc.user_id = sg.user_id AND uc.class_id = sg.class_id " +
                "WHERE sg.class_id = ? AND COALESCE(sg.total_score, 0) > " +
                "(SELECT COALESCE(total_score, 0) FROM student_grade WHERE user_id = ? AND class_id = ?)";
            return jdbcTemplate.queryForObject(sql, Integer.class, classId, userId, classId);
        } catch (Exception e) {
            return 1;
        }
    }

    public List<Map<String, Object>> getPeerQuestionMastery(Integer userId, Integer classId, Integer courseId) {
        try {
            String sql =
                "SELECT q.id AS questionId, q.stem, q.difficulty, " +
                "       COUNT(DISTINCT sar2.student_id) AS correctPeerCount " +
                "FROM student_answer_record sar1 " +
                "JOIN user_class uc1 ON uc1.user_id = sar1.student_id AND uc1.class_id = ? " +
                "JOIN question q ON sar1.question_id = q.id " +
                "JOIN student_answer_record sar2 ON sar2.question_id = q.id " +
                "    AND sar2.student_id != ? AND sar2.status = 2 " +
                "    AND sar2.score IS NOT NULL AND q.score IS NOT NULL " +
                "    AND sar2.score >= q.score * 0.6 " +
                "WHERE sar1.student_id = ? AND sar1.status = 2 " +
                "  AND (sar1.score IS NULL OR q.score IS NULL OR sar1.score < q.score * 0.6) " +
                (courseId != null ? " AND q.course_id = " + courseId : "") +
                " GROUP BY q.id, q.stem, q.difficulty " +
                "HAVING correctPeerCount > 0 " +
                "ORDER BY correctPeerCount DESC LIMIT 20";
            return jdbcTemplate.queryForList(sql, classId, userId, userId);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public List<Map<String, Object>> getPeerPendingQuestions(Integer userId, Integer classId, Integer courseId) {
        try {
            String sql =
                "SELECT q.id AS questionId, q.stem, q.difficulty, " +
                "       COUNT(DISTINCT sar.student_id) AS correctPeerCount " +
                "FROM question q " +
                "JOIN student_answer_record sar ON sar.question_id = q.id " +
                "    AND sar.student_id IN ( " +
                "        SELECT uc.user_id FROM user_class uc " +
                "        JOIN user_role ur ON ur.user_id = uc.user_id AND ur.role_id = 3 " +
                "        WHERE uc.class_id = ? AND uc.user_id != ? " +
                "    ) " +
                "    AND sar.status = 2 AND sar.score IS NOT NULL " +
                "    AND q.score IS NOT NULL AND sar.score >= q.score * 0.6 " +
                "WHERE q.status = 1 AND q.id NOT IN ( " +
                "    SELECT DISTINCT question_id FROM student_answer_record " +
                "    WHERE student_id = ? AND status = 2 " +
                ") " +
                (courseId != null ? " AND q.course_id = " + courseId : "") +
                " GROUP BY q.id, q.stem, q.difficulty " +
                "HAVING correctPeerCount >= 2 " +
                "ORDER BY correctPeerCount DESC LIMIT 20";
            return jdbcTemplate.queryForList(sql, classId, userId, userId);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
