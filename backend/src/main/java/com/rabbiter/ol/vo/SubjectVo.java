package com.rabbiter.ol.vo;

import java.io.Serializable;

/**
 * 课程 (course表) VO
 * 
 * @author 
 * @email ${email}
 * @date 2024-02-15 21:39:15
 */
public class SubjectVo {

	/**
	 * 课程ID
	 */
	private Integer id;
	/**
	 * 课程名称
	 */
	private String courseName;
	/**
	 * 当前页
	 */
	private Integer page;

	/**
	 * 每页条数
	 */
	private Integer pageSize;

	/**
	 * 班级ID
	 */
	private Integer classId;

	/**
	 * 教师用户ID
	 */
	private Integer userId;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getCourseName() {
		return courseName;
	}

	public void setCourseName(String courseName) {
		this.courseName = courseName;
	}

	public Integer getPage() {
		return page;
	}

	public void setPage(Integer page) {
		this.page = page;
	}

	public Integer getPageSize() {
		return pageSize;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
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
}