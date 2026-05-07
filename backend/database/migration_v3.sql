-- =====================================================
-- Migration v3: 教师端6大核心功能模块 - 数据库架构增强
-- =====================================================

-- =====================================================
-- 1. subject 表扩展: 课程基础信息字段
-- =====================================================
ALTER TABLE `subject`
  ADD COLUMN IF NOT EXISTS `cover` varchar(500) DEFAULT NULL COMMENT '课程封面URL' AFTER `user_id`,
  ADD COLUMN IF NOT EXISTS `description` text COMMENT '课程简介' AFTER `cover`,
  ADD COLUMN IF NOT EXISTS `goal` text COMMENT '教学目标' AFTER `description`;

-- =====================================================
-- 2. 课件资源表 (课件库 - 支持视频/PDF/PPT/外部链接)
-- =====================================================
DROP TABLE IF EXISTS `course_resource`;
CREATE TABLE `course_resource` (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '资源ID',
  `subject_id` int UNSIGNED NOT NULL COMMENT '所属课程ID',
  `resource_name` varchar(255) NOT NULL COMMENT '资源名称',
  `resource_type` tinyint UNSIGNED NOT NULL COMMENT '资源类型(1-视频,2-PDF,3-PPT,4-外部链接,5-其他)',
  `file_url` varchar(500) DEFAULT NULL COMMENT '文件URL或外部链接',
  `file_size` bigint UNSIGNED DEFAULT 0 COMMENT '文件大小(字节)',
  `description` text COMMENT '资源描述',
  `creator` int UNSIGNED DEFAULT NULL COMMENT '上传人ID(教师)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_subject_id` (`subject_id`),
  KEY `idx_resource_type` (`resource_type`),
  KEY `idx_creator` (`creator`),
  CONSTRAINT `fk_resource_subject` FOREIGN KEY (`subject_id`) REFERENCES `subject` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课件资源表';

-- =====================================================
-- 3. 章节-资源关联表 (支持将同一素材复用到不同章节)
-- =====================================================
DROP TABLE IF EXISTS `chapter_resource_ref`;
CREATE TABLE `chapter_resource_ref` (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  `chapter_id` int UNSIGNED NOT NULL COMMENT '章节ID',
  `resource_id` int UNSIGNED NOT NULL COMMENT '资源ID',
  `sort_order` int UNSIGNED DEFAULT 0 COMMENT '排序',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_chapter_resource` (`chapter_id`, `resource_id`),
  KEY `idx_chapter_id` (`chapter_id`),
  KEY `idx_resource_id` (`resource_id`),
  CONSTRAINT `fk_ref_chapter` FOREIGN KEY (`chapter_id`) REFERENCES `course_chapter` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_ref_resource` FOREIGN KEY (`resource_id`) REFERENCES `course_resource` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='章节-资源关联表';

-- =====================================================
-- 4. 成绩权重设置表
-- =====================================================
DROP TABLE IF EXISTS `grade_weight`;
CREATE TABLE `grade_weight` (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '权重ID',
  `class_id` int UNSIGNED NOT NULL COMMENT '班级ID',
  `weight_name` varchar(100) NOT NULL COMMENT '指标名称(如:视频观看,作业,期末考试)',
  `weight_percent` decimal(5,2) NOT NULL DEFAULT 0.00 COMMENT '权重百分比(如30.00表示30%)',
  `sort_order` int UNSIGNED DEFAULT 0 COMMENT '排序',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_class_id` (`class_id`),
  CONSTRAINT `fk_weight_class` FOREIGN KEY (`class_id`) REFERENCES `class` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='成绩权重设置表';

-- =====================================================
-- 5. 学生成绩表 (按权重计算后的综合得分)
-- =====================================================
DROP TABLE IF EXISTS `student_grade`;
CREATE TABLE `student_grade` (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '成绩ID',
  `class_id` int UNSIGNED NOT NULL COMMENT '班级ID',
  `user_id` int UNSIGNED NOT NULL COMMENT '学生ID',
  `video_score` decimal(10,2) DEFAULT 0.00 COMMENT '视频观看得分',
  `homework_score` decimal(10,2) DEFAULT 0.00 COMMENT '作业得分',
  `exam_score` decimal(10,2) DEFAULT 0.00 COMMENT '考试得分',
  `total_score` decimal(10,2) DEFAULT 0.00 COMMENT '综合得分(按权重计算)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_class_user` (`class_id`, `user_id`),
  KEY `idx_class_id` (`class_id`),
  KEY `idx_user_id` (`user_id`),
  CONSTRAINT `fk_grade_class` FOREIGN KEY (`class_id`) REFERENCES `class` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_grade_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生成绩表';

-- =====================================================
-- 6. 班级通知/公告表
-- =====================================================
DROP TABLE IF EXISTS `class_notice`;
CREATE TABLE `class_notice` (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `class_id` int UNSIGNED NOT NULL COMMENT '班级ID',
  `title` varchar(255) NOT NULL COMMENT '通知标题',
  `content` text COMMENT '通知内容',
  `notice_type` tinyint UNSIGNED DEFAULT 1 COMMENT '通知类型(1-开课提醒,2-作业催交,3-考试通知,4-系统消息,5-其他)',
  `sender_id` int UNSIGNED DEFAULT NULL COMMENT '发送人ID(教师)',
  `is_pinned` tinyint UNSIGNED DEFAULT 0 COMMENT '是否置顶(0-否,1-是)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_class_id` (`class_id`),
  KEY `idx_notice_type` (`notice_type`),
  KEY `idx_is_pinned` (`is_pinned`),
  CONSTRAINT `fk_notice_class` FOREIGN KEY (`class_id`) REFERENCES `class` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='班级通知/公告表';

-- =====================================================
-- 7. ask_questions 表扩展: 问答讨论区增强
-- =====================================================
ALTER TABLE `ask_questions`
  ADD COLUMN IF NOT EXISTS `is_pinned` tinyint UNSIGNED DEFAULT 0 COMMENT '是否置顶(0-否,1-是)' AFTER `status`,
  ADD COLUMN IF NOT EXISTS `chapter_id` int UNSIGNED DEFAULT NULL COMMENT '关联章节ID' AFTER `video_id`,
  ADD COLUMN IF NOT EXISTS `subject_id` int UNSIGNED DEFAULT NULL COMMENT '关联课程ID' AFTER `chapter_id`;

-- =====================================================
-- 8. class 表扩展: 开课/结课时间、邀请码
-- =====================================================
ALTER TABLE `class`
  ADD COLUMN IF NOT EXISTS `start_date` date DEFAULT NULL COMMENT '开课日期' AFTER `study_duration`,
  ADD COLUMN IF NOT EXISTS `end_date` date DEFAULT NULL COMMENT '结课日期' AFTER `start_date`,
  ADD COLUMN IF NOT EXISTS `invite_code` varchar(32) DEFAULT NULL COMMENT '班级邀请码' AFTER `end_date`,
  ADD COLUMN IF NOT EXISTS `class_cover` varchar(500) DEFAULT NULL COMMENT '班级封面' AFTER `invite_code`,
  ADD COLUMN IF NOT EXISTS `class_desc` text COMMENT '班级简介' AFTER `class_cover`,
  ADD UNIQUE KEY IF EXISTS `uk_invite_code` (`invite_code`);

-- =====================================================
-- 9. exercises 表扩展: 题目类型字段
-- =====================================================
ALTER TABLE `exercises`
  ADD COLUMN IF NOT EXISTS `exercise_type` tinyint UNSIGNED DEFAULT 1 COMMENT '题目类型(1-单选题,2-多选题,3-填空题,4-主观题)' AFTER `class_id`,
  ADD COLUMN IF NOT EXISTS `options` text COMMENT '选项(JSON格式,选择题用)' AFTER `exercise_type`,
  ADD COLUMN IF NOT EXISTS `subject_id` int UNSIGNED DEFAULT NULL COMMENT '所属课程ID' AFTER `options`,
  ADD KEY IF NOT EXISTS `idx_subject_id` (`subject_id`);

-- =====================================================
-- 10. homework 表扩展: 截止时间、防作弊设置
-- =====================================================
ALTER TABLE `homework`
  ADD COLUMN IF NOT EXISTS `deadline` datetime DEFAULT NULL COMMENT '截止时间' AFTER `class_id`,
  ADD COLUMN IF NOT EXISTS `homework_type` tinyint UNSIGNED DEFAULT 1 COMMENT '类型(1-作业,2-测验,3-考试)' AFTER `deadline`,
  ADD COLUMN IF NOT EXISTS `anti_cheat` tinyint UNSIGNED DEFAULT 0 COMMENT '防作弊(0-关闭,1-切屏限制)' AFTER `homework_type`,
  ADD COLUMN IF NOT EXISTS `total_score` decimal(10,2) DEFAULT 100.00 COMMENT '满分' AFTER `anti_cheat`;