<template>
  <div class="shelter-dashboard">
    <h2>工作台概览</h2>
    <p class="subtitle">欢迎回来，{{ dashboard?.nickname || dashboard?.shelterName || '机构用户' }}</p>

    <el-row :gutter="16" v-loading="loading">
      <el-col :xs="24" :sm="12" :lg="6">
        <div class="stat-card" @click="$router.push('/shelter/pets')">
          <div class="stat-icon" style="background:#e6f7ff"><span>🐾</span></div>
          <div class="stat-body">
            <div class="stat-num">{{ dashboard?.petTotal || 0 }}</div>
            <div class="stat-label">累计发布宠物</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <div class="stat-card" style="cursor:default">
          <div class="stat-icon" style="background:#f6ffed"><span>✅</span></div>
          <div class="stat-body">
            <div class="stat-num">{{ dashboard?.availableCount || 0 }}</div>
            <div class="stat-label">在架宠物</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <div class="stat-card" @click="$router.push('/shelter/adoptions')">
          <div class="stat-icon" style="background:#fff7e6"><span>📋</span></div>
          <div class="stat-body">
            <div class="stat-num">{{ dashboard?.pendingReview || 0 }}</div>
            <div class="stat-label">待处理申请</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <div class="stat-card" style="cursor:default">
          <div class="stat-icon" style="background:#fff2f0"><span>🏡</span></div>
          <div class="stat-body">
            <div class="stat-num">{{ dashboard?.adoptedCount || 0 }}</div>
            <div class="stat-label">已成功领养</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top:24px">
      <el-col :xs="24" :md="12">
        <div class="info-card">
          <h3>认证状态</h3>
          <div class="cert-status">
            <el-tag v-if="dashboard?.certStatus === 'APPROVED'" type="success" size="large" effect="dark">已认证</el-tag>
            <el-tag v-else-if="dashboard?.certStatus === 'PENDING'" type="warning" size="large">审核中</el-tag>
            <el-tag v-else type="info" size="large">未认证</el-tag>
            <span v-if="dashboard?.certStatus !== 'APPROVED'" style="margin-left:12px;font-size:13px;color:#909399">
              认证后可展示机构主页，增加信任度
            </span>
          </div>
          <el-button v-if="dashboard?.certStatus !== 'APPROVED'" type="primary" size="small" style="margin-top:12px" @click="$router.push('/shelter/cert')">
            去认证
          </el-button>
        </div>
      </el-col>
      <el-col :xs="24" :md="12">
        <div class="info-card">
          <h3>快捷操作</h3>
          <div class="quick-actions">
            <el-button type="primary" @click="$router.push('/pets/publish')">发布领养信息</el-button>
            <el-button @click="$router.push('/shelter/adoptions')">查看待处理申请</el-button>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getShelterDashboard } from '@/api/shelter'

const dashboard = ref(null)
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    const res = await getShelterDashboard()
    dashboard.value = res.data
  } catch {} finally { loading.value = false }
})
</script>

<style lang="scss" scoped>
h2 { margin: 0 0 4px; font-size: 20px; }
.subtitle { color: #909399; margin-bottom: 20px; }

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  margin-bottom: 16px;
  transition: box-shadow 0.2s;
  &:hover { box-shadow: 0 4px 16px rgba(0,0,0,0.08); }
}

.stat-icon {
  width: 52px; height: 52px; border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  span { font-size: 24px; }
}

.stat-num { font-size: 28px; font-weight: bold; color: #303133; }
.stat-label { font-size: 13px; color: #909399; margin-top: 2px; }

.info-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 16px;
  h3 { font-size: 16px; margin-bottom: 12px; }
}

.cert-status { display: flex; align-items: center; }
.quick-actions { display: flex; gap: 12px; flex-wrap: wrap; }
</style>
