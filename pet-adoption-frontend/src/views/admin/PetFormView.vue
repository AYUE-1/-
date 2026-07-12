<template>
  <div class="pet-form">
    <div class="page-header">
      <h2 class="page-title">{{ isEdit ? '编辑宠物' : '添加宠物' }}</h2>
      <el-button @click="router.push('/admin/pets')">
        <el-icon><ArrowLeft /></el-icon>
        <span>返回列表</span>
      </el-button>
    </div>

    <div class="form-container">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        label-position="right"
        size="large"
        v-loading="pageLoading"
      >
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入宠物名称" maxlength="50" show-word-limit />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="分类" prop="categoryId">
              <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
                <el-option
                  v-for="cat in categories"
                  :key="cat.id"
                  :label="cat.name"
                  :value="cat.id"
                />
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
            <el-form-item label="健康状况" prop="healthStatus">
              <el-input v-model="form.healthStatus" placeholder="请输入健康状况" maxlength="100" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="疫苗情况" prop="vaccination">
              <el-input v-model="form.vaccination" placeholder="请输入疫苗情况" maxlength="100" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="是否绝育" prop="sterilization">
          <el-radio-group v-model="form.sterilization">
            <el-radio :label="true">是</el-radio>
            <el-radio :label="false">否</el-radio>
            <el-radio :label="null">未知</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="详细描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="5"
            placeholder="请输入宠物的详细描述"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="封面图片">
          <el-upload
            class="cover-upload"
            action="/api/files/upload"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="onCoverUploadSuccess"
            :on-error="onCoverUploadError"
            :before-upload="beforeCoverUpload"
            accept="image/*"
          >
            <el-image
              v-if="form.coverImage"
              :src="form.coverImage"
              fit="cover"
              style="width: 150px; height: 150px; border-radius: 8px;"
              :preview-src-list="[form.coverImage]"
              preview-teleported
            />
            <el-icon v-else class="cover-upload-icon"><Plus /></el-icon>
          </el-upload>
          <div class="cover-tip">点击上传封面图片，支持 JPG/PNG/WebP，不超过 5MB</div>
        </el-form-item>

        <el-form-item v-if="isEdit" label="状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择状态" style="width: 200px">
            <el-option label="可领养" value="AVAILABLE" />
            <el-option label="已领养" value="ADOPTED" />
            <el-option label="已预留" value="RESERVED" />
            <el-option label="已下架" value="OFFLINE" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">
            {{ isEdit ? '保存修改' : '添加宠物' }}
          </el-button>
          <el-button @click="router.push('/admin/pets')">取消</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
// (computed already imported above)
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Plus } from '@element-plus/icons-vue'
import { getPetDetail, createPet, updatePet } from '@/api/pet'
import request from '@/utils/request'

const router = useRouter()
const route = useRoute()

// 上传请求头（携带 JWT Token）
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${localStorage.getItem('token') || ''}`
}))

function onCoverUploadSuccess(response) {
  if (response.code === 200 && response.data) {
    form.coverImage = response.data
    ElMessage.success('封面上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

function onCoverUploadError() {
  ElMessage.error('图片上传失败，请重试')
}

function beforeCoverUpload(file) {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isImage) {
    ElMessage.error('只能上传图片文件！')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB！')
    return false
  }
  return true
}

const isEdit = computed(() => !!route.params.id)
const formRef = ref(null)
const pageLoading = ref(false)
const submitting = ref(false)
const categories = ref([])

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
  sterilization: null,
  description: '',
  coverImage: '',
  status: ''
})

const rules = {
  name: [{ required: true, message: '请输入宠物名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }]
}

async function fetchCategories() {
  try {
    const res = await request.get('/api/categories')
    categories.value = res.data ?? []
  } catch (error) {
    console.error('获取分类列表失败:', error)
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
      form.sterilization = pet.sterilization ?? null
      form.description = pet.description ?? ''
      form.coverImage = pet.coverImage ?? ''
      form.status = pet.status ?? ''
    }
  } catch (error) {
    console.error('获取宠物详情失败:', error)
    ElMessage.error('获取宠物详情失败')
    router.push('/admin/pets')
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
      data.status = form.status
      await updatePet(route.params.id, data)
      ElMessage.success('宠物信息已更新')
    } else {
      await createPet(data)
      ElMessage.success('宠物添加成功')
    }

    router.push('/admin/pets')
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchCategories()
  fetchPetDetail()
})
</script>

<style lang="scss" scoped>
.pet-form {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;

  .page-title {
    font-size: 22px;
    font-weight: 600;
    color: #303133;
    margin: 0;
  }
}

.form-container {
  max-width: 860px;
  background: #fff;
  border-radius: 8px;
  padding: 32px 40px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.cover-preview {
  margin-top: 8px;
}

@media (max-width: 768px) {
  .pet-form {
    padding: 12px;
  }

  .form-container {
    padding: 20px 16px;
  }
}
</style>
