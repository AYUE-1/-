<template>
  <div class="shelter-cert">
    <h2>机构认证</h2>
    <p class="subtitle">认证后可展示机构主页，增加信任度，提高领养成功率</p>

    <!-- 已认证 -->
    <template v-if="certStatus === 'APPROVED'">
      <el-result icon="success" title="您的机构已通过认证" sub-title="认证标识将在机构主页展示">
        <template #extra>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="机构名称">{{ form.shelterName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="机构地址">{{ form.shelterAddress || '-' }}</el-descriptions-item>
          </el-descriptions>
          <el-button type="primary" style="margin-top:16px" @click="$router.push(`/shelter/${authStore.user?.id}`)">
            查看我的机构主页
          </el-button>
        </template>
      </el-result>
    </template>

    <!-- 审核中 -->
    <template v-else-if="certStatus === 'PENDING'">
      <el-result icon="warning" title="认证申请审核中" sub-title="请耐心等待管理员审核，通常需要1-3个工作日">
        <template #extra>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="机构名称">{{ form.shelterName }}</el-descriptions-item>
            <el-descriptions-item label="机构地址">{{ form.shelterAddress }}</el-descriptions-item>
          </el-descriptions>
        </template>
      </el-result>
    </template>

    <!-- 未认证 / 已拒绝 -->
    <template v-else>
      <el-alert
        v-if="certStatus === 'REJECTED'"
        title="您的认证申请未通过，请修改信息后重新提交"
        type="error"
        show-icon
        :closable="false"
        style="margin-bottom:20px"
      />

      <el-card shadow="never">
        <template #header>提交认证申请</template>
        <el-form :model="form" label-width="100px" style="max-width:500px">
          <el-form-item label="机构名称" required>
            <el-input v-model="form.shelterName" placeholder="如: 阳光宠物救助中心" />
          </el-form-item>
          <el-form-item label="机构地址" required>
            <el-input v-model="form.shelterAddress" placeholder="如: 北京市朝阳区某某路100号" />
          </el-form-item>
          <el-form-item label="联系人">
            <el-input v-model="form.contactPerson" placeholder="机构负责人姓名" />
          </el-form-item>
          <el-form-item label="联系电话" required>
            <el-input v-model="form.contactPhone" placeholder="联系电话" />
          </el-form-item>
          <el-form-item label="资质说明">
            <el-input v-model="form.licenseInfo" type="textarea" :rows="4" placeholder="请描述您的机构资质、营业许可等信息..." />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="submitting" @click="handleSubmit">提交认证申请</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { getShelterDashboard } from '@/api/shelter'
import { applyCert } from '@/api/cert'

const authStore = useAuthStore()
const certStatus = ref('UNCERTIFIED')
const submitting = ref(false)
const form = ref({
  shelterName: '',
  shelterAddress: '',
  contactPerson: '',
  contactPhone: '',
  licenseInfo: ''
})

async function fetchStatus() {
  try {
    const res = await getShelterDashboard()
    certStatus.value = res.data?.certStatus || 'UNCERTIFIED'
    form.value.shelterName = res.data?.shelterName || ''
    form.value.shelterAddress = res.data?.shelterAddress || ''
  } catch {}
}

async function handleSubmit() {
  if (!form.value.shelterName || !form.value.shelterAddress || !form.value.contactPhone) {
    ElMessage.warning('请填写机构名称、地址和联系电话')
    return
  }
  submitting.value = true
  try {
    await applyCert(form.value)
    ElMessage.success('认证申请已提交')
    certStatus.value = 'PENDING'
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

onMounted(fetchStatus)
</script>

<style lang="scss" scoped>
h2 { margin: 0 0 4px; font-size: 20px; }
.subtitle { color: #909399; margin-bottom: 20px; }
</style>
