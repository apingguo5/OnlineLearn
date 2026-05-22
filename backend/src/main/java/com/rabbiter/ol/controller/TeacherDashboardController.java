package com.rabbiter.ol.controller;

import com.rabbiter.ol.entity.SubjectEntity;
import com.rabbiter.ol.entity.CourseResourceEntity;
import com.rabbiter.ol.service.SubjectService;
import com.rabbiter.ol.service.CourseResourceService;
import com.rabbiter.ol.tool.FileUtil;
import com.rabbiter.ol.tool.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 教师端仪表盘接口
 * 基于 course 表（课程）
 */
@RestController
@RequestMapping("/study/teacher/dashboard")
public class TeacherDashboardController {

    private static final Logger log = LoggerFactory.getLogger(TeacherDashboardController.class);

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

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
     * 更新课程
     * POST /study/teacher/dashboard/updateSubject
     * @param params { id, courseName?, description?, coverUrl? }
     */
    @PostMapping("/updateSubject")
    public Map<String, Object> updateSubject(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            Integer id = Integer.valueOf(params.get("id").toString());

            SubjectEntity entity = subjectService.getById(id);
            if (entity == null) {
                result.put("code", 400);
                result.put("resultData", "课程不存在");
                return result;
            }

            String courseName = (String) params.get("courseName");
            if (courseName != null) {
                if (courseName.trim().isEmpty()) {
                    result.put("code", 400);
                    result.put("resultData", "课程名称不能为空");
                    return result;
                }
                entity.setCourseName(courseName.trim());
            }

            String description = (String) params.get("description");
            if (description != null) {
                entity.setDescription(description.trim());
            }

            String coverUrl = (String) params.get("coverUrl");
            if (coverUrl != null) {
                entity.setCoverUrl(coverUrl.trim());
            }

            entity.setUpdateTime(new Date());
            subjectService.updateById(entity);

            result.put("code", 200);
            result.put("resultData", entity);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("resultData", "更新课程失败: " + e.getMessage());
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

    // ======================== 课件资源管理 ========================

    @Autowired
    private CourseResourceService courseResourceService;

    /**
     * 上传课件资源文件
     * POST /study/teacher/dashboard/uploadFile
     * @param file 上传的文件
     * @param subjectId 课程ID
     */
    @PostMapping("/uploadFile")
    public Map<String, Object> uploadFile(@RequestParam("file") MultipartFile file,
                                          @RequestParam("subjectId") Integer subjectId) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (file.isEmpty()) {
                result.put("code", 400);
                result.put("resultData", "文件不能为空");
                return result;
            }

            long randomNum = System.currentTimeMillis();
            String originalFileName = file.getOriginalFilename();
            String fileName = randomNum + originalFileName;

            // 保存文件
            FileUtil.uploadFile(file.getBytes(),
                    PathUtils.getClassLoadRootPath() + "/file/resourceFile/",
                    fileName);

            // 保存资源记录
            CourseResourceEntity resource = new CourseResourceEntity();
            resource.setCourseId(subjectId);
            resource.setResourceName(originalFileName);
            resource.setFileUrl("/file/resourceFile/" + fileName);
            resource.setFileSize(file.getSize());
            resource.setResourceType(originalFileName.substring(originalFileName.lastIndexOf(".") + 1).hashCode());
            resource.setCreateTime(new Date());

            courseResourceService.save(resource);

            result.put("code", 200);
            result.put("resultData", resource);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("resultData", "上传文件失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 获取 resource 目录下的图片列表（用于课程封面选择）
     * GET /study/teacher/dashboard/listCoverImages
     * @return { code, resultData: [ { name, relativePath } ] }
     */
    @GetMapping("/listCoverImages")
    public Map<String, Object> listCoverImages() {
        Map<String, Object> result = new HashMap<>();
        try {
            String resourceDir = PathUtils.getClassLoadRootPath() + "/resource";
            File dir = new File(resourceDir);
            List<Map<String, Object>> images = new java.util.ArrayList<>();

            if (dir.exists() && dir.isDirectory()) {
                File[] files = dir.listFiles((d, name) -> {
                    String lower = name.toLowerCase();
                    return lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg")
                            || lower.endsWith(".gif") || lower.endsWith(".bmp") || lower.endsWith(".svg");
                });
                if (files != null) {
                    for (File f : files) {
                        Map<String, Object> item = new HashMap<>();
                        item.put("name", f.getName());
                        item.put("relativePath", "./" + f.getName());
                        images.add(item);
                    }
                }
            }

            result.put("code", 200);
            result.put("resultData", images);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("resultData", "获取图片列表失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 导入课程封面图片到 resource 目录，并直接更新 course 表的 cover_url
     * POST /study/teacher/dashboard/importCoverImage
     * @param file      上传的图片文件
     * @param courseId  课程ID（用于更新数据库 + 防文件名冲突）
     * @return { code, resultData: { relativePath, coverUrl, fileSize, written } }
     */
    @PostMapping("/importCoverImage")
    public Map<String, Object> importCoverImage(@RequestParam("file") MultipartFile file,
                                                @RequestParam("courseId") Integer courseId) {
        Map<String, Object> result = new HashMap<>();
        try {
            log.info("===== importCoverImage 被调用: courseId={}, fileName={}, size={} =====",
                    courseId, file.getOriginalFilename(), file.getSize());

            if (file.isEmpty()) {
                log.warn("importCoverImage: 文件为空");
                result.put("code", 400);
                result.put("resultData", "文件不能为空");
                return result;
            }

            String originalName = file.getOriginalFilename();
            String ext = "";
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf(".")).toLowerCase();
            }
            List<String> allowedExts = Arrays.asList(".png", ".jpg", ".jpeg", ".gif", ".bmp", ".svg", ".webp");
            if (!allowedExts.contains(ext)) {
                result.put("code", 400);
                result.put("resultData", "仅支持图片格式: " + String.join(", ", allowedExts));
                return result;
            }

            String fileName = courseId + "_" + System.currentTimeMillis() + ext;
            String rootPath = PathUtils.getClassLoadRootPath();
            String resourceDir = rootPath + "/resource/";
            String fullFilePath = resourceDir + fileName;

            log.info("importCoverImage: rootPath={}", rootPath);
            log.info("importCoverImage: 目标目录={}", resourceDir);
            log.info("importCoverImage: 目标文件={}", fullFilePath);

            FileUtil.uploadFile(file.getBytes(), resourceDir, fileName);

            File writtenFile = new File(fullFilePath);
            boolean fileExists = writtenFile.exists();
            log.info("importCoverImage: 文件写入后验证 - exists={}, size={}",
                    fileExists, fileExists ? writtenFile.length() : -1);

            String relativePath = "./" + fileName;

            // 使用 JdbcTemplate 直接执行 SQL（最可靠的方式，绕过 MyBatis Plus）
            int rows = jdbcTemplate.update(
                    "UPDATE course SET cover_url = ?, update_time = NOW() WHERE id = ?",
                    relativePath, courseId
            );
            log.info("importCoverImage: JdbcTemplate UPDATE 影响行数={}, courseId={}, coverUrl={}",
                    rows, courseId, relativePath);

            // 验证 DB 写入结果
            String dbCoverUrl = null;
            try {
                dbCoverUrl = jdbcTemplate.queryForObject(
                        "SELECT cover_url FROM course WHERE id = ?", String.class, courseId);
                log.info("importCoverImage: DB 中 cover_url 当前值={}", dbCoverUrl);
            } catch (Exception dbEx) {
                log.warn("importCoverImage: 查询验证 cover_url 失败: {}", dbEx.getMessage());
            }

            Map<String, Object> data = new HashMap<>();
            data.put("relativePath", relativePath);
            data.put("coverUrl", relativePath);
            data.put("fileName", fileName);
            data.put("fileSize", file.getSize());
            data.put("fileWritten", fileExists);
            data.put("dbRows", rows);
            result.put("code", 200);
            result.put("resultData", data);
            log.info("===== importCoverImage 完成: code=200 =====");
        } catch (Exception e) {
            log.error("importCoverImage 致命错误: courseId={}, error={}", courseId, e.getMessage(), e);
            result.put("code", 500);
            result.put("resultData", "封面上传失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 获取课程资源列表
     * POST /study/teacher/dashboard/getResources
     * @param params { subjectId }
     */
    @PostMapping("/getResources")
    public Map<String, Object> getResources(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            Integer subjectId = Integer.valueOf(params.get("subjectId").toString());
            List<CourseResourceEntity> list = courseResourceService.lambdaQuery()
                    .eq(CourseResourceEntity::getCourseId, subjectId)
                    .orderByDesc(CourseResourceEntity::getId)
                    .list();

            result.put("code", 200);
            result.put("resultData", list);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("resultData", "获取资源列表失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 删除资源
     * POST /study/teacher/dashboard/deleteResource
     * @param params { id }
     */
    @PostMapping("/deleteResource")
    public Map<String, Object> deleteResource(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            Integer id = Integer.valueOf(params.get("id").toString());
            CourseResourceEntity resource = courseResourceService.getById(id);
            if (resource != null) {
                // 删除物理文件
                FileUtil.deleteFile(PathUtils.getClassLoadRootPath() + resource.getFileUrl());
                // 删除数据库记录
                courseResourceService.removeById(id);
            }

            result.put("code", 200);
            result.put("resultData", "删除成功");
        } catch (Exception e) {
            result.put("code", 500);
            result.put("resultData", "删除资源失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 获取教师管理的班级列表（用于教师仪表盘的"班级概况"）
     * POST /study/teacher/dashboard/myClasses
     * @param params { userId }
     * 返回结构：[ { id, className, courseId, courseName, studentCount, status } ]
     */
    @PostMapping("/myClasses")
    public Map<String, Object> myClasses(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            Object userIdObj = params == null ? null : params.get("userId");
            if (userIdObj == null) {
                result.put("code", 400);
                result.put("resultData", "缺少 userId 参数");
                return result;
            }
            Integer userId = Integer.valueOf(userIdObj.toString());

            // 同时返回：教师作为负责人的班级 + 教师创建的课程下的所有班级
            // 通过 LEFT JOIN course 取课程名，通过子查询统计班级人数
            String sql =
                    "SELECT c.id AS id, " +
                    "       c.class_name AS className, " +
                    "       c.course_id AS courseId, " +
                    "       co.course_name AS courseName, " +
                    "       (SELECT COUNT(*) FROM user_class uc WHERE uc.class_id = c.id) AS studentCount " +
                    "  FROM class c " +
                    "  LEFT JOIN course co ON co.id = c.course_id " +
                    " WHERE c.user_id = ? OR co.creator_id = ? " +
                    " ORDER BY c.id DESC";

            List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, userId, userId);
            for (Map<String, Object> row : list) {
                row.put("status", "active");
            }

            log.info("myClasses: userId={}, 返回班级数={}", userId, list.size());
            result.put("code", 200);
            result.put("resultData", list);
        } catch (Exception e) {
            log.error("myClasses 失败: {}", e.getMessage(), e);
            result.put("code", 500);
            result.put("resultData", "获取班级列表失败: " + e.getMessage());
        }
        return result;
    }
}
