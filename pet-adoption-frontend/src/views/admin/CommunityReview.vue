<template>
  <div class="admin-community">
    <h2>社区帖子审核</h2>
    <p style="color:#909399;margin-bottom:16px">审核用户发布的社区帖子，通过后才会公开显示</p>

    <el-radio-group v-model="filterStatus" @change="fetchPosts" style="margin-bottom:16px">
      <el-radio-button value="">全部</el-radio-button>
      <el-radio-button value="PENDING">待审核</el-radio-button>
      <el-radio-button value="PUBLISHED">已通过</el-radio-button>
      <el-radio-button value="BLOCKED">已屏蔽</el-radio-button>
    </el-radio-group>

    <el-table :data="posts" stripe v-loading="loading" empty-text="暂无帖子">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="title" label="标题" min-width="180" />
      <el-table-column prop="content" label="内容" min-width="200">
        <template #default="{ row }">
          <span class="text-ellipsis" style="max-width:200px;display:inline-block">{{ row.content }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="username" label="作者" width="120" />
      <el-table-column prop="typeName" label="类型" width="100" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusTag(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="时间" width="120">
        <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <template v-if="row.status === 'PENDING'">
            <el-button size="small" type="success" @click="handleReview(row.id, 'PUBLISHED')">
              <el-icon><Select /></el-icon> 通过
            </el-button>
            <el-button size="small" type="danger" @click="handleReview(row.id, 'BLOCKED')">
              <el-icon><CloseBold /></el-icon> 屏蔽
            </el-button>
          </template>
          <el-tag v-else size="small" type="info">已处理</el-tag>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Select, CloseBold } from '@element-plus/icons-vue'
import request from '@/utils/request'

const posts = ref([])
const loading = ref(false)
const filterStatus = ref('')

function statusTag(s) { return s === 'PUBLISHED' ? 'success' : s === 'PENDING' ? 'warning' : 'danger' }
function statusText(s) { return s === 'PUBLISHED' ? '已通过' : s === 'PENDING' ? '待审核' : s === 'BLOCKED' ? '已屏蔽' : s }
function formatTime(t) { return t ? new Date(t).toLocaleDateString() : '' }

async function fetchPosts() {
  loading.value = true
  try {
    const res = await request.get('/api/community/admin/posts', { params: { page: 1, size: 50, status: filterStatus.value } })
    posts.value = res.data?.records || []
  } catch {
    posts.value = []
  } finally {
    loading.value = false
  }
}

async function handleReview(id, status) {
  try {
    await request.put(`/api/community/admin/posts/${id}/review`, { status })
    ElMessage.success(status === 'PUBLISHED' ? '已通过' : '已屏蔽')
    fetchPosts()
  } catch {
    ElMessage.error('操作失败')
  }
}

onMounted(fetchPosts)
</script>
