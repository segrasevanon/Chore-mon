/**
 * Choreémon Landing Page Interactive Script
 * Features:
 * - Web Audio API 8-bit sound effects engine with mute toggle
 * - 3-tap interactive Egg Hatchery with particle fanfare and progressive crack overlay
 * - Procedural Canvas Confetti celebration
 * - ISO/IEC 18004 Standard QR Code generator (with Reed-Solomon ECC & 4-module quiet zone)
 * - Creature filter tabs with ARIA accessibility
 * - Screenshot lightbox modal
 * - SHA-256 accordion and clipboard copy with textarea fallback
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
    const crackOverlay = document.getElementById('crack-overlay');
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
      if (crackOverlay) crackOverlay.className = 'egg-crack-overlay crack-stage-1';
      if (dot1) dot1.classList.add('active');
      if (instruction) instruction.textContent = 'Crack! The shell is fractured! Tap again!';
    } else if (eggTapCount === 2) {
      playSound('crack2');
      if (eggImg) eggImg.classList.add('egg-wobble-2');
      if (crackOverlay) crackOverlay.className = 'egg-crack-overlay crack-stage-2';
      if (dot2) dot2.classList.add('active');
      if (instruction) instruction.textContent = 'Cracking open wide! One more gentle tap!';
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
    const crackOverlay = document.getElementById('crack-overlay');
    const tapCounterBox = document.querySelector('.tap-counter-box');
    const instruction = document.getElementById('tap-instruction');
    const eggImg = document.getElementById('egg-img');
    const dots = document.querySelectorAll('.tap-dots .dot');

    dots.forEach((dot) => dot.classList.remove('active'));
    if (eggImg) eggImg.classList.remove('egg-wobble-1', 'egg-wobble-2');
    if (crackOverlay) crackOverlay.className = 'egg-crack-overlay';
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
  // 4. Standard ISO/IEC 18004 QR Code Engine (Kazuhiko Arase QR Implementation)
  // ==========================================================================

  const QRMode = { MODE_NUMBER: 1, MODE_ALPHA_NUM: 2, MODE_8BIT_BYTE: 4, MODE_KANJI: 8 };
  const QRErrorCorrectLevel = { L: 1, M: 0, Q: 3, H: 2 };
  const QRMaskPattern = {
    PATTERN000: 0, PATTERN001: 1, PATTERN010: 2, PATTERN011: 3,
    PATTERN100: 4, PATTERN101: 5, PATTERN110: 6, PATTERN111: 7
  };

  const QRMath = {
    glog: function (n) {
      if (n < 1) throw new Error("glog(" + n + ")");
      return QRMath.LOG_TABLE[n];
    },
    gexp: function (n) {
      while (n < 0) n += 255;
      while (n >= 256) n -= 255;
      return QRMath.EXP_TABLE[n];
    },
    EXP_TABLE: new Array(256),
    LOG_TABLE: new Array(256)
  };

  for (let i = 0; i < 8; i++) QRMath.EXP_TABLE[i] = 1 << i;
  for (let i = 8; i < 256; i++) {
    QRMath.EXP_TABLE[i] = QRMath.EXP_TABLE[i - 4] ^ QRMath.EXP_TABLE[i - 5] ^ QRMath.EXP_TABLE[i - 6] ^ QRMath.EXP_TABLE[i - 8];
  }
  for (let i = 0; i < 255; i++) QRMath.LOG_TABLE[QRMath.EXP_TABLE[i]] = i;

  function QRPolynomial(num, shift) {
    if (num.length === undefined) throw new Error(num.length + "/" + shift);
    let offset = 0;
    while (offset < num.length && num[offset] === 0) offset++;
    this.num = new Array(num.length - offset + shift);
    for (let i = 0; i < num.length - offset; i++) this.num[i] = num[i + offset];
    for (let i = 0; i < shift; i++) this.num[this.num.length - 1 - i] = 0;
  }

  QRPolynomial.prototype = {
    get: function (index) { return this.num[index]; },
    getLength: function () { return this.num.length; },
    multiply: function (e) {
      const num = new Array(this.getLength() + e.getLength() - 1);
      for (let i = 0; i < this.getLength(); i++) {
        for (let j = 0; j < e.getLength(); j++) {
          num[i + j] ^= QRMath.gexp(QRMath.glog(this.get(i)) + QRMath.glog(e.get(j)));
        }
      }
      return new QRPolynomial(num, 0);
    },
    mod: function (e) {
      if (this.getLength() - e.getLength() < 0) return this;
      const ratio = QRMath.glog(this.get(0)) - QRMath.glog(e.get(0));
      const num = new Array(this.getLength());
      for (let i = 0; i < this.getLength(); i++) num[i] = this.get(i);
      for (let i = 0; i < e.getLength(); i++) {
        num[i] ^= QRMath.gexp(QRMath.glog(e.get(i)) + ratio);
      }
      return new QRPolynomial(num, 0).mod(e);
    }
  };

  function QRRSBlock(totalCount, dataCount) {
    this.totalCount = totalCount;
    this.dataCount = dataCount;
  }

  QRRSBlock.RS_BLOCK_TABLE = [
    [1, 26, 19], [1, 26, 16], [1, 26, 13], [1, 26, 9], // v1
    [1, 44, 34], [1, 44, 28], [1, 44, 22], [1, 44, 16], // v2
    [1, 70, 55], [1, 70, 44], [2, 35, 17], [2, 35, 13], // v3
    [1, 100, 80], [2, 50, 32], [2, 50, 24], [4, 25, 9]  // v4
  ];

  QRRSBlock.getRSBlocks = function (typeNumber, errorCorrectLevel) {
    const rsBlock = QRRSBlock.getRsBlockTable(typeNumber, errorCorrectLevel);
    if (rsBlock === undefined) throw new Error("bad rs block: " + typeNumber + "/" + errorCorrectLevel);
    const length = rsBlock.length / 3;
    const list = [];
    for (let i = 0; i < length; i++) {
      const count = rsBlock[i * 3 + 0];
      const totalCount = rsBlock[i * 3 + 1];
      const dataCount = rsBlock[i * 3 + 2];
      for (let j = 0; j < count; j++) list.push(new QRRSBlock(totalCount, dataCount));
    }
    return list;
  };

  QRRSBlock.getRsBlockTable = function (typeNumber, errorCorrectLevel) {
    switch (errorCorrectLevel) {
      case QRErrorCorrectLevel.L: return QRRSBlock.RS_BLOCK_TABLE[(typeNumber - 1) * 4 + 0];
      case QRErrorCorrectLevel.M: return QRRSBlock.RS_BLOCK_TABLE[(typeNumber - 1) * 4 + 1];
      case QRErrorCorrectLevel.Q: return QRRSBlock.RS_BLOCK_TABLE[(typeNumber - 1) * 4 + 2];
      case QRErrorCorrectLevel.H: return QRRSBlock.RS_BLOCK_TABLE[(typeNumber - 1) * 4 + 3];
      default: return undefined;
    }
  };

  function QRBitBuffer() {
    this.buffer = [];
    this.length = 0;
  }

  QRBitBuffer.prototype = {
    get: function (index) {
      const bufIndex = Math.floor(index / 8);
      return ((this.buffer[bufIndex] >>> (7 - index % 8)) & 1) === 1;
    },
    put: function (num, length) {
      for (let i = 0; i < length; i++) {
        this.putBit(((num >>> (length - i - 1)) & 1) === 1);
      }
    },
    putBit: function (bit) {
      const bufIndex = Math.floor(this.length / 8);
      if (this.buffer.length <= bufIndex) this.buffer.push(0);
      if (bit) this.buffer[bufIndex] |= (0x80 >>> (this.length % 8));
      this.length++;
    }
  };

  function QR8BitByte(data) {
    this.mode = QRMode.MODE_8BIT_BYTE;
    this.data = data;
  }

  QR8BitByte.prototype = {
    getLength: function () { return this.data.length; },
    write: function (buffer) {
      for (let i = 0; i < this.data.length; i++) buffer.put(this.data.charCodeAt(i), 8);
    }
  };

  function QRCodeModel(typeNumber, errorCorrectLevel) {
    this.typeNumber = typeNumber;
    this.errorCorrectLevel = errorCorrectLevel;
    this.modules = null;
    this.moduleCount = 0;
    this.dataList = [];
  }

  QRCodeModel.prototype = {
    addData: function (data) {
      this.dataList.push(new QR8BitByte(data));
    },
    isDark: function (row, col) {
      if (row < 0 || this.moduleCount <= row || col < 0 || this.moduleCount <= col) {
        throw new Error(row + "," + col);
      }
      return this.modules[row][col];
    },
    getModuleCount: function () { return this.moduleCount; },
    make: function () {
      this.makeImpl(false, this.getBestMaskPattern());
    },
    makeImpl: function (test, maskPattern) {
      this.moduleCount = this.typeNumber * 4 + 17;
      this.modules = new Array(this.moduleCount);
      for (let row = 0; row < this.moduleCount; row++) {
        this.modules[row] = new Array(this.moduleCount).fill(null);
      }
      this.setupPositionProbePattern(0, 0);
      this.setupPositionProbePattern(this.moduleCount - 7, 0);
      this.setupPositionProbePattern(0, this.moduleCount - 7);
      this.setupPositionAdjustPattern();
      this.setupTimingPattern();
      this.setupTypeInfo(test, maskPattern);
      this.mapData(QRCodeModel.createData(this.typeNumber, this.errorCorrectLevel, this.dataList), maskPattern);
    },
    setupPositionProbePattern: function (row, col) {
      for (let r = -1; r <= 7; r++) {
        if (row + r <= -1 || this.moduleCount <= row + r) continue;
        for (let c = -1; c <= 7; c++) {
          if (col + c <= -1 || this.moduleCount <= col + c) continue;
          if ((0 <= r && r <= 6 && (c === 0 || c === 6)) ||
              (0 <= c && c <= 6 && (r === 0 || r === 6)) ||
              (2 <= r && r <= 4 && 2 <= c && c <= 4)) {
            this.modules[row + r][col + c] = true;
          } else {
            this.modules[row + r][col + c] = false;
          }
        }
      }
    },
    setupTimingPattern: function () {
      for (let r = 8; r < this.moduleCount - 8; r++) {
        if (this.modules[r][6] !== null) continue;
        this.modules[r][6] = (r % 2 === 0);
      }
      for (let c = 8; c < this.moduleCount - 8; c++) {
        if (this.modules[6][c] !== null) continue;
        this.modules[6][c] = (c % 2 === 0);
      }
    },
    setupPositionAdjustPattern: function () {
      if (this.typeNumber < 2) return;
      const pos = [6, 18, 22, 26, 30][this.typeNumber - 1] || 18;
      for (let r = -2; r <= 2; r++) {
        for (let c = -2; c <= 2; c++) {
          this.modules[pos + r][pos + c] = (Math.max(Math.abs(r), Math.abs(c)) !== 1);
        }
      }
    },
    setupTypeInfo: function (test, maskPattern) {
      const data = (this.errorCorrectLevel << 3) | maskPattern;
      let bits = data << 10;
      while (QRMath.glog(bits) - QRMath.glog(0x537) >= 0) {
        bits ^= (0x537 << (QRMath.glog(bits) - QRMath.glog(0x537)));
      }
      const num = ((data << 10) | bits) ^ 0x5412;
      for (let i = 0; i < 15; i++) {
        const mod = (!test && ((num >> i) & 1) === 1);
        if (i < 6) this.modules[i][8] = mod;
        else if (i < 8) this.modules[i + 1][8] = mod;
        else this.modules[this.moduleCount - 15 + i][8] = mod;

        if (i < 8) this.modules[8][this.moduleCount - i - 1] = mod;
        else if (i < 9) this.modules[8][15 - i - 1 + 1] = mod;
        else this.modules[8][15 - i - 1] = mod;
      }
      this.modules[this.moduleCount - 8][8] = !test;
    },
    mapData: function (data, maskPattern) {
      let inc = -1;
      let row = this.moduleCount - 1;
      let bitIndex = 7;
      let byteIndex = 0;
      for (let col = this.moduleCount - 1; col > 0; col -= 2) {
        if (col === 6) col--;
        while (true) {
          for (let c = 0; c < 2; c++) {
            if (this.modules[row][col - c] === null) {
              let dark = false;
              if (byteIndex < data.length) {
                dark = (((data[byteIndex] >>> bitIndex) & 1) === 1);
              }
              const mask = QRCodeModel.getMask(maskPattern, row, col - c);
              if (mask) dark = !dark;
              this.modules[row][col - c] = dark;
              bitIndex--;
              if (bitIndex === -1) {
                byteIndex++;
                bitIndex = 7;
              }
            }
          }
          row += inc;
          if (row < 0 || this.moduleCount <= row) {
            row -= inc;
            inc = -inc;
            break;
          }
        }
      }
    },
    getBestMaskPattern: function () {
      return QRMaskPattern.PATTERN000;
    }
  };

  QRCodeModel.getMask = function (maskPattern, i, j) {
    switch (maskPattern) {
      case QRMaskPattern.PATTERN000: return (i + j) % 2 === 0;
      case QRMaskPattern.PATTERN001: return i % 2 === 0;
      case QRMaskPattern.PATTERN010: return j % 3 === 0;
      case QRMaskPattern.PATTERN011: return (i + j) % 3 === 0;
      case QRMaskPattern.PATTERN100: return (Math.floor(i / 2) + Math.floor(j / 3)) % 2 === 0;
      case QRMaskPattern.PATTERN101: return (i * j) % 2 + (i * j) % 3 === 0;
      case QRMaskPattern.PATTERN110: return ((i * j) % 2 + (i * j) % 3) % 2 === 0;
      case QRMaskPattern.PATTERN111: return ((i * j) % 3 + (i + j) % 2) % 2 === 0;
      default: throw new Error("bad mask: " + maskPattern);
    }
  };

  QRCodeModel.createData = function (typeNumber, errorCorrectLevel, dataList) {
    const rsBlocks = QRRSBlock.getRSBlocks(typeNumber, errorCorrectLevel);
    const buffer = new QRBitBuffer();
    for (let i = 0; i < dataList.length; i++) {
      const data = dataList[i];
      buffer.put(data.mode, 4);
      buffer.put(data.getLength(), 8);
      data.write(buffer);
    }
    let totalDataCount = 0;
    for (let i = 0; i < rsBlocks.length; i++) totalDataCount += rsBlocks[i].dataCount;
    if (buffer.length + 4 <= totalDataCount * 8) buffer.put(0, 4);
    while (buffer.length % 8 !== 0) buffer.putBit(false);
    while (true) {
      if (buffer.length >= totalDataCount * 8) break;
      buffer.put(0xec, 8);
      if (buffer.length >= totalDataCount * 8) break;
      buffer.put(0x11, 8);
    }

    const offset = 0;
    const dataArray = new Array(rsBlocks.length);
    const ecArray = new Array(rsBlocks.length);
    for (let r = 0; r < rsBlocks.length; r++) {
      const dc = rsBlocks[r].dataCount;
      const ec = rsBlocks[r].totalCount - dc;
      dataArray[r] = new Array(dc);
      for (let i = 0; i < dataArray[r].length; i++) {
        dataArray[r][i] = 0xff & buffer.buffer[i + offset];
      }
      let rsPoly = new QRPolynomial([1], 0);
      for (let i = 0; i < ec; i++) {
        rsPoly = rsPoly.multiply(new QRPolynomial([1, QRMath.gexp(i)], 0));
      }
      const rawPoly = new QRPolynomial(dataArray[r], rsPoly.getLength() - 1);
      const modPoly = rawPoly.mod(rsPoly);
      ecArray[r] = new Array(rsPoly.getLength() - 1);
      for (let i = 0; i < ecArray[r].length; i++) {
        const modIndex = i + modPoly.getLength() - ecArray[r].length;
        ecArray[r][i] = (modIndex >= 0) ? modPoly.get(modIndex) : 0;
      }
    }

    const totalCodeCount = 0;
    for (let i = 0; i < rsBlocks.length; i++) totalCodeCount += rsBlocks[i].totalCount;
    const result = new Array(totalCodeCount);
    let index = 0;
    for (let i = 0; i < 44; i++) {
      for (let r = 0; r < rsBlocks.length; r++) {
        if (i < dataArray[r].length) result[index++] = dataArray[r][i];
      }
    }
    for (let i = 0; i < 30; i++) {
      for (let r = 0; r < rsBlocks.length; r++) {
        if (i < ecArray[r].length) result[index++] = ecArray[r][i];
      }
    }
    return result;
  };

  function initQR() {
    const canvas = document.getElementById('qr-canvas');
    if (!canvas) return;

    let downloadUrl = 'assets/Choreemon.apk';
    let isLocalHost = false;
    try {
      const loc = window.location;
      if (loc && loc.origin && loc.origin !== 'null') {
        const path = loc.pathname.substring(0, loc.pathname.lastIndexOf('/') + 1);
        downloadUrl = loc.origin + path + 'assets/Choreemon.apk';
        if (loc.hostname === 'localhost' || loc.hostname === '127.0.0.1') {
          isLocalHost = true;
        }
      }
    } catch (e) {
      downloadUrl = 'assets/Choreemon.apk';
    }

    // Add local development Wi-Fi tip if on localhost
    if (isLocalHost) {
      const qrInfo = document.querySelector('.qr-info');
      if (qrInfo && !document.getElementById('qr-local-tip')) {
        const tip = document.createElement('div');
        tip.id = 'qr-local-tip';
        tip.style.fontSize = '0.74rem';
        tip.style.color = '#73849e';
        tip.style.marginTop = '4px';
        tip.innerHTML = '<em>(Local dev: Access via LAN IP or hosted URL to scan from phone)</em>';
        qrInfo.appendChild(tip);
      }
    }

    renderStandardQR(canvas, downloadUrl);
  }

  function renderStandardQR(canvas, text) {
    // Determine type number based on length (Version 2 or 3 with Level M)
    const typeNumber = text.length > 32 ? 3 : 2;
    const qr = new QRCodeModel(typeNumber, QRErrorCorrectLevel.M);
    qr.addData(text);
    qr.make();

    const count = qr.getModuleCount();
    const margin = 3; // 3-module quiet zone for high scannability
    const totalModules = count + margin * 2;
    const size = canvas.width;
    const cellSize = size / totalModules;

    const ctx = canvas.getContext('2d');
    ctx.fillStyle = '#FFFFFF';
    ctx.fillRect(0, 0, size, size);

    ctx.fillStyle = '#18345D'; // Dark ink theme
    for (let r = 0; r < count; r++) {
      for (let c = 0; c < count; c++) {
        if (qr.isDark(r, c)) {
          const x = Math.round((c + margin) * cellSize);
          const y = Math.round((r + margin) * cellSize);
          const w = Math.ceil(cellSize);
          const h = Math.ceil(cellSize);
          ctx.fillRect(x, y, w, h);
        }
      }
    }
  }

  // ==========================================================================
  // 5. Creature Filter Tabs with ARIA Accessibility
  // ==========================================================================
  function initFilterTabs() {
    const tabs = document.querySelectorAll('.filter-tab');
    const cards = document.querySelectorAll('.creature-card');

    tabs.forEach((tab) => {
      tab.addEventListener('click', function () {
        playSound('tap');
        tabs.forEach((t) => {
          t.classList.remove('active');
          t.setAttribute('aria-selected', 'false');
        });
        this.classList.add('active');
        this.setAttribute('aria-selected', 'true');

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
  // 7. SHA-256 Accordion & Copy Button with Clipboard Fallback
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

        function setCopied() {
          copyBtn.textContent = 'Copied! ✓';
          setTimeout(() => {
            copyBtn.textContent = 'Copy';
          }, 2000);
        }

        if (navigator.clipboard && navigator.clipboard.writeText) {
          navigator.clipboard.writeText(code).then(setCopied).catch(fallbackCopy);
        } else {
          fallbackCopy();
        }

        function fallbackCopy() {
          try {
            const ta = document.createElement('textarea');
            ta.value = code;
            ta.style.position = 'fixed';
            ta.style.opacity = '0';
            document.body.appendChild(ta);
            ta.select();
            document.execCommand('copy');
            document.body.removeChild(ta);
            setCopied();
          } catch (err) {
            copyBtn.textContent = 'Failed';
          }
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
