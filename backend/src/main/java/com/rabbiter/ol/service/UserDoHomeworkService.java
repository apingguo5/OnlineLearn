package com.rabbiter.ol.service;

import com.baomidou.mybatisplus.extension.service.IService;

import com.rabbiter.ol.entity.UserDoHomeworkEntity;
import com.rabbiter.ol.vo.UserDoHomeworkVo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 *
 * @author 
 * @email ${email}
 * @date 2024-02-12 00:24:20
 */
public interface UserDoHomeworkService extends IService<UserDoHomeworkEntity> {


    Map<String, Object> queryPage(UserDoHomeworkVo userDoHomeworkVo);

    Boolean updateModeByUserId(String userId, String homeworkId,String score,String remark);

    List<HashMap> queryByHomeworkId(Integer homeworkId);

    Boolean updateGrade(Integer recordId, String mode, String score, String remark);

    /**
     * 自动批改作业：将学生作答与参考答案进行匹配
     * @param studentReply 学生作答内容
     * @param referenceAnswer 参考答案
     * @return 自动批改得分（0-100），无法自动批改时返回 null
     */
    Double autoGradeHomework(String studentReply, String referenceAnswer);
}

