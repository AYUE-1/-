<template>
  <div class="application-review">
    <h2 class="page-title">领养申请审核</h2>

    <!-- 筛选标签 -->
    <div class="filter-tabs">
      <el-radio-group v-model="filterStatus" @change="handleFilterChange" size="large">
        <el-radio-button label="">全部</el-radio-button>
        <el-radio-button label="PENDING">待审核</el-radio-button>
        <el-radio-button label="ADMIN_APPROVED">管理员已通过</el-radio-button>
        <el-radio-button label="APPROVED">发布者已确认</el-radio-button>
        <el-radio-button label="REJECTED">已拒绝</el-radio-button>
        <el-radio-button label="COMPLETED">已完成</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 申请表 -->
    <el-table
      v-loading="loading"
      :data="applicationList"
      border
      stripe
      style="width: 100%"
      class="application-table"
      @expand-change="handleExpand"
    >
      <el-table-column type="expand">
        <template #default="{ row }">
          <div class="expand-content">
            <el-descriptions :column="2" border size="small">
              <el-descriptions-item label="真实姓名">{{ row.realName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="联系电话">{{ row.phone || '-' }}</el-descriptions-item>
              <el-descriptions-item label="联系地址">{{ row.address || '-' }}</el-descriptions-item>
              <el-descriptions-item label="住房类型">{{ row.housingType || '-' }}</el-descriptions-item>
              <el-descriptions-item label="养宠经验">{{ row.hasExperience ? '有' : '无' }}</el-descriptions-item>
              <el-descriptions-item label="申请时间">{{ formatDate(row.createTime) }}</el-descriptions-item>
              <el-descriptions-item label="审核时间">{{ formatDate(row.reviewTime) }}</el-descriptions-item>
              <el-descriptions-item label="审核意见">{{ row.reviewComment || '-' }}</el-descriptions-item>
              <el-descriptions-item v-if="row.reason" label="申请理由" :span="2">
                {{ row.reason }}
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="申请人" width="100" prop="applicantName">
        <template #default="{ row }">
          {{ row.realName || row.applicantName || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="宠物名称" width="130" prop="petName" show-overflow-tooltip />
      <el-table-column label="联系电话" width="130" prop="phone" />
      <el-table-column label="申请时间" width="170" align="center">
        <template #default="{ row }">
          {{ formatDate(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)" size="small">
            {{ statusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100" align="center" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="row.status === 'PENDING'"
            type="primary"
            size="small"
            link
            @click="openReviewDialog(row)"
          >
            <el-icon><Edit /></el-icon>
            <span>审核</span>
          </el-button>
          <span v-else class="no-action">-</span>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        background
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>

    <!-- 审核对话框 -->
    <el-dialog
      v-model="reviewDialogVisible"
      title="审核领养申请"
      width="560px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <div v-if="currentApplication" class="review-info">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="申请人">{{ currentApplication.realName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="宠物名称">{{ currentApplication.petName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentApplication.phone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="联系地址">{{ currentApplication.address || '-' }}</el-descriptions-item>
          <el-descriptions-item label="住房类型">{{ currentApplication.housingType || '-' }}</el-descriptions-item>
          <el-descriptions-item label="养宠经验">{{ currentApplication.hasExperience ? '有' : '无' }}</el-descriptions-item>
          <el-descriptions-item v-if="currentApplication.reason" label="申请理由" :span="2">
            {{ currentApplication.reason }}
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <el-divider />

      <el-form :model="reviewForm" label-width="80px">
        <el-form-item label="审核结果" required>
          <el-radio-group v-model="reviewForm.status">
            <el-radio value="ADMIN_APPROVED">通过（转发布者确认）</el-radio>
            <el-radio value="REJECTED">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核意见">
          <el-input
            v-model="reviewForm.reviewComment"
            type="textarea"
            :rows="3"
            placeholder="请输入审核意见（选填）"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="reviewDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="reviewSubmitting" @click="handleReviewSubmit">
          确认提交
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Edit } from '@element-plus/icons-vue'
import { getAdminApplications, reviewApplication } from '@/api/adoption'

const loading = ref(false)
const applicationList = ref([])
const filterStatus = ref('')

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const reviewDialogVisible = ref(false)
const reviewSubmitting = ref(false)
const currentApplication = ref(null)

const reviewForm = reactive({
  status: 'APPROVED',
  reviewComment: ''
})

function statusLabel(status) {
  const map = {
    PENDING: '待管理员审核',
    ADMIN_APPROVED: '管理员已通过',
    APPROVED: '发布者已确认',
    REJECTED: '已拒绝',
    OWNER_REJECTED: '发布者已拒绝',
    COMPLETED: '已完成',
    CANCELLED: '已取消'
  }
  return map[status] || status
}

function statusTagType(status) {
  const map = {
    PENDING: 'warning',
    ADMIN_APPROVED: '',
    APPROVED: 'success',
    REJECTED: 'danger',
    OWNER_REJECTED: 'danger',
    COMPLETED: 'primary',
    CANCELLED: 'info'
  }
  return map[status] || 'info'
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

async function fetchApplications() {
  loading.value = true
  try {
    const res = await getAdminApplications(
      pagination.page,
      pagination.size,
      filterStatus.value || undefined
    )
    applicationList.value = res.data?.records ?? []
    pagination.total = res.data?.total ?? 0
  } catch (error) {
    console.error('获取申请列表失败:', error)
  } finally {
    loading.value = false
  }
}

function handleFilterChange() {
  pagination.page = 1
  fetchApplications()
}

function handlePageChange(page) {
  pagination.page = page
  fetchApplications()
}

function handleSizeChange(size) {
  pagination.size = size
  pagination.page = 1
  fetchApplications()
}

function handleExpand(row, expandedRows) {
  // 点击行展开更多详情
}

function openReviewDialog(row) {
  currentApplication.value = row
  reviewForm.status = 'ADMIN_APPROVED'
  reviewForm.reviewComment = ''
  reviewDialogVisible.value = true
}

async function handleReviewSubmit() {
  if (!currentApplication.value) return

  reviewSubmitting.value = true
  try {
    await reviewApplication(currentApplication.value.id, {
      status: reviewForm.status,
      reviewComment: reviewForm.reviewComment
    })
    ElMessage.success('审核完成')
    reviewDialogVisible.value = false
    fetchApplications()
  } catch (error) {
    console.error('审核提交失败:', error)
  } finally {
    reviewSubmitting.value = false
  }
}

onMounted(() => {
  fetchApplications()
})
</script>

<style lang="scss" scoped>
.application-review {
  padding: 20px;
}

.page-title {
  font-size: 22px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 20px 0;
}

.filter-tabs {
  margin-bottom: 16px;
}

.application-table {
  border-radius: 6px;

  .no-action {
    color: #c0c4cc;
  }
}

.expand-content {
  padding: 16px 32px;
  background: #fafafa;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.review-info {
  margin-bottom: 8px;
}

@media (max-width: 768px) {
  .application-review {
    padding: 12px;
  }

  .filter-tabs {
    :deep(.el-radio-button__inner) {
      padding: 8px 12px;
      font-size: 13px;
    }
  }

  .expand-content {
    padding: 12px 8px;
  }
}
</style>
