#!/usr/bin/env python3
"""Build configuration checks; does not substitute for compilation or runtime tests."""
from pathlib import Path
import plistlib
import re
import sys

ROOT = Path(__file__).resolve().parents[1]
errors = []
info = plistlib.loads((ROOT / 'iOS/App/Info.plist').read_bytes())
privacy = plistlib.loads((ROOT / 'iOS/App/PrivacyInfo.xcprivacy').read_bytes())
spec = (ROOT / 'iOS/project.yml').read_text()
if not re.search(r"MARKETING_VERSION:\s*['\"]?0\.", spec):
    errors.append('Development version must be below 1.0.0.')
if 'PRODUCT_BUNDLE_IDENTIFIER: ch.pfvr.app.test' not in spec:
    errors.append('Initial port must keep its test bundle identifier.')
if info.get('NSAppTransportSecurity', {}).get('NSAllowsArbitraryLoads'):
    errors.append('Global arbitrary HTTP loads must not be enabled.')
if privacy.get('NSPrivacyTracking') is not False:
    errors.append('Unexpected tracking declaration.')
reasons = {entry['NSPrivacyAccessedAPIType']: entry['NSPrivacyAccessedAPITypeReasons']
           for entry in privacy.get('NSPrivacyAccessedAPITypes', [])}
if 'CA92.1' not in reasons.get('NSPrivacyAccessedAPICategoryUserDefaults', []):
    errors.append('App-local preferences require a UserDefaults privacy reason.')
for path in (ROOT / 'iOS').rglob('*'):
    if not path.is_file() or any(part in {'.build', 'build', 'DerivedData'} for part in path.parts):
        continue
    if path.suffix in {'.p12', '.pfx', '.mobileprovision', '.jks', '.keystore'}:
        errors.append(f'Raw signing material: {path.relative_to(ROOT)}')
if errors:
    print('\n'.join(errors), file=sys.stderr)
    sys.exit(1)
print('iOS plist/configuration audit passed (no compilation performed).')
