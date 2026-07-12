<template>
  <div class="welfare-page">
    <AppHeader />
    <div class="container">
      <div class="page-header"><h1 class="page-title">💚 公益板块</h1><p class="page-subtitle">一起行动，让流浪动物不再流浪</p></div>
      <el-row :gutter="16">
        <el-col :xs="24" :md="8">
          <div class="w-card card-hover" @click="$router.push('/rescue')">
            <span class="wc-icon">🏥</span><h3>救助动态</h3><p>查看附近流浪动物求助信息，伸出援手</p>
          </div>
        </el-col>
        <el-col :xs="24" :md="8">
          <div class="w-card card-hover" @click="handleVolunteerCard">
            <span class="wc-icon">🤝</span><h3>{{ isVolunteer ? '您已是认证志愿者' : '成为志愿者' }}</h3>
            <p>{{ isVolunteer ? '查看您的志愿者主页，展示公益贡献' : '加入救助网络，帮助更多流浪动物' }}</p>
          </div>
        </el-col>
        <el-col :xs="24" :md="8">
          <div class="w-card card-hover">
            <span class="wc-icon">📖</span><h3>TNR科普</h3><p>了解TNR(诱捕-绝育-放归)，科学控制流浪动物数量</p>
          </div>
        </el-col>
      </el-row>

      <!-- 志愿者统计数据 -->
      <section class="section" v-if="stats">
        <h2 class="section-title">📊 公益数据</h2>
        <el-row :gutter="16">
          <el-col :xs="12" :md="8">
            <div class="stat-card">
              <div class="stat-num">{{ stats.volunteerCount || 0 }}</div>
              <div class="stat-desc">认证志愿者</div>
            </div>
          </el-col>
          <el-col :xs="12" :md="8">
            <div class="stat-card">
              <div class="stat-num">{{ stats.rescuedCount || 0 }}</div>
              <div class="stat-desc">累计救助动物</div>
            </div>
          </el-col>
          <el-col :xs="24" :md="8">
            <div class="stat-card">
              <div class="stat-num">{{ stats.message || '' }}</div>
              <div class="stat-desc" style="font-size:14px">{{ stats.message || '每一份爱心都值得被看见' }}</div>
            </div>
          </el-col>
        </el-row>
      </section>

      <!-- 志愿者排行榜 -->
      <section class="section" v-if="volunteerList.length">
        <h2 class="section-title">🌟 志愿者风采</h2>
        <div class="volunteer-list">
          <div
            class="volunteer-item"
            v-for="(v, idx) in volunteerList"
            :key="v.id"
            @click="$router.push(`/welfare/volunteer/${v.id}`)"
          >
            <div class="v-rank">
              <span v-if="idx === 0">🥇</span>
              <span v-else-if="idx === 1">🥈</span>
              <span v-else-if="idx === 2">🥉</span>
              <span v-else class="rank-num">{{ idx + 1 }}</span>
            </div>
            <el-avatar :size="44" :src="v.avatar" class="v-avatar">
              {{ (v.userName || '?')[0] }}
            </el-avatar>
            <div class="v-info">
              <div class="v-name">{{ v.userName }}</div>
              <div class="v-tags">
                <el-tag size="small" type="info" v-for="(s, i) in skillTags(v.skills)" :key="i">{{ s }}</el-tag>
                <span class="v-region" v-if="v.region">📍{{ v.region }}</span>
              </div>
            </div>
            <div class="v-rescue">
              <span class="v-count">{{ v.rescueCount || 0 }}</span>
              <span class="v-unit">救助</span>
            </div>
          </div>
        </div>
        <div v-if="!volunteerList.length" class="no-volunteers">
          <el-empty description="暂无志愿者，快来成为第一位志愿者吧！" :image-size="80">
            <el-button type="primary" @click="$router.push('/welfare/volunteer/apply')">成为志愿者</el-button>
          </el-empty>
        </div>
      </section>

      <!-- TNR科普 -->
      <section class="section">
        <h2 class="section-title">关于 TNR</h2>
        <div class="tnr-content">
          <p><strong>TNR (Trap-Neuter-Return)</strong> 是一种人道且有效的流浪动物管理方法。</p>
          <el-steps :active="3" align-center style="margin:24px 0">
            <el-step title="Trap 诱捕" description="使用人道捕猫笼安全捕获" />
            <el-step title="Neuter 绝育" description="送往合作医院进行绝育手术" />
            <el-step title="Return 放归" description="恢复后放归原栖息地" />
          </el-steps>
          <p>TNR 可以有效控制流浪动物数量、减少扰民问题、提高动物福利，是国际公认的最佳实践。</p>
        </div>
      </section>
    </div>
    <AppFooter />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getVolunteers, getWelfareStats, getMyVolunteer } from '@/api/trust'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'

const router = useRouter()
const stats = ref(null)
const volunteerList = ref([])
const isVolunteer = ref(false)

function skillTags(skills) {
  if (!skills) return []
  return skills.split(/[,，、]/).map(s => s.trim()).filter(Boolean).slice(0, 3)
}

async function handleVolunteerCard() {
  if (isVolunteer.value) {
    try {
      const res = await getMyVolunteer()
      if (res.data?.id) {
        router.push(`/welfare/volunteer/${res.data.id}`)
        return
      }
    } catch {}
  }
  router.push('/welfare/volunteer/apply')
}

onMounted(async () => {
  try {
    const [volRes, statsRes] = await Promise.all([
      getVolunteers().catch(() => ({ data: [] })),
      getWelfareStats().catch(() => ({ data: null }))
    ])
    volunteerList.value = volRes.data || []
    stats.value = statsRes.data
  } catch {}

  // 检查当前用户是否已是志愿者
  const token = localStorage.getItem('token')
  if (token) {
    try {
      const my = await getMyVolunteer()
      isVolunteer.value = my.data?.status === 'ACTIVE'
    } catch {}
  }
})
</script>

<style lang="scss" scoped>
.welfare-page { min-height: 100vh; background: var(--color-bg); }
.container { max-width: 960px; margin: 0 auto; padding: 0 16px 40px; }
.page-header { text-align: center; padding: 40px 0 24px; }
.page-title { font-size: 28px; margin-bottom: 8px; }
.page-subtitle { color: var(--color-text-secondary); font-size: 16px; }

.w-card { background: #fff; padding: 32px; border-radius: var(--border-radius); text-align: center; cursor: pointer; margin-bottom: 16px;
  .wc-icon { font-size: 48px; display: block; margin-bottom: 12px; }
  h3 { margin-bottom: 8px; } p { color: var(--color-text-secondary); font-size: 14px; }
}

.section { margin-top: 40px; }
.section-title { font-size: 20px; margin-bottom: 16px; }

// 统计卡片
.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  text-align: center;
  margin-bottom: 16px;
}
.stat-num { font-size: 32px; font-weight: bold; color: var(--color-primary); margin-bottom: 8px; }
.stat-desc { font-size: 15px; color: var(--color-text-secondary); }

// 志愿者列表
.volunteer-list {
  background: #fff;
  border-radius: var(--border-radius);
  overflow: hidden;
}
.volunteer-item {
  display: flex;
  align-items: center;
  padding: 16px 20px;
  gap: 16px;
  cursor: pointer;
  transition: background 0.2s;
  border-bottom: 1px solid #f0f0f0;
  &:last-child { border-bottom: none; }
  &:hover { background: var(--el-color-primary-light-9); }
}
.v-rank {
  width: 32px;
  text-align: center;
  font-size: 22px;
}
.rank-num {
  font-size: 14px;
  color: var(--color-text-secondary);
  font-weight: bold;
}
.v-avatar { flex-shrink: 0; background: var(--color-primary); color: #fff; }
.v-info { flex: 1; min-width: 0; }
.v-name { font-weight: 600; margin-bottom: 4px; }
.v-tags { display: flex; align-items: center; gap: 6px; flex-wrap: wrap; }
.v-region { font-size: 12px; color: var(--color-text-secondary); }
.v-rescue { text-align: center; flex-shrink: 0; }
.v-count { font-size: 22px; font-weight: bold; color: var(--color-primary); }
.v-unit { font-size: 12px; color: var(--color-text-secondary); display: block; }

.tnr-content { background: #fff; padding: 24px; border-radius: var(--border-radius); line-height: 1.8; }

@media (max-width: 768px) {
  .volunteer-item { padding: 12px; gap: 10px; }
  .v-tags { display: none; }
}
</style>
