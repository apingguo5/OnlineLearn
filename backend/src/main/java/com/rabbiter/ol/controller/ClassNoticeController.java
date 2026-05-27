package com.rabbiter.ol.controller;

import java.util.Date;
import java.util.Map;

import com.rabbiter.ol.common.Result;
import com.rabbiter.ol.entity.ClassNoticeEntity;
import com.rabbiter.ol.service.ClassNoticeService;
import com.rabbiter.ol.vo.ClassNoticeVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("study/classNotice")
@CrossOrigin
public class ClassNoticeController {

    @Autowired
    private ClassNoticeService classNoticeService;

    /**
     * 分页查询通知列表（教师端）
     */
    @RequestMapping("/list")
    public Result list(@RequestBody ClassNoticeVo classNoticeVo) {
        classNoticeVo.setPage((classNoticeVo.getPage() - 1) * classNoticeVo.getPageSize());
        Map<String, Object> page = classNoticeService.queryPage(classNoticeVo);
        return Result.success(page);
    }

    /**
     * 学生端：查询与我班级相关的通知
     */
    @RequestMapping("/studentList")
    public Result studentList(@RequestBody ClassNoticeVo classNoticeVo) {
        classNoticeVo.setPage((classNoticeVo.getPage() - 1) * classNoticeVo.getPageSize());
        Map<String, Object> page = classNoticeService.queryByStudentClasses(classNoticeVo);
        return Result.success(page);
    }

    /**
     * 发送/保存通知
     */
    @RequestMapping("/save")
    public Result save(@RequestBody ClassNoticeEntity classNoticeEntity) {
        classNoticeEntity.setCreateTime(new Date());
        boolean save = classNoticeService.save(classNoticeEntity);
        if (save) {
            return Result.successCode();
        }
        return Result.failureCode();
    }

    /**
     * 删除通知
     */
    @RequestMapping("/delete")
    public Result delete(@RequestBody ClassNoticeEntity classNoticeEntity) {
        boolean remove = classNoticeService.removeById(classNoticeEntity.getId());
        if (remove) {
            return Result.successCode();
        }
        return Result.failureCode();
    }
}
