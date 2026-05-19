package com.rabbiter.ol.service.impl;

import com.rabbiter.ol.dao.ExamPaperDao;
import com.rabbiter.ol.dao.ExamPaperQuestionDao;
import com.rabbiter.ol.dao.QuestionDao;
import com.rabbiter.ol.dao.StudentAnswerRecordDao;
import com.rabbiter.ol.entity.ExamPaperEntity;
import com.rabbiter.ol.entity.ExamPaperQuestionEntity;
import com.rabbiter.ol.entity.QuestionEntity;
import com.rabbiter.ol.service.ExamPaperService;
import com.rabbiter.ol.vo.ExamPaperVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

@Service("examPaperService")
public class ExamPaperServiceImpl extends ServiceImpl<ExamPaperDao, ExamPaperEntity> implements ExamPaperService {

    @Autowired
    private ExamPaperDao examPaperDao;

    @Autowired
    private QuestionDao questionDao;

    @Autowired
    private ExamPaperQuestionDao examPaperQuestionDao;

    @Override
    public Map<String, Object> queryPage(ExamPaperVo examPaperVo) {
        if (examPaperVo.getPage() != null && examPaperVo.getPageSize() != null) {
            examPaperVo.setOffset((examPaperVo.getPage() - 1) * examPaperVo.getPageSize());
        }
        Integer total = examPaperDao.queryCount(examPaperVo);
        List<HashMap<String, Object>> data = examPaperDao.queryData(examPaperVo);
        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("data", data);
        return result;
    }

    @Override
    @Transactional
    public void savePaper(ExamPaperVo examPaperVo) {
        ExamPaperEntity entity = new ExamPaperEntity();
        entity.setTitle(examPaperVo.getTitle());
        entity.setDescription(examPaperVo.getDescription());
        entity.setPaperType(examPaperVo.getPaperType());
        entity.setCourseId(examPaperVo.getCourseId());
        entity.setChapterId(examPaperVo.getChapterId());
        entity.setClassId(examPaperVo.getClassId());
        entity.setCreatorId(examPaperVo.getCreatorId());
        entity.setDuration(examPaperVo.getDuration());
        entity.setTotalScore(examPaperVo.getTotalScore());
        entity.setStatus(0); // 草稿状态
        entity.setCreateTime(new Date());
        entity.setUpdateTime(new Date());
        examPaperDao.insert(entity);

        // 保存试卷-题目关联
        if (examPaperVo.getQuestionIds() != null && !examPaperVo.getQuestionIds().isEmpty()) {
            for (int i = 0; i < examPaperVo.getQuestionIds().size(); i++) {
                ExamPaperQuestionEntity ref = new ExamPaperQuestionEntity();
                ref.setPaperId(entity.getId());
                ref.setQuestionId(examPaperVo.getQuestionIds().get(i));
                ref.setSortOrder(i);
                ref.setCreateTime(new Date());
                examPaperQuestionDao.insert(ref);
            }
        }
    }

    @Override
    @Transactional
    public void updatePaper(ExamPaperVo examPaperVo) {
        ExamPaperEntity entity = examPaperDao.selectById(examPaperVo.getId());
        if (entity != null) {
            entity.setTitle(examPaperVo.getTitle());
            entity.setDescription(examPaperVo.getDescription());
            entity.setPaperType(examPaperVo.getPaperType());
            entity.setDuration(examPaperVo.getDuration());
            entity.setTotalScore(examPaperVo.getTotalScore());
            entity.setUpdateTime(new Date());
            examPaperDao.updateById(entity);

            // 更新题目关联：先删除旧的，再插入新的
            Map<String, Object> deleteParams = new HashMap<>();
            deleteParams.put("paper_id", entity.getId());
            examPaperQuestionDao.deleteByMap(deleteParams);

            if (examPaperVo.getQuestionIds() != null && !examPaperVo.getQuestionIds().isEmpty()) {
                for (int i = 0; i < examPaperVo.getQuestionIds().size(); i++) {
                    ExamPaperQuestionEntity ref = new ExamPaperQuestionEntity();
                    ref.setPaperId(entity.getId());
                    ref.setQuestionId(examPaperVo.getQuestionIds().get(i));
                    ref.setSortOrder(i);
                    ref.setCreateTime(new Date());
                    examPaperQuestionDao.insert(ref);
                }
            }
        }
    }

    @Override
    @Transactional
    public void deletePaper(Integer id) {
        examPaperDao.deleteById(id);
        // 删除关联
        Map<String, Object> params = new HashMap<>();
        params.put("paper_id", id);
        examPaperQuestionDao.deleteByMap(params);
    }

    @Override
    @Transactional
    public void publishPaper(Integer id) {
        ExamPaperEntity entity = examPaperDao.selectById(id);
        if (entity != null) {
            entity.setStatus(1); // 已发布
            entity.setPublishTime(new Date());
            entity.setUpdateTime(new Date());
            examPaperDao.updateById(entity);
        }
    }

    @Override
    public Map<String, Object> getStudentPapers(ExamPaperVo examPaperVo) {
        // 查询已发布且对于该班级/课程可见的试卷
        if (examPaperVo.getPage() != null && examPaperVo.getPageSize() != null) {
            examPaperVo.setOffset((examPaperVo.getPage() - 1) * examPaperVo.getPageSize());
        }
        examPaperVo.setStatus(1); // 只查已发布的
        Integer total = examPaperDao.queryCount(examPaperVo);
        List<HashMap<String, Object>> data = examPaperDao.queryData(examPaperVo);
        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("data", data);
        return result;
    }

    @Autowired
    private StudentAnswerRecordDao studentAnswerRecordDao;

    @Override
    public List<ExamPaperEntity> getPapersByCourseId(Integer courseId) {
        Map<String, Object> params = new HashMap<>();
        params.put("course_id", courseId);
        return examPaperDao.selectByMap(params);
    }

    @Override
    public List<HashMap<String, Object>> getPaperStudentAnswers(Integer paperId) {
        return studentAnswerRecordDao.getPaperStudentList(paperId);
    }

    @Override
    public Map<String, Object> getPaperDetail(Integer paperId) {
        Map<String, Object> result = new HashMap<>();
        ExamPaperEntity paper = examPaperDao.selectById(paperId);
        if (paper == null) {
            return result;
        }
        // 将试卷字段平铺到返回结果中，方便前端直接访问
        result.put("id", paper.getId());
        result.put("title", paper.getTitle());
        result.put("description", paper.getDescription());
        result.put("paperType", paper.getPaperType());
        result.put("courseId", paper.getCourseId());
        result.put("chapterId", paper.getChapterId());
        result.put("classId", paper.getClassId());
        result.put("creatorId", paper.getCreatorId());
        result.put("duration", paper.getDuration());
        result.put("totalScore", paper.getTotalScore());
        result.put("status", paper.getStatus());
        result.put("publishTime", paper.getPublishTime());
        result.put("endTime", paper.getEndTime());

        // 查询题目列表（按 sort_order 排序）
        Map<String, Object> params = new HashMap<>();
        params.put("paper_id", paperId);
        List<ExamPaperQuestionEntity> refs = examPaperQuestionDao.selectByMap(params);

        if (refs != null && !refs.isEmpty()) {
            // 按 sort_order 排序
            refs.sort(java.util.Comparator.comparingInt(ExamPaperQuestionEntity::getSortOrder));
            List<Integer> questionIds = refs.stream()
                    .map(ExamPaperQuestionEntity::getQuestionId)
                    .collect(java.util.stream.Collectors.toList());
            
            // 按题库顺序获取题目
            List<QuestionEntity> questions = questionDao.selectBatchIds(questionIds);
            
            // 保持与 sort_order 一致
            Map<Integer, QuestionEntity> questionMap = new HashMap<>();
            for (QuestionEntity q : questions) {
                questionMap.put(q.getId(), q);
            }
            List<HashMap<String, Object>> orderedQuestions = new java.util.ArrayList<>();
            for (Integer qid : questionIds) {
                QuestionEntity q = questionMap.get(qid);
                if (q != null) {
                    HashMap<String, Object> qMap = new HashMap<>();
                    qMap.put("id", q.getId());
                    qMap.put("questionType", q.getQuestionType());
                    qMap.put("stem", q.getStem());
                    qMap.put("options", q.getOptions());
                    qMap.put("answer", q.getAnswer());
                    qMap.put("score", q.getScore());
                    qMap.put("analysis", q.getAnalysis());
                    orderedQuestions.add(qMap);
                }
            }
            result.put("questions", orderedQuestions);
        }

        return result;
    }
}