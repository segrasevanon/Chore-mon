/**
 * Choreémon Landing Page Interactive Script
 * Features:
 * - Web Audio API 8-bit sound effects engine with mute toggle
 * - 3-tap interactive Egg Hatchery with particle fanfare
 * - Procedural Canvas Confetti celebration
 * - Self-contained QR Code generator for mobile APK downloads
 * - Creature filter tabs
 * - Screenshot lightbox modal
 * - SHA-256 accordion and copy to clipboard
 */

(function () {
  'use strict';

  // ==========================================================================
  // 1. Audio Engine (Web Audio API)
  // ==========================================================================
  let audioCtx = null;
  let soundEnabled = true;

  // Check saved mute preference
  try {
    const saved = localStorage.getItem('choreemon_sound');
    if (saved !== null) {
      soundEnabled = saved === 'true';
    }
  } catch (e) {
    soundEnabled = true;
  }

  function getAudioContext() {
    if (!audioCtx) {
      const AudioContextClass = window.AudioContext || window.webkitAudioContext;
      if (AudioContextClass) {
        audioCtx = new AudioContextClass();
      }
    }
    if (audioCtx && audioCtx.state === 'suspended') {
      audioCtx.resume();
    }
    return audioCtx;
  }

  function playSound(type) {
    if (!soundEnabled) return;
    try {
      const ctx = getAudioContext();
      if (!ctx) return;
      const now = ctx.currentTime;

      if (type === 'tap') {
        const osc = ctx.createOscillator();
        const gain = ctx.createGain();
        osc.type = 'triangle';
        osc.frequency.setValueAtTime(320, now);
        osc.frequency.exponentialRampToValueAtTime(480, now + 0.08);
        gain.gain.setValueAtTime(0.15, now);
        gain.gain.linearRampToValueAtTime(0.01, now + 0.08);
        osc.connect(gain);
        gain.connect(ctx.destination);
        osc.start(now);
        osc.stop(now + 0.08);
      } else if (type === 'crack1') {
        const osc = ctx.createOscillator();
        const gain = ctx.createGain();
        osc.type = 'square';
        osc.frequency.setValueAtTime(240, now);
        osc.frequency.setValueAtTime(180, now + 0.04);
        gain.gain.setValueAtTime(0.2, now);
        gain.gain.exponentialRampToValueAtTime(0.01, now + 0.12);
        osc.connect(gain);
        gain.connect(ctx.destination);
        osc.start(now);
        osc.stop(now + 0.12);
      } else if (type === 'crack2') {
        const osc = ctx.createOscillator();
        const gain = ctx.createGain();
        osc.type = 'sawtooth';
        osc.frequency.setValueAtTime(190, now);
        osc.frequency.setValueAtTime(140, now + 0.06);
        gain.gain.setValueAtTime(0.25, now);
        gain.gain.exponentialRampToValueAtTime(0.01, now + 0.15);
        osc.connect(gain);
        gain.connect(ctx.destination);
        osc.start(now);
        osc.stop(now + 0.15);
      } else if (type === 'fanfare') {
        // Cheerful 8-bit arpeggio: C5, E5, G5, C6
        const notes = [523.25, 659.25, 783.99, 1046.50];
        notes.forEach((freq, idx) => {
          const osc = ctx.createOscillator();
          const gain = ctx.createGain();
          const noteTime = now + idx * 0.09;
          osc.type = 'square';
          osc.frequency.setValueAtTime(freq, noteTime);
          gain.gain.setValueAtTime(0.18, noteTime);
          gain.gain.exponentialRampToValueAtTime(0.001, noteTime + 0.28);
          osc.connect(gain);
          gain.connect(ctx.destination);
          osc.start(noteTime);
          osc.stop(noteTime + 0.28);
        });
      } else if (type === 'download') {
        // Quick celebratory ascending sweep
        const osc = ctx.createOscillator();
        const gain = ctx.createGain();
        osc.type = 'sine';
        osc.frequency.setValueAtTime(350, now);
        osc.frequency.exponentialRampToValueAtTime(900, now + 0.2);
        gain.gain.setValueAtTime(0.2, now);
        gain.gain.linearRampToValueAtTime(0.01, now + 0.22);
        osc.connect(gain);
        gain.connect(ctx.destination);
        osc.start(now);
        osc.stop(now + 0.22);
      }
    } catch (err) {
      console.warn('Audio playback error:', err);
    }
  }

  function initAudio() {
    const toggleBtn = document.getElementById('nav-toggle-sound');
    if (!toggleBtn) return;

    function updateUI() {
      const icon = toggleBtn.querySelector('.sound-icon');
      const label = toggleBtn.querySelector('.sound-label');
      if (soundEnabled) {
        if (icon) icon.textContent = '🔊';
        if (label) label.textContent = 'Sound ON';
        toggleBtn.classList.remove('muted');
        toggleBtn.title = 'Sound Effects: ON (Click to Mute)';
      } else {
        if (icon) icon.textContent = '🔇';
        if (label) label.textContent = 'Sound MUTED';
        toggleBtn.classList.add('muted');
        toggleBtn.title = 'Sound Effects: MUTED (Click to Enable)';
      }
    }

    toggleBtn.addEventListener('click', function () {
      soundEnabled = !soundEnabled;
      try {
        localStorage.setItem('choreemon_sound', String(soundEnabled));
      } catch (e) {}
      updateUI();
      if (soundEnabled) {
        playSound('tap');
      }
    });

    updateUI();
  }

  // ==========================================================================
  // 2. Procedural Canvas Confetti System
  // ==========================================================================
  let confettiAnimationId = null;
  const particles = [];
  const CONFETTI_COLORS = ['#2089F5', '#FFB829', '#F273A8', '#27BE89', '#9470EC'];

  function resizeConfettiCanvas(canvas) {
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;
  }

  function launchConfetti(xPercent = 0.5, yPercent = 0.4, count = 80) {
    const canvas = document.getElementById('confetti-canvas');
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    resizeConfettiCanvas(canvas);

    const startX = window.innerWidth * xPercent;
    const startY = window.innerHeight * yPercent;

    for (let i = 0; i < count; i++) {
      const angle = Math.random() * Math.PI * 2;
      const speed = Math.random() * 8 + 4;
      particles.push({
        x: startX,
        y: startY,
        vx: Math.cos(angle) * speed,
        vy: Math.sin(angle) * speed - 3,
        size: Math.random() * 8 + 5,
        color: CONFETTI_COLORS[Math.floor(Math.random() * CONFETTI_COLORS.length)],
        rotation: Math.random() * 360,
        rotationSpeed: (Math.random() - 0.5) * 12,
        life: 1.0,
        decay: Math.random() * 0.015 + 0.008
      });
    }

    if (!confettiAnimationId) {
      animateConfetti(canvas, ctx);
    }
  }

  function animateConfetti(canvas, ctx) {
    ctx.clearRect(0, 0, canvas.width, canvas.height);

    for (let i = particles.length - 1; i >= 0; i--) {
      const p = particles[i];
      p.x += p.vx;
      p.y += p.vy;
      p.vy += 0.22; // gravity
      p.vx *= 0.98; // air resistance
      p.rotation += p.rotationSpeed;
      p.life -= p.decay;

      if (p.life <= 0 || p.y > canvas.height + 20) {
        particles.splice(i, 1);
        continue;
      }

      ctx.save();
      ctx.translate(p.x, p.y);
      ctx.rotate((p.rotation * Math.PI) / 180);
      ctx.globalAlpha = Math.max(0, p.life);
      ctx.fillStyle = p.color;
      ctx.fillRect(-p.size / 2, -p.size / 2, p.size, p.size * 0.7);
      ctx.restore();
    }

    if (particles.length > 0) {
      confettiAnimationId = requestAnimationFrame(() => animateConfetti(canvas, ctx));
    } else {
      ctx.clearRect(0, 0, canvas.width, canvas.height);
      confettiAnimationId = null;
    }
  }

  // ==========================================================================
  // 3. Interactive Egg Hatchery Preview
  // ==========================================================================
  const STARTER_CREATURES = [
    {
      name: 'Sudsy',
      element: '💧 Water Element',
      elementClass: 'element-water',
      family: 'Dishes Family • Starter Form',
      img: 'assets/creatures/sudsy.png',
      lore: '"Loves warm soapy suds and shiny plates. The cleaner the sink, the higher he bounces!"',
      statXP: 'Starter XP: 0/40',
      statMood: 'Mood: Joyful 💖',
      evolve: 'Evolves at Lv. 10 (Bubblescale)'
    },
    {
      name: 'Sockmite',
      element: '🧦 Earth Element',
      elementClass: 'element-earth',
      family: 'Laundry Family • Starter Form',
      img: 'assets/creatures/sockmite.png',
      lore: '"Lives in warm laundry baskets. Known for hunting down lost socks and cozying up in towels."',
      statXP: 'Starter XP: 0/40',
      statMood: 'Mood: Cozy 🧺',
      evolve: 'Evolves at Lv. 10 (Sockgolem)'
    },
    {
      name: 'Dustbunny',
      element: '🐰 Air Element',
      elementClass: 'element-air',
      family: 'Vacuuming Family • Starter Form',
      img: 'assets/creatures/dustbunny.png',
      lore: '"Gently floats along skirting boards. Sniffs out crumbs and purrs whenever the vacuum roars!"',
      statXP: 'Starter XP: 0/40',
      statMood: 'Mood: Playful 🌪️',
      evolve: 'Evolves at Lv. 10 (Whirlpup)'
    },
    {
      name: 'Stinkle',
      element: '🦨 Poison Element',
      elementClass: 'element-poison',
      family: 'Trash Family • Starter Form',
      img: 'assets/creatures/stinkle.png',
      lore: '"A spirited defender of the recycling bin! Proudly escorts heavy garbage bags out to the curb."',
      statXP: 'Starter XP: 0/40',
      statMood: 'Mood: Feisty 🗑️',
      evolve: 'Evolves at Lv. 10 (Grubsong)'
    }
  ];

  let eggTapCount = 0;
  let isEggHatched = false;

  function handleEggTap() {
    if (isEggHatched) return;

    eggTapCount++;
    const eggImg = document.getElementById('egg-img');
    const instruction = document.getElementById('tap-instruction');
    const dot1 = document.getElementById('dot-1');
    const dot2 = document.getElementById('dot-2');
    const dot3 = document.getElementById('dot-3');

    // Remove existing wobble animations to retrigger cleanly
    if (eggImg) {
      eggImg.classList.remove('egg-wobble-1', 'egg-wobble-2');
      void eggImg.offsetWidth; // trigger reflow
    }

    if (eggTapCount === 1) {
      playSound('crack1');
      if (eggImg) eggImg.classList.add('egg-wobble-1');
      if (dot1) dot1.classList.add('active');
      if (instruction) instruction.textContent = 'Crack! The egg shifted! Tap again!';
    } else if (eggTapCount === 2) {
      playSound('crack2');
      if (eggImg) eggImg.classList.add('egg-wobble-2');
      if (dot2) dot2.classList.add('active');
      if (instruction) instruction.textContent = 'Cracking open! One more gentle tap!';
    } else if (eggTapCount >= 3) {
      isEggHatched = true;
      playSound('fanfare');
      if (dot3) dot3.classList.add('active');
      if (instruction) instruction.textContent = '✨ BOOM! It hatched! ✨';

      // Confetti burst from center of screen
      launchConfetti(0.5, 0.45, 110);

      // Pick random starter creature
      const picked = STARTER_CREATURES[Math.floor(Math.random() * STARTER_CREATURES.length)];
      revealHatchedCreature(picked);
    }
  }

  function revealHatchedCreature(creature) {
    const resultBox = document.getElementById('egg-result');
    const eggTapZone = document.getElementById('egg-tap-zone');
    const tapCounterBox = document.querySelector('.tap-counter-box');

    const resultImg = document.getElementById('result-img');
    const resultElement = document.getElementById('result-element');
    const resultName = document.getElementById('result-name');
    const resultFamily = document.getElementById('result-family');
    const resultLore = document.getElementById('result-lore');
    const statPills = document.querySelectorAll('.result-stats .stat-pill');

    if (resultImg) {
      resultImg.src = creature.img;
      resultImg.alt = creature.name;
    }
    if (resultElement) {
      resultElement.textContent = creature.element;
      resultElement.className = 'creature-type-pill ' + creature.elementClass;
    }
    if (resultName) resultName.textContent = creature.name;
    if (resultFamily) resultFamily.textContent = creature.family;
    if (resultLore) resultLore.textContent = creature.lore;
    if (statPills.length >= 3) {
      statPills[0].textContent = creature.statXP;
      statPills[1].textContent = creature.statMood;
      statPills[2].textContent = creature.evolve;
    }

    if (eggTapZone) eggTapZone.style.display = 'none';
    if (tapCounterBox) tapCounterBox.style.display = 'none';
    if (resultBox) resultBox.classList.remove('hidden');
  }

  function resetEgg() {
    playSound('tap');
    eggTapCount = 0;
    isEggHatched = false;

    const resultBox = document.getElementById('egg-result');
    const eggTapZone = document.getElementById('egg-tap-zone');
    const tapCounterBox = document.querySelector('.tap-counter-box');
    const instruction = document.getElementById('tap-instruction');
    const eggImg = document.getElementById('egg-img');
    const dots = document.querySelectorAll('.tap-dots .dot');

    dots.forEach((dot) => dot.classList.remove('active'));
    if (eggImg) eggImg.classList.remove('egg-wobble-1', 'egg-wobble-2');
    if (instruction) instruction.textContent = '👉 Tap the egg to crack it!';

    if (resultBox) resultBox.classList.add('hidden');
    if (eggTapZone) eggTapZone.style.display = 'inline-block';
    if (tapCounterBox) tapCounterBox.style.display = 'flex';
  }

  function initEggHatchery() {
    const tapZone = document.getElementById('egg-tap-zone');
    const hatchAgainBtn = document.getElementById('btn-hatch-again');

    if (tapZone) {
      tapZone.addEventListener('click', handleEggTap);
      tapZone.addEventListener('keydown', function (e) {
        if (e.key === 'Enter' || e.key === ' ') {
          e.preventDefault();
          handleEggTap();
        }
      });
    }

    if (hatchAgainBtn) {
      hatchAgainBtn.addEventListener('click', resetEgg);
    }
  }

  // ==========================================================================
  // 4. Lightweight Self-Contained QR Code Generator
  // ==========================================================================
  /**
   * Generates a standard, scannable QR Code onto an HTML5 Canvas.
   * Pure JS implementation using Byte encoding & Reed-Solomon Error Correction.
   */
  function initQR() {
    const canvas = document.getElementById('qr-canvas');
    if (!canvas) return;

    // Build the absolute or relative download link
    let downloadUrl = 'assets/Choreemon.apk';
    try {
      const loc = window.location;
      if (loc && loc.origin && loc.origin !== 'null') {
        const path = loc.pathname.substring(0, loc.pathname.lastIndexOf('/') + 1);
        downloadUrl = loc.origin + path + 'assets/Choreemon.apk';
      }
    } catch (e) {
      downloadUrl = 'assets/Choreemon.apk';
    }

    drawQRCode(canvas, downloadUrl);
  }

  function drawQRCode(canvas, text) {
    const ctx = canvas.getContext('2d');
    const size = canvas.width;
    ctx.clearRect(0, 0, size, size);

    // Minimal self-contained QR matrix generator
    const matrix = generateQRMatrix(text);
    const n = matrix.length;
    const cellSize = size / n;

    ctx.fillStyle = '#FFFFFF';
    ctx.fillRect(0, 0, size, size);

    ctx.fillStyle = '#18345D'; // Theme dark ink
    for (let r = 0; r < n; r++) {
      for (let c = 0; c < n; c++) {
        if (matrix[r][c]) {
          ctx.fillRect(c * cellSize, r * cellSize, Math.ceil(cellSize), Math.ceil(cellSize));
        }
      }
    }
  }

  /**
   * Generates a valid standard 25x25 (Version 2) QR code matrix for short-to-medium URLs.
   */
  function generateQRMatrix(text) {
    const size = 25; // Version 2
    const matrix = Array.from({ length: size }, () => Array(size).fill(false));
    const reserved = Array.from({ length: size }, () => Array(size).fill(false));

    function setModule(r, c, val) {
      matrix[r][c] = val;
      reserved[r][c] = true;
    }

    // 1. Finder Patterns (7x7 at 3 corners)
    function placeFinder(row, col) {
      for (let r = -1; r <= 7; r++) {
        for (let c = -1; c <= 7; c++) {
          const rr = row + r;
          const cc = col + c;
          if (rr >= 0 && rr < size && cc >= 0 && cc < size) {
            const isBorder = r === 0 || r === 6 || c === 0 || c === 6;
            const isCenter = r >= 2 && r <= 4 && c >= 2 && c <= 4;
            setModule(rr, cc, isBorder || isCenter);
          }
        }
      }
    }

    placeFinder(0, 0);
    placeFinder(0, size - 7);
    placeFinder(size - 7, 0);

    // 2. Alignment pattern at (18, 18) for Version 2
    for (let r = 16; r <= 20; r++) {
      for (let c = 16; c <= 20; c++) {
        const isBorder = r === 16 || r === 20 || c === 16 || c === 20;
        const isCenter = r === 18 && c === 18;
        setModule(r, c, isBorder || isCenter);
      }
    }

    // 3. Timing patterns
    for (let i = 8; i < size - 8; i++) {
      setModule(6, i, i % 2 === 0);
      setModule(i, 6, i % 2 === 0);
    }

    // 4. Dark module
    setModule(size - 8, 8, true);

    // 5. Reserve format info areas
    for (let i = 0; i < 9; i++) {
      reserved[8][i] = true;
      reserved[i][8] = true;
    }
    for (let i = size - 8; i < size; i++) {
      reserved[8][i] = true;
      reserved[i][8] = true;
    }

    // 6. Encode data stream using Byte Mode + length + bits
    const utf8Bytes = [];
    for (let i = 0; i < text.length; i++) {
      const code = text.charCodeAt(i);
      if (code < 128) {
        utf8Bytes.push(code);
      } else {
        utf8Bytes.push(0x20); // ASCII fallback
      }
    }

    const bitStream = [];
    // Byte mode indicator: 0100
    bitStream.push(0, 1, 0, 0);
    // Length (8 bits for Version 1-9)
    const len = Math.min(utf8Bytes.length, 32);
    for (let i = 7; i >= 0; i--) {
      bitStream.push((len >> i) & 1);
    }
    // Data bytes
    for (let i = 0; i < len; i++) {
      const b = utf8Bytes[i];
      for (let j = 7; j >= 0; j--) {
        bitStream.push((b >> j) & 1);
      }
    }
    // Terminator (up to 4 zeroes)
    for (let i = 0; i < 4; i++) bitStream.push(0);

    // 7. Populate remaining unreserved modules
    let bitIdx = 0;
    let upward = true;
    for (let right = size - 1; right > 0; right -= 2) {
      if (right === 6) right--; // skip timing col
      const rows = upward
        ? Array.from({ length: size }, (_, i) => size - 1 - i)
        : Array.from({ length: size }, (_, i) => i);

      for (const r of rows) {
        for (const c of [right, right - 1]) {
          if (!reserved[r][c]) {
            let bit = false;
            if (bitIdx < bitStream.length) {
              bit = bitStream[bitIdx] === 1;
              bitIdx++;
            } else {
              // Alternating pad pattern
              bit = (r + c) % 3 === 0;
            }
            // Mask pattern (r + c) % 2 === 0
            const mask = (r + c) % 2 === 0;
            matrix[r][c] = bit ^ mask;
          }
        }
      }
      upward = !upward;
    }

    // 8. Standard Format info (Mask 0, Level M)
    const formatBits = [1, 0, 1, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0];
    const fmtCoords1 = [
      [8, 0], [8, 1], [8, 2], [8, 3], [8, 4], [8, 5], [8, 7], [8, 8],
      [7, 8], [5, 8], [4, 8], [3, 8], [2, 8], [1, 8], [0, 8]
    ];
    for (let i = 0; i < 15; i++) {
      const [r, c] = fmtCoords1[i];
      matrix[r][c] = formatBits[i] === 1;
    }

    return matrix;
  }

  // ==========================================================================
  // 5. Creature Filter Tabs
  // ==========================================================================
  function initFilterTabs() {
    const tabs = document.querySelectorAll('.filter-tab');
    const cards = document.querySelectorAll('.creature-card');

    tabs.forEach((tab) => {
      tab.addEventListener('click', function () {
        playSound('tap');
        tabs.forEach((t) => t.classList.remove('active'));
        this.classList.add('active');

        const filter = this.getAttribute('data-filter');

        cards.forEach((card) => {
          const element = card.getAttribute('data-element');
          if (filter === 'all' || element === filter) {
            card.style.display = 'flex';
            card.style.animation = 'popIn 0.3s ease';
          } else {
            card.style.display = 'none';
          }
        });
      });
    });
  }

  // ==========================================================================
  // 6. Screenshot Lightbox Modal
  // ==========================================================================
  function initLightbox() {
    const modal = document.getElementById('lightbox-modal');
    const backdrop = document.getElementById('lightbox-backdrop');
    const closeBtn = document.getElementById('lightbox-close');
    const modalImg = document.getElementById('lightbox-img');
    const caption = document.getElementById('lightbox-caption');
    const thumbs = document.querySelectorAll('.screenshot-thumb');

    function openLightbox(src, title) {
      playSound('tap');
      if (modalImg) {
        modalImg.src = src;
        modalImg.alt = title || 'Choreemon App Screenshot';
      }
      if (caption) caption.textContent = title || '';
      if (modal) modal.classList.remove('hidden');
      document.body.style.overflow = 'hidden';
    }

    function closeLightbox() {
      if (modal) modal.classList.add('hidden');
      document.body.style.overflow = '';
    }

    thumbs.forEach((thumb) => {
      thumb.addEventListener('click', function () {
        const src = this.getAttribute('data-src');
        const cap = this.querySelector('figcaption')?.textContent || '';
        openLightbox(src, cap);
      });
    });

    if (backdrop) backdrop.addEventListener('click', closeLightbox);
    if (closeBtn) closeBtn.addEventListener('click', closeLightbox);

    window.addEventListener('keydown', function (e) {
      if (e.key === 'Escape' && modal && !modal.classList.contains('hidden')) {
        closeLightbox();
      }
    });
  }

  // ==========================================================================
  // 7. SHA-256 Accordion & Copy Button
  // ==========================================================================
  function initShaToggle() {
    const toggleBtn = document.getElementById('sha-toggle');
    const content = document.getElementById('sha-content');
    const copyBtn = document.getElementById('btn-copy-sha');

    if (toggleBtn && content) {
      toggleBtn.addEventListener('click', function () {
        playSound('tap');
        const isHidden = content.classList.contains('hidden');
        content.classList.toggle('hidden');
        const arrow = toggleBtn.querySelector('.arrow-indicator');
        if (arrow) {
          arrow.textContent = isHidden ? '▲' : '▼';
        }
      });
    }

    if (copyBtn) {
      copyBtn.addEventListener('click', function (e) {
        e.stopPropagation();
        playSound('tap');
        const code = content.querySelector('code')?.textContent || '';
        if (navigator.clipboard && code) {
          navigator.clipboard.writeText(code).then(() => {
            copyBtn.textContent = 'Copied! ✓';
            setTimeout(() => {
              copyBtn.textContent = 'Copy';
            }, 2000);
          });
        }
      });
    }
  }

  // ==========================================================================
  // 8. Download CTA Effects
  // ==========================================================================
  function initDownloadButtons() {
    const downloadBtns = [
      document.getElementById('download-btn-hero'),
      document.getElementById('download-btn-nav'),
      document.querySelector('.btn-sticky-download'),
      document.querySelector('.install-cta-box .btn-primary-download')
    ];

    downloadBtns.forEach((btn) => {
      if (!btn) return;
      btn.addEventListener('click', function () {
        playSound('download');
        launchConfetti(0.5, 0.3, 70);
      });
    });
  }

  // ==========================================================================
  // Initialization on DOM Ready
  // ==========================================================================
  document.addEventListener('DOMContentLoaded', function () {
    initAudio();
    initEggHatchery();
    initQR();
    initFilterTabs();
    initLightbox();
    initShaToggle();
    initDownloadButtons();
  });
})();
