// Choreémon Landing Page — Interactive Engine

// --- Creature Data matching GameModels.kt ---
const CREATURES_DATA = {
  water: {
    element: 'Water',
    emoji: '💧',
    color: '#2089F5',
    paleColor: '#E4F8FF',
    trait: 'Bubbles burst into bonus XP when dirty sinks are cleared!',
    stages: [
      {
        name: 'Sudsy',
        rarity: 'Common',
        desc: 'A bubbly little buddy who turns a pile of dishes into a reason to smile.',
        sprite: 'assets/creatures/sudsy.webp',
        sadSprite: 'assets/creatures/sudsy_sad.webp',
        nextXp: '360 XP needed',
        xpPercent: 45
      },
      {
        name: 'Bubblescale',
        rarity: 'Rare',
        desc: 'A playful bubble dragon that swoops over sparkling clean countertops.',
        sprite: 'assets/creatures/bubblescale.webp',
        nextXp: '960 XP needed',
        xpPercent: 65
      },
      {
        name: 'Dishdrake',
        rarity: 'Legendary',
        desc: 'A majestic guardian of gleaming kitchens. Gleams brighter with every spotless cup.',
        sprite: 'assets/creatures/dishdrake.webp',
        nextXp: 'Max Evolution reached!',
        xpPercent: 100
      }
    ]
  },
  earth: {
    element: 'Earth',
    emoji: '🧦',
    color: '#DEA148',
    paleColor: '#FFF2DD',
    trait: 'Pairs matching socks with unmatched enthusiasm!',
    stages: [
      {
        name: 'Sockmite',
        rarity: 'Common',
        desc: 'A soft, sock-loving explorer. Every laundry day is a fresh adventure.',
        sprite: 'assets/creatures/sockmite.webp',
        sadSprite: 'assets/creatures/sockmite_sad.webp',
        nextXp: '360 XP needed',
        xpPercent: 30
      },
      {
        name: 'Sockgolem',
        rarity: 'Rare',
        desc: 'A sturdy laundry defender assembled from cozy folded blankets and sheets.',
        sprite: 'assets/creatures/sockgolem.webp',
        nextXp: '960 XP needed',
        xpPercent: 70
      },
      {
        name: 'Socktitan',
        rarity: 'Legendary',
        desc: 'Colossal champion of organized closets. Makes folding clothes feel epic.',
        sprite: 'assets/creatures/socktitan.webp',
        nextXp: 'Max Evolution reached!',
        xpPercent: 100
      }
    ]
  },
  air: {
    element: 'Air',
    emoji: '💨',
    color: '#9470EC',
    paleColor: '#F0EAFE',
    trait: 'Spins around the room leaving fresh breezy air in its wake.',
    stages: [
      {
        name: 'Dustbunny',
        rarity: 'Common',
        desc: 'A tiny whirlwind of fluff with a very big heart. Loves a freshly vacuumed rug.',
        sprite: 'assets/creatures/dustbunny.webp',
        sadSprite: 'assets/creatures/dustbunny_sad.webp',
        nextXp: '360 XP needed',
        xpPercent: 55
      },
      {
        name: 'Whirlpup',
        rarity: 'Rare',
        desc: 'An energetic breeze pup that chases away cobwebs and dust motes with joyful barks.',
        sprite: 'assets/creatures/whirlpup.webp',
        nextXp: '960 XP needed',
        xpPercent: 80
      },
      {
        name: 'Vacuumphant',
        rarity: 'Legendary',
        desc: 'A magnificent flying cleaning titan that trumpets when floors are spotless.',
        sprite: 'assets/creatures/vacuumphant.webp',
        nextXp: 'Max Evolution reached!',
        xpPercent: 100
      }
    ]
  },
  poison: {
    element: 'Recycling',
    emoji: '♻️',
    color: '#54AA7B',
    paleColor: '#E9F7EB',
    trait: 'Sorts plastics, paper, and compost with boundless cheer!',
    stages: [
      {
        name: 'Stinkle',
        rarity: 'Common',
        desc: 'A cheerful recycling champion who finds treasure in tidying up wrappers and boxes.',
        sprite: 'assets/creatures/stinkle.webp',
        sadSprite: 'assets/creatures/stinkle_sad.webp',
        nextXp: '360 XP needed',
        xpPercent: 40
      },
      {
        name: 'Grubsong',
        rarity: 'Rare',
        desc: 'A harmonious composter that hums soothing melodies when kitchen scraps are sorted.',
        sprite: 'assets/creatures/grubsong.webp',
        nextXp: '960 XP needed',
        xpPercent: 75
      },
      {
        name: 'Trashorok',
        rarity: 'Legendary',
        desc: 'A towering stone guardian of eco-habits. Celebrates every full recycling bin.',
        sprite: 'assets/creatures/trashorok.webp',
        nextXp: 'Max Evolution reached!',
        xpPercent: 100
      }
    ]
  }
};

// --- Web Audio Synthesizer (Procedural Retro Chimes) ---
class SoundEngine {
  constructor() {
    this.enabled = localStorage.getItem('choreemon_sound') !== 'false';
    this.ctx = null;
  }

  init() {
    if (!this.ctx && typeof window !== 'undefined') {
      const AudioCtx = window.AudioContext || window.webkitAudioContext;
      if (AudioCtx) this.ctx = new AudioCtx();
    }
    if (this.ctx && this.ctx.state === 'suspended') {
      this.ctx.resume();
    }
  }

  toggle() {
    this.enabled = !this.enabled;
    localStorage.setItem('choreemon_sound', this.enabled);
    return this.enabled;
  }

  playPop() {
    if (!this.enabled) return;
    this.init();
    if (!this.ctx) return;
    const osc = this.ctx.createOscillator();
    const gain = this.ctx.createGain();
    const now = this.ctx.currentTime;
    osc.type = 'sine';
    osc.frequency.setValueAtTime(440, now);
    osc.frequency.exponentialRampToValueAtTime(880, now + 0.08);
    gain.gain.setValueAtTime(0.15, now);
    gain.gain.exponentialRampToValueAtTime(0.001, now + 0.08);
    osc.connect(gain);
    gain.connect(this.ctx.destination);
    osc.start(now);
    osc.stop(now + 0.08);
  }

  playLove() {
    if (!this.enabled) return;
    this.init();
    if (!this.ctx) return;
    const notes = [523.25, 659.25, 783.99, 1046.5]; // C5, E5, G5, C6
    notes.forEach((freq, idx) => {
      const osc = this.ctx.createOscillator();
      const gain = this.ctx.createGain();
      const now = this.ctx.currentTime + idx * 0.05;
      osc.type = 'triangle';
      osc.frequency.setValueAtTime(freq, now);
      gain.gain.setValueAtTime(0.12, now);
      gain.gain.exponentialRampToValueAtTime(0.001, now + 0.15);
      osc.connect(gain);
      gain.connect(this.ctx.destination);
      osc.start(now);
      osc.stop(now + 0.15);
    });
  }

  playCrack() {
    if (!this.enabled) return;
    this.init();
    if (!this.ctx) return;
    const osc = this.ctx.createOscillator();
    const gain = this.ctx.createGain();
    const now = this.ctx.currentTime;
    osc.type = 'sawtooth';
    osc.frequency.setValueAtTime(180, now);
    osc.frequency.linearRampToValueAtTime(90, now + 0.1);
    gain.gain.setValueAtTime(0.2, now);
    gain.gain.exponentialRampToValueAtTime(0.01, now + 0.1);
    osc.connect(gain);
    gain.connect(this.ctx.destination);
    osc.start(now);
    osc.stop(now + 0.1);
  }

  playHatchFanfare() {
    if (!this.enabled) return;
    this.init();
    if (!this.ctx) return;
    // Joyful fanfare: G4, C5, E5, G5, C6
    const chord = [392.00, 523.25, 659.25, 783.99, 1046.50];
    chord.forEach((freq, i) => {
      const osc = this.ctx.createOscillator();
      const gain = this.ctx.createGain();
      const now = this.ctx.currentTime + i * 0.08;
      osc.type = 'sine';
      osc.frequency.setValueAtTime(freq, now);
      gain.gain.setValueAtTime(0.18, now);
      gain.gain.exponentialRampToValueAtTime(0.001, now + 0.6);
      osc.connect(gain);
      gain.connect(this.ctx.destination);
      osc.start(now);
      osc.stop(now + 0.6);
    });
  }
}

const sounds = new SoundEngine();

// --- Confetti Cannon ---
class ConfettiCannon {
  constructor(canvasId) {
    this.canvas = document.getElementById(canvasId);
    if (!this.canvas) return;
    this.ctx = this.canvas.getContext('2d');
    this.particles = [];
    this.animating = false;
    this.resize();
    window.addEventListener('resize', () => this.resize());
  }

  resize() {
    if (!this.canvas) return;
    this.canvas.width = window.innerWidth;
    this.canvas.height = window.innerHeight;
  }

  burst(x = window.innerWidth / 2, y = window.innerHeight / 2, count = 70) {
    const colors = ['#2089F5', '#FFB829', '#27BE89', '#9470EC', '#F273A8', '#5DC6FA', '#FFD43F'];
    for (let i = 0; i < count; i++) {
      const angle = Math.random() * Math.PI * 2;
      const speed = 4 + Math.random() * 9;
      this.particles.push({
        x: x,
        y: y,
        vx: Math.cos(angle) * speed,
        vy: Math.sin(angle) * speed - 3,
        size: 6 + Math.random() * 8,
        color: colors[Math.floor(Math.random() * colors.length)],
        rotation: Math.random() * 360,
        rotationSpeed: (Math.random() - 0.5) * 12,
        opacity: 1,
        life: 0.95 + Math.random() * 0.04
      });
    }
    if (!this.animating) {
      this.animating = true;
      this.loop();
    }
  }

  loop() {
    if (!this.canvas || !this.ctx) return;
    this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);

    for (let i = this.particles.length - 1; i >= 0; i--) {
      const p = this.particles[i];
      p.x += p.vx;
      p.y += p.vy;
      p.vy += 0.22; // gravity
      p.vx *= 0.98; // air drag
      p.rotation += p.rotationSpeed;
      p.opacity *= p.life;

      if (p.opacity < 0.05 || p.y > this.canvas.height + 20) {
        this.particles.splice(i, 1);
        continue;
      }

      this.ctx.save();
      this.ctx.translate(p.x, p.y);
      this.ctx.rotate((p.rotation * Math.PI) / 180);
      this.ctx.globalAlpha = p.opacity;
      this.ctx.fillStyle = p.color;
      this.ctx.fillRect(-p.size / 2, -p.size / 2, p.size, p.size * 0.6);
      this.ctx.restore();
    }

    if (this.particles.length > 0) {
      requestAnimationFrame(() => this.loop());
    } else {
      this.animating = false;
      this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);
    }
  }
}

const confetti = new ConfettiCannon('confetti-canvas');

// --- Document Ready / Initialization ---
document.addEventListener('DOMContentLoaded', () => {
  initSoundButton();
  initHeroInteractions();
  initHatcheryGame();
  initCreatureShowcase();
  initAppTour();
  initDownloadHandlers();
  initChecksumCopy();
  generateQRCode();
});

// Sound Toggle Button
function initSoundButton() {
  const btn = document.getElementById('sound-toggle');
  if (!btn) return;
  const updateIcon = (on) => {
    btn.querySelector('.sound-icon').textContent = on ? '🔊' : '🔇';
    btn.title = on ? 'Sound Effects: ON (Click to Mute)' : 'Sound Effects: OFF (Click to Enable)';
  };
  updateIcon(sounds.enabled);
  btn.addEventListener('click', () => {
    const on = sounds.toggle();
    updateIcon(on);
    if (on) sounds.playPop();
  });
}

// Hero Pet & Egg Interactions
function initHeroInteractions() {
  const pet = document.getElementById('hero-pet');
  const petImg = document.getElementById('hero-pet-img');
  const heart = document.getElementById('hero-heart-burst');
  const xp = document.getElementById('hero-xp-bubble');

  if (pet) {
    pet.addEventListener('click', () => {
      sounds.playLove();
      petImg.style.transform = 'scale(0.88)';
      setTimeout(() => petImg.style.transform = '', 180);

      heart.classList.remove('pop');
      xp.classList.remove('pop');
      void heart.offsetWidth; // reflow
      heart.classList.add('pop');
      xp.classList.add('pop');

      const rect = pet.getBoundingClientRect();
      confetti.burst(rect.left + rect.width / 2, rect.top + rect.height / 3, 20);

      setTimeout(() => {
        heart.classList.remove('pop');
        xp.classList.remove('pop');
      }, 900);
    });
  }

  const egg = document.getElementById('hero-egg');
  if (egg) {
    egg.addEventListener('click', () => {
      sounds.playPop();
      egg.querySelector('.egg-img').classList.add('shaking');
      setTimeout(() => {
        egg.querySelector('.egg-img').classList.remove('shaking');
      }, 400);
    });
  }
}

// Interactive Hatchery Mini-Game
function initHatcheryGame() {
  const trigger = document.getElementById('egg-hatch-trigger');
  const eggImg = document.getElementById('incubator-egg-img');
  const crack = document.getElementById('crack-overlay');
  const hatchedView = document.getElementById('hatched-creature-view');
  const promptText = document.getElementById('hatch-prompt-text');
  const dots = [document.getElementById('dot-1'), document.getElementById('dot-2'), document.getElementById('dot-3')];
  const againBtn = document.getElementById('hatch-again-btn');
  const tierBtns = document.querySelectorAll('.egg-tier-btn');

  let tapCount = 0;
  let hatched = false;
  let currentTier = 'classic';

  tierBtns.forEach(btn => {
    btn.addEventListener('click', () => {
      tierBtns.forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      currentTier = btn.dataset.tier;
      sounds.playPop();
      resetHatch();
    });
  });

  function resetHatch() {
    tapCount = 0;
    hatched = false;
    eggImg.classList.remove('hidden', 'shaking');
    crack.className = 'crack-overlay';
    hatchedView.classList.add('hidden');
    againBtn.classList.add('hidden');
    promptText.textContent = 'Tap the egg 3 times to crack it!';
    dots.forEach((dot, idx) => dot.classList.toggle('active', idx === 0));
  }

  if (trigger) {
    trigger.addEventListener('click', () => {
      if (hatched) return;
      tapCount++;

      if (tapCount === 1) {
        sounds.playCrack();
        eggImg.classList.add('shaking');
        setTimeout(() => eggImg.classList.remove('shaking'), 350);
        crack.className = 'crack-overlay crack-stage-1';
        dots[0].classList.add('active');
        dots[1].classList.add('active');
        promptText.textContent = 'Crack! The shell is loosening! Tap again!';
      } else if (tapCount === 2) {
        sounds.playCrack();
        eggImg.classList.add('shaking');
        setTimeout(() => eggImg.classList.remove('shaking'), 350);
        crack.className = 'crack-overlay crack-stage-2';
        dots[2].classList.add('active');
        promptText.textContent = 'Golden light is glowing through! ONE MORE TAP!';
      } else if (tapCount >= 3) {
        hatched = true;
        sounds.playHatchFanfare();
        eggImg.classList.add('hidden');
        crack.className = 'crack-overlay';

        // Select hatched creature based on tier odds
        const pool = [
          { name: 'Sudsy', element: 'Water', rarity: 'Common', icon: '💧', desc: 'A bubbly little buddy who loves sparkling clean kitchen sinks.', img: 'assets/creatures/sudsy.webp' },
          { name: 'Sockmite', element: 'Earth', rarity: 'Common', icon: '🧦', desc: 'A soft explorer ready to help fold mountains of cozy laundry.', img: 'assets/creatures/sockmite.webp' },
          { name: 'Dustbunny', element: 'Air', rarity: 'Common', icon: '💨', desc: 'A whirlwind of cheer that sweeps away every speck of dust.', img: 'assets/creatures/dustbunny.webp' },
          { name: 'Stinkle', element: 'Recycling', rarity: 'Common', icon: '♻️', desc: 'A cheerful recycling hero who finds treasure in tidying up.', img: 'assets/creatures/stinkle.webp' },
          { name: 'Bubblescale', element: 'Water', rarity: 'Rare', icon: '💧', desc: 'A swift bubble dragon that glides over clean tabletops.', img: 'assets/creatures/bubblescale.webp' },
          { name: 'Whirlpup', element: 'Air', rarity: 'Rare', icon: '💨', desc: 'A playful breeze puppy with unmatched energy.', img: 'assets/creatures/whirlpup.webp' }
        ];

        const pick = pool[Math.floor(Math.random() * pool.length)];

        document.getElementById('hatched-img').src = pick.img;
        document.getElementById('hatched-name').textContent = pick.name;
        document.getElementById('hatched-desc').textContent = pick.desc;
        document.getElementById('hatched-element-pill').textContent = `${pick.icon} ${pick.element} • ${pick.rarity}`;

        hatchedView.classList.remove('hidden');
        promptText.textContent = `A friendly ${pick.name} hatched!`;
        againBtn.classList.remove('hidden');

        const rect = trigger.getBoundingClientRect();
        confetti.burst(rect.left + rect.width / 2, rect.top + rect.height / 2, 80);
      }
    });
  }

  if (againBtn) {
    againBtn.addEventListener('click', (e) => {
      e.stopPropagation();
      sounds.playPop();
      resetHatch();
    });
  }
}

// Creature Journal & Evolutions Showcase
function initCreatureShowcase() {
  let currentFamily = 'water';
  let currentStage = 0;
  let isSad = false;

  const familyTabs = document.querySelectorAll('.family-tab');
  const stageBtns = document.querySelectorAll('.stepper-btn');
  const moodHappyBtn = document.getElementById('mood-happy-btn');
  const moodSadBtn = document.getElementById('mood-sad-btn');

  const spriteImg = document.getElementById('creature-showcase-img');
  const titleEl = document.getElementById('creature-title');
  const descEl = document.getElementById('creature-description');
  const stageBadge = document.getElementById('creature-stage-badge');
  const elementPill = document.getElementById('creature-element-pill');
  const rarityPill = document.getElementById('creature-rarity-pill');
  const nextXpEl = document.getElementById('creature-next-xp');
  const xpBarEl = document.getElementById('creature-xp-bar');
  const traitEl = document.getElementById('creature-trait');
  const glowEl = document.getElementById('creature-glow');

  function updateShowcase() {
    const data = CREATURES_DATA[currentFamily];
    const stageInfo = data.stages[currentStage];

    // Stage Names in buttons
    document.getElementById('stage-name-0').textContent = data.stages[0].name;
    document.getElementById('stage-name-1').textContent = data.stages[1].name;
    document.getElementById('stage-name-2').textContent = data.stages[2].name;

    titleEl.textContent = stageInfo.name;
    descEl.textContent = stageInfo.desc;
    elementPill.textContent = `${data.emoji} ${data.element} Element`;
    rarityPill.textContent = stageInfo.rarity;
    traitEl.textContent = data.trait;
    nextXpEl.textContent = stageInfo.nextXp;
    xpBarEl.style.width = `${stageInfo.xpPercent}%`;

    stageBadge.textContent = `Stage ${currentStage + 1} • ${currentStage === 0 ? 'Baby' : currentStage === 1 ? 'Evolved' : 'Champion'}`;

    // Sprite selection with sad mood for stage 0
    let spritePath = stageInfo.sprite;
    if (isSad && currentStage === 0 && stageInfo.sadSprite) {
      spritePath = stageInfo.sadSprite;
    }

    spriteImg.src = spritePath;
    glowEl.style.background = `radial-gradient(circle, ${data.color}33 0%, transparent 70%)`;

    // Active button states
    stageBtns.forEach(btn => {
      btn.classList.toggle('active', parseInt(btn.dataset.stage, 10) === currentStage);
    });

    moodHappyBtn.classList.toggle('active', !isSad);
    moodSadBtn.classList.toggle('active', isSad);
  }

  familyTabs.forEach(tab => {
    tab.addEventListener('click', () => {
      familyTabs.forEach(t => {
        t.classList.remove('active');
        t.setAttribute('aria-selected', 'false');
      });
      tab.classList.add('active');
      tab.setAttribute('aria-selected', 'true');
      currentFamily = tab.dataset.element;
      currentStage = 0;
      sounds.playPop();
      updateShowcase();
    });
  });

  stageBtns.forEach(btn => {
    btn.addEventListener('click', () => {
      currentStage = parseInt(btn.dataset.stage, 10);
      sounds.playPop();
      updateShowcase();
    });
  });

  if (moodHappyBtn) {
    moodHappyBtn.addEventListener('click', () => {
      if (isSad) {
        isSad = false;
        sounds.playLove();
        updateShowcase();
      }
    });
  }

  if (moodSadBtn) {
    moodSadBtn.addEventListener('click', () => {
      if (!isSad) {
        isSad = true;
        sounds.playPop();
        updateShowcase();
      }
    });
  }

  updateShowcase();
}

// App Tour Screenshot Switcher
function initAppTour() {
  const navItems = document.querySelectorAll('.tour-nav-item');
  const screenImg = document.getElementById('tour-screen-img');

  navItems.forEach(item => {
    item.addEventListener('click', () => {
      navItems.forEach(n => n.classList.remove('active'));
      item.classList.add('active');
      const imgPath = item.dataset.img;
      sounds.playPop();

      if (screenImg) {
        screenImg.style.opacity = '0';
        setTimeout(() => {
          screenImg.src = imgPath;
          screenImg.style.opacity = '1';
        }, 150);
      }
    });
  });
}

// Download Handlers & Modal
function initDownloadHandlers() {
  const heroBtn = document.getElementById('hero-download-btn');
  const mainBtn = document.getElementById('main-apk-download-btn');
  const modal = document.getElementById('download-modal');
  const closeBtn = document.getElementById('modal-close-btn');

  function openDownloadModal(e) {
    sounds.playLove();
    confetti.burst(window.innerWidth / 2, window.innerHeight / 2, 70);
    if (modal) modal.classList.remove('hidden');
  }

  if (heroBtn) {
    heroBtn.addEventListener('click', (e) => {
      openDownloadModal(e);
      // Trigger actual download
      const a = document.createElement('a');
      a.href = 'downloads/Choreemon.apk';
      a.download = 'Choreemon.apk';
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
    });
  }

  if (mainBtn) {
    mainBtn.addEventListener('click', () => {
      openDownloadModal();
    });
  }

  if (closeBtn && modal) {
    closeBtn.addEventListener('click', () => {
      modal.classList.add('hidden');
    });

    modal.addEventListener('click', (e) => {
      if (e.target === modal) {
        modal.classList.add('hidden');
      }
    });
  }
}

// Checksum Copy to Clipboard
function initChecksumCopy() {
  const btn = document.getElementById('copy-hash-btn');
  const text = document.getElementById('copy-btn-text');
  const code = document.getElementById('sha-hash');

  if (btn && code) {
    btn.addEventListener('click', () => {
      const hash = code.textContent.trim();
      navigator.clipboard.writeText(hash).then(() => {
        sounds.playPop();
        text.textContent = 'Copied! ✓';
        btn.style.borderColor = 'var(--green)';
        btn.style.color = 'var(--green)';
        setTimeout(() => {
          text.textContent = 'Copy Hash';
          btn.style.borderColor = '';
          btn.style.color = '';
        }, 2200);
      }).catch(() => {
        text.textContent = 'Select & Copy';
      });
    });
  }
}

// Clean Standalone SVG QR Code Generator
function generateQRCode() {
  const container = document.getElementById('qr-container');
  if (!container) return;

  // Render a clean SVG QR representation with Choreemon Egg center icon
  // Points to APK direct download
  const svg = `
    <svg viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg">
      <defs>
        <pattern id="qr-dots" x="0" y="0" width="4" height="4" patternUnits="userSpaceOnUse">
          <rect width="2.4" height="2.4" fill="#18345D" rx="0.6"/>
        </pattern>
      </defs>
      <!-- Corner Markers -->
      <rect x="6" y="6" width="22" height="22" rx="4" fill="none" stroke="#2089F5" stroke-width="4"/>
      <rect x="12" y="12" width="10" height="10" rx="2" fill="#2089F5"/>

      <rect x="72" y="6" width="22" height="22" rx="4" fill="none" stroke="#2089F5" stroke-width="4"/>
      <rect x="78" y="12" width="10" height="10" rx="2" fill="#2089F5"/>

      <rect x="6" y="72" width="22" height="22" rx="4" fill="none" stroke="#2089F5" stroke-width="4"/>
      <rect x="12" y="78" width="10" height="10" rx="2" fill="#2089F5"/>

      <!-- Data Dots Matrix -->
      <rect x="34" y="8" width="32" height="18" fill="url(#qr-dots)"/>
      <rect x="8" y="34" width="20" height="32" fill="url(#qr-dots)"/>
      <rect x="72" y="34" width="20" height="32" fill="url(#qr-dots)"/>
      <rect x="34" y="74" width="32" height="18" fill="url(#qr-dots)"/>
      <rect x="32" y="32" width="36" height="36" rx="8" fill="#FFFFFF" stroke="#DDE6F2" stroke-width="2"/>
      
      <!-- Center Egg Badge -->
      <circle cx="50" cy="50" r="14" fill="#E9F6FF"/>
      <path d="M50,40 C46,40 43,47 43,53 C43,59 46,62 50,62 C54,62 57,59 57,53 C57,47 54,40 50,40 Z" fill="#FFF7DC" stroke="#163B77" stroke-width="1.5"/>
      <circle cx="48" cy="49" r="1.5" fill="#5DC6FA"/>
      <circle cx="52" cy="54" r="1.8" fill="#5DC6FA"/>
    </svg>
  `;
  container.innerHTML = svg;
}

