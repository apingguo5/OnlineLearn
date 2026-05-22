"""
执行 SQL 脚本到远程 MySQL（替代本地 mysql 客户端缺失的情况）
用法: python run_sql.py <sql文件路径>
"""
import sys
import os
import re

DB_CONFIG = {
    'host': '192.168.1.38',
    'port': 3306,
    'user': 'newuser',
    'password': 'yourpassword',
    'database': 'online_learn',
    'charset': 'utf8mb4',
}


def split_sql_statements(sql_text: str):
    """
    按 ; 分割 SQL 语句，但忽略字符串字面量、注释中的分号
    并去掉空语句
    """
    # 去掉单行注释 --
    cleaned_lines = []
    for line in sql_text.splitlines():
        stripped = line.lstrip()
        if stripped.startswith('--'):
            continue
        cleaned_lines.append(line)
    text = '\n'.join(cleaned_lines)

    # 简易分号分割（项目脚本里没有 stored procedure / DELIMITER）
    statements = []
    buf = []
    in_squote = False
    in_dquote = False
    in_bquote = False
    for ch in text:
        if ch == "'" and not in_dquote and not in_bquote:
            in_squote = not in_squote
        elif ch == '"' and not in_squote and not in_bquote:
            in_dquote = not in_dquote
        elif ch == '`' and not in_squote and not in_dquote:
            in_bquote = not in_bquote
        if ch == ';' and not in_squote and not in_dquote and not in_bquote:
            stmt = ''.join(buf).strip()
            if stmt:
                statements.append(stmt)
            buf = []
        else:
            buf.append(ch)
    tail = ''.join(buf).strip()
    if tail:
        statements.append(tail)
    return statements


def run(sql_path: str):
    import pymysql
    with open(sql_path, 'r', encoding='utf-8') as f:
        sql_text = f.read()
    statements = split_sql_statements(sql_text)
    print(f'[INFO] 解析到 {len(statements)} 条 SQL 语句')

    conn = pymysql.connect(**DB_CONFIG)
    cursor = conn.cursor()
    success = 0
    failed = 0
    select_results = []
    try:
        for idx, stmt in enumerate(statements, 1):
            short = stmt[:80].replace('\n', ' ')
            try:
                cursor.execute(stmt)
                # 如果是 SELECT，收集结果
                if re.match(r'^\s*SELECT', stmt, re.IGNORECASE):
                    rows = cursor.fetchall()
                    cols = [d[0] for d in cursor.description] if cursor.description else []
                    select_results.append((short, cols, rows))
                else:
                    affected = cursor.rowcount
                    if affected > 0:
                        print(f'  [{idx:02d}] OK  affected={affected} | {short}...')
                    else:
                        print(f'  [{idx:02d}] OK              | {short}...')
                success += 1
            except Exception as e:
                failed += 1
                print(f'  [{idx:02d}] ERR | {short}...')
                print(f'         {e}')
        conn.commit()
    finally:
        cursor.close()
        conn.close()

    print('\n' + '=' * 60)
    print(f'执行完成：成功 {success} 条，失败 {failed} 条')
    print('=' * 60)

    if select_results:
        print('\n📊 SELECT 验证结果：')
        for short, cols, rows in select_results:
            print(f'\n>>> {short}')
            if cols:
                print('    ' + ' | '.join(cols))
                print('    ' + '-' * (len(' | '.join(cols))))
            for r in rows:
                print('    ' + ' | '.join(str(x) for x in r))


if __name__ == '__main__':
    if len(sys.argv) < 2:
        print('用法: python run_sql.py <sql文件路径>')
        sys.exit(1)
    sql_path = sys.argv[1]
    if not os.path.exists(sql_path):
        print(f'文件不存在: {sql_path}')
        sys.exit(1)
    run(sql_path)
