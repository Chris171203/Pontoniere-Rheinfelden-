#!/usr/bin/env python3
"""Prepare Xcode's app-icon asset from the unchanged checked-in club logo."""
from pathlib import Path
import hashlib
import json
import subprocess
import sys

root = Path(__file__).resolve().parents[1]
source = root / 'iOS/App/Assets.xcassets/PFVRLogo.imageset/pfvr_logo.jpg'
output = root / 'iOS/App/Assets.xcassets/AppIcon.appiconset'
if sys.platform != 'darwin':
    raise SystemExit('AppIcon asset conversion requires macOS sips; no image was generated.')
output.mkdir(parents=True, exist_ok=True)
# Build-time format/size conversion only: preserve the official source artwork.
subprocess.run(['/usr/bin/sips', '-s', 'format', 'png', '-z', '1024', '1024',
                str(source), '--out', str(output / 'AppIcon.png')], check=True)
(output / 'Contents.json').write_text(json.dumps({
    'images': [{'filename': 'AppIcon.png', 'idiom': 'universal',
                'platform': 'ios', 'size': '1024x1024'}],
    'info': {'author': 'xcode', 'version': 1}
}, indent=2) + '\n')
print('APPICON-SOURCE-SHA256: ' + hashlib.sha256(source.read_bytes()).hexdigest())
print('AppIcon prepared from existing 96x96 club logo; resizing adds no source detail.')
