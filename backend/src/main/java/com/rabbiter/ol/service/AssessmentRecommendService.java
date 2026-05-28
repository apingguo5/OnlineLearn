package com.rabbiter.ol.service;

import com.rabbiter.ol.vo.*;

import java.util.List;

public interface AssessmentRecommendService {

    List<WrongQuestionRecommendVo> recommendWrongQuestionReview(Integer userId, Integer courseId, int topN);

    List<ExamPaperRecommendVo> recommendExamPapers(Integer userId, Integer courseId, Integer classId, int topN);

    List<WeakPointDiagnosisVo> diagnoseWeakPoints(Integer userId, Integer courseId, int topK);

    PeerCompareVo compareWithPeers(Integer userId, Integer classId, Integer courseId);

    GradeAlertVo generateAlert(Integer userId, Integer classId);
}
