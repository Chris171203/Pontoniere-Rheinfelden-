from pathlib import Path


def replace_once(text, old, new, label):
    count=text.count(old)
    if count!=1: raise SystemExit(f"{label}: expected 1 occurrence, got {count}")
    return text.replace(old,new,1)

main_path=Path("Android/app/src/main/java/ch/pfvr/internapp/MainActivity.java")
main=main_path.read_text()
main=replace_once(
    main,
    "document.querySelector('.pfvr-attendance-mobile')",
    "document.querySelector('.pfvr-attendance-mobile .pfvr-attendance-matrix')",
    "wait for completed attendance matrix"
)
main_path.write_text(main)

test_path=Path("Android/app/src/test/java/ch/pfvr/internapp/InternalAppViewSourceTest.java")
test=test_path.read_text()
test=replace_once(
    test,
    "document.querySelector('.pfvr-attendance-mobile')",
    "document.querySelector('.pfvr-attendance-mobile .pfvr-attendance-matrix')",
    "readiness source assertion"
)
test_path.write_text(test)
