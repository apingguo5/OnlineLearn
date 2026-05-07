package com.rabbiter.ol.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 学生成绩
 */
@TableName("student_grade")
public class StudentGradeEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer classId;

    private Integer userId;

    private BigDecimal videoScore;

    private BigDecimal homeworkScore;

    private BigDecimal examScore;

    private BigDecimal totalScore;

    private Date createTime;

    private Date updateTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getClassId() {
        return classId;
    }

    public void setClassId(Integer classId) {
        this.classId = classId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public BigDecimal getVideoScore() {
        return videoScore;
    }

    public void setVideoScore(BigDecimal videoScore) {
        this.videoScore = videoScore;
    }

    public BigDecimal getHomeworkScore() {
        return homeworkScore;
    }

    public void setHomeworkScore(BigDecimal homeworkScore) {
        this.homeworkScore = homeworkScore;
    }

    public BigDecimal getExamScore() {
        return examScore;
    }

    public void setExamScore(BigDecimal examScore) {
        this.examScore = examScore;
    }

    public BigDecimal getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(BigDecimal totalScore) {
        this.totalScore = totalScore;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}