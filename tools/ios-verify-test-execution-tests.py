#!/usr/bin/env python3
"""Regression checks for the fail-closed iPad XCTest execution marker."""
import importlib.util
from pathlib import Path
import unittest


SCRIPT = Path(__file__).with_name('ios-verify-test-execution.py')
spec = importlib.util.spec_from_file_location('ios_verify_test_execution', SCRIPT)
module = importlib.util.module_from_spec(spec)
spec.loader.exec_module(module)


def target_log(target, names_and_results):
    cases = '\n'.join(
        f"Test Case '-[{target}.{target} {name}]' {result} (0.1 seconds)"
        for name, result in names_and_results
    )
    return cases + f"\nTest Suite '{target}.xctest' passed at 2026-01-01 00:00:00.\n"


def tablet_log(names, skipped=()):
    skipped = set(skipped)
    return target_log('PFVRUITests', [(name, 'skipped' if name in skipped else 'passed') for name in names])


class TabletExecutionVerificationTests(unittest.TestCase):
    def test_accepts_all_required_ui_tests_once(self):
        counts = module.verify(tablet_log(sorted(module.TABLET_UI_TESTS)), 'tablet')
        self.assertEqual(counts, {'PFVRUITests': {'passed': 11, 'skipped': 0}})

    def test_rejects_missing_required_ui_test(self):
        log = tablet_log(sorted(module.TABLET_UI_TESTS - {'testEveryTabIsReachableAndCapturesScreenshots'}))
        with self.assertRaisesRegex(ValueError, 'missing='):
            module.verify(log, 'tablet')

    def test_rejects_duplicate_ui_test(self):
        names = sorted(module.TABLET_UI_TESTS) + ['testEveryTabIsReachableAndCapturesScreenshots']
        with self.assertRaisesRegex(ValueError, 'duplicated='):
            module.verify(tablet_log(names), 'tablet')

    def test_rejects_skipped_tablet_ui_test(self):
        names = sorted(module.TABLET_UI_TESTS)
        with self.assertRaisesRegex(ValueError, 'no skipped UI'):
            module.verify(tablet_log(names, skipped={'testEveryTabIsReachableAndCapturesScreenshots'}), 'tablet')

    def test_rejects_incomplete_suite_marker(self):
        log = tablet_log(sorted(module.TABLET_UI_TESTS)).replace("Test Suite 'PFVRUITests.xctest' passed", "Test Suite 'PFVRUITests.xctest' failed")
        with self.assertRaisesRegex(ValueError, 'did not finish successfully'):
            module.verify(log, 'tablet')


class CompactExecutionVerificationTests(unittest.TestCase):
    def test_accepts_intentional_skipped_core_smoke_tests(self):
        log = ''.join([
            target_log('PFVRCoreTests', [('testCoreBehavior', 'passed')] + [(f'testLiveSourceSmoke{index}', 'skipped') for index in range(1, 5)]),
            target_log('PFVRAppTests', [('testAppBehavior', 'passed')]),
            target_log('PFVRUITests', [('testUIBehavior', 'passed')]),
        ])
        self.assertEqual(module.verify(log, 'compact')['PFVRCoreTests'], {'passed': 1, 'skipped': 4})


if __name__ == '__main__':
    unittest.main()
