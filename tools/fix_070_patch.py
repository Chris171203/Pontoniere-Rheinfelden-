from pathlib import Path

p = Path('tools/apply_070.py')
s = p.read_text(encoding='utf-8')
old = '    s = s[:i] + replacement + s[j:]\n'
new = '    s = s[:i] + replacement + s[j+len(end):]\n'
if old in s:
    s = s.replace(old, new, 1)
elif new not in s:
    raise SystemExit('replace_between implementation not found')
p.write_text(s, encoding='utf-8')
print('Fixed 0.7.0 patch boundaries')
