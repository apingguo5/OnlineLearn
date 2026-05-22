"""
为测试目的批量生成学生：
- 班级 1 (2026春季1班) 共 55 人（已有 1 个，再补 54 个）
- 班级 4 (2026春季2班) 共 50 人（已有 0 个，补 50 个）

新用户：account = stu_test_{NNN}，password = 123456 (md5: e10adc3949ba59abbe56e057f20f883e)
"""
import pymysql
import hashlib

PWD_PLAIN = '123456'
PWD_MD5 = hashlib.md5(PWD_PLAIN.encode()).hexdigest()

conn = pymysql.connect(host='192.168.1.38', user='newuser', password='yourpassword', database='online_learn', charset='utf8mb4', autocommit=False)
cur = conn.cursor()

# 当前 max user id
cur.execute("SELECT COALESCE(MAX(id), 0) FROM user")
max_uid = cur.fetchone()[0]
print(f'当前最大 user.id = {max_uid}')

# 找已有 stu_test_ 的最大序号，避免重复
cur.execute("SELECT COUNT(*) FROM user WHERE account LIKE 'stu_test_%'")
existing = cur.fetchone()[0]
print(f'已存在 stu_test_* 数量 = {existing}')

# 学生角色 id
cur.execute("SELECT id FROM role WHERE role_name='学生' LIMIT 1")
student_role_id = cur.fetchone()[0]
print(f'学生角色 role_id = {student_role_id}')

# 班级目标
targets = [
    {'class_id': 1, 'class_name': '2026春季1班', 'target_count': 55},
    {'class_id': 4, 'class_name': '2026春季2班', 'target_count': 50},
]

# 班级目前已有的人数
for t in targets:
    cur.execute("SELECT COUNT(*) FROM user_class WHERE class_id=%s", (t['class_id'],))
    t['current_count'] = cur.fetchone()[0]
    t['need'] = max(0, t['target_count'] - t['current_count'])
    # 班级 max_students 上调到 target_count
    cur.execute("UPDATE class SET max_students=%s WHERE id=%s", (max(t['target_count'], 60), t['class_id']))
    print(f"班级 id={t['class_id']} {t['class_name']}: 当前 {t['current_count']} 人，需补 {t['need']} 人")

next_seq = existing + 1
new_users = []
new_user_classes = []
new_user_roles = []

for t in targets:
    for i in range(t['need']):
        account = f"stu_test_{next_seq:03d}"
        user_name = f"测试学生{next_seq:03d}"
        new_users.append((account, PWD_MD5, user_name))
        new_user_classes.append((account, t['class_id']))
        new_user_roles.append(account)
        next_seq += 1

print(f'\n准备批量插入 {len(new_users)} 个学生...')

# 一次性 INSERT user
sql_user = "INSERT INTO user (account, password, user_name, create_time) VALUES (%s, %s, %s, NOW())"
cur.executemany(sql_user, new_users)
print(f'✅ user 插入 {cur.rowcount} 行')

# 获取这些新用户的 id（按 account 倒查）
account_to_id = {}
if new_users:
    accounts = [u[0] for u in new_users]
    placeholders = ','.join(['%s'] * len(accounts))
    cur.execute(f"SELECT id, account FROM user WHERE account IN ({placeholders})", accounts)
    for uid, acct in cur.fetchall():
        account_to_id[acct] = uid

# 插入 user_role
ur_rows = [(account_to_id[a], student_role_id) for a in new_user_roles if a in account_to_id]
if ur_rows:
    cur.executemany("INSERT INTO user_role (user_id, role_id) VALUES (%s, %s)", ur_rows)
    print(f'✅ user_role 插入 {cur.rowcount} 行')

# 插入 user_class
uc_rows = [(account_to_id[a], cid) for a, cid in new_user_classes if a in account_to_id]
if uc_rows:
    cur.executemany("INSERT INTO user_class (user_id, class_id) VALUES (%s, %s)", uc_rows)
    print(f'✅ user_class 插入 {cur.rowcount} 行')

conn.commit()
print('\n=== 验证 ===')
for t in targets:
    cur.execute("SELECT COUNT(*) FROM user_class WHERE class_id=%s", (t['class_id'],))
    cnt = cur.fetchone()[0]
    cur.execute("SELECT max_students FROM class WHERE id=%s", (t['class_id'],))
    maxs = cur.fetchone()[0]
    print(f"  班级 id={t['class_id']} {t['class_name']}: 实际人数={cnt} / 上限={maxs}")

cur.close()
conn.close()
print('\n🎉 完成！')
