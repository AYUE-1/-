<template>
  <div class="rescue-publish-page">
    <AppHeader />
    <div class="container">
      <h1 class="page-title" style="padding:40px 0 24px">📢 发布流浪动物求助</h1>
      <el-form :model="form" label-width="100px" style="max-width:600px">
        <el-form-item label="标题" required><el-input v-model="form.title" placeholder="简要描述求助内容" /></el-form-item>
        <el-form-item label="描述" required><el-input v-model="form.description" type="textarea" :rows="4" placeholder="详细描述动物状况、位置等" /></el-form-item>
        <el-form-item label="动物类型" required>
          <el-select v-model="form.animalType"><el-option label="猫咪" value="CAT" /><el-option label="狗狗" value="DOG" /><el-option label="其他" value="OTHER" /></el-select>
        </el-form-item>
        <el-form-item label="紧急程度">
          <el-radio-group v-model="form.emergencyLevel">
            <el-radio-button value="NORMAL">普通</el-radio-button>
            <el-radio-button value="URGENT">紧急</el-radio-button>
            <el-radio-button value="CRITICAL">危急</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="动物描述"><el-input v-model="form.animalDesc" placeholder="体型、颜色、特征等" /></el-form-item>
        <el-form-item label="地址描述" required><el-input v-model="form.addressDesc" placeholder="如: XX市XX区XX路XX号附近" /></el-form-item>
        <el-form-item label="GPS定位">
          <el-input v-model="latInput" placeholder="纬度" style="width:48%" />&nbsp;
          <el-input v-model="lngInput" placeholder="经度" style="width:48%" />
          <el-button size="small" @click="getGPS" style="margin-top:8px">📍 自动获取位置</el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">发布求助</el-button>
          <el-button @click="$router.back()">取消</el-button>
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
import { createRescue } from '@/api/rescue'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'

const router = useRouter()
const submitting = ref(false)
const form = ref({ title: '', description: '', animalType: 'CAT', emergencyLevel: 'NORMAL', animalDesc: '', addressDesc: '' })
const latInput = ref('')
const lngInput = ref('')

function getGPS() {
  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(pos => {
      latInput.value = pos.coords.latitude.toFixed(6)
      lngInput.value = pos.coords.longitude.toFixed(6)
      ElMessage.success('定位成功')
    }, () => ElMessage.error('定位失败'))
  }
}

async function handleSubmit() {
  if (!form.value.title || !form.value.description || !form.value.addressDesc) {
    return ElMessage.warning('请填写必填项')
  }
  submitting.value = true
  try {
    await createRescue({
      ...form.value,
      latitude: parseFloat(latInput.value) || 0,
      longitude: parseFloat(lngInput.value) || 0
    })
    ElMessage.success('发布成功')
    router.push('/rescue')
  } catch { /* error handled by interceptor */ }
  finally { submitting.value = false }
}
</script>

<style lang="scss" scoped>
.rescue-publish-page { min-height: 100vh; background: var(--color-bg); }
</style>
