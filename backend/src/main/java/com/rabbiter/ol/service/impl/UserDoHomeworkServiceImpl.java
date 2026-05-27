package com.rabbiter.ol.service.impl;

import com.rabbiter.ol.dao.UserDoHomeworkDao;
import com.rabbiter.ol.entity.UserDoHomeworkEntity;
import com.rabbiter.ol.vo.UserDoHomeworkVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.rabbiter.ol.service.UserDoHomeworkService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;


@Service("userDoHomeworkService")
public class UserDoHomeworkServiceImpl extends ServiceImpl<UserDoHomeworkDao, UserDoHomeworkEntity> implements UserDoHomeworkService {

    @Autowired
    private UserDoHomeworkDao userDoHomeworkDao;

    @Override
    public Map<String, Object> queryPage(UserDoHomeworkVo userDoHomeworkVo) {
        Integer total = userDoHomeworkDao.queryCount(userDoHomeworkVo);
        List<HashMap> data = userDoHomeworkDao.queryData(userDoHomeworkVo);
        Map<String, Object> result = new HashMap<>();
        result.put("total",total);
        result.put("data",data);
        return result;
    }

    @Override
    public Boolean updateModeByUserId(String userId, String homeworkId,String score,String remark) {
        return userDoHomeworkDao.updateModeByUserId(userId,homeworkId,score,remark);
    }

    @Override
    public List<HashMap> queryByHomeworkId(Integer homeworkId) {
        List<HashMap> list = userDoHomeworkDao.queryByHomeworkId(homeworkId);
        for (HashMap record : list) {
            String reply = (String) record.get("reply");
            String answer = (String) record.get("answer");
            Double autoScore = autoGradeHomework(reply, answer);
            record.put("autoScore", autoScore);
        }
        return list;
    }

    @Override
    public Boolean updateGrade(Integer recordId, String mode, String score, String remark) {
        return userDoHomeworkDao.updateGrade(recordId, mode, score, remark);
    }

    @Override
    public Double autoGradeHomework(String studentReply, String referenceAnswer) {
        if (studentReply == null || referenceAnswer == null) {
            return null;
        }
        String reply = studentReply.trim().replaceAll("\\s+", " ");
        String answer = referenceAnswer.trim().replaceAll("\\s+", " ");
        if (reply.isEmpty() || answer.isEmpty()) {
            return null;
        }
        if (reply.equalsIgnoreCase(answer)) {
            return 100.0;
        }
        String[] keywords = answer.split("[，,;；、\\s]+");
        if (keywords.length > 1) {
            int matchCount = 0;
            for (String keyword : keywords) {
                String kw = keyword.trim();
                if (!kw.isEmpty() && Pattern.compile(Pattern.quote(kw), Pattern.CASE_INSENSITIVE).matcher(reply).find()) {
                    matchCount++;
                }
            }
            double ratio = (double) matchCount / keywords.length;
            if (ratio > 0) {
                return Math.round(ratio * 100.0 * 10.0) / 10.0;
            }
        }
        if (reply.contains(answer) || answer.contains(reply)) {
            return 100.0;
        }
        return null;
    }
}