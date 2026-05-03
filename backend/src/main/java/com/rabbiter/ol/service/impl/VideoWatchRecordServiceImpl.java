package com.rabbiter.ol.service.impl;

import com.rabbiter.ol.dao.VideoWatchRecordDao;
import com.rabbiter.ol.entity.VideoWatchRecordEntity;
import com.rabbiter.ol.service.VideoWatchRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;


@Service("videoWatchRecordService")
public class VideoWatchRecordServiceImpl extends ServiceImpl<VideoWatchRecordDao, VideoWatchRecordEntity> implements VideoWatchRecordService {

    @Autowired
    private VideoWatchRecordDao videoWatchRecordDao;

    @Override
    public void saveWatchTime(Integer userId, Integer videoId, Integer videoTotalId, Integer watchTime) {
        QueryWrapper<VideoWatchRecordEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("video_id", videoId);
        
        VideoWatchRecordEntity record = this.getOne(queryWrapper);
        
        if (record == null) {
            record = new VideoWatchRecordEntity();
            record.setUserId(userId);
            record.setVideoId(videoId);
            record.setVideoTotalId(videoTotalId);
            record.setWatchTime(watchTime);
            record.setLastWatchTime(new Date());
            this.save(record);
        } else {
            record.setWatchTime(record.getWatchTime() + watchTime);
            record.setLastWatchTime(new Date());
            this.updateById(record);
        }
    }

    @Override
    public List<HashMap> queryWatchTimeByUserId(Integer userId) {
        return videoWatchRecordDao.queryWatchTimeByUserId(userId);
    }

    @Override
    public List<HashMap> queryWatchTimeByVideoTotalId(Integer videoTotalId) {
        return videoWatchRecordDao.queryWatchTimeByVideoTotalId(videoTotalId);
    }

    @Override
    public List<HashMap> queryWatchTimeByClassId(Integer classId) {
        return videoWatchRecordDao.queryWatchTimeByClassId(classId);
    }

    @Override
    public HashMap getTotalWatchTimeByUserId(Integer userId) {
        QueryWrapper<VideoWatchRecordEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        
        List<VideoWatchRecordEntity> records = this.list(queryWrapper);
        
        int totalSeconds = 0;
        for (VideoWatchRecordEntity record : records) {
            totalSeconds += record.getWatchTime();
        }
        
        HashMap<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("totalWatchTime", totalSeconds);
        result.put("totalWatchTimeMinutes", totalSeconds / 60);
        
        return result;
    }
}