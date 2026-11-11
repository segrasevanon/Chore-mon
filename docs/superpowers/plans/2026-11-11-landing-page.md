# Choreémon APK Landing Page Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a responsive, vibrant, funky single-page landing site for Choreémon with direct APK download, QR code, interactive creature showcase & egg hatchery, screenshots gallery, and Android sideloading instructions.

**Architecture:** Standalone, dependency-free static web application placed in `landing/` (with a root `index.html` entry point). It uses semantic HTML5, modern CSS3 with custom variables and bouncy keyframe animations, and vanilla JavaScript leveraging the Web Audio API for sound effects and an HTML5 canvas confetti particle engine.

**Tech Stack:** HTML5, CSS3, Vanilla ES6+ JavaScript, Web Audio API, Canvas API, Google Fonts (Nunito & Fredoka).

**Spec:** `docs/superpowers/specs/2026-11-11-landing-page-design.md`

## Global Constraints
- Must be 100% self-contained and run on any static web server or direct browser open without npm/bundler dependencies.
- Match Choreémon app aesthetic: Dark Ink `#18345D` borders/typography, Electric Blue `#2089F5`, Sunny Gold `#FFB829`, Sky `#E9F6FF`, and Element colors (`#2089F5`, `#DEA148`, `#9470EC`, `#27BE89`).
- Direct download button must point cleanly to `Choreemon.apk` (~51.5 MB).
- Mobile-responsive design down to 360px viewport width.
- Web Audio API sounds must only play on user interaction and support a one-click mute toggle.

## Review Focus
1. Direct APK download link resolving correctly whether served from root or `landing/`.
2. Mobile viewport layout: buttons, creature cards, and screenshots must wrap cleanly without horizontal scrollbars.
3. Audio context autoplay restrictions handled gracefully (unlock on first user tap/click).
4. Interactive egg hatch state machine correctly handling 3 sequential taps, reset state, and random creature reveal.
5. High-resolution transparent creature sprites and screenshots displaying crisply with appropriate fallback alt texts.

---

### Task 1: Asset Pipeline and Directory Setup

**Files:**
- Create: `landing/assets/creatures/` (copied from `app/src/main/res/drawable-nodpi/`)
- Create: `landing/assets/screenshots/` (copied from `verification/`)
- Create: `landing/assets/Choreemon.apk` (copy or link from root `Choreemon.apk`)
- Test: `scripts/verify_assets.ps1`

**Interfaces:**
- Consumes: Existing images in `app/src/main/res/drawable-nodpi/`, `verification/`, and `Choreemon.apk`.
- Produces: Standardized asset directory `landing/assets/` accessible via relative URLs.

- [ ] **Step 1: Write asset verification script**
Create `scripts/verify_assets.ps1` checking that all required creature PNGs, screenshot PNGs, and the APK exist in `landing/assets/`.

- [ ] **Step 2: Run verification script to confirm initial failure**
Run: `powershell -ExecutionPolicy Bypass -File scripts\verify_assets.ps1`
Expected: FAIL reporting missing directories/files.

- [ ] **Step 3: Copy assets into `landing/assets/`**
Copy the 18 creature/room assets from `app/src/main/res/drawable-nodpi/` to `landing/assets/creatures/`, the 6 verified screenshots from `verification/` to `landing/assets/screenshots/`, and create `landing/assets/Choreemon.apk` from `Choreemon.apk`.

- [ ] **Step 4: Run verification script to confirm pass**
Run: `powershell -ExecutionPolicy Bypass -File scripts\verify_assets.ps1`
Expected: PASS with 18 creatures, 6 screenshots, and APK present.

- [ ] **Step 5: Commit asset structure**
```bash
git add landing/assets/ scripts/verify_assets.ps1
```

---

### Task 2: Core HTML Markup Structure

**Files:**
- Create: `landing/index.html`
- Create: `index.html` (root entry point that serves/embeds or links to the landing experience)
- Test: `scripts/verify_markup.ps1`

**Interfaces:**
- Consumes: Asset paths from Task 1.
- Produces: Complete semantic DOM elements with IDs and classes for styling and JS behavior:
  - Navigation with Sound Toggle & Jump Links (`#nav-toggle-sound`, `#download-btn-nav`)
  - Hero with Headline, Stats, Download CTA (`#download-btn-hero`), and QR code container (`#qr-canvas`)
  - Interactive Egg Hatchery Preview widget (`#egg-widget`, `#egg-tap-zone`, `#egg-result`)
  - Creature Showcase with Family tabs (`.element-tab`, `#creatures-grid`)
  - Key App Features grid (`.feature-card`)
  - Screenshot Lightbox Gallery (`.screenshot-thumb`, `#lightbox-modal`)
  - Android 3-Step Sideloading Guide (`.install-step`)
  - Footer with file metadata and SHA256 checksum toggle

- [ ] **Step 1: Write markup verification script**
Create `scripts/verify_markup.ps1` validating that `landing/index.html` and `index.html` exist, contain valid HTML5 doctype, essential semantic IDs (`#download-btn-hero`, `#qr-canvas`, `#egg-widget`, `#creatures-grid`), and reference `styles.css` and `app.js`.

- [ ] **Step 2: Run markup verification to confirm failure**
Run: `powershell -ExecutionPolicy Bypass -File scripts\verify_markup.ps1`
Expected: FAIL with missing `landing/index.html`.

- [ ] **Step 3: Implement semantic HTML in `landing/index.html` and root `index.html`**
Draft the complete, accessible HTML structure with clear sections, SVG icons, responsive viewport tags, OpenGraph social meta tags, and data attributes for creature families and screenshot links.

- [ ] **Step 4: Run markup verification to confirm pass**
Run: `powershell -ExecutionPolicy Bypass -File scripts\verify_markup.ps1`
Expected: PASS with all required section IDs and assets linked.

- [ ] **Step 5: Commit markup**
```bash
git add landing/index.html index.html scripts/verify_markup.ps1
```

---

### Task 3: Neo-Brutalist & Funky Styling

**Files:**
- Create: `landing/styles.css`
- Test: `scripts/verify_styles.ps1`

**Interfaces:**
- Consumes: Semantic classes and IDs defined in `landing/index.html`.
- Produces: Cohesive styling including:
  - Custom properties for color palette (`--ink: #18345D`, `--blue: #2089F5`, `--gold: #FFB829`, `--pink: #F273A8`, `--green: #27BE89`, `--purple: #9470EC`, etc.)
  - Funky borders (`3px solid var(--ink)`), playful drop shadows (`4px 4px 0px var(--ink)`), and tactile active/hover states (`transform: translate(-2px, -2px)` / active `translate(2px, 2px)`)
  - Bouncy CSS keyframe animations (`@keyframes float`, `@keyframes eggWobble`, `@keyframes pulseGlow`, `@keyframes popIn`)
  - Responsive layout grids for Hero, QR Card, Creatures Showcase, Features, and Screenshot Gallery (320px to 1440px+)
  - Mobile bottom download sticky bar for small screens.

- [ ] **Step 1: Write styles verification script**
Create `scripts/verify_styles.ps1` ensuring `styles.css` contains required CSS variables, responsive `@media` breakpoints, animation keyframes, and proper box-shadow styling.

- [ ] **Step 2: Run styles verification to confirm failure**
Run: `powershell -ExecutionPolicy Bypass -File scripts\verify_styles.ps1`
Expected: FAIL with missing `landing/styles.css`.

- [ ] **Step 3: Implement `landing/styles.css`**
Write clean, modern, responsive CSS implementing the complete visual design system.

- [ ] **Step 4: Run styles verification to confirm pass**
Run: `powershell -ExecutionPolicy Bypass -File scripts\verify_styles.ps1`
Expected: PASS with all variables, keyframes, and breakpoints verified.

- [ ] **Step 5: Commit styles**
```bash
git add landing/styles.css scripts/verify_styles.ps1
```

---

### Task 4: Interactive Gameplay & Audio Engine (`app.js`)

**Files:**
- Create: `landing/app.js`
- Test: `scripts/verify_js.ps1`

**Interfaces:**
- Consumes: DOM elements from `landing/index.html`.
- Produces:
  - Synthesized Web Audio API sound effects (`playPop()`, `playCrack()`, `playFanfare()`, `playChime()`) with global mute toggle state saved in localStorage.
  - Interactive Egg Hatching mini-game: tracking tap counts (1..3), applying dynamic wobble CSS classes, launching confetti explosion on hatch, and revealing a random creature card with lore.
  - Dynamic QR code generation directly onto `#qr-canvas` pointing to the downloadable APK URL.
  - Creature Showcase interactive tab switcher (filtering between All, Dishes, Laundry, Vacuuming, and Trash).
  - Screenshot Lightbox viewer for full-size inspection.
  - Canvas Confetti particle simulator for celebration moments.

- [ ] **Step 1: Write JavaScript verification script**
Create `scripts/verify_js.ps1` testing for syntax validity and required function definitions (`initAudio`, `hatchEgg`, `renderQR`, `switchFamilyTab`, `openLightbox`).

- [ ] **Step 2: Run JS verification to confirm failure**
Run: `powershell -ExecutionPolicy Bypass -File scripts\verify_js.ps1`
Expected: FAIL with missing `landing/app.js`.

- [ ] **Step 3: Implement `landing/app.js`**
Code all interactive behaviors cleanly without external libraries, handling audio context lifecycle, canvas confetti particles, touch and click events, and QR code rendering.

- [ ] **Step 4: Run JS verification to confirm pass**
Run: `powershell -ExecutionPolicy Bypass -File scripts\verify_js.ps1`
Expected: PASS.

- [ ] **Step 5: Commit app.js**
```bash
git add landing/app.js scripts/verify_js.ps1
```

---

### Task 5: End-to-End Verification & Browser Experience Testing

**Files:**
- Test: Launch local HTTP server and verify with automated script or browser subagent.
- Create: `verification/landing-verification.md`

**Interfaces:**
- Consumes: Completed static site (`landing/index.html`, `styles.css`, `app.js`, `assets/`, root `index.html`).
- Produces: Verification report confirming all interactive features, download triggers, and responsive layout.

- [ ] **Step 1: Run comprehensive local validation**
Start a lightweight background HTTP server and verify HTTP 200 responses for `index.html`, `styles.css`, `app.js`, `Choreemon.apk`, and image assets.

- [ ] **Step 2: Verify responsive design and interactive elements**
Test desktop and mobile viewport states, QR code link resolution, egg hatching clicks, and modal opening.

- [ ] **Step 3: Document verification results**
Generate `verification/landing-verification.md` detailing all verified capabilities and instructions for running the landing page.

- [ ] **Step 4: Final commit**
```bash
git add verification/landing-verification.md
```

