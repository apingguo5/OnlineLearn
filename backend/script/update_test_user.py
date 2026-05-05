#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
测试用户修改脚本
功能：根据用户名查找已存在的用户，将密码和角色修改为新的指定值

用法：
    python update_test_user.py <username> <password> <role>

参数：
    username  已有用户的登录账号（查找依据）
    password  新的登录密码
    role      新的用户角色（1=管理员, 2=教师, 3=学生）

示例：
    python update_test_user.py admin newpass456 2
    python update_test_user.py student 654321 3
"""

import sys
from datetime import datetime

# ============ 数据库配置（与 application.yml 保持一致）============
DB_CONFIG = {
    "host": "192.168.1.38",
    "port": 3306,
    "user": "newuser",
    "password": "yourpassword",
    "database": "online_learn",
}
# ================================================================

ROLE_MAP = {1: "管理员", 2: "教师", 3: "学生"}


def main():
    # ---- 1. 检查参数 ----
    if len(sys.argv) != 4:
        print("错误：参数数量不正确")
        print("用法：python update_test_user.py <username> <password> <role>")
        print("  role: 1=管理员, 2=教师, 3=学生")
        sys.exit(1)

    username = sys.argv[1].strip()
    password = sys.argv[2].strip()
    role_str = sys.argv[3].strip()

    if not username:
        print("错误：用户名不能为空")
        sys.exit(1)
    if not password:
        print("错误：密码不能为空")
        sys.exit(1)

    try:
        role_id = int(role_str)
    except ValueError:
        print(f"错误：角色参数必须是数字（1=管理员, 2=教师, 3=学生），收到: {role_str}")
        sys.exit(1)

    if role_id not in ROLE_MAP:
        print(f"错误：无效的角色值 {role_id}，可选：1=管理员, 2=教师, 3=学生")
        sys.exit(1)

    role_name = ROLE_MAP[role_id]
    print(f"准备修改测试用户：")
    print(f"  查找用户名: {username}")
    print(f"  新密码: {password}")
    print(f"  新角色: {role_name} (role_id={role_id})")
    print()

    # ---- 2. 导入依赖 ----
    import hashlib

    try:
        import mysql.connector
    except ImportError:
        print("错误：缺少 mysql-connector-python 库。请执行: pip install mysql-connector-python")
        sys.exit(1)

    # ---- 3. MD5 加密密码（与后端 MD5Util.java 保持一致）----
    print(">>> 正在加密新密码...")
    hashed_password = hashlib.md5(password.encode("utf-8")).hexdigest()
    print(f"    加密完成: {hashed_password[:20]}...")
    print()

    # ---- 4. 连接数据库 ----
    print(">>> 正在连接数据库...")
    try:
        conn = mysql.connector.connect(
            host=DB_CONFIG["host"],
            port=DB_CONFIG["port"],
            user=DB_CONFIG["user"],
            password=DB_CONFIG["password"],
            database=DB_CONFIG["database"],
            charset="utf8mb4",
        )
        cursor = conn.cursor()
        print("    数据库连接成功！")
    except Exception as e:
        print(f"错误：数据库连接失败 - {e}")
        sys.exit(1)
    print()

    # ---- 5. 查找用户 ----
    print(">>> 正在查找用户...")
    cursor.execute("SELECT id, account, user_name FROM `user` WHERE `account` = %s", (username,))
    user = cursor.fetchone()
    if not user:
        print(f"错误：用户 '{username}' 不存在，无法修改。请使用 create_test_user.py 先创建用户。")
        cursor.close()
        conn.close()
        sys.exit(1)

    user_id = user[0]
    user_name = user[2] or username
    print(f"    找到用户: id={user_id}, account={user[1]}, user_name={user_name}")
    print()

    # ---- 6. 检查并插入角色（如果不存在）----
    print(">>> 检查角色表...")
    cursor.execute("SELECT id, role_name FROM `role` WHERE id = %s", (role_id,))
    role_row = cursor.fetchone()
    if role_row:
        print(f"    角色已存在: id={role_row[0]}, name={role_row[1]}")
    else:
        print(f"    角色不存在，正在创建角色 (id={role_id}, name={role_name})...")
        cursor.execute(
            "INSERT INTO `role` (`id`, `role_name`, `description`) VALUES (%s, %s, %s)",
            (role_id, role_name, f"{role_name}角色"),
        )
        conn.commit()
        print(f"    角色创建成功！")
    print()

    # ---- 7. 更新密码 ----
    print(">>> 正在更新密码...")
    cursor.execute(
        "UPDATE `user` SET `password` = %s WHERE `id` = %s",
        (hashed_password, user_id),
    )
    conn.commit()
    print(f"    密码已更新！")
    print()

    # ---- 8. 更新角色关联 ----
    print(">>> 正在更新角色关联...")
    # 先删除该用户的所有旧角色关联
    cursor.execute("DELETE FROM `user_role` WHERE `user_id` = %s", (user_id,))
    # 插入新角色关联
    cursor.execute(
        "INSERT INTO `user_role` (`user_id`, `role_id`) VALUES (%s, %s)",
        (user_id, role_id),
    )
    conn.commit()
    print(f"    角色关联已更新！user_id={user_id}, role_id={role_id}")
    print()

    # ---- 9. 关闭连接 ----
    cursor.close()
    conn.close()

    # ---- 10. 输出结果 ----
    print("=" * 50)
    print("✅ 测试用户修改成功！")
    print("=" * 50)
    print(f"  用户名: {username}")
    print(f"  新密码: {password}")
    print(f"  新角色: {role_name} (role_id={role_id})")
    print(f"  用户ID: {user_id}")
    print("=" * 50)
    print(f"数据库: {DB_CONFIG['host']}:{DB_CONFIG['port']}/{DB_CONFIG['database']}")


if __name__ == "__main__":
    main()