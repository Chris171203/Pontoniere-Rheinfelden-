#!/usr/bin/env bash
# Compile a device Release archive without signing or exporting an installable IPA.
set -euo pipefail
PFVR_REPO=$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)
if [[ $(uname -s) != Darwin ]]; then
  echo 'Device archive verification requires macOS and Xcode. No archive was built.' >&2
  exit 2
fi
cd "$PFVR_REPO"
for PFVR_TOOL in xcodebuild xcrun xcodegen python3; do
  command -v "$PFVR_TOOL" >/dev/null || { echo "Missing tool: $PFVR_TOOL" >&2; exit 2; }
done
PFVR_OUT="$PFVR_REPO/artifacts/ios-device"
mkdir -p "$PFVR_OUT"
for PFVR_RESULT in PFVR.xcarchive archive.xcresult; do
  [[ ! -e "$PFVR_OUT/$PFVR_RESULT" ]] || { echo "Move existing artifact before rerunning: $PFVR_RESULT" >&2; exit 2; }
done
printf 'Commit: %s\nPurpose: unsigned device Release compile, no distribution\n' "${GITHUB_SHA:-$(git rev-parse HEAD)}" > "$PFVR_OUT/run-info.txt"
xcodebuild -version >> "$PFVR_OUT/run-info.txt"
python3 tools/ios-audit.py
xcodegen generate --spec iOS/project.yml
xcodebuild -project iOS/PFVR.xcodeproj -scheme PFVR -configuration Release \
  -destination 'generic/platform=iOS' -derivedDataPath iOS/build/device \
  -archivePath "$PFVR_OUT/PFVR.xcarchive" -resultBundlePath "$PFVR_OUT/archive.xcresult" \
  CODE_SIGNING_ALLOWED=NO CODE_SIGNING_REQUIRED=NO archive \
  2>&1 | tee "$PFVR_OUT/archive.log"
python3 tools/ios-verify-device-archive.py "$PFVR_OUT/PFVR.xcarchive" \
  | tee "$PFVR_OUT/archive-verification.json"
