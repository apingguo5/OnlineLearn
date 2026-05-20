package com.rabbiter.ol.controller;

import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.List;


import com.rabbiter.ol.common.Result;
import com.rabbiter.ol.entity.UserDoHomeworkEntity;
import com.rabbiter.ol.service.UserDoHomeworkService;
import com.rabbiter.ol.vo.UserDoHomeworkVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author 
 * @email ${email}
 * @date 2024-02-12 00:24:20
 */
@RestController
@RequestMapping("study/userdohomework")
@CrossOrigin
public class UserDoHomeworkController {
    @Autowired
    private UserDoHomeworkService userDoHomeworkService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    public Result list(@RequestBody UserDoHomeworkVo userDoHomeworkVo) {
        userDoHomeworkVo.setPage((userDoHomeworkVo.getPage() - 1) * userDoHomeworkVo.getPageSize());
        Map<String, Object> page = userDoHomeworkService.queryPage(userDoHomeworkVo);
        return Result.success(page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public Result info(@PathVariable("id") Integer id) {
        UserDoHomeworkEntity userDoHomework = userDoHomeworkService.getById(id);

        return Result.success(userDoHomework);
    }

    /**
     * 保存（支持学生再次提交）
     */
    @RequestMapping("/save")
    public Result save(@RequestBody UserDoHomeworkVo userDoHomeworkVo) {
        // 检查是否已经提交过
        Map<String, Object> params = new HashMap<>();
        params.put("homework_id", userDoHomeworkVo.getHomeworkId());
        params.put("user_id", userDoHomeworkVo.getUserId());
        List<UserDoHomeworkEntity> list = userDoHomeworkService.listByMap(params);
        
        if (list != null && list.size() > 0) {
            // 已提交过，更新
            UserDoHomeworkEntity existing = list.get(0);
            existing.setReply(userDoHomeworkVo.getContent());
            existing.setMode("0"); // 重置为待批改状态
            existing.setCompletionTime(new Date());
            existing.setScore(null); // 清空前次得分
            existing.setRemark(null); // 清空前次评语
            boolean b = userDoHomeworkService.updateById(existing);
            if (b){
                return Result.successCode();
            }
        } else {
            // 首次提交
            UserDoHomeworkEntity userDoHomeworkEntity = new UserDoHomeworkEntity();
            userDoHomeworkEntity.setHomeworkId(userDoHomeworkVo.getHomeworkId());
            userDoHomeworkEntity.setReply(userDoHomeworkVo.getContent());
            userDoHomeworkEntity.setUserId(userDoHomeworkVo.getUserId());
            userDoHomeworkEntity.setMode("0");
            userDoHomeworkEntity.setCompletionTime(new Date());
            boolean save = userDoHomeworkService.save(userDoHomeworkEntity);
            if (save){
                return Result.successCode();
            }
        }
        return Result.failureCode();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public Result update(@RequestBody UserDoHomeworkEntity userDoHomework) {
        boolean b = userDoHomeworkService.updateById(userDoHomework);
        if (b){
            return Result.successCode();
        }
        return Result.failureCode();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public Result delete(@RequestBody Integer[] ids) {
        userDoHomeworkService.removeByIds(Arrays.asList(ids));

        return Result.successCode();
    }

    /**
     * 教师端：按作业查询提交列表
     */
    @RequestMapping("/byHomework")
    public Result byHomework(@RequestBody Map<String, Object> params) {
        Integer homeworkId = Integer.valueOf(params.get("homeworkId").toString());
        List<HashMap> list = userDoHomeworkService.queryByHomeworkId(homeworkId);
        return Result.success(list);
    }

    /**
     * 教师端：批改/打回作业
     * mode: 1=已批改通过, 2=已打回
     */
    @RequestMapping("/grade")
    public Result grade(@RequestBody Map<String, Object> params) {
        Integer recordId = Integer.valueOf(params.get("recordId").toString());
        String mode = params.get("mode").toString();
        String score = params.containsKey("score") ? params.get("score").toString() : "0";
        String remark = params.containsKey("remark") ? params.get("remark").toString() : "";
        userDoHomeworkService.updateGrade(recordId, mode, score, remark);
        return Result.successCode();
    }

}
