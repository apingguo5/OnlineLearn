package com.rabbiter.ol.controller;

import com.rabbiter.ol.common.Result;
import com.rabbiter.ol.service.AssessmentRecommendService;
import com.rabbiter.ol.vo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/study/recommend/assessment")
@CrossOrigin
public class AssessmentRecommendController {

    private static final Logger log = LoggerFactory.getLogger(AssessmentRecommendController.class);

    @Autowired
    private AssessmentRecommendService assessmentRecommendService;

    @PostMapping("/wrong-questions")
    public Result recommendWrongQuestions(@RequestBody Map<String, Object> params) {
        try {
            Integer userId = parseInt(params.get("userId"));
            Integer courseId = parseInt(params.get("courseId"));
            int topN = params.get("topN") != null ? parseInt(params.get("topN")) : 10;
            if (userId == null) {
                return Result.failure("userId 不能为空");
            }
            List<WrongQuestionRecommendVo> list =
                    assessmentRecommendService.recommendWrongQuestionReview(userId, courseId, topN);
            return Result.success(list);
        } catch (Exception e) {
            log.error("错题推荐失败", e);
            return Result.failure("错题推荐失败: " + e.getMessage());
        }
    }

    @PostMapping("/exam-papers")
    public Result recommendExamPapers(@RequestBody Map<String, Object> params) {
        try {
            Integer userId = parseInt(params.get("userId"));
            Integer courseId = parseInt(params.get("courseId"));
            Integer classId = parseInt(params.get("classId"));
            int topN = params.get("topN") != null ? parseInt(params.get("topN")) : 5;
            if (userId == null) {
                return Result.failure("userId 不能为空");
            }
            List<ExamPaperRecommendVo> list =
                    assessmentRecommendService.recommendExamPapers(userId, courseId, classId, topN);
            return Result.success(list);
        } catch (Exception e) {
            log.error("试卷推荐失败", e);
            return Result.failure("试卷推荐失败: " + e.getMessage());
        }
    }

    @PostMapping("/weak-points")
    public Result diagnoseWeakPoints(@RequestBody Map<String, Object> params) {
        try {
            Integer userId = parseInt(params.get("userId"));
            Integer courseId = parseInt(params.get("courseId"));
            int topK = params.get("topK") != null ? parseInt(params.get("topK")) : 5;
            if (userId == null || courseId == null) {
                return Result.failure("userId 与 courseId 均不能为空");
            }
            List<WeakPointDiagnosisVo> list =
                    assessmentRecommendService.diagnoseWeakPoints(userId, courseId, topK);
            return Result.success(list);
        } catch (Exception e) {
            log.error("薄弱点诊断失败", e);
            return Result.failure("薄弱点诊断失败: " + e.getMessage());
        }
    }

    @PostMapping("/peers")
    public Result compareWithPeers(@RequestBody Map<String, Object> params) {
        try {
            Integer userId = parseInt(params.get("userId"));
            Integer classId = parseInt(params.get("classId"));
            Integer courseId = parseInt(params.get("courseId"));
            if (userId == null || classId == null) {
                return Result.failure("userId 与 classId 均不能为空");
            }
            PeerCompareVo vo = assessmentRecommendService.compareWithPeers(userId, classId, courseId);
            return Result.success(vo);
        } catch (Exception e) {
            log.error("同伴对比失败", e);
            return Result.failure("同伴对比失败: " + e.getMessage());
        }
    }

    @PostMapping("/alert")
    public Result generateAlert(@RequestBody Map<String, Object> params) {
        try {
            Integer userId = parseInt(params.get("userId"));
            Integer classId = parseInt(params.get("classId"));
            if (userId == null || classId == null) {
                return Result.failure("userId 与 classId 均不能为空");
            }
            GradeAlertVo vo = assessmentRecommendService.generateAlert(userId, classId);
            return Result.success(vo);
        } catch (Exception e) {
            log.error("预警生成失败", e);
            return Result.failure("预警生成失败: " + e.getMessage());
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
