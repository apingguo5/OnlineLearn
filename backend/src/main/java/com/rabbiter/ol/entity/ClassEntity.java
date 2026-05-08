package com.rabbiter.ol.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;


/**
 * 班级表
 */
@TableName("class")
public class ClassEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 班级ID
	 */
	@TableId(type = IdType.AUTO)
	private Integer id;
	/**
	 * 班级名称
	 */
	private String className;
	/**
	 * 班级负责人
	 */
	private Integer userId;
	/**
	 * 课程ID (对应 course 表)
	 */
	@TableField("course_id")
	private Integer courseId;
	/**
	 * 学习时长(小时)
	 */
	private Integer studyDuration;

	/**
	 * 班级创建时间
	 */
	@TableField("create_time")
	private Date createTime;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public Integer getCourseId() {
		return courseId;
	}

	public void setCourseId(Integer courseId) {
		this.courseId = courseId;
	}

	public Integer getStudyDuration() {
		return studyDuration;
	}

	public void setStudyDuration(Integer studyDuration) {
		this.studyDuration = studyDuration;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
}