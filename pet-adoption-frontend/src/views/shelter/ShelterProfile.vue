<template>
  <div class="shelter-profile-page">
    <AppHeader />
    <div class="container">
      <div v-if="loading" class="loading-wrap"><el-skeleton :rows="6" animated /></div>
      <div v-else-if="!profile" class="empty-wrap">
        <el-empty description="该机构不存在"><el-button @click="$router.push('/')">返回首页</el-button></el-empty>
      </div>
      <template v-else>
        <!-- 机构信息 -->
        <div class="profile-card">
          <div class="profile-header">
            <el-avatar :size="72" :src="profile.avatar" class="profile-avatar">
              {{ (profile.nickname || '?')[0] }}
            </el-avatar>
            <div class="profile-info">
              <div class="name-row">
                <h1>{{ profile.nickname }}</h1>
                <el-tag v-if="profile.certStatus === 'APPROVED'" type="success" size="large" effect="dark">已认证</el-tag>
                <el-tag v-else type="info" size="large">未认证</el-tag>
              </div>
              <p class="meta" v-if="profile.shelterName">🏠 {{ profile.shelterName }}</p>
              <p class="meta" v-if="profile.shelterAddress">📍 {{ profile.shelterAddress }}</p>
              <p class="meta" v-if="profile.bio">{{ profile.bio }}</p>
            </div>
          </div>
          <div class="profile-stats">
            <div class="pstat"><span class="pstat-num">{{ profile.petCount || 0 }}</span><span class="pstat-label">在架宠物</span></div>
            <div class="pstat"><span class="pstat-num">{{ profile.adoptedCount || 0 }}</span><span class="pstat-label">已成功领养</span></div>
          </div>
        </div>

        <!-- 在架宠物 -->
        <section class="pets-section">
          <h2 class="section-title">在架宠物</h2>
          <el-row :gutter="16" v-if="profile.pets && profile.pets.length">
            <el-col v-for="pet in profile.pets" :key="pet.id" :xs="24" :sm="12" :md="8" :lg="6">
              <div class="pet-card" @click="$router.push(`/pets/${pet.id}`)">
                <div class="pet-cover">
                  <img :src="pet.coverImage || '/placeholder.jpg'" :alt="pet.name" />
                </div>
                <div class="pet-body">
                  <h4>{{ pet.name }}</h4>
                  <p>{{ pet.breed || '未知' }} · {{ pet.age || '未知' }}</p>
                </div>
              </div>
            </el-col>
          </el-row>
          <el-empty v-else description="暂无在架宠物" :image-size="80" />
        </section>
      </template>
    </div>
    <AppFooter />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getShelterProfile } from '@/api/shelter'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'

const route = useRoute()
const profile = ref(null)
const loading = ref(true)

onMounted(async () => {
  try {
    const res = await getShelterProfile(route.params.id)
    profile.value = res.data
  } catch { profile.value = null }
  finally { loading.value = false }
})
</script>

<style lang="scss" scoped>
.shelter-profile-page { min-height: 100vh; background: var(--color-bg); }
.container { max-width: 960px; margin: 0 auto; padding: 32px 16px 64px; }
.loading-wrap { padding: 40px; background: #fff; border-radius: 16px; }
.empty-wrap { padding: 80px 0; }

.profile-card {
  background: #fff; border-radius: 16px; padding: 32px;
  margin-bottom: 24px; box-shadow: 0 2px 12px rgba(0,0,0,0.04);
}
.profile-header { display: flex; gap: 24px; align-items: center; margin-bottom: 24px; }
.profile-avatar { background: var(--color-primary); color: #fff; font-size: 28px; flex-shrink: 0; }
.name-row { display: flex; align-items: center; gap: 12px; margin-bottom: 8px; h1 { font-size: 22px; margin: 0; } }
.meta { color: var(--color-text-secondary); font-size: 14px; margin-bottom: 4px; }

.profile-stats { display: flex; gap: 32px; padding-top: 20px; border-top: 1px solid #f0f0f0; }
.pstat { text-align: center; }
.pstat-num { font-size: 24px; font-weight: bold; color: var(--color-primary); display: block; }
.pstat-label { font-size: 13px; color: #909399; }

.section-title { font-size: 20px; margin-bottom: 16px; }

.pet-card {
  background: #fff; border-radius: 12px; overflow: hidden; cursor: pointer;
  margin-bottom: 16px; box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  transition: box-shadow 0.2s;
  &:hover { box-shadow: 0 4px 16px rgba(0,0,0,0.1); }
}
.pet-cover { height: 140px; overflow: hidden; img { width: 100%; height: 100%; object-fit: cover; } }
.pet-body { padding: 12px 16px; h4 { margin: 0 0 4px; } p { font-size: 13px; color: #909399; margin: 0; } }

@media (max-width: 768px) {
  .profile-header { flex-direction: column; text-align: center; }
  .name-row { justify-content: center; }
}
</style>
