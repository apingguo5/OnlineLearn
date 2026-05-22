"""测试 /courses/** 文件下载"""
import requests

urls = [
    '/courses/Android%E5%BC%80%E5%8F%91%E5%9F%BA%E7%A1%80/01_%E8%AF%BE%E7%A8%8B%E5%AF%BC%E8%AE%BA/1.1_%E8%AF%BE%E7%A8%8B%E7%9B%AE%E6%A0%87%E4%B8%8E%E5%AD%A6%E4%B9%A0%E6%96%B9%E6%B3%95/reading.md',
    '/courses/Android%E5%BC%80%E5%8F%91%E5%9F%BA%E7%A1%80/intro.md',
    '/courses/Android%E5%BC%80%E5%8F%91%E5%9F%BA%E7%A1%80/README.md',
]

for u in urls:
    full = 'http://localhost:9251' + u
    print(f'\nGET {u}')
    r = requests.get(full, timeout=5)
    print(f'  status={r.status_code}')
    print(f'  CORS  ={r.headers.get("Access-Control-Allow-Origin", "MISSING")}')
    print(f'  CT    ={r.headers.get("Content-Type")}')
    print(f'  len   ={len(r.content)}')
    if r.status_code == 200:
        try:
            print(f'  head  ={r.content[:200].decode("utf-8", errors="replace")}')
        except:
            pass
