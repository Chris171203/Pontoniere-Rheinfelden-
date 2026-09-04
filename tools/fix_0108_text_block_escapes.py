from pathlib import Path

path = Path("Android/app/src/main/java/ch/pfvr/internapp/InternalAttendanceSkin.java")
text = path.read_text(encoding="utf-8")

replacements = [
    (
        r".replace(/[\u{1F300}-\u{1FAFF}]/gu,' ')",
        r".replace(/[\\u{1F300}-\\u{1FAFF}]/gu,' ')",
        1,
        "Unicode range in Java text block",
    ),
    (
        r".replace(/\s*,\s*/g,', ')",
        r".replace(/\\s*,\\s*/g,', ')",
        1,
        "comma whitespace regex",
    ),
    (
        r".replace(/\s+/g,' ')",
        r".replace(/\\s+/g,' ')",
        2,
        "whitespace regexes",
    ),
    (
        r"/^(?:person|teilnehmer)\s+\d+$/i",
        r"/^(?:person|teilnehmer)\\s+\\d+$/i",
        1,
        "placeholder person regex",
    ),
    (
        r"/[^a-z0-9\s]/g",
        r"/[^a-z0-9\\s]/g",
        1,
        "person token regex",
    ),
]

for old, new, expected, label in replacements:
    count = text.count(old)
    if count != expected:
        raise RuntimeError(f"{label}: expected {expected} occurrence(s), found {count}")
    text = text.replace(old, new)

path.write_text(text, encoding="utf-8")
print("Fixed Java text-block escapes for embedded JavaScript regexes")
