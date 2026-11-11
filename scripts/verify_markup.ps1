# Verification script for Task 2: Core HTML Markup Structure
$ErrorActionPreference = "Stop"

$files = @("landing/index.html", "index.html")
foreach ($f in $files) {
    if (-not (Test-Path $f)) {
        Write-Host "FAIL: Missing file $f" -ForegroundColor Red
        exit 1
    }
}

$html = Get-Content "landing/index.html" -Raw

$requiredIds = @(
    "nav-toggle-sound",
    "download-btn-hero",
    "qr-canvas",
    "egg-widget",
    "egg-tap-zone",
    "egg-result",
    "creatures-grid",
    "lightbox-modal",
    "sha-toggle"
)

$missing = @()
foreach ($id in $requiredIds) {
    if ($html -notmatch "id=['`"]$id['`"]") {
        $missing += "id='$id'"
    }
}

if ($html -notmatch "href=['`"].*styles\.css['`"]") {
    $missing += "styles.css link"
}

if ($html -notmatch "src=['`"].*app\.js['`"]") {
    $missing += "app.js script"
}

if ($missing.Count -gt 0) {
    Write-Host "FAIL: Missing required markup elements in landing/index.html:" -ForegroundColor Red
    $missing | ForEach-Object { Write-Host " - $_" -ForegroundColor Red }
    exit 1
} else {
    Write-Host "PASS: All required semantic IDs and links exist in landing/index.html." -ForegroundColor Green
    exit 0
}
