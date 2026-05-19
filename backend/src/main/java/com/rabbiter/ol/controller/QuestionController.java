package com.rabbiter.ol.controller;

import com.rabbiter.ol.common.Result;
import com.rabbiter.ol.service.QuestionService;
import com.rabbiter.ol.vo.QuestionVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/study/exam/question")
@CrossOrigin
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @PostMapping("/list")
    public Result list(@RequestBody QuestionVo questionVo) {
        Map<String, Object> page = questionService.queryPage(questionVo);
        return Result.success(page);
    }

    @PostMapping("/save")
    public Result save(@RequestBody QuestionVo questionVo) {
        questionService.saveQuestion(questionVo);
        return Result.successCode();
    }

    @PostMapping("/update")
    public Result update(@RequestBody QuestionVo questionVo) {
        questionService.updateQuestion(questionVo);
        return Result.successCode();
    }

    @PostMapping("/delete")
    public Result delete(@RequestBody QuestionVo questionVo) {
        questionService.deleteQuestion(questionVo.getId());
        return Result.successCode();
    }

    @PostMapping("/batchDelete")
    public Result batchDelete(@RequestBody List<Integer> ids) {
        questionService.batchDelete(ids);
        return Result.successCode();
    }

    @GetMapping("/info/{id}")
    public Result info(@PathVariable("id") Integer id) {
        return Result.success(questionService.getById(id));
    }

    @GetMapping("/byPaper/{paperId}")
    public Result getByPaper(@PathVariable("paperId") Integer paperId) {
        return Result.success(questionService.getQuestionsByPaperId(paperId));
    }
}