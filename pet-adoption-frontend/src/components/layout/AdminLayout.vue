<template>
  <div class="admin-layout">
    <el-container>
      <!-- 左侧边栏 -->
      <el-aside :width="isCollapse ? '64px' : '220px'" class="admin-sidebar">
        <div class="sidebar-header" @click="collapseSidebar">
          <span v-show="!isCollapse" class="sidebar-title">
            <span class="sidebar-icon">🐾</span>
            <span>管理后台</span>
          </span>
          <el-icon class="collapse-icon">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
        </div>

        <el-menu
          :default-active="activeMenu"
          :router="true"
          :collapse="isCollapse"
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#FF7F50"
          class="sidebar-menu"
        >
          <el-menu-item index="/admin/dashboard">
            <el-icon><DataAnalysis /></el-icon>
            <template #title>仪表盘</template>
          </el-menu-item>
          <el-menu-item index="/admin/pets">
            <el-icon><Grid /></el-icon>
            <template #title>宠物管理</template>
          </el-menu-item>
          <el-menu-item index="/admin/adoptions">
            <el-icon><DocumentChecked /></el-icon>
            <template #title>领养审核</template>
          </el-menu-item>
          <el-menu-item index="/admin/users">
            <el-icon><User /></el-icon>
            <template #title>用户管理</template>
          </el-menu-item>
          <el-menu-item index="/admin/articles">
            <el-icon><Reading /></el-icon>
            <template #title>科普文章</template>
          </el-menu-item>
          <el-menu-item index="/admin/community">
            <el-icon><ChatDotRound /></el-icon>
            <template #title>社区审核</template>
          </el-menu-item>
          <el-menu-item index="/admin/reports">
            <el-icon><Warning /></el-icon>
            <template #title>举报管理</template>
          </el-menu-item>
          <el-menu-item index="/admin/certs">
            <el-icon><Stamp /></el-icon>
            <template #title>认证审核</template>
          </el-menu-item>
        </el-menu>

        <div class="sidebar-footer">
          <router-link to="/" class="back-home-link">
            <el-icon><HomeFilled /></el-icon>
            <span v-show="!isCollapse">返回前台</span>
          </router-link>
        </div>
      </el-aside>

      <!-- 右侧内容区域 -->
      <el-container>
        <el-header class="admin-topbar">
          <div class="topbar-left">
            <el-breadcrumb separator="/">
              <el-breadcrumb-item :to="{ path: '/admin/dashboard' }">管理后台</el-breadcrumb-item>
              <el-breadcrumb-item v-if="pageTitle">{{ pageTitle }}</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          <div class="topbar-right">
            <router-link to="/" class="back-home-btn">
              <el-icon><HomeFilled /></el-icon>
              <span>返回前台</span>
            </router-link>
            <span class="admin-user">{{ authStore.user?.username }}</span>
            <el-button text @click="authStore.logout()">退出</el-button>
          </div>
        </el-header>

        <el-main class="admin-main">
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

const activeMenu = computed(() => {
  const path = route.path
  if (path.startsWith('/admin/pets')) return '/admin/pets'
  if (path.startsWith('/admin/adoptions')) return '/admin/adoptions'
  if (path.startsWith('/admin/users')) return '/admin/users'
  if (path.startsWith('/admin/articles')) return '/admin/articles'
  if (path.startsWith('/admin/community')) return '/admin/community'
  if (path.startsWith('/admin/reports')) return '/admin/reports'
  if (path.startsWith('/admin/certs')) return '/admin/certs'
  return '/admin/dashboard'
})

const pageTitle = computed(() => {
  return route.meta?.title || ''
})

function collapseSidebar() {
  isCollapse.value = !isCollapse.value
}

onMounted(() => {
  if (!authStore.isAdmin) {
    ElMessage.warning('您没有管理员权限')
    router.push('/')
  }
})
</script>

<style lang="scss" scoped>
.admin-layout {
  height: 100vh;
}

:deep(.el-container) {
  height: 100%;
}

.admin-sidebar {
  background-color: #304156;
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
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  color: #fff;

  .sidebar-title {
    display: flex;
    align-items: center;
    font-size: 16px;
    font-weight: bold;
    white-space: nowrap;

    .sidebar-icon {
      margin-right: 8px;
      font-size: 18px;
    }
  }

  .collapse-icon {
    font-size: 18px;
    color: #bfcbd9;
  }
}

.sidebar-menu {
  flex: 1;
  border-right: none;
}

.sidebar-footer {
  padding: 12px 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
}

.back-home-link {
  display: flex;
  align-items: center;
  color: #bfcbd9;
  font-size: 13px;
  transition: color 0.2s;

  .el-icon {
    margin-right: 6px;
  }

  &:hover {
    color: #409eff;
  }
}

.admin-topbar {
  background: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  height: 50px;
}

.topbar-left {
  :deep(.el-breadcrumb) {
    font-size: 14px;
  }
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.back-home-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  color: var(--color-primary);
  font-size: 13px;
  text-decoration: none;
  padding: 4px 10px;
  border-radius: 6px;
  transition: background 0.2s;
  &:hover { background: var(--el-color-primary-light-9); }
}

.admin-user {
  font-size: 13px;
  color: #666;
}

.admin-main {
  background: #f0f2f5;
  padding: 20px;
  min-height: calc(100vh - 50px);
}

@media (max-width: 768px) {
  .admin-sidebar {
    position: absolute;
    z-index: 100;
    height: 100%;
  }
}
</style>
