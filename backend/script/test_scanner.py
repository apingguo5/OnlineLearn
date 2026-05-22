"""测试 course-scanner/tree 接口"""
import requests, json
r = requests.get('http://localhost:9251/study/teacher/course-scanner/tree', timeout=5)
print(f'status: {r.status_code}')
data = r.json()
print(json.dumps(data, indent=2, ensure_ascii=False)[:3000])
