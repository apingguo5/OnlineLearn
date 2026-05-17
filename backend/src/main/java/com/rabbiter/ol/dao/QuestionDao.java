package com.rabbiter.ol.dao;

import com.rabbiter.ol.vo.QuestionVo;
import com.rabbiter.ol.entity.QuestionEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.HashMap;
import java.util.List;

@Mapper
public interface QuestionDao extends BaseMapper<QuestionEntity> {

    Integer queryCount(QuestionVo questionVo);

    List<HashMap<String, Object>> queryData(QuestionVo questionVo);
}