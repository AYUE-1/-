<template>
  <div class="article-submit-page">
    <AppHeader />
    <div class="container">
      <div class="page-header">
        <h1 class="page-title">✍️ 投递科普文章</h1>
        <p class="page-subtitle">分享你的养宠知识和经验，审核通过后将对所有人可见</p>
      </div>

      <el-form :model="form" label-width="80px" class="submit-form" @submit.prevent>
        <el-form-item label="标题" required>
          <el-input v-model="form.title" placeholder="请输入文章标题" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="分类" required>
          <el-select v-model="form.category">
            <el-option label="猫咪" value="CAT" />
            <el-option label="狗狗" value="DOG" />
            <el-option label="综合" value="GENERAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="摘要">
          <el-input v-model="form.summary" type="textarea" :rows="2" placeholder="简要描述文章内容（可选）" maxlength="300" show-word-limit />
        </el-form-item>
        <el-form-item label="内容" required>
          <div class="editor-wrapper" ref="editorRef"></div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" @click="handleSubmit" :loading="submitting">
            📤 提交审核
          </el-button>
          <el-button size="large" @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>

      <el-alert type="info" :closable="false" style="max-width:600px;margin-top:16px" title="投稿说明" description="文章提交后需要管理员审核，审核通过后才会在科普页面展示。请勿发布违规内容。" show-icon />
    </div>
    <AppFooter />
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createArticle } from '@/api/article'
import Quill from 'quill'
import 'quill/dist/quill.snow.css'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'

const router = useRouter()
const submitting = ref(false)
const form = ref({ title: '', category: 'CAT', summary: '', content: '' })
const editorRef = ref(null)
let quill = null

onMounted(() => {
  quill = new Quill(editorRef.value, {
    theme: 'snow',
    placeholder: '写下你的文章内容...',
    modules: {
      toolbar: [
        [{ header: [1, 2, 3, false] }],
        ['bold', 'italic', 'underline', 'strike'],
        [{ color: [] }, { background: [] }],
        [{ list: 'ordered' }, { list: 'bullet' }],
        [{ align: [] }],
        ['blockquote', 'code-block'],
        ['link', 'image'],
        ['clean']
      ]
    }
  })
})

onBeforeUnmount(() => {
  quill = null
})

async function handleSubmit() {
  if (quill) form.value.content = quill.root.innerHTML
  const text = form.value.content.replace(/<[^>]*>/g, '').trim()
  if (!form.value.title.trim()) return ElMessage.warning('请输入标题')
  if (!text) return ElMessage.warning('请输入内容')
  submitting.value = true
  try {
    await createArticle(form.value)
    ElMessage.success('投稿成功，请等待管理员审核')
    router.push('/education')
  } catch {
    ElMessage.error('投稿失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style lang="scss" scoped>
.article-submit-page { min-height: 100vh; background: var(--color-bg); }
.submit-form { max-width: 800px; margin: 0 auto; background: #fff; padding: 32px; border-radius: var(--border-radius); }
.editor-wrapper { width: 100%; min-height: 400px; border-radius: 8px; }
:deep(.ql-editor) { min-height: 350px; font-size: 15px; }
:deep(.ql-toolbar) { border-radius: 8px 8px 0 0; }
:deep(.ql-container) { border-radius: 0 0 8px 8px; }
</style>
