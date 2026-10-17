# Verification — 17 October 2026

The Kotlin app builds successfully and all 25 automated checks pass.

## Build

`gradlew.bat :app:assembleDebug :app:testDebugUnitTest --no-watch-fs --console=plain`

Result: **BUILD SUCCESSFUL**. See `final-build.log`.

The debug APK is `app/build/outputs/apk/debug/app-debug.apk` (about 52 MiB). It is installable and was installed on the Android emulator.

## Domain tests

**21 tests passed, zero failures.** The JUnit XML is in `app/build/test-results/testDebugUnitTest/`.

Coverage includes exact-once chore rewards, daily and one-time availability, approval/rejection, spending and score independence, all egg rarity boundaries, insufficient funds, consecutive-day streaks, mood decay, daily love, newly hatched mood, active-buddy XP, all evolution stages and rarities, weekly rollover, assignments, archiving, quest validation, member switching, and milestone gift idempotency.

## Native Android tests

**4 instrumented tests passed, zero failures**, on Android 16 / API 36.1, Medium Phone 1080×2400 / 420 dpi. See `instrumentation2.txt` (`OK (4 tests)`). The test APK was built by Gradle and run with `adb shell am instrument -w -r com.choreemon.app.test/androidx.test.runner.AndroidJUnitRunner`.

- A new household starts with its own name, a free starter, zero points, and no demo history.
- Explicit JSON serialization and local persistence preserve every game field; correct and incorrect parent PINs are checked.
- Complete a four-step chore, earn 10 points, spend 50 points, tap an egg three times, add a new buddy, recreate the activity, and verify the 80-point balance and saved collection.
- Reject an incorrect parent PIN, unlock management, create a quest that needs approval, submit it as a child, verify points stay pending, approve it as a parent, and verify exactly 10 points arrive.

## Visual inspection

Native screenshots were captured with ADB and inspected directly. The home screen has the supplied references' pastel game palette, original character art, a full illustrated room, bold rounded type, visible XP, chore cards, and five working navigation tabs. The variable font was replaced with six bundled static weights after visual inspection showed incorrect weight rendering on Android.

The final visual pass also grouped page headings into a single layout block to keep section spacing consistent and enlarged podium cells so every ranking score remains visible.

After these layout corrections, the app and unit tests were rebuilt successfully, and the complete parent creation/submission/approval test passed again on the final APK (`instrumentation-final.txt`, `OK (1 test)`). That test explicitly waits for the navigation frame after leaving parent mode to avoid racing the emulator's UI dispatch.

Generated assets: 12 evolution sprites, 4 sad starter sprites, one egg, and one room. Prompt provenance is in `docs/artwork-prompts.json`.

## Environment notes

- Windows Gradle file watching stalled on this host; it is disabled in this project.
- The accented project path caused Java's test-worker argument file to misread runtime paths. The Windows-only test task mirrors runtime outputs to an ASCII cache in LocalAppData. Sources remain in the original project folder.
- Android 16 removed an API used by the older test injector. The test runner and Espresso dependencies were updated to compatible stable releases.
- The emulator showed intermittent Android **System UI** ANRs, including before app interactions. Hardware graphics and 4 GiB of RAM were used for the passing test run. This is retained as an emulator limitation; it is not evidence of physical-device performance.

## Product boundaries

This build is an offline shared-device app. Cross-device synchronization, remote invitations, accounts, push scheduling, and Play Store publishing require additional services. Optional photo links belong to the local Android device. The delivered APK is a debug build for installation and demonstration.
