-- 创建数据库，并指定字符集
CREATE DATABASE IF NOT EXISTS `online_learn` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `online_learn`;

-- 1. 角色表 (`role`)
DROP TABLE IF EXISTS `role`;
CREATE TABLE `role` (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '角色id',
  `role_name` varchar(255) DEFAULT NULL COMMENT '角色名',
  `description` varchar(255) DEFAULT NULL COMMENT '角色描述',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- 2. 用户表 (`user`)
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

-- 3. 用户角色关联表 (`user_role`)
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

-- 4. 课程表 (`course`) - 核心表 (已简化，仅保留必要字段)
DROP TABLE IF EXISTS `course`;
CREATE TABLE `course` (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '课程ID',
  `course_name` varchar(255) NOT NULL COMMENT '课程名称',
  `description` text COMMENT '课程描述',
  `creator_id` int UNSIGNED DEFAULT NULL COMMENT '课程创建者',
  `cover_url` varchar(500) DEFAULT NULL COMMENT '课程封面图URL',
  `status` tinyint UNSIGNED NOT NULL DEFAULT 1 COMMENT '课程状态（1: 启用, 2: 停用, 3: 草稿）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_course_name` (`course_name`),
  KEY `idx_creator_id` (`creator_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_status_create_time` (`status`, `create_time`),
  CONSTRAINT `fk_course_creator` FOREIGN KEY (`creator_id`) REFERENCES `user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程表';

-- 5. 班级表 (`class`)
DROP TABLE IF EXISTS `class`;
CREATE TABLE `class` (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '班级ID',
  `class_name` varchar(255) DEFAULT NULL COMMENT '班级名称',
  `course_id` int UNSIGNED NOT NULL COMMENT '所属课程ID',
  `user_id` int UNSIGNED DEFAULT NULL COMMENT '班级负责人',
  `academic_year` varchar(20) DEFAULT NULL COMMENT '学年',
  `semester` tinyint UNSIGNED DEFAULT NULL COMMENT '学期（1: 春季, 2: 秋季）',
  `max_students` int UNSIGNED DEFAULT NULL COMMENT '班级最大学生数',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '班级创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_class_name` (`class_name`),
  KEY `idx_course_id` (`course_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_academic_year` (`academic_year`),
  CONSTRAINT `fk_class_course` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_class_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='班级表';

-- 6. 用户班级关联表 (`user_class`)
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

-- 7. 入班申请表 (`applicant`)
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

-- 8. 课程章节表 (`course_chapter`) - 用于组织资源
DROP TABLE IF EXISTS `course_chapter`;
CREATE TABLE `course_chapter` (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '章节ID',
  `course_id` int UNSIGNED NOT NULL COMMENT '所属课程ID',
  `chapter_name` varchar(255) NOT NULL COMMENT '章节名称',
  `parent_id` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '父章节ID（0表示根章节）',
  `sort_order` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '章节排序序号',
  `creator_id` int UNSIGNED DEFAULT NULL COMMENT '创建者ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_course_id` (`course_id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_sort_order` (`sort_order`),
  KEY `idx_creator_id` (`creator_id`),
  CONSTRAINT `fk_chapter_course` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_chapter_creator` FOREIGN KEY (`creator_id`) REFERENCES `user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程章节表';

-- 9. 课程资源表 (`course_resource`) - 统一管理所有课程资源 (视频、文档等)
DROP TABLE IF EXISTS `course_resource`;
CREATE TABLE `course_resource` (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '资源ID',
  `course_id` int UNSIGNED NOT NULL COMMENT '所属课程ID',
  `resource_name` varchar(255) NOT NULL COMMENT '资源名称',
  `resource_type` tinyint UNSIGNED NOT NULL COMMENT '资源类型（1: 视频, 2: PDF文档, 3: PPT, 4: 习题集, 5: 其他）',
  `file_url` varchar(500) DEFAULT NULL COMMENT '文件访问URL（对象存储路径）',
  `storage_bucket` varchar(255) DEFAULT NULL COMMENT '存储桶名称',
  `object_key` varchar(500) DEFAULT NULL COMMENT '对象键（文件路径）',
  `file_hash` varchar(64) DEFAULT NULL COMMENT '文件哈希值（用于去重、校验）',
  `file_size` bigint UNSIGNED DEFAULT NULL COMMENT '文件大小（字节）',
  `duration` int UNSIGNED DEFAULT NULL COMMENT '视频/音频时长（秒）',
  `chapter_id` int UNSIGNED DEFAULT NULL COMMENT '所属章节ID',
  `sort_order` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '同一章节内的排序序号',
  `uploader_id` int UNSIGNED DEFAULT NULL COMMENT '上传者ID',
  `is_public` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否公开（0: 仅班级学生, 1: 公开）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  PRIMARY KEY (`id`),
  KEY `idx_course_id` (`course_id`),
  KEY `idx_resource_type` (`resource_type`),
  KEY `idx_chapter_id` (`chapter_id`),
  KEY `idx_sort_order` (`sort_order`),
  KEY `idx_chapter_sort` (`chapter_id`, `sort_order`),
  KEY `idx_uploader_id` (`uploader_id`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_resource_course` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_resource_chapter` FOREIGN KEY (`chapter_id`) REFERENCES `course_chapter` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_resource_uploader` FOREIGN KEY (`uploader_id`) REFERENCES `user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程资源表';

-- 10. 问答表 (`ask_questions`) - 修改：`video_id` 关联至 `course_resource.id`
DROP TABLE IF EXISTS `ask_questions`;
CREATE TABLE `ask_questions` (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '问题ID',
  `sender` int UNSIGNED DEFAULT NULL COMMENT '发送人ID',
  `content` text COMMENT '内容',
  `recipient` int UNSIGNED DEFAULT NULL COMMENT '接收人ID',
  `resource_id` int UNSIGNED DEFAULT NULL COMMENT '课程资源ID', -- 字段名和注释已更新
  `reply` text COMMENT '回复内容',
  `status` tinyint UNSIGNED NOT NULL DEFAULT 2 COMMENT '状态（1：已回复；2：未回复）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '时间',
  PRIMARY KEY (`id`),
  KEY `idx_sender` (`sender`),
  KEY `idx_recipient` (`recipient`),
  KEY `idx_resource_id` (`resource_id`), -- 索引名已更新
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_ask_questions_recipient` FOREIGN KEY (`recipient`) REFERENCES `user` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_ask_questions_sender` FOREIGN KEY (`sender`) REFERENCES `user` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_ask_questions_resource` FOREIGN KEY (`resource_id`) REFERENCES `course_resource` (`id`) ON DELETE SET NULL -- 外键已更新
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问答表';

-- 11. 练习题库表 (`exercises`)
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

-- 12. 作业表 (`homework`)
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

-- 13. 知识点表 (`knowledge_point`)
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

-- 14. 用户练习记录表 (`user_do_exercise`)
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

-- 15. 用户作业记录表 (`user_do_homework`)
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

-- 16. 视频观看记录表 (`video_watch_record`) - 修改：`video_id` 关联至 `course_resource.id`
DROP TABLE IF EXISTS `video_watch_record`;
CREATE TABLE `video_watch_record` (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` int UNSIGNED NOT NULL COMMENT '用户ID',
  `resource_id` int UNSIGNED NOT NULL COMMENT '课程资源ID', -- 字段名和注释已更新
  `watch_seconds` int NOT NULL DEFAULT 0 COMMENT '已观看秒数',
  `total_seconds` int DEFAULT NULL COMMENT '视频总时长(秒)',
  `is_finished` tinyint UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否看完(0-未看完,1-已看完)',
  `last_watch_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后观看时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '首次观看时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_user_video` (`user_id`, `resource_id`), -- 索引名语义已更新
  KEY `idx_resource_id` (`resource_id`), -- 索引名已更新
  KEY `idx_is_finished` (`is_finished`),
  KEY `idx_last_watch_time` (`last_watch_time`),
  CONSTRAINT `fk_watch_record_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_watch_record_resource` FOREIGN KEY (`resource_id`) REFERENCES `course_resource` (`id`) ON DELETE CASCADE -- 外键已更新
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='资源观看记录表';