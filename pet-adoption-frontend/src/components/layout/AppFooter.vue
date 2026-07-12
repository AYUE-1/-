<template>
  <footer class="app-footer">
    <div class="footer-container">
      <el-divider />
      <div class="footer-content">
        <div class="footer-info">
          <p class="footer-title">
            <span class="footer-icon">🐾</span>
            <span>宠物领养平台</span>
          </p>
          <p class="footer-slogan">给流浪动物一个温暖的家</p>
          <p class="footer-contact">
            <el-icon class="contact-icon"><Message /></el-icon>
            <span>联系我们: petadoption@example.com</span>
          </p>
        </div>
        <div class="footer-links">
          <router-link to="/">首页</router-link>
          <router-link to="/pets">宠物列表</router-link>
          <router-link to="/login">登录</router-link>
          <router-link to="/register">注册</router-link>
        </div>
      </div>
      <div class="footer-copyright">
        <p>&copy; {{ currentYear }} 宠物领养平台 - 保留所有权利</p>
        <p class="footer-beian">致力于为流浪动物找到温暖的家</p>
      </div>
    </div>
  </footer>
</template>

<script setup>
const currentYear = new Date().getFullYear()
</script>

<style lang="scss" scoped>
.app-footer {
  background: #2c3e50;
  color: #ecf0f1;
  padding: 20px 0 0;
  margin-top: 60px;
}

.footer-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 16px;
}

:deep(.el-divider) {
  border-top-color: rgba(255, 255, 255, 0.1);
  margin: 0 0 24px;
}

.footer-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  flex-wrap: wrap;
  gap: 24px;
  margin-bottom: 20px;
}

.footer-info {
  .footer-title {
    font-size: 18px;
    font-weight: bold;
    margin-bottom: 8px;
    display: flex;
    align-items: center;

    .footer-icon {
      margin-right: 6px;
      font-size: 20px;
    }
  }

  .footer-slogan {
    font-size: 14px;
    color: #bdc3c7;
    margin-bottom: 8px;
  }

  .footer-contact {
    font-size: 13px;
    color: #95a5a6;
    display: flex;
    align-items: center;

    .contact-icon {
      margin-right: 6px;
    }
  }
}

.footer-links {
  display: flex;
  gap: 24px;

  a {
    color: #bdc3c7;
    font-size: 14px;
    transition: color 0.2s;

    &:hover {
      color: #409eff;
    }
  }
}

.footer-copyright {
  text-align: center;
  padding: 16px 0;
  border-top: 1px solid rgba(255, 255, 255, 0.08);

  p {
    font-size: 12px;
    color: #7f8c8d;
    margin: 4px 0;
  }

  .footer-beian {
    font-size: 11px;
  }
}

@media (max-width: 768px) {
  .footer-content {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }

  .footer-contact {
    justify-content: center;
  }
}
</style>
