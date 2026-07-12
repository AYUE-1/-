<template>
  <div class="volunteer-detail-page">
    <AppHeader />
    <div class="container">
      <!-- Loading -->
      <div v-if="loading" class="loading-wrap">
        <el-skeleton :rows="8" animated />
      </div>

      <!-- 不存在 -->
      <div v-else-if="!volunteer" class="empty-wrap">
        <el-empty description="该志愿者不存在或尚未认证">
          <el-button type="primary" @click="$router.push('/welfare')">返回公益板块</el-button>
        </el-empty>
      </div>

      <!-- 志愿者信息 -->
      <template v-else>
        <!-- 基本信息卡片 -->
        <div class="profile-card">
          <div class="profile-header">
            <el-avatar :size="80" :src="volunteer.avatar">
              {{ (volunteer.userName || '?')[0] }}
            </el-avatar>
            <div class="profile-info">
              <div class="name-row">
                <h1>{{ volunteer.userName }}</h1>
                <el-tag type="success" size="large" effect="dark">认证志愿者</el-tag>
              </div>
              <p class="meta">
                <span v-if="volunteer.region">📍 {{ volunteer.region }}</span>
                <span v-if="volunteer.approvedAt">🕐 {{ formatDate(volunteer.approvedAt) }} 加入</span>
              </p>
            </div>
          </div>

          <!-- 技能标签 -->
          <div class="info-rows" v-if="volunteer.skills">
            <div class="info-label">技能特长</div>
            <div class="info-value">
              <el-tag v-for="(skill, i) in skillsList" :key="i" class="skill-tag" type="info">{{ skill }}</el-tag>
            </div>
          </div>
          <div class="info-rows" v-if="volunteer.availableTime">
            <div class="info-label">可服务时间</div>
            <div class="info-value">{{ volunteer.availableTime }}</div>
          </div>
        </div>

        <!-- 救助统计 -->
        <div class="stats-section">
          <h2 class="section-title">🏆 救助成就</h2>
          <div class="rescue-stat-card">
            <div class="stat-number">{{ volunteer.rescueCount || 0 }}</div>
            <div class="stat-label">已救助动物</div>
          </div>
        </div>

        <!-- 救助动物列表 -->
        <div class="rescued-section" v-if="volunteer.rescuedAnimals && volunteer.rescuedAnimals.length">
          <h3 class="sub-title">救助记录</h3>
          <div class="rescued-list">
            <div
              class="rescued-item"
              v-for="animal in volunteer.rescuedAnimals"
              :key="animal.id"
              @click="$router.push(`/rescue/${animal.id}`)"
            >
              <div class="animal-info">
                <span class="animal-type">{{ getAnimalEmoji(animal.animalType) }} {{ animal.animalType || '未知' }}</span>
                <span class="animal-title">{{ animal.title }}</span>
              </div>
              <div class="animal-meta">
                <span v-if="animal.rescuedAt">救助时间: {{ formatDate(animal.rescuedAt) }}</span>
                <el-icon><ArrowRight /></el-icon>
              </div>
            </div>
          </div>
        </div>

        <!-- 公益事迹 -->
        <div class="welfare-section">
          <h2 class="section-title">💚 公益事迹</h2>
          <div class="welfare-content" v-if="volunteer.welfareActivities">
            <p>{{ volunteer.welfareActivities }}</p>
          </div>
          <el-empty v-else description="该志愿者尚未填写公益经历" :image-size="80" />
        </div>
      </template>
    </div>
    <AppFooter />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getVolunteerDetail } from '@/api/trust'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'

const route = useRoute()
const volunteer = ref(null)
const loading = ref(true)

const skillsList = computed(() => {
  if (!volunteer.value?.skills) return []
  return volunteer.value.skills.split(/[,，、]/).map(s => s.trim()).filter(Boolean)
})

function formatDate(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' })
}

function getAnimalEmoji(type) {
  const map = { '猫': '🐱', '狗': '🐶', '其他': '🐾', '小型犬': '🐶', '中型犬': '🐕', '大型犬': '🦮' }
  return map[type] || '🐾'
}

onMounted(async () => {
  loading.value = true
  try {
    const res = await getVolunteerDetail(route.params.id)
    volunteer.value = res.data
  } catch {
    volunteer.value = null
  } finally {
    loading.value = false
  }
})
</script>

<style lang="scss" scoped>
.volunteer-detail-page {
  min-height: 100vh;
  background: var(--color-bg);
}
.container {
  max-width: 800px;
  margin: 0 auto;
  padding: 32px 16px 64px;
}
.loading-wrap {
  padding: 40px;
  background: #fff;
  border-radius: var(--border-radius);
}
.empty-wrap {
  padding: 80px 0;
}

// 基本信息卡片
.profile-card {
  background: #fff;
  border-radius: 16px;
  padding: 32px;
  margin-bottom: 24px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
}
.profile-header {
  display: flex;
  align-items: center;
  gap: 24px;
  margin-bottom: 24px;
}
.name-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
  h1 { font-size: 24px; margin: 0; }
}
.meta {
  color: var(--color-text-secondary);
  font-size: 14px;
  span { margin-right: 20px; }
}
.info-rows {
  display: flex;
  margin-bottom: 12px;
}
.info-label {
  width: 90px;
  flex-shrink: 0;
  color: var(--color-text-secondary);
  font-size: 14px;
}
.info-value {
  flex: 1;
  font-size: 14px;
}
.skill-tag {
  margin-right: 8px;
  margin-bottom: 4px;
}

// 救助统计
.stats-section {
  margin-bottom: 24px;
}
.section-title {
  font-size: 20px;
  margin-bottom: 16px;
}
.rescue-stat-card {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  border-radius: 16px;
  padding: 32px;
  text-align: center;
  color: #fff;
}
.stat-number {
  font-size: 48px;
  font-weight: bold;
}
.stat-label {
  font-size: 16px;
  margin-top: 4px;
  opacity: 0.9;
}

// 救助记录
.rescued-section {
  background: #fff;
  border-radius: 16px;
  padding: 24px 32px;
  margin-bottom: 24px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
}
.sub-title {
  font-size: 16px;
  margin-bottom: 16px;
}
.rescued-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.rescued-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  border-radius: 10px;
  background: var(--color-bg);
  cursor: pointer;
  transition: background 0.2s;
  &:hover { background: var(--el-color-primary-light-9); }
}
.animal-info {
  display: flex;
  gap: 12px;
  align-items: center;
}
.animal-type {
  font-size: 13px;
  color: var(--color-text-secondary);
}
.animal-title {
  font-weight: 500;
}
.animal-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--color-text-secondary);
}

// 公益事迹
.welfare-section {
  background: #fff;
  border-radius: 16px;
  padding: 24px 32px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
}
.welfare-content {
  line-height: 1.8;
  font-size: 15px;
  white-space: pre-wrap;
}

@media (max-width: 768px) {
  .profile-header { flex-direction: column; text-align: center; }
  .name-row { justify-content: center; }
  .info-rows { flex-direction: column; }
  .info-label { margin-bottom: 4px; }
  .rescued-item { flex-direction: column; align-items: flex-start; gap: 8px; }
}
</style>
