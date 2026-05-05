#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
批量用户导入脚本
功能：
  1. 将给定的用户名和密码通过 BCrypt 加密后，生成 SQL INSERT 语句
  2. 支持 CSV / JSON / 命令行参数 三种输入方式
  3. 可选直接连接数据库插入（需安装 mysql-connector-python）
  4. 自动生成 user 表 INSERT 语句及 user_role 关联表 INSERT 语句

用法：
  # 方式一：命令行参数（单个用户）
  python batch_user_import.py --account test001 --password 123456 --user_name "测试用户1" --role_id 2

  # 方式二：CSV 文件（批量）
  python batch_user_import.py --csv users.csv

  # 方式三：JSON 文件（批量）
  python batch_user_import.py --json users.json

  # 生成 SQL 文件到指定输出
  python batch_user_import.py --csv users.csv --output insert_users.sql

  # 直接插入数据库
  python batch_user_import.py --csv users.csv --db-host 192.168.1.38 --db-port 3306 --db-user newuser --db-pass yourpassword --db-name online_learn

示例 CSV 格式 (users.csv):
  account,password,user_name,phone,sex,description,role_id
  test001,123456,测试用户1,13800000001,0,测试账号,2
  test002,123456,测试用户2,13800000002,1,测试账号,2
  admin,admin123,管理员,13900000000,0,管理员账号,1

示例 JSON 格式 (users.json):
  [
    {"account": "test001", "password": "123456", "user_name": "测试用户1", "phone": "13800000001", "sex": 0, "description": "测试账号", "role_id": 2},
    {"account": "test002", "password": "123456", "user_name": "测试用户2", "phone": "13800000002", "sex": 1, "description": "测试账号", "role_id": 2}
  ]
"""

import argparse
import csv
import json
import sys
import os
from datetime import datetime

try:
    import bcrypt
except ImportError:
    print("错误: 缺少 bcrypt 库。请执行: pip install bcrypt")
    sys.exit(1)


def bcrypt_encrypt(password: str) -> str:
    """
    使用 BCrypt 对密码进行加密（自动生成 salt）
    Spring Security 默认使用 $2a$10$ 格式的 BCrypt，这里保持一致
    """
    if not password:
        raise ValueError("密码不能为空")
    salt = bcrypt.gensalt(rounds=10)
    hashed = bcrypt.hashpw(password.encode("utf-8"), salt)
    return hashed.decode("utf-8")


def generate_insert_sql(users: list, output_file: str = None) -> str:
    """
    根据用户列表生成 SQL INSERT 语句

    users 列表中的每个元素是一个字典，包含字段：
      - account (str): 登录账号
      - password (str): 明文密码（会被 BCrypt 加密）
      - user_name (str): 用户昵称
      - phone (str): 电话号码
      - sex (int): 性别（0-男, 1-女）
      - description (str): 个人描述
      - role_id (int): 角色ID（可选，如果提供则同时生成 user_role 表 INSERT）
    """
    now_str = datetime.now().strftime("%Y-%m-%d %H:%M:%S")

    sql_lines = []
    sql_lines.append("-- ============================================")
    sql_lines.append(f"-- 批量用户导入脚本自动生成")
    sql_lines.append(f"-- 生成时间: {now_str}")
    sql_lines.append(f"-- 用户数量: {len(users)}")
    sql_lines.append("-- ============================================")
    sql_lines.append("")

    # 生成 user 表 INSERT
    sql_lines.append("-- 1. 插入用户表 (user)")
    sql_lines.append("INSERT INTO `user` (`account`, `password`, `user_name`, `phone`, `sex`, `description`, `create_time`) VALUES")

    value_lines = []
    need_role_insert = False

    for i, user in enumerate(users):
        account = user.get("account", "").strip()
        plain_password = user.get("password", "").strip()
        user_name = user.get("user_name", "").strip() or account
        phone = user.get("phone", "").strip() or "NULL"
        sex = user.get("sex")
        description = user.get("description", "").strip() or "NULL"
        role_id = user.get("role_id")

        if not account:
            print(f"警告: 第 {i+1} 条记录缺少 account，跳过")
            continue
        if not plain_password:
            print(f"警告: 用户 '{account}' 缺少 password，跳过")
            continue

        # BCrypt 加密密码
        hashed_password = bcrypt_encrypt(plain_password)

        # 构造 SQL 值
        sex_val = "NULL" if sex is None else str(int(sex))
        phone_val = f"'{phone}'" if phone != "NULL" else "NULL"
        desc_val = f"'{description}'" if description != "NULL" else "NULL"

        value_lines.append(
            f"('{account}', '{hashed_password}', '{user_name}', {phone_val}, {sex_val}, {desc_val}, '{now_str}')"
        )

        # 记录 role_id 供后续生成 user_role INSERT
        if role_id is not None:
            user["_has_role"] = True
            need_role_insert = True
        else:
            user["_has_role"] = False

    if not value_lines:
        print("错误: 没有有效的用户数据可生成 SQL")
        return ""

    sql_lines.append(",\n".join(value_lines) + ";")
    sql_lines.append("")

    # 生成 user_role 表 INSERT
    if need_role_insert:
        sql_lines.append("-- 2. 插入用户角色关联表 (user_role)")
        sql_lines.append(
            "-- 注意: 这里假定 user 表中 id 自增，需要确保 SQL 执行顺序一致。\n"
            "-- 如果数据库已存在数据，请替换 user_id 的自增 ID。\n"
            "-- 建议在实际执行前手动确认 user_id 是否正确。"
        )

        # 使用可选的 user_id 映射：在 INSERT INTO user 时，
        # 我们可以使用 MySQL 变量或子查询来获取自增 ID
        # 但最简单的方法是分别插入，然后手动关联
        # 这里使用子查询方式自动匹配
        role_values = []
        for i, user in enumerate(users):
            account = user.get("account", "").strip()
            role_id = user.get("role_id")
            if account and role_id is not None and user.get("_has_role", False):
                role_values.append(
                    f"((SELECT `id` FROM `user` WHERE `account` = '{account}' LIMIT 1), {int(role_id)})"
                )

        if role_values:
            sql_lines.append("INSERT INTO `user_role` (`user_id`, `role_id`) VALUES")
            sql_lines.append(",\n".join(role_values) + ";")
            sql_lines.append("")

    sql_lines.append("-- ============================================")
    sql_lines.append("-- 生成完毕")
    sql_lines.append("-- ============================================")

    result = "\n".join(sql_lines)

    if output_file:
        os.makedirs(os.path.dirname(output_file) or ".", exist_ok=True)
        with open(output_file, "w", encoding="utf-8") as f:
            f.write(result)
        print(f"SQL 文件已生成: {os.path.abspath(output_file)}")
    else:
        print(result)

    return result


def parse_csv(file_path: str) -> list:
    """解析 CSV 文件为用户列表"""
    users = []
    with open(file_path, "r", encoding="utf-8-sig") as f:
        reader = csv.DictReader(f)
        for row in reader:
            # 去除 key 和 value 两端的空白
            clean_row = {}
            for k, v in row.items():
                key = k.strip() if k else ""
                clean_row[key] = v.strip() if v else ""

            # 处理数字字段
            if "sex" in clean_row and clean_row["sex"]:
                try:
                    clean_row["sex"] = int(clean_row["sex"])
                except ValueError:
                    clean_row["sex"] = None
            if "role_id" in clean_row and clean_row["role_id"]:
                try:
                    clean_row["role_id"] = int(clean_row["role_id"])
                except ValueError:
                    clean_row["role_id"] = None

            users.append(clean_row)
    return users


def parse_json(file_path: str) -> list:
    """解析 JSON 文件为用户列表"""
    with open(file_path, "r", encoding="utf-8") as f:
        data = json.load(f)
    if not isinstance(data, list):
        raise ValueError("JSON 文件格式错误，应为数组格式")
    return data


def insert_to_database(users: list, db_config: dict):
    """
    直接连接数据库插入用户数据
    需要安装: pip install mysql-connector-python
    """
    try:
        import mysql.connector
    except ImportError:
        print("错误: 缺少 mysql-connector-python 库。请执行: pip install mysql-connector-python")
        sys.exit(1)

    conn = mysql.connector.connect(
        host=db_config.get("host", "localhost"),
        port=db_config.get("port", 3306),
        user=db_config.get("user"),
        password=db_config.get("password"),
        database=db_config.get("database"),
        charset="utf8mb4",
    )
    cursor = conn.cursor()
    now_str = datetime.now().strftime("%Y-%m-%d %H:%M:%S")

    inserted = 0
    try:
        for i, user in enumerate(users):
            account = user.get("account", "").strip()
            plain_password = user.get("password", "").strip()
            user_name = user.get("user_name", "").strip() or account
            phone = user.get("phone", "").strip()
            sex = user.get("sex")
            description = user.get("description", "").strip()
            role_id = user.get("role_id")

            if not account or not plain_password:
                print(f"警告: 第 {i+1} 条记录缺少 account 或 password，跳过")
                continue

            # BCrypt 加密
            hashed_password = bcrypt_encrypt(plain_password)

            # 插入 user 表
            insert_user_sql = """
                INSERT INTO `user` (`account`, `password`, `user_name`, `phone`, `sex`, `description`, `create_time`)
                VALUES (%s, %s, %s, %s, %s, %s, %s)
            """
            cursor.execute(insert_user_sql, (
                account, hashed_password, user_name,
                phone if phone else None,
                sex,
                description if description else None,
                now_str
            ))

            # 如果有 role_id，插入 user_role 表
            if role_id is not None:
                user_id = cursor.lastrowid
                insert_role_sql = """
                    INSERT INTO `user_role` (`user_id`, `role_id`)
                    VALUES (%s, %s)
                """
                cursor.execute(insert_role_sql, (user_id, int(role_id)))

            inserted += 1
            print(f"✓ 已插入: {account} (user_name: {user_name})")

        conn.commit()
        print(f"\n成功插入 {inserted} 条用户记录到数据库")

    except Exception as e:
        conn.rollback()
        print(f"\n错误: 数据库操作失败，已回滚。原因: {e}")
        raise
    finally:
        cursor.close()
        conn.close()


def main():
    parser = argparse.ArgumentParser(
        description="批量用户导入工具 - BCrypt 密码加密 & SQL 生成 / 数据库直插",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
使用示例:
  # 命令行单用户
  python batch_user_import.py --account test001 --password 123456 --user_name "测试用户1" --role_id 2

  # CSV 批量 - 生成 SQL
  python batch_user_import.py --csv users.csv

  # CSV 批量 - 生成 SQL 到文件
  python batch_user_import.py --csv users.csv --output insert_users.sql

  # JSON 批量 - 直接插入数据库
  python batch_user_import.py --json users.json --db-host localhost --db-user root --db-pass yourpass --db-name online_learn
        """
    )

    # 输入方式（三选一）
    input_group = parser.add_argument_group("输入方式（三选一）")
    input_group.add_argument("--account", type=str, help="单个用户的登录账号")
    input_group.add_argument("--password", type=str, help="单个用户的明文密码")
    input_group.add_argument("--user_name", type=str, help="单个用户的昵称/真实姓名")
    input_group.add_argument("--phone", type=str, help="单个用户的电话号码")
    input_group.add_argument("--sex", type=int, choices=[0, 1], help="单个用户的性别（0-男, 1-女）")
    input_group.add_argument("--description", type=str, help="单个用户的个人描述")
    input_group.add_argument("--role_id", type=int, help="单个用户的角色ID")
    input_group.add_argument("--csv", type=str, help="CSV 文件路径（批量导入）")
    input_group.add_argument("--json", type=str, help="JSON 文件路径（批量导入）")

    # 输出方式
    output_group = parser.add_argument_group("输出选项")
    output_group.add_argument("--output", "-o", type=str, help="输出 SQL 文件路径（默认输出到控制台）")

    # 数据库连接（可选）
    db_group = parser.add_argument_group("数据库直连（可选，安装 mysql-connector-python 后可用）")
    db_group.add_argument("--db-host", type=str, default="localhost", help="数据库主机地址")
    db_group.add_argument("--db-port", type=int, default=3306, help="数据库端口")
    db_group.add_argument("--db-user", type=str, help="数据库用户名")
    db_group.add_argument("--db-pass", type=str, help="数据库密码")
    db_group.add_argument("--db-name", type=str, help="数据库名称")

    args = parser.parse_args()

    # ===== 收集用户数据 =====
    users = []

    # 方式1: CSV 文件
    if args.csv:
        if not os.path.exists(args.csv):
            print(f"错误: CSV 文件不存在: {args.csv}")
            sys.exit(1)
        users = parse_csv(args.csv)
        print(f"从 CSV 文件读取了 {len(users)} 条用户记录")

    # 方式2: JSON 文件
    elif args.json:
        if not os.path.exists(args.json):
            print(f"错误: JSON 文件不存在: {args.json}")
            sys.exit(1)
        users = parse_json(args.json)
        print(f"从 JSON 文件读取了 {len(users)} 条用户记录")

    # 方式3: 命令行参数（单用户）
    elif args.account and args.password:
        user = {
            "account": args.account,
            "password": args.password,
            "user_name": args.user_name or args.account,
            "phone": args.phone or "",
            "sex": args.sex,
            "description": args.description or "",
        }
        if args.role_id is not None:
            user["role_id"] = args.role_id
        users = [user]
        print(f"命令行输入: account={args.account}")

    else:
        parser.print_help()
        print("\n错误: 请提供用户数据。使用 --csv / --json / --account+--password 三种方式之一。")
        sys.exit(1)

    if not users:
        print("错误: 没有有效的用户数据")
        sys.exit(1)

    # 显示用户摘要
    print(f"\n待处理用户: {len(users)} 个")
    for u in users:
        role_info = f", role_id={u.get('role_id')}" if u.get("role_id") else ""
        print(f"  - {u.get('account', '?')} ({u.get('user_name', '?')}){role_info}")
    print()

    # ===== 执行输出 =====
    # 是否直连数据库
    if args.db_user and args.db_pass and args.db_name:
        db_config = {
            "host": args.db_host,
            "port": args.db_port,
            "user": args.db_user,
            "password": args.db_pass,
            "database": args.db_name,
        }
        print(">>> 模式: 直连数据库插入 <<<")
        insert_to_database(users, db_config)
    else:
        # 生成 SQL
        print(">>> 模式: 生成 SQL 语句 <<<")
        generate_insert_sql(users, args.output)

    print("\n完成!")


if __name__ == "__main__":
    main()