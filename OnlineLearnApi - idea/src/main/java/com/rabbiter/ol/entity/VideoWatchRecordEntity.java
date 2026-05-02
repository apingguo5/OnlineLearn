package com.rabbiter.ol.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;


/**
 * 视频观看记录
 * 
 * @author 
 * @email ${email}
 * @date 2026-03-19 21:19:00
 */

@TableName("video_watch_record")
public class VideoWatchRecordEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 主键ID
	 */
	@TableId(type = IdType.AUTO)
	private Integer id;
	/**
	 * 用户ID
	 */
	private Integer userId;
	/**
	 * 视频ID
	 */
	private Integer videoId;
	/**
	 * 视频总集ID
	 */
	private Integer videoTotalId;
	/**
	 * 观看时长（秒）
	 */
	private Integer watchTime;
	/**
	 * 最后观看时间
	 */
	private Date lastWatchTime;
	/**
	 * 创建时间
	 */
	private Date createTime;
	/**
	 * 更新时间
	 */
	private Date updateTime;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public Integer getVideoId() {
		return videoId;
	}

	public void setVideoId(Integer videoId) {
		this.videoId = videoId;
	}

	public Integer getVideoTotalId() {
		return videoTotalId;
	}

	public void setVideoTotalId(Integer videoTotalId) {
		this.videoTotalId = videoTotalId;
	}

	public Integer getWatchTime() {
		return watchTime;
	}

	public void setWatchTime(Integer watchTime) {
		this.watchTime = watchTime;
	}

	public Date getLastWatchTime() {
		return lastWatchTime;
	}

	public void setLastWatchTime(Date lastWatchTime) {
		this.lastWatchTime = lastWatchTime;
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