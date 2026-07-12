<template>
  <div class="pet-list-page">
    <AppHeader />

    <div class="page-content">
      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-input
          v-model="query.keyword"
          placeholder="请输入宠物名称或品种搜索..."
          class="search-input"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="handleSearch" :icon="Search">搜索</el-button>
      </div>

      <!-- 筛选面板 -->
      <div class="filter-panel">
        <div class="filter-row">
          <div class="filter-item">
            <label>分类：</label>
            <el-select
              v-model="query.categoryId"
              placeholder="全部分类"
              clearable
              @change="handleFilterChange"
            >
              <el-option
                v-for="cat in categories"
                :key="cat.id"
                :label="cat.name"
                :value="cat.id"
              />
            </el-select>
          </div>

          <div class="filter-item">
            <label>性别：</label>
            <el-select
              v-model="query.gender"
              placeholder="全部性别"
              clearable
              @change="handleFilterChange"
            >
              <el-option label="公" value="公" />
              <el-option label="母" value="母" />
            </el-select>
          </div>

          <div class="filter-item">
            <label>体型：</label>
            <el-select
              v-model="query.size"
              placeholder="全部体型"
              clearable
              @change="handleFilterChange"
            >
              <el-option label="小型" value="小型" />
              <el-option label="中型" value="中型" />
              <el-option label="大型" value="大型" />
            </el-select>
          </div>

          <div class="filter-item">
            <label>排序：</label>
            <el-radio-group
              v-model="query.sortBy"
              @change="handleFilterChange"
              size="small"
            >
              <el-radio-button value="latest">最新发布</el-radio-button>
              <el-radio-button value="popular">最受欢迎</el-radio-button>
            </el-radio-group>
          </div>
        </div>
      </div>

      <!-- 宠物列表 -->
      <div v-if="loading" class="loading-container">
        <el-icon class="is-loading" :size="32"><Loading /></el-icon>
        <p>正在加载...</p>
      </div>

      <div v-else-if="petList.length === 0" class="empty-container">
        <el-empty description="暂无宠物信息" />
      </div>

      <div v-else class="pet-grid">
        <PetCard v-for="pet in petList" :key="pet.id" :pet="pet" />
      </div>

      <!-- 分页 -->
      <div v-if="total > 0" class="pagination-wrapper">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.size"
          :page-sizes="[8, 12, 20, 40]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="handlePageSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </div>
    <AppFooter />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, Loading } from '@element-plus/icons-vue'
import { getPetList } from '@/api/pet'
import { useAuthStore } from '@/stores/auth'
import request from '@/utils/request'
import PetCard from './PetCard.vue'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'

const router = useRouter()
const authStore = useAuthStore()

// 分类列表
const categories = ref([])

// 查询参数
const query = reactive({
  keyword: '',
  categoryId: null,
  gender: '',
  size: '',
  sortBy: 'latest',
  page: 1,
  size: 12
})

// 宠物列表
const petList = ref([])
const total = ref(0)
const loading = ref(false)

// 获取分类列表
async function fetchCategories() {
  try {
    const res = await request.get('/api/categories')
    categories.value = res.data || []
  } catch {
    // 静默处理，分类筛选仍可使用
  }
}

// 获取宠物列表
async function fetchPetList() {
  loading.value = true
  try {
    const params = {
      page: query.page,
      size: query.size,
      sortBy: query.sortBy
    }
    if (query.keyword) params.keyword = query.keyword
    if (query.categoryId) params.categoryId = query.categoryId
    if (query.gender) params.gender = query.gender
    if (query.size) params.size = query.size

    const res = await getPetList(params)
    const data = res.data
    if (data && data.records) {
      petList.value = data.records
      total.value = data.total || 0
    } else if (Array.isArray(data)) {
      petList.value = data
      total.value = data.length
    } else {
      petList.value = data?.records || data?.list || []
      total.value = data?.total || data?.totalElements || 0
    }
  } catch (error) {
    ElMessage.error('获取宠物列表失败')
    petList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 搜索
function handleSearch() {
  query.page = 1
  fetchPetList()
}

// 筛选变更
function handleFilterChange() {
  query.page = 1
  fetchPetList()
}

// 翻页
function handlePageChange(page) {
  query.page = page
  fetchPetList()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function handlePageSizeChange() {
  query.page = 1
  fetchPetList()
}

onMounted(() => {
  fetchCategories()
  fetchPetList()
})
</script>

<style scoped lang="scss">
.pet-list-page {
  min-height: 100vh;
  background: var(--color-bg);
}

.page-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 20px;
}

// 搜索栏
.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;

  .search-input {
    flex: 1;
    max-width: 500px;
  }
}

// 筛选面板
.filter-panel {
  background: #fff;
  border-radius: 8px;
  padding: 16px 20px;
  margin-bottom: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);

  .filter-row {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 20px;
  }

  .filter-item {
    display: flex;
    align-items: center;
    gap: 8px;

    label {
      font-size: 14px;
      color: #606266;
      white-space: nowrap;
    }

    .el-select {
      width: 140px;
    }
  }
}

// 加载中
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 0;
  color: #909399;

  p {
    margin-top: 12px;
  }
}

// 空状态
.empty-container {
  padding: 80px 0;
}

// 宠物网格
.pet-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;

  @media (max-width: 1024px) {
    grid-template-columns: repeat(3, 1fr);
  }
  @media (max-width: 768px) {
    grid-template-columns: repeat(2, 1fr);
  }
  @media (max-width: 480px) {
    grid-template-columns: 1fr;
  }
}

// 分页
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 32px;
  padding-bottom: 20px;
}
</style>
