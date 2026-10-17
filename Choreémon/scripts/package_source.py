"""Package the portable Android project without local paths, caches, or build products."""
from pathlib import Path
import hashlib, zipfile

root = Path(__file__).resolve().parents[1]
out = root / "output"
out.mkdir(exist_ok=True)
files = [root / x for x in ["README.md", ".gitignore", "settings.gradle.kts", "build.gradle.kts", "gradle.properties", "gradlew", "gradlew.bat", "app/build.gradle.kts", "app/proguard-rules.pro"]]
for folder in ["app/src", "gradle", "docs", "scripts"]:
    files.extend(p for p in (root / folder).rglob("*") if p.is_file() and "__pycache__" not in p.parts)
for name in ["QA.md", "final-build.log", "instrumentation2.txt", "instrumentation-final.txt", "01-welcome.png", "02-home.png", "03-chore.png", "04-hatchery.png", "05-buddies.png", "06-heroes.png", "07-parent.png"]:
    p = root / "verification" / name
    if p.exists(): files.append(p)
with zipfile.ZipFile(out / "Choreemon-source.zip", "w", zipfile.ZIP_DEFLATED) as archive:
    for p in sorted(set(files)):
        archive.write(p, "Choreemon/" + p.relative_to(root).as_posix())
checksums = []
for p in sorted(out.glob("*")):
    if p.suffix in [".apk", ".zip"]:
        checksums.append(hashlib.sha256(p.read_bytes()).hexdigest() + "  " + p.name)
(out / "SHA256SUMS.txt").write_text("\n".join(checksums) + "\n", encoding="utf-8")
print("\n".join(f"{p.name}: {p.stat().st_size:,} bytes" for p in out.iterdir()))
