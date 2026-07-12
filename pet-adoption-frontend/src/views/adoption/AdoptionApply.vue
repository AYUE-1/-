<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import AppHeader from '@/components/layout/AppHeader.vue'
import { getPetDetail } from '@/api/pet'
import { applyAdoption } from '@/api/adoption'

const route = useRoute()
const router = useRouter()

const petInfo = ref(null)
const loading = ref(false)
const submitting = ref(false)
const formRef = ref(null)

const housingOptions = [
  { label: '自有住房', value: '自有' },
  { label: '整租住房', value: '租房' },
  { label: '合租住房', value: '合租' }
]

const incomeOptions = [
  { label: '3000元以下', value: '3000以下' },
  { label: '3000-5000元', value: '3000-5000' },
  { label: '5000-8000元', value: '5000-8000' },
  { label: '8000-15000元', value: '8000-15000' },
  { label: '15000元以上', value: '15000以上' }
]

const applicationForm = reactive({
  realName: '',
  idCard: '',
  phone: '',
  address: '',
  housingType: '',
  hasExperience: '',
  familyAgreed: '',
  occupation: '',
  incomeRange: '',
  reason: ''
})

const formRules = {
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    {
      pattern: /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/,
      message: '请输入正确的身份证号',
      trigger: 'blur'
    }
  ],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    {
      pattern: /^1[3-9]\d{9}$/,
      message: '请输入正确的手机号',
      trigger: 'blur'
    }
  ],
  address: [{ required: true, message: '请输入居住地址', trigger: 'blur' }],
  housingType: [{ required: true, message: '请选择住房类型', trigger: 'change' }],
  hasExperience: [{ required: true, message: '请选择养宠经验', trigger: 'change' }],
  familyAgreed: [{ required: true, message: '请选择家人是否同意', trigger: 'change' }],
  occupation: [{ required: true, message: '请输入职业', trigger: 'blur' }],
  incomeRange: [{ required: true, message: '请选择收入范围', trigger: 'change' }],
  reason: [
    { required: true, message: '请输入领养理由', trigger: 'blur' },
    { min: 10, message: '领养理由不能少于10个字', trigger: 'blur' }
  ]
}

async function fetchPetInfo() {
  const petId = route.params.petId
  if (!petId) {
    ElMessage.error('缺少宠物信息')
    router.replace('/pets')
    return
  }

  loading.value = true
  try {
    const res = await getPetDetail(petId)
    petInfo.value = res.data
  } catch (error) {
    ElMessage.error('获取宠物信息失败')
    router.replace('/pets')
  } finally {
    loading.value = false
  }
}

async function handleSubmit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  submitting.value = true
  try {
    const data = {
      petId: Number(route.params.petId),
      ...applicationForm
    }
    await applyAdoption(data)
    ElMessage.success('领养申请已提交，请等待审核')
    router.push('/user/applications')
  } catch (error) {
    ElMessage.error('提交失败，请重试')
  } finally {
    submitting.value = false
  }
}

function goBack() {
  router.back()
}

onMounted(() => {
  fetchPetInfo()
})
</script>

<template>
  <div class="apply-page">
    <AppHeader />

    <div class="container" v-loading="loading">
      <div class="page-header">
        <h1 class="page-title">提交领养申请</h1>
        <p class="page-subtitle">请认真填写以下信息，我们将尽快审核您的申请</p>
      </div>

      <!-- 宠物信息卡片 -->
      <div class="pet-info-card" v-if="petInfo">
        <div class="pet-cover">
          <img
            v-if="petInfo.coverImage"
            :src="petInfo.coverImage"
            :alt="petInfo.name"
          />
          <div v-else class="no-image">暂无图片</div>
        </div>
        <div class="pet-detail">
          <h3 class="pet-name">{{ petInfo.name }}</h3>
          <p class="pet-breed">{{ petInfo.breed || '-' }}</p>
          <p class="pet-brief" v-if="petInfo.description">
            {{ petInfo.description.length > 80 ? petInfo.description.slice(0, 80) + '...' : petInfo.description }}
          </p>
        </div>
      </div>

      <div class="form-card">
        <el-form
          ref="formRef"
          :model="applicationForm"
          :rules="formRules"
          label-width="100px"
          size="default"
        >
          <div class="form-section">
            <h3 class="form-section-title">基本信息</h3>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="真实姓名" prop="realName">
                  <el-input v-model="applicationForm.realName" placeholder="请输入真实姓名" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="身份证号" prop="idCard">
                  <el-input v-model="applicationForm.idCard" placeholder="请输入身份证号" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="联系电话" prop="phone">
                  <el-input v-model="applicationForm.phone" placeholder="请输入联系电话" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="职业" prop="occupation">
                  <el-input v-model="applicationForm.occupation" placeholder="请输入职业" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="居住地址" prop="address">
              <el-input v-model="applicationForm.address" placeholder="请输入详细居住地址" />
            </el-form-item>
          </div>

          <div class="form-section">
            <h3 class="form-section-title">住房与经验</h3>
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="住房类型" prop="housingType">
                  <el-select v-model="applicationForm.housingType" placeholder="请选择" style="width: 100%">
                    <el-option
                      v-for="item in housingOptions"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="养宠经验" prop="hasExperience">
                  <el-radio-group v-model="applicationForm.hasExperience">
                    <el-radio value="有">有</el-radio>
                    <el-radio value="无">无</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="家人同意" prop="familyAgreed">
                  <el-radio-group v-model="applicationForm.familyAgreed">
                    <el-radio value="是">是</el-radio>
                    <el-radio value="否">否</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="收入范围" prop="incomeRange">
              <el-select v-model="applicationForm.incomeRange" placeholder="请选择收入范围" style="width: 300px">
                <el-option
                  v-for="item in incomeOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </div>

          <div class="form-section">
            <h3 class="form-section-title">领养理由</h3>
            <el-form-item label="领养理由" prop="reason">
              <el-input
                v-model="applicationForm.reason"
                type="textarea"
                :rows="5"
                placeholder="请详细说明您想领养这只宠物的原因，以及您将如何照顾它（至少10个字）"
                maxlength="500"
                show-word-limit
              />
            </el-form-item>
          </div>

          <div class="form-actions">
            <el-button @click="goBack">返回</el-button>
            <el-button type="primary" :loading="submitting" @click="handleSubmit">
              {{ submitting ? '提交中...' : '提交申请' }}
            </el-button>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.apply-page {
  min-height: 100vh;
  background: #f5f7fa;
}

.container {
  max-width: 900px;
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

.pet-info-card {
  display: flex;
  align-items: center;
  gap: 20px;
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

  .pet-cover {
    width: 120px;
    height: 90px;
    border-radius: 8px;
    overflow: hidden;
    flex-shrink: 0;
    background: #f5f7fa;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .no-image {
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #999;
      font-size: 13px;
    }
  }

  .pet-detail {
    flex: 1;
    min-width: 0;
  }

  .pet-name {
    font-size: 18px;
    font-weight: 600;
    color: #333;
    margin-bottom: 4px;
  }

  .pet-breed {
    font-size: 13px;
    color: #999;
    margin-bottom: 4px;
  }

  .pet-brief {
    font-size: 13px;
    color: #666;
    line-height: 1.5;
  }
}

.form-card {
  background: #fff;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.form-section {
  margin-bottom: 16px;
}

.form-section-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
  padding-left: 10px;
  border-left: 3px solid #409eff;
}

.form-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #ebeef5;
}

@media (max-width: 768px) {
  .form-card {
    padding: 16px;
  }

  .pet-info-card {
    flex-direction: column;
    align-items: flex-start;

    .pet-cover {
      width: 100%;
      height: 180px;
    }
  }

  :deep(.el-col) {
    margin-bottom: 0;
  }
}
</style>
