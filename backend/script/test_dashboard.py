"""测试 dashboard/stats 接口（教师 userId=1, 2 测试）"""
import requests, json
for uid in [1, 2, 3]:
    print(f'\n=== userId={uid} ===')
    r = requests.post('http://localhost:9251/study/teacher/dashboard/stats',
                      json={'userId': uid}, timeout=10)
    print(f'status={r.status_code}')
    try:
        data = r.json()
        if data.get('code') == 200:
            d = data['resultData']
            print(f"  课程数: {d.get('courseCount')}")
            print(f"  班级数: {d.get('classCount')}")
            print(f"  学生数: {d.get('studentCount')}")
            print(f"  待批改: {d.get('pendingHomeworkCount')}")
            print(f"  待回复: {d.get('pendingQuestionCount')}")
            print(f"  即将截止: {d.get('nearDueHomeworkCount')}")
            print(f"  最近课程: {[c.get('courseName') + ' (班级' + str(c.get('classCount')) + '/学生' + str(c.get('studentCount')) + ')' for c in (d.get('recentCourses') or [])]}")
            print(f"  班级概况: {[(c.get('className'), c.get('studentCount'), c.get('completionRate')) for c in (d.get('classOverview') or [])]}")
        else:
            print(f"  body={r.text[:300]}")
    except Exception as e:
        print(f'  parse err: {e}, body={r.text[:200]}')
