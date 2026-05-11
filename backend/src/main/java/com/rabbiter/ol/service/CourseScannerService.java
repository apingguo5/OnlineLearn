package com.rabbiter.ol.service;

import com.rabbiter.ol.tool.PathUtils;

import java.io.File;
import java.util.*;

/**
 * 扫描项目根目录下的 courses/ 文件夹，自动识别课程层次结构
 * 目录结构约定：
 *   courses/
 *     课程名称/          -> 课程
 *       章节名称/        -> 章节
 *         资源文件       -> 资源（ppt, mp4, doc, pdf 等）
 *
 * 增强：支持多级子目录，子目录名称会作为资源分类标记（如 "PPT"、"视频"、"代码"）
 */
public class CourseScannerService {

    private String coursesBasePath;

    public CourseScannerService() {
        // 计算 courses 目录的绝对路径
        // 项目根目录 = PathUtils.getClassLoadRootPath() 得到的路径再向上找
        String rootPath = PathUtils.getClassLoadRootPath();
        File rootDir = new File(rootPath);

        // 多种策略定位项目根目录
        // 1. 如果当前目录名是 backend，取父目录
        if (rootDir.getName().equals("backend")) {
            rootDir = rootDir.getParentFile();
        }
        // 2. 如果当前目录下直接有 courses 子目录，直接用
        File coursesUnderRoot = new File(rootDir, "courses");
        if (coursesUnderRoot.exists() && coursesUnderRoot.isDirectory()) {
            this.coursesBasePath = coursesUnderRoot.getAbsolutePath();
        } else {
            // 3. 尝试从 classLoadRootPath 的父目录往上找 courses
            File parent = new File(rootPath);
            while (parent != null) {
                File candidate = new File(parent, "courses");
                if (candidate.exists() && candidate.isDirectory()) {
                    this.coursesBasePath = candidate.getAbsolutePath();
                    rootDir = parent;
                    break;
                }
                parent = parent.getParentFile();
            }
            if (this.coursesBasePath == null) {
                // 兜底：就用原始路径
                this.coursesBasePath = coursesUnderRoot.getAbsolutePath();
            }
        }
        System.out.println("[CourseScanner] courses base path: " + this.coursesBasePath);
    }

    /**
     * 获取 courses 目录的绝对路径
     */
    public String getCoursesBasePath() {
        return coursesBasePath;
    }

    /**
     * 扫描 courses 目录，返回完整的课程树形结构
     * 返回结构：
     * [{
     *   name: "课程名",
     *   path: "绝对路径",
     *   chapters: [{
     *     name: "章节名",
     *     path: "绝对路径",
     *     sortOrder: 1,
     *     resources: [{
     *       fileName: "文件名称",
     *       filePath: "绝对路径",
     *       fileType: "pdf",
     *       fileSize: 12345,
     *       sortOrder: 1,
     *       subDirectory: "PPT"   // 相对于章节的子目录名，用于分组
     *     }]
     *   }]
     * }]
     */
    public List<Map<String, Object>> scanAllCourses() {
        List<Map<String, Object>> courseList = new ArrayList<>();
        File coursesDir = new File(coursesBasePath);
        if (!coursesDir.exists() || !coursesDir.isDirectory()) {
            System.err.println("[CourseScanner] courses directory not found: " + coursesBasePath);
            return courseList;
        }

        File[] courseDirs = coursesDir.listFiles(File::isDirectory);
        if (courseDirs == null) return courseList;

        // 按名称排序
        Arrays.sort(courseDirs, Comparator.comparing(File::getName));

        for (File courseDir : courseDirs) {
            // 跳过隐藏目录
            if (courseDir.getName().startsWith(".")) continue;

            Map<String, Object> course = new LinkedHashMap<>();
            course.put("name", courseDir.getName());
            course.put("path", getRelativePath(courseDir));

            List<Map<String, Object>> chapters = new ArrayList<>();
            File[] chapterDirs = courseDir.listFiles(File::isDirectory);
            if (chapterDirs != null) {
                Arrays.sort(chapterDirs, Comparator.comparing(File::getName));
                int chapterIndex = 1;
                for (File chapterDir : chapterDirs) {
                    // 跳过隐藏目录
                    if (chapterDir.getName().startsWith(".")) continue;

                    Map<String, Object> chapter = new LinkedHashMap<>();
                    chapter.put("name", chapterDir.getName());
                    chapter.put("path", getRelativePath(chapterDir));
                    chapter.put("sortOrder", chapterIndex++);

                    // 递归扫描章节目录下所有文件（包括子目录中的文件）
                    List<Map<String, Object>> resources = new ArrayList<>();
                    List<File> allFiles = new ArrayList<>();
                    collectAllFiles(chapterDir, allFiles);
                    Collections.sort(allFiles, Comparator.comparing(File::getName));
                    int resourceIndex = 1;
                    for (File file : allFiles) {
                        Map<String, Object> resource = new LinkedHashMap<>();
                        resource.put("fileName", file.getName());
                        resource.put("filePath", getRelativePath(file));
                        resource.put("fileType", getExtension(file.getName()));
                        resource.put("fileSize", file.length());
                        resource.put("sortOrder", resourceIndex++);
                        // 添加所在子目录信息（相对于章节目录），方便前端分组展示
                        String subDir = getSubDirectory(chapterDir, file);
                        resource.put("subDirectory", subDir);
                        resources.add(resource);
                    }
                    chapter.put("resources", resources);
                    chapters.add(chapter);
                }
            }
            course.put("chapters", chapters);

            // 统计信息
            int totalResources = 0;
            for (Map<String, Object> ch : chapters) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> res = (List<Map<String, Object>>) ch.get("resources");
                totalResources += res != null ? res.size() : 0;
            }
            course.put("totalChapters", chapters.size());
            course.put("totalResources", totalResources);

            courseList.add(course);
        }
        return courseList;
    }

    /**
     * 递归收集目录下的所有文件（不包含目录本身）
     */
    private void collectAllFiles(File dir, List<File> result) {
        File[] entries = dir.listFiles();
        if (entries == null) return;
        for (File entry : entries) {
            if (entry.isFile()) {
                result.add(entry);
            } else if (entry.isDirectory()) {
                collectAllFiles(entry, result);
            }
        }
    }

    /**
     * 获取文件相对于章节目录的子目录路径
     * 例如章节/chapterDir/PPT/xxx.pptx → "PPT"
     * 如果文件直接在章节目录下 → ""
     */
    private String getSubDirectory(File chapterDir, File file) {
        String chapterPath = chapterDir.getAbsolutePath().replace("\\", "/");
        String filePath = file.getAbsolutePath().replace("\\", "/");
        if (filePath.startsWith(chapterPath)) {
            String relative = filePath.substring(chapterPath.length());
            if (relative.startsWith("/")) relative = relative.substring(1);
            int slashIdx = relative.lastIndexOf('/');
            if (slashIdx > 0) {
                return relative.substring(0, slashIdx);
            }
        }
        return "";
    }

    /**
     * 获取课程的层级信息（课程名、章节数、资源数）
     */
    public List<Map<String, Object>> getCourseOverview() {
        List<Map<String, Object>> overview = new ArrayList<>();
        List<Map<String, Object>> allCourses = scanAllCourses();
        for (Map<String, Object> course : allCourses) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", course.get("name"));
            item.put("path", course.get("path"));
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> chapters = (List<Map<String, Object>>) course.get("chapters");
            item.put("chapterCount", chapters != null ? chapters.size() : 0);
            int totalResources = 0;
            if (chapters != null) {
                for (Map<String, Object> ch : chapters) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> resources = (List<Map<String, Object>>) ch.get("resources");
                    totalResources += resources != null ? resources.size() : 0;
                }
            }
            item.put("resourceCount", totalResources);
            overview.add(item);
        }
        return overview;
    }

    // ---- 辅助方法 ----

    private String getRelativePath(File file) {
        String absPath = file.getAbsolutePath();
        // 将路径中的反斜杠统一为正斜杠
        return absPath.replace("\\", "/");
    }

    private String getExtension(String fileName) {
        int idx = fileName.lastIndexOf('.');
        if (idx > 0 && idx < fileName.length() - 1) {
            return fileName.substring(idx + 1).toLowerCase();
        }
        return "";
    }
}