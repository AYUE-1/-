<template>
  <div class="articles-page">
    <AppHeader />
    <div class="container">
      <div class="page-header"><h1 class="page-title">📚 养宠科普</h1><p class="page-subtitle">科学养宠，让爱更专业</p></div>
      <div class="articles-layout">
        <div class="left-nav">
          <el-menu :default-active="activeCategory" @select="onCategorySelect">
            <el-menu-item index="ALL">全部文章</el-menu-item>
            <el-menu-item index="CAT">🐱 猫咪</el-menu-item>
            <el-menu-item index="DOG">🐶 狗狗</el-menu-item>
            <el-menu-item index="GENERAL">📋 综合</el-menu-item>
            <el-menu-item index="CHECKLIST">✅ 养前清单</el-menu-item>
          </el-menu>
          <el-button type="primary" style="margin-top:12px;width:100%" @click="handleSubmit">✍️ 投递文章</el-button>
          <el-button style="margin-top:8px;width:100%" @click="$router.push('/assessment')">🐾 去养宠测评</el-button>
        </div>
        <div class="right-content">
          <div v-if="loading"><el-skeleton :rows="3" animated /></div>
          <div v-else-if="articles.length === 0"><el-empty description="暂无文章" /></div>
          <div v-else class="articles-grid">
            <div v-for="a in articles" :key="a.id" class="article-card card-hover" @click="$router.push(`/education/${a.id}`)">
              <div class="ac-cover" :style="{ background: randomGradient(a.id) }">
                <span class="ac-emoji">{{ getCategoryEmoji(a.category) }}</span>
              </div>
              <div class="ac-body">
                <h3>{{ a.title }}</h3>
                <p class="ac-summary text-ellipsis-2">{{ a.summary || '暂无摘要' }}</p>
                <div class="ac-meta">
                  <span>{{ a.categoryName }}</span>
                  <span>👁 {{ a.viewCount }}</span>
                  <span>{{ formatTime(a.createdAt) }}</span>
                </div>
              </div>
            </div>
          </div>
          <div class="pagination-wrap" v-if="total > 12">
            <el-pagination background layout="prev, pager, next" :total="total" :page-size="12" v-model:current-page="page" @current-change="fetchArticles" />
          </div>
        </div>
      </div>
    </div>
    <AppFooter />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getArticles } from '@/api/article'
import { useAuthStore } from '@/stores/auth'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'

const router = useRouter()
const authStore = useAuthStore()

function handleSubmit() {
  if (!authStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return router.push({ path: '/login', query: { redirect: '/education/submit' } })
  }
  router.push('/education/submit')
}

const articles = ref([])
const loading = ref(true)
const page = ref(1)
const total = ref(0)
const activeCategory = ref('ALL')

function getCategoryEmoji(c) { return c === 'CAT' ? '🐱' : c === 'DOG' ? '🐶' : c === 'CHECKLIST' ? '✅' : '📋' }
function randomGradient(id) {
  const gradients = ['#FF7F50,#FFA07A', '#98D8C8,#C8F0E4', '#FFB899,#FFD1B8', '#A8D8EA,#C8E8F4']
  return `linear-gradient(135deg, ${gradients[id % gradients.length]})`
}
function formatTime(t) { return t ? new Date(t).toLocaleDateString() : '' }

function onCategorySelect(cat) { activeCategory.value = cat; page.value = 1; fetchArticles() }

async function fetchArticles() {
  loading.value = true
  try {
    const res = await getArticles({ page: page.value, size: 12, category: activeCategory.value === 'ALL' ? '' : activeCategory.value })
    articles.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally { loading.value = false }
}

onMounted(fetchArticles)
</script>

<style lang="scss" scoped>
.articles-page { min-height: 100vh; background: var(--color-bg); }
.articles-layout { display: flex; gap: 24px; }
.left-nav { width: 200px; flex-shrink: 0; }
.right-content { flex: 1; }
.articles-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; @media (max-width: 768px) { grid-template-columns: 1fr; } }
.article-card { background: #fff; border-radius: var(--border-radius); overflow: hidden; cursor: pointer; }
.ac-cover { height: 120px; display: flex; align-items: center; justify-content: center; .ac-emoji { font-size: 48px; } }
.ac-body { padding: 16px; h3 { font-size: 16px; margin-bottom: 8px; } .ac-summary { font-size: 13px; color: var(--color-text-secondary); margin-bottom: 8px; } .ac-meta { font-size: 12px; color: #999; display: flex; gap: 8px; } }
.pagination-wrap { display: flex; justify-content: center; padding: 32px 0; }
@media (max-width: 768px) { .articles-layout { flex-direction: column; } .left-nav { width: 100%; } }
</style>
