package com.rabbiter.ol.controller;

public class VideoWatchRecordDto {
    private Integer userId;
    private Integer videoId;
    private Integer videoTotalId;
    private Integer watchTime;

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
}
