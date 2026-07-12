<template>
  <div class="home-page">
    <AppHeader />

    <!-- Hero 横幅 -->
    <section class="hero-section">
      <div class="hero-content container">
        <h1 class="hero-title">为流浪动物找到温暖的家</h1>
        <p class="hero-subtitle">领养代替购买，用爱终止流浪</p>
        <div class="hero-actions">
          <el-button type="primary" size="large" round class="hero-btn" @click="router.push('/pets')">
            🐾 浏览待领养宠物
          </el-button>
          <el-button size="large" round class="hero-btn-secondary" @click="router.push('/assessment')">
            🎯 养宠测评
          </el-button>
        </div>
        <div class="hero-stats">
          <div class="stat-item"><span class="stat-num">{{ stats.petCount }}+</span><span>待领养宠物</span></div>
          <div class="stat-item"><span class="stat-num">{{ stats.rescueCount }}+</span><span>救助成功</span></div>
          <div class="stat-item"><span class="stat-num">{{ stats.adoptionCount }}+</span><span>已领养</span></div>
        </div>
      </div>
    </section>

    <!-- 快捷入口 -->
    <section class="section quick-links">
      <div class="container">
        <h2 class="section-title">探索爪印</h2>
        <el-row :gutter="16">
          <el-col v-for="link in quickLinks" :key="link.path" :xs="12" :sm="6" :md="6">
            <div class="quick-card card-hover" @click="router.push(link.path)">
              <span class="qc-icon">{{ link.icon }}</span>
              <h3>{{ link.label }}</h3>
              <p>{{ link.desc }}</p>
            </div>
          </el-col>
        </el-row>
      </div>
    </section>

    <!-- 分类 -->
    <section class="section">
      <div class="container">
        <h2 class="section-title">快速查找</h2>
        <el-row :gutter="20">
          <el-col v-for="cat in categories" :key="cat.name" :xs="8" :sm="8" :md="8">
            <div class="category-card card-hover" @click="goToCategory(cat.name)">
              <div class="category-icon">{{ cat.icon }}</div>
              <h3 class="category-name">{{ cat.name }}</h3>
              <p class="category-desc">{{ cat.desc }}</p>
            </div>
          </el-col>
        </el-row>
      </div>
    </section>

    <!-- 最新宠物 -->
    <section class="section">
      <div class="container">
        <h2 class="section-title">最新待领养</h2>
        <div v-if="loading"><el-skeleton :rows="2" animated /></div>
        <div v-else-if="latestPets.length === 0"><el-empty description="暂无待领养宠物" /></div>
        <div v-else class="pet-grid">
          <div v-for="pet in latestPets" :key="pet.id" class="pet-card card-hover" @click="router.push(`/pets/${pet.id}`)">
            <div class="pc-image">
              <el-image :src="getCoverUrl(pet.coverImage)" fit="cover" style="width:100%;height:200px">
                <template #error><div class="pc-placeholder">🐾</div></template>
              </el-image>
              <el-tag class="pc-status" :type="statusTagType(pet.status)">{{ statusText(pet.status) }}</el-tag>
            </div>
            <div class="pc-body">
              <h3>{{ pet.name }}</h3>
              <p class="pc-breed">{{ pet.breed || pet.categoryName }}</p>
              <div class="pc-meta">
                <span>{{ pet.gender === 'Male' ? '♂' : pet.gender === 'Female' ? '♀' : '' }} {{ pet.age }}</span>
                <span>📍 {{ pet.location || '未知' }}</span>
              </div>
            </div>
          </div>
        </div>
        <div style="text-align:center;margin-top:24px">
          <el-button type="primary" @click="router.push('/pets')">查看更多 →</el-button>
        </div>
      </div>
    </section>

    <!-- 领养流程 -->
    <section class="section flow-section">
      <div class="container">
        <h2 class="section-title" style="color:#fff;border-left-color:#fff">领养流程</h2>
        <el-row :gutter="20">
          <el-col v-for="(step, idx) in steps" :key="idx" :xs="12" :sm="6" :md="6">
            <div class="step-card">
              <div class="step-num">{{ idx + 1 }}</div>
              <div class="step-icon">{{ step.icon }}</div>
              <h4>{{ step.title }}</h4>
              <p>{{ step.desc }}</p>
            </div>
          </el-col>
        </el-row>
      </div>
    </section>

    <AppFooter />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getPetList } from '@/api/pet'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'

const router = useRouter()
const latestPets = ref([])
const loading = ref(true)
const stats = reactive({ petCount: 120, rescueCount: 360, adoptionCount: 520 })

const quickLinks = [
  { path: '/assessment', icon: '🎯', label: '养宠测评', desc: '智能推荐最适合你的宠物' },
  { path: '/rescue', icon: '🆘', label: '流浪救助', desc: '发布或认领救助任务' },
  { path: '/education', icon: '📚', label: '养宠科普', desc: '科学养宠知识库' },
  { path: '/community', icon: '💬', label: '宠友社区', desc: '分享领养喜悦' }
]
const categories = [
  { name: '猫咪', icon: '🐱', desc: '优雅独立的喵星人' },
  { name: '狗狗', icon: '🐶', desc: '忠诚可爱的汪星人' },
  { name: '其他', icon: '🐰', desc: '更多可爱小动物' }
]
const steps = [
  { icon: '🔍', title: '浏览宠物', desc: '在平台上找到心仪的宠物' },
  { icon: '📝', title: '提交申请', desc: '填写领养申请表' },
  { icon: '✅', title: '审核沟通', desc: '送养方审核并与您沟通' },
  { icon: '🏠', title: '带它回家', desc: '签订协议，迎接新成员' }
]

function statusTagType(s) { return s === 'AVAILABLE' ? 'success' : s === 'RESERVED' ? 'warning' : 'info' }
function statusText(s) { return s === 'AVAILABLE' ? '待领养' : s === 'RESERVED' ? '已预留' : s }

function getCoverUrl(url) {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://') || url.startsWith('data:')) return url
  if (url.startsWith('/')) return url
  const baseURL = import.meta.env.PROD ? '' : 'http://localhost:8080'
  return baseURL + '/' + url
}

function goToCategory(name) {
  const categoryMap = { '猫咪': 1, '狗狗': 2, '其他': 3 }
  router.push({ path: '/pets', query: { categoryId: categoryMap[name] } })
}

onMounted(async () => {
  try {
    const res = await getPetList({ page: 1, size: 4 })
    latestPets.value = res.data?.records || []
  } finally { loading.value = false }
})
</script>

<style lang="scss" scoped>
.home-page { background: var(--color-bg); }

// Hero
.hero-section {
  background: linear-gradient(135deg, #FF7F50 0%, #FFA07A 50%, #FFB899 100%);
  padding: 80px 0 60px; position: relative; overflow: hidden;
  &::before {
    content: '🐾🐾🐾🐾🐾🐾🐾🐾🐾🐾';
    position: absolute; top: 20px; left: 0; right: 0;
    font-size: 40px; opacity: 0.1; white-space: nowrap; animation: slidePaws 20s linear infinite;
  }
}
@keyframes slidePaws { from { transform: translateX(0); } to { transform: translateX(-50%); } }
.hero-content { text-align: center; position: relative; z-index: 1; }
.hero-title { font-size: 42px; color: #fff; font-weight: 800; margin-bottom: 12px; text-shadow: 0 2px 4px rgba(0,0,0,0.1); }
.hero-subtitle { font-size: 20px; color: rgba(255,255,255,0.9); margin-bottom: 28px; }
.hero-actions { display: flex; gap: 16px; justify-content: center; margin-bottom: 40px; }
.hero-btn { background: #fff; color: #FF7F50; border-color: #fff; font-size: 16px; padding: 14px 32px; &:hover { background: #FFF1E9; } }
.hero-btn-secondary { background: rgba(255,255,255,0.2); color: #fff; border: 2px solid rgba(255,255,255,0.5); font-size: 16px; padding: 14px 32px; &:hover { background: rgba(255,255,255,0.3); } }
.hero-stats { display: flex; justify-content: center; gap: 48px; }
.stat-item { text-align: center; color: #fff; .stat-num { font-size: 32px; font-weight: 800; display: block; } span { font-size: 14px; opacity: 0.9; } }

// Quick links
.quick-links { margin-top: 48px; }
.quick-card {
  background: #fff; padding: 28px 20px; border-radius: var(--border-radius); text-align: center; cursor: pointer; margin-bottom: 16px;
  .qc-icon { font-size: 40px; display: block; margin-bottom: 10px; }
  h3 { font-size: 17px; margin-bottom: 6px; }
  p { font-size: 13px; color: var(--color-text-secondary); }
}

// Category
.category-card {
  background: #fff; padding: 32px 20px; border-radius: var(--border-radius); text-align: center; cursor: pointer;
  .category-icon { font-size: 48px; display: block; margin-bottom: 10px; }
  .category-name { font-size: 18px; margin-bottom: 6px; }
  .category-desc { font-size: 13px; color: var(--color-text-secondary); }
}

// Pet card
.pet-card { background: #fff; border-radius: var(--border-radius); overflow: hidden; cursor: pointer; }
.pc-image { position: relative; .pc-placeholder { display: flex; align-items: center; justify-content: center; height: 200px; background: #f8f9fa; font-size: 48px; } .pc-status { position: absolute; top: 8px; right: 8px; } }
.pc-body { padding: 16px; h3 { font-size: 17px; margin-bottom: 4px; } .pc-breed { color: var(--color-text-secondary); font-size: 13px; margin-bottom: 8px; } .pc-meta { font-size: 12px; color: #999; display: flex; gap: 8px; } }

// Steps
.flow-section { background: var(--color-text); padding: 60px 0; margin: 0; }
.step-card { background: rgba(255,255,255,0.1); padding: 28px 20px; border-radius: var(--border-radius); text-align: center; color: #fff; margin-bottom: 16px;
  .step-num { width: 32px; height: 32px; border-radius: 50%; background: var(--color-primary); display: flex; align-items: center; justify-content: center; margin: 0 auto 10px; font-weight: 700; font-size: 14px; }
  .step-icon { font-size: 32px; margin-bottom: 8px; }
  h4 { margin-bottom: 6px; } p { font-size: 13px; opacity: 0.8; }
}

@media (max-width: 768px) {
  .hero-title { font-size: 28px; }
  .hero-stats { gap: 20px; }
}
</style>
