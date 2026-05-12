-- =========================================================================
-- 数据库迁移脚本: 修复 course_chapter 表结构 + 创建 chapter_content 表
-- 迁移原因: 现有 course_chapter 表基于旧版设计，与新扩展的实体不匹配
-- =========================================================================

-- 1. 修复 course_chapter 表: 添加缺失列，重命名不匹配列
ALTER TABLE `course_chapter`
  -- 添加缺失的列（允许 NULL，有默认值，避免影响已有数据）
  ADD COLUMN `chapter_type` varchar(50) DEFAULT NULL COMMENT '章节类型: video/quiz/reading' AFTER `chapter_name`,
  ADD COLUMN `description` text DEFAULT NULL COMMENT '章节简介' AFTER `chapter_type`,
  ADD COLUMN `publish_status` int UNSIGNED DEFAULT 0 COMMENT '发布状态: 0未配置 1已配置 2已发布' AFTER `parent_id`,
  ADD COLUMN `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `create_time`;

-- 2. 重命名 course_id -> class_id（章节是按班级组织的）
ALTER TABLE `course_chapter` CHANGE COLUMN `course_id` `class_id` int UNSIGNED NOT NULL COMMENT '所属班级ID';

-- 3. 添加索引
ALTER TABLE `course_chapter`
  ADD INDEX `idx_class_id` (`class_id`),
  ADD INDEX `idx_parent_id` (`parent_id`),
  ADD INDEX `idx_publish_status` (`publish_status`);

-- 4. 创建 chapter_content 表（存储章节关联的视频/阅读等资源）
DROP TABLE IF EXISTS `chapter_content`;
CREATE TABLE `chapter_content` (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '内容ID',
  `chapter_id` int UNSIGNED NOT NULL COMMENT '所属章节ID',
  `content_type` tinyint UNSIGNED NOT NULL DEFAULT 1 COMMENT '内容类型: 1=视频 2=阅读材料',
  `content_title` varchar(255) DEFAULT NULL COMMENT '内容标题（自动解析）',
  `ref_id` int UNSIGNED DEFAULT NULL COMMENT '关联资源ID（videos/knowledgepoint表）',
  `sort_order` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '排序序号',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_chapter_id` (`chapter_id`),
  KEY `idx_content_type` (`content_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='章节内容关联表';