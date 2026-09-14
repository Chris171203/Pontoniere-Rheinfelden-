#!/usr/bin/env bash
# Reproducible ad-hoc-signed simulator build + Core/App/UI tests. Run from any directory.
set -euo pipefail

PFVR_REPO=$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)
PFVR_PROFILE=${1:-compact}
case "$PFVR_PROFILE" in compact|large|tablet) ;; *) echo 'Usage: ios-test.sh [compact|large|tablet]' >&2; exit 2;; esac
if [[ $(uname -s) != Darwin ]]; then
  echo 'iOS simulator tests require macOS and Xcode. No iOS build was performed.' >&2
  exit 2
fi
cd "$PFVR_REPO"
for PFVR_TOOL in xcodebuild xcrun xcodegen python3; do
  command -v "$PFVR_TOOL" >/dev/null || { echo "Missing tool: $PFVR_TOOL" >&2; exit 2; }
done
PFVR_OUT="$PFVR_REPO/artifacts/ios-$PFVR_PROFILE"
PFVR_DERIVED="$PFVR_REPO/iOS/build/$PFVR_PROFILE"
mkdir -p "$PFVR_OUT"
# Result bundles must not already exist; keep all source files unchanged.
for PFVR_RESULT in build.xcresult tests.xcresult release.xcresult; do
  if [[ -e "$PFVR_OUT/$PFVR_RESULT" ]]; then
    echo "Existing result bundle: $PFVR_OUT/$PFVR_RESULT. Move old artifacts before rerunning." >&2
    exit 2
  fi
done

PFVR_TESTS_FINISHED=0
collect_evidence() {
  PFVR_EXIT=$?
  # Bash 3.2 can report zero to EXIT after a failed pipeline expansion.
  # A run without a completed xcodebuild test command must always fail.
  if [[ "$PFVR_EXIT" == 0 && "$PFVR_TESTS_FINISHED" != 1 ]]; then
    PFVR_EXIT=1
    echo "Simulator test command did not complete successfully." >&2
  fi
  trap - EXIT
  set +e
  printf 'Exit status: %s\nCommit: %s\nProfile: %s\n' "$PFVR_EXIT" "${GITHUB_SHA:-$(git rev-parse HEAD)}" "$PFVR_PROFILE" > "$PFVR_OUT/run-info.txt"
  xcodebuild -version >> "$PFVR_OUT/run-info.txt" 2>&1
  swift --version >> "$PFVR_OUT/run-info.txt" 2>&1
  xcodegen --version >> "$PFVR_OUT/run-info.txt" 2>&1
  if [[ -d "$PFVR_OUT/tests.xcresult" ]]; then
    xcrun xcresulttool get test-results summary --path "$PFVR_OUT/tests.xcresult" > "$PFVR_OUT/test-summary.json" 2> "$PFVR_OUT/summary-export.log"
    xcrun xcresulttool export attachments --path "$PFVR_OUT/tests.xcresult" --output-path "$PFVR_OUT/screenshots" > "$PFVR_OUT/attachment-export.log" 2>&1
    swift tools/ios-visual-evidence.swift "$PFVR_OUT/screenshots" "$PFVR_OUT/visual-contact.jpg" "$PFVR_PROFILE" 2>&1
    xcrun xccov view --report --json "$PFVR_OUT/tests.xcresult" > "$PFVR_OUT/coverage.json" 2> "$PFVR_OUT/coverage-export.log"
  fi
  if [[ -d "$PFVR_DERIVED/Build/Products/Debug-iphonesimulator/PFVR.app" ]]; then
    codesign -d --entitlements :- "$PFVR_DERIVED/Build/Products/Debug-iphonesimulator/PFVR.app" > "$PFVR_OUT/macos-signature-entitlements.plist" 2> "$PFVR_OUT/simulator-signature.log"
    ditto -c -k --sequesterRsrc --keepParent "$PFVR_DERIVED/Build/Products/Debug-iphonesimulator/PFVR.app" "$PFVR_OUT/PFVR-simulator.app.zip"
  fi
  if [[ -d "$PFVR_REPO/iOS/PFVR.xcodeproj" ]]; then
    ditto -c -k --keepParent "$PFVR_REPO/iOS/PFVR.xcodeproj" "$PFVR_OUT/PFVR.xcodeproj.zip"
  fi
  exit "$PFVR_EXIT"
}
trap collect_evidence EXIT

python3 tools/ios-audit.py
xcodegen generate --spec iOS/project.yml
xcrun simctl list devices available --json > "$PFVR_OUT/available-devices.json"
PFVR_DEVICE=$(python3 tools/ios-select-simulator.py "$PFVR_PROFILE" 2> "$PFVR_OUT/destination.txt")
cat "$PFVR_OUT/destination.txt"
xcrun simctl boot "$PFVR_DEVICE" 2>/dev/null || true
xcrun simctl bootstatus "$PFVR_DEVICE" -b
xcrun simctl status_bar "$PFVR_DEVICE" override --time '9:41' --dataNetwork wifi --wifiMode active --wifiBars 3 --batteryState charged --batteryLevel 100
if [[ "$PFVR_PROFILE" == large ]]; then
  xcrun simctl ui "$PFVR_DEVICE" appearance dark
else
  xcrun simctl ui "$PFVR_DEVICE" appearance light
fi
PFVR_ARGS=(-project iOS/PFVR.xcodeproj -scheme PFVR -configuration Debug
  -destination "platform=iOS Simulator,id=$PFVR_DEVICE"
  -derivedDataPath "$PFVR_DERIVED" CODE_SIGNING_ALLOWED=YES CODE_SIGNING_REQUIRED=YES CODE_SIGN_IDENTITY=- CODE_SIGN_STYLE=Manual)
xcodebuild "${PFVR_ARGS[@]}" build-for-testing -resultBundlePath "$PFVR_OUT/build.xcresult" 2>&1 | tee "$PFVR_OUT/build.log"
python3 tools/ios-verify-simulator-signing.py "$PFVR_DERIVED/Build/Products/Debug-iphonesimulator/PFVR.app" "$PFVR_OUT/simulator-entitlements.plist"
if [[ "$PFVR_PROFILE" == compact ]]; then
  xcodebuild -project iOS/PFVR.xcodeproj -scheme PFVR -configuration Release \
    -destination "platform=iOS Simulator,id=$PFVR_DEVICE" -derivedDataPath "$PFVR_DERIVED-release" \
    CODE_SIGNING_ALLOWED=YES CODE_SIGNING_REQUIRED=YES CODE_SIGN_IDENTITY=- CODE_SIGN_STYLE=Manual build -resultBundlePath "$PFVR_OUT/release.xcresult" \
    2>&1 | tee "$PFVR_OUT/release-build.log"
fi
# Tablet repeats the entire UI suite only; Core/App tests already run on compact
# and large profiles. The verifier rejects incomplete or duplicated UI execution.
if [[ "$PFVR_PROFILE" == tablet ]]; then
  PFVR_ARGS+=(-only-testing:PFVRUITests)
fi
xcodebuild "${PFVR_ARGS[@]}" test-without-building -resultBundlePath "$PFVR_OUT/tests.xcresult" \
  -parallel-testing-enabled NO -test-timeouts-enabled YES \
  -default-test-execution-time-allowance 60 -maximum-test-execution-time-allowance 120 \
  2>&1 | tee "$PFVR_OUT/test.log"

[[ -d "$PFVR_OUT/tests.xcresult" ]]
python3 tools/ios-verify-test-execution.py "$PFVR_OUT/test.log" "$PFVR_PROFILE"
PFVR_TESTS_FINISHED=1
