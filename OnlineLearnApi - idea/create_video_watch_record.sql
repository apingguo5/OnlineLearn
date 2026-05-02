-- 创建视频观看记录表
DROP TABLE IF EXISTS `video_watch_record`;
CREATE TABLE `video_watch_record`  (
  `id` int(0) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` int(0) NOT NULL COMMENT '用户ID',
  `video_id` int(0) NOT NULL COMMENT '视频ID',
  `video_total_id` int(0) NOT NULL COMMENT '视频总集ID',
  `watch_time` int(0) NULL DEFAULT 0 COMMENT '观看时长（秒）',
  `last_watch_time` datetime NULL DEFAULT NULL COMMENT '最后观看时间',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_user_video`(`user_id`, `video_id`) USING BTREE
) ENGINE = MyISAM AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;
