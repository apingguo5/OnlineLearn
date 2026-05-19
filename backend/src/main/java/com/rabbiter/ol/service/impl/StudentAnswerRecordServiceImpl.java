package com.rabbiter.ol.service.impl;

import com.rabbiter.ol.dao.ExamPaperDao;
import com.rabbiter.ol.dao.ExamPaperQuestionDao;
import com.rabbiter.ol.dao.QuestionDao;
import com.rabbiter.ol.dao.StudentAnswerRecordDao;
import com.rabbiter.ol.entity.ExamPaperQuestionEntity;
import com.rabbiter.ol.entity.QuestionEntity;
import com.rabbiter.ol.entity.StudentAnswerRecordEntity;
import com.rabbiter.ol.service.StudentAnswerRecordService;
import com.rabbiter.ol.vo.StudentAnswerRecordVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

@Service("studentAnswerRecordService")
public class StudentAnswerRecordServiceImpl extends ServiceImpl<StudentAnswerRecordDao, StudentAnswerRecordEntity> implements StudentAnswerRecordService {

    @Autowired
    private StudentAnswerRecordDao studentAnswerRecordDao;

    @Autowired
    private QuestionDao questionDao;

    @Autowired
    private ExamPaperDao examPaperDao;

    @Autowired
    private ExamPaperQuestionDao examPaperQuestionDao;

    /**
     * 自动判分逻辑
     * @param question 题目实体
     * @param studentAnswer 学生答案
     * @return 得分（null 表示无法自动判分）
     */
    private Double autoGrade(QuestionEntity question, String studentAnswer) {
        if (question == null || studentAnswer == null) {
            return null;
        }
        if (question.getAnswer() == null || question.getAnswer().trim().isEmpty()) {
            return null;
        }

        String correctAnswer = question.getAnswer().trim();
        String answer = studentAnswer.trim();

        switch (question.getQuestionType()) {
            case 1: // 单选 - 精确匹配
            case 3: // 判断 - 精确匹配
                if (correctAnswer.equals(answer)) {
                    return question.getScore();
                }
                return 0.0;

            case 2: // 多选 - 全对满分，少选或错选0分（可扩展为部分得分）
                if (correctAnswer.equals(answer)) {
                    return question.getScore();
                }
                return 0.0;

            case 4: // 填空 - 精确匹配（忽略前后空格）
                if (correctAnswer.equals(answer)) {
                    return question.getScore();
                }
                // 支持多空格分隔的多个答案，学生答对一个即可
                String[] correctParts = correctAnswer.split("\\s+");
                if (correctParts.length > 1) {
                    for (String part : correctParts) {
                        if (part.trim().equalsIgnoreCase(answer)) {
                            return question.getScore();
                        }
                    }
                }
                return 0.0;

            default: // 问答题等无法自动判分
                return null;
        }
    }


    @Override
    public Map<String, Object> queryPage(StudentAnswerRecordVo studentAnswerRecordVo) {
        // 计算 offset 分页参数
        if (studentAnswerRecordVo.getPage() != null && studentAnswerRecordVo.getPageSize() != null) {
            studentAnswerRecordVo.setOffset((studentAnswerRecordVo.getPage() - 1) * studentAnswerRecordVo.getPageSize());
        }
        Integer total = studentAnswerRecordDao.queryCount(studentAnswerRecordVo);
        List<HashMap<String, Object>> data = studentAnswerRecordDao.queryData(studentAnswerRecordVo);
        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("data", data);
        return result;
    }

    @Override
    @Transactional
    public void submitAnswer(List<StudentAnswerRecordVo> answerList) {
        for (StudentAnswerRecordVo vo : answerList) {
            // 检查是否已有记录
            Map<String, Object> params = new HashMap<>();
            params.put("paper_id", vo.getPaperId());
            params.put("question_id", vo.getQuestionId());
            params.put("student_id", vo.getStudentId());
            List<StudentAnswerRecordEntity> existing = studentAnswerRecordDao.selectByMap(params);

            StudentAnswerRecordEntity entity;
            if (existing != null && !existing.isEmpty()) {
                // 更新已有记录
                entity = existing.get(0);
                entity.setAnswer(vo.getAnswer());
                entity.setStatus(1); // 已提交
                entity.setSubmitTime(new Date());
                entity.setUpdateTime(new Date());

                // 自动判分（支持单选、多选、判断、填空）
                QuestionEntity question = questionDao.selectById(vo.getQuestionId());
                Double score = autoGrade(question, vo.getAnswer());
                if (score != null) {
                    entity.setScore(score);
                    entity.setReviewStatus(1); // 自动批改完成
                } else {
                    entity.setReviewStatus(0); // 待批改（问答题等）
                }

                studentAnswerRecordDao.updateById(entity);
            } else {
                // 新建记录
                entity = new StudentAnswerRecordEntity();
                entity.setPaperId(vo.getPaperId());
                entity.setQuestionId(vo.getQuestionId());
                entity.setStudentId(vo.getStudentId());
                entity.setAnswer(vo.getAnswer());
                entity.setStatus(1); // 已提交
                entity.setSubmitTime(new Date());
                entity.setCreateTime(new Date());
                entity.setUpdateTime(new Date());

                // 自动判分（支持单选、多选、判断、填空）
                QuestionEntity question = questionDao.selectById(vo.getQuestionId());
                Double score = autoGrade(question, vo.getAnswer());
                if (score != null) {
                    entity.setScore(score);
                    entity.setReviewStatus(1); // 自动批改完成
                } else {
                    entity.setReviewStatus(0); // 待批改（问答题等）
                }

                studentAnswerRecordDao.insert(entity);
            }
        }
    }

    @Override
    @Transactional
    public void startExam(Integer paperId, Integer studentId) {
        // 先清除该学生该试卷之前的草稿记录
        Map<String, Object> params = new HashMap<>();
        params.put("paper_id", paperId);
        params.put("student_id", studentId);
        List<StudentAnswerRecordEntity> existing = studentAnswerRecordDao.selectByMap(params);
        if (existing != null) {
            for (StudentAnswerRecordEntity e : existing) {
                studentAnswerRecordDao.deleteById(e.getId());
            }
        }
        // 获取试卷的所有题目，创建空的答题记录
        Map<String, Object> questionParams = new HashMap<>();
        questionParams.put("paper_id", paperId);
        List<ExamPaperQuestionEntity> questions = examPaperQuestionDao.selectByMap(questionParams);
        if (questions != null) {
            Date now = new Date();
            for (ExamPaperQuestionEntity epq : questions) {
                StudentAnswerRecordEntity entity = new StudentAnswerRecordEntity();
                entity.setPaperId(paperId);
                entity.setQuestionId(epq.getQuestionId());
                entity.setStudentId(studentId);
                entity.setStatus(0); // 草稿/未答
                entity.setReviewStatus(0); // 待批改
                entity.setCreateTime(now);
                entity.setUpdateTime(now);
                studentAnswerRecordDao.insert(entity);
            }
        }
    }

    @Override
    @Transactional
    public void saveDraft(Integer paperId, Integer studentId, List<StudentAnswerRecordVo> answerList) {
        for (StudentAnswerRecordVo vo : answerList) {
            Map<String, Object> params = new HashMap<>();
            params.put("paper_id", paperId);
            params.put("question_id", vo.getQuestionId());
            params.put("student_id", studentId);
            List<StudentAnswerRecordEntity> existing = studentAnswerRecordDao.selectByMap(params);

            StudentAnswerRecordEntity entity;
            if (existing != null && !existing.isEmpty()) {
                entity = existing.get(0);
                entity.setAnswer(vo.getAnswer());
                entity.setStatus(0); // 草稿
                entity.setUpdateTime(new Date());
                studentAnswerRecordDao.updateById(entity);
            } else {
                entity = new StudentAnswerRecordEntity();
                entity.setPaperId(paperId);
                entity.setQuestionId(vo.getQuestionId());
                entity.setStudentId(studentId);
                entity.setAnswer(vo.getAnswer());
                entity.setStatus(0); // 草稿
                entity.setCreateTime(new Date());
                entity.setUpdateTime(new Date());
                studentAnswerRecordDao.insert(entity);
            }
        }
    }

    @Override
    public List<StudentAnswerRecordEntity> getStudentAnswers(Integer paperId, Integer studentId) {
        Map<String, Object> params = new HashMap<>();
        params.put("paper_id", paperId);
        params.put("student_id", studentId);
        return studentAnswerRecordDao.selectByMap(params);
    }

    @Override
    public List<HashMap<String, Object>> getStudentDetail(Integer paperId, Integer studentId) {
        return studentAnswerRecordDao.getStudentDetail(paperId, studentId);
    }

    @Override
    @Transactional
    public void reviewAnswer(Integer recordId, Double score, String remark) {
        StudentAnswerRecordEntity entity = studentAnswerRecordDao.selectById(recordId);
        if (entity != null) {
            entity.setScore(score);
            entity.setRemark(remark);
            entity.setReviewStatus(1); // 已批改
            entity.setReviewTime(new Date());
            entity.setUpdateTime(new Date());
            studentAnswerRecordDao.updateById(entity);
        }
    }

    @Override
    @Transactional
    public void batchReview(List<StudentAnswerRecordVo> reviewList) {
        for (StudentAnswerRecordVo vo : reviewList) {
            reviewAnswer(vo.getId(), vo.getScore(), vo.getRemark());
        }
    }

    @Override
    public List<HashMap<String, Object>> getPaperStudentList(Integer paperId) {
        return studentAnswerRecordDao.getPaperStudentList(paperId);
    }
}