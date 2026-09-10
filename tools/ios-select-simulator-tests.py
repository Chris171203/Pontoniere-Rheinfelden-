#!/usr/bin/env python3
"""Regression for a runner containing newer runtimes than the selected Xcode supports."""
import importlib.util
from pathlib import Path
import unittest

spec = importlib.util.spec_from_file_location('ios_simulator_selector', Path(__file__).with_name('ios-select-simulator.py'))
selector = importlib.util.module_from_spec(spec)
spec.loader.exec_module(selector)

DEVICES = {
    'com.apple.CoreSimulator.SimRuntime.iOS-18-5': [
        {'name': 'iPhone SE (3rd generation)', 'udid': 'SE-18', 'isAvailable': True},
        {'name': 'iPhone 16 Pro Max', 'udid': 'MAX-18', 'isAvailable': True},
    ],
    'com.apple.CoreSimulator.SimRuntime.iOS-26-2': [
        {'name': 'iPhone 17', 'udid': 'BASE-26', 'isAvailable': True},
        {'name': 'iPhone 17 Pro Max', 'udid': 'MAX-26', 'isAvailable': True},
    ],
}


class SimulatorSelectionTests(unittest.TestCase):
    def test_xcode_16_sdk_never_selects_installed_ios_26(self):
        self.assertEqual(selector.select(DEVICES, 'compact', '18.5')[1]['udid'], 'SE-18')
        self.assertEqual(selector.select(DEVICES, 'large', '18.5')[1]['udid'], 'MAX-18')

    def test_new_xcode_can_use_its_matching_runtime(self):
        self.assertEqual(selector.select(DEVICES, 'large', '26.2')[1]['udid'], 'MAX-26')

    def test_only_incompatible_runtime_fails_clearly(self):
        newer_only = {'com.apple.CoreSimulator.SimRuntime.iOS-26-2': DEVICES['com.apple.CoreSimulator.SimRuntime.iOS-26-2']}
        with self.assertRaisesRegex(RuntimeError, 'SDK 18.5'):
            selector.select(newer_only, 'compact', '18.5')


if __name__ == '__main__':
    unittest.main()
