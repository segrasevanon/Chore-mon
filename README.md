# Choreémon

A native Kotlin + Jetpack Compose Android game that turns everyday household chores into little adventures. Complete quests, collect points, hatch a new friend, and help it grow from a tiny buddy into a household legend.

## Run it

Open this folder in Android Studio and let Gradle sync. Run the `app` configuration on an Android 8.0+ phone or emulator.

Requirements: JDK 17 or newer, Android SDK 36, Build Tools 36.1.0. This project includes the Gradle 9.1 wrapper and uses Android Gradle Plugin 9.0.1 with built-in Kotlin 2.2.10. The Compose compiler plugin matches Kotlin.

```powershell
# The helper discovers Android Studio's bundled Java on Windows.
.\scripts\build.ps1

# Build, install, and launch on the connected Android device.
.\scripts\build.ps1 -Install

# Or use Gradle directly, with JAVA_HOME and your SDK configured.
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest
.\gradlew.bat :app:connectedDebugAndroidTest
```

The installable debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`. It is signed with the standard development key, suitable for local testing and presentations.

Packaged handoff files are in `output/`: `Choreemon-debug.apk`, `Choreemon-source.zip`, and `SHA256SUMS.txt`. The source archive excludes local SDK paths, caches, and generated build products. See `verification/QA.md` for the build, test, and device verification results.



## A quick presentation

1. Tap **Start playing** to explore the Chen family demo. Mia starts with 120 points, a five-day streak, Sudsy, and Sockmite.
2. Open **Wash dishes**, check its four steps, and tap **Mark complete**. Celebrate the 10 points and 12 XP.
3. Visit **Hatch**. Choose the 50-point Classic egg, then give the egg three gentle taps. The new creature is already safely saved when the reveal begins.
4. Give it a nickname and tap **Adventure together**. See it in **Buddies**, make it a favorite, and explore its evolution tree.
5. Open **Settings → Grown-up corner**. The demo parent PIN is **2468**. Create a quest, assign household members, choose a repeat schedule, and optionally require approval.
6. Switch profiles from the household menu or greeting. Each member has their own wallet, companions, quest progress, and streak.

**Create our household** starts fresh with your own name, a parent PIN, one adventurer, a free Sudsy, and four editable starter quests. Add up to eight adventurers in the grown-up corner. The PIN-protected **Reset the presentation demo** action restores the sample household and erases current local progress after confirmation.

## What's implemented

- Native onboarding, household setup, member switching, dashboard, quest list and detail, hatchery, creature journal, leaderboard, settings, and parent management.
- Four creature families, twelve individually illustrated evolution forms, and four gently sad starter sprites. Transparent creature assets and a custom illustrated room are bundled in the app.
- Breathing and bobbing pets, touch reactions, bouncy buttons, animated XP bars, egg wobble, a three-tap hatch reveal, confetti, reward celebrations, and evolution celebrations.
- Daily, weekday, weekend, and one-time quests; individual or multiple assignments; custom descriptions and 1–8 checklist steps; editing and reversible archiving.
- Optional photo proof through Android's system photo picker. Images stay on the device; no broad storage or camera permissions are requested.
- Optional parent approval. Pending quests cannot be resubmitted; rewards are granted exactly once when approved. Returned quests can be tried again.
- Nicknames, favorites, active companion selection, mood, daily love, XP, stages, streaks, and every-ten-quest milestone gifts.
- Weekly and daily household rankings. Spending a wallet balance never reduces the weekly score. Weekly scores reset on Monday.
- Calmer-animation preference, optional sound chimes, haptic toggle, accessible native controls, scrollable forms, and system back navigation.
- Versioned local JSON saves, damaged-save preservation, salted parent-PIN hashes, and parent-session relocking when the app goes into the background.

## Game rules

| Family | Starter | Level 10 | Level 25 | Element |
| --- | --- | --- | --- | --- |
| Dishes | Sudsy | Bubblescale | Dishdrake | Water |
| Laundry | Sockmite | Sockgolem | Socktitan | Earth |
| Vacuuming | Dustbunny | Whirlpup | Vacuumphant | Air |
| Trash | Stinkle | Grubsong | Trashorok | Poison |

An approved quest grants its point value to the wallet and weekly score. The active companion earns XP equal to the quest's points multiplied by mood: Happy 1.2×, Content 1.0×, Sad 0.8×, rounded to the nearest integer. Every 40 XP gains a level. Evolution is a free, explicit action at 360 XP (level 10) and 960 XP (level 25); all rarities can reach the final stage.

Mood uses the owner's most recent approved chore: Happy at 0–1 days, Content at 2–3 days, Sad after that. A new buddy begins happy. Daily love lifts mood by one step for that day and awards no points or XP. Any approved chore brightens the whole team; only the active companion gains XP. Missed days never remove earned points or creatures.

| Egg | Cost | Common | Rare | Legendary |
| --- | ---: | ---: | ---: | ---: |
| Classic | 50 pts | 60% | 35% | 5% |
| Enchanted | 100 pts | 25% | 65% | 10% |
| Aurora | 200 pts | 10% | 60% | 30% |

Each family is equally likely independently of rarity. Duplicate buddies are possible. Legendary buddies have a golden aura; rarity does not gate evolution. No purchases, ads, paid currency, or network connection are used.

The demo includes seeded wallet balances and weekly scores for presentation. Start a new household for a clean history. A streak counts distinct consecutive approved chore dates and can bridge yesterday until today's quest is finished. A delayed approval credits the current week's score and retains the original submission date for the streak.

## Project map

```text
app/src/main/java/com/choreemon/app/
  MainActivity.kt           Navigation, overlays, lifecycle, feedback
  data/GameModels.kt       Immutable models and pure game rules
  data/GameRepository.kt   Explicit JSON codec and local persistence
  data/GameViewModel.kt    StateFlow state and user actions
  ui/                      Compose screens, theme, animated components
app/src/main/res/
  drawable-nodpi/           Original creature and room artwork
  font/nunito.ttf           Bundled Nunito font
app/src/test/              Pure game-rule tests
app/src/androidTest/       Emulator flow and save/persistence tests
docs/artwork-prompts.json  Image generation provenance and prompts
verification/             Build/test logs, screenshots, QA results
```

## Scope

This is a complete **offline shared-device implementation**. Profiles are local household profiles, and the household code is a local identifier. It does not provide multi-phone syncing, remote invitations, server accounts, remote parent approval, scheduled push notifications, or Play Store publishing. Those require a backend and deployment configuration. Photo URIs are local to the phone. Progress survives app restarts but is removed if app storage is cleared or the app is uninstalled; Android cloud backup is disabled.

The parent PIN is a local management gate, not a substitute for device security or separate online identities. No credentials or external services are needed to run the app.

Artwork was created using the built-in image generation tool, guided by the supplied references. Nunito is bundled under the SIL Open Font License; see `docs/Nunito-OFL.txt`. UI icons come from AndroidX Material Icons.
