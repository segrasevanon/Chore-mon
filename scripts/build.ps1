param([switch]$Install)
$ErrorActionPreference = 'Stop'
$taskRoot = Split-Path -Parent $PSScriptRoot
Set-Location -LiteralPath $taskRoot
if (-not $env:JAVA_HOME) {
    $studioJava = 'C:\Program Files\Android\Android Studio\jbr'
    if (Test-Path -LiteralPath "$studioJava\bin\java.exe") { $env:JAVA_HOME = $studioJava }
    else { throw 'Set JAVA_HOME to a JDK 17+ installation, or install Android Studio.' }
}
$sdkPath = $env:ANDROID_HOME
if (-not $sdkPath) { $sdkPath = Join-Path $env:LOCALAPPDATA 'Android\Sdk' }
if (-not (Test-Path -LiteralPath 'local.properties')) {
    if (-not (Test-Path -LiteralPath $sdkPath)) { throw 'Install Android SDK 36 and set ANDROID_HOME.' }
    $normalizedSdk = $sdkPath.Replace('\', '/')
    Set-Content -LiteralPath 'local.properties' -Value "sdk.dir=$normalizedSdk"
}
$tasks = @(':app:assembleDebug', ':app:testDebugUnitTest')
if ($Install) { $tasks += ':app:installDebug' }
& .\gradlew.bat @tasks --console=plain
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
if ($Install) { & (Join-Path $sdkPath 'platform-tools\adb.exe') shell am start -n 'com.choreemon.app/.MainActivity' }
