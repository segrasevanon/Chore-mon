"""ADB QA helper: derive every tap from the current UI hierarchy, never a screenshot."""
from pathlib import Path
import os, re, subprocess, sys
import xml.etree.ElementTree as ET

root = Path(__file__).resolve().parents[1]
sdk = Path(os.environ.get("ANDROID_HOME", Path(os.environ["LOCALAPPDATA"]) / "Android/Sdk"))
adb = sdk / "platform-tools/adb.exe"

def run(*args):
    return subprocess.run([str(adb), *args], check=True, capture_output=True, text=True, encoding="utf-8", errors="replace").stdout

def tree(name="current"):
    run("shell", "uiautomator", "dump", "/sdcard/choreemon-ui.xml")
    target = root / "verification" / f"ui-{name}.xml"
    run("pull", "/sdcard/choreemon-ui.xml", str(target))
    return ET.parse(target)

def nodes(doc):
    return [n for n in doc.iter("node") if n.get("text") or n.get("content-desc")]

action = sys.argv[1]
if action == "tap":
    label = sys.argv[2]
    doc = tree()
    matches = [n for n in nodes(doc) if n.get("text") == label or n.get("content-desc") == label]
    if not matches:
        raise SystemExit("Not found: " + label + "\n" + "\n".join(n.get("text", "") or n.get("content-desc", "") for n in nodes(doc)))
    n = matches[0]
    x1, y1, x2, y2 = map(int, re.findall(r"\d+", n.get("bounds")))
    print(f"Tap {label}: tree bounds {n.get('bounds')}")
    run("shell", "input", "tap", str((x1+x2)//2), str((y1+y2)//2))
elif action == "capture":
    name = sys.argv[2]
    doc = tree(name)
    run("shell", "screencap", "-p", "/sdcard/choreemon-screen.png")
    run("pull", "/sdcard/choreemon-screen.png", str(root / "verification" / f"{name}.png"))
    print("\n".join(n.get("text", "") or n.get("content-desc", "") for n in nodes(doc)))
elif action == "scroll":
    doc = tree()
    bounds = [list(map(int, re.findall(r"\d+", n.get("bounds")))) for n in doc.iter("node")]
    width, height = max(b[2] for b in bounds), max(b[3] for b in bounds)
    down = len(sys.argv) < 3 or sys.argv[2] == "down"
    y1, y2 = (int(height*.77), int(height*.32)) if down else (int(height*.32), int(height*.77))
    run("shell", "input", "swipe", str(width//2), str(y1), str(width//2), str(y2), "450")
elif action == "text":
    run("shell", "input", "text", sys.argv[2].replace(" ", "%s"))
elif action == "back":
    run("shell", "input", "keyevent", "4")
elif action == "tree":
    for n in nodes(tree()): print(n.get("text", "") or n.get("content-desc", ""), n.get("bounds"))
