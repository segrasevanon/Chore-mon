# Choreémon — Official Web Landing & APK Distribution Page

A high-performance, responsive landing page and distribution hub for **Choreémon** — the native Android game that turns everyday household chores into little adventures.

Styled in pixel-perfect alignment with Choreémon's Android Material3 / Jetpack Compose design system:
- **Typography:** Google Fonts Nunito (Regular, SemiBold, ExtraBold, Black)
- **Palette:** Ink (`#18345D`), Sky (`#E9F6FF`), Warm Gold (`#FFB829`), Mint Green (`#27BE89`), Air Purple (`#9470EC`), Water Blue (`#2089F5`)
- **Micro-interactions:** Bouncy buttons, ambient gradient mesh, confetti explosions, procedural retro chimes (via Web Audio API)

---

## Features

1. **Interactive Hatchery Playground:**
   - Select between Classic, Enchanted, and Aurora eggs.
   - Tap 3 times to crack the egg with sound effects, crack animation, and confetti celebration.
   - Hatches one of the 4 creature families with elemental stats and lore.

2. **Creature Journal & Evolutions Showcase:**
   - 4 elemental families: 💧 Water (Sudsy family), 🧦 Earth (Sockmite family), 💨 Air (Dustbunny family), ♻️ Recycling (Stinkle family).
   - Interactive 3-stage evolution stepper (Baby ➔ Grown ➔ Legendary Champion).
   - Happy vs. Sad starter mood toggle.

3. **App Tour & Verified Screenshots:**
   - Real high-resolution Android app screenshots embedded in a mobile frame with tabbed navigation.

4. **1-Click APK Download & Verification:**
   - Direct download for `Choreemon.apk` (51.5 MB).
   - GitHub raw mirror fallback.
   - SHA-256 Checksum with 1-click clipboard copy (`677D4060A931D3E49A7BE3D1640CA2921354DE8214DC151A57E3A08D1B65815E`).
   - Clean SVG QR Code for instant phone scanning.
   - Step-by-step Android sideloading guide (enabling "Install unknown apps").

5. **Parent & Grown-up Corner Section:**
   - Details on PIN protection (Demo PIN: `2468`), multi-profile support (up to 8 family members), and 100% offline local privacy.

---

## Local Development & Preview

The landing page is 100% self-contained standard web technology (HTML5, CSS3, ES Modules). No build step or node native binaries are required.

To preview locally:
```bash
# Python
python -m http.server 3000

# Or Node.js
npx serve .
```
Then open `http://localhost:3000` in any browser.

---

## Vercel Deployment

This repository includes pre-configured `vercel.json` files for zero-configuration Vercel deployment:

### Option A: Deploy from GitHub (Recommended)
1. Push your repository to GitHub (`segrasevanon/Chore-mon`).
2. Go to [vercel.com/new](https://vercel.com/new) and import the repository.
3. Keep default settings (the root `vercel.json` automatically rewrites traffic to `/landing/` and serves `/downloads/Choreemon.apk` with the correct Android APK MIME types and download headers).
4. Click **Deploy**. Done!

### Option B: Deploy with Root Directory set to `landing`
If you set the Vercel project's **Root Directory** setting to `landing`:
- `landing/vercel.json` handles clean URLs, caching, and APK downloads directly.

---

## File Structure

```
landing/
├── index.html        # Main landing page
├── style.css         # Compose-aligned theme & responsive styles
├── main.js           # Interactive hatchery, creature showcase, audio synth, and QR generator
├── favicon.svg       # Brand egg favicon
├── vercel.json       # Subfolder Vercel deployment config
├── package.json      # Node scripts
├── README.md         # Documentation
└── assets/
    ├── creatures/    # Optimized WebP and PNG creature sprites & room
    └── screenshots/  # Optimized WebP and PNG app tour screenshots
```
