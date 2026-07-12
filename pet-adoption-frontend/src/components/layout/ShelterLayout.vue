<template>
  <div class="shelter-layout">
    <el-container>
      <el-aside :width="isCollapse ? '64px' : '220px'" class="shelter-sidebar">
        <div class="sidebar-header" @click="collapseSidebar">
          <span v-show="!isCollapse" class="sidebar-title">
            <span class="sidebar-icon">🏠</span>
            <span>机构工作台</span>
          </span>
          <el-icon class="collapse-icon"><Fold v-if="!isCollapse" /><Expand v-else /></el-icon>
        </div>

        <el-menu
          :default-active="activeMenu"
          :router="true"
          :collapse="isCollapse"
          background-color="#2c3e50"
          text-color="#bfcbd9"
          active-text-color="#FF7F50"
          class="sidebar-menu"
        >
          <el-menu-item index="/shelter/dashboard">
            <el-icon><DataAnalysis /></el-icon>
            <template #title>工作台</template>
          </el-menu-item>
          <el-menu-item index="/shelter/pets">
            <el-icon><Grid /></el-icon>
            <template #title>宠物管理</template>
          </el-menu-item>
          <el-menu-item index="/shelter/adoptions">
            <el-icon><DocumentChecked /></el-icon>
            <template #title>领养审核</template>
          </el-menu-item>
          <el-menu-item index="/shelter/cert">
            <el-icon><Stamp /></el-icon>
            <template #title>机构认证</template>
          </el-menu-item>
        </el-menu>

        <div class="sidebar-footer">
          <router-link to="/" class="back-home-link">
            <el-icon><HomeFilled /></el-icon>
            <span v-show="!isCollapse">返回前台</span>
          </router-link>
        </div>
      </el-aside>

      <el-container>
        <el-header class="shelter-topbar">
          <div class="topbar-left">
            <el-breadcrumb separator="/">
              <el-breadcrumb-item :to="{ path: '/shelter/dashboard' }">机构工作台</el-breadcrumb-item>
              <el-breadcrumb-item v-if="pageTitle">{{ pageTitle }}</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          <div class="topbar-right">
            <router-link to="/" class="back-home-btn">
              <el-icon><HomeFilled /></el-icon>
              <span>返回前台</span>
            </router-link>
            <span class="shelter-user">{{ authStore.user?.nickname || authStore.user?.username }}</span>
            <el-tag v-if="dashboard?.certStatus === 'APPROVED'" type="success" size="small">已认证</el-tag>
            <el-button text @click="authStore.logout()">退出</el-button>
          </div>
        </el-header>

        <el-main class="shelter-main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const isCollapse = ref(false)
const dashboard = ref(null)

const activeMenu = computed(() => {
  const path = route.path
  if (path.startsWith('/shelter/pets')) return '/shelter/pets'
  if (path.startsWith('/shelter/adoptions')) return '/shelter/adoptions'
  if (path.startsWith('/shelter/cert')) return '/shelter/cert'
  return '/shelter/dashboard'
})

const pageTitle = computed(() => route.meta?.title || '')

function collapseSidebar() {
  isCollapse.value = !isCollapse.value
}

onMounted(() => {
  if (!authStore.isShelter) {
    ElMessage.warning('您没有机构权限')
    router.push('/')
  }
})
</script>

<style lang="scss" scoped>
.shelter-layout { height: 100vh; }
:deep(.el-container) { height: 100%; }

.shelter-sidebar {
  background-color: #2c3e50;
  transition: width 0.3s;
  overflow-x: hidden;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 16px;
  cursor: pointer;
  border-bottom: 1px solid rgba(255,255,255,0.06);
  color: #fff;
  .sidebar-title {
    display: flex; align-items: center; font-size: 16px; font-weight: bold; white-space: nowrap;
    .sidebar-icon { margin-right: 8px; font-size: 18px; }
  }
  .collapse-icon { font-size: 18px; color: #bfcbd9; }
}

.sidebar-menu { flex: 1; border-right: none; }

.sidebar-footer {
  padding: 12px 16px;
  border-top: 1px solid rgba(255,255,255,0.06);
}

.back-home-link {
  display: flex; align-items: center; color: #bfcbd9; font-size: 13px;
  transition: color 0.2s;
  .el-icon { margin-right: 6px; }
  &:hover { color: #409eff; }
}

.shelter-topbar {
  background: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  height: 50px;
}

.topbar-left {
  :deep(.el-breadcrumb) { font-size: 14px; }
}

.topbar-right {
  display: flex; align-items: center; gap: 16px;
}

.back-home-btn {
  display: flex; align-items: center; gap: 4px;
  color: var(--color-primary); font-size: 13px; text-decoration: none;
  padding: 4px 10px; border-radius: 6px;
  transition: background 0.2s;
  &:hover { background: var(--el-color-primary-light-9); }
}

.shelter-user { font-size: 13px; color: #666; }

.shelter-main {
  background: #f0f2f5;
  padding: 20px;
  min-height: calc(100vh - 50px);
}

@media (max-width: 768px) {
  .shelter-sidebar { position: absolute; z-index: 100; height: 100%; }
}
</style>
