<template>
  <div class="login-page">
    <!-- 左侧品牌区 -->
    <div class="brand-panel">
      <div class="brand-content">
        <router-link to="/" class="brand-logo">🐾 爪印</router-link>
        <h1>欢迎回来</h1>
        <p class="brand-desc">每一只宠物都值得被温柔以待<br>登录继续你的领养之旅</p>
        <div class="brand-features">
          <div class="bf-item">🔍 发现待领养宠物</div>
          <div class="bf-item">🎯 智能养宠测评</div>
          <div class="bf-item">🆘 参与流浪救助</div>
        </div>
      </div>
      <div class="brand-paws">
        <span v-for="i in 12" :key="i" class="paw" :style="pawStyle(i)">🐾</span>
      </div>
    </div>

    <!-- 右侧表单区 -->
    <div class="form-panel">
      <div class="form-wrapper">
        <div class="form-header">
          <h2>登录</h2>
          <p>还没有账号？<router-link to="/register">立即注册</router-link></p>
        </div>

        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="login-form" @keyup.enter="handleLogin">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="form.username" placeholder="请输入用户名" :prefix-icon="User" size="large" />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password :prefix-icon="Lock" size="large" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" size="large" :loading="loading" class="submit-btn" @click="handleLogin">
              {{ loading ? '登录中...' : '登 录' }}
            </el-button>
          </el-form-item>
        </el-form>

        <div class="demo-accounts">
          <p class="demo-title">🔑 体验账号</p>
          <div class="demo-list">
            <span class="demo-tag" @click="quickLogin('admin','admin123')">管理员</span>
            <span class="demo-tag" @click="quickLogin('zhangsan','123456')">普通用户</span>
            <span class="demo-tag" @click="quickLogin('shelter','shelter123')">救助机构</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 登录成功动画 -->
    <LoginSuccessOverlay
      :visible="showOverlay"
      :nickname="loginNickname"
      :role="loginRole"
      @done="onOverlayDone"
    />
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import LoginSuccessOverlay from '@/components/common/LoginSuccessOverlay.vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const formRef = ref(null)
const loading = ref(false)
const showOverlay = ref(false)
const loginNickname = ref('')
const loginRole = ref('USER')

const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

function pawStyle(i) {
  return {
    left: `${(i * 17) % 100}%`,
    top: `${10 + (i * 23) % 70}%`,
    opacity: 0.06 + (i % 5) * 0.02,
    fontSize: `${20 + (i % 4) * 10}px`,
    transform: `rotate(${(i * 37) % 360}deg)`,
    animationDelay: `${i * 0.5}s`
  }
}

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const res = await authStore.login({ username: form.username.trim(), password: form.password })
    loginNickname.value = res.user?.nickname || form.username.trim()
    loginRole.value = res.user?.role || 'USER'
    showOverlay.value = true
  } catch { ElMessage.error('登录失败，请检查用户名和密码') }
  finally { loading.value = false }
}

function onOverlayDone() {
  showOverlay.value = false
  let redirect = '/'
  if (loginRole.value === 'ADMIN') {
    redirect = '/admin/dashboard'
  } else if (loginRole.value === 'SHELTER') {
    redirect = '/shelter'
  } else {
    redirect = route.query.redirect || '/'
  }
  router.push(redirect)
}

function quickLogin(u, p) {
  form.username = u; form.password = p
  handleLogin()
}
</script>

<style lang="scss" scoped>
.login-page {
  display: flex; min-height: 100vh;
  @media (max-width: 768px) { flex-direction: column; }
}

// === 左侧品牌区 ===
.brand-panel {
  flex: 1; background: linear-gradient(160deg, #FF7F50 0%, #FF6347 30%, #FFA07A 70%, #FFB899 100%);
  display: flex; align-items: center; justify-content: center; position: relative; overflow: hidden;
  padding: 60px 40px;
  @media (max-width: 768px) { padding: 40px 20px; min-height: 40vh; }
}
.brand-content {
  text-align: center; position: relative; z-index: 1; color: #fff;
  .brand-logo { font-size: 28px; font-weight: 800; text-decoration: none; color: #fff; display: block; margin-bottom: 32px; }
  h1 { font-size: 38px; font-weight: 800; margin-bottom: 16px; text-shadow: 0 2px 8px rgba(0,0,0,0.1); }
  .brand-desc { font-size: 17px; opacity: 0.9; line-height: 1.8; margin-bottom: 40px; }
}
.brand-features {
  text-align: left; display: inline-block;
  .bf-item { font-size: 15px; padding: 8px 0; opacity: 0.85; }
}
.brand-paws {
  position: absolute; inset: 0; pointer-events: none;
  .paw { position: absolute; animation: floatPaw 6s ease-in-out infinite; }
}
@keyframes floatPaw {
  0%, 100% { transform: translateY(0) rotate(var(--r,0deg)); }
  50% { transform: translateY(-12px) rotate(var(--r,0deg)); }
}

// === 右侧表单区 ===
.form-panel {
  width: 500px; display: flex; align-items: center; justify-content: center; padding: 40px;
  background: var(--color-bg);
  @media (max-width: 768px) { width: 100%; padding: 30px 20px; }
}
.form-wrapper { width: 100%; max-width: 380px; }
.form-header {
  text-align: center; margin-bottom: 32px;
  h2 { font-size: 28px; font-weight: 700; color: var(--color-text); margin-bottom: 8px; }
  p { font-size: 14px; color: var(--color-text-secondary);
    a { color: var(--color-primary); font-weight: 500; &:hover { text-decoration: underline; } }
  }
}
.login-form {
  :deep(.el-form-item__label) { font-weight: 500; }
  :deep(.el-input__wrapper) { border-radius: 10px; box-shadow: 0 1px 3px rgba(0,0,0,0.06); }
}
.submit-btn {
  width: 100%; height: 48px; font-size: 17px; border-radius: 12px; font-weight: 600;
  letter-spacing: 4px;
}

.demo-accounts {
  margin-top: 32px; text-align: center; border-top: 1px solid var(--color-border); padding-top: 20px;
  .demo-title { font-size: 13px; color: var(--color-text-secondary); margin-bottom: 10px; }
  .demo-list { display: flex; gap: 8px; justify-content: center; flex-wrap: wrap; }
  .demo-tag {
    padding: 5px 14px; background: #fff; border: 1px solid var(--color-border); border-radius: 20px;
    font-size: 12px; color: var(--color-text-secondary); cursor: pointer; transition: all 0.2s;
    &:hover { border-color: var(--color-primary); color: var(--color-primary); background: var(--el-color-primary-light-9); }
  }
}
</style>
