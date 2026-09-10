#!/usr/bin/env python3
"""Select an iPhone size or iPad on an installed runtime supported by the selected Xcode SDK."""
import argparse
import json
import re
import subprocess
import sys


def select(devices, profile, sdk_version):
    def version(runtime):
        return tuple(int(n) for n in re.findall(r'\d+', runtime))

    for runtime in sorted(devices, key=version, reverse=True):
        if '.iOS-' not in runtime:
            continue
        runtime_version = version(runtime)[:2]
        # simctl lists runtimes installed by other Xcode versions too. A newer
        # runtime may be bootable while actool/xcodebuild cannot target it.
        if runtime_version < (17, 0) or runtime_version > version(sdk_version)[:2]:
            continue
        if profile == 'tablet':
            tablets = [d for d in devices[runtime] if d.get('isAvailable') and d['name'].startswith('iPad')]
            if tablets:
                return runtime, min(tablets, key=lambda d: ('11' not in d['name'], d['name']))
            continue
        phones = [d for d in devices[runtime] if d.get('isAvailable') and d['name'].startswith('iPhone')]
        small = [d for d in phones if not any(s in d['name'] for s in ('Max', 'Plus', 'Air'))]
        large = [d for d in phones if any(s in d['name'] for s in ('Max', 'Plus'))]
        if not small or not large:
            continue
        # Prefer an actual small screen when the runner supplies one; otherwise a standard iPhone.
        def compact_key(d):
            n = d['name']
            return (0 if 'SE' in n else 1 if 'mini' in n else 2 if n.endswith('e') else 3, n)
        device = min(small, key=compact_key) if profile == 'compact' else max(large, key=lambda d: d['name'])
        return runtime, device
    requirement = 'an iPad' if profile == 'tablet' else 'both iPhone sizes'
    raise RuntimeError(f'No iOS 17+ runtime compatible with selected Xcode SDK {sdk_version} has {requirement}. Install a matching simulator runtime or select the matching Xcode with DEVELOPER_DIR.')


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('profile', choices=['compact', 'large', 'tablet'])
    args = parser.parse_args()
    listing = json.loads(subprocess.check_output(['xcrun', 'simctl', 'list', 'devices', 'available', '--json']))
    sdk_version = subprocess.check_output(['xcrun', '--sdk', 'iphonesimulator', '--show-sdk-version'], text=True).strip()
    runtime, device = select(listing['devices'], args.profile, sdk_version)
    print(f"{args.profile}: {device['name']} / {runtime} / {device['udid']} / SDK {sdk_version}", file=sys.stderr)
    print(device['udid'])


if __name__ == '__main__':
    main()
