import sys
# 检查 course 表中是否有数据
try:
    import pymysql
    conn = pymysql.connect(host='192.168.1.38', user='newuser', password='yourpassword', database='online_learn')
    cursor = conn.cursor()
    cursor.execute("SELECT id, course_name, creator_id, status FROM online_learn.course LIMIT 20")
    rows = cursor.fetchall()
    print(f"Found {len(rows)} courses:")
    for row in rows:
        print(f"  id={row[0]}, name={row[1]}, creator_id={row[2]}, status={row[3]}")
    cursor.execute("SELECT COUNT(*) FROM online_learn.course")
    total = cursor.fetchone()[0]
    print(f"Total courses: {total}")
    cursor.close()
    conn.close()
except ImportError:
    print("pymysql not installed, trying mysql-connector...")
    try:
        import mysql.connector
        conn = mysql.connector.connect(host='192.168.1.38', user='newuser', password='yourpassword', database='online_learn')
        cursor = conn.cursor()
        cursor.execute("SELECT id, course_name, creator_id, status FROM online_learn.course LIMIT 20")
        rows = cursor.fetchall()
        print(f"Found {len(rows)} courses:")
        for row in rows:
            print(f"  id={row[0]}, name={row[1]}, creator_id={row[2]}, status={row[3]}")
        cursor.execute("SELECT COUNT(*) FROM online_learn.course")
        total = cursor.fetchone()[0]
        print(f"Total courses: {total}")
        cursor.close()
        conn.close()
    except ImportError:
        print("Neither pymysql nor mysql-connector is installed. Cannot check database.")
        print("Install with: pip install pymysql")