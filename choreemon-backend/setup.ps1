# setup.ps1 — Run once to bootstrap the Gradle wrapper.
# The gradle-wrapper.jar is copied from the sibling Android project
# (it is a generic bootstrap binary, not project-specific).
#
# Usage: .\setup.ps1
#
param()
$ErrorActionPreference = "Stop"

$androidProject = if (Test-Path "$PSScriptRoot\..\app") { "$PSScriptRoot\.." } else { "$PSScriptRoot\..\Chore-mon" }
$backendProject  = "$PSScriptRoot"
$wrapperDir      = "$backendProject\gradle\wrapper"

if (-not (Test-Path "$androidProject\gradle\wrapper\gradle-wrapper.jar")) {
    Write-Error "Android project not found at $androidProject. Run this script from choreemon-backend\."
    exit 1
}

Write-Host "Copying Gradle wrapper from Android project..." -ForegroundColor Cyan
New-Item -ItemType Directory -Force -Path $wrapperDir | Out-Null
Copy-Item "$androidProject\gradle\wrapper\gradle-wrapper.jar" "$wrapperDir\" -Force
Copy-Item "$androidProject\gradlew"     "$backendProject\" -Force
Copy-Item "$androidProject\gradlew.bat" "$backendProject\" -Force

Write-Host "Wrapper installed (Gradle 8.10.2 will be downloaded on first run)." -ForegroundColor Green
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "  1. Copy .env.example to .env and fill in your values"
Write-Host "  2. docker compose up -d        (start SQL Server + MinIO)"
Write-Host "  3. .\gradlew.bat run           (start the API on :8080)"
