<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import AppHeader from '@/components/layout/AppHeader.vue'
import { getMyFavorites, toggleFavorite } from '@/api/favorite'

const router = useRouter()

const favorites = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)

const statusMap = {
  AVAILABLE: { label: '可领养', type: 'success' },
  ADOPTED: { label: '已领养', type: 'info' },
  PENDING: { label: '审核中', type: 'warning' },
  UNAVAILABLE: { label: '暂不可领养', type: 'danger' }
}

async function fetchFavorites() {
  loading.value = true
  try {
    const res = await getMyFavorites(currentPage.value, pageSize.value)
    favorites.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    ElMessage.error('获取收藏列表失败')
  } finally {
    loading.value = false
  }
}

async function handleCancelFavorite(pet) {
  try {
    await ElMessageBox.confirm(
      `确定要取消收藏"${pet.name}"吗？`,
      '取消收藏',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
  } catch {
    return
  }

  try {
    await toggleFavorite(pet.id)
    ElMessage.success('已取消收藏')
    fetchFavorites()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

function handlePageChange(page) {
  currentPage.value = page
  fetchFavorites()
}

function goToDetail(petId) {
  router.push(`/pets/${petId}`)
}

onMounted(() => {
  fetchFavorites()
})
</script>

<template>
  <div class="favorites-page">
    <AppHeader />

    <div class="container">
      <div class="page-header">
        <h1 class="page-title">我的收藏</h1>
      </div>

      <div v-loading="loading">
        <template v-if="favorites.length > 0">
          <div class="pet-grid">
            <div
              v-for="item in favorites"
              :key="item.id"
              class="favorite-card"
              @click="item.petId ? goToDetail(item.petId) : null"
            >
              <div class="card-cover">
                <img
                  v-if="item.coverImage || item.pet?.coverImage"
                  :src="item.coverImage || item.pet?.coverImage"
                  :alt="item.petName || item.pet?.name"
                  class="cover-image"
                />
                <div v-else class="cover-placeholder">
                  <span>暂无图片</span>
                </div>
              </div>
              <div class="card-body">
                <h3 class="pet-name text-ellipsis">
                  {{ item.petName || item.pet?.name || '未知' }}
                </h3>
                <p class="pet-breed text-ellipsis">
                  {{ item.breed || item.pet?.breed || '-' }}
                </p>
                <div class="card-footer">
                  <el-tag
                    :type="statusMap[item.status || item.pet?.status]?.type || 'info'"
                    size="small"
                  >
                    {{ statusMap[item.status || item.pet?.status]?.label || '未知' }}
                  </el-tag>
                  <el-button
                    type="danger"
                    size="small"
                    plain
                    @click.stop="handleCancelFavorite(item)"
                  >
                    取消收藏
                  </el-button>
                </div>
              </div>
            </div>
          </div>

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
          description="暂无收藏，快去浏览宠物吧"
          :image-size="160"
        />
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.favorites-page {
  min-height: 100vh;
  background: #f5f7fa;
}

.container {
  max-width: 1200px;
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

.pet-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;

  @media (max-width: 1200px) { grid-template-columns: repeat(3, 1fr); }
  @media (max-width: 992px)  { grid-template-columns: repeat(2, 1fr); }
  @media (max-width: 576px)  { grid-template-columns: 1fr; }
}

.favorite-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  }
}

.card-cover {
  width: 100%;
  height: 200px;
  overflow: hidden;
  background: #f5f7fa;

  .cover-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .cover-placeholder {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #999;
    font-size: 14px;
  }
}

.card-body {
  padding: 14px 16px 16px;
}

.pet-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 6px;
}

.pet-breed {
  font-size: 13px;
  color: #999;
  margin-bottom: 12px;
}

.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 40px;
}
</style>
