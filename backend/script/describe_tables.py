"""快速诊断 course_chapter 表的实际字段"""
import pymysql
conn = pymysql.connect(host='192.168.1.38', user='newuser', password='yourpassword', database='online_learn', charset='utf8mb4')
cur = conn.cursor()
for t in ('course', 'class', 'course_chapter', 'chapter_content', 'course_resource'):
    print(f'\n=== {t} ===')
    try:
        cur.execute(f'DESCRIBE `{t}`')
        for row in cur.fetchall():
            print(f'  {row[0]:30s} {row[1]:30s} null={row[2]:4s} key={row[3]:4s}')
    except Exception as e:
        print(f'  ERROR: {e}')
cur.close()
conn.close()
