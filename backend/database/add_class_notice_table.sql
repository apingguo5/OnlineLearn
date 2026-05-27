-- ============================================================================
-- 新增：班级通知表 (class_notice)
-- 教师可在互动答疑模块中向指定班级发送通知
-- 学生可在问答社区中查看所属班级的通知
-- ============================================================================

DROP TABLE IF EXISTS `class_notice`;
CREATE TABLE `class_notice` (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `class_id` int UNSIGNED DEFAULT NULL COMMENT '目标班级ID（null 表示全局通知）',
  `title` varchar(255) NOT NULL COMMENT '通知标题',
  `content` text COMMENT '通知内容',
  `notice_type` tinyint UNSIGNED DEFAULT 1 COMMENT '通知类型（1: 公告, 2: 提醒, 3: 其他）',
  `sender_id` int UNSIGNED DEFAULT NULL COMMENT '发送人ID（教师）',
  `is_pinned` tinyint UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否置顶（0: 否, 1: 是）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  PRIMARY KEY (`id`),
  KEY `idx_class_id` (`class_id`),
  KEY `idx_sender_id` (`sender_id`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_is_pinned` (`is_pinned`),
  CONSTRAINT `fk_notice_class` FOREIGN KEY (`class_id`) REFERENCES `class` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_notice_sender` FOREIGN KEY (`sender_id`) REFERENCES `user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='班级通知表';
