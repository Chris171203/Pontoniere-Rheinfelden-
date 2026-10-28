#!/usr/bin/env python3
"""Fail the Play release if the built AAB's merged manifest drifts from policy."""
import argparse
from pathlib import Path
import xml.etree.ElementTree as ET

ANDROID = "{http://schemas.android.com/apk/res/android}"
ALLOWED_PERMISSIONS = {
    "android.permission.INTERNET",
    "android.permission.ACCESS_NETWORK_STATE",
    "android.permission.WAKE_LOCK",
    "android.permission.RECEIVE_BOOT_COMPLETED",
    "android.permission.FOREGROUND_SERVICE",
}

def verify(path: Path, version_name: str, version_code: str) -> set[str]:
    root = ET.parse(path).getroot()
    if root.tag != "manifest" or root.get("package") != "ch.pfvr.app":
        raise ValueError("unexpected release package")
    if root.get(ANDROID + "versionName") != version_name or root.get(ANDROID + "versionCode") != version_code:
        raise ValueError("release version differs from Gradle definition")
    permissions = {
        item.get(ANDROID + "name")
        for item in root
        if item.tag in {"uses-permission", "uses-permission-sdk-23"}
    }
    if "android.permission.INTERNET" not in permissions:
        raise ValueError("expected INTERNET permission missing")
    unexpected = permissions - ALLOWED_PERMISSIONS
    if unexpected:
        raise ValueError("unexpected release permissions: " + ", ".join(sorted(unexpected)))
    return permissions

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("manifest", type=Path)
    parser.add_argument("version_name")
    parser.add_argument("version_code")
    args = parser.parse_args()
    permissions = verify(args.manifest, args.version_name, args.version_code)
    print("Release manifest verified: ch.pfvr.app, " + args.version_name + " (" + args.version_code + "), " + ", ".join(sorted(permissions)))

if __name__ == "__main__":
    main()
