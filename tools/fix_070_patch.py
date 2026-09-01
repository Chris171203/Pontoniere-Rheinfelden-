from pathlib import Path

p = Path('tools/apply_070.py')
s = p.read_text(encoding='utf-8')

# Replacement blocks already include their following method marker, so consume
# that marker from the original source instead of duplicating it.
old = '    s = s[:i] + replacement + s[j:]\n'
new = '    s = s[:i] + replacement + s[j+len(end):]\n'
if old in s:
    s = s.replace(old, new, 1)
elif new not in s:
    raise SystemExit('replace_between implementation not found')

# apply_070.py contains Java source inside Python triple-quoted strings.
# A single \n in that Python source becomes a real newline while generating
# Java. Double it here so the resulting Java keeps the intended "\\n" escape.
fixes = [
    (r'+"\nWind ', r'+"\\nWind '),
    (r'details+="\nUV ', r'details+="\\nUV '),
    (r'sub.append("\n")', r'sub.append("\\n")'),
    (
        r'amountLine+"\n"+CLUB_PAYEE+"\n"+CLUB_IBAN+"\n"+CLUB_PAYMENT_NOTE',
        r'amountLine+"\\n"+CLUB_PAYEE+"\\n"+CLUB_IBAN+"\\n"+CLUB_PAYMENT_NOTE',
    ),
    (r'"Rheinweg 42\n4310 Rheinfelden"', r'"Rheinweg 42\\n4310 Rheinfelden"'),
]
for before, after in fixes:
    if before in s:
        s = s.replace(before, after, 1)
    elif after not in s:
        raise SystemExit(f'newline escape target not found: {before}')

p.write_text(s, encoding='utf-8')
print('Fixed 0.7.0 patch boundaries and Java newline escapes')
