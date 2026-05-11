package com.rabbiter.ol.controller;

import com.rabbiter.ol.common.Result;
import com.rabbiter.ol.service.CourseScannerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 课程文件扫描 - 扫描 courses/ 目录自动识别课程层次结构
 */
@RestController
@RequestMapping("study/teacher/course-scanner")
@CrossOrigin
public class CourseScannerController {

    private final CourseScannerService courseScannerService;

    public CourseScannerController() {
        this.courseScannerService = new CourseScannerService();
    }

    /**
     * 获取 courses 目录中所有课程的完整树形结构
     * GET /study/teacher/course-scanner/tree
     */
    @GetMapping("/tree")
    public Result getCourseTree() {
        List<Map<String, Object>> tree = courseScannerService.scanAllCourses();
        return Result.success(tree);
    }

    /**
     * 获取课程概览（名称、章节数、资源数）
     * GET /study/teacher/course-scanner/overview
     */
    @GetMapping("/overview")
    public Result getCourseOverview() {
        List<Map<String, Object>> overview = courseScannerService.getCourseOverview();
        return Result.success(overview);
    }
}