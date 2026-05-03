package com.rabbiter.ol.service;

import com.baomidou.mybatisplus.extension.service.IService;

import com.rabbiter.ol.entity.VideoWatchRecordEntity;

import java.util.HashMap;
import java.util.List;

/**
 * 视频观看记录
 *
 * @author 
 * @email ${email}
 * @date 2026-03-19 21:19:00
 */
public interface VideoWatchRecordService extends IService<VideoWatchRecordEntity> {

    void saveWatchTime(Integer userId, Integer videoId, Integer videoTotalId, Integer watchTime);

    List<HashMap> queryWatchTimeByUserId(Integer userId);

    List<HashMap> queryWatchTimeByVideoTotalId(Integer videoTotalId);

    List<HashMap> queryWatchTimeByClassId(Integer classId);

    HashMap getTotalWatchTimeByUserId(Integer userId);
}