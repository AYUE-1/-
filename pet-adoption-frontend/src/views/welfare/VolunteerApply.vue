<template>
  <div class="volunteer-apply-page">
    <AppHeader />
    <div class="container">
      <h1 class="page-title" style="padding:40px 0 24px">🤝 申请成为志愿者</h1>
      <el-form :model="form" label-width="100px" style="max-width:500px">
        <el-form-item label="技能特长"><el-input v-model="form.skills" placeholder="如: 医疗护理、驾驶、摄影、宣传等" /></el-form-item>
        <el-form-item label="可服务时间"><el-input v-model="form.availableTime" placeholder="如: 周末、工作日晚上" /></el-form-item>
        <el-form-item label="所在区域"><el-input v-model="form.region" placeholder="如: 北京市朝阳区" /></el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">提交申请</el-button>
        </el-form-item>
      </el-form>
    </div>
    <AppFooter />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { applyVolunteer } from '@/api/trust'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'

const router = useRouter()
const submitting = ref(false)
const form = ref({ skills: '', availableTime: '', region: '' })

async function handleSubmit() {
  submitting.value = true
  try { await applyVolunteer(form.value); ElMessage.success('申请成功'); router.push('/welfare') }
  catch {} finally { submitting.value = false }
}
</script>

<style lang="scss" scoped>
.volunteer-apply-page { min-height: 100vh; background: var(--color-bg); }
</style>
