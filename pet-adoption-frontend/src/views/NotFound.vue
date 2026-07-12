<template>
  <div class="not-found-page">
    <div class="not-found-content">
      <el-result
        icon="warning"
        title="404"
        sub-title="抱歉，您访问的页面不存在"
      >
        <template #extra>
          <el-button type="primary" size="large" @click="goHome">
            <el-icon><HomeFilled /></el-icon>
            <span>返回首页</span>
          </el-button>
        </template>
      </el-result>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

function goHome() {
  router.push('/')
}
</script>

<style lang="scss" scoped>
.not-found-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
}

.not-found-content {
  text-align: center;

  :deep(.el-result__title) {
    font-size: 72px;
    font-weight: bold;
    color: #909399;
    margin-bottom: 8px;
  }

  :deep(.el-result__subtitle) {
    font-size: 16px;
    color: #999;
    margin-top: 12px;
  }

  :deep(.el-result__extra) {
    margin-top: 24px;
  }
}
</style>
