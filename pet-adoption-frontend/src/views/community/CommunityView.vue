<template>
  <div class="community-page">
    <AppHeader />
    <div class="container">
      <div class="page-header"><h1 class="page-title">💬 宠友社区</h1><p class="page-subtitle">分享领养喜悦，帮助走失宠物回家</p></div>
      <div class="comm-actions">
        <el-button type="primary" @click="$router.push('/community/publish')" v-if="authStore.isLoggedIn">✍️ 发帖</el-button>
        <el-radio-group v-model="postType" @change="fetchPosts">
          <el-radio-button value="">全部</el-radio-button>
          <el-radio-button value="SHARING">🐾 领养晒图</el-radio-button>
          <el-radio-button value="LOST_FOUND">🔍 失宠寻宠</el-radio-button>
        </el-radio-group>
      </div>
      <div v-if="loading"><el-skeleton :rows="3" animated /></div>
      <div v-else-if="posts.length === 0"><el-empty description="暂无帖子，快来发第一帖吧" /></div>
      <div v-else class="posts-grid">
        <div v-for="post in posts" :key="post.id" class="post-card card-hover" @click="$router.push(`/community/${post.id}`)">
          <div class="pc-header">
            <el-avatar :size="32">{{ post.username?.charAt(0) }}</el-avatar>
            <span class="pc-username">{{ post.username }}</span>
            <el-tag size="small">{{ post.typeName }}</el-tag>
          </div>
          <h3>{{ post.title }}</h3>
          <p class="text-ellipsis-2">{{ post.content }}</p>
          <div class="pc-footer">
            <span>❤️ {{ post.likeCount }}</span>
            <span>💬 {{ post.commentCount }}</span>
            <span>👁 {{ post.viewCount }}</span>
          </div>
        </div>
      </div>
    </div>
    <AppFooter />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getCommunityPosts } from '@/api/community'
import { useAuthStore } from '@/stores/auth'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'

const authStore = useAuthStore()
const posts = ref([])
const loading = ref(true)
const postType = ref('')

async function fetchPosts() {
  loading.value = true
  try { const res = await getCommunityPosts({ page: 1, size: 12, type: postType.value }); posts.value = res.data?.records || [] }
  finally { loading.value = false }
}

onMounted(fetchPosts)
</script>

<style lang="scss" scoped>
.community-page { min-height: 100vh; background: var(--color-bg); }
.comm-actions { display: flex; gap: 16px; margin-bottom: 20px; align-items: center; flex-wrap: wrap; }
.posts-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; @media (max-width: 768px) { grid-template-columns: 1fr; } }
.post-card { background: #fff; padding: 20px; border-radius: var(--border-radius); cursor: pointer;
  h3 { font-size: 16px; margin: 10px 0; }
  p { font-size: 14px; color: var(--color-text-secondary); }
}
.pc-header { display: flex; align-items: center; gap: 8px; .pc-username { font-weight: 500; flex: 1; } }
.pc-footer { display: flex; gap: 16px; margin-top: 12px; font-size: 13px; color: var(--color-text-secondary); }
</style>
