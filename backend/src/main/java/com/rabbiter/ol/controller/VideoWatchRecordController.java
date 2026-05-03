package com.rabbiter.ol.controller;

import com.rabbiter.ol.common.Result;
import com.rabbiter.ol.service.VideoWatchRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

/**
 * 视频观看记录
 * 
 * @author 
 * @email ${email}
 * @date 2026-03-19 21:19:00
 */
@RestController
@RequestMapping("study/videoWatchRecord")
@CrossOrigin
public class VideoWatchRecordController {

    @Autowired
    private VideoWatchRecordService videoWatchRecordService;

    /**
     * 保存观看时长
     */
    @RequestMapping("/saveWatchTime")
    public Result saveWatchTime(@RequestBody VideoWatchRecordDto dto) {
        videoWatchRecordService.saveWatchTime(dto.getUserId(), dto.getVideoId(), dto.getVideoTotalId(), dto.getWatchTime());
        return Result.successCode();
    }

    /**
     * 查询用户观看时长
     */
    @RequestMapping("/queryByUserId")
    public Result queryByUserId(@RequestParam Integer userId) {
        List<HashMap> watchTimes = videoWatchRecordService.queryWatchTimeByUserId(userId);
        return Result.success(watchTimes);
    }

    /**
     * 查询用户总观看时长
     */
    @RequestMapping("/queryTotalByUserId")
    public Result queryTotalByUserId(@RequestParam Integer userId) {
        HashMap totalWatchTime = videoWatchRecordService.getTotalWatchTimeByUserId(userId);
        return Result.success(totalWatchTime);
    }

    /**
     * 查询视频总集观看时长
     */
    @RequestMapping("/queryByVideoTotalId")
    public Result queryByVideoTotalId(@RequestParam Integer videoTotalId) {
        List<HashMap> watchTimes = videoWatchRecordService.queryWatchTimeByVideoTotalId(videoTotalId);
        return Result.success(watchTimes);
    }

    /**
     * 查询班级观看时长
     */
    @RequestMapping("/queryByClassId")
    public Result queryByClassId(@RequestParam Integer classId) {
        List<HashMap> watchTimes = videoWatchRecordService.queryWatchTimeByClassId(classId);
        return Result.success(watchTimes);
    }
}