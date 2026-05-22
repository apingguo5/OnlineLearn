"""直接调用接口，看实际错误"""
import requests
import json

BASE = 'http://localhost:9251'

# 测试父章节 id=5 和叶子章节 id=6
for cid in [5, 6, 8, 10]:
    print(f'\n=== chapterId={cid} ===')
    try:
        r = requests.post(f'{BASE}/study/student/course/chapterContents',
                          headers={'Content-Type': 'application/json'},
                          data=json.dumps({'chapterId': cid}),
                          timeout=10)
        print(f'  status={r.status_code}')
        print(f'  body={r.text[:600]}')
    except Exception as e:
        print(f'  EXCEPTION: {e}')
