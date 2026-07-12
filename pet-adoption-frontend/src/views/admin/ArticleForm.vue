<template>
  <div class="admin-article-form">
    <h2>{{ isEdit ? '编辑文章' : '添加文章' }}</h2>
    <el-form :model="form" label-width="80px" style="max-width:800px;margin-top:20px">
      <el-form-item label="标题">
        <el-input v-model="form.title" placeholder="文章标题" />
      </el-form-item>
      <el-form-item label="分类">
        <el-select v-model="form.category">
          <el-option label="猫咪" value="CAT" />
          <el-option label="狗狗" value="DOG" />
          <el-option label="综合" value="GENERAL" />
          <el-option label="Checklist" value="CHECKLIST" />
        </el-select>
      </el-form-item>
      <el-form-item label="摘要">
        <el-input v-model="form.summary" type="textarea" :rows="2" placeholder="文章摘要" />
      </el-form-item>
      <el-form-item label="内容">
        <div class="editor-wrapper" ref="editorRef"></div>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSave">保存</el-button>
        <el-button @click="$router.back()">取消</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createArticle, updateArticle, getArticleDetail } from '@/api/article'
import Quill from 'quill'
import 'quill/dist/quill.snow.css'

const route = useRoute()
const router = useRouter()
const isEdit = computed(() => !!route.params.id)
const form = ref({ title: '', category: 'GENERAL', summary: '', content: '' })
const editorRef = ref(null)
let quill = null

onMounted(() => {
  quill = new Quill(editorRef.value, {
    theme: 'snow',
    placeholder: '在这里编写文章内容...',
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

  if (isEdit.value) {
    loadArticle()
  }
})

onBeforeUnmount(() => {
  quill = null
})

async function loadArticle() {
  try {
    const res = await getArticleDetail(route.params.id)
    form.value = res.data
    if (quill && res.data.content) {
      quill.root.innerHTML = res.data.content
    }
  } catch {}
}

async function handleSave() {
  if (quill) form.value.content = quill.root.innerHTML
  try {
    if (isEdit.value) { await updateArticle(route.params.id, form.value) }
    else { await createArticle(form.value) }
    ElMessage.success('保存成功')
    router.push('/admin/articles')
  } catch { ElMessage.error('保存失败') }
}
</script>

<style lang="scss" scoped>
.editor-wrapper { width: 100%; min-height: 400px; border-radius: 8px; }
:deep(.ql-editor) { min-height: 350px; font-size: 15px; }
:deep(.ql-toolbar) { border-radius: 8px 8px 0 0; }
:deep(.ql-container) { border-radius: 0 0 8px 8px; }
</style>
