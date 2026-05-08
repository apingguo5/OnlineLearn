package com.rabbiter.ol.controller;

import com.rabbiter.ol.entity.SubjectEntity;
import com.rabbiter.ol.service.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 教师端仪表盘接口
 * 基于 course 表（课程）
 */
@RestController
@RequestMapping("/study/teacher/dashboard")
public class TeacherDashboardController {

    @Autowired
    private SubjectService subjectService;

    /**
     * 获取教师创建的课程列表
     * POST /study/teacher/dashboard/mySubjects
     * @param params { userId }
     */
    @PostMapping("/mySubjects")
    public Map<String, Object> mySubjects(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            Integer userId = Integer.valueOf(params.get("userId").toString());

            // 使用 SubjectService 查询 course 表中该教师的所有课程
            // 通过 MyBatis Plus 的 lambdaQuery
            java.util.List<SubjectEntity> list = subjectService.lambdaQuery()
                    .eq(SubjectEntity::getCreatorId, userId)
                    .orderByDesc(SubjectEntity::getId)
                    .list();

            result.put("code", 200);
            result.put("resultData", list);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("resultData", "获取课程列表失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 创建课程
     * POST /study/teacher/dashboard/createSubject
     * @param params { courseName, userId, description? }
     */
    @PostMapping("/createSubject")
    public Map<String, Object> createSubject(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            String courseName = (String) params.get("courseName");
            if (courseName == null || courseName.trim().isEmpty()) {
                result.put("code", 400);
                result.put("resultData", "课程名称不能为空");
                return result;
            }

            Integer userId = Integer.valueOf(params.get("userId").toString());
            String description = (String) params.get("description");

            SubjectEntity entity = new SubjectEntity();
            entity.setCourseName(courseName.trim());
            entity.setCreatorId(userId);
            entity.setDescription(description != null ? description.trim() : null);
            entity.setStatus(1);  // 启用
            entity.setCreateTime(new Date());
            entity.setUpdateTime(new Date());

            subjectService.save(entity);

            result.put("code", 200);
            result.put("resultData", entity);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("resultData", "创建课程失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 删除课程
     * POST /study/teacher/dashboard/deleteSubject
     * @param params { id }
     */
    @PostMapping("/deleteSubject")
    public Map<String, Object> deleteSubject(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            Integer id = Integer.valueOf(params.get("id").toString());
            subjectService.removeById(id);

            result.put("code", 200);
            result.put("resultData", "删除成功");
        } catch (Exception e) {
            result.put("code", 500);
            result.put("resultData", "删除失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 获取所有课程列表（系统级）
     * GET /study/teacher/dashboard/subjects
     */
    @GetMapping("/subjects")
    public Map<String, Object> subjects() {
        Map<String, Object> result = new HashMap<>();
        try {
            java.util.List<SubjectEntity> list = subjectService.lambdaQuery()
                    .orderByDesc(SubjectEntity::getId)
                    .list();

            Map<String, Object> data = new HashMap<>();
            data.put("list", list);
            result.put("code", 0);
            result.put("data", data);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("resultData", "获取课程列表失败: " + e.getMessage());
        }
        return result;
    }
}