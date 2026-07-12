<template>
  <div class="shelter-pets">
    <div class="page-header">
      <h2>宠物管理</h2>
      <el-button type="primary" @click="$router.push('/pets/publish')">发布宠物</el-button>
    </div>

    <el-tabs v-model="activeStatus" @tab-change="fetchPets" type="card">
      <el-tab-pane label="全部" name="" />
      <el-tab-pane label="在架" name="AVAILABLE" />
      <el-tab-pane label="已预留" name="RESERVED" />
      <el-tab-pane label="已领养" name="ADOPTED" />
      <el-tab-pane label="已下架" name="OFFLINE" />
    </el-tabs>

    <el-row :gutter="16" v-loading="loading">
      <el-col v-for="pet in pets" :key="pet.id" :xs="24" :sm="12" :md="8" :lg="6">
        <div class="pet-card">
          <div class="pet-cover" @click="$router.push(`/pets/${pet.id}`)">
            <img :src="pet.coverImage || '/placeholder.jpg'" :alt="pet.name" />
            <el-tag :type="statusType(pet.status)" class="status-tag" size="small">{{ statusLabel(pet.status) }}</el-tag>
          </div>
          <div class="pet-body">
            <h4 class="pet-name" @click="$router.push(`/pets/${pet.id}`)">{{ pet.name }}</h4>
            <p class="pet-info">{{ pet.breed || '未知品种' }} · {{ pet.age || '未知年龄' }} · {{ pet.gender || '未知' }}</p>
            <p class="pet-views">👁 {{ pet.viewCount || 0 }} 次浏览</p>
          </div>
          <div class="pet-actions">
            <el-button size="small" @click="$router.push(`/pets/edit/${pet.id}`)">编辑</el-button>
            <el-button v-if="pet.status === 'AVAILABLE'" size="small" type="warning" @click="handleStatus(pet.id, 'OFFLINE')">下架</el-button>
            <el-button v-if="pet.status === 'OFFLINE'" size="small" type="success" @click="handleStatus(pet.id, 'AVAILABLE')">上架</el-button>
            <el-popconfirm title="确定删除该宠物？" @confirm="handleDelete(pet.id)">
              <template #reference>
                <el-button size="small" type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </div>
        </div>
      </el-col>
    </el-row>
    <el-empty v-if="!loading && pets.length === 0" description="暂无宠物" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyPets, deletePet, updatePetStatus } from '@/api/pet'

const pets = ref([])
const loading = ref(false)
const activeStatus = ref('')

function statusType(s) { return s === 'AVAILABLE' ? 'success' : s === 'ADOPTED' ? 'info' : s === 'RESERVED' ? 'warning' : 'danger' }
function statusLabel(s) { return s === 'AVAILABLE' ? '在架' : s === 'ADOPTED' ? '已领养' : s === 'RESERVED' ? '已预留' : '已下架' }

async function fetchPets() {
  loading.value = true
  try {
    const res = await getMyPets(1, 100)
    let list = res.data?.records || []
    if (activeStatus.value) list = list.filter(p => p.status === activeStatus.value)
    pets.value = list
  } catch { pets.value = [] }
  finally { loading.value = false }
}

async function handleStatus(petId, status) {
  try {
    await updatePetStatus(petId, status)
    ElMessage.success('状态已更新')
    fetchPets()
  } catch { ElMessage.error('操作失败') }
}

async function handleDelete(petId) {
  try {
    await deletePet(petId)
    ElMessage.success('已删除')
    fetchPets()
  } catch { ElMessage.error('删除失败') }
}

onMounted(fetchPets)
</script>

<style lang="scss" scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; h2 { margin: 0; font-size: 20px; } }

.pet-card {
  background: #fff; border-radius: 12px; overflow: hidden; margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  transition: box-shadow 0.2s;
  &:hover { box-shadow: 0 4px 16px rgba(0,0,0,0.1); }
}

.pet-cover {
  position: relative; height: 160px; overflow: hidden; cursor: pointer;
  img { width: 100%; height: 100%; object-fit: cover; }
  .status-tag { position: absolute; top: 8px; right: 8px; }
}

.pet-body { padding: 12px 16px 8px; }
.pet-name { margin: 0 0 4px; font-size: 15px; cursor: pointer; &:hover { color: var(--color-primary); } }
.pet-info { font-size: 13px; color: #909399; margin: 0 0 4px; }
.pet-views { font-size: 12px; color: #c0c4cc; margin: 0; }

.pet-actions {
  padding: 8px 16px 12px; display: flex; gap: 6px; flex-wrap: wrap;
  border-top: 1px solid #f0f0f0;
}
</style>
