<template>
  <div class="post-publish-page">
    <AppHeader />
    <div class="container">
      <h1 class="page-title" style="padding:40px 0 24px">✍️ 发布帖子</h1>
      <el-form :model="form" label-width="80px" style="max-width:600px">
        <el-form-item label="类型" required>
          <el-radio-group v-model="form.type"><el-radio-button value="SHARING">🐾 领养晒图</el-radio-button><el-radio-button value="LOST_FOUND">🔍 失宠寻宠</el-radio-button></el-radio-group>
        </el-form-item>
        <el-form-item label="标题" required><el-input v-model="form.title" placeholder="给你的帖子取个标题" /></el-form-item>
        <el-form-item label="内容" required><el-input v-model="form.content" type="textarea" :rows="6" placeholder="分享你的故事..." /></el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">发布</el-button>
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
import { createCommunityPost } from '@/api/community'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'

const router = useRouter()
const submitting = ref(false)
const form = ref({ title: '', content: '', type: 'SHARING' })

async function handleSubmit() {
  if (!form.value.title || !form.value.content) return ElMessage.warning('请填写标题和内容')
  submitting.value = true
  try { await createCommunityPost(form.value); ElMessage.success('发布成功'); router.push('/community') }
  catch {} finally { submitting.value = false }
}
</script>

<style lang="scss" scoped>
.post-publish-page { min-height: 100vh; background: var(--color-bg); }
</style>
