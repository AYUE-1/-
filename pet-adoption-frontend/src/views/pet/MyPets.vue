<template>
  <div class="my-pets-page">
    <AppHeader />
    <div class="container">
      <div class="page-header">
        <h1 class="page-title">我发布的宠物</h1>
        <el-button type="primary" @click="$router.push('/pets/publish')">
          <el-icon><Plus /></el-icon>
          <span>发布领养</span>
        </el-button>
      </div>

      <div v-if="pets.length === 0 && !loading" class="empty-state">
        <el-empty description="还没有发布过领养信息">
          <el-button type="primary" @click="$router.push('/pets/publish')">发布领养</el-button>
        </el-empty>
      </div>

      <div v-loading="loading" class="pet-grid">
        <div v-for="pet in pets" :key="pet.id" class="pet-card">
          <div class="pet-cover" @click="$router.push(`/pets/${pet.id}`)">
            <img v-if="pet.coverImage" :src="pet.coverImage" :alt="pet.name" />
            <div v-else class="no-image">暂无图片</div>
            <el-tag
              :type="pet.status === 'AVAILABLE' ? 'success' : pet.status === 'ADOPTED' ? 'info' : 'warning'"
              class="status-tag"
              size="small"
            >
              {{ statusLabel(pet.status) }}
            </el-tag>
          </div>
          <div class="pet-info">
            <h3 class="pet-name" @click="$router.push(`/pets/${pet.id}`)">{{ pet.name }}</h3>
            <p class="pet-meta">{{ pet.breed || '-' }} · {{ pet.age || '-' }} · {{ pet.gender === 'MALE' ? '♂' : pet.gender === 'FEMALE' ? '♀' : '' }}</p>
            <p class="pet-views">👁 {{ pet.viewCount }} 次浏览</p>
            <div class="pet-actions">
              <el-button size="small" @click="$router.push(`/pets/${pet.id}`)">查看</el-button>
              <el-button size="small" type="primary" @click="$router.push(`/pets/edit/${pet.id}`)">编辑</el-button>
              <el-popconfirm title="确定要删除此宠物信息吗？" @confirm="handleDelete(pet.id)">
                <template #reference>
                  <el-button size="small" type="danger">删除</el-button>
                </template>
              </el-popconfirm>
            </div>
          </div>
        </div>
      </div>

      <div v-if="total > pagination.size" class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          :page-size="pagination.size"
          :total="total"
          layout="prev, pager, next"
          background
          @current-change="fetchPets"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import AppHeader from '@/components/layout/AppHeader.vue'
import { getMyPets, deletePet } from '@/api/pet'

const loading = ref(false)
const pets = ref([])
const total = ref(0)
const pagination = reactive({ page: 1, size: 12 })

function statusLabel(status) {
  const map = { AVAILABLE: '可领养', RESERVED: '已预留', ADOPTED: '已领养', OFFLINE: '已下架' }
  return map[status] || status
}

async function fetchPets() {
  loading.value = true
  try {
    const res = await getMyPets(pagination.page, pagination.size)
    pets.value = res.data?.records ?? []
    total.value = res.data?.total ?? 0
  } catch (e) {
    console.error('获取我的宠物失败:', e)
  } finally {
    loading.value = false
  }
}

async function handleDelete(id) {
  try {
    await deletePet(id)
    ElMessage.success('已删除')
    fetchPets()
  } catch (e) {
    console.error('删除失败:', e)
  }
}

onMounted(() => fetchPets())
</script>

<style lang="scss" scoped>
.my-pets-page {
  min-height: 100vh;
  background: #f5f7fa;
}

.container {
  max-width: 1100px;
  margin: 0 auto;
  padding: 0 16px 40px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 0;
}

.page-title {
  font-size: 22px;
  font-weight: bold;
  color: #333;
}

.pet-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 20px;
}

.pet-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  transition: transform 0.2s;
  &:hover { transform: translateY(-2px); }
}

.pet-cover {
  position: relative;
  height: 180px;
  cursor: pointer;
  background: #f5f7fa;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .no-image {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 100%;
    color: #999;
    font-size: 14px;
  }

  .status-tag {
    position: absolute;
    top: 8px;
    right: 8px;
  }
}

.pet-info {
  padding: 14px 16px;
}

.pet-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  cursor: pointer;
  margin: 0 0 4px;
  &:hover { color: #409eff; }
}

.pet-meta {
  font-size: 13px;
  color: #999;
  margin: 0 0 4px;
}

.pet-views {
  font-size: 12px;
  color: #bbb;
  margin: 0 0 10px;
}

.pet-actions {
  display: flex;
  gap: 8px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}

.empty-state {
  padding: 80px 0;
}

@media (max-width: 768px) {
  .pet-grid {
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 12px;
  }
}
</style>
