package com.rabbiter.ol.service.impl;

import com.rabbiter.ol.dao.ExamPaperQuestionDao;
import com.rabbiter.ol.dao.QuestionDao;
import com.rabbiter.ol.entity.QuestionEntity;
import com.rabbiter.ol.service.QuestionService;
import com.rabbiter.ol.vo.QuestionVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

@Service("questionService")
public class QuestionServiceImpl extends ServiceImpl<QuestionDao, QuestionEntity> implements QuestionService {

    @Autowired
    private QuestionDao questionDao;

    @Autowired
    private ExamPaperQuestionDao examPaperQuestionDao;

    @Override
    public Map<String, Object> queryPage(QuestionVo questionVo) {
        // 修复：计算 offset 分页参数
        if (questionVo.getPage() != null && questionVo.getPageSize() != null) {
            questionVo.setOffset((questionVo.getPage() - 1) * questionVo.getPageSize());
        }
        Integer total = questionDao.queryCount(questionVo);
        List<HashMap<String, Object>> data = questionDao.queryData(questionVo);
        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("data", data);
        return result;
    }

    @Override
    @Transactional
    public void saveQuestion(QuestionVo questionVo) {
        QuestionEntity entity = new QuestionEntity();
        entity.setQuestionType(questionVo.getQuestionType());
        entity.setStem(questionVo.getStem());
        entity.setOptions(questionVo.getOptions());
        entity.setAnswer(questionVo.getAnswer());
        entity.setAnalysis(questionVo.getAnalysis());
        entity.setScore(questionVo.getScore());
        entity.setDifficulty(questionVo.getDifficulty());
        entity.setCreatorId(questionVo.getCreatorId());
        entity.setCourseId(questionVo.getCourseId());
        entity.setChapterId(questionVo.getChapterId());
        entity.setStatus(1); // 启用
        entity.setCreateTime(new Date());
        entity.setUpdateTime(new Date());
        questionDao.insert(entity);
    }

    @Override
    @Transactional
    public void updateQuestion(QuestionVo questionVo) {
        QuestionEntity entity = questionDao.selectById(questionVo.getId());
        if (entity != null) {
            entity.setQuestionType(questionVo.getQuestionType());
            entity.setStem(questionVo.getStem());
            entity.setOptions(questionVo.getOptions());
            entity.setAnswer(questionVo.getAnswer());
            entity.setAnalysis(questionVo.getAnalysis());
            entity.setScore(questionVo.getScore());
            entity.setDifficulty(questionVo.getDifficulty());
            entity.setCourseId(questionVo.getCourseId());
            entity.setChapterId(questionVo.getChapterId());
            entity.setUpdateTime(new Date());
            questionDao.updateById(entity);
        }
    }

    @Override
    @Transactional
    public void deleteQuestion(Integer id) {
        questionDao.deleteById(id);
    }

    @Override
    @Transactional
    public void batchDelete(List<Integer> ids) {
        questionDao.deleteBatchIds(ids);
    }

    @Override
    public List<QuestionEntity> getQuestionsByPaperId(Integer paperId) {
        Map<String, Object> params = new HashMap<>();
        params.put("paper_id", paperId);
        List<com.rabbiter.ol.entity.ExamPaperQuestionEntity> refs = examPaperQuestionDao.selectByMap(params);
        if (refs != null && !refs.isEmpty()) {
            List<Integer> questionIds = refs.stream()
                    .map(com.rabbiter.ol.entity.ExamPaperQuestionEntity::getQuestionId)
                    .collect(java.util.stream.Collectors.toList());
            return questionDao.selectBatchIds(questionIds);
        }
        return java.util.Collections.emptyList();
    }
}