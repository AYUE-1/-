<template>
  <Teleport to="body">
    <div
      v-if="visible"
      class="lso-overlay"
      :class="phaseClass"
      role="alert"
      aria-live="polite"
    >
      <div class="lso-backdrop" />

      <div class="lso-card">
        <!-- 粒子层 -->
        <div class="lso-particles">
          <span
            v-for="(p, i) in particles"
            :key="i"
            class="lso-particle"
            :style="p.style"
          >{{ p.emoji }}</span>
        </div>

        <!-- 主 emoji -->
        <div class="lso-emoji-burst">🎉</div>

        <!-- 欢迎文字 -->
        <div class="lso-text">
          <h2 class="lso-greeting">欢迎回来<span class="lso-nickname">，{{ displayName }}</span></h2>
          <p class="lso-sub">{{ subMessage }}</p>
        </div>

        <!-- 底部爪印装饰 -->
        <div class="lso-footer-paws">
          <span>🐾</span><span>🐾</span><span>🐾</span>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { ref, computed, watch, onBeforeUnmount } from 'vue'

const props = defineProps({
  visible: { type: Boolean, default: false },
  nickname: { type: String, default: '' },
  role: { type: String, default: 'USER' }
})

const emit = defineEmits(['done'])

const phase = ref('entering')
let timers = []

const displayName = computed(() => props.nickname || '用户')

const subMessage = computed(() => {
  switch (props.role) {
    case 'ADMIN': return '正在进入管理中心...'
    case 'SHELTER': return '正在进入机构工作台...'
    default: return '愿每一只宠物都能找到温暖的家 🏠'
  }
})

const phaseClass = computed(() => `lso-phase-${phase.value}`)

// 生成随机粒子
const emojiPool = ['🐾', '❤️', '🏠', '✨', '🎉', '🐾', '❤️', '🐱', '🐶', '💛', '🌟']
const particles = ref([])

function generateParticles() {
  particles.value = Array.from({ length: 16 }, (_, i) => {
    const angle = (i / 16) * Math.PI * 2 + (Math.random() - 0.5) * 0.5
    const radius = 80 + Math.random() * 120
    return {
      emoji: emojiPool[i % emojiPool.length],
      style: {
        '--start-x': `${Math.cos(angle) * radius}px`,
        '--start-y': `${Math.sin(angle) * radius * 0.6}px`,
        '--drift-x': `${(Math.random() - 0.5) * 60}px`,
        '--duration': `${1.5 + Math.random() * 1.2}s`,
        '--delay': `${Math.random() * 0.4}s`,
        '--size': `${18 + Math.random() * 14}px`,
        '--rotation': `${(Math.random() - 0.5) * 80}deg`
      }
    }
  })
}

function clearAllTimers() {
  timers.forEach(t => clearTimeout(t))
  timers = []
}

function startSequence() {
  clearAllTimers()
  phase.value = 'entering'
  generateParticles()

  timers.push(setTimeout(() => { phase.value = 'visible' }, 50))

  // 1.8 秒后开始退出
  timers.push(setTimeout(() => { phase.value = 'exiting' }, 1800))

  // 2 秒后通知父组件完成
  timers.push(setTimeout(() => { emit('done') }, 2050))
}

watch(() => props.visible, (v) => {
  if (v) startSequence()
})

onBeforeUnmount(() => clearAllTimers())
</script>

<style lang="scss" scoped>
// ===== 覆盖层 =====
.lso-overlay {
  position: fixed;
  inset: 0;
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: opacity 0.35s ease;
}

.lso-backdrop {
  position: absolute;
  inset: 0;
  background: rgba(250, 250, 245, 0.88);
  backdrop-filter: blur(6px);
  -webkit-backdrop-filter: blur(6px);
}

// ===== 动画阶段 =====
.lso-phase-entering {
  opacity: 0;
  .lso-card { transform: scale(0.6); }
  .lso-emoji-burst { transform: scale(0) rotate(-10deg); opacity: 0; }
  .lso-greeting,
  .lso-sub { transform: translateY(20px); opacity: 0; }
  .lso-footer-paws { opacity: 0; }
}

.lso-phase-visible {
  opacity: 1;
  .lso-card {
    transform: scale(1);
    transition: transform 0.45s cubic-bezier(0.34, 1.56, 0.64, 1);
  }
  .lso-emoji-burst {
    transform: scale(1) rotate(0deg);
    opacity: 1;
    transition: transform 0.45s cubic-bezier(0.34, 1.56, 0.64, 1) 0.1s,
                opacity 0.3s ease 0.1s;
  }
  .lso-greeting {
    transform: translateY(0);
    opacity: 1;
    transition: transform 0.4s ease 0.3s, opacity 0.3s ease 0.3s;
  }
  .lso-sub {
    transform: translateY(0);
    opacity: 1;
    transition: transform 0.4s ease 0.48s, opacity 0.3s ease 0.48s;
  }
  .lso-footer-paws {
    opacity: 1;
    transition: opacity 0.3s ease 0.6s;
  }
}

.lso-phase-exiting {
  opacity: 0;
  transition: opacity 0.25s ease;
}

// ===== 卡片 =====
.lso-card {
  position: relative;
  z-index: 1;
  background: #fff;
  border-radius: 24px;
  padding: 56px 52px 40px;
  text-align: center;
  box-shadow: 0 8px 32px rgba(0,0,0,0.10);
  max-width: 420px;
  width: 90%;
  overflow: visible;
  animation: softPulse 3s ease-in-out infinite;

  @media (max-width: 576px) {
    padding: 40px 28px 32px;
    border-radius: 20px;
  }
}

@keyframes softPulse {
  0%, 100% { box-shadow: 0 8px 32px rgba(255,127,80,0.12); }
  50%      { box-shadow: 0 12px 44px rgba(255,127,80,0.22); }
}

// ===== 粒子 =====
.lso-particles {
  position: absolute;
  top: 50%;
  left: 50%;
  pointer-events: none;
}

.lso-particle {
  position: absolute;
  font-size: var(--size);
  animation: particleFloat var(--duration) ease-out var(--delay) both;
}

@keyframes particleFloat {
  0%   { transform: translate(var(--start-x), var(--start-y)) rotate(0deg); opacity: 0; }
  15%  { opacity: 1; }
  70%  { opacity: 0.7; }
  100% { transform: translate(
           calc(var(--start-x) + var(--drift-x)),
           calc(var(--start-y) - 140px)
         ) rotate(var(--rotation)); opacity: 0; }
}

// ===== Emoji 弹入 =====
.lso-emoji-burst {
  font-size: 56px;
  margin-bottom: 20px;
  line-height: 1;
  display: inline-block;

  @media (max-width: 576px) { font-size: 42px; margin-bottom: 14px; }
}

// ===== 文字 =====
.lso-text {
  margin-bottom: 20px;
}

.lso-greeting {
  font-size: 26px;
  font-weight: 700;
  color: var(--color-text, #2C3E50);
  margin: 0 0 10px;
  line-height: 1.4;

  @media (max-width: 576px) { font-size: 21px; }
}

.lso-nickname {
  color: var(--color-primary, #FF7F50);
  font-weight: 800;
}

.lso-sub {
  font-size: 15px;
  color: var(--color-text-secondary, #7F8C8D);
  margin: 0;
  line-height: 1.6;
}

// ===== 底部爪印 =====
.lso-footer-paws {
  font-size: 18px;
  display: flex;
  justify-content: center;
  gap: 10px;
  letter-spacing: 6px;
  animation: footerWiggle 1.5s ease-in-out infinite;

  span {
    display: inline-block;
    animation: pawHop 1.5s ease-in-out infinite;
    &:nth-child(2) { animation-delay: 0.2s; }
    &:nth-child(3) { animation-delay: 0.4s; }
  }
}

@keyframes footerWiggle {
  0%, 100% { transform: translateY(0); }
  50%      { transform: translateY(-4px); }
}

@keyframes pawHop {
  0%, 100% { transform: translateY(0) scale(1); }
  30%      { transform: translateY(-6px) scale(1.15); }
  60%      { transform: translateY(0) scale(1); }
}
</style>
