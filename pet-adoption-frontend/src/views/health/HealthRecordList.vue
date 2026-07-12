<template>
  <div class="health-record-page">
    <AppHeader />
    <div class="container">
      <div class="page-header"><h1 class="page-title">💊 健康记录</h1></div>
      <el-button type="primary" @click="showForm = true">+ 添加记录</el-button>
      <div class="records-timeline" style="margin-top:24px">
        <div v-for="r in records" :key="r.id" class="record-card">
          <div class="rc-type">{{ getRecordEmoji(r.recordType) }} {{ r.recordTypeName }}</div>
          <h4>{{ r.title }}</h4><p>{{ r.description }}</p>
          <div class="rc-meta"><span>📅 {{ r.recordDate }}</span><span v-if="r.nextDate">⏰ {{ r.nextDate }}</span></div>
        </div>
        <el-empty v-if="records.length === 0" description="暂无记录" />
      </div>
    </div>
    <AppFooter />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getHealthRecords } from '@/api/health'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'

const route = useRoute()
const records = ref([])
const showForm = ref(false)

function getRecordEmoji(t) { return t === 'VACCINE' ? '💉' : t === 'DEWORM' ? '💊' : t === 'MEDICAL' ? '🏥' : '📝' }

onMounted(async () => {
  try { const res = await getHealthRecords(route.params.petId); records.value = res.data || [] } catch {}
})
</script>

<style lang="scss" scoped>
.health-record-page { min-height: 100vh; background: var(--color-bg); }
.records-timeline { max-width: 600px; }
.record-card { background: #fff; padding: 16px; border-radius: var(--border-radius); margin-bottom: 12px; border-left: 4px solid var(--color-primary);
  .rc-type { font-weight: 600; color: var(--color-primary); margin-bottom: 4px; }
  h4 { margin-bottom: 4px; } p { color: var(--color-text-secondary); font-size: 14px; }
  .rc-meta { margin-top: 8px; font-size: 13px; color: var(--color-text-secondary); display: flex; gap: 16px; }
}
</style>
