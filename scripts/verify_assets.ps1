# Verification script for Task 1: Asset Pipeline
$ErrorActionPreference = "Stop"

$requiredCreatures = @(
    "bubblescale.png", "dishdrake.png", "dustbunny.png", "dustbunny_sad.png",
    "egg.png", "grubsong.png", "room.png", "sockgolem.png",
    "sockmite.png", "sockmite_sad.png", "socktitan.png", "stinkle.png",
    "stinkle_sad.png", "sudsy.png", "sudsy_sad.png", "trashorok.png",
    "vacuumphant.png", "whirlpup.png"
)

$requiredScreenshots = @(
    "01-welcome.png", "02-home.png", "03-chore.png",
    "04-hatchery.png", "05-buddies.png", "06-heroes.png"
)

$missing = @()

if (-not (Test-Path "landing/assets/Choreemon.apk")) {
    $missing += "landing/assets/Choreemon.apk"
}

foreach ($c in $requiredCreatures) {
    $path = "landing/assets/creatures/$c"
    if (-not (Test-Path $path)) {
        $missing += $path
    }
}

foreach ($s in $requiredScreenshots) {
    $path = "landing/assets/screenshots/$s"
    if (-not (Test-Path $path)) {
        $missing += $path
    }
}

if ($missing.Count -gt 0) {
    Write-Host "FAIL: Missing $($missing.Count) assets:" -ForegroundColor Red
    $missing | ForEach-Object { Write-Host " - $_" -ForegroundColor Red }
    exit 1
} else {
    Write-Host "PASS: All required assets exist in landing/assets/ (18 creatures, 6 screenshots, 1 APK)." -ForegroundColor Green
    exit 0
}
