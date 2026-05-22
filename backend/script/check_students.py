"""查看班级和学生当前状态"""
import pymysql
conn = pymysql.connect(host='192.168.1.38', user='newuser', password='yourpassword', database='online_learn', charset='utf8mb4')
cur = conn.cursor()

print('=== class ===')
cur.execute("SELECT c.id, c.class_name, c.course_id, c.user_id, c.max_students, (SELECT COUNT(*) FROM user_class WHERE class_id=c.id) AS cnt FROM class c ORDER BY id")
for r in cur.fetchall():
    print(f'  id={r[0]:3d} name={r[1]:30s} course_id={r[2]} teacher={r[3]} max={r[4]} cur_cnt={r[5]}')

print('\n=== user 角色 (前 5 个学生角色) ===')
cur.execute("DESCRIBE user")
print('  user 表字段:')
for r in cur.fetchall(): print(f'    {r[0]} {r[1]}')

print('\n=== role 表 ===')
try:
    cur.execute("SELECT id, role_name FROM role")
    for r in cur.fetchall(): print(f'  id={r[0]} name={r[1]}')
except Exception as e:
    print(f'  错误: {e}')

print('\n=== user_role 表 ===')
try:
    cur.execute("DESCRIBE user_role")
    for r in cur.fetchall(): print(f'  {r[0]} {r[1]}')
except Exception as e:
    print(f'  错误: {e}')

print('\n=== 现有学生数量 (角色判断) ===')
try:
    cur.execute("SELECT COUNT(*) FROM user u JOIN user_role ur ON u.id=ur.user_id WHERE ur.role_id IN (SELECT id FROM role WHERE role_name LIKE '%学生%' OR role_name LIKE '%student%')")
    print(f'  学生用户数 = {cur.fetchone()[0]}')
except Exception as e:
    print(f'  错误: {e}')

# 看看 user 表前 5 个数据
cur.execute("SELECT * FROM user LIMIT 3")
print('\n=== user 表 前 3 行 ===')
for r in cur.fetchall():
    print(f'  {r}')

cur.close()
conn.close()
