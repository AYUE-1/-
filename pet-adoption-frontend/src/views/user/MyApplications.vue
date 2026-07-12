<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import AppHeader from '@/components/layout/AppHeader.vue'
import { getMyApplications, cancelApplication } from '@/api/adoption'

const router = useRouter()

const applications = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const statusMap = {
  PENDING: { label: '审核中', type: 'warning' },
  APPROVED: { label: '已通过', type: 'success' },
  REJECTED: { label: '已拒绝', type: 'danger' },
  CANCELLED: { label: '已取消', type: 'info' },
  COMPLETED: { label: '已完成', type: 'primary' }
}

async function fetchApplications() {
  loading.value = true
  try {
    const res = await getMyApplications(currentPage.value, pageSize.value)
    applications.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    ElMessage.error('获取申请列表失败')
  } finally {
    loading.value = false
  }
}

async function handleCancel(application) {
  try {
    await ElMessageBox.confirm(
      `确定要取消对"${application.petName || application.pet?.name}"的领养申请吗？`,
      '取消申请',
      {
        confirmButtonText: '确定',
        cancelButtonText: '返回',
        type: 'warning'
      }
    )
  } catch {
    return
  }

  try {
    await cancelApplication(application.id)
    ElMessage.success('申请已取消')
    fetchApplications()
  } catch (error) {
    ElMessage.error('取消失败')
  }
}

function handleViewDetail(row) {
  router.push(`/user/applications/${row.id}`)
}

function handleRowClick(row) {
  handleViewDetail(row)
}

function handlePageChange(page) {
  currentPage.value = page
  fetchApplications()
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
  fetchApplications()
})
</script>

<template>
  <div class="applications-page">
    <AppHeader />

    <div class="container">
      <div class="page-header">
        <h1 class="page-title">我的申请</h1>
      </div>

      <div class="table-card" v-loading="loading">
        <template v-if="applications.length > 0">
          <el-table
            :data="applications"
            stripe
            style="width: 100%"
            @row-click="handleRowClick"
          >
            <el-table-column prop="petName" label="宠物名称" min-width="160">
              <template #default="{ row }">
                <span>{{ row.petName || row.pet?.name || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="申请时间" width="180">
              <template #default="{ row }">
                <span>{{ formatDate(row.createdAt || row.applyTime) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="120" align="center">
              <template #default="{ row }">
                <el-tag :type="statusMap[row.status]?.type || 'info'" size="small">
                  {{ statusMap[row.status]?.label || row.status || '-' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180" align="center">
              <template #default="{ row }">
                <el-button
                  type="primary"
                  link
                  size="small"
                  @click.stop="handleViewDetail(row)"
                >
                  查看详情
                </el-button>
                <el-button
                  v-if="row.status === 'PENDING'"
                  type="danger"
                  link
                  size="small"
                  @click.stop="handleCancel(row)"
                >
                  取消
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-wrapper" v-if="total > pageSize">
            <el-pagination
              v-model:current-page="currentPage"
              :page-size="pageSize"
              :total="total"
              layout="prev, pager, next"
              background
              @current-change="handlePageChange"
            />
          </div>
        </template>

        <el-empty
          v-else
          description="暂无申请记录"
          :image-size="160"
        />
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.applications-page {
  min-height: 100vh;
  background: #f5f7fa;
}

.container {
  max-width: 1000px;
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

.table-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

  :deep(.el-table) {
    cursor: pointer;
  }

  :deep(.el-table__body tr:hover) {
    cursor: pointer;
  }
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

@media (max-width: 768px) {
  .table-card {
    padding: 12px;
  }
}
</style>
