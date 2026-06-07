#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
代码导出脚本
功能：默认导出 backend 和 frontend 目录下的源码文件（去除注释和空行）到 Markdown

用法：
    python export_code_to_md.py                          # 导出 backend + frontend
    python export_code_to_md.py -o code.md               # 指定输出文件
    python export_code_to_md.py <路径...> [-o 输出文件]    # 仅导出指定路径
"""

import argparse
import os
import re
import sys


def strip_java_comments(content: str) -> str:
    """去除 Java 风格注释：// 和 /* */"""
    # 先去除多行注释 /* ... */
    content = re.sub(r'/\*.*?\*/', '', content, flags=re.DOTALL)
    # 去除单行注释 //
    lines = []
    for line in content.split('\n'):
        if '//' in line:
            line = line[:line.index('//')]
        lines.append(line)
    return '\n'.join(lines)


def strip_python_comments(content: str) -> str:
    """去除 Python 风格注释：# 和 ''' ''' 以及 \"\"\" \"\"\" """
    # 去除多行字符串/注释
    content = re.sub(r'("""|\'\'\').*?\1', '', content, flags=re.DOTALL)
    # 去除单行注释 #
    lines = []
    for line in content.split('\n'):
        stripped = line.lstrip()
        if stripped.startswith('#'):
            line = line[:line.index('#')]
        elif '  #' in line:
            line = line[:line.rindex('  #')]
        elif '\t#' in line:
            line = line[:line.rindex('\t#')]
        lines.append(line)
    return '\n'.join(lines)


def strip_xml_comments(content: str) -> str:
    """去除 XML/HTML 注释：<!-- -->"""
    return re.sub(r'<!--.*?-->', '', content, flags=re.DOTALL)


def strip_sql_comments(content: str) -> str:
    """去除 SQL 注释：-- 和 /* */"""
    # 先去除多行注释
    content = re.sub(r'/\*.*?\*/', '', content, flags=re.DOTALL)
    # 去除单行注释 --
    lines = []
    for line in content.split('\n'):
        stripped = line.lstrip()
        if stripped.startswith('--'):
            line = line[:line.index('--')]
        lines.append(line)
    return '\n'.join(lines)


def strip_js_ts_comments(content: str) -> str:
    """去除 JavaScript/TypeScript 注释：// 和 /* */ """
    return strip_java_comments(content)


def strip_css_comments(content: str) -> str:
    """去除 CSS 注释：/* */ """
    return re.sub(r'/\*.*?\*/', '', content, flags=re.DOTALL)


COMMENT_STRIPPERS = {
    '.java': strip_java_comments,
    '.scala': strip_java_comments,
    '.kt': strip_java_comments,
    '.groovy': strip_java_comments,
    '.js': strip_js_ts_comments,
    '.ts': strip_js_ts_comments,
    '.jsx': strip_js_ts_comments,
    '.tsx': strip_js_ts_comments,
    '.vue': strip_js_ts_comments,
    '.py': strip_python_comments,
    '.xml': strip_xml_comments,
    '.html': strip_xml_comments,
    '.xhtml': strip_xml_comments,
    '.css': strip_css_comments,
    '.scss': strip_css_comments,
    '.less': strip_css_comments,
    '.c': strip_java_comments,
    '.cpp': strip_java_comments,
    '.h': strip_java_comments,
    '.hpp': strip_java_comments,
    '.cs': strip_java_comments,
    '.go': strip_java_comments,
    '.rs': strip_java_comments,
    '.swift': strip_java_comments,
}

LANGUAGE_MAP = {
    '.java': 'java', '.py': 'python', '.xml': 'xml',
    '.html': 'html', '.js': 'javascript', '.ts': 'typescript',
    '.jsx': 'jsx', '.tsx': 'tsx', '.vue': 'vue',
    '.css': 'css', '.scss': 'scss', '.less': 'less',
    '.scala': 'scala', '.kt': 'kotlin', '.groovy': 'groovy',
    '.c': 'c', '.cpp': 'cpp', '.h': 'c', '.hpp': 'cpp',
    '.cs': 'csharp', '.go': 'go', '.rs': 'rust',
    '.swift': 'swift',
}

CODE_EXTENSIONS = set(COMMENT_STRIPPERS.keys())


def strip_comments(content: str, ext: str) -> str:
    """根据文件扩展名去除注释"""
    stripper = COMMENT_STRIPPERS.get(ext)
    if stripper:
        return stripper(content)
    return content


def remove_blank_lines(content: str) -> str:
    """去除空行（只含空白字符的行）"""
    return '\n'.join(line for line in content.split('\n') if line.strip() != '')


def process_file(filepath: str, root_dir: str) -> str | None:
    """处理单个文件，返回 Markdown 格式的代码块字符串"""
    ext = os.path.splitext(filepath)[1].lower()
    if ext not in CODE_EXTENSIONS:
        return None

    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
    except UnicodeDecodeError:
        try:
            with open(filepath, 'r', encoding='gbk') as f:
                content = f.read()
        except Exception:
            print(f"警告：无法读取文件 {filepath}，已跳过", file=sys.stderr)
            return None

    # 去除注释
    content = strip_comments(content, ext)
    # 去除空行
    content = remove_blank_lines(content)

    if not content.strip():
        return None

    # 相对路径
    rel_path = os.path.relpath(filepath, root_dir).replace('\\', '/')
    lang = LANGUAGE_MAP.get(ext, '')

    return f"## {rel_path}\n\n```{lang}\n{content}\n```\n"


def collect_files(paths: list[str]) -> list[str]:
    """收集所有代码文件"""
    files = []
    for path in paths:
        if os.path.isfile(path):
            ext = os.path.splitext(path)[1].lower()
            if ext in CODE_EXTENSIONS:
                files.append(os.path.abspath(path))
        elif os.path.isdir(path):
            for root, dirs, filenames in os.walk(path):
                # 跳过隐藏目录和常见的非源码目录
                dirs[:] = [d for d in dirs if not d.startswith('.') and d not in (
                    '__pycache__', 'node_modules', 'target', 'build', 'dist',
                    'out', '.git', '.idea', '.vscode', 'venv', '.venv',
                    'resource', 'file', 'docs',
                )]
                for fname in filenames:
                    fpath = os.path.join(root, fname)
                    ext = os.path.splitext(fname)[1].lower()
                    if ext in CODE_EXTENSIONS:
                        files.append(os.path.abspath(fpath))
    return sorted(files)


def find_common_root(files: list[str]) -> str:
    """找到所有文件的公共根目录"""
    if not files:
        return os.getcwd()
    common = os.path.commonpath(files)
    if os.path.isfile(common):
        common = os.path.dirname(common)
    return common


def find_source_dirs() -> list[str]:
    """返回默认的源码目录（backend 和 frontend）"""
    script_dir = os.path.dirname(os.path.abspath(__file__))
    # backend/script -> backend -> 项目根
    parent = os.path.dirname(script_dir)
    project_root = os.path.dirname(parent)
    dirs = []
    for d in ['backend', 'frontend']:
        p = os.path.join(project_root, d)
        if os.path.isdir(p):
            dirs.append(p)
    return dirs


def main():
    parser = argparse.ArgumentParser(
        description='将代码文件去除注释和空行后导出为 Markdown 文件'
    )
    parser.add_argument(
        'paths', nargs='*',
        help='要导出的文件或目录路径（可多个，默认整个项目）'
    )
    parser.add_argument(
        '-o', '--output', default='code_export.md',
        help='输出的 Markdown 文件名（默认：code_export.md）'
    )
    args = parser.parse_args()

    if args.paths:
        paths = [os.path.abspath(p) for p in args.paths]
    else:
        paths = find_source_dirs()
        if not paths:
            paths = [os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))]

    for p in paths:
        if not os.path.exists(p):
            print(f"错误：路径不存在 - {p}", file=sys.stderr)
            sys.exit(1)

    files = collect_files(paths)
    if not files:
        print("未找到任何代码文件", file=sys.stderr)
        sys.exit(1)

    root_dir = find_common_root(files)
    output_path = os.path.abspath(args.output)

    # 排除输出文件自身
    files = [f for f in files if os.path.abspath(f) != output_path]

    print(f"找到 {len(files)} 个代码文件")
    print(f"根目录：{root_dir}")
    print(f"输出文件：{output_path}")

    md_blocks = []
    for f in files:
        block = process_file(f, root_dir)
        if block:
            md_blocks.append(block)

    with open(output_path, 'w', encoding='utf-8') as f:
        f.write('# 代码导出\n\n')
        f.write(f'共 {len(md_blocks)} 个文件\n\n---\n\n')
        f.write('\n---\n\n'.join(md_blocks))

    print(f"完成！已导出 {len(md_blocks)} 个文件到 {output_path}")


if __name__ == '__main__':
    main()
