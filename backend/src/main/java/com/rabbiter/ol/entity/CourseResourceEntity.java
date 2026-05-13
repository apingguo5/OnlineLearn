package com.rabbiter.ol.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

/**
 * 课程资源表 - 统一管理所有课程资源 (视频、文档等)
 */
@TableName("course_resource")
public class CourseResourceEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 所属课程ID */
    private Integer courseId;

    /** 资源名称 */
    private String resourceName;

    /** 资源类型（1: 视频, 2: PDF文档, 3: PPT, 4: 习题集, 5: 其他） */
    private Integer resourceType;

    /** 文件访问URL（对象存储路径/本地路径） */
    private String fileUrl;

    /** 存储桶名称 */
    private String storageBucket;

    /** 对象键（文件路径） */
    private String objectKey;

    /** 文件哈希值（用于去重、校验） */
    private String fileHash;

    /** 文件大小（字节） */
    private Long fileSize;

    /** 视频/音频时长（秒） */
    private Integer duration;

    /** 所属章节ID */
    private Integer chapterId;

    /** 同一章节内的排序序号 */
    private Integer sortOrder;

    /** 上传者ID */
    private Integer uploaderId;

    /** 是否公开（0: 仅班级学生, 1: 公开） */
    private Integer isPublic;

    /** 上传时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getCourseId() { return courseId; }
    public void setCourseId(Integer courseId) { this.courseId = courseId; }

    public String getResourceName() { return resourceName; }
    public void setResourceName(String resourceName) { this.resourceName = resourceName; }

    public Integer getResourceType() { return resourceType; }
    public void setResourceType(Integer resourceType) { this.resourceType = resourceType; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public String getStorageBucket() { return storageBucket; }
    public void setStorageBucket(String storageBucket) { this.storageBucket = storageBucket; }

    public String getObjectKey() { return objectKey; }
    public void setObjectKey(String objectKey) { this.objectKey = objectKey; }

    public String getFileHash() { return fileHash; }
    public void setFileHash(String fileHash) { this.fileHash = fileHash; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }

    public Integer getChapterId() { return chapterId; }
    public void setChapterId(Integer chapterId) { this.chapterId = chapterId; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public Integer getUploaderId() { return uploaderId; }
    public void setUploaderId(Integer uploaderId) { this.uploaderId = uploaderId; }

    public Integer getIsPublic() { return isPublic; }
    public void setIsPublic(Integer isPublic) { this.isPublic = isPublic; }

    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }

    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
}