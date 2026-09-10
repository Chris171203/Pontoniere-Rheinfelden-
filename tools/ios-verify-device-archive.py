#!/usr/bin/env python3
"""Verify the actual archived product; this is not signing or App Store validation."""
import json
from pathlib import Path
import plistlib
import struct
import subprocess
import sys


def has_macho_section(executable, segment_name, section_name):
    """Return whether a thin 64-bit Mach-O has the named section."""
    data = executable.read_bytes()
    byte_order = {b'\xcf\xfa\xed\xfe': '<', b'\xfe\xed\xfa\xcf': '>'}.get(data[:4])
    if byte_order is None:
        raise ValueError('Expected a thin 64-bit Mach-O executable')
    command_count = struct.unpack_from(byte_order + 'I', data, 16)[0]
    cursor = 32
    for _ in range(command_count):
        if cursor + 8 > len(data):
            raise ValueError('Malformed Mach-O load command')
        command, command_size = struct.unpack_from(byte_order + 'II', data, cursor)
        if command_size < 8 or cursor + command_size > len(data):
            raise ValueError('Malformed Mach-O load command')
        if command == 0x19:  # LC_SEGMENT_64
            if command_size < 72:
                raise ValueError('Malformed Mach-O segment command')
            section_count = struct.unpack_from(byte_order + 'I', data, cursor + 64)[0]
            for index in range(section_count):
                section = cursor + 72 + index * 80
                if section + 80 > cursor + command_size:
                    raise ValueError('Malformed Mach-O section')
                name = data[section:section + 16].rstrip(b'\0')
                segment = data[section + 16:section + 32].rstrip(b'\0')
                if name == section_name and segment == segment_name:
                    return True
        cursor += command_size
    return False


def verify(archive):
    archive = Path(archive)
    metadata = plistlib.loads((archive / 'Info.plist').read_bytes())
    properties = metadata['ApplicationProperties']
    relative = Path(properties['ApplicationPath'])
    if relative.is_absolute() or '..' in relative.parts:
        raise ValueError('Invalid archive application path')
    app = archive / 'Products' / relative
    info = plistlib.loads((app / 'Info.plist').read_bytes())
    expected = {'CFBundleIdentifier': 'ch.pfvr.app.test',
                'CFBundleShortVersionString': '0.12.6',
                'CFBundlePackageType': 'APPL',
                'CFBundleSupportedPlatforms': ['iPhoneOS'],
                'UIDeviceFamily': [1, 2], 'MinimumOSVersion': '17.0'}
    for key, value in expected.items():
        if info.get(key) != value:
            raise ValueError(f'{key}: expected {value!r}, got {info.get(key)!r}')
    if not (app / 'PrivacyInfo.xcprivacy').is_file():
        raise ValueError('Privacy manifest missing from device product')
    for key in ['CFBundleIcons', 'CFBundleIcons~ipad']:
        icon = info.get(key, {}).get('CFBundlePrimaryIcon', {})
        if icon.get('CFBundleIconName') != 'AppIcon' or not icon.get('CFBundleIconFiles'):
            raise ValueError(f'{key}: compiled AppIcon metadata missing')
    if not (app / 'Assets.car').is_file():
        raise ValueError('Compiled asset catalog missing')
    executable = app / info['CFBundleExecutable']
    architectures = subprocess.check_output(['xcrun', 'lipo', '-archs', str(executable)], text=True).strip()
    if architectures != 'arm64':
        raise ValueError(f'Expected arm64 device executable, got {architectures}')
    build = subprocess.check_output(['xcrun', 'vtool', '-show-build', str(executable)], text=True)
    platforms = [line.split()[-1] for line in build.splitlines() if line.strip().startswith('platform ')]
    if platforms != ['IOS']:
        raise ValueError(f'Expected Mach-O platform IOS, got {platforms}')
    # Simulator-only entitlements are embedded as a real Mach-O section. Check
    # the section table so an arbitrary string cannot trigger a false positive,
    # and so the verifier cannot miss the actual simulator entitlement names.
    if has_macho_section(executable, b'__TEXT', b'__entitlements'):
        raise ValueError('Unexpected embedded simulator/ad-hoc entitlement section')
    if (app / 'embedded.mobileprovision').exists() or (app / '_CodeSignature').exists():
        raise ValueError('This CI archive must contain no signing/provisioning material')
    return {'status': 'passed', 'bundle': info['CFBundleIdentifier'],
            'version': info['CFBundleShortVersionString'], 'build': info['CFBundleVersion'],
            'platform': platforms[0], 'architecture': architectures,
            'minimum_ios': info['MinimumOSVersion'], 'device_families': info['UIDeviceFamily'],
            'app_icon': 'AppIcon', 'signed': False, 'installable_ipa': False, 'store_validation': 'not performed'}


if __name__ == '__main__':
    print(json.dumps(verify(sys.argv[1]), indent=2, sort_keys=True))
