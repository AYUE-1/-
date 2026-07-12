<template>
  <div class="rescue-page">
    <AppHeader />
    <div class="container">
      <div class="page-header">
        <h1 class="page-title">🐾 流浪动物救助</h1>
        <p class="page-subtitle">每一份善意都值得被看见</p>
      </div>
      <div class="action-bar">
        <el-input v-model="keyword" placeholder="搜索救助信息..." prefix-icon="Search" style="width:300px" clearable @clear="fetchList" @keyup.enter="fetchList" />
        <el-select v-model="filters.animalType" placeholder="动物类型" clearable @change="fetchList" style="width:130px">
          <el-option label="猫咪" value="CAT" /><el-option label="狗狗" value="DOG" /><el-option label="其他" value="OTHER" />
        </el-select>
        <el-select v-model="filters.emergencyLevel" placeholder="紧急程度" clearable @change="fetchList" style="width:130px">
          <el-option label="普通" value="NORMAL" /><el-option label="紧急" value="URGENT" /><el-option label="危急" value="CRITICAL" />
        </el-select>
        <el-button type="primary" @click="$router.push('/rescue/publish')" v-if="authStore.isLoggedIn">发布求助</el-button>
        <el-button @click="$router.push('/rescue/map')">地图模式</el-button>
      </div>

      <div v-if="loading" style="padding:40px"><el-skeleton :rows="3" animated /></div>
      <div v-else-if="list.length === 0"><el-empty description="暂无救助信息" /></div>
      <div v-else class="rescue-grid">
        <div v-for="item in list" :key="item.id" class="rescue-card card-hover" @click="$router.push(`/rescue/${item.id}`)">
          <div class="rc-header">
            <span class="rc-type">{{ getAnimalIcon(item.animalType) }} {{ item.title }}</span>
            <el-tag :type="emergencyTagType(item.emergencyLevel)" size="small">{{ emergencyText(item.emergencyLevel) }}</el-tag>
          </div>
          <p class="rc-desc text-ellipsis-2">{{ item.description }}</p>
          <div class="rc-meta">
            <span>📍 {{ item.addressDesc || '未知位置' }}</span>
            <span v-if="item.distance !== undefined && item.distance < 9999">📏 {{ item.distance }}km</span>
          </div>
          <div class="rc-footer">
            <el-tag :type="statusTagType(item.status)" size="small">{{ item.statusDesc }}</el-tag>
            <span class="rc-time">{{ formatTime(item.createdAt) }}</span>
          </div>
        </div>
      </div>
      <div class="pagination-wrap" v-if="total > 0">
        <el-pagination background layout="prev, pager, next" :total="total" :page-size="12" v-model:current-page="page" @current-change="fetchList" />
      </div>
    </div>
    <AppFooter />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getRescueList } from '@/api/rescue'
import { useAuthStore } from '@/stores/auth'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'

const authStore = useAuthStore()
const list = ref([])
const loading = ref(true)
const keyword = ref('')
const page = ref(1)
const total = ref(0)
const filters = reactive({ animalType: '', emergencyLevel: '' })

function getAnimalIcon(t) { return t === 'CAT' ? '🐱' : t === 'DOG' ? '🐶' : '🐾' }
function emergencyText(l) { return l === 'CRITICAL' ? '危急' : l === 'URGENT' ? '紧急' : '普通' }
function emergencyTagType(l) { return l === 'CRITICAL' ? 'danger' : l === 'URGENT' ? 'warning' : 'info' }
function statusTagType(s) { return s === 'PENDING' ? 'warning' : s === 'PROCESSING' ? 'primary' : s === 'RESCUED' ? 'success' : 'info' }
function formatTime(t) {
  if (!t) return ''
  const d = new Date(t), now = new Date(), diff = now - d
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  return d.toLocaleDateString()
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getRescueList({ page: page.value, size: 12, animalType: filters.animalType, emergencyLevel: filters.emergencyLevel })
    list.value = res.data || []
    total.value = res.data?.length > 0 ? (page.value * 12 + 1) : 0
  } finally { loading.value = false }
}

onMounted(fetchList)
</script>

<style lang="scss" scoped>
.rescue-page { min-height: 100vh; background: var(--color-bg); }
.action-bar { display: flex; gap: 12px; margin-bottom: 24px; flex-wrap: wrap; }
.rescue-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; @media (max-width: 992px) { grid-template-columns: repeat(2, 1fr); } @media (max-width: 576px) { grid-template-columns: 1fr; } }
.rescue-card {
  background: #fff; padding: 20px; border-radius: var(--border-radius); cursor: pointer;
  .rc-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; .rc-type { font-weight: 600; font-size: 16px; } }
  .rc-desc { color: var(--color-text-secondary); font-size: 14px; margin-bottom: 10px; }
  .rc-meta { font-size: 13px; color: var(--color-text-secondary); display: flex; gap: 12px; margin-bottom: 10px; }
  .rc-footer { display: flex; justify-content: space-between; align-items: center; .rc-time { font-size: 12px; color: #999; } }
}
.pagination-wrap { display: flex; justify-content: center; padding: 32px 0; }
</style>
