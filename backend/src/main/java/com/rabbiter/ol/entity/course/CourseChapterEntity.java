package com.rabbiter.ol.entity.course;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

/**
 * 课程章节表
 * 数据库字段：id, course_id, chapter_name, chapter_type, description,
 *           parent_id, publish_status, sort_order, creator_id, create_time, update_time
 * 设计原则：章节直接挂在 course 下，一门课的多个班级共享同一份章节大纲
 */
@TableName("course_chapter")
public class CourseChapterEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 所属课程ID（真实数据库字段） */
    @TableField("course_id")
    private Integer courseId;

    /**
     * 历史字段保留：classId
     * 数据库无此列；setClassId 同步写到 courseId 以保留旧调用兼容性
     * （旧代码中"classId"参数实际期望是 courseId）
     */
    @TableField(exist = false)
    private Integer classId;

    private String chapterName;

    /** 章节类型: video/quiz/reading */
    private String chapterType;

    /** 章节简介 */
    private String description;

    /** 父章节ID（0表示根章节） */
    private Integer parentId;

    private Integer sortOrder;

    private Date createTime;

    /** 更新时间 */
    private Date updateTime;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getCourseId() { return courseId; }
    public void setCourseId(Integer courseId) { this.courseId = courseId; }

    public Integer getClassId() { return classId; }
    /**
     * 兼容旧代码：调用 setClassId 时同时把 courseId 也填上
     */
    public void setClassId(Integer classId) {
        this.classId = classId;
        if (this.courseId == null) {
            this.courseId = classId;
        }
    }

    public String getChapterName() { return chapterName; }
    public void setChapterName(String chapterName) { this.chapterName = chapterName; }

    public String getChapterType() { return chapterType; }
    public void setChapterType(String chapterType) { this.chapterType = chapterType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getParentId() { return parentId; }
    public void setParentId(Integer parentId) { this.parentId = parentId; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }

    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
}
