package com.rabbiter.ol.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.rabbiter.ol.common.Result;
import com.rabbiter.ol.entity.UserClassEntity;
import com.rabbiter.ol.entity.UserDoHomeworkEntity;
import com.rabbiter.ol.entity.UserEntity;
import com.rabbiter.ol.entity.UserRoleEntity;
import com.rabbiter.ol.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 教师仪表盘 - 整合教师核心功能
 * 布置批改作业 + 管理自己授课班级的学生
 */
@RestController
@RequestMapping("study/teacher/dashboard")
@CrossOrigin
public class TeacherDashboardController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserClassService userClassService;

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private UserDoHomeworkService userDoHomeworkService;

    @Autowired
    private HomeworkService homeworkService;

    /**
     * 搜索学生（按账号或姓名模糊搜索，只返回学生角色）
     */
    @RequestMapping("/searchStudent")
    public Result searchStudent(@RequestBody Map<String, Object> params) {
        String keyword = (String) params.get("keyword");
        Integer page = (Integer) params.getOrDefault("page", 1);
        Integer pageSize = (Integer) params.getOrDefault("pageSize", 10);

        // 先查出所有学生角色ID
        List<UserRoleEntity> studentRoles = userRoleService.list(
                new QueryWrapper<UserRoleEntity>().eq("role_id", 3)
        );
        if (studentRoles.isEmpty()) {
            return Result.success(new HashMap<String, Object>() {{
                put("data", new java.util.ArrayList<>());
                put("total", 0);
            }});
        }
        List<Integer> studentIds = studentRoles.stream()
                .map(UserRoleEntity::getUserId)
                .collect(Collectors.toList());

        // 按关键词模糊搜索学生
        QueryWrapper<UserEntity> wrapper = new QueryWrapper<>();
        wrapper.in("id", studentIds);
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w
                    .like("account", keyword)
                    .or()
                    .like("user_name", keyword)
            );
        }
        wrapper.orderByDesc("id");

        // 分页
        List<UserEntity> allList = userService.list(wrapper);
        int total = allList.size();
        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        List<UserEntity> pagedList = (fromIndex >= total) ? new java.util.ArrayList<>() :
                allList.subList(fromIndex, toIndex);

        Map<String, Object> result = new HashMap<>();
        result.put("data", pagedList);
        result.put("total", total);
        return Result.success(result);
    }

    /**
     * 添加学生到班级
     */
    @RequestMapping("/addStudentToClass")
    public Result addStudentToClass(@RequestBody Map<String, Object> params) {
        Integer studentId = (Integer) params.get("studentId");
        Integer classId = (Integer) params.get("classId");

        if (studentId == null || classId == null) {
            return Result.failure("参数错误");
        }

        // 检查是否已在该班级
        long count = userClassService.count(
                new QueryWrapper<UserClassEntity>()
                        .eq("user_id", studentId)
                        .eq("class_id", classId)
        );
        if (count > 0) {
            return Result.failure("该学生已在本班级中");
        }

        UserClassEntity userClass = new UserClassEntity();
        userClass.setUserId(studentId);
        userClass.setClassId(classId);
        boolean save = userClassService.save(userClass);
        if (save) {
            return Result.successCode();
        }
        return Result.failureCode();
    }

    /**
     * 批量添加学生到班级
     */
    @RequestMapping("/batchAddStudents")
    public Result batchAddStudents(@RequestBody Map<String, Object> params) {
        List<Integer> studentIds = (List<Integer>) params.get("studentIds");
        Integer classId = (Integer) params.get("classId");

        if (studentIds == null || studentIds.isEmpty() || classId == null) {
            return Result.failure("参数错误");
        }

        int successCount = 0;
        for (Integer studentId : studentIds) {
            long count = userClassService.count(
                    new QueryWrapper<UserClassEntity>()
                            .eq("user_id", studentId)
                            .eq("class_id", classId)
            );
            if (count == 0) {
                UserClassEntity userClass = new UserClassEntity();
                userClass.setUserId(studentId);
                userClass.setClassId(classId);
                if (userClassService.save(userClass)) {
                    successCount++;
                }
            }
        }
        final int finalSuccess = successCount;
        return Result.success(new HashMap<String, Object>() {{
            put("successCount", finalSuccess);
        }});
    }

    /**
     * 移出学生
     */
    @RequestMapping("/removeStudent")
    public Result removeStudent(@RequestBody Map<String, Object> params) {
        Integer studentId = (Integer) params.get("studentId");
        Integer classId = (Integer) params.get("classId");

        if (studentId == null || classId == null) {
            return Result.failure("参数错误");
        }

        boolean remove = userClassService.remove(
                new QueryWrapper<UserClassEntity>()
                        .eq("user_id", studentId)
                        .eq("class_id", classId)
        );
        if (remove) {
            return Result.successCode();
        }
        return Result.failureCode();
    }

    /**
     * 获取当前班级学生列表
     */
    @RequestMapping("/classStudents")
    public Result classStudents(@RequestBody Map<String, Object> params) {
        Integer classId = (Integer) params.get("classId");
        Integer page = (Integer) params.getOrDefault("page", 1);
        Integer pageSize = (Integer) params.getOrDefault("pageSize", 10);

        if (classId == null) {
            return Result.failure("班级ID不能为空");
        }

        // 获取班级所有学生关联
        List<UserClassEntity> userClassList = userClassService.list(
                new QueryWrapper<UserClassEntity>().eq("class_id", classId)
        );
        if (userClassList.isEmpty()) {
            return Result.success(new HashMap<String, Object>() {{
                put("data", new java.util.ArrayList<>());
                put("total", 0);
            }});
        }

        List<Integer> userIds = userClassList.stream()
                .map(UserClassEntity::getUserId)
                .collect(Collectors.toList());

        // 获取学生详情
        List<UserEntity> students = userService.list(
                new QueryWrapper<UserEntity>().in("id", userIds)
        );

        int total = students.size();
        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        List<UserEntity> pagedList = (fromIndex >= total) ? new java.util.ArrayList<>() :
                students.subList(fromIndex, toIndex);

        Map<String, Object> result = new HashMap<>();
        result.put("data", pagedList);
        result.put("total", total);
        return Result.success(result);
    }

    /**
     * 批改作业 - 更新评分和评语
     */
    @RequestMapping("/gradeHomework")
    public Result gradeHomework(@RequestBody Map<String, Object> params) {
        Integer id = (Integer) params.get("id");
        Double score = params.get("score") != null ?
                Double.parseDouble(params.get("score").toString()) : null;
        String remark = (String) params.get("remark");

        if (id == null || score == null) {
            return Result.failure("参数错误");
        }

        UserDoHomeworkEntity entity = new UserDoHomeworkEntity();
        entity.setId(id);
        entity.setScore(score);
        entity.setRemark(remark);
        boolean b = userDoHomeworkService.updateById(entity);
        if (b) {
            return Result.successCode();
        }
        return Result.failureCode();
    }

    /**
     * 获取某作业的提交列表（待批改/已批改）
     */
    @RequestMapping("/homeworkSubmissions")
    public Result homeworkSubmissions(@RequestBody Map<String, Object> params) {
        Integer homeworkId = (Integer) params.get("homeworkId");
        Integer page = (Integer) params.getOrDefault("page", 1);
        Integer pageSize = (Integer) params.getOrDefault("pageSize", 10);

        if (homeworkId == null) {
            return Result.failure("作业ID不能为空");
        }

        // 获取作业提交记录
        QueryWrapper<UserDoHomeworkEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("homework_id", homeworkId);
        wrapper.orderByDesc("completion_time");

        List<UserDoHomeworkEntity> allList = userDoHomeworkService.list(wrapper);
        int total = allList.size();
        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        List<UserDoHomeworkEntity> pagedList = (fromIndex >= total) ? new java.util.ArrayList<>() :
                allList.subList(fromIndex, toIndex);

        // 获取学生姓名并构建增强数据
        List<Map<String, Object>> enhancedList = new java.util.ArrayList<>();
        for (UserDoHomeworkEntity item : pagedList) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", item.getId());
            map.put("userId", item.getUserId());
            map.put("homeworkId", item.getHomeworkId());
            map.put("reply", item.getReply());
            map.put("completionTime", item.getCompletionTime());
            map.put("score", item.getScore());
            map.put("remark", item.getRemark());

            UserEntity user = userService.getById(item.getUserId());
            if (user != null) {
                map.put("studentName", user.getUserName());
                map.put("studentAccount", user.getAccount());
            } else {
                map.put("studentName", "未知");
                map.put("studentAccount", "");
            }
            enhancedList.add(map);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("data", enhancedList);
        result.put("total", total);
        return Result.success(result);
    }
}