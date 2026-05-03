-- 创建数据库，并指定字符集
CREATE DATABASE IF NOT EXISTS `online_learn` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `online_learn`;

-- 1. 角色表
DROP TABLE IF EXISTS `role`;
CREATE TABLE `role` (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '角色id',
  `role_name` varchar(255) DEFAULT NULL COMMENT '角色名',
  `description` varchar(255) DEFAULT NULL COMMENT '角色描述',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- 2. 用户表 (优化：主键UNSIGNED, 时间默认值, 账号注释区分, phone长度优化)
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户主键',
  `account` varchar(255) DEFAULT '' COMMENT '登录账号',
  `password` varchar(255) NOT NULL COMMENT '用户密码（必须使用BCrypt等强哈希算法加密后存储，切勿明文，无默认值）',
  `user_name` varchar(255) DEFAULT NULL COMMENT '用户昵称/真实姓名',
  `phone` varchar(20) DEFAULT NULL COMMENT '电话号码',
  `sex` tinyint UNSIGNED DEFAULT NULL COMMENT '性别（0：男；1：女）',
  `description` varchar(255) DEFAULT NULL COMMENT '个人描述',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_account` (`account`) COMMENT '登录账号唯一'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 3. 用户角色关联表
DROP TABLE IF EXISTS `user_role`;
CREATE TABLE `user_role` (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户角色中间表ID',
  `user_id` int UNSIGNED NOT NULL COMMENT '用户ID',
  `role_id` int UNSIGNED NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
  KEY `idx_role_id` (`role_id`),
  CONSTRAINT `fk_user_role_role` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_user_role_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- 4. 科目表
DROP TABLE IF EXISTS `subject`;
CREATE TABLE `subject` (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '科目ID',
  `subject_name` varchar(255) DEFAULT NULL COMMENT '科目名称',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='科目表';

-- 5. 班级表
DROP TABLE IF EXISTS `class`;
CREATE TABLE `class` (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '班级ID',
  `class_name` varchar(255) DEFAULT NULL COMMENT '班级名称',
  `user_id` int UNSIGNED DEFAULT NULL COMMENT '班级负责人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '班级创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  CONSTRAINT `fk_class_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='班级表';

-- 6. 用户班级关联表
DROP TABLE IF EXISTS `user_class`;
CREATE TABLE `user_class` (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户与班级中间表id',
  `user_id` int UNSIGNED NOT NULL COMMENT '用户id',
  `class_id` int UNSIGNED NOT NULL COMMENT '班级id',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_class` (`user_id`, `class_id`),
  KEY `idx_class_id` (`class_id`),
  CONSTRAINT `fk_user_class_class` FOREIGN KEY (`class_id`) REFERENCES `class` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_user_class_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户班级关联表';

-- 7. 用户科目关联表
DROP TABLE IF EXISTS `user_subject`;
CREATE TABLE `user_subject` (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '科目与用户中间表id',
  `user_id` int UNSIGNED NOT NULL COMMENT '用户id',
  `subject_id` int UNSIGNED NOT NULL COMMENT '科目id',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_subject` (`user_id`, `subject_id`),
  KEY `idx_subject_id` (`subject_id`),
  CONSTRAINT `fk_user_subject_subject` FOREIGN KEY (`subject_id`) REFERENCES `subject` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_user_subject_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户科目关联表';

-- 8. 入班申请表
DROP TABLE IF EXISTS `applicant`;
CREATE TABLE `applicant` (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '学生申请加入班级表',
  `user_id` int UNSIGNED NOT NULL COMMENT '学生id',
  `class_id` int UNSIGNED NOT NULL COMMENT '班级id',
  `status` tinyint UNSIGNED DEFAULT 1 COMMENT '状态（1：待审核；2：成功；3：失败）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_class_applying` (`user_id`, `class_id`) COMMENT '同一班级同一用户只能有一条待审核申请',
  KEY `idx_class_id` (`class_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_applicant_class` FOREIGN KEY (`class_id`) REFERENCES `class` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_applicant_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='入班申请表';

-- 9. 视频总集表
DROP TABLE IF EXISTS `video_total`;
CREATE TABLE `video_total` (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '视频总体id',
  `topic` varchar(255) DEFAULT NULL COMMENT '主题名称',
  `cover_url` varchar(255) DEFAULT NULL COMMENT '封面路径',
  `user_id` int UNSIGNED DEFAULT NULL COMMENT '上传人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_video_total_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='视频总集表';

-- 10. 视频分集表 (优化：sort字段改为UNSIGNED)
DROP TABLE IF EXISTS `videos`;
CREATE TABLE `videos` (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '视频分集id',
  `topic` varchar(255) DEFAULT NULL COMMENT '视频名称',
  `video_total_id` int UNSIGNED DEFAULT NULL COMMENT '视频总集ID',
  `video_url` varchar(255) DEFAULT NULL COMMENT '视频访问路径',
  `path` varchar(255) DEFAULT NULL COMMENT '视频存储路径',
  `sort` int UNSIGNED DEFAULT NULL COMMENT '排序（第几集）',
  PRIMARY KEY (`id`),
  KEY `idx_video_total_id` (`video_total_id`),
  KEY `idx_sort` (`video_total_id`, `sort`),
  CONSTRAINT `fk_videos_video_total` FOREIGN KEY (`video_total_id`) REFERENCES `video_total` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='视频分集表';

-- 11. 问答表
DROP TABLE IF EXISTS `ask_questions`;
CREATE TABLE `ask_questions` (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '问题ID',
  `sender` int UNSIGNED DEFAULT NULL COMMENT '发送人ID',
  `content` text COMMENT '内容',
  `recipient` int UNSIGNED DEFAULT NULL COMMENT '接收人ID',
  `video_id` int UNSIGNED DEFAULT NULL COMMENT '视频ID',
  `reply` text COMMENT '回复内容',
  `status` tinyint UNSIGNED NOT NULL DEFAULT 2 COMMENT '状态（1：已回复；2：未回复）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '时间',
  PRIMARY KEY (`id`),
  KEY `idx_sender` (`sender`),
  KEY `idx_recipient` (`recipient`),
  KEY `idx_video_id` (`video_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_ask_questions_recipient` FOREIGN KEY (`recipient`) REFERENCES `user` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_ask_questions_sender` FOREIGN KEY (`sender`) REFERENCES `user` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_ask_questions_video` FOREIGN KEY (`video_id`) REFERENCES `videos` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问答表';

-- 12. 练习题库表
DROP TABLE IF EXISTS `exercises`;
CREATE TABLE `exercises` (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '练习题ID',
  `title` varchar(255) DEFAULT NULL COMMENT '标题',
  `content` text COMMENT '题目',
  `answer` text COMMENT '参考答案',
  `creator` int UNSIGNED DEFAULT NULL COMMENT '创建人',
  `class_id` int UNSIGNED DEFAULT NULL COMMENT '所属班级ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_creator` (`creator`),
  KEY `idx_class_id` (`class_id`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_exercises_class` FOREIGN KEY (`class_id`) REFERENCES `class` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_exercises_creator` FOREIGN KEY (`creator`) REFERENCES `user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='练习题库表';

-- 13. 作业表
DROP TABLE IF EXISTS `homework`;
CREATE TABLE `homework` (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '作业ID',
  `title` varchar(255) DEFAULT NULL COMMENT '标题',
  `content` text COMMENT '题目',
  `answer` text COMMENT '参考答案',
  `creator` int UNSIGNED DEFAULT NULL COMMENT '创建人',
  `class_id` int UNSIGNED DEFAULT NULL COMMENT '所属班级ID',
  `commit_time` date DEFAULT NULL COMMENT '提交截止时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_creator` (`creator`),
  KEY `idx_class_id` (`class_id`),
  KEY `idx_commit_time` (`commit_time`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_homework_class` FOREIGN KEY (`class_id`) REFERENCES `class` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_homework_creator` FOREIGN KEY (`creator`) REFERENCES `user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='作业表';

-- 14. 知识点表
DROP TABLE IF EXISTS `knowledge_point`;
CREATE TABLE `knowledge_point` (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '知识点ID',
  `title` varchar(255) DEFAULT NULL COMMENT '知识点标题',
  `content` text COMMENT '知识点内容',
  `creator` int UNSIGNED DEFAULT NULL COMMENT '创建人',
  `class_id` int UNSIGNED DEFAULT NULL COMMENT '所属班级ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_creator` (`creator`),
  KEY `idx_class_id` (`class_id`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_knowledge_point_class` FOREIGN KEY (`class_id`) REFERENCES `class` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_knowledge_point_creator` FOREIGN KEY (`creator`) REFERENCES `user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识点表';

-- 15. 用户练习记录表 (优化：score字段改为DECIMAL)
DROP TABLE IF EXISTS `user_do_exercise`;
CREATE TABLE `user_do_exercise` (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '练习题回答ID',
  `user_id` int UNSIGNED DEFAULT NULL COMMENT '回答人ID',
  `exercise_id` int UNSIGNED DEFAULT NULL COMMENT '练习题ID',
  `reply` text COMMENT '答案',
  `score` decimal(5,2) DEFAULT NULL COMMENT '分数(支持小数, 如 99.5)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_exercise` (`user_id`, `exercise_id`),
  KEY `idx_exercise_id` (`exercise_id`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_user_do_exercise_exercise` FOREIGN KEY (`exercise_id`) REFERENCES `exercises` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_user_do_exercise_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户练习记录表';

-- 16. 用户作业记录表 (优化：mode重命名、score改为DECIMAL、增加update_time)
DROP TABLE IF EXISTS `user_do_homework`;
CREATE TABLE `user_do_homework` (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '作业回答ID',
  `user_id` int UNSIGNED DEFAULT NULL COMMENT '回答人ID',
  `homework_id` int UNSIGNED DEFAULT NULL COMMENT '作业ID',
  `reply` text COMMENT '答案',
  `completion_time` datetime DEFAULT NULL COMMENT '完成时间',
  `submit_mode` tinyint UNSIGNED DEFAULT 0 COMMENT '提交模式（0-正常,1-补交）',
  `score` decimal(5,2) DEFAULT 0.00 COMMENT '得分(支持小数)',
  `remark` varchar(255) DEFAULT NULL COMMENT '评语',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间(批改/修改时自动更新)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_homework` (`user_id`, `homework_id`),
  KEY `idx_homework_id` (`homework_id`),
  KEY `idx_completion_time` (`completion_time`),
  CONSTRAINT `fk_user_do_homework_homework` FOREIGN KEY (`homework_id`) REFERENCES `homework` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_user_do_homework_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户作业记录表';

-- 17. 视频观看记录表
DROP TABLE IF EXISTS `video_watch_record`;
CREATE TABLE `video_watch_record` (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` int UNSIGNED NOT NULL COMMENT '用户ID',
  `video_id` int UNSIGNED NOT NULL COMMENT '视频分集ID',
  `watch_seconds` int NOT NULL DEFAULT 0 COMMENT '已观看秒数',
  `total_seconds` int DEFAULT NULL COMMENT '视频总时长(秒)',
  `is_finished` tinyint UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否看完(0-未看完,1-已看完)',
  `last_watch_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后观看时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '首次观看时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_user_video` (`user_id`, `video_id`),
  KEY `idx_video_id` (`video_id`),
  KEY `idx_is_finished` (`is_finished`),
  KEY `idx_last_watch_time` (`last_watch_time`),
  CONSTRAINT `fk_watch_record_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_watch_record_video` FOREIGN KEY (`video_id`) REFERENCES `videos` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='视频观看记录表';