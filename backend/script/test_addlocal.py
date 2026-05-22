"""测试 addLocalResource 接口"""
import requests, json

# 找一个 ch 子章节 id
import pymysql
conn = pymysql.connect(host='192.168.1.38', user='newuser', password='yourpassword', database='online_learn', charset='utf8mb4')
cur = conn.cursor()
cur.execute("SELECT id, chapter_name FROM course_chapter WHERE course_id=3 AND parent_id != 0 LIMIT 1")
row = cur.fetchone()
print(f'测试章节: id={row[0]} name={row[1]}')
ch_id = row[0]
cur.close(); conn.close()

# 调接口
url = 'http://localhost:9251/study/teacher/course/addLocalResource'
payload = {
    'chapterId': ch_id,
    'courseId': 3,
    'localPath': '/courses/Android开发基础/01_课程导论/1.1_课程目标与学习方法/reading.md'
}
r = requests.post(url, json=payload, timeout=10)
print(f'status={r.status_code}')
print(f'body={r.text[:600]}')
