-- =============================================================================
-- 【方案修正版】将 course_chapter 表的 class_id 改为 course_id
-- 配合代码（entity / mapper）同步修改，实现"章节挂课程，所有班级共享"
-- =============================================================================
USE `online_learn`;

-- -----------------------------------------------------------------------------
-- 1. 修改 course_chapter 表结构：class_id -> course_id
-- -----------------------------------------------------------------------------
-- 先删除外键约束
SET @fk_name = (
  SELECT CONSTRAINT_NAME
  FROM information_schema.KEY_COLUMN_USAGE
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'course_chapter'
    AND COLUMN_NAME = 'class_id'
    AND REFERENCED_TABLE_NAME IS NOT NULL
  LIMIT 1
);
SET @drop_fk_sql = IF(@fk_name IS NOT NULL, CONCAT('ALTER TABLE `course_chapter` DROP FOREIGN KEY `', @fk_name, '`'), 'SELECT 1');
PREPARE stmt FROM @drop_fk_sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 删除索引（如果存在）
SET @idx_name = (
  SELECT INDEX_NAME
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'course_chapter'
    AND COLUMN_NAME = 'class_id'
  LIMIT 1
);
SET @drop_idx_sql = IF(@idx_name IS NOT NULL, CONCAT('ALTER TABLE `course_chapter` DROP INDEX `', @idx_name, '`'), 'SELECT 1');
PREPARE stmt FROM @drop_idx_sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 重命名列 class_id -> course_id
ALTER TABLE `course_chapter`
  CHANGE COLUMN `class_id` `course_id` INT UNSIGNED NOT NULL COMMENT '所属课程ID';

-- 新增索引与外键
ALTER TABLE `course_chapter`
  ADD INDEX `idx_course_id` (`course_id`),
  ADD CONSTRAINT `fk_chapter_course` FOREIGN KEY (`course_id`)
      REFERENCES `course` (`id`) ON DELETE CASCADE;

-- -----------------------------------------------------------------------------
-- 2. 清理之前错插入的脏数据
-- -----------------------------------------------------------------------------
-- 删除新创建的 id=11 课程及其级联数据
DELETE cr FROM `course_resource` cr
  INNER JOIN `course` c ON cr.course_id = c.id
  WHERE c.id = 11;
DELETE FROM `class` WHERE course_id = 11;
DELETE FROM `course` WHERE id = 11;

-- 删除孤立的 chapter_content（其 chapter_id 不存在）
DELETE FROM `chapter_content`
  WHERE chapter_id NOT IN (SELECT id FROM `course_chapter`);

-- -----------------------------------------------------------------------------
-- 3. 为现有"安卓开发基础"（id=3）插入完整章节结构
-- -----------------------------------------------------------------------------
SET @course_id = 3;

-- 清理可能已有的章节（防重）
DELETE cc FROM `chapter_content` cc
  INNER JOIN `course_chapter` ch ON cc.chapter_id = ch.id
  WHERE ch.course_id = @course_id;
DELETE cr FROM `course_resource` cr
  WHERE cr.course_id = @course_id
    AND cr.file_url LIKE '/courses/Android开发基础/%';
DELETE FROM `course_chapter` WHERE course_id = @course_id;

-- 第一章 ----------------------------------------------------------------------
INSERT INTO `course_chapter` (`course_id`, `chapter_name`, `parent_id`, `sort_order`, `creator_id`, `create_time`)
VALUES (@course_id, '第一章 课程导论', 0, 1, 1, NOW());
SET @ch1 = LAST_INSERT_ID();

INSERT INTO `course_chapter` (`course_id`, `chapter_name`, `parent_id`, `sort_order`, `creator_id`, `create_time`)
VALUES (@course_id, '1.1 课程目标与学习方法', @ch1, 1, 1, NOW());
SET @ch1_1 = LAST_INSERT_ID();

-- 第二章 ----------------------------------------------------------------------
INSERT INTO `course_chapter` (`course_id`, `chapter_name`, `parent_id`, `sort_order`, `creator_id`, `create_time`)
VALUES (@course_id, '第二章 你的第一个 Android 应用', 0, 2, 1, NOW());
SET @ch2 = LAST_INSERT_ID();

INSERT INTO `course_chapter` (`course_id`, `chapter_name`, `parent_id`, `sort_order`, `creator_id`, `create_time`)
VALUES (@course_id, '2.1 Kotlin 入门', @ch2, 1, 1, NOW());
SET @ch2_1 = LAST_INSERT_ID();
INSERT INTO `course_chapter` (`course_id`, `chapter_name`, `parent_id`, `sort_order`, `creator_id`, `create_time`)
VALUES (@course_id, '2.2 搭建 Android Studio', @ch2, 2, 1, NOW());
SET @ch2_2 = LAST_INSERT_ID();
INSERT INTO `course_chapter` (`course_id`, `chapter_name`, `parent_id`, `sort_order`, `creator_id`, `create_time`)
VALUES (@course_id, '2.3 构建基础布局', @ch2, 3, 1, NOW());
SET @ch2_3 = LAST_INSERT_ID();

-- 第三章 ----------------------------------------------------------------------
INSERT INTO `course_chapter` (`course_id`, `chapter_name`, `parent_id`, `sort_order`, `creator_id`, `create_time`)
VALUES (@course_id, '第三章 构建应用 UI', 0, 3, 1, NOW());
SET @ch3 = LAST_INSERT_ID();

INSERT INTO `course_chapter` (`course_id`, `chapter_name`, `parent_id`, `sort_order`, `creator_id`, `create_time`)
VALUES (@course_id, '3.1 Kotlin 进阶', @ch3, 1, 1, NOW());
SET @ch3_1 = LAST_INSERT_ID();
INSERT INTO `course_chapter` (`course_id`, `chapter_name`, `parent_id`, `sort_order`, `creator_id`, `create_time`)
VALUES (@course_id, '3.2 添加按钮与事件响应', @ch3, 2, 1, NOW());
SET @ch3_2 = LAST_INSERT_ID();
INSERT INTO `course_chapter` (`course_id`, `chapter_name`, `parent_id`, `sort_order`, `creator_id`, `create_time`)
VALUES (@course_id, '3.3 与 UI 及状态交互', @ch3, 3, 1, NOW());
SET @ch3_3 = LAST_INSERT_ID();

-- 第四章 ----------------------------------------------------------------------
INSERT INTO `course_chapter` (`course_id`, `chapter_name`, `parent_id`, `sort_order`, `creator_id`, `create_time`)
VALUES (@course_id, '第四章 列表与 Material Design', 0, 4, 1, NOW());
SET @ch4 = LAST_INSERT_ID();

INSERT INTO `course_chapter` (`course_id`, `chapter_name`, `parent_id`, `sort_order`, `creator_id`, `create_time`)
VALUES (@course_id, '4.1 数据类、函数与集合', @ch4, 1, 1, NOW());
SET @ch4_1 = LAST_INSERT_ID();
INSERT INTO `course_chapter` (`course_id`, `chapter_name`, `parent_id`, `sort_order`, `creator_id`, `create_time`)
VALUES (@course_id, '4.2 构建可滚动列表', @ch4, 2, 1, NOW());
SET @ch4_2 = LAST_INSERT_ID();
INSERT INTO `course_chapter` (`course_id`, `chapter_name`, `parent_id`, `sort_order`, `creator_id`, `create_time`)
VALUES (@course_id, '4.3 打造精美应用', @ch4, 3, 1, NOW());
SET @ch4_3 = LAST_INSERT_ID();

-- -----------------------------------------------------------------------------
-- 4. 课程资源 course_resource
--    resource_type: 1=视频 2=PDF 3=PPT 4=习题集 5=其他（如 markdown）
-- -----------------------------------------------------------------------------
INSERT INTO `course_resource` (`course_id`, `resource_name`, `resource_type`, `file_url`, `chapter_id`, `sort_order`, `uploader_id`, `is_public`, `create_time`)
VALUES (@course_id, '课程目标与学习方法', 5, '/courses/Android开发基础/01_课程导论/1.1_课程目标与学习方法/reading.md', @ch1_1, 1, 1, 1, NOW());
SET @r_1_1 = LAST_INSERT_ID();

INSERT INTO `course_resource` (`course_id`, `resource_name`, `resource_type`, `file_url`, `chapter_id`, `sort_order`, `uploader_id`, `is_public`, `create_time`)
VALUES (@course_id, 'Kotlin 入门 - 讲义', 5, '/courses/Android开发基础/02_第一个Android应用/2.1_Kotlin入门/reading.md', @ch2_1, 1, 1, 1, NOW());
SET @r_2_1_r = LAST_INSERT_ID();
INSERT INTO `course_resource` (`course_id`, `resource_name`, `resource_type`, `file_url`, `chapter_id`, `sort_order`, `uploader_id`, `is_public`, `create_time`)
VALUES (@course_id, 'Kotlin 入门 - 视频', 1, '/courses/Android开发基础/02_第一个Android应用/2.1_Kotlin入门/video.mp4', @ch2_1, 2, 1, 1, NOW());
SET @r_2_1_v = LAST_INSERT_ID();

INSERT INTO `course_resource` (`course_id`, `resource_name`, `resource_type`, `file_url`, `chapter_id`, `sort_order`, `uploader_id`, `is_public`, `create_time`)
VALUES (@course_id, '搭建 Android Studio - 讲义', 5, '/courses/Android开发基础/02_第一个Android应用/2.2_搭建Android_Studio/reading.md', @ch2_2, 1, 1, 1, NOW());
SET @r_2_2 = LAST_INSERT_ID();

INSERT INTO `course_resource` (`course_id`, `resource_name`, `resource_type`, `file_url`, `chapter_id`, `sort_order`, `uploader_id`, `is_public`, `create_time`)
VALUES (@course_id, '构建基础布局 - 讲义', 5, '/courses/Android开发基础/02_第一个Android应用/2.3_构建基础布局/reading.md', @ch2_3, 1, 1, 1, NOW());
SET @r_2_3_r = LAST_INSERT_ID();
INSERT INTO `course_resource` (`course_id`, `resource_name`, `resource_type`, `file_url`, `chapter_id`, `sort_order`, `uploader_id`, `is_public`, `create_time`)
VALUES (@course_id, '构建基础布局 - 视频', 1, '/courses/Android开发基础/02_第一个Android应用/2.3_构建基础布局/video.mp4', @ch2_3, 2, 1, 1, NOW());
SET @r_2_3_v = LAST_INSERT_ID();

INSERT INTO `course_resource` (`course_id`, `resource_name`, `resource_type`, `file_url`, `chapter_id`, `sort_order`, `uploader_id`, `is_public`, `create_time`)
VALUES (@course_id, 'Kotlin 进阶 - 讲义', 5, '/courses/Android开发基础/03_构建应用UI/3.1_Kotlin进阶/reading.md', @ch3_1, 1, 1, 1, NOW());
SET @r_3_1 = LAST_INSERT_ID();
INSERT INTO `course_resource` (`course_id`, `resource_name`, `resource_type`, `file_url`, `chapter_id`, `sort_order`, `uploader_id`, `is_public`, `create_time`)
VALUES (@course_id, '添加按钮与事件响应 - 讲义', 5, '/courses/Android开发基础/03_构建应用UI/3.2_添加按钮与事件响应/reading.md', @ch3_2, 1, 1, 1, NOW());
SET @r_3_2 = LAST_INSERT_ID();
INSERT INTO `course_resource` (`course_id`, `resource_name`, `resource_type`, `file_url`, `chapter_id`, `sort_order`, `uploader_id`, `is_public`, `create_time`)
VALUES (@course_id, '与 UI 及状态交互 - 讲义', 5, '/courses/Android开发基础/03_构建应用UI/3.3_与UI及状态交互/reading.md', @ch3_3, 1, 1, 1, NOW());
SET @r_3_3 = LAST_INSERT_ID();

INSERT INTO `course_resource` (`course_id`, `resource_name`, `resource_type`, `file_url`, `chapter_id`, `sort_order`, `uploader_id`, `is_public`, `create_time`)
VALUES (@course_id, '数据类、函数与集合 - 讲义', 5, '/courses/Android开发基础/04_列表与MaterialDesign/4.1_数据类_函数_集合/reading.md', @ch4_1, 1, 1, 1, NOW());
SET @r_4_1 = LAST_INSERT_ID();
INSERT INTO `course_resource` (`course_id`, `resource_name`, `resource_type`, `file_url`, `chapter_id`, `sort_order`, `uploader_id`, `is_public`, `create_time`)
VALUES (@course_id, '构建可滚动列表 - 讲义', 5, '/courses/Android开发基础/04_列表与MaterialDesign/4.2_构建可滚动列表/reading.md', @ch4_2, 1, 1, 1, NOW());
SET @r_4_2 = LAST_INSERT_ID();
INSERT INTO `course_resource` (`course_id`, `resource_name`, `resource_type`, `file_url`, `chapter_id`, `sort_order`, `uploader_id`, `is_public`, `create_time`)
VALUES (@course_id, '打造精美应用 - 讲义', 5, '/courses/Android开发基础/04_列表与MaterialDesign/4.3_打造精美应用/reading.md', @ch4_3, 1, 1, 1, NOW());
SET @r_4_3 = LAST_INSERT_ID();

-- -----------------------------------------------------------------------------
-- 5. 章节学习清单 chapter_content（学生进入章节看到的内容卡片列表）
--    content_type: 1=视频 2=知识点 3=资源文件
-- -----------------------------------------------------------------------------
INSERT INTO `chapter_content` (`chapter_id`, `content_type`, `content_title`, `ref_id`, `sort_order`, `create_time`) VALUES
(@ch1_1, 3, '课程目标与学习方法', @r_1_1, 1, NOW()),

(@ch2_1, 3, 'Kotlin 入门讲义',     @r_2_1_r, 1, NOW()),
(@ch2_1, 1, 'Kotlin 入门视频',     @r_2_1_v, 2, NOW()),
(@ch2_2, 3, '搭建 Android Studio', @r_2_2,   1, NOW()),
(@ch2_3, 3, '构建基础布局讲义',     @r_2_3_r, 1, NOW()),
(@ch2_3, 1, '构建基础布局演示视频', @r_2_3_v, 2, NOW()),

(@ch3_1, 3, 'Kotlin 进阶讲义',     @r_3_1, 1, NOW()),
(@ch3_2, 3, '添加按钮与事件响应',   @r_3_2, 1, NOW()),
(@ch3_3, 3, '与 UI 及状态交互',    @r_3_3, 1, NOW()),

(@ch4_1, 3, '数据类、函数与集合',   @r_4_1, 1, NOW()),
(@ch4_2, 3, '构建可滚动列表',       @r_4_2, 1, NOW()),
(@ch4_3, 3, '打造精美应用',         @r_4_3, 1, NOW());

-- -----------------------------------------------------------------------------
-- 6. 验证
-- -----------------------------------------------------------------------------
SELECT '✅ 章节数' AS step, COUNT(*) AS cnt FROM course_chapter WHERE course_id = @course_id;
SELECT '✅ 资源数' AS step, COUNT(*) AS cnt FROM course_resource WHERE course_id = @course_id;
SELECT '✅ 内容数' AS step, COUNT(*) AS cnt FROM chapter_content cc
  JOIN course_chapter ch ON cc.chapter_id = ch.id
  WHERE ch.course_id = @course_id;
