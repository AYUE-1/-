<template>
  <div class="reviews-page">
    <AppHeader />
    <div class="container">
      <div class="page-header"><h1 class="page-title">⭐ 用户评价</h1><p class="page-subtitle">领养人与送养人之间的双向信任</p></div>
      <div v-if="reviews.length === 0"><el-empty description="暂无评价" /></div>
      <div v-else class="reviews-list">
        <div v-for="r in reviews" :key="r.id" class="review-card">
          <div class="rv-header">
            <span class="rv-user">{{ r.reviewerName }}</span>
            <el-rate v-model="r.rating" disabled show-score text-color="#FF7F50" />
          </div>
          <p class="rv-content">{{ r.content }}</p>
          <div class="rv-meta">
            <el-tag size="small">{{ r.roleType === 'ADOPTER' ? '领养人' : '送养方' }}</el-tag>
            <span>{{ formatTime(r.createdAt) }}</span>
          </div>
        </div>
      </div>
    </div>
    <AppFooter />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getUserReviews } from '@/api/trust'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'

const route = useRoute()
const reviews = ref([])
function formatTime(t) { return t ? new Date(t).toLocaleDateString() : '' }

onMounted(async () => {
  try { const res = await getUserReviews(route.params.userId); reviews.value = res.data || [] } catch {}
})
</script>

<style lang="scss" scoped>
.reviews-page { min-height: 100vh; background: var(--color-bg); }
.reviews-list { max-width: 600px; margin: 0 auto; }
.review-card { background: #fff; padding: 20px; border-radius: var(--border-radius); margin-bottom: 12px;
  .rv-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; .rv-user { font-weight: 600; } }
  .rv-content { font-size: 15px; line-height: 1.6; margin-bottom: 8px; }
  .rv-meta { display: flex; gap: 8px; align-items: center; font-size: 12px; color: #999; }
}
</style>
