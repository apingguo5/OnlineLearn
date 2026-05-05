#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
测试用户创建脚本
功能：将单个测试用户存入后端数据库，每次创建用户时同时插入 user_role 角色关联

用法：
    python create_test_user.py <username> <password> <role>

参数：
    username  登录账号
    password  登录密码
    role      用户角色（1=管理员, 2=教师, 3=学生）

示例：
    python create_test_user.py admin admin123 1
    python create_test_user.py teacher 123456 2
    python create_test_user.py student 123456 3
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
        print("用法：python create_test_user.py <username> <password> <role>")
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
    print(f"准备创建测试用户：")
    print(f"  用户名: {username}")
    print(f"  密码: {password}")
    print(f"  角色: {role_name} (role_id={role_id})")
    print()

    # ---- 2. 导入依赖 ----
    try:
        import bcrypt
    except ImportError:
        print("错误：缺少 bcrypt 库。请执行: pip install bcrypt")
        sys.exit(1)

    try:
        import mysql.connector
    except ImportError:
        print("错误：缺少 mysql-connector-python 库。请执行: pip install mysql-connector-python")
        sys.exit(1)

    # ---- 3. BCrypt 加密密码（兼容 Spring Security）----
    print(">>> 正在加密密码...")
    salt = bcrypt.gensalt(rounds=10)
    hashed = bcrypt.hashpw(password.encode("utf-8"), salt).decode("utf-8")
    # Python bcrypt 默认使用 $2b$ 前缀，Spring Security 使用 $2a$ 前缀
    # 将 $2b$ 替换为 $2a$ 以确保兼容
    if hashed.startswith("$2b$"):
        hashed_password = "$2a$" + hashed[4:]
    else:
        hashed_password = hashed
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

    # ---- 5. 检查并插入角色（如果不存在）----
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

    # ---- 6. 插入用户 ----
    print(">>> 正在插入用户...")
    now_str = datetime.now().strftime("%Y-%m-%d %H:%M:%S")

    # 如果用户已存在，先删除（级联删除 user_role 关联）
    cursor.execute("SELECT id FROM `user` WHERE `account` = %s", (username,))
    existing = cursor.fetchone()
    if existing:
        print(f"    用户 '{username}' 已存在 (id={existing[0]})，正在重建...")
        cursor.execute("DELETE FROM `user` WHERE `id` = %s", (existing[0],))
        conn.commit()
        print(f"    已删除旧用户记录")

    cursor.execute(
        "INSERT INTO `user` (`account`, `password`, `user_name`, `create_time`) VALUES (%s, %s, %s, %s)",
        (username, hashed_password, username, now_str),
    )
    user_id = cursor.lastrowid
    conn.commit()
    print(f"    用户创建成功！user_id={user_id}")
    print()

    # ---- 7. 插入用户角色关联（user_role）----
    print(">>> 正在插入用户角色关联...")
    cursor.execute(
        "INSERT INTO `user_role` (`user_id`, `role_id`) VALUES (%s, %s)",
        (user_id, role_id),
    )
    conn.commit()
    print(f"    角色关联创建成功！user_id={user_id}, role_id={role_id}")
    print()

    # ---- 8. 关闭连接 ----
    cursor.close()
    conn.close()

    # ---- 9. 输出结果 ----
    print("=" * 50)
    print("✅ 测试用户创建成功！")
    print("=" * 50)
    print(f"  用户名: {username}")
    print(f"  密码: {password}")
    print(f"  角色: {role_name} (role_id={role_id})")
    print(f"  用户ID: {user_id}")
    print("=" * 50)
    print(f"数据库: {DB_CONFIG['host']}:{DB_CONFIG['port']}/{DB_CONFIG['database']}")


if __name__ == "__main__":
    main()