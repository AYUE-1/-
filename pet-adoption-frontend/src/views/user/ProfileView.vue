<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, UploadFilled } from '@element-plus/icons-vue'
import AppHeader from '@/components/layout/AppHeader.vue'
import { getProfile, updateProfile, updatePassword, updateAvatar } from '@/api/user'
import { uploadFile } from '@/api/upload'
import { useAuthStore } from '@/stores/auth'
import { getMyVolunteer, updateMyVolunteer } from '@/api/trust'

const authStore = useAuthStore()
const activeTab = ref('info')

const profileFormRef = ref(null)
const passwordFormRef = ref(null)
const uploading = ref(false)

// ---- 志愿者相关 ----
const volunteer = ref(null)
const volunteerSaving = ref(false)
const volunteerForm = reactive({
  skills: '',
  availableTime: '',
  region: '',
  welfareActivities: ''
})

const profileForm = reactive({
  nickname: '',
  email: '',
  phone: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const avatarUrl = ref('')

const profileRules = {
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
}

const validateConfirmPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请确认新密码'))
  } else if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [{ required: true, validator: validateConfirmPassword, trigger: 'blur' }]
}

async function fetchProfile() {
  try {
    const res = await getProfile()
    const data = res.data
    profileForm.nickname = data.nickname || ''
    profileForm.email = data.email || ''
    profileForm.phone = data.phone || ''
    avatarUrl.value = data.avatar || ''
  } catch (error) {
    ElMessage.error('获取用户信息失败')
  }
}

async function handleSaveProfile() {
  if (!profileFormRef.value) return
  try {
    await profileFormRef.value.validate()
  } catch {
    return
  }

  try {
    await updateProfile(profileForm)
    authStore.updateUser({ nickname: profileForm.nickname })
    ElMessage.success('保存成功')
  } catch (error) {
    ElMessage.error('保存失败，请重试')
  }
}

async function handleSavePassword() {
  if (!passwordFormRef.value) return
  try {
    await passwordFormRef.value.validate()
  } catch {
    return
  }

  try {
    await updatePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    ElMessage.success('密码修改成功')
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
    passwordFormRef.value.resetFields()
  } catch (error) {
    ElMessage.error('密码修改失败，请检查原密码是否正确')
  }
}

async function handleAvatarUpload(options) {
  const file = options.file
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过2MB')
    return
  }

  uploading.value = true
  try {
    const res = await uploadFile(file)
    const url = res.data
    await updateAvatar(url)
    avatarUrl.value = url
    ElMessage.success('头像更新成功')
  } catch (error) {
    ElMessage.error('头像上传失败')
  } finally {
    uploading.value = false
  }
}

function handleAvatarError() {
  avatarUrl.value = ''
}

// ---- 志愿者相关函数 ----
async function fetchVolunteer() {
  try {
    const res = await getMyVolunteer()
    volunteer.value = res.data
    if (res.data) {
      volunteerForm.skills = res.data.skills || ''
      volunteerForm.availableTime = res.data.availableTime || ''
      volunteerForm.region = res.data.region || ''
      volunteerForm.welfareActivities = res.data.welfareActivities || ''
    }
  } catch {}
}

async function handleSaveVolunteer() {
  volunteerSaving.value = true
  try {
    await updateMyVolunteer(volunteerForm)
    await fetchVolunteer()
    ElMessage.success('志愿者信息已更新')
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '更新失败')
  } finally {
    volunteerSaving.value = false
  }
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' })
}

onMounted(() => {
  fetchProfile()
  fetchVolunteer()
})
</script>

<template>
  <div class="profile-page">
    <AppHeader />

    <div class="container">
      <div class="page-header">
        <h1 class="page-title">个人资料</h1>
      </div>

      <el-card class="profile-card" shadow="never">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="基本信息" name="info">
            <div class="avatar-section">
              <el-avatar :size="80" :src="avatarUrl" class="profile-avatar">
                {{ (authStore.user?.username || 'U')[0].toUpperCase() }}
              </el-avatar>
              <el-upload
                class="avatar-upload"
                :show-file-list="false"
                :http-request="handleAvatarUpload"
                accept="image/*"
              >
                <el-button type="primary" :loading="uploading" :icon="Plus" size="small">
                  {{ uploading ? '上传中...' : '更换头像' }}
                </el-button>
              </el-upload>
            </div>

            <el-form
              ref="profileFormRef"
              :model="profileForm"
              :rules="profileRules"
              label-width="80px"
              class="profile-form"
            >
              <el-form-item label="用户名">
                <el-input :model-value="authStore.user?.username" disabled />
              </el-form-item>
              <el-form-item label="昵称" prop="nickname">
                <el-input v-model="profileForm.nickname" placeholder="请输入昵称" />
              </el-form-item>
              <el-form-item label="邮箱" prop="email">
                <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
              </el-form-item>
              <el-form-item label="手机号" prop="phone">
                <el-input v-model="profileForm.phone" placeholder="请输入手机号" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleSaveProfile">保存</el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="修改密码" name="password">
            <el-form
              ref="passwordFormRef"
              :model="passwordForm"
              :rules="passwordRules"
              label-width="100px"
              class="password-form"
            >
              <el-form-item label="原密码" prop="oldPassword">
                <el-input
                  v-model="passwordForm.oldPassword"
                  type="password"
                  placeholder="请输入原密码"
                  show-password
                />
              </el-form-item>
              <el-form-item label="新密码" prop="newPassword">
                <el-input
                  v-model="passwordForm.newPassword"
                  type="password"
                  placeholder="请输入新密码"
                  show-password
                />
              </el-form-item>
              <el-form-item label="确认新密码" prop="confirmPassword">
                <el-input
                  v-model="passwordForm.confirmPassword"
                  type="password"
                  placeholder="请再次输入新密码"
                  show-password
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleSavePassword">修改密码</el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="志愿者信息" name="volunteer">
            <!-- 非志愿者 -->
            <div v-if="!volunteer" class="volunteer-empty">
              <el-empty description="您还不是志愿者" :image-size="80">
                <el-button type="primary" @click="$router.push('/welfare/volunteer/apply')">申请成为志愿者</el-button>
              </el-empty>
            </div>

            <!-- 已认证志愿者 -->
            <template v-else-if="volunteer.status === 'ACTIVE'">
              <div class="volunteer-badge-row">
                <el-tag type="success" size="large" effect="dark">认证志愿者</el-tag>
                <span class="volunteer-stat">已救助 <strong>{{ volunteer.rescueCount || 0 }}</strong> 只动物</span>
                <span class="volunteer-stat" v-if="volunteer.approvedAt">🕐 {{ formatDate(volunteer.approvedAt) }} 加入</span>
                <el-button size="small" type="primary" @click="$router.push(`/welfare/volunteer/${volunteer.id}`)" style="margin-left:auto">
                  查看我的主页
                </el-button>
              </div>

              <el-form label-width="100px" class="volunteer-form">
                <el-form-item label="技能特长">
                  <el-input v-model="volunteerForm.skills" placeholder="如: 医疗护理、驾驶、摄影、宣传等" />
                </el-form-item>
                <el-form-item label="可服务时间">
                  <el-input v-model="volunteerForm.availableTime" placeholder="如: 周末、工作日晚上" />
                </el-form-item>
                <el-form-item label="所在区域">
                  <el-input v-model="volunteerForm.region" placeholder="如: 北京市朝阳区" />
                </el-form-item>
                <el-form-item label="公益事迹">
                  <el-input
                    v-model="volunteerForm.welfareActivities"
                    type="textarea"
                    :rows="4"
                    placeholder="分享你参与的公益活动、救助经历..."
                  />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" :loading="volunteerSaving" @click="handleSaveVolunteer">保存</el-button>
                </el-form-item>
              </el-form>
            </template>

            <!-- 已拒绝 -->
            <div v-else-if="volunteer.status === 'REJECTED'" class="volunteer-empty">
              <el-empty description="您的志愿者申请未通过" :image-size="80">
                <el-button type="primary" @click="$router.push('/welfare/volunteer/apply')">重新申请</el-button>
              </el-empty>
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.profile-page {
  min-height: 100vh;
  background: #f5f7fa;
}

.container {
  max-width: 800px;
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

.profile-card {
  :deep(.el-card__body) {
    padding: 24px 40px;
  }

  :deep(.el-tabs__header) {
    margin-bottom: 24px;
  }
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 32px;
  padding-bottom: 24px;
  border-bottom: 1px solid #ebeef5;

  .profile-avatar {
    background: #409eff;
    color: #fff;
    font-size: 32px;
    flex-shrink: 0;
  }
}

.profile-form,
.password-form,
.volunteer-form {
  max-width: 480px;
}

.volunteer-empty {
  padding: 40px 0;
}

.volunteer-badge-row {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
  flex-wrap: wrap;
}

.volunteer-stat {
  font-size: 14px;
  color: var(--color-text-secondary);
}

@media (max-width: 768px) {
  .profile-card {
    :deep(.el-card__body) {
      padding: 16px;
    }
  }

  .avatar-section {
    flex-direction: column;
    align-items: flex-start;
  }

  .profile-form,
  .password-form {
    max-width: 100%;
  }
}
</style>
