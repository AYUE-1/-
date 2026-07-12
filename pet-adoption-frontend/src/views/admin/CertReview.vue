<template>
  <div class="admin-certs">
    <h2>机构认证审核</h2>
    <p style="color:#909399;margin-bottom:16px">审核救助机构的认证申请</p>

    <el-table :data="certs" stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="shelterName" label="机构名称" min-width="160" />
      <el-table-column prop="shelterAddress" label="机构地址" min-width="180" />
      <el-table-column prop="phone" label="联系电话" width="130" />
      <el-table-column prop="certStatus" label="认证状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusTag(row.certStatus)">{{ statusText(row.certStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <template v-if="row.certStatus === 'PENDING'">
            <el-button size="small" type="success" @click="handleReview(row.id, 'APPROVED')">通过</el-button>
            <el-button size="small" type="danger" @click="handleReject(row.id)">拒绝</el-button>
          </template>
          <span v-else style="color:#909399">已处理</span>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-if="!loading && certs.length === 0" description="暂无认证申请" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCertList, reviewCert } from '@/api/cert'

const certs = ref([])
const loading = ref(false)

function statusTag(s) {
  return s === 'APPROVED' ? 'success' : s === 'PENDING' ? 'warning' : 'danger'
}
function statusText(s) {
  return s === 'APPROVED' ? '已认证' : s === 'PENDING' ? '待审核' : s === 'REJECTED' ? '已拒绝' : '未认证'
}

async function fetchCerts() {
  loading.value = true
  try {
    const res = await getCertList({ page: 1, size: 100 })
    certs.value = res.data?.records || []
  } catch {
    certs.value = []
  } finally {
    loading.value = false
  }
}

async function handleReview(userId, status) {
  try {
    await reviewCert(userId, { status, comment: '' })
    ElMessage.success(status === 'APPROVED' ? '已通过认证' : '已拒绝')
    fetchCerts()
  } catch {
    ElMessage.error('操作失败')
  }
}

async function handleReject(userId) {
  try {
    const { value } = await ElMessageBox.prompt('请输入拒绝理由', '拒绝认证', {
      confirmButtonText: '确认拒绝',
      cancelButtonText: '取消',
      inputType: 'textarea',
      inputPlaceholder: '请填写拒绝理由...'
    })
    await reviewCert(userId, { status: 'REJECTED', comment: value || '' })
    ElMessage.success('已拒绝')
    fetchCerts()
  } catch {
    // 用户取消
  }
}

onMounted(fetchCerts)
</script>
