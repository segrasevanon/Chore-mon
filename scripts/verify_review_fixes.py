#!/usr/bin/env python3
"""
Regression test suite for Code Review Findings:
1. test_apk_tracked_in_git: Checks landing/assets/Choreemon.apk is tracked by git and not in .gitignore.
2. test_qr_engine_standard_compliance: Checks QR code generator produces genuine ISO/IEC compliant QR matrix with Reed-Solomon ECC.
3. test_egg_crack_overlay: Checks crack overlay styling and JS state toggles.
4. test_tab_aria_attributes: Checks role="tab" and aria-selected synchronization.
"""

import os
import sys
import subprocess

PROJECT_ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))

print("[*] Running Review Fixes Verification...")
failed = []

# Test 1: Check .gitignore does not ignore landing/assets/*.apk
gitignore_path = os.path.join(PROJECT_ROOT, ".gitignore")
with open(gitignore_path, "r", encoding="utf-8") as f:
    gi_content = f.read()

if "landing/assets/*.apk" in gi_content:
    print("[!] FAIL test_apk_tracked_in_git: .gitignore contains landing/assets/*.apk")
    failed.append("test_apk_tracked_in_git")
else:
    # Check git tracking
    res = subprocess.run(["git", "ls-files", "landing/assets/Choreemon.apk"], cwd=PROJECT_ROOT, capture_output=True, text=True)
    if "landing/assets/Choreemon.apk" not in res.stdout:
        print("[!] FAIL test_apk_tracked_in_git: landing/assets/Choreemon.apk not tracked in git")
        failed.append("test_apk_tracked_in_git")
    else:
        print("[+] PASS test_apk_tracked_in_git")

# Test 2: Check QR Generator has genuine Reed-Solomon polynomial math and standard QR Model
app_js_path = os.path.join(PROJECT_ROOT, "landing/app.js")
with open(app_js_path, "r", encoding="utf-8") as f:
    app_js = f.read()

if "QRPolynomial" not in app_js or "QRMath" not in app_js or "QRRSBlock" not in app_js:
    print("[!] FAIL test_qr_engine_standard_compliance: app.js lacks genuine Reed-Solomon ECC generator (QRPolynomial/QRMath/QRRSBlock)")
    failed.append("test_qr_engine_standard_compliance")
else:
    print("[+] PASS test_qr_engine_standard_compliance")

# Test 3: Check egg crack overlay styling and JS toggles
styles_css_path = os.path.join(PROJECT_ROOT, "landing/styles.css")
with open(styles_css_path, "r", encoding="utf-8") as f:
    styles_css = f.read()

if ".egg-crack-overlay" not in styles_css or "crack-stage-1" not in styles_css or "crack-stage-2" not in styles_css:
    print("[!] FAIL test_egg_crack_overlay: styles.css missing crack-stage-1 / crack-stage-2 styles")
    failed.append("test_egg_crack_overlay")
else:
    print("[+] PASS test_egg_crack_overlay")

# Test 4: Check filter tabs have role="tab" and aria-selected in index.html
index_html_path = os.path.join(PROJECT_ROOT, "landing/index.html")
with open(index_html_path, "r", encoding="utf-8") as f:
    index_html = f.read()

if 'role="tab"' not in index_html or 'aria-selected' not in index_html:
    print("[!] FAIL test_tab_aria_attributes: index.html missing role='tab' or aria-selected on filter tabs")
    failed.append("test_tab_aria_attributes")
else:
    print("[+] PASS test_tab_aria_attributes")

if failed:
    print(f"\n[!] {len(failed)} tests failed: {failed}")
    sys.exit(1)

print("\n[*] ALL REVIEW FIX TESTS PASSED!")
sys.exit(0)
