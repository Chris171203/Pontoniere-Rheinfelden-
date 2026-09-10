#!/usr/bin/env python3
"""Verify the real simulator signature and its own fixture Keychain access group."""
from pathlib import Path
import plistlib
import subprocess
import sys

app = Path(sys.argv[1])
subprocess.run(['codesign', '--verify', '--strict', str(app)], check=True)
entitlements = subprocess.run(['codesign', '-d', '--entitlements', ':-', str(app)], check=True, capture_output=True).stdout
values = plistlib.loads(entitlements)
identity = 'ch.pfvr.app.test'
assert values.get('application-identifier') == identity, 'Unexpected simulator application identity'
assert identity in values.get('keychain-access-groups', []), 'Own simulator Keychain group is missing'
print('SIMULATOR-SIGNATURE: ad-hoc verification passed; application-identifier=ch.pfvr.app.test; own keychain-access-group present.')
