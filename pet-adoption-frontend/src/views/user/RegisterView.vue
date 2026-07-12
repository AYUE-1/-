<template>
  <div class="register-page">
    <!-- 左侧品牌区 -->
    <div class="brand-panel">
      <div class="brand-content">
        <router-link to="/" class="brand-logo">🐾 爪印</router-link>
        <h1>加入我们</h1>
        <p class="brand-desc">注册账号，开启你的领养之旅<br>给流浪动物一个温暖的家</p>
        <div class="brand-steps">
          <div class="bs-item"><span class="bs-num">1</span> 创建账号</div>
          <div class="bs-item"><span class="bs-num">2</span> 浏览待领养宠物</div>
          <div class="bs-item"><span class="bs-num">3</span> 提交领养申请</div>
          <div class="bs-item"><span class="bs-num">4</span> 带它回家 🏠</div>
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
          <h2>注册</h2>
          <p>已有账号？<router-link to="/login">立即登录</router-link></p>
        </div>

        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="register-form" @keyup.enter="handleRegister">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="form.username" placeholder="3-50个字符" :prefix-icon="User" size="large" />
          </el-form-item>
          <el-row :gutter="12">
            <el-col :span="12">
              <el-form-item label="密码" prop="password">
                <el-input v-model="form.password" type="password" placeholder="至少6位" show-password :prefix-icon="Lock" size="large" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input v-model="form.confirmPassword" type="password" placeholder="再次输入" show-password :prefix-icon="Lock" size="large" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="昵称" prop="nickname">
            <el-input v-model="form.nickname" placeholder="你的昵称" :prefix-icon="User" size="large" />
          </el-form-item>
          <el-form-item label="身份" prop="role">
            <el-radio-group v-model="form.role" class="role-group">
              <el-radio-button value="USER">
                <span style="font-size:20px">👤</span> 普通用户
              </el-radio-button>
              <el-radio-button value="SHELTER">
                <span style="font-size:20px">🏠</span> 救助机构
              </el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" size="large" :loading="loading" class="submit-btn" @click="handleRegister">
              {{ loading ? '注册中...' : '注 册' }}
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '', password: '', confirmPassword: '', nickname: '', role: 'USER'
})

const validateConfirm = (rule, value, callback) => {
  if (!value) callback(new Error('请再次输入密码'))
  else if (value !== form.password) callback(new Error('两次密码不一致'))
  else callback()
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '3-50个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 100, message: '至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  role: [{ required: true, message: '请选择身份', trigger: 'change' }]
}

function pawStyle(i) {
  return {
    left: `${(i * 17 + 8) % 100}%`,
    top: `${15 + (i * 23) % 65}%`,
    opacity: 0.05 + (i % 5) * 0.02,
    fontSize: `${18 + (i % 4) * 12}px`,
    transform: `rotate(${(i * 53) % 360}deg)`,
    animationDelay: `${i * 0.7}s`
  }
}

async function handleRegister() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await authStore.register({
      username: form.username.trim(),
      password: form.password,
      nickname: form.nickname.trim(),
      role: form.role
    })
    ElMessage.success('注册成功')
    router.push('/')
  } catch { ElMessage.error('注册失败') }
  finally { loading.value = false }
}
</script>

<style lang="scss" scoped>
.register-page {
  display: flex; min-height: 100vh;
  @media (max-width: 768px) { flex-direction: column; }
}

// === 左侧品牌区 ===
.brand-panel {
  flex: 1; background: linear-gradient(160deg, #FF7F50 0%, #FF6347 30%, #FFA07A 70%, #FFB899 100%);
  display: flex; align-items: center; justify-content: center; position: relative; overflow: hidden;
  padding: 60px 40px;
  @media (max-width: 768px) { padding: 40px 20px; min-height: 30vh; }
}
.brand-content {
  text-align: center; position: relative; z-index: 1; color: #fff;
  .brand-logo { font-size: 28px; font-weight: 800; text-decoration: none; color: #fff; display: block; margin-bottom: 24px; }
  h1 { font-size: 36px; font-weight: 800; margin-bottom: 12px; text-shadow: 0 2px 8px rgba(0,0,0,0.1); }
  .brand-desc { font-size: 16px; opacity: 0.9; line-height: 1.7; margin-bottom: 32px; }
}
.brand-steps {
  text-align: left; display: inline-block;
  .bs-item { font-size: 15px; padding: 6px 0; opacity: 0.85; }
  .bs-num { display: inline-block; width: 22px; height: 22px; background: rgba(255,255,255,0.25);
    border-radius: 50%; text-align: center; line-height: 22px; font-size: 12px; margin-right: 8px; font-weight: 700; }
}
.brand-paws {
  position: absolute; inset: 0; pointer-events: none;
  .paw { position: absolute; animation: floatPaw 7s ease-in-out infinite; }
}
@keyframes floatPaw {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-14px); }
}

// === 右侧表单区 ===
.form-panel {
  width: 520px; display: flex; align-items: center; justify-content: center; padding: 40px;
  background: var(--color-bg);
  @media (max-width: 768px) { width: 100%; padding: 30px 20px; }
}
.form-wrapper { width: 100%; max-width: 420px; }
.form-header {
  text-align: center; margin-bottom: 28px;
  h2 { font-size: 28px; font-weight: 700; color: var(--color-text); margin-bottom: 6px; }
  p { font-size: 14px; color: var(--color-text-secondary);
    a { color: var(--color-primary); font-weight: 500; &:hover { text-decoration: underline; } }
  }
}
.register-form {
  :deep(.el-form-item__label) { font-weight: 500; }
  :deep(.el-input__wrapper) { border-radius: 10px; }
  :deep(.el-form-item) { margin-bottom: 16px; }
}
.role-group {
  width: 100%;
  :deep(.el-radio-button) { flex: 1; }
  :deep(.el-radio-button__inner) { width: 100%; text-align: center; border-radius: 10px !important; }
}
.submit-btn {
  width: 100%; height: 48px; font-size: 17px; border-radius: 12px; font-weight: 600;
  letter-spacing: 4px; margin-top: 4px;
}
</style>
