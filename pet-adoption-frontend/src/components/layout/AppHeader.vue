<template>
  <header class="app-header">
    <div class="header-container">
      <router-link to="/" class="logo">
        <span class="logo-icon">🐾</span>
        <span class="logo-text">爪印</span>
      </router-link>

      <!-- 管理员导航：简洁 -->
      <el-menu v-if="authStore.isAdmin" mode="horizontal" :default-active="currentRoute" :router="true" class="nav-menu">
        <el-menu-item index="/">首页</el-menu-item>
        <el-menu-item index="/pets">领养</el-menu-item>
      </el-menu>

      <!-- 普通用户导航：完整 -->
      <el-menu v-else mode="horizontal" :default-active="currentRoute" :router="true" class="nav-menu">
        <el-menu-item index="/">首页</el-menu-item>
        <el-menu-item index="/pets">领养</el-menu-item>
        <el-menu-item index="/assessment">测评</el-menu-item>
        <el-menu-item index="/rescue">救助</el-menu-item>
        <el-menu-item index="/education">科普</el-menu-item>
        <el-menu-item index="/community">社区</el-menu-item>
        <el-menu-item index="/welfare">公益</el-menu-item>
      </el-menu>

      <div class="user-area">
        <!-- 管理员：精简菜单 -->
        <template v-if="authStore.isAdmin">
          <span class="admin-badge">管理员</span>
          <el-button type="warning" size="small" @click="router.push('/admin/dashboard')">
            ⚙ 管理后台
          </el-button>
          <el-button text size="small" @click="authStore.logout()">退出</el-button>
        </template>
        <!-- 普通用户/机构：完整菜单 -->
        <template v-else-if="authStore.isLoggedIn">
          <el-dropdown trigger="click" @command="handleUserCommand">
            <span class="user-dropdown-trigger">
              <el-avatar :size="32" class="user-avatar">
                {{ (authStore.user?.nickname || authStore.user?.username || 'U')[0].toUpperCase() }}
              </el-avatar>
              <span class="username">{{ authStore.user?.nickname || authStore.user?.username }}</span>
              <el-icon class="arrow-icon"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile"><el-icon><User /></el-icon><span>个人资料</span></el-dropdown-item>
                <el-dropdown-item command="favorites"><el-icon><Star /></el-icon><span>我的收藏</span></el-dropdown-item>
                <el-dropdown-item command="applications"><el-icon><Document /></el-icon><span>我的申请</span></el-dropdown-item>
                <el-dropdown-item command="ownerReview"><el-icon><Bell /></el-icon><span>收到的申请</span></el-dropdown-item>
                <el-dropdown-item command="myPets"><el-icon><FolderOpened /></el-icon><span>我发布的宠物</span></el-dropdown-item>
                <el-dropdown-item command="publish"><el-icon><EditPen /></el-icon><span>发布领养</span></el-dropdown-item>
                <el-dropdown-item command="myRescues"><el-icon><Location /></el-icon><span>我的救助</span></el-dropdown-item>
                <el-dropdown-item command="health"><el-icon><FirstAidKit /></el-icon><span>健康档案</span></el-dropdown-item>
                <el-dropdown-item v-if="authStore.isShelter" command="shelter" divided><el-icon><Setting /></el-icon><span>机构工作台</span></el-dropdown-item>
                <el-dropdown-item command="logout" divided><el-icon><SwitchButton /></el-icon><span>退出登录</span></el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button class="btn-login" @click="router.push('/login')">登录</el-button>
          <el-button type="primary" class="btn-register" @click="router.push('/register')">注册</el-button>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const currentRoute = computed(() => {
  const path = route.path
  if (path.startsWith('/pets')) return '/pets'
  if (path.startsWith('/assessment')) return '/assessment'
  if (path.startsWith('/rescue')) return '/rescue'
  if (path.startsWith('/education')) return '/education'
  if (path.startsWith('/community')) return '/community'
  if (path.startsWith('/welfare')) return '/welfare'
  return '/'
})

function handleUserCommand(command) {
  const commandMap = {
    profile: '/user/profile',
    favorites: '/user/favorites',
    applications: '/user/applications',
    ownerReview: '/user/reviews',
    myPets: '/pets/my',
    publish: '/pets/publish',
    myRescues: '/rescue/my',
    health: '/health',
    admin: '/admin/dashboard',
    shelter: '/shelter'
  }
  if (command === 'logout') {
    authStore.logout()
    ElMessage.success('已退出登录')
  } else if (commandMap[command]) {
    router.push(commandMap[command])
  }
}
</script>

<style lang="scss" scoped>
.app-header {
  position: sticky; top: 0; z-index: 1000;
  background: #fff; border-bottom: 1px solid var(--color-border);
  box-shadow: var(--shadow-card);
}
.header-container {
  max-width: 1200px; margin: 0 auto; padding: 0 20px;
  display: flex; align-items: center; height: 60px;
}
.logo {
  display: flex; align-items: center; text-decoration: none; color: var(--color-primary);
  font-size: 20px; font-weight: 700; flex-shrink: 0; margin-right: 24px;
  .logo-icon { font-size: 24px; margin-right: 6px; }
  .logo-text { white-space: nowrap; }
}
.nav-menu {
  flex: 1; border-bottom: none !important;
  :deep(.el-menu-item) { font-size: 15px; }
  :deep(.el-menu-item.is-active) { color: var(--color-primary); border-bottom-color: var(--color-primary); }
}
.user-area { display: flex; align-items: center; flex-shrink: 0; margin-left: 16px; }
.user-dropdown-trigger {
  display: flex; align-items: center; cursor: pointer; padding: 4px 8px;
  border-radius: 6px; transition: background 0.2s;
  &:hover { background: var(--color-bg); }
  .user-avatar { margin-right: 8px; background: var(--color-primary); color: #fff; font-size: 14px; }
  .username { font-size: 14px; color: var(--color-text); max-width: 120px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
  .arrow-icon { margin-left: 4px; font-size: 12px; color: #999; }
}
:deep(.el-dropdown-menu__item) { .el-icon { margin-right: 8px; } }
.btn-login { margin-right: 8px; }
.admin-badge {
  font-size: 12px; background: #f56c6c; color: #fff; padding: 2px 10px;
  border-radius: 20px; margin-right: 10px; font-weight: 500;
}

@media (max-width: 768px) {
  .logo-text { display: none; }
  .username { display: none; }
  .nav-menu { :deep(.el-menu-item) { padding: 0 10px; font-size: 13px; } }
}
</style>
