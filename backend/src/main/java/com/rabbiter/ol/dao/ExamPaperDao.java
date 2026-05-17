package com.rabbiter.ol.dao;

import com.rabbiter.ol.vo.ExamPaperVo;
import com.rabbiter.ol.entity.ExamPaperEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.HashMap;
import java.util.List;

@Mapper
public interface ExamPaperDao extends BaseMapper<ExamPaperEntity> {

    Integer queryCount(ExamPaperVo examPaperVo);

    List<HashMap<String, Object>> queryData(ExamPaperVo examPaperVo);
}