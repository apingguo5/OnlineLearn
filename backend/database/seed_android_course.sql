-- =============================================================================
-- Android 开发基础课程 - 完整数据种子脚本
-- =============================================================================
-- 关系链：course → course_chapter → chapter_content → course_resource
-- 真实文件位于：项目根目录 /courses/Android开发基础/
-- 数据库中存储的路径形如：./Android开发基础/01_课程导论/1.1.../reading.md
-- 注意：该路径与现有 MyWebMvcConfig 中 /resource/** → /resource/ 的映射规则一致
--      若希望访问 /courses/** 文件，需额外配置一条静态资源映射（见文末说明）
-- =============================================================================

USE `online_learn`;

-- =============================================================================
-- 1. 清理旧数据（仅清理同名课程，避免重复）
-- =============================================================================
DELETE cr FROM `course_resource` cr
  INNER JOIN `course` c ON cr.course_id = c.id
  WHERE c.course_name = 'Android 开发基础';

DELETE cc FROM `chapter_content` cc
  INNER JOIN `course_chapter` ch ON cc.chapter_id = ch.id
  INNER JOIN `course` c ON ch.course_id = c.id
  WHERE c.course_name = 'Android 开发基础';

DELETE ch FROM `course_chapter` ch
  INNER JOIN `course` c ON ch.course_id = c.id
  WHERE c.course_name = 'Android 开发基础';

DELETE FROM `course` WHERE course_name = 'Android 开发基础';

-- =============================================================================
-- 2. 插入课程主记录
-- =============================================================================
INSERT INTO `course` (`course_name`, `description`, `creator_id`, `cover_url`, `status`, `create_time`)
VALUES (
    'Android 开发基础',
    '从零开始系统学习 Kotlin 与 Jetpack Compose，构建你的第一个现代 Android 应用。课程基于 Google 官方培训资料组织，覆盖 Kotlin 基础、UI 构建、列表与 Material Design 等核心主题。',
    1,
    './安卓开发.png',  -- 复用 backend/resource 中已有的封面图
    1,
    NOW()
);
SET @course_id = LAST_INSERT_ID();

-- =============================================================================
-- 3. 插入章节（顶级章节 parent_id=0，子章节 parent_id 指向父章节 id）
-- =============================================================================

-- ---- 第 1 章 ----
INSERT INTO `course_chapter` (`course_id`, `chapter_name`, `parent_id`, `sort_order`, `creator_id`, `create_time`) VALUES
(@course_id, '第一章 课程导论', 0, 1, 1, NOW());
SET @ch1 = LAST_INSERT_ID();
INSERT INTO `course_chapter` (`course_id`, `chapter_name`, `parent_id`, `sort_order`, `creator_id`, `create_time`) VALUES
(@course_id, '1.1 课程目标与学习方法', @ch1, 1, 1, NOW());
SET @ch1_1 = LAST_INSERT_ID();

-- ---- 第 2 章 ----
INSERT INTO `course_chapter` (`course_id`, `chapter_name`, `parent_id`, `sort_order`, `creator_id`, `create_time`) VALUES
(@course_id, '第二章 你的第一个 Android 应用', 0, 2, 1, NOW());
SET @ch2 = LAST_INSERT_ID();
INSERT INTO `course_chapter` (`course_id`, `chapter_name`, `parent_id`, `sort_order`, `creator_id`, `create_time`) VALUES
(@course_id, '2.1 Kotlin 入门', @ch2, 1, 1, NOW());
SET @ch2_1 = LAST_INSERT_ID();
INSERT INTO `course_chapter` (`course_id`, `chapter_name`, `parent_id`, `sort_order`, `creator_id`, `create_time`) VALUES
(@course_id, '2.2 搭建 Android Studio', @ch2, 2, 1, NOW());
SET @ch2_2 = LAST_INSERT_ID();
INSERT INTO `course_chapter` (`course_id`, `chapter_name`, `parent_id`, `sort_order`, `creator_id`, `create_time`) VALUES
(@course_id, '2.3 构建基础布局', @ch2, 3, 1, NOW());
SET @ch2_3 = LAST_INSERT_ID();

-- ---- 第 3 章 ----
INSERT INTO `course_chapter` (`course_id`, `chapter_name`, `parent_id`, `sort_order`, `creator_id`, `create_time`) VALUES
(@course_id, '第三章 构建应用 UI', 0, 3, 1, NOW());
SET @ch3 = LAST_INSERT_ID();
INSERT INTO `course_chapter` (`course_id`, `chapter_name`, `parent_id`, `sort_order`, `creator_id`, `create_time`) VALUES
(@course_id, '3.1 Kotlin 进阶', @ch3, 1, 1, NOW());
SET @ch3_1 = LAST_INSERT_ID();
INSERT INTO `course_chapter` (`course_id`, `chapter_name`, `parent_id`, `sort_order`, `creator_id`, `create_time`) VALUES
(@course_id, '3.2 添加按钮与事件响应', @ch3, 2, 1, NOW());
SET @ch3_2 = LAST_INSERT_ID();
INSERT INTO `course_chapter` (`course_id`, `chapter_name`, `parent_id`, `sort_order`, `creator_id`, `create_time`) VALUES
(@course_id, '3.3 与 UI 及状态交互', @ch3, 3, 1, NOW());
SET @ch3_3 = LAST_INSERT_ID();

-- ---- 第 4 章 ----
INSERT INTO `course_chapter` (`course_id`, `chapter_name`, `parent_id`, `sort_order`, `creator_id`, `create_time`) VALUES
(@course_id, '第四章 列表与 Material Design', 0, 4, 1, NOW());
SET @ch4 = LAST_INSERT_ID();
INSERT INTO `course_chapter` (`course_id`, `chapter_name`, `parent_id`, `sort_order`, `creator_id`, `create_time`) VALUES
(@course_id, '4.1 数据类、函数与集合', @ch4, 1, 1, NOW());
SET @ch4_1 = LAST_INSERT_ID();
INSERT INTO `course_chapter` (`course_id`, `chapter_name`, `parent_id`, `sort_order`, `creator_id`, `create_time`) VALUES
(@course_id, '4.2 构建可滚动列表', @ch4, 2, 1, NOW());
SET @ch4_2 = LAST_INSERT_ID();
INSERT INTO `course_chapter` (`course_id`, `chapter_name`, `parent_id`, `sort_order`, `creator_id`, `create_time`) VALUES
(@course_id, '4.3 打造精美应用', @ch4, 3, 1, NOW());
SET @ch4_3 = LAST_INSERT_ID();

-- =============================================================================
-- 4. 插入课程资源 course_resource
--    resource_type:  1=视频  2=PDF  3=PPT  4=习题集  5=其他(含 markdown)
--    file_url 字段统一使用 /courses/... 前缀，便于前后端拼接
-- =============================================================================

-- 1.1
INSERT INTO `course_resource` (`course_id`, `resource_name`, `resource_type`, `file_url`, `chapter_id`, `sort_order`, `uploader_id`, `is_public`, `create_time`) VALUES
(@course_id, '课程目标与学习方法',  5, '/courses/Android开发基础/01_课程导论/1.1_课程目标与学习方法/reading.md', @ch1_1, 1, 1, 1, NOW());
SET @res_1_1_reading = LAST_INSERT_ID();

-- 2.1
INSERT INTO `course_resource` (`course_id`, `resource_name`, `resource_type`, `file_url`, `chapter_id`, `sort_order`, `uploader_id`, `is_public`, `create_time`) VALUES
(@course_id, 'Kotlin 入门 - 讲义',  5, '/courses/Android开发基础/02_第一个Android应用/2.1_Kotlin入门/reading.md', @ch2_1, 1, 1, 1, NOW());
SET @res_2_1_reading = LAST_INSERT_ID();
INSERT INTO `course_resource` (`course_id`, `resource_name`, `resource_type`, `file_url`, `chapter_id`, `sort_order`, `uploader_id`, `is_public`, `create_time`) VALUES
(@course_id, 'Kotlin 入门 - 视频',  1, '/courses/Android开发基础/02_第一个Android应用/2.1_Kotlin入门/video.mp4',  @ch2_1, 2, 1, 1, NOW());
SET @res_2_1_video = LAST_INSERT_ID();

-- 2.2
INSERT INTO `course_resource` (`course_id`, `resource_name`, `resource_type`, `file_url`, `chapter_id`, `sort_order`, `uploader_id`, `is_public`, `create_time`) VALUES
(@course_id, '搭建 Android Studio - 讲义', 5, '/courses/Android开发基础/02_第一个Android应用/2.2_搭建Android_Studio/reading.md', @ch2_2, 1, 1, 1, NOW());
SET @res_2_2_reading = LAST_INSERT_ID();

-- 2.3
INSERT INTO `course_resource` (`course_id`, `resource_name`, `resource_type`, `file_url`, `chapter_id`, `sort_order`, `uploader_id`, `is_public`, `create_time`) VALUES
(@course_id, '构建基础布局 - 讲义',  5, '/courses/Android开发基础/02_第一个Android应用/2.3_构建基础布局/reading.md', @ch2_3, 1, 1, 1, NOW());
SET @res_2_3_reading = LAST_INSERT_ID();
INSERT INTO `course_resource` (`course_id`, `resource_name`, `resource_type`, `file_url`, `chapter_id`, `sort_order`, `uploader_id`, `is_public`, `create_time`) VALUES
(@course_id, '构建基础布局 - 视频',  1, '/courses/Android开发基础/02_第一个Android应用/2.3_构建基础布局/video.mp4',  @ch2_3, 2, 1, 1, NOW());
SET @res_2_3_video = LAST_INSERT_ID();

-- 3.x
INSERT INTO `course_resource` (`course_id`, `resource_name`, `resource_type`, `file_url`, `chapter_id`, `sort_order`, `uploader_id`, `is_public`, `create_time`) VALUES
(@course_id, 'Kotlin 进阶 - 讲义',     5, '/courses/Android开发基础/03_构建应用UI/3.1_Kotlin进阶/reading.md',          @ch3_1, 1, 1, 1, NOW());
SET @res_3_1_reading = LAST_INSERT_ID();
INSERT INTO `course_resource` (`course_id`, `resource_name`, `resource_type`, `file_url`, `chapter_id`, `sort_order`, `uploader_id`, `is_public`, `create_time`) VALUES
(@course_id, '添加按钮与事件响应 - 讲义', 5, '/courses/Android开发基础/03_构建应用UI/3.2_添加按钮与事件响应/reading.md', @ch3_2, 1, 1, 1, NOW());
SET @res_3_2_reading = LAST_INSERT_ID();
INSERT INTO `course_resource` (`course_id`, `resource_name`, `resource_type`, `file_url`, `chapter_id`, `sort_order`, `uploader_id`, `is_public`, `create_time`) VALUES
(@course_id, '与 UI 及状态交互 - 讲义',  5, '/courses/Android开发基础/03_构建应用UI/3.3_与UI及状态交互/reading.md',    @ch3_3, 1, 1, 1, NOW());
SET @res_3_3_reading = LAST_INSERT_ID();

-- 4.x
INSERT INTO `course_resource` (`course_id`, `resource_name`, `resource_type`, `file_url`, `chapter_id`, `sort_order`, `uploader_id`, `is_public`, `create_time`) VALUES
(@course_id, '数据类、函数与集合 - 讲义', 5, '/courses/Android开发基础/04_列表与MaterialDesign/4.1_数据类_函数_集合/reading.md', @ch4_1, 1, 1, 1, NOW());
SET @res_4_1_reading = LAST_INSERT_ID();
INSERT INTO `course_resource` (`course_id`, `resource_name`, `resource_type`, `file_url`, `chapter_id`, `sort_order`, `uploader_id`, `is_public`, `create_time`) VALUES
(@course_id, '构建可滚动列表 - 讲义',     5, '/courses/Android开发基础/04_列表与MaterialDesign/4.2_构建可滚动列表/reading.md',     @ch4_2, 1, 1, 1, NOW());
SET @res_4_2_reading = LAST_INSERT_ID();
INSERT INTO `course_resource` (`course_id`, `resource_name`, `resource_type`, `file_url`, `chapter_id`, `sort_order`, `uploader_id`, `is_public`, `create_time`) VALUES
(@course_id, '打造精美应用 - 讲义',       5, '/courses/Android开发基础/04_列表与MaterialDesign/4.3_打造精美应用/reading.md',     @ch4_3, 1, 1, 1, NOW());
SET @res_4_3_reading = LAST_INSERT_ID();

-- =============================================================================
-- 5. 插入章节内容 chapter_content（学生侧"学习入口列表"）
--    content_type:  1=视频  2=知识点(阅读)  3=资源文件
--    ref_id      :  指向 course_resource.id 或 knowledge_point.id
-- =============================================================================
INSERT INTO `chapter_content` (`chapter_id`, `content_type`, `content_title`, `ref_id`, `sort_order`, `create_time`) VALUES
(@ch1_1, 3, '课程目标与学习方法',  @res_1_1_reading, 1, NOW()),

(@ch2_1, 3, 'Kotlin 入门讲义',     @res_2_1_reading, 1, NOW()),
(@ch2_1, 1, 'Kotlin 入门视频',     @res_2_1_video,   2, NOW()),
(@ch2_2, 3, '搭建 Android Studio', @res_2_2_reading, 1, NOW()),
(@ch2_3, 3, '构建基础布局讲义',     @res_2_3_reading, 1, NOW()),
(@ch2_3, 1, '构建基础布局演示视频', @res_2_3_video,   2, NOW()),

(@ch3_1, 3, 'Kotlin 进阶讲义',     @res_3_1_reading, 1, NOW()),
(@ch3_2, 3, '添加按钮与事件响应',   @res_3_2_reading, 1, NOW()),
(@ch3_3, 3, '与 UI 及状态交互',    @res_3_3_reading, 1, NOW()),

(@ch4_1, 3, '数据类、函数与集合',   @res_4_1_reading, 1, NOW()),
(@ch4_2, 3, '构建可滚动列表',       @res_4_2_reading, 1, NOW()),
(@ch4_3, 3, '打造精美应用',         @res_4_3_reading, 1, NOW());

-- =============================================================================
-- 6. （可选）为该课程创建一个示例班级
-- =============================================================================
INSERT INTO `class` (`class_name`, `course_id`, `user_id`, `academic_year`, `semester`, `max_students`, `create_time`)
VALUES ('Android 开发基础 - 2025 春季班', @course_id, 1, '2024-2025', 1, 60, NOW());

-- =============================================================================
-- 7. 验证查询
-- =============================================================================
SELECT '✅ 课程创建' AS step, id, course_name FROM course WHERE id = @course_id;

SELECT '✅ 章节列表' AS step, id, chapter_name, parent_id, sort_order
FROM course_chapter
WHERE course_id = @course_id
ORDER BY parent_id, sort_order, id;

SELECT '✅ 章节内容' AS step,
       cc.id, ch.chapter_name, cc.content_type, cc.content_title, cc.ref_id, cc.sort_order
FROM chapter_content cc
JOIN course_chapter ch ON cc.chapter_id = ch.id
WHERE ch.course_id = @course_id
ORDER BY ch.sort_order, cc.sort_order;

SELECT '✅ 课程资源' AS step,
       id, resource_name, resource_type, chapter_id, file_url
FROM course_resource
WHERE course_id = @course_id
ORDER BY chapter_id, sort_order;

-- =============================================================================
-- ⚠️ 提示：要让前端能真正打开 /courses/Android开发基础/xxx/reading.md
--    需要在 backend/src/main/java/com/rabbiter/ol/config/MyWebMvnConfig.java
--    中追加一条静态资源映射：
--
--    registry.addResourceHandler("/courses/**")
--            .addResourceLocations("file:" + PathUtils.getProjectRoot() + "/courses/");
--
--    或者临时把 /courses 目录拷贝到 backend/resource/ 下，复用现有
--    /resource/** 映射规则。
-- =============================================================================
