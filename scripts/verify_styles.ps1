# Verification script for Task 3: Neo-Brutalist & Funky Styling
$ErrorActionPreference = "Stop"

if (-not (Test-Path "landing/styles.css")) {
    Write-Host "FAIL: Missing landing/styles.css" -ForegroundColor Red
    exit 1
}

$css = Get-Content "landing/styles.css" -Raw

$requiredTokens = @(
    "--ink",
    "--blue",
    "--gold",
    "--canvas",
    "@keyframes",
    "eggWobble",
    "@media",
    "box-shadow",
    ".hero-section",
    ".hatchery-card",
    ".creatures-grid",
    ".lightbox-modal"
)

$missing = @()
foreach ($token in $requiredTokens) {
    if ($css -notmatch [regex]::Escape($token)) {
        $missing += $token
    }
}

if ($missing.Count -gt 0) {
    Write-Host "FAIL: Missing required CSS tokens in landing/styles.css:" -ForegroundColor Red
    $missing | ForEach-Object { Write-Host " - $_" -ForegroundColor Red }
    exit 1
} else {
    Write-Host "PASS: landing/styles.css contains all required design tokens, keyframes, and selectors." -ForegroundColor Green
    exit 0
}
