<template>
  <div class="publish-page">
    <AppHeader />
    <div class="container">
      <div class="page-header">
        <h1 class="page-title">{{ isEdit ? '编辑宠物信息' : '发布领养信息' }}</h1>
        <p class="page-subtitle">填写宠物的详细信息，帮助它找到新家</p>
      </div>

      <div class="form-card">
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="100px"
          v-loading="pageLoading"
        >
          <el-form-item label="名称" prop="name">
            <el-input v-model="form.name" placeholder="请输入宠物名称" maxlength="50" show-word-limit />
          </el-form-item>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="分类" prop="categoryId">
                <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
                  <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="品种" prop="breed">
                <el-input v-model="form.breed" placeholder="请输入品种" maxlength="50" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="年龄" prop="age">
                <el-input v-model="form.age" placeholder='例如："3个月"、"2岁"' maxlength="20" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="性别" prop="gender">
                <el-select v-model="form.gender" placeholder="请选择性别" style="width: 100%">
                  <el-option label="公" value="MALE" />
                  <el-option label="母" value="FEMALE" />
                  <el-option label="未知" value="UNKNOWN" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="体型" prop="size">
                <el-select v-model="form.size" placeholder="请选择体型" style="width: 100%">
                  <el-option label="小型" value="SMALL" />
                  <el-option label="中型" value="MEDIUM" />
                  <el-option label="大型" value="LARGE" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="毛色" prop="color">
                <el-input v-model="form.color" placeholder="请输入毛色" maxlength="30" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="健康状况">
                <el-input v-model="form.healthStatus" placeholder="如：健康，已驱虫" maxlength="100" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="疫苗情况">
                <el-input v-model="form.vaccination" placeholder="如：已完成全部疫苗" maxlength="100" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="是否绝育">
            <el-radio-group v-model="form.sterilization">
              <el-radio :value="1">是</el-radio>
              <el-radio :value="0">否</el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="详细描述" prop="description">
            <el-input
              v-model="form.description"
              type="textarea"
              :rows="5"
              placeholder="请详细描述宠物的性格、习惯、健康状况、领养要求等"
              maxlength="2000"
              show-word-limit
            />
          </el-form-item>

          <el-form-item label="封面图片" prop="coverImage">
            <el-input
              v-model="form.coverImage"
              placeholder="粘贴图片URL地址，或使用下方上传"
              clearable
            />
            <div class="cover-tip">可粘贴网络图片链接，或使用下方上传功能</div>
          </el-form-item>

          <el-form-item label="上传图片">
            <el-upload
              class="cover-upload"
              action="/api/files/upload"
              :headers="uploadHeaders"
              :show-file-list="false"
              :on-success="onCoverUpload"
              :on-error="onUploadError"
              :before-upload="beforeUpload"
              accept="image/*"
            >
              <el-button type="primary" plain>
                <el-icon><Upload /></el-icon>
                <span>上传图片</span>
              </el-button>
            </el-upload>
          </el-form-item>

          <el-form-item v-if="form.coverImage" label="预览">
            <el-image
              :src="form.coverImage"
              fit="cover"
              style="width: 200px; height: 150px; border-radius: 8px;"
              :preview-src-list="[form.coverImage]"
              preview-teleported
            />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" :loading="submitting" @click="handleSubmit" size="large">
              {{ isEdit ? '保存修改' : '发布领养信息' }}
            </el-button>
            <el-button @click="goBack" size="large">取消</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Upload } from '@element-plus/icons-vue'
import AppHeader from '@/components/layout/AppHeader.vue'
import { getPetDetail, createPet, updatePet } from '@/api/pet'
import request from '@/utils/request'

const router = useRouter()
const route = useRoute()
const isEdit = computed(() => !!route.params.id)

const formRef = ref(null)
const pageLoading = ref(false)
const submitting = ref(false)
const categories = ref([])

const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${localStorage.getItem('token') || ''}`
}))

const form = reactive({
  name: '',
  categoryId: null,
  breed: '',
  age: '',
  gender: '',
  size: '',
  color: '',
  healthStatus: '',
  vaccination: '',
  sterilization: 0,
  description: '',
  coverImage: ''
})

const rules = {
  name: [{ required: true, message: '请输入宠物名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  description: [{ required: true, message: '请输入描述信息', trigger: 'blur' }]
}

function onCoverUpload(response) {
  if (response.code === 200 && response.data) {
    form.coverImage = response.data
    ElMessage.success('上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

function onUploadError() {
  ElMessage.error('图片上传失败，请重试')
}

function beforeUpload(file) {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isImage) { ElMessage.error('只能上传图片文件！'); return false }
  if (!isLt5M) { ElMessage.error('图片大小不能超过 5MB！'); return false }
  return true
}

async function fetchCategories() {
  try {
    const res = await request.get('/api/categories')
    categories.value = res.data ?? []
  } catch (e) {
    console.error('获取分类失败:', e)
  }
}

async function fetchPetDetail() {
  if (!isEdit.value) return
  pageLoading.value = true
  try {
    const res = await getPetDetail(route.params.id)
    const pet = res.data
    if (pet) {
      form.name = pet.name ?? ''
      form.categoryId = pet.categoryId ?? null
      form.breed = pet.breed ?? ''
      form.age = pet.age ?? ''
      form.gender = pet.gender ?? ''
      form.size = pet.size ?? ''
      form.color = pet.color ?? ''
      form.healthStatus = pet.healthStatus ?? ''
      form.vaccination = pet.vaccination ?? ''
      form.sterilization = pet.sterilization ?? 0
      form.description = pet.description ?? ''
      form.coverImage = pet.coverImage ?? ''
    }
  } catch (e) {
    ElMessage.error('获取宠物信息失败')
    router.push('/pets/my')
  } finally {
    pageLoading.value = false
  }
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const data = {
      name: form.name,
      categoryId: form.categoryId,
      breed: form.breed,
      age: form.age,
      gender: form.gender,
      size: form.size,
      color: form.color,
      healthStatus: form.healthStatus,
      vaccination: form.vaccination,
      sterilization: form.sterilization,
      description: form.description,
      coverImage: form.coverImage
    }

    if (isEdit.value) {
      await updatePet(route.params.id, data)
      ElMessage.success('宠物信息已更新')
    } else {
      await createPet(data)
      ElMessage.success('发布成功！')
    }
    router.push('/pets/my')
  } catch (e) {
    console.error('提交失败:', e)
  } finally {
    submitting.value = false
  }
}

function goBack() {
  router.back()
}

onMounted(() => {
  fetchCategories()
  fetchPetDetail()
})
</script>

<style lang="scss" scoped>
.publish-page {
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

.page-subtitle {
  font-size: 14px;
  color: #999;
  margin-top: 8px;
}

.form-card {
  background: #fff;
  border-radius: 12px;
  padding: 32px 40px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.cover-tip {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

@media (max-width: 768px) {
  .form-card {
    padding: 20px 16px;
  }
}
</style>
