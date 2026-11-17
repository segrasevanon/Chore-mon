#!/usr/bin/env python3
"""
Verification script for Vercel Deployment configuration.
Tests:
1. vercel.json format, required rewrites, and MIME/download headers.
2. .vercelignore existence and proper exclusions for Android build files & duplicate root APK.
3. Node syntax check on landing/app.js.
"""

import json
import os
import subprocess
import sys

PROJECT_ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))

print("[*] Running Vercel Deployment Verification...")

# 1. Verify vercel.json
vercel_json_path = os.path.join(PROJECT_ROOT, "vercel.json")
if not os.path.isfile(vercel_json_path):
    print("[!] FAILED: vercel.json not found")
    sys.exit(1)

with open(vercel_json_path, "r", encoding="utf-8") as f:
    config = json.load(f)

rewrites = {r.get("source"): r.get("destination") for r in config.get("rewrites", [])}
required_sources = ["/", "/download", "/styles.css", "/app.js", "/assets/(.*)"]

for src in required_sources:
    if src not in rewrites:
        print(f"[!] FAILED: Missing rewrite for '{src}' in vercel.json")
        sys.exit(1)

# Check headers
apk_header = None
for h in config.get("headers", []):
    if ".apk" in h.get("source", ""):
        apk_header = {hdr["key"]: hdr["value"] for hdr in h.get("headers", [])}
        break

if not apk_header:
    print("[!] FAILED: No APK header configuration found")
    sys.exit(1)

if apk_header.get("Content-Type") != "application/vnd.android.package-archive":
    print(f"[!] FAILED: Content-Type is '{apk_header.get('Content-Type')}', expected 'application/vnd.android.package-archive'")
    sys.exit(1)

if "attachment" not in apk_header.get("Content-Disposition", ""):
    print(f"[!] FAILED: Content-Disposition missing 'attachment': {apk_header.get('Content-Disposition')}")
    sys.exit(1)

print("[+] PASS: vercel.json rewrites and headers are valid.")

# 2. Verify .vercelignore
vercelignore_path = os.path.join(PROJECT_ROOT, ".vercelignore")
if not os.path.isfile(vercelignore_path):
    print("[!] FAILED: .vercelignore not found")
    sys.exit(1)

with open(vercelignore_path, "r", encoding="utf-8") as f:
    ignore_lines = [line.strip() for line in f if line.strip() and not line.startswith("#")]

required_ignores = ["app/", ".gradle/", "/Choreemon.apk"]
for req in required_ignores:
    if not any(req in line for line in ignore_lines):
        print(f"[!] FAILED: .vercelignore missing ignore rule for '{req}'")
        sys.exit(1)

print("[+] PASS: .vercelignore properly ignores Android artifacts and duplicate root APK.")

# 3. Node syntax check
res = subprocess.run(["node", "-c", os.path.join(PROJECT_ROOT, "landing", "app.js")], capture_output=True, text=True)
if res.returncode != 0:
    print(f"[!] FAILED: Node syntax check failed:\n{res.stderr}")
    sys.exit(1)

print("[+] PASS: landing/app.js passed Node.js syntax check.")

print("\n[*] ALL VERCEL DEPLOYMENT CHECKS PASSED!")
sys.exit(0)

