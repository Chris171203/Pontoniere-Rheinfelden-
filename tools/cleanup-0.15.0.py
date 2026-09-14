#!/usr/bin/env python3
"""Version-scoped cleanup of explicitly reviewed refs; never deletes an open PR head."""
import json
import os
from pathlib import Path
import subprocess
import urllib.parse
import urllib.request

ROOT = Path(__file__).resolve().parents[1]
manifest = json.loads((ROOT / "tools/cleanup-0.15.0.json").read_text())
repo = os.environ["GITHUB_REPOSITORY"]
if repo != manifest["repository"] or os.environ["GITHUB_REF"] != "refs/heads/main":
    raise SystemExit("Cleanup is restricted to the reviewed repository/main.")
token = os.environ["GH_TOKEN"]
def api(path):
    request = urllib.request.Request("https://api.github.com/repos/" + repo + "/" + path,
        headers={"Authorization": "Bearer " + token, "Accept": "application/vnd.github+json", "X-GitHub-Api-Version": "2022-11-28"})
    with urllib.request.urlopen(request, timeout=30) as response:
        return json.load(response)
def git(*args, check=True):
    return subprocess.run(["git", *args], text=True, capture_output=True, check=check)
def ref_sha(ref):
    line = git("ls-remote", "origin", ref).stdout.strip()
    return line.split()[0] if line else None
def archive_tag(name, sha):
    ref = "refs/tags/" + name
    existing = ref_sha(ref)
    if existing:
        if existing != sha:
            raise ValueError("Existing archive tag points elsewhere")
        return
    git("fetch", "--no-tags", "origin", sha)
    git("update-ref", ref, sha)
    git("push", "origin", ref + ":" + ref)
open_heads = set()
for page in range(1, 100):
    pulls = api("pulls?state=open&per_page=100&page=" + str(page))
    for pr in pulls:
        if pr["head"]["repo"] and pr["head"]["repo"]["full_name"] == repo:
            open_heads.add(pr["head"]["ref"])
    if len(pulls) < 100:
        break
results = []
for entry in manifest["branches"]:
    name, expected = entry["name"], entry["sha"]
    result = {"branch": name, "expected": expected, "reason": entry["reason"]}
    try:
        if name == "main" or name in open_heads:
            result["status"] = "retained-open-or-main"
        elif ref_sha("refs/heads/" + name) is None:
            result["status"] = "already-absent"
        else:
            current = api("branches/" + urllib.parse.quote(name, safe=""))
            if current["protected"] or current["commit"]["sha"] != expected:
                raise ValueError("Protected or changed branch")
            if entry["reason"] == "ancestor":
                git("merge-base", "--is-ancestor", expected, "HEAD")
            elif entry["reason"] == "merged-pr":
                pr = api("pulls/" + str(entry["pr"]))
                if not pr["merged"] or pr["base"]["ref"] != "main" or pr["head"]["sha"] != expected:
                    raise ValueError("Merged-PR evidence no longer matches")
            elif entry["reason"] == "archive-before-delete":
                archive_tag(entry["archive_tag"], expected)
                result["archive_tag"] = entry["archive_tag"]
            else:
                raise ValueError("Unknown cleanup reason")
            # Receive-pack checks the expected old object atomically, including deletion.
            git("push", "--force-with-lease=refs/heads/" + name + ":" + expected, "origin", ":refs/heads/" + name)
            if ref_sha("refs/heads/" + name) is not None:
                raise ValueError("Branch remains after delete")
            result["status"] = "deleted"
    except Exception as error:
        result["status"] = "retained-error"
        result["error"] = str(error)[:250]
    results.append(result)
    print(json.dumps(result))
output = ROOT / "artifacts/cleanup-0.15.0.json"
output.parent.mkdir(parents=True, exist_ok=True)
output.write_text(json.dumps({"version": manifest["version"], "results": results}, indent=2) + "\n")
if any(r["status"] == "retained-error" for r in results):
    raise SystemExit("Some refs were retained; inspect cleanup evidence.")
