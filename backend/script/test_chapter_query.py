"""模拟 chapter_content 关联查询，看实际是否能返回内容"""
import pymysql
conn = pymysql.connect(host='192.168.1.38', user='newuser', password='yourpassword', database='online_learn', charset='utf8mb4')
cur = conn.cursor(pymysql.cursors.DictCursor)

# 找一个有内容的叶子章节
cur.execute("""
SELECT cc.id, cc.chapter_name, COUNT(c.id) AS content_cnt
FROM course_chapter cc
LEFT JOIN chapter_content c ON c.chapter_id = cc.id
WHERE cc.course_id = 3
GROUP BY cc.id, cc.chapter_name
ORDER BY cc.parent_id, cc.sort_order
""")
print('=== 章节及其内容数量 ===')
for r in cur.fetchall():
    print(f"  ch_id={r['id']:3d} | content_cnt={r['content_cnt']} | {r['chapter_name']}")

# 测试 chapter_content 联表查询（模拟 ChapterContentDao.queryListWithDetails）
print('\n=== 测试 chapterId=5 (1.1 子章节) 的 chapter_content 联表 ===')
test_chapter_id = None
cur.execute("SELECT id FROM course_chapter WHERE course_id=3 AND parent_id != 0 ORDER BY id LIMIT 1")
row = cur.fetchone()
if row:
    test_chapter_id = row['id']
    print(f"  使用 chapterId={test_chapter_id}")
    cur.execute("""
SELECT
    c.id, c.chapter_id AS chapterId, c.content_type AS contentType,
    c.content_title AS contentTitle, c.ref_id AS refId,
    c.sort_order AS sortOrder, c.create_time AS createTime,
    CASE
        WHEN c.content_type = 1 THEN cr1.resource_name
        WHEN c.content_type = 2 THEN kp.title
        WHEN c.content_type = 3 THEN cr3.resource_name
        ELSE ''
    END AS refTitle
FROM chapter_content c
LEFT JOIN course_resource cr1 ON c.content_type = 1 AND c.ref_id = cr1.id
LEFT JOIN knowledge_point kp ON c.content_type = 2 AND c.ref_id = kp.id
LEFT JOIN course_resource cr3 ON c.content_type = 3 AND c.ref_id = cr3.id
WHERE c.chapter_id = %s
ORDER BY c.sort_order ASC, c.id ASC
""", (test_chapter_id,))
    for r in cur.fetchall():
        print(f"  {r}")

# 检查 knowledge_point 表是否存在
print('\n=== knowledge_point 表是否存在 ===')
try:
    cur.execute('DESCRIBE knowledge_point')
    print('  存在')
except Exception as e:
    print(f'  错误: {e}')

cur.close()
conn.close()
