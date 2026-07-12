<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import AppHeader from '@/components/layout/AppHeader.vue'
import { getApplicationDetail, cancelApplication } from '@/api/adoption'

const route = useRoute()
const router = useRouter()

const application = ref(null)
const loading = ref(false)

const statusMap = {
  PENDING: { label: '待管理员审核', type: 'warning' },
  ADMIN_APPROVED: { label: '管理员已通过，待发布者确认', type: '' },
  APPROVED: { label: '发布者已确认', type: 'success' },
  REJECTED: { label: '管理员已拒绝', type: 'danger' },
  OWNER_REJECTED: { label: '发布者已拒绝', type: 'danger' },
  CANCELLED: { label: '已取消', type: 'info' },
  COMPLETED: { label: '已完成', type: 'primary' }
}

const statusLabel = computed(() => {
  if (!application.value) return '-'
  return statusMap[application.value.status]?.label || application.value.status
})

const statusType = computed(() => {
  if (!application.value) return 'info'
  return statusMap[application.value.status]?.type || 'info'
})

const isPending = computed(() => {
  return application.value?.status === 'PENDING' || application.value?.status === 'ADMIN_APPROVED'
})

const timelineItems = computed(() => {
  if (!application.value) return []

  const items = []
  const app = application.value

  // 提交申请
  if (app.createdAt) {
    items.push({
      content: '提交申请',
      timestamp: formatDate(app.createdAt),
      color: '#409eff'
    })
  }

  // 管理员审核
  if (app.status === 'ADMIN_APPROVED' || app.status === 'APPROVED' || app.status === 'COMPLETED' || app.status === 'REJECTED') {
    const approved = app.status !== 'REJECTED'
    items.push({
      content: approved ? '管理员初审通过' : '管理员拒绝',
      timestamp: formatDate(app.reviewedAt),
      color: approved ? '#409eff' : '#f56c6c',
      comment: app.reviewComment || ''
    })
  }

  // 发布者审核
  if (app.status === 'APPROVED' || app.status === 'COMPLETED' || app.status === 'OWNER_REJECTED') {
    const approved = app.status !== 'OWNER_REJECTED'
    items.push({
      content: approved ? '发布者同意领养' : '发布者拒绝',
      timestamp: formatDate(app.ownerReviewedAt),
      color: approved ? '#67c23a' : '#f56c6c',
      comment: app.ownerReviewComment || ''
    })
  }

  // 已完成
  if (app.status === 'COMPLETED') {
    items.push({
      content: '领养完成',
      timestamp: '-',
      color: '#67c23a'
    })
  }

  // 已取消
  if (app.status === 'CANCELLED') {
    if (app.updatedAt) {
      items.push({
        content: '申请已取消',
        timestamp: formatDate(app.updatedAt),
        color: '#909399'
      })
    }
  }

  return items
})

async function fetchDetail() {
  const id = route.params.id
  if (!id) {
    ElMessage.error('缺少申请信息')
    router.replace('/user/applications')
    return
  }

  loading.value = true
  try {
    const res = await getApplicationDetail(id)
    application.value = res.data
  } catch (error) {
    ElMessage.error('获取申请详情失败')
    router.replace('/user/applications')
  } finally {
    loading.value = false
  }
}

async function handleCancel() {
  try {
    await ElMessageBox.confirm(
      '确定要取消该领养申请吗？取消后不可恢复。',
      '取消申请',
      {
        confirmButtonText: '确定取消',
        cancelButtonText: '返回',
        type: 'warning'
      }
    )
  } catch {
    return
  }

  try {
    await cancelApplication(application.value.id)
    ElMessage.success('申请已取消')
    fetchDetail()
  } catch (error) {
    ElMessage.error('取消失败')
  }
}

function goBack() {
  router.push('/user/applications')
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

onMounted(() => {
  fetchDetail()
})
</script>

<template>
  <div class="detail-page">
    <AppHeader />

    <div class="container" v-loading="loading">
      <div class="page-header">
        <h1 class="page-title">申请详情</h1>
      </div>

      <template v-if="application">
        <!-- 状态标签 -->
        <div class="status-bar">
          <span class="status-label">申请状态：</span>
          <el-tag :type="statusType" size="large">
            {{ statusLabel }}
          </el-tag>
          <el-button
            v-if="isPending"
            type="danger"
            size="small"
            style="margin-left: 16px;"
            @click="handleCancel"
          >
            取消申请
          </el-button>
        </div>

        <!-- 基本信息 -->
        <div class="detail-card">
          <h3 class="section-title">宠物信息</h3>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="宠物名称">
              {{ application.petName || application.pet?.name || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="宠物品种">
              {{ application.petBreed || application.pet?.breed || '-' }}
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="detail-card">
          <h3 class="section-title">申请人信息</h3>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="真实姓名">
              {{ application.realName || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="联系电话">
              {{ application.phone || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="身份证号" :span="2">
              {{ application.idCard || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="居住地址" :span="2">
              {{ application.address || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="住房类型">
              {{ application.housingType || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="职业">
              {{ application.occupation || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="养宠经验">
              {{ application.hasExperience || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="家人同意">
              {{ application.familyAgreed || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="收入范围">
              {{ application.incomeRange || '-' }}
            </el-descriptions-item>
          </el-descriptions>

          <div class="reason-box">
            <h4 class="reason-title">领养理由</h4>
            <p class="reason-content">{{ application.reason || '-' }}</p>
          </div>
        </div>

        <!-- 审核意见 -->
        <div
          class="detail-card"
          v-if="(application.reviewComment || application.review_comment) && (application.status === 'APPROVED' || application.status === 'REJECTED' || application.status === 'COMPLETED')"
        >
          <h3 class="section-title">审核意见</h3>
          <div class="review-comment">
            {{ application.reviewComment || application.review_comment }}
          </div>
        </div>

        <!-- 状态时间线 -->
        <div class="detail-card">
          <h3 class="section-title">申请进度</h3>
          <el-timeline>
            <el-timeline-item
              v-for="(item, index) in timelineItems"
              :key="index"
              :timestamp="item.timestamp"
              :color="item.color"
              placement="top"
            >
              <div class="timeline-content">
                <p class="timeline-title">{{ item.content }}</p>
                <p class="timeline-comment" v-if="item.comment">{{ item.comment }}</p>
              </div>
            </el-timeline-item>
          </el-timeline>
          <el-empty
            v-if="timelineItems.length === 0"
            description="暂无进度信息"
            :image-size="80"
          />
        </div>
      </template>

      <el-empty
        v-else-if="!loading"
        description="未找到申请信息"
        :image-size="160"
      />

      <div class="back-bar">
        <el-button @click="goBack">返回申请列表</el-button>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.detail-page {
  min-height: 100vh;
  background: #f5f7fa;
}

.container {
  max-width: 900px;
  margin: 0 auto;
  padding: 0 16px 40px;
}

.page-header {
  text-align: center;
  padding: 32px 0 24px;
}

.page-title {
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.status-bar {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 8px;
  padding: 16px 24px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.status-label {
  font-size: 14px;
  color: #666;
  margin-right: 8px;
}

.detail-card {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
  padding-left: 10px;
  border-left: 3px solid #409eff;
}

.reason-box {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

.reason-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.reason-content {
  font-size: 14px;
  color: #666;
  line-height: 1.8;
  white-space: pre-wrap;
}

.review-comment {
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  font-size: 14px;
  color: #666;
  line-height: 1.8;
  white-space: pre-wrap;
}

.timeline-content {
  .timeline-title {
    font-size: 14px;
    font-weight: 600;
    color: #333;
  }

  .timeline-comment {
    font-size: 13px;
    color: #999;
    margin-top: 4px;
  }
}

.back-bar {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

@media (max-width: 768px) {
  .detail-card {
    padding: 16px;
  }

  .status-bar {
    flex-wrap: wrap;
    gap: 8px;
  }
}
</style>
