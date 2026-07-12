<template>
  <div class="dashboard">
    <h2 class="page-title">管理仪表盘</h2>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="24" :sm="12" :lg="6">
        <div class="stat-card" @click="router.push('/admin/pets')">
          <div class="stat-icon pets-icon">
            <el-icon :size="28"><Grid /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ stats.petTotal }}</div>
            <div class="stat-label">宠物总数</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <div class="stat-card" @click="router.push('/admin/adoptions')">
          <div class="stat-icon pending-icon">
            <el-icon :size="28"><Document /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ stats.pendingTotal }}</div>
            <div class="stat-label">待审核申请</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <div class="stat-card" @click="router.push('/admin/pets')">
          <div class="stat-icon adopted-icon">
            <el-icon :size="28"><Check /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ stats.adoptedTotal }}</div>
            <div class="stat-label">已领养宠物</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <div class="stat-card" @click="router.push('/admin/users')">
          <div class="stat-icon users-icon">
            <el-icon :size="28"><User /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ stats.userTotal }}</div>
            <div class="stat-label">注册用户数</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 快捷操作 -->
    <div class="quick-actions">
      <h3>快捷操作</h3>
      <el-button type="primary" size="large" @click="router.push('/admin/pets/add')">
        <el-icon><Plus /></el-icon>
        <span>添加宠物</span>
      </el-button>
      <el-button type="warning" size="large" @click="router.push('/admin/adoptions')">
        <el-icon><DocumentChecked /></el-icon>
        <span>审核申请</span>
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Grid, Document, Check, User, Plus, DocumentChecked } from '@element-plus/icons-vue'
import { getPetList } from '@/api/pet'
import { getAdminApplications } from '@/api/adoption'
import { getUserList } from '@/api/user'

const router = useRouter()

const stats = reactive({
  petTotal: 0,
  pendingTotal: 0,
  adoptedTotal: 0,
  userTotal: 0
})

const loading = ref(true)

async function fetchStats() {
  try {
    const [petRes, pendingRes, adoptedRes, userRes] = await Promise.all([
      getPetList({ page: 1, size: 1 }),
      getAdminApplications(1, 1, 'PENDING'),
      getPetList({ page: 1, size: 1, status: 'ADOPTED' }),
      getUserList(1, 1)
    ])

    stats.petTotal = petRes.data?.total ?? 0
    stats.pendingTotal = pendingRes.data?.total ?? 0
    stats.adoptedTotal = adoptedRes.data?.total ?? 0
    stats.userTotal = userRes.data?.total ?? 0
  } catch (error) {
    console.error('获取统计数据失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchStats()
})
</script>

<style lang="scss" scoped>
.dashboard {
  padding: 20px;
}

.page-title {
  font-size: 22px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 24px 0;
}

.stats-row {
  margin-bottom: 32px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 24px 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  }
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  flex-shrink: 0;
  color: #fff;
}

.pets-icon {
  background: linear-gradient(135deg, #409eff, #66b1ff);
}

.pending-icon {
  background: linear-gradient(135deg, #e6a23c, #ebb563);
}

.adopted-icon {
  background: linear-gradient(135deg, #67c23a, #85ce61);
}

.users-icon {
  background: linear-gradient(135deg, #f56c6c, #f78989);
}

.stat-info {
  flex: 1;
  min-width: 0;
}

.stat-number {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.quick-actions {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);

  h3 {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
    margin: 0 0 16px 0;
  }

  .el-button {
    margin-right: 12px;

    .el-icon {
      margin-right: 6px;
    }
  }
}

@media (max-width: 768px) {
  .dashboard {
    padding: 12px;
  }

  .stats-row {
    .el-col {
      margin-bottom: 12px;
    }
  }

  .stat-card {
    padding: 16px;
  }

  .stat-number {
    font-size: 22px;
  }
}
</style>
