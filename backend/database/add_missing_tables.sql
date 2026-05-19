-- ============================================================================
-- 补全缺失的表：作业管理相关表 (homework, user_do_homework)
-- 执行方式: mysql -u root -p online_learn < add_missing_tables.sql
-- ============================================================================

-- ============================================================================
-- 1. 作业表 (`homework`)
--    教师发布的作业，关联班级，学生需作答提交
-- ============================================================================
DROP TABLE IF EXISTS `homework`;
CREATE TABLE `homework` (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '作业ID',
  `title` varchar(255) NOT NULL COMMENT '作业标题',
  `content` text COMMENT '作业题目内容（支持富文本）',
  `answer` text COMMENT '参考答案',
  `creator` int UNSIGNED DEFAULT NULL COMMENT '创建教师ID',
  `class_id` int UNSIGNED DEFAULT NULL COMMENT '目标班级ID',
  `commit_time` datetime DEFAULT NULL COMMENT '提交截止时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_creator` (`creator`),
  KEY `idx_class_id` (`class_id`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_homework_creator` FOREIGN KEY (`creator`) REFERENCES `user` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_homework_class` FOREIGN KEY (`class_id`) REFERENCES `class` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='作业表';

-- ============================================================================
-- 2. 作业作答表 (`user_do_homework`)
--    学生提交的作业答案，支持批改评分
-- ============================================================================
DROP TABLE IF EXISTS `user_do_homework`;
CREATE TABLE `user_do_homework` (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '作答记录ID',
  `user_id` int UNSIGNED NOT NULL COMMENT '学生ID',
  `homework_id` int UNSIGNED NOT NULL COMMENT '作业ID',
  `reply` text COMMENT '学生作答内容',
  `completion_time` datetime DEFAULT NULL COMMENT '完成/提交时间',
  `mode` varchar(20) DEFAULT NULL COMMENT '批改模式（1-已批改）',
  `score` decimal(5,1) DEFAULT NULL COMMENT '得分',
  `remark` varchar(500) DEFAULT NULL COMMENT '教师评语',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_homework` (`user_id`, `homework_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_homework_id` (`homework_id`),
  KEY `idx_completion_time` (`completion_time`),
  CONSTRAINT `fk_udh_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_udh_homework` FOREIGN KEY (`homework_id`) REFERENCES `homework` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='作业作答表（学生提交的作业答案）';
