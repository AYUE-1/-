<template>
  <div class="shelter-applications">
    <h2>领养审核</h2>
    <p class="subtitle">审核用户对你发布的宠物提交的领养申请</p>

    <el-radio-group v-model="activeStatus" @change="fetchList" style="margin-bottom:16px">
      <el-radio-button value="">全部</el-radio-button>
      <el-radio-button value="ADMIN_APPROVED">待我处理</el-radio-button>
      <el-radio-button value="APPROVED">已同意</el-radio-button>
      <el-radio-button value="OWNER_REJECTED">已拒绝</el-radio-button>
      <el-radio-button value="COMPLETED">已完成</el-radio-button>
    </el-radio-group>

    <el-table :data="list" stripe v-loading="loading" empty-text="暂无领养申请">
      <el-table-column prop="applicantName" label="申请人" width="120" />
      <el-table-column prop="petName" label="宠物名称" width="140" />
      <el-table-column prop="phone" label="联系电话" width="130" />
      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="statusTag(row.status)">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="申请时间" width="180">
        <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <template v-if="row.status === 'ADMIN_APPROVED'">
            <el-button size="small" type="success" @click="handleReview(row.id, 'APPROVED')">同意领养</el-button>
            <el-button size="small" type="danger" @click="handleReview(row.id, 'OWNER_REJECTED')">拒绝</el-button>
          </template>
          <span v-else style="color:#909399">-</span>
        </template>
      </el-table-column>
      <el-table-column type="expand">
        <template #default="{ row }">
          <div class="expand-detail">
            <el-descriptions :column="2" border size="small">
              <el-descriptions-item label="真实姓名">{{ row.realName }}</el-descriptions-item>
              <el-descriptions-item label="身份证号">{{ row.idCard }}</el-descriptions-item>
              <el-descriptions-item label="联系电话">{{ row.phone }}</el-descriptions-item>
              <el-descriptions-item label="居住地址">{{ row.address }}</el-descriptions-item>
              <el-descriptions-item label="住房类型">{{ row.housingType }}</el-descriptions-item>
              <el-descriptions-item label="职业">{{ row.occupation }}</el-descriptions-item>
              <el-descriptions-item label="收入范围">{{ row.incomeRange }}</el-descriptions-item>
              <el-descriptions-item label="养宠经验">{{ row.hasExperience ? '有' : '无' }}</el-descriptions-item>
              <el-descriptions-item label="家人同意">{{ row.familyAgreed ? '是' : '否' }}</el-descriptions-item>
              <el-descriptions-item label="申请理由" :span="2">{{ row.reason }}</el-descriptions-item>
              <el-descriptions-item label="管理员备注" :span="2" v-if="row.reviewComment">{{ row.reviewComment }}</el-descriptions-item>
            </el-descriptions>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-if="total > pageSize"
      v-model:current-page="page"
      :page-size="pageSize"
      :total="total"
      layout="total, prev, pager, next, jumper"
      @current-change="fetchList"
      style="margin-top:16px;justify-content:center"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOwnerApplications, ownerReviewApplication } from '@/api/adoption'

const list = ref([])
const loading = ref(false)
const activeStatus = ref('ADMIN_APPROVED')
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

function statusTag(s) {
  return s === 'APPROVED' || s === 'COMPLETED' ? 'success' : s === 'ADMIN_APPROVED' ? 'warning' : s === 'OWNER_REJECTED' ? 'danger' : 'info'
}
function statusLabel(s) {
  return s === 'PENDING' ? '待管理员审核' : s === 'ADMIN_APPROVED' ? '待您确认' : s === 'APPROVED' ? '已同意' : s === 'REJECTED' ? '管理员已拒绝' : s === 'OWNER_REJECTED' ? '您已拒绝' : s === 'COMPLETED' ? '已完成' : s === 'CANCELLED' ? '已取消' : s
}
function formatDate(d) { return d ? new Date(d).toLocaleString('zh-CN') : '' }

async function fetchList() {
  loading.value = true
  try {
    const res = await getOwnerApplications(page.value, pageSize.value, activeStatus.value || null)
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch { list.value = []; total.value = 0 }
  finally { loading.value = false }
}

async function handleReview(id, status) {
  const action = status === 'APPROVED' ? '同意领养' : '拒绝'
  try {
    await ElMessageBox.confirm(`确认${action}该申请？`, action, {
      confirmButtonText: `确认${action}`,
      cancelButtonText: '取消',
      type: status === 'APPROVED' ? 'success' : 'warning',
      inputType: 'textarea',
      inputPlaceholder: `请输入${action}理由（选填）`
    }).then(async ({ value }) => {
      await ownerReviewApplication(id, { status, reviewComment: value || '' })
      ElMessage.success(`${action}成功`)
      fetchList()
    }).catch(() => {})
  } catch {}
}

onMounted(fetchList)
</script>

<style lang="scss" scoped>
h2 { margin: 0 0 4px; font-size: 20px; }
.subtitle { color: #909399; margin-bottom: 16px; }
.expand-detail { padding: 16px; }
</style>
