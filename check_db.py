import subprocess
result = subprocess.run([
    'mysql', '-h', '192.168.1.38', '-u', 'newuser', '-pyourpassword',
    '-e', "SELECT id, resource_name, file_url FROM online_learn.course_resource WHERE file_url LIKE '%.webm%' LIMIT 10"
], capture_output=True, text=True)
print('STDOUT:', result.stdout)
print('STDERR:', result.stderr)