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
	@TableField("class_name")
	private String className;
	/**
	 * 班级负责人
	 */
	@TableField("user_id")
	private Integer userId;
	/**
	 * 课程ID (对应 course 表)
	 */
	@TableField("course_id")
	private Integer courseId;
	/**
	 * 学年 (例如: 2025-2026)
	 */
	@TableField("academic_year")
	private String academicYear;
	/**
	 * 学期 (1: 春季, 2: 秋季)
	 */
	@TableField("semester")
	private Integer semester;
	/**
	 * 班级最大学生数
	 */
	@TableField("max_students")
	private Integer maxStudents;
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

	public String getAcademicYear() {
		return academicYear;
	}

	public void setAcademicYear(String academicYear) {
		this.academicYear = academicYear;
	}

	public Integer getSemester() {
		return semester;
	}

	public void setSemester(Integer semester) {
		this.semester = semester;
	}

	public Integer getMaxStudents() {
		return maxStudents;
	}

	public void setMaxStudents(Integer maxStudents) {
		this.maxStudents = maxStudents;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
}
