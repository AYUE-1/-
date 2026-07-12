<template>
  <div class="pet-manage">
    <div class="page-header">
      <h2 class="page-title">宠物管理</h2>
      <el-button type="primary" @click="router.push('/admin/pets/add')">
        <el-icon><Plus /></el-icon> 添加宠物
      </el-button>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input v-model="searchKeyword" placeholder="搜索宠物名称..." clearable style="width:220px"
        @keyup.enter="handleSearch" @clear="handleSearch">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <span class="count-hint">共 {{ totalCount }} 只宠物</span>
    </div>

    <!-- 按状态分 Tab -->
    <el-tabs v-model="filterStatus" @tab-change="handleSearch" type="card">
      <el-tab-pane v-for="tab in statusTabs" :key="tab.value" :name="tab.value">
        <template #label>
          <span>{{ tab.label }} <el-tag :type="tab.tagType" size="small" effect="plain">{{ tab.count }}</el-tag></span>
        </template>
      </el-tab-pane>
    </el-tabs>

    <!-- 宠物表格 -->
    <el-table v-loading="loading" :data="petList" border stripe class="pet-table">
      <el-table-column label="封面" width="80" align="center">
        <template #default="{ row }">
          <el-image v-if="row.coverImage" :src="getCoverUrl(row.coverImage)" fit="cover"
            class="table-thumb" :preview-src-list="[getCoverUrl(row.coverImage)]" preview-teleported />
          <span v-else class="no-image">-</span>
        </template>
      </el-table-column>
      <el-table-column prop="name" label="名称" min-width="120" show-overflow-tooltip />
      <el-table-column prop="categoryName" label="分类" width="80" />
      <el-table-column prop="breed" label="品种" width="110" show-overflow-tooltip />
      <el-table-column prop="gender" label="性别" width="60" align="center">
        <template #default="{ row }">{{ genderLabel(row.gender) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="viewCount" label="浏览" width="70" align="center" />
      <el-table-column label="发布者" width="100">
        <template #default="{ row }">
          <span v-if="row.createdBy">{{ '用户#' + row.createdBy }}</span>
          <span v-else style="color:#ccc">-</span>
        </template>
      </el-table-column>
      <el-table-column label="时间" width="110" align="center">
        <template #default="{ row }">{{ formatDate(row.createdAt || row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="180" align="center" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" link @click="handleEdit(row)">
            <el-icon><Edit /></el-icon> 编辑
          </el-button>
          <el-button type="warning" size="small" link @click="handleChangeStatus(row)">
            <el-icon><Switch /></el-icon> 状态
          </el-button>
          <el-button type="danger" size="small" link @click="handleDelete(row)">
            <el-icon><Delete /></el-icon> 删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-wrapper">
      <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50]" :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper" background
        @size-change="handleSizeChange" @current-change="handlePageChange" />
    </div>

    <!-- 修改状态弹窗 -->
    <el-dialog v-model="showStatusDialog" title="修改宠物状态" width="400px">
      <el-select v-model="newStatus" style="width:100%">
        <el-option label="待领养" value="AVAILABLE" />
        <el-option label="已预留" value="RESERVED" />
        <el-option label="已领养" value="ADOPTED" />
        <el-option label="个人宠物" value="OWNED" />
        <el-option label="已下架" value="OFFLINE" />
      </el-select>
      <template #footer>
        <el-button @click="showStatusDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmStatusChange">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Edit, Delete, Switch } from '@element-plus/icons-vue'
import { getPetList, deletePet, updatePetStatus } from '@/api/pet'

const router = useRouter()
const loading = ref(false)
const petList = ref([])
const searchKeyword = ref('')
const filterStatus = ref('ALL')
const totalCount = ref(0)
const showStatusDialog = ref(false)
const statusTarget = ref(null)
const newStatus = ref('')

const statusTabs = reactive([
  { label: '全部', value: 'ALL', tagType: 'info', count: 0 },
  { label: '待领养', value: 'AVAILABLE', tagType: 'success', count: 0 },
  { label: '已预留', value: 'RESERVED', tagType: 'warning', count: 0 },
  { label: '已领养', value: 'ADOPTED', tagType: 'primary', count: 0 },
  { label: '个人宠物', value: 'OWNED', tagType: '', count: 0 },
  { label: '已下架', value: 'OFFLINE', tagType: 'danger', count: 0 },
])

const pagination = reactive({ page: 1, size: 10, total: 0 })

function getCoverUrl(url) {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  if (url.startsWith('/')) return url
  return 'http://localhost:8080/' + url
}
function genderLabel(g) { return g === 'MALE' ? '公' : g === 'FEMALE' ? '母' : g || '-' }
function statusLabel(s) {
  const m = { AVAILABLE: '待领养', ADOPTED: '已领养', RESERVED: '已预留', OWNED: '个人宠物', OFFLINE: '已下架' }
  return m[s] || s
}
function statusTagType(s) {
  const m = { AVAILABLE: 'success', ADOPTED: 'primary', RESERVED: 'warning', OWNED: 'info', OFFLINE: 'danger' }
  return m[s] || 'info'
}
function formatDate(d) {
  if (!d) return '-'
  const t = new Date(d)
  return `${t.getFullYear()}-${String(t.getMonth()+1).padStart(2,'0')}-${String(t.getDate()).padStart(2,'0')}`
}

async function fetchPets() {
  loading.value = true
  try {
    const params = { page: pagination.page, size: pagination.size, status: filterStatus.value || 'ALL' }
    if (searchKeyword.value) params.keyword = searchKeyword.value
    const res = await getPetList(params)
    petList.value = res.data?.records ?? []
    pagination.total = res.data?.total ?? 0
  } catch { /* ignore */ } finally { loading.value = false }
}

async function fetchCounts() {
  // 异步获取各状态的宠物数量
  const statuses = ['ALL', 'AVAILABLE', 'RESERVED', 'ADOPTED', 'OWNED', 'OFFLINE']
  for (let i = 0; i < statuses.length; i++) {
    try {
      const res = await getPetList({ page: 1, size: 1, status: statuses[i] })
      statusTabs[i].count = res.data?.total ?? 0
      if (statuses[i] === 'ALL') totalCount.value = res.data?.total ?? 0
    } catch { statusTabs[i].count = 0 }
  }
}

function handleSearch() { pagination.page = 1; fetchPets() }
function handlePageChange(p) { pagination.page = p; fetchPets() }
function handleSizeChange(s) { pagination.size = s; pagination.page = 1; fetchPets() }
function handleEdit(row) { router.push(`/admin/pets/edit/${row.id}`) }

function handleChangeStatus(row) {
  statusTarget.value = row
  newStatus.value = row.status
  showStatusDialog.value = true
}

async function confirmStatusChange() {
  try {
    await updatePetStatus(statusTarget.value.id, newStatus.value)
    ElMessage.success('状态更新成功')
    showStatusDialog.value = false
    fetchPets()
  } catch { ElMessage.error('状态更新失败') }
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定要删除宠物「${row.name}」吗？`, '删除确认', { type: 'warning' })
    .then(async () => {
      try { await deletePet(row.id); ElMessage.success('已删除'); fetchPets() } catch {}
    }).catch(() => {})
}

onMounted(() => { fetchPets(); fetchCounts() })
</script>

<style lang="scss" scoped>
.pet-manage { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;
  .page-title { font-size: 22px; font-weight: 600; margin: 0; }
}
.search-bar { display: flex; gap: 12px; margin-bottom: 16px; align-items: center; flex-wrap: wrap; }
.count-hint { font-size: 13px; color: var(--color-text-secondary); margin-left: auto; }
.pet-table { border-radius: 6px; overflow: hidden;
  .table-thumb { width: 50px; height: 50px; border-radius: 4px; }
  .no-image { color: #c0c4cc; }
}
.pagination-wrapper { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
