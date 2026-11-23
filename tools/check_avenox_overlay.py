#!/usr/bin/env python3
from __future__ import annotations
import argparse, json, subprocess, sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
MANIFEST = ROOT / "docs" / "AVENOX_OVERLAY.json"

def fail(msg: str) -> None:
    print(f"ERROR: {msg}", file=sys.stderr)
    raise SystemExit(1)

def git(*args: str) -> str:
    p = subprocess.run(["git", *args], cwd=ROOT, text=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    if p.returncode:
        fail(p.stderr.strip() or "git command failed")
    return p.stdout

def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--upstream-ref")
    args = parser.parse_args()
    data = json.loads(MANIFEST.read_text(encoding="utf-8"))
    if data.get("schema") != 1:
        fail("unsupported overlay schema")

    integration = {}
    for point in data.get("integration_points", []):
        path, marker = point["path"], point["marker"]
        source = ROOT / path
        if not source.is_file():
            fail(f"missing integration point: {path}")
        if marker not in source.read_text(encoding="utf-8"):
            fail(f"missing marker {marker} in {path}")
        integration[path] = marker

    if args.upstream_ref:
        changed = [x for x in git("diff", "--name-only", f"{args.upstream_ref}...HEAD").splitlines() if x]

        # A fork can temporarily lag upstream. A file changed earlier in fork history may now be
        # byte-identical to the current upstream tip; don't treat that historical/reverted delta
        # as a live overlay change.
        live_changed = []
        for path in changed:
            head_blob = git("rev-parse", f"HEAD:{path}").strip() if subprocess.run(
                ["git", "cat-file", "-e", f"HEAD:{path}"], cwd=ROOT
            ).returncode == 0 else None
            upstream_blob = git("rev-parse", f"{args.upstream_ref}:{path}").strip() if subprocess.run(
                ["git", "cat-file", "-e", f"{args.upstream_ref}:{path}"], cwd=ROOT
            ).returncode == 0 else None
            if head_blob == upstream_blob:
                continue
            live_changed.append(path)

        prefixes = tuple(data.get("owned_prefixes", []))
        owned_files = set(data.get("owned_files", []))
        unexpected = [path for path in live_changed if path not in owned_files and not path.startswith(prefixes) and path not in integration]
        if unexpected:
            fail("unexpected upstream-owned delta: " + ", ".join(unexpected))
        print(f"Avenox overlay guard: OK ({len(live_changed)} live delta paths)")
    else:
        print("Avenox overlay static guard: OK")
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
