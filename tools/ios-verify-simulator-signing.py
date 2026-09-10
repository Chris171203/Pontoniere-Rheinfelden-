#!/usr/bin/env python3
"""Verify the host signature and the simulator entitlements embedded by Xcode.

Xcode puts iOS entitlements in Mach-O __TEXT,__entitlements; the separate
macOS ad-hoc signature can legitimately have an empty entitlement dictionary.
"""
from pathlib import Path
import json
import plistlib
import struct
import subprocess
import sys


def simulator_entitlements(executable):
    data = executable.read_bytes()
    byte_order = {b'\xcf\xfa\xed\xfe': '<', b'\xfe\xed\xfa\xcf': '>'}.get(data[:4])
    if byte_order is None:
        raise ValueError('Expected the selected simulator architecture as a 64-bit Mach-O executable')
    command_count = struct.unpack_from(byte_order + 'I', data, 16)[0]
    cursor = 32  # mach_header_64
    for _ in range(command_count):
        command, command_size = struct.unpack_from(byte_order + 'II', data, cursor)
        if command_size < 8 or cursor + command_size > len(data):
            raise ValueError('Malformed Mach-O load command')
        if command == 0x19:  # LC_SEGMENT_64, followed by section_64 records
            section_count = struct.unpack_from(byte_order + 'I', data, cursor + 64)[0]
            for index in range(section_count):
                section = cursor + 72 + index * 80
                if section + 80 > cursor + command_size:
                    raise ValueError('Malformed Mach-O section')
                name = data[section:section + 16].rstrip(b'\0')
                segment = data[section + 16:section + 32].rstrip(b'\0')
                if name == b'__entitlements' and segment == b'__TEXT':
                    size = struct.unpack_from(byte_order + 'Q', data, section + 40)[0]
                    offset = struct.unpack_from(byte_order + 'I', data, section + 48)[0]
                    if offset + size > len(data):
                        raise ValueError('Embedded entitlement section exceeds executable')
                    return plistlib.loads(data[offset:offset + size].rstrip(b'\0'))
        cursor += command_size
    raise ValueError('The actual app executable has no embedded simulator entitlements')


def main():
    app = Path(sys.argv[1])
    subprocess.run(['codesign', '--verify', '--strict', str(app)], check=True)
    signed = subprocess.run(['codesign', '-d', '--entitlements', ':-', str(app)], check=True, capture_output=True).stdout
    signed_values = plistlib.loads(signed) if signed.strip() else {}
    info = plistlib.loads((app / 'Info.plist').read_bytes())
    values = simulator_entitlements(app / info['CFBundleExecutable'])
    print('SIMULATOR-SIGNATURE: actual macOS ad-hoc signature verified; signature entitlement keys=' + ','.join(sorted(signed_values)))
    print('SIMULATOR-ENTITLEMENTS: actual Mach-O __TEXT,__entitlements=' + json.dumps(values, sort_keys=True), flush=True)
    if len(sys.argv) > 2:
        Path(sys.argv[2]).write_bytes(plistlib.dumps(values))
    identity = 'ch.pfvr.app.test'
    assert values.get('application-identifier') == identity, 'Unexpected embedded simulator application identity'
    assert identity in values.get('keychain-access-groups', []), 'Own embedded simulator Keychain group is missing'
    print('SIMULATOR-SIGNATURE: signature and embedded simulator Keychain fixture verification passed.')


if __name__ == '__main__':
    main()
