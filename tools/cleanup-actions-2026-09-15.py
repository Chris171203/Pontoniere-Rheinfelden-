import json
import os
import pathlib
import re
import time
import urllib.error
import urllib.request

manifest = json.loads(pathlib.Path('tools/cleanup-actions-2026-09-15.json').read_text())
repo = manifest['repository']
assert repo == os.environ['GITHUB_REPOSITORY'] == 'Chris171203/Pontoniere-Rheinfelden-'
assert os.environ['GITHUB_REF'] == 'refs/heads/main'
base = f'https://api.github.com/repos/{repo}'

def api(method, path):
    req = urllib.request.Request(base + path, method=method, headers={
        'Authorization': 'Bearer ' + os.environ['GH_TOKEN'],
        'Accept': 'application/vnd.github+json',
        'X-GitHub-Api-Version': '2022-11-28',
    })
    with urllib.request.urlopen(req, timeout=30) as response:
        data = response.read()
        return json.loads(data) if data else None

protected = {r['id'] for r in manifest['keep']}
for p in pathlib.Path('.').rglob('*.md'):
    if '.git' not in p.parts:
        protected.update(map(int, re.findall(r'actions/runs/(\d+)', p.read_text(errors='replace'))))
open_heads = set()
for page in range(1, 100):
    prs = api('GET', f'/pulls?state=open&per_page=100&page={page}')
    open_heads.update(pr['head']['sha'] for pr in prs)
    if len(prs) < 100:
        break
else:
    raise RuntimeError('Open PR pagination incomplete')

targets = manifest['delete']
assert len(targets) == len({r['id'] for r in targets})
assert not protected.intersection(r['id'] for r in targets)
assert int(os.environ['GITHUB_RUN_ID']) not in {r['id'] for r in targets}
report = {'deleted': [], 'skipped': [], 'failed': []}
try:
    for target in targets:
        run_id = target['id']
        try:
            current = api('GET', f'/actions/runs/{run_id}')
            assert current['head_sha'] == target['head_sha']
            assert current['path'] == target['path']
            if current['status'] != 'completed' or current['head_sha'] in open_heads:
                report['skipped'].append(run_id)
                continue
            api('DELETE', f'/actions/runs/{run_id}')
            report['deleted'].append(run_id)
            print(f'Deleted {run_id}', flush=True)
            time.sleep(0.25)
        except Exception as exc:
            report['failed'].append({'id': run_id, 'error': str(exc)})
            raise
finally:
    pathlib.Path('cleanup-result.json').write_text(json.dumps(report, indent=2) + '\n')
    summary = {key: len(value) for key, value in report.items()}
    print(json.dumps(summary), flush=True)
    with open(os.environ['GITHUB_STEP_SUMMARY'], 'a') as f:
        f.write('Workflow cleanup: ' + json.dumps(summary) + '\n')
