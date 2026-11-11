# Verification script for Task 4: Interactive Gameplay & Audio Engine
$ErrorActionPreference = "Stop"

if (-not (Test-Path "landing/app.js")) {
    Write-Host "FAIL: Missing landing/app.js" -ForegroundColor Red
    exit 1
}

# 1. Syntax check with Node
node -c landing/app.js
if ($LASTEXITCODE -ne 0) {
    Write-Host "FAIL: Syntax error in landing/app.js" -ForegroundColor Red
    exit 1
}

# 2. Check for required symbols and functions
$js = Get-Content "landing/app.js" -Raw

$requiredTokens = @(
    "AudioContext",
    "playSound",
    "handleEggTap",
    "launchConfetti",
    "initQR",
    "initFilterTabs",
    "initLightbox",
    "initShaToggle"
)

$missing = @()
foreach ($token in $requiredTokens) {
    if ($js -notmatch [regex]::Escape($token)) {
        $missing += $token
    }
}

if ($missing.Count -gt 0) {
    Write-Host "FAIL: Missing required JS tokens/functions in landing/app.js:" -ForegroundColor Red
    $missing | ForEach-Object { Write-Host " - $_" -ForegroundColor Red }
    exit 1
} else {
    Write-Host "PASS: landing/app.js passed syntax check and contains all required interactive functions." -ForegroundColor Green
    exit 0
}
