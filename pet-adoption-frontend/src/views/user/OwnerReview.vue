<template>
  <div class="owner-review-page">
    <AppHeader />
    <div class="container">
      <div class="page-header">
        <h1 class="page-title">收到的领养申请</h1>
        <p class="page-subtitle">管理员初审通过后，由您最终决定是否同意领养</p>
      </div>

      <!-- 筛选 -->
      <div class="filter-tabs">
        <el-radio-group v-model="filterStatus" @change="handleFilterChange" size="default">
          <el-radio-button label="">全部</el-radio-button>
          <el-radio-button label="ADMIN_APPROVED">待我处理</el-radio-button>
          <el-radio-button label="APPROVED">已同意</el-radio-button>
          <el-radio-button label="OWNER_REJECTED">已拒绝</el-radio-button>
        </el-radio-group>
      </div>

      <!-- 申请列表 -->
      <div v-loading="loading">
        <el-empty v-if="!loading && applications.length === 0" description="暂无领养申请" />

        <div v-for="app in applications" :key="app.id" class="application-card">
          <div class="card-header">
            <div class="pet-info">
              <el-image
                v-if="app.petCoverImage"
                :src="app.petCoverImage"
                fit="cover"
                style="width:80px;height:60px;border-radius:6px"
                :preview-src-list="[app.petCoverImage]"
                preview-teleported
              />
              <div class="pet-text">
                <h4>{{ app.petName || '-' }}</h4>
                <p>{{ app.username || '-' }} 的申请</p>
              </div>
            </div>
            <el-tag :type="statusTagType(app.status)" size="small">
              {{ statusLabel(app.status) }}
            </el-tag>
          </div>

          <div class="card-body">
            <el-descriptions :column="3" size="small" border>
              <el-descriptions-item label="姓名">{{ app.realName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="电话">{{ app.phone || '-' }}</el-descriptions-item>
              <el-descriptions-item label="职业">{{ app.occupation || '-' }}</el-descriptions-item>
              <el-descriptions-item label="住房">{{ app.housingType || '-' }}</el-descriptions-item>
              <el-descriptions-item label="养宠经验">{{ app.hasExperience ? '有' : '无' }}</el-descriptions-item>
              <el-descriptions-item label="家人同意">{{ app.familyAgreed ? '是' : '否' }}</el-descriptions-item>
              <el-descriptions-item label="收入范围">{{ app.incomeRange || '-' }}</el-descriptions-item>
              <el-descriptions-item label="地址" :span="2">{{ app.address || '-' }}</el-descriptions-item>
            </el-descriptions>
            <div class="reason-box" v-if="app.reason">
              <strong>领养理由：</strong>{{ app.reason }}
            </div>
            <div class="review-box" v-if="app.reviewComment">
              <strong>管理员意见：</strong>{{ app.reviewComment }}
            </div>
          </div>

          <div class="card-actions" v-if="app.status === 'ADMIN_APPROVED'">
            <el-button type="success" @click="openReview(app, 'APPROVED')">
              <el-icon><Select /></el-icon>
              <span>同意领养</span>
            </el-button>
            <el-button type="danger" @click="openReview(app, 'OWNER_REJECTED')">
              <el-icon><CloseBold /></el-icon>
              <span>拒绝</span>
            </el-button>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div v-if="total > pagination.size" class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          :page-size="pagination.size"
          :total="total"
          layout="prev, pager, next"
          background
          @current-change="fetchApplications"
        />
      </div>
    </div>

    <!-- 审核对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="确认审核决定"
      width="480px"
      :close-on-click-modal="false"
    >
      <el-form :model="reviewForm" label-width="80px">
        <el-form-item label="审核决定">
          <el-tag :type="reviewForm.status === 'APPROVED' ? 'success' : 'danger'" size="large">
            {{ reviewForm.status === 'APPROVED' ? '同意领养' : '拒绝申请' }}
          </el-tag>
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="reviewForm.reviewComment"
            type="textarea"
            :rows="3"
            placeholder="给申请人的留言（选填）"
            maxlength="300"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          确认
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Select, CloseBold } from '@element-plus/icons-vue'
import AppHeader from '@/components/layout/AppHeader.vue'
import { getOwnerApplications, ownerReviewApplication } from '@/api/adoption'

const loading = ref(false)
const applications = ref([])
const filterStatus = ref('')
const dialogVisible = ref(false)
const submitting = ref(false)
const currentApp = ref(null)

const pagination = reactive({ page: 1, size: 10 })
const total = ref(0)

const reviewForm = reactive({ status: '', reviewComment: '' })

function statusLabel(s) {
  const map = {
    PENDING: '待管理员审核',
    ADMIN_APPROVED: '待您确认',
    APPROVED: '已同意',
    REJECTED: '管理员已拒绝',
    OWNER_REJECTED: '您已拒绝',
    COMPLETED: '已完成',
    CANCELLED: '已取消'
  }
  return map[s] || s
}

function statusTagType(s) {
  const map = { PENDING: 'warning', ADMIN_APPROVED: '', APPROVED: 'success', REJECTED: 'danger', OWNER_REJECTED: 'danger', COMPLETED: 'primary', CANCELLED: 'info' }
  return map[s] || 'info'
}

async function fetchApplications() {
  loading.value = true
  try {
    const res = await getOwnerApplications(pagination.page, pagination.size, filterStatus.value || undefined)
    applications.value = res.data?.records ?? []
    total.value = res.data?.total ?? 0
  } catch (e) {
    console.error('获取申请列表失败:', e)
  } finally {
    loading.value = false
  }
}

function handleFilterChange() {
  pagination.page = 1
  fetchApplications()
}

function openReview(app, status) {
  currentApp.value = app
  reviewForm.status = status
  reviewForm.reviewComment = ''
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!currentApp.value) return
  submitting.value = true
  try {
    await ownerReviewApplication(currentApp.value.id, {
      status: reviewForm.status,
      reviewComment: reviewForm.reviewComment
    })
    ElMessage.success(reviewForm.status === 'APPROVED' ? '已同意领养申请' : '已拒绝领养申请')
    dialogVisible.value = false
    fetchApplications()
  } catch (e) {
    console.error('审核失败:', e)
  } finally {
    submitting.value = false
  }
}

onMounted(() => fetchApplications())
</script>

<style lang="scss" scoped>
.owner-review-page {
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

.page-subtitle {
  font-size: 14px;
  color: #999;
  margin-top: 8px;
}

.filter-tabs {
  margin-bottom: 20px;
  text-align: center;
}

.application-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px 24px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;

  .pet-info {
    display: flex;
    align-items: center;
    gap: 12px;

    h4 { margin: 0; font-size: 16px; color: #333; }
    p { margin: 4px 0 0; font-size: 13px; color: #999; }
  }
}

.card-body {
  margin-bottom: 12px;

  .reason-box, .review-box {
    margin-top: 12px;
    padding: 10px 14px;
    background: #f5f7fa;
    border-radius: 6px;
    font-size: 13px;
    color: #666;
    line-height: 1.7;
    strong { color: #333; }
  }
}

.card-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

@media (max-width: 768px) {
  .application-card {
    padding: 16px;
  }
}
</style>
