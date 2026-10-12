#!/usr/bin/env python3
"""Read tracked files without executing repository code. Block incident indicators."""
import json
import pathlib
import re
import subprocess
import sys

FORBIDDEN_NAMES = {'fa-solid-300.llf', 'temp_auto_push.bat',
                   'temp_interactive_push.bat', 'branch_structure.json'}
HIDDEN_IGNORE = {'temp_auto_push.bat', 'temp_interactive_push.bat', 'branch_structure.json'}

def violations(path, data):
    """Return reasons without logging file contents or credentials."""
    lower = path.lower().replace('\\', '/')
    name = lower.rsplit('/', 1)[-1]
    problems = []
    if name in FORBIDDEN_NAMES or lower.endswith('.llf'):
        problems.append('incident payload or propagation filename')
    text = data.decode('utf-8', errors='replace')
    if ('.vscode/' in lower or lower.endswith('.code-workspace')):
        # Accept JSONC without evaluating it. Decode escaped strings too.
        strings = []
        for match in re.finditer(r'"(?:[^"\\]|\\.)*"', text):
            try:
                strings.append(json.loads(match.group()))
            except ValueError:
                pass
        if 'folderOpen' in strings:
            problems.append('editor task runs automatically when a folder opens')
        if re.search(r'"task\.allowAutomaticTasks"\s*:\s*(?:"on"|true)', text):
            problems.append('editor enables automatic tasks without approval')
        if 'task.allowAutomaticTasks' in strings and 'on' in strings:
            problems.append('editor enables automatic tasks without approval')
    if name == '.gitignore':
        for line in text.splitlines():
            if line.strip().lstrip('/').lower() in HIDDEN_IGNORE:
                problems.append('ignore rule conceals incident propagation tools')
    # The incident JavaScript is concealed behind hundreds of tabs, even if renamed.
    if re.match(rb'\s{100,}(?:global[.\[]|var\s+_0x|function\s+_0x)', data):
        problems.append('concealed incident JavaScript')
    font_headers = {'.woff': (b'wOFF',), '.woff2': (b'wOF2',),
                    '.ttf': (b'\x00\x01\x00\x00', b'true', b'ttcf'), '.otf': (b'OTTO',)}
    suffix = pathlib.PurePosixPath(lower).suffix
    if suffix in font_headers and not data.startswith(font_headers[suffix]):
        problems.append('font extension does not match a valid font header')
    if re.search(rb'-----BEGIN (?:RSA |EC |OPENSSH |DSA )?PRIVATE KEY-----', data):
        problems.append('private signing key committed to repository')
    if re.search(rb'\b(?:gh[pousr]_[A-Za-z0-9]{30,}|github_pat_[A-Za-z0-9_]{40,})\b', data):
        problems.append('GitHub access token committed to repository')
    return sorted(set(problems))

def main():
    root = pathlib.Path(subprocess.check_output(['git', 'rev-parse', '--show-toplevel'], text=True).strip())
    paths = subprocess.check_output(['git', 'ls-files', '-z']).decode('utf-8', errors='surrogateescape').split('\0')
    failures = []
    for path in filter(None, paths):
        target = root / path
        if target.is_symlink() or not target.is_file():
            # Never follow repository symlinks outside the checkout.
            continue
        # Inspect all small files, regardless of extension, including disguised fonts.
        if target.stat().st_size > 8 * 1024 * 1024:
            continue
        for reason in violations(path, target.read_bytes()):
            failures.append((path, reason))
    for path, reason in failures:
        print(json.dumps({'path': path, 'reason': reason}, ensure_ascii=True))
    print(f'Repository security: {len(failures)} violation(s), {len(paths) - 1} tracked files checked.')
    return 1 if failures else 0

if __name__ == '__main__':
    sys.exit(main())
