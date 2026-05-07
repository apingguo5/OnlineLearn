package com.rabbiter.ol.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;


/**
 * 
 * 
 * @author 
 * @email ${email}
 * @date 2024-02-12 00:22:45
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
	 * 科目ID
	 */
	private Integer subjectId;
	/**
	 * 学习时长(小时)
	 */
	private Integer studyDuration;

	public Integer getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(Integer subjectId) {
		this.subjectId = subjectId;
	}

	public Integer getStudyDuration() {
		return studyDuration;
	}

	public void setStudyDuration(Integer studyDuration) {
		this.studyDuration = studyDuration;
	}

	/**
	 * 班级创建时间
	 */
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

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
}
