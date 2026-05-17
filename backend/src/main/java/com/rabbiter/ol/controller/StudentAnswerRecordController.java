package com.rabbiter.ol.controller;

import com.rabbiter.ol.common.Result;
import com.rabbiter.ol.service.StudentAnswerRecordService;
import com.rabbiter.ol.vo.StudentAnswerRecordVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/study/exam/answer")
@CrossOrigin
public class StudentAnswerRecordController {

    @Autowired
    private StudentAnswerRecordService studentAnswerRecordService;

    @PostMapping("/list")
    public Result list(@RequestBody StudentAnswerRecordVo studentAnswerRecordVo) {
        Map<String, Object> page = studentAnswerRecordService.queryPage(studentAnswerRecordVo);
        return Result.success(page);
    }

    /**
     * 开始考试，初始化答题记录
     * 请求体: { paperId: 1, studentId: 1 }
     */
    @PostMapping("/start")
    public Result startExam(@RequestBody Map<String, Object> params) {
        Integer paperId = (Integer) params.get("paperId");
        Integer studentId = (Integer) params.get("studentId");
        studentAnswerRecordService.startExam(paperId, studentId);
        return Result.successCode();
    }

    /**
     * 提交答案（批量提交整卷答案）
     * 请求体: { paperId: 1, studentId: 1, answers: [{ questionId: 1, answer: "A" }, ...] }
     */
    @SuppressWarnings("unchecked")
    @PostMapping("/submit")
    public Result submit(@RequestBody Map<String, Object> params) {
        Integer paperId = (Integer) params.get("paperId");
        Integer studentId = (Integer) params.get("studentId");
        List<Map<String, Object>> answers = (List<Map<String, Object>>) params.get("answers");

        List<StudentAnswerRecordVo> answerList = answers.stream().map(a -> {
            StudentAnswerRecordVo vo = new StudentAnswerRecordVo();
            vo.setPaperId(paperId);
            vo.setStudentId(studentId);
            vo.setQuestionId((Integer) a.get("questionId"));
            vo.setAnswer((String) a.get("answer"));
            return vo;
        }).toList();

        studentAnswerRecordService.submitAnswer(answerList);
        return Result.successCode();
    }

    /**
     * 暂存答案（存草稿）
     * 请求体: { paperId: 1, studentId: 1, answers: [{ questionId: 1, answer: "A" }, ...] }
     */
    @SuppressWarnings("unchecked")
    @PostMapping("/saveDraft")
    public Result saveDraft(@RequestBody Map<String, Object> params) {
        Integer paperId = (Integer) params.get("paperId");
        Integer studentId = (Integer) params.get("studentId");
        List<Map<String, Object>> answers = (List<Map<String, Object>>) params.get("answers");

        List<StudentAnswerRecordVo> answerList = answers.stream().map(a -> {
            StudentAnswerRecordVo vo = new StudentAnswerRecordVo();
            vo.setQuestionId((Integer) a.get("questionId"));
            vo.setAnswer((String) a.get("answer"));
            return vo;
        }).toList();

        studentAnswerRecordService.saveDraft(paperId, studentId, answerList);
        return Result.successCode();
    }

    /**
     * 获取学生答题详情（含题目和批改信息）
     */
    @GetMapping("/studentDetail")
    public Result getStudentDetail(@RequestParam Integer paperId, @RequestParam Integer studentId) {
        return Result.success(studentAnswerRecordService.getStudentDetail(paperId, studentId));
    }

    /**
     * 获取学生某试卷的答题记录
     */
    @PostMapping("/studentAnswers")
    public Result getStudentAnswers(@RequestBody Map<String, Object> params) {
        Integer paperId = (Integer) params.get("paperId");
        Integer studentId = (Integer) params.get("studentId");
        return Result.success(studentAnswerRecordService.getStudentAnswers(paperId, studentId));
    }

    /**
     * 获取试卷的学生答题情况（教师端）
     */
    @GetMapping("/paperStudentList")
    public Result getPaperStudentList(@RequestParam Integer paperId) {
        return Result.success(studentAnswerRecordService.getPaperStudentList(paperId));
    }

    /**
     * 批改单题
     */
    @PostMapping("/review")
    public Result review(@RequestBody StudentAnswerRecordVo studentAnswerRecordVo) {
        studentAnswerRecordService.reviewAnswer(
                studentAnswerRecordVo.getId(),
                studentAnswerRecordVo.getScore(),
                studentAnswerRecordVo.getRemark()
        );
        return Result.successCode();
    }

    /**
     * 批量批改
     */
    @PostMapping("/batchReview")
    public Result batchReview(@RequestBody List<StudentAnswerRecordVo> reviewList) {
        studentAnswerRecordService.batchReview(reviewList);
        return Result.successCode();
    }
}