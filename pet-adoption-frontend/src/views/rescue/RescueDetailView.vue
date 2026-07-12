<template>
  <div class="rescue-detail-page">
    <AppHeader />
    <div class="container" v-if="post">
      <el-breadcrumb separator="/"><el-breadcrumb-item :to="{path:'/'}">首页</el-breadcrumb-item><el-breadcrumb-item :to="{path:'/rescue'}">流浪救助</el-breadcrumb-item><el-breadcrumb-item>{{ post.title }}</el-breadcrumb-item></el-breadcrumb>
      <div class="detail-header">
        <h1>{{ getAnimalIcon(post.animalType) }} {{ post.title }}</h1>
        <div class="dh-tags">
          <el-tag :type="emergencyTagType(post.emergencyLevel)">{{ emergencyText(post.emergencyLevel) }}</el-tag>
          <el-tag :type="statusTagType(post.status)">{{ post.statusDesc }}</el-tag>
        </div>
      </div>
      <div class="detail-body">
        <div class="db-main">
          <div class="info-card">
            <h3>求助详情</h3>
            <p>{{ post.description }}</p>
            <div class="info-grid">
              <div>📍 {{ post.addressDesc }}</div>
              <div>🕐 {{ formatTime(post.createdAt) }}</div>
              <div v-if="post.claimedByName">👤 认领人: {{ post.claimedByName }}</div>
            </div>
          </div>
          <div class="status-timeline">
            <h3>救助进度</h3>
            <el-steps :active="statusStep" align-center>
              <el-step title="待救助" description="等待志愿者" />
              <el-step title="处理中" description="已被认领" />
              <el-step title="已救助" description="救助完成" />
              <el-step title="已关闭" description="后续跟进" />
            </el-steps>
          </div>
          <div v-if="post.followUpText" class="follow-up">
            <h3>📝 救助后续</h3>
            <p>{{ post.followUpText }}</p>
          </div>
        </div>
        <div class="db-sidebar">
          <div class="action-card">
            <div class="poster-info">
              <el-avatar :size="40">{{ post.username?.charAt(0) }}</el-avatar>
              <span>{{ post.username }}</span>
            </div>
            <el-button v-if="post.status === 'PENDING' && authStore.isLoggedIn && post.userId !== authStore.user?.id"
                       type="primary" size="large" style="width:100%" @click="handleClaim">🤝 我来救助</el-button>
            <el-button v-if="canUpdateStatus" type="success" size="large" style="width:100%;margin-top:8px"
                       @click="handleRescued">✅ 标记已救助</el-button>
          </div>
        </div>
      </div>
    </div>
    <AppFooter />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRescueDetail, claimRescue, updateRescueStatus } from '@/api/rescue'
import { useAuthStore } from '@/stores/auth'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'

const route = useRoute()
const authStore = useAuthStore()
const post = ref(null)

const statusStep = computed(() => post.value?.status === 'PENDING' ? 0 : post.value?.status === 'PROCESSING' ? 1 : post.value?.status === 'RESCUED' ? 2 : 3)
const canUpdateStatus = computed(() => {
  if (!authStore.isLoggedIn || !post.value) return false
  return (post.value.status === 'PROCESSING' &&
    (post.value.claimedBy === authStore.user?.id || post.value.userId === authStore.user?.id))
})

function getAnimalIcon(t) { return t === 'CAT' ? '🐱' : t === 'DOG' ? '🐶' : '🐾' }
function emergencyText(l) { return l === 'CRITICAL' ? '危急' : l === 'URGENT' ? '紧急' : '普通' }
function emergencyTagType(l) { return l === 'CRITICAL' ? 'danger' : l === 'URGENT' ? 'warning' : 'info' }
function statusTagType(s) { return s === 'PENDING' ? 'warning' : s === 'PROCESSING' ? 'primary' : s === 'RESCUED' ? 'success' : 'info' }
function formatTime(t) { return t ? new Date(t).toLocaleString() : '' }

async function handleClaim() {
  if (!authStore.isLoggedIn) return ElMessage.warning('请先登录')
  try {
    await ElMessageBox.confirm('确认认领此救助任务？', '确认认领', { confirmButtonText: '确认', cancelButtonText: '取消', type: 'info' })
    await claimRescue(post.value.id)
    ElMessage.success('认领成功')
    fetchDetail()
  } catch { /* cancelled */ }
}

async function handleRescued() {
  try {
    await ElMessageBox.confirm('确认救助已完成？', '确认完成', { confirmButtonText: '确认' })
    await updateRescueStatus(post.value.id, 'RESCUED')
    ElMessage.success('状态已更新')
    fetchDetail()
  } catch { /* cancelled */ }
}

async function fetchDetail() {
  const res = await getRescueDetail(route.params.id)
  post.value = res.data
}

onMounted(fetchDetail)
</script>

<style lang="scss" scoped>
.rescue-detail-page { min-height: 100vh; background: var(--color-bg); }
.detail-header { padding: 24px 0; h1 { font-size: 24px; } .dh-tags { margin-top: 8px; display: flex; gap: 8px; } }
.detail-body { display: flex; gap: 24px; }
.db-main { flex: 1; }
.db-sidebar { width: 280px; }
.info-card, .status-timeline, .follow-up { background: #fff; padding: 24px; border-radius: var(--border-radius); margin-bottom: 16px; }
.info-grid { margin-top: 12px; color: var(--color-text-secondary); }
.action-card { background: #fff; padding: 24px; border-radius: var(--border-radius); position: sticky; top: 80px; }
.poster-info { display: flex; align-items: center; gap: 8px; margin-bottom: 16px; }
@media (max-width: 768px) { .detail-body { flex-direction: column; } .db-sidebar { width: 100%; } }
</style>
