<template>
  <div class="admin-articles">
    <h2>科普文章管理</h2>
    <p style="color:#909399;margin-bottom:16px">管理所有文章，审核用户投稿</p>

    <div style="margin-bottom:16px;display:flex;gap:12px;align-items:center;flex-wrap:wrap">
      <el-button type="primary" @click="$router.push('/admin/articles/add')">+ 添加文章</el-button>
      <el-radio-group v-model="filterStatus" @change="fetchList">
        <el-radio-button value="">全部</el-radio-button>
        <el-radio-button value="PENDING">待审核</el-radio-button>
        <el-radio-button value="PUBLISHED">已发布</el-radio-button>
        <el-radio-button value="BLOCKED">已屏蔽</el-radio-button>
      </el-radio-group>
    </div>

    <el-table :data="articles" stripe v-loading="loading" empty-text="暂无文章">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="title" label="标题" min-width="200" />
      <el-table-column prop="authorName" label="作者" width="120" />
      <el-table-column prop="categoryName" label="分类" width="100" />
      <el-table-column prop="viewCount" label="浏览" width="80" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusTag(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="时间" width="120">
        <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" min-width="240">
        <template #default="{ row }">
          <template v-if="row.status === 'PENDING'">
            <el-button size="small" type="success" @click="handleReview(row.id, 'PUBLISHED')">通过</el-button>
            <el-button size="small" type="danger" @click="handleReview(row.id, 'BLOCKED')">屏蔽</el-button>
          </template>
          <el-button size="small" @click="$router.push(`/admin/articles/edit/${row.id}`)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const articles = ref([])
const loading = ref(false)
const filterStatus = ref('')

function statusTag(s) { return s === 'PUBLISHED' ? 'success' : s === 'PENDING' ? 'warning' : 'danger' }
function statusText(s) { return s === 'PUBLISHED' ? '已发布' : s === 'PENDING' ? '待审核' : s === 'BLOCKED' ? '已屏蔽' : s }
function formatTime(t) { return t ? new Date(t).toLocaleDateString() : '' }

async function fetchList() {
  loading.value = true
  try {
    const res = await request.get('/api/articles/admin/list', { params: { page: 1, size: 50, status: filterStatus.value } })
    articles.value = res.data?.records || []
  } finally { loading.value = false }
}

async function handleReview(id, status) {
  try {
    await request.put(`/api/articles/admin/${id}/review`, { status })
    ElMessage.success(status === 'PUBLISHED' ? '已通过' : '已屏蔽')
    fetchList()
  } catch { ElMessage.error('操作失败') }
}

async function handleDelete(id) {
  try {
    await request.delete(`/api/articles/${id}`)
    ElMessage.success('已删除')
    fetchList()
  } catch { /* ignore */ }
}

onMounted(fetchList)
</script>
