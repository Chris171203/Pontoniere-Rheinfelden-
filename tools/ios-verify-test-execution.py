#!/usr/bin/env python3
"""Reject a nominally successful build unless the intended XCTest targets ran."""
import json
from pathlib import Path
import re
import sys


TABLET_UI_TESTS = frozenset({
    'testFirstLaunchRemainsLockedAfterInvalidCode',
    'testEveryTabIsReachableAndCapturesScreenshots',
    'testRiverRendersActualFixtureReadingsAndBothGraphs',
    'testCartSurvivesProcessRestartAndCanBeClearedExplicitly',
    'testReturnedPaymentRequiresExplicitConfirmation',
    'testViewingPaymentQRCodePreservesCart',
    'testSystemShareAndCalendarEditorsCanBeCancelled',
    'testEventDetailPreservesSourceTextAndExposesActions',
    'testLanguageChoiceSurvivesRestartAndLeavesEventTitleUnchanged',
    'testTileVisibilityAndOrderPersistWhileCartRemainsPinned',
    'testClearingPublicCachePreservesCartAndLanguage',
})


def completed_cases(log):
    pattern = r"Test Case '-\[(PFVR(?:Core|App|UI)Tests)\.[^ ]+ ([^\]]+)\]' (passed|failed|skipped)"
    return [(module, name.removesuffix('()'), result) for module, name, result in re.findall(pattern, log)]


def verify(log, profile):
    completed = completed_cases(log)
    targets = ['PFVRUITests'] if profile == 'tablet' else ['PFVRCoreTests', 'PFVRAppTests', 'PFVRUITests']
    counts = {}
    for target in targets:
        cases = [(name, result) for module, name, result in completed if module == target]
        passed = [name for name, result in cases if result == 'passed']
        if not passed or any(result == 'failed' for _, result in cases):
            raise ValueError(f'{target}: expected actual passing test cases and no failures')
        if target == 'PFVRUITests' and any(result == 'skipped' for _, result in cases):
            raise ValueError('PFVRUITests: expected no skipped UI test cases')
        if f"Test Suite '{target}.xctest' passed" not in log:
            raise ValueError(f'{target}: its real test bundle did not finish successfully')
        counts[target] = {'passed': len(passed), 'skipped': sum(result == 'skipped' for _, result in cases)}

    if profile == 'tablet':
        actual = set(passed)
        duplicate_names = sorted(name for name in actual if passed.count(name) != 1)
        missing_names = sorted(TABLET_UI_TESTS - actual)
        unexpected_names = sorted(actual - TABLET_UI_TESTS)
        if duplicate_names or missing_names or unexpected_names:
            details = []
            if duplicate_names:
                details.append(f'duplicated={duplicate_names}')
            if missing_names:
                details.append(f'missing={missing_names}')
            if unexpected_names:
                details.append(f'unexpected={unexpected_names}')
            raise ValueError('PFVRUITests tablet execution must contain each required UI test exactly once: ' + '; '.join(details))
    return counts


if __name__ == '__main__':
    counts = verify(Path(sys.argv[1]).read_text(), sys.argv[2])
    print('SIMULATOR-TEST-EXECUTION: ' + json.dumps(counts, sort_keys=True))
