package com.rabbiter.ol.controller;

import com.rabbiter.ol.common.Result;
import com.rabbiter.ol.service.ExamPaperService;
import com.rabbiter.ol.vo.ExamPaperVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/study/exam/paper")
@CrossOrigin
public class ExamPaperController {

    @Autowired
    private ExamPaperService examPaperService;

    @PostMapping("/list")
    public Result list(@RequestBody ExamPaperVo examPaperVo) {
        Map<String, Object> page = examPaperService.queryPage(examPaperVo);
        return Result.success(page);
    }

    @PostMapping("/save")
    public Result save(@RequestBody ExamPaperVo examPaperVo) {
        examPaperService.savePaper(examPaperVo);
        return Result.successCode();
    }

    @PostMapping("/update")
    public Result update(@RequestBody ExamPaperVo examPaperVo) {
        examPaperService.updatePaper(examPaperVo);
        return Result.successCode();
    }

    @PostMapping("/delete")
    public Result delete(@RequestBody ExamPaperVo examPaperVo) {
        examPaperService.removeById(examPaperVo.getId());
        return Result.successCode();
    }

    @PostMapping("/batchDelete")
    public Result batchDelete(@RequestBody List<Integer> ids) {
        examPaperService.removeByIds(ids);
        return Result.successCode();
    }

    @GetMapping("/info/{id}")
    public Result info(@PathVariable("id") Integer id) {
        return Result.success(examPaperService.getById(id));
    }

    @GetMapping("/byCourse/{courseId}")
    public Result getByCourse(@PathVariable("courseId") Integer courseId) {
        return Result.success(examPaperService.getPapersByCourseId(courseId));
    }

    /**
     * 发布试卷
     */
    @PostMapping("/publish")
    public Result publish(@RequestBody ExamPaperVo examPaperVo) {
        examPaperService.publishPaper(examPaperVo.getId());
        return Result.successCode();
    }

    @GetMapping("/studentList/{paperId}")
    public Result getStudentList(@PathVariable("paperId") Integer paperId) {
        return Result.success(examPaperService.getPaperStudentAnswers(paperId));
    }

    /**
     * 获取试卷详情（含题目列表）
     */
    @PostMapping("/detail")
    public Result detail(@RequestBody ExamPaperVo examPaperVo) {
        return Result.success(examPaperService.getPaperDetail(examPaperVo.getId()));
    }

    /**
     * 获取学生可见的试卷列表
     */
    @PostMapping("/studentPapers")
    public Result studentPapers(@RequestBody ExamPaperVo examPaperVo) {
        return Result.success(examPaperService.getStudentPapers(examPaperVo));
    }

    /**
     * 根据课程ID获取试卷列表
     */
    @PostMapping("/listByCourse")
    public Result listByCourse(@RequestBody ExamPaperVo examPaperVo) {
        return Result.success(examPaperService.getPapersByCourseId(examPaperVo.getCourseId()));
    }
}
