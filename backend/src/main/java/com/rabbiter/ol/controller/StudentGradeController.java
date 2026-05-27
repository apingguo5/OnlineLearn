package com.rabbiter.ol.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.rabbiter.ol.common.Result;
import com.rabbiter.ol.entity.GradeWeightEntity;
import com.rabbiter.ol.entity.StudentGradeEntity;
import com.rabbiter.ol.service.GradeWeightService;
import com.rabbiter.ol.service.StudentGradeService;
import com.rabbiter.ol.dao.StudentGradeDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@RestController
@RequestMapping("study/grade")
@CrossOrigin
public class StudentGradeController {

    @Autowired
    private StudentGradeService studentGradeService;

    @Autowired
    private GradeWeightService weightService;

    @Autowired
    private StudentGradeDao studentGradeDao;

    @RequestMapping("/classGrades")
    public Result getClassGrades(@RequestBody Map<String, Object> params) {
        Integer classId = (Integer) params.get("classId");
        if (classId == null) {
            return Result.failureCode();
        }
        List<Map<String, Object>> result = studentGradeDao.queryClassGrades(classId);
        return Result.success(result);
    }

    @RequestMapping("/saveWeights")
    public Result saveWeights(@RequestBody Map<String, Object> params) {
        Integer classId = (Integer) params.get("classId");
        if (classId == null) {
            return Result.failureCode();
        }
        List<Map<String, Object>> weights = (List<Map<String, Object>>) params.get("weights");
        weightService.remove(new QueryWrapper<GradeWeightEntity>().eq("class_id", classId));
        for (int i = 0; i < weights.size(); i++) {
            GradeWeightEntity w = new GradeWeightEntity();
            w.setClassId(classId);
            w.setWeightName((String) weights.get(i).get("label"));
            w.setWeightPercent(new BigDecimal(weights.get(i).get("percent").toString()));
            w.setSortOrder(i + 1);
            w.setCreateTime(new Date());
            w.setUpdateTime(new Date());
            weightService.save(w);
        }
        recalcTotalScores(classId);
        return Result.successCode();
    }

    @RequestMapping("/loadWeights")
    public Result loadWeights(@RequestBody Map<String, Object> params) {
        Integer classId = (Integer) params.get("classId");
        if (classId == null) {
            return Result.failureCode();
        }
        List<GradeWeightEntity> weights = weightService.list(
            new QueryWrapper<GradeWeightEntity>().eq("class_id", classId).orderByAsc("sort_order")
        );
        List<Map<String, Object>> result = new ArrayList<>();
        for (GradeWeightEntity w : weights) {
            Map<String, Object> item = new HashMap<>();
            item.put("label", w.getWeightName());
            item.put("key", getKeyByName(w.getWeightName()));
            item.put("percent", w.getWeightPercent() != null ? w.getWeightPercent().intValue() : 0);
            result.add(item);
        }
        return Result.success(result);
    }

    @RequestMapping("/recalc")
    public Result recalcScores(@RequestBody Map<String, Object> params) {
        Integer classId = (Integer) params.get("classId");
        if (classId == null) {
            return Result.failureCode();
        }
        recalcTotalScores(classId);
        return Result.successCode();
    }

    private String getKeyByName(String name) {
        if (name == null) return "";
        if (name.contains("视频")) return "videoScore";
        if (name.contains("作业")) return "homeworkScore";
        if (name.contains("考试")) return "examScore";
        if (name.contains("讨论")) return "discussionScore";
        return "";
    }

    private void recalcTotalScores(Integer classId) {
        List<GradeWeightEntity> weights = weightService.list(
            new QueryWrapper<GradeWeightEntity>().eq("class_id", classId).orderByAsc("sort_order")
        );
        List<StudentGradeEntity> grades = studentGradeService.list(
            new QueryWrapper<StudentGradeEntity>().eq("class_id", classId)
        );
        for (StudentGradeEntity g : grades) {
            BigDecimal total = BigDecimal.ZERO;
            for (GradeWeightEntity w : weights) {
                String key = getKeyByName(w.getWeightName());
                BigDecimal score = BigDecimal.ZERO;
                if ("videoScore".equals(key)) score = g.getVideoScore();
                else if ("homeworkScore".equals(key)) score = g.getHomeworkScore();
                else if ("examScore".equals(key)) score = g.getExamScore();
                else if ("discussionScore".equals(key)) score = g.getDiscussionScore();
                if (score != null && w.getWeightPercent() != null) {
                    total = total.add(score.multiply(w.getWeightPercent()).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP));
                }
            }
            g.setTotalScore(total.setScale(0, RoundingMode.HALF_UP));
            g.setUpdateTime(new Date());
            studentGradeService.updateById(g);
        }
    }
}
