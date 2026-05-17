package com.rabbiter.ol.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rabbiter.ol.entity.ExamPaperEntity;
import com.rabbiter.ol.vo.ExamPaperVo;

import java.util.Map;

public interface ExamPaperService extends IService<ExamPaperEntity> {

    Map<String, Object> queryPage(ExamPaperVo examPaperVo);

    void savePaper(ExamPaperVo examPaperVo);

    void updatePaper(ExamPaperVo examPaperVo);

    void deletePaper(Integer id);

    void publishPaper(Integer id);

    /**
     * 获取学生可见的试卷列表
     */
    Map<String, Object> getStudentPapers(ExamPaperVo examPaperVo);

    /**
     * 获取试卷详情（含题目）
     */
    Map<String, Object> getPaperDetail(Integer paperId);

    /**
     * 获取课程下所有试卷
     */
    java.util.List<com.rabbiter.ol.entity.ExamPaperEntity> getPapersByCourseId(Integer courseId);

    /**
     * 获取某试卷的学生作答情况（用于教师批改）
     */
    java.util.List<java.util.HashMap<String, Object>> getPaperStudentAnswers(Integer paperId);
}