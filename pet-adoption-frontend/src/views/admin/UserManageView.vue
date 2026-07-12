<template>
  <div class="user-manage">
    <h2 class="page-title">用户管理</h2>

    <!-- 用户表格 -->
    <el-table
      v-loading="loading"
      :data="userList"
      border
      stripe
      style="width: 100%"
      class="user-table"
    >
      <el-table-column prop="id" label="ID" width="70" align="center" />
      <el-table-column prop="username" label="用户名" min-width="120" show-overflow-tooltip />
      <el-table-column label="昵称" width="120" show-overflow-tooltip>
        <template #default="{ row }">
          {{ row.nickname || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="email" label="邮箱" min-width="160" show-overflow-tooltip />
      <el-table-column label="手机号" width="130" align="center">
        <template #default="{ row }">
          {{ row.phone || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="角色" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'primary'" size="small">
            {{ row.role === 'ADMIN' ? '管理员' : '用户' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'" size="small">
            {{ row.status === 'ACTIVE' ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="注册时间" width="170" align="center">
        <template #default="{ row }">
          {{ formatDate(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" align="center" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="row.status === 'ACTIVE'"
            type="danger"
            size="small"
            link
            @click="handleToggleStatus(row)"
          >
            禁用
          </el-button>
          <el-button
            v-else
            type="success"
            size="small"
            link
            @click="handleToggleStatus(row)"
          >
            启用
          </el-button>
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, updateUserStatus } from '@/api/user'

const loading = ref(false)
const userList = ref([])

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

function formatDate(dateStr) {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

async function fetchUsers() {
  loading.value = true
  try {
    const res = await getUserList(pagination.page, pagination.size)
    userList.value = res.data?.records ?? []
    pagination.total = res.data?.total ?? 0
  } catch (error) {
    console.error('获取用户列表失败:', error)
  } finally {
    loading.value = false
  }
}

function handlePageChange(page) {
  pagination.page = page
  fetchUsers()
}

function handleSizeChange(size) {
  pagination.size = size
  pagination.page = 1
  fetchUsers()
}

function handleToggleStatus(row) {
  const newStatus = row.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE'
  const actionText = newStatus === 'ACTIVE' ? '启用' : '禁用'

  ElMessageBox.confirm(
    `确定要${actionText}用户「${row.username}」吗？`,
    `${actionText}确认`,
    {
      confirmButtonText: `确定${actionText}`,
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
    .then(async () => {
      try {
        await updateUserStatus(row.id, newStatus)
        row.status = newStatus
        ElMessage.success(`用户已${actionText}`)
      } catch (error) {
        console.error('更新用户状态失败:', error)
      }
    })
    .catch(() => {
      // 用户取消操作
    })
}

onMounted(() => {
  fetchUsers()
})
</script>

<style lang="scss" scoped>
.user-manage {
  padding: 20px;
}

.page-title {
  font-size: 22px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 20px 0;
}

.user-table {
  border-radius: 6px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

@media (max-width: 768px) {
  .user-manage {
    padding: 12px;
  }
}
</style>
