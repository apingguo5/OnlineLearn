package com.rabbiter.ol.dao;

import com.rabbiter.ol.vo.StudentAnswerRecordVo;
import com.rabbiter.ol.entity.StudentAnswerRecordEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.List;

@Mapper
public interface StudentAnswerRecordDao extends BaseMapper<StudentAnswerRecordEntity> {

    Integer queryCount(StudentAnswerRecordVo studentAnswerRecordVo);

    List<HashMap<String, Object>> queryData(StudentAnswerRecordVo studentAnswerRecordVo);

    List<HashMap<String, Object>> getPaperStudentList(Integer paperId);

    List<HashMap<String, Object>> getStudentDetail(@Param("paperId") Integer paperId, @Param("studentId") Integer studentId);
}
