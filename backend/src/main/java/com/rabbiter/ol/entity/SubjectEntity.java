package com.rabbiter.ol.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

/**
 * 课程表 (course)
 * 
 */
@TableName("course")
public class SubjectEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 课程ID
	 */
	@TableId(type = IdType.AUTO)
	private Integer id;
	/**
	 * 课程名称
	 */
	@TableField("course_name")
	private String courseName;
	/**
	 * 课程描述
	 */
	@TableField("description")
	private String description;
	/**
	 * 课程创建者
	 */
	@TableField("creator_id")
	private Integer creatorId;
	/**
	 * 课程封面图URL
	 */
	@TableField("cover_url")
	private String coverUrl;
	/**
	 * 课程状态（1: 启用, 2: 停用, 3: 草稿）
	 */
	@TableField("status")
	private Integer status;
	/**
	 * 创建时间
	 */
	@TableField("create_time")
	private Date createTime;
	/**
	 * 最后更新时间
	 */
	@TableField("update_time")
	private Date updateTime;

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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getCreatorId() {
		return creatorId;
	}

	public void setCreatorId(Integer creatorId) {
		this.creatorId = creatorId;
	}

	public String getCoverUrl() {
		return coverUrl;
	}

	public void setCoverUrl(String coverUrl) {
		this.coverUrl = coverUrl;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
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