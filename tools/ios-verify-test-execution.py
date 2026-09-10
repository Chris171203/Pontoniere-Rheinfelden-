#!/usr/bin/env python3
"""Reject a nominally successful build unless the intended XCTest targets ran."""
import json
from pathlib import Path
import re
import sys


def verify(log, profile):
    completed = re.findall(r"Test Case '-\[(PFVR(?:Core|App|UI)Tests)\.[^ ]+ ([^\]]+)\]' (passed|failed|skipped)", log)
    targets = ['PFVRUITests'] if profile == 'tablet' else ['PFVRCoreTests', 'PFVRAppTests', 'PFVRUITests']
    counts = {}
    for target in targets:
        cases = [(name, result) for module, name, result in completed if module == target]
        passed = [name for name, result in cases if result == 'passed']
        if not passed or any(result == 'failed' for _, result in cases):
            raise ValueError(f'{target}: expected actual passing test cases and no failures')
        if f"Test Suite '{target}.xctest' passed" not in log:
            raise ValueError(f'{target}: its real test bundle did not finish successfully')
        if profile == 'tablet' and 'testSystemShareAndCalendarEditorsCanBeCancelled' not in passed:
            raise ValueError('The intended iPad system-editor test did not pass')
        counts[target] = {'passed': len(passed), 'skipped': sum(result == 'skipped' for _, result in cases)}
    return counts


if __name__ == '__main__':
    counts = verify(Path(sys.argv[1]).read_text(), sys.argv[2])
    print('SIMULATOR-TEST-EXECUTION: ' + json.dumps(counts, sort_keys=True))
