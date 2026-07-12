<template>
  <div class="my-rescues-page">
    <AppHeader />
    <div class="container">
      <h1 class="page-title" style="padding:40px 0 24px">我的救助</h1>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="我发布的求助" name="posted">
          <div class="card-grid">
            <div v-for="item in myPosted" :key="item.id" class="rc-card card-hover" @click="$router.push(`/rescue/${item.id}`)">
              <div class="rc-header"><span>{{ item.title }}</span><el-tag size="small">{{ item.statusDesc }}</el-tag></div>
              <p>{{ item.addressDesc }}</p>
              <span class="rc-time">{{ formatTime(item.createdAt) }}</span>
            </div>
            <el-empty v-if="myPosted.length === 0" description="暂无发布的求助" />
          </div>
        </el-tab-pane>
        <el-tab-pane label="我认领的求助" name="claimed">
          <div class="card-grid">
            <div v-for="item in myClaims" :key="item.id" class="rc-card card-hover" @click="$router.push(`/rescue/${item.id}`)">
              <div class="rc-header"><span>{{ item.title }}</span><el-tag size="small">{{ item.statusDesc }}</el-tag></div>
              <p>{{ item.addressDesc }}</p>
              <span class="rc-time">{{ formatTime(item.createdAt) }}</span>
            </div>
            <el-empty v-if="myClaims.length === 0" description="暂无认领的求助" />
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
    <AppFooter />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMyRescues, getMyClaims } from '@/api/rescue'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'

const activeTab = ref('posted')
const myPosted = ref([])
const myClaims = ref([])

function formatTime(t) { return t ? new Date(t).toLocaleDateString() : '' }

onMounted(async () => {
  try { const r1 = await getMyRescues(); myPosted.value = r1.data || [] } catch {}
  try { const r2 = await getMyClaims(); myClaims.value = r2.data || [] } catch {}
})
</script>

<style lang="scss" scoped>
.my-rescues-page { min-height: 100vh; background: var(--color-bg); }
.card-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; @media (max-width: 768px) { grid-template-columns: 1fr; } }
.rc-card { background: #fff; padding: 16px; border-radius: var(--border-radius); cursor: pointer;
  .rc-header { display: flex; justify-content: space-between; margin-bottom: 8px; }
  p { color: var(--color-text-secondary); font-size: 14px; }
  .rc-time { font-size: 12px; color: #999; }
}
</style>
