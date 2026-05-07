-- =====================================================
-- Migration v2: 教师工作台增强 - 课程章节内容管理
-- =====================================================

-- 0. subject 表新增字段：创建人ID（教师创建的课程）
ALTER TABLE `subject`
ADD COLUMN `user_id` int UNSIGNED DEFAULT NULL COMMENT '创建人ID(教师)' AFTER `subject_name`,
ADD INDEX `idx_user_id` (`user_id`);

-- 1. class 表新增字段：科目ID、学习时长
ALTER TABLE `class`
ADD COLUMN `subject_id` int UNSIGNED DEFAULT NULL COMMENT '科目ID' AFTER `user_id`,
ADD COLUMN `study_duration` int DEFAULT NULL COMMENT '学习时长(小时)' AFTER `subject_id`,
ADD INDEX `idx_subject_id` (`subject_id`);
-- 如果已有外键约束则不需要再添加（根据实际情况）
-- ALTER TABLE `class` ADD CONSTRAINT `fk_class_subject` FOREIGN KEY (`subject_id`) REFERENCES `subject` (`id`) ON DELETE SET NULL;

-- 2. 课程章节表
DROP TABLE IF EXISTS `course_chapter`;
CREATE TABLE `course_chapter` (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '章节ID',
  `class_id` int UNSIGNED NOT NULL COMMENT '班级ID',
  `chapter_name` varchar(255) NOT NULL COMMENT '章节名称',
  `sort_order` int UNSIGNED DEFAULT 0 COMMENT '排序',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_class_id` (`class_id`),
  KEY `idx_sort_order` (`class_id`, `sort_order`),
  CONSTRAINT `fk_chapter_class` FOREIGN KEY (`class_id`) REFERENCES `class` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程章节表';

-- 3. 章节内容表（视频/文字阅读内容）
DROP TABLE IF EXISTS `chapter_content`;
CREATE TABLE `chapter_content` (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '内容ID',
  `chapter_id` int UNSIGNED NOT NULL COMMENT '章节ID',
  `content_type` tinyint UNSIGNED NOT NULL DEFAULT 1 COMMENT '内容类型(1-视频课程,2-文字阅读)',
  `content_title` varchar(255) NOT NULL COMMENT '内容标题',
  `ref_id` int UNSIGNED DEFAULT NULL COMMENT '关联ID(视频id或知识点id)',
  `sort_order` int UNSIGNED DEFAULT 0 COMMENT '排序',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_chapter_id` (`chapter_id`),
  KEY `idx_sort_order` (`chapter_id`, `sort_order`),
  CONSTRAINT `fk_content_chapter` FOREIGN KEY (`chapter_id`) REFERENCES `course_chapter` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='章节内容表';