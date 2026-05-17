package com.rabbiter.ol.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rabbiter.ol.entity.QuestionEntity;
import com.rabbiter.ol.vo.QuestionVo;

import java.util.List;
import java.util.Map;

public interface QuestionService extends IService<QuestionEntity> {

    Map<String, Object> queryPage(QuestionVo questionVo);

    void saveQuestion(QuestionVo questionVo);

    void updateQuestion(QuestionVo questionVo);

    void deleteQuestion(Integer id);

    void batchDelete(List<Integer> ids);

    /**
     * 根据试卷ID获取题目列表
     */
    List<QuestionEntity> getQuestionsByPaperId(Integer paperId);
}