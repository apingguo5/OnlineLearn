"""测试班级更新接口"""
import requests, json

# 找一个班级
import pymysql
conn = pymysql.connect(host='192.168.1.38', user='newuser', password='yourpassword', database='online_learn', charset='utf8mb4')
cur = conn.cursor()
cur.execute("SELECT id, class_name FROM class ORDER BY id LIMIT 1")
row = cur.fetchone()
print(f'测试前: id={row[0]}, name={row[1]}')
class_id = row[0]
orig_name = row[1]
cur.close(); conn.close()

# 调用更新接口
new_name = orig_name + ' [测试更新]'
r = requests.post('http://localhost:9251/study/class/update',
    json={'id': class_id, 'className': new_name}, timeout=5)
print(f'更新接口 status={r.status_code}')
print(f'  body={r.text[:200]}')

# 查询验证
conn = pymysql.connect(host='192.168.1.38', user='newuser', password='yourpassword', database='online_learn', charset='utf8mb4')
cur = conn.cursor()
cur.execute("SELECT class_name FROM class WHERE id=%s", (class_id,))
n = cur.fetchone()[0]
print(f'更新后 db 中 name={n}')

# 回滚
cur.execute("UPDATE class SET class_name=%s WHERE id=%s", (orig_name, class_id))
conn.commit()
print(f'已回滚为 {orig_name}')
cur.close(); conn.close()
