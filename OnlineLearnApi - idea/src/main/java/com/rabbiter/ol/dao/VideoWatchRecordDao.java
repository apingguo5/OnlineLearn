package com.rabbiter.ol.dao;

import com.rabbiter.ol.entity.VideoWatchRecordEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.HashMap;
import java.util.List;

/**
 * 视频观看记录
 * 
 * @author 
 * @email ${email}
 * @date 2026-03-19 21:19:00
 */
@Mapper
public interface VideoWatchRecordDao extends BaseMapper<VideoWatchRecordEntity> {

    List<HashMap> queryWatchTimeByUserId(Integer userId);

    List<HashMap> queryWatchTimeByVideoTotalId(Integer videoTotalId);

    List<HashMap> queryWatchTimeByClassId(Integer classId);
}