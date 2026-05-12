-- 学生端课程列表重构所需的数据库变更
-- 为class表添加课程起止时间，用于学生端展示课程进度

ALTER TABLE `class` 
  ADD COLUMN `start_time` datetime DEFAULT NULL COMMENT '课程开始时间' AFTER `semester`,
  ADD COLUMN `end_time` datetime DEFAULT NULL COMMENT '课程结束时间' AFTER `start_time`;