#!/usr/bin/env python3
"""
End-to-End Verification for Choreemon Landing Page.
Tests:
1. Static files integrity (HTML, CSS, JS, APK, creatures, screenshots).
2. Live HTTP server test serving root and landing pages.
3. APK download headers and byte size verification.
"""

import os
import sys
import time
import subprocess
import urllib.request

PROJECT_ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
PORT = 8089

print(f"[*] Running End-to-End Verification in: {PROJECT_ROOT}")

# 1. Verify File Integrity
expected_files = [
    "index.html",
    "landing/index.html",
    "landing/styles.css",
    "landing/app.js",
    "landing/assets/Choreemon.apk",
    "landing/assets/creatures/sudsy.png",
    "landing/assets/creatures/sockmite.png",
    "landing/assets/creatures/dustbunny.png",
    "landing/assets/creatures/stinkle.png",
    "landing/assets/creatures/egg.png",
    "landing/assets/creatures/room.png",
    "landing/assets/screenshots/01-welcome.png",
    "landing/assets/screenshots/02-home.png",
]

for f in expected_files:
    full_path = os.path.join(PROJECT_ROOT, f)
    if not os.path.isfile(full_path):
        print(f"[!] FAILED: Missing file {f}")
        sys.exit(1)
    size = os.path.getsize(full_path)
    if size == 0:
        print(f"[!] FAILED: File {f} is empty")
        sys.exit(1)

print("[+] All critical static files exist and are non-empty.")

# 2. Check APK size
apk_path = os.path.join(PROJECT_ROOT, "landing/assets/Choreemon.apk")
apk_size = os.path.getsize(apk_path)
if apk_size != 54045640:
    print(f"[!] Warning: APK size {apk_size} differs from expected 54045640")
else:
    print(f"[+] APK size verified: {apk_size} bytes (51.5 MB).")

# 3. Spin up ephemeral HTTP server subprocess
server_proc = subprocess.Popen(
    [sys.executable, "-m", "http.server", str(PORT), "--directory", PROJECT_ROOT],
    stdout=subprocess.DEVNULL,
    stderr=subprocess.DEVNULL
)
time.sleep(1.2)

endpoints = [
    ("/", 200, "landing/index.html"),
    ("/landing/index.html", 200, "Choreémon"),
    ("/landing/styles.css", 200, "--ink"),
    ("/landing/app.js", 200, "AudioContext"),
    ("/landing/assets/creatures/egg.png", 200, None),
    ("/landing/assets/screenshots/01-welcome.png", 200, None),
    ("/landing/assets/Choreemon.apk", 200, None),
]

all_passed = True
try:
    for path, exp_code, exp_content in endpoints:
        url = f"http://127.0.0.1:{PORT}{path}"
        req = urllib.request.urlopen(url, timeout=5)
        code = req.getcode()
        body = req.read(2048) # sample first 2KB
        
        if code != exp_code:
            print(f"[!] FAILED endpoint {path}: got {code}, expected {exp_code}")
            all_passed = False
        elif exp_content and exp_content.encode('utf-8') not in body:
            print(f"[!] FAILED endpoint {path}: content '{exp_content}' not found in response")
            all_passed = False
        else:
            print(f"[+] HTTP {code} OK: {path}")

    # Check APK content-length from HTTP
    apk_url = f"http://127.0.0.1:{PORT}/landing/assets/Choreemon.apk"
    req_apk = urllib.request.urlopen(apk_url, timeout=5)
    content_length = int(req_apk.headers.get("Content-Length", 0))
    if content_length != apk_size:
        print(f"[!] FAILED: HTTP Content-Length {content_length} != {apk_size}")
        all_passed = False
    else:
        print(f"[+] HTTP Content-Length matches APK size ({content_length} bytes).")

finally:
    server_proc.terminate()
    server_proc.wait(timeout=3)

if not all_passed:
    print("[!] Some E2E tests failed.")
    sys.exit(1)

print("[*] PASS: All E2E HTTP endpoints and assets verified successfully!")
sys.exit(0)
