package com.rabbiter.ol.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rabbiter.ol.entity.CourseResourceEntity;

import java.util.List;
import java.util.Map;

public interface CourseResourceService extends IService<CourseResourceEntity> {

    /**
     * 根据章节ID查询资源列表
     */
    List<CourseResourceEntity> listByChapterId(Integer chapterId);

    /**
     * 根据课程ID查询资源列表
     */
    List<CourseResourceEntity> listByCourseId(Integer courseId);

    /**
     * 添加本地路径资源（将本地文件路径注册到course_resource表）
     * @param chapterId 章节ID
     * @param resourceName 资源名称
     * @param localPath 本地文件路径
     * @param resourceType 资源类型 (1:视频, 5:其他)
     * @param uploaderId 上传者ID（可选）
     * @return 新增的资源实体
     */
    CourseResourceEntity addLocalResource(Integer chapterId, String resourceName, String localPath, Integer resourceType, Integer uploaderId);

    /**
     * 删除资源
     */
    void deleteResource(Integer resourceId);

    /**
     * 更新资源
     */
    void updateResource(CourseResourceEntity resource);

    /**
     * 批量删除资源
     */
    void batchDeleteResources(List<Integer> ids);

    /**
     * 重新排序
     */
    void sortResources(List<Integer> ids);

    /**
     * 查询目录资源的同名文件是否已存在
     */
    boolean existsByFilePath(String filePath);

    /**
     * 获取章节对应的课程ID（通过class_id中间表关联）
     */
    Integer getCourseIdByChapterId(Integer chapterId);

    /**
     * 扫描并导入本地资源
     * 扫描课程文件系统目录中的文件，将尚未导入course_resource表的文件导入
     * @param courseId 课程ID
     * @param chapterId 章节ID
     * @param localBasePath 本地扫描基础路径
     * @param uploaderId 上传者ID
     * @return 导入的资源数量
     */
    int scanAndImportLocalResources(Integer courseId, Integer chapterId, String localBasePath, Integer uploaderId);

    /**
     * 查询资源（支持按条件过滤）
     */
    List<CourseResourceEntity> searchResources(Integer courseId, Integer chapterId, Integer resourceType);
}