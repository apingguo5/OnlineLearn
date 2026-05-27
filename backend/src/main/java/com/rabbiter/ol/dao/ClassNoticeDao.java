package com.rabbiter.ol.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rabbiter.ol.entity.ClassNoticeEntity;
import com.rabbiter.ol.vo.ClassNoticeVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.HashMap;
import java.util.List;

@Mapper
public interface ClassNoticeDao extends BaseMapper<ClassNoticeEntity> {

    Integer queryCount(ClassNoticeVo classNoticeVo);

    List<HashMap> queryData(ClassNoticeVo classNoticeVo);

    List<HashMap> queryByStudentClasses(ClassNoticeVo classNoticeVo);

    Integer countByStudentClasses(ClassNoticeVo classNoticeVo);
}
