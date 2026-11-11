# Choreémon APK Landing Page Design Spec

## Overview & Intent
A vibrant, funky, and delightful single-page HTML landing site for **Choreémon**—a native Android game that gamifies household chores into monster-taming adventures. The site serves as the official portal to easily download the installable APK (`Choreemon.apk`), explore creature evolution trees, experience interactive mini-features (egg hatching), view verified gameplay screenshots, and follow step-by-step installation instructions for Android devices.

## Brand Identity & Aesthetic
- **Visual Style**: Neo-brutalist pop game aesthetic—chunky rounded card containers (`border: 3px solid #18345D`), crisp offset drop shadows (`5px 5px 0px #18345D`), playful pill badges, floating bouncy creature stickers, and subtle dot-grid background.
- **Palette**:
  - Ink (Primary Dark / Borders / Text): `#18345D`
  - Canvas (Background): `#F5F9FD`
  - Electric Blue (Water / Primary Brand): `#2089F5`
  - Sunny Gold (XP / Eggs / Rewards): `#FFB829`
  - Bubble Pink (Accents): `#F273A8`
  - Arcane Purple (Air Element): `#9470EC`
  - Meadow Green (Poison/Nature Element / Success): `#27BE89`
  - Earth Gold (Earth Element): `#DEA148`
- **Typography**: Nunito & Fredoka (via Google Fonts or bundled fallback), weights 700 to 900 for punchy headers.
- **Sound & Micro-interactions**: Web Audio API synthesized playful 8-bit sound effects (egg taps, hatch fanfares, download clicks) with a global mute toggle; canvas confetti on hatch and download.

## Structure & Sections
1. **Header / Navigation**:
   - Choreémon logo with bobbing egg badge.
   - Quick jump links: Features, Buddies, Gallery, How to Install.
   - Audio mute/unmute toggle pill.
   - Quick "Download APK" CTA.
2. **Hero Section**:
   - Punchy badge: `✨ Level Up Your Daily Quests`.
   - Headline: "Turn Everyday Chores Into Creature Adventures."
   - Subtitle explaining the app: 100% offline, zero ads, family-friendly RPG chores companion for Android.
   - **Main Download Box**:
     - Chunky bouncing "Download APK (51.5 MB)" button linking to `Choreemon.apk`.
     - Metadata badges: `v1.0` • `Android 8.0+` • `Offline & Safe` • `SHA-256 Verified`.
     - Expandable SHA-256 checksum info box.
   - **QR Code Card**:
     - Instant QR code to scan directly from Android phones for instant on-device download.
   - Hero mascot visual: Animated, bobbing Sudsy and Sockmite with floating XP bubble badges (`+12 XP`, `Streak: 5 Days!`).
3. **Interactive Egg Hatchery Preview**:
   - An interactive, 3-tap egg widget mirroring the in-game mechanic.
   - Tap 1: Egg wobbles gently (`*crack!*`).
   - Tap 2: Egg wobbles vigorously with crack particles.
   - Tap 3: Burst of confetti, celebratory fanfare, and a surprise creature reveal (Sudsy, Sockmite, Dustbunny, or Stinkle) with its element and personality lore.
   - "Hatch Again" button.
4. **Creature Showcase (The 4 Families & Evolutions)**:
   - Filterable tabs for the four chore elements:
     - 💧 **Dishes**: Sudsy ➔ Bubblescale ➔ Dishdrake
     - 🧦 **Laundry**: Sockmite ➔ Sockgolem ➔ Socktitan
     - 🐰 **Vacuuming**: Dustbunny ➔ Whirlpup ➔ Vacuumphant
     - 🦨 **Trash**: Stinkle ➔ Grubsong ➔ Trashorok
   - Each card displays full transparent art, element tags, evolution stage, and quirky backstory.
5. **Key Game Features**:
   - 🛡️ **100% Offline & Private**: No tracking, no ads, parent PIN locked (`2468`).
   - 📜 **Custom Household Quests**: Checklists, recurring schedules, and optional photo proof.
   - ⭐ **XP & Evolutions**: Happy creatures gain bonus XP and evolve into epic legends.
   - 🏆 **Household Leaderboard**: Weekly sibling competitions that never erase earned gold.
6. **Gameplay Screenshots Gallery**:
   - Responsive showcase of in-game screenshots (`verification/01-welcome.png`, `02-home.png`, etc.) with interactive modal/lightbox view.
7. **How to Install Guide (Android 3-Step)**:
   - Step 1: Download `Choreemon.apk` onto your Android phone.
   - Step 2: Open file and enable "Install unknown apps" if prompted by Chrome or Files.
   - Step 3: Tap Install, launch Choreémon, and explore the demo or create your household!
8. **Footer**:
   - Direct download link fallback, system requirements, open-source / student project credits.

## File Organization
- `landing/`:
  - `index.html`: Core markup and semantic structure.
  - `styles.css`: Custom funky stylesheet with CSS variables, keyframe animations, responsive grid/flexbox.
  - `app.js`: Interactive logic (egg hatching, sound synthesis, QR generation, tab switcher, screenshot lightbox, confetti).
  - `assets/`:
    - `creatures/`: Web copies of transparent PNG creature sprites.
    - `screenshots/`: Verified app screenshots.
    - `Choreemon.apk`: Direct download payload (or relative link to root `../Choreemon.apk`).
- Root `index.html`: Forwarding / direct entry point linking to `landing/index.html` or serving the landing page directly.

