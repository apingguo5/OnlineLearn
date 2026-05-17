package com.rabbiter.ol.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rabbiter.ol.entity.StudentAnswerRecordEntity;
import com.rabbiter.ol.vo.StudentAnswerRecordVo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface StudentAnswerRecordService extends IService<StudentAnswerRecordEntity> {

    Map<String, Object> queryPage(StudentAnswerRecordVo studentAnswerRecordVo);

    /**
     * 开始考试，初始化答题记录
     */
    void startExam(Integer paperId, Integer studentId);

    /**
     * 提交答案（单题提交或整卷提交）
     */
    void submitAnswer(List<StudentAnswerRecordVo> answerList);

    /**
     * 暂存答案（存草稿）
     */
    void saveDraft(Integer paperId, Integer studentId, List<StudentAnswerRecordVo> answerList);

    /**
     * 获取学生的答题记录
     */
    List<StudentAnswerRecordEntity> getStudentAnswers(Integer paperId, Integer studentId);

    /**
     * 获取学生答题详情（含题目和批改信息）
     */
    List<HashMap<String, Object>> getStudentDetail(Integer paperId, Integer studentId);

    /**
     * 教师批改打分
     */
    void reviewAnswer(Integer recordId, Double score, String remark);

    /**
     * 教师批量批改
     */
    void batchReview(List<StudentAnswerRecordVo> reviewList);

    /**
     * 获取试卷的学生答题情况（教师端）
     */
    List<HashMap<String, Object>> getPaperStudentList(Integer paperId);
}