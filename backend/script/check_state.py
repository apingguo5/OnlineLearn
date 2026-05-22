"""查看 course_chapter 现有数据 + 现存课程/班级"""
import pymysql
conn = pymysql.connect(host='192.168.1.38', user='newuser', password='yourpassword', database='online_learn', charset='utf8mb4')
cur = conn.cursor()

print('=== course ===')
cur.execute('SELECT id, course_name, status FROM course ORDER BY id')
for r in cur.fetchall(): print(f'  id={r[0]:3d} name={r[1]} status={r[2]}')

print('\n=== class ===')
cur.execute('SELECT id, class_name, course_id, user_id FROM class ORDER BY id')
for r in cur.fetchall(): print(f'  id={r[0]:3d} name={r[1]} course_id={r[2]} user_id={r[3]}')

print('\n=== course_chapter 行数 ===')
cur.execute('SELECT COUNT(*) FROM course_chapter')
print(f'  total: {cur.fetchone()[0]}')
cur.execute('SELECT id, class_id, chapter_name, parent_id, sort_order FROM course_chapter ORDER BY id')
for r in cur.fetchall(): print(f'  id={r[0]:3d} class_id={r[1]:3d} name={r[2]:30s} parent={r[3]} sort={r[4]}')

print('\n=== chapter_content 行数 ===')
cur.execute('SELECT COUNT(*) FROM chapter_content')
print(f'  total: {cur.fetchone()[0]}')

print('\n=== course_resource for Android 课程 ===')
cur.execute("SELECT id, course_id, resource_name, chapter_id, file_url FROM course_resource WHERE course_id IN (SELECT id FROM course WHERE course_name='Android 开发基础') ORDER BY id")
for r in cur.fetchall(): print(f'  id={r[0]:3d} course_id={r[1]} name={r[2]:30s} chapter_id={r[3]} url={r[4]}')

cur.close()
conn.close()
