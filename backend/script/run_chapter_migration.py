import mysql.connector
import sys

# 数据库连接配置
config = {
    'host': '192.168.1.38',
    'user': 'newuser',
    'password': 'yourpassword',
    'database': 'online_learn',
    'charset': 'utf8mb4'
}

# SQL 迁移脚本
sql_script = """
-- 1. 添加缺失的列
ALTER TABLE `course_chapter`
  ADD COLUMN IF NOT EXISTS `chapter_type` varchar(50) DEFAULT NULL COMMENT '章节类型: video/quiz/reading' AFTER `chapter_name`,
  ADD COLUMN IF NOT EXISTS `description` text DEFAULT NULL COMMENT '章节简介' AFTER `chapter_type`,
  ADD COLUMN IF NOT EXISTS `publish_status` int UNSIGNED DEFAULT 0 COMMENT '发布状态: 0未配置 1已配置 2已发布' AFTER `parent_id`,
  ADD COLUMN IF NOT EXISTS `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `create_time`;

-- 2. 检查并重命名 course_id -> class_id
SET @exist := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'online_learn' AND TABLE_NAME = 'course_chapter' AND COLUMN_NAME = 'course_id');
SET @sql := IF(@exist > 0, 'ALTER TABLE `course_chapter` CHANGE COLUMN `course_id` `class_id` int UNSIGNED NOT NULL COMMENT \\'所属班级ID\\'', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3. 添加索引
SET @idx1 := (SELECT COUNT(*) FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = 'online_learn' AND TABLE_NAME = 'course_chapter' AND INDEX_NAME = 'idx_class_id');
SET @sql1 := IF(@idx1 = 0, 'CREATE INDEX `idx_class_id` ON `course_chapter`(`class_id`)', 'SELECT 1');
PREPARE stmt1 FROM @sql1;
EXECUTE stmt1;
DEALLOCATE PREPARE stmt1;

SET @idx2 := (SELECT COUNT(*) FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = 'online_learn' AND TABLE_NAME = 'course_chapter' AND INDEX_NAME = 'idx_parent_id');
SET @sql2 := IF(@idx2 = 0, 'CREATE INDEX `idx_parent_id` ON `course_chapter`(`parent_id`)', 'SELECT 1');
PREPARE stmt2 FROM @sql2;
EXECUTE stmt2;
DEALLOCATE PREPARE stmt2;

SET @idx3 := (SELECT COUNT(*) FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = 'online_learn' AND TABLE_NAME = 'course_chapter' AND INDEX_NAME = 'idx_publish_status');
SET @sql3 := IF(@idx3 = 0, 'CREATE INDEX `idx_publish_status` ON `course_chapter`(`publish_status`)', 'SELECT 1');
PREPARE stmt3 FROM @sql3;
EXECUTE stmt3;
DEALLOCATE PREPARE stmt3;

-- 4. 创建 chapter_content 表
CREATE TABLE IF NOT EXISTS `chapter_content` (
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
"""

def run_sql_script(cursor, script):
    """逐条执行 SQL 语句（跳过注释和空行）"""
    statements = []
    current_stmt = []
    for line in script.split('\n'):
        stripped = line.strip()
        if not stripped or stripped.startswith('--') or stripped.startswith('#'):
            continue
        current_stmt.append(line)
        if stripped.endswith(';'):
            statements.append('\n'.join(current_stmt))
            current_stmt = []
    if current_stmt:
        statements.append('\n'.join(current_stmt))

    for i, stmt in enumerate(statements):
        try:
            # 跳过 SET/@PREPARE/DEALLOCATE 这些属于存储过程语法，用简单的方式
            stmt_upper = stmt.strip().upper()
            if stmt_upper.startswith('SET @') or stmt_upper.startswith('PREPARE') or stmt_upper.startswith('DEALLOCATE') or stmt_upper.startswith('EXECUTE'):
                print(f"  [存储过程语句, 跳过直接执行]")
                continue
            
            cursor.execute(stmt)
            print(f"  ✓ 语句 {i+1} 执行成功")
        except Exception as e:
            # 有些 ALTER ADD COLUMN 如果列已存在会报错，可忽略
            err_str = str(e)
            if "Duplicate column" in err_str or "Duplicate key name" in err_str:
                print(f"  - 语句 {i+1} 已存在: {err_str.split('(')[0]}")
            else:
                print(f"  ✗ 语句 {i+1} 执行失败: {err_str}")

def main():
    print("=" * 60)
    print("开始执行 course_chapter 表结构迁移...")
    print("=" * 60)
    
    try:
        conn = mysql.connector.connect(**config)
        cursor = conn.cursor()
        print("✓ 数据库连接成功")
        
        # 先检查当前表结构
        cursor.execute("DESCRIBE course_chapter")
        print("\n当前 course_chapter 表结构:")
        for col in cursor.fetchall():
            print(f"  {col[0]:20s} {col[1]:30s} {'PRI' if col[3] == 'PRI' else ''}")
        
        print("\n开始执行迁移...")
        
        # 分别执行各个迁移步骤
        # Step 1: 添加列
        print("\n[Step 1] 添加缺失列...")
        alter_commands = [
            "ALTER TABLE `course_chapter` ADD COLUMN `chapter_type` varchar(50) DEFAULT NULL COMMENT '章节类型: video/quiz/reading' AFTER `chapter_name`",
            "ALTER TABLE `course_chapter` ADD COLUMN `description` text DEFAULT NULL COMMENT '章节简介' AFTER `chapter_type`",
            "ALTER TABLE `course_chapter` ADD COLUMN `publish_status` int UNSIGNED DEFAULT 0 COMMENT '发布状态: 0未配置 1已配置 2已发布' AFTER `parent_id`",
            "ALTER TABLE `course_chapter` ADD COLUMN `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `create_time`"
        ]
        for cmd in alter_commands:
            try:
                cursor.execute(cmd)
                print(f"  ✓ {cmd.split('ADD COLUMN')[1].split('`')[1]}")
            except Exception as e:
                if "Duplicate column" in str(e):
                    print(f"  - 列已存在，跳过")
                else:
                    raise e
        
        # Step 2: 重命名 course_id -> class_id
        print("\n[Step 2] 重命名 course_id -> class_id...")
        cursor.execute("SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'online_learn' AND TABLE_NAME = 'course_chapter' AND COLUMN_NAME = 'course_id'")
        has_course_id = cursor.fetchone()[0] > 0
        if has_course_id:
            cursor.execute("ALTER TABLE `course_chapter` CHANGE COLUMN `course_id` `class_id` int UNSIGNED NOT NULL COMMENT '所属班级ID'")
            print("  ✓ 重命名成功")
        else:
            print("  - 列名已为 class_id，无需重命名")
        
        # Step 3: 添加索引
        print("\n[Step 3] 添加索引...")
        index_checks = [
            ('idx_class_id', 'class_id'),
            ('idx_parent_id', 'parent_id'),
            ('idx_publish_status', 'publish_status')
        ]
        for idx_name, col_name in index_checks:
            cursor.execute(f"SELECT COUNT(*) FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = 'online_learn' AND TABLE_NAME = 'course_chapter' AND INDEX_NAME = '{idx_name}'")
            if cursor.fetchone()[0] == 0:
                cursor.execute(f"CREATE INDEX `{idx_name}` ON `course_chapter`(`{col_name}`)")
                print(f"  ✓ 创建索引 {idx_name}")
            else:
                print(f"  - 索引 {idx_name} 已存在")
        
        # Step 4: 创建 chapter_content 表
        print("\n[Step 4] 创建 chapter_content 表...")
        cursor.execute("""
            CREATE TABLE IF NOT EXISTS `chapter_content` (
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
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='章节内容关联表'
        """)
        print("  ✓ chapter_content 表创建成功")
        
        conn.commit()
        
        # 最终验证
        print("\n" + "=" * 60)
        print("迁移完成！最终表结构验证:")
        print("=" * 60)
        
        cursor.execute("DESCRIBE course_chapter")
        print("\ncourse_chapter 表:")
        for col in cursor.fetchall():
            print(f"  {col[0]:20s} {col[1]:30s}")
        
        cursor.execute("DESCRIBE chapter_content")
        print("\nchapter_content 表:")
        for col in cursor.fetchall():
            print(f"  {col[0]:20s} {col[1]:30s}")
        
        cursor.close()
        conn.close()
        print("\n✓ 迁移脚本执行完毕！")
        
    except Exception as e:
        print(f"\n✗ 迁移失败: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)

if __name__ == '__main__':
    main()