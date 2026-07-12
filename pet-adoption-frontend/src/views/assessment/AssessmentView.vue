<template>
  <div class="assessment-page">
    <AppHeader />
    <div class="container">
      <div class="page-header">
        <h1 class="page-title">🐾 养宠测评</h1>
        <p class="page-subtitle">回答几个简单问题，智能推荐最适合你的宠物伙伴</p>
      </div>

      <!-- 选择宠物类别 -->
      <div v-if="step === 0" class="category-select">
        <h3>你想了解哪类宠物？</h3>
        <el-row :gutter="16" justify="center">
          <el-col :span="6" v-for="cat in categories" :key="cat.value">
            <div class="category-card card-hover" :class="{ active: selectedCategory === cat.value }"
                 @click="selectCategory(cat.value)">
              <span class="category-icon">{{ cat.icon }}</span>
              <span class="category-name">{{ cat.label }}</span>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 答题 -->
      <div v-if="step === 1" class="question-section">
        <el-progress :percentage="progress" :stroke-width="8" :color="'#FF7F50'" />
        <div class="question-card card-hover" v-if="currentQuestion">
          <div class="q-num">第 {{ currentIndex + 1 }} / {{ questions.length }} 题</div>
          <h3 class="q-text">{{ currentQuestion.questionText }}</h3>
          <el-radio-group v-model="answers[currentQuestion.id]" class="q-options" @change="autoNext">
            <div v-for="(opt, idx) in parseOptions(currentQuestion.options)" :key="idx"
                 class="q-option" :class="{ selected: answers[currentQuestion.id] === idx }">
              <el-radio :value="idx">{{ opt.label }}</el-radio>
            </div>
          </el-radio-group>
          <div class="q-actions">
            <el-button v-if="currentIndex > 0" @click="prevQuestion">上一题</el-button>
            <el-button type="primary" @click="nextQuestion" :disabled="answers[currentQuestion.id] === undefined">
              {{ currentIndex < questions.length - 1 ? '下一题' : '查看结果' }}
            </el-button>
          </div>
        </div>
      </div>

      <!-- 加载结果 -->
      <div v-if="step === 2" class="loading-section">
        <el-icon class="loading-icon" :size="48"><Loading /></el-icon>
        <p>正在为你分析最佳宠物伙伴...</p>
      </div>
    </div>
    <AppFooter />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { getQuestions, submitAssessment } from '@/api/assessment'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'

const router = useRouter()
const step = ref(0)
const selectedCategory = ref('ALL')
const categories = [
  { value: 'ALL', label: '都可以', icon: '🐾' },
  { value: 'CAT', label: '猫咪', icon: '🐱' },
  { value: 'DOG', label: '狗狗', icon: '🐶' },
  { value: 'OTHER', label: '其他', icon: '🐹' }
]

const questions = ref([])
const currentIndex = ref(0)
const answers = ref({})
const submitting = ref(false)

const currentQuestion = computed(() => questions.value[currentIndex.value] || null)
const progress = computed(() => questions.value.length > 0
  ? Math.round(((currentIndex.value + 1) / questions.value.length) * 100)
  : 0)

function parseOptions(options) {
  try { return JSON.parse(options) } catch { return [] }
}

function selectCategory(cat) {
  selectedCategory.value = cat
  loadQuestions()
}

async function loadQuestions() {
  try {
    const res = await getQuestions(selectedCategory.value)
    questions.value = res.data || []
    if (questions.value.length > 0) {
      step.value = 1
    } else {
      ElMessage.warning('暂无测评题目')
    }
  } catch {
    ElMessage.error('加载题目失败')
  }
}

function nextQuestion() {
  if (currentIndex.value < questions.value.length - 1) {
    currentIndex.value++
  } else {
    submit()
  }
}

function prevQuestion() {
  if (currentIndex.value > 0) currentIndex.value--
}

function autoNext() {
  setTimeout(() => nextQuestion(), 300)
}

async function submit() {
  if (submitting.value) return
  submitting.value = true
  step.value = 2
  try {
    const answerList = Object.entries(answers.value).map(([qId, idx]) => ({
      questionId: parseInt(qId),
      selectedIndex: idx
    }))
    const res = await submitAssessment({ petCategory: selectedCategory.value, answers: answerList })
    const result = res.data
    // 将结果存到 sessionStorage 并跳转
    sessionStorage.setItem('assessmentResult', JSON.stringify(result))
    router.push({ name: 'AssessmentResult', params: { id: result.id || 'latest' } })
  } catch {
    ElMessage.error('测评提交失败')
    step.value = 1
  } finally {
    submitting.value = false
  }
}
</script>

<style lang="scss" scoped>
.assessment-page { min-height: 100vh; background: var(--color-bg); }
.category-select {
  max-width: 600px; margin: 0 auto; text-align: center;
  h3 { margin-bottom: 24px; font-size: 18px; }
}
.category-card {
  padding: 24px 12px; text-align: center; background: #fff; border-radius: var(--border-radius);
  border: 2px solid transparent; cursor: pointer;
  .category-icon { font-size: 40px; display: block; margin-bottom: 8px; }
  .category-name { font-size: 16px; font-weight: 500; }
  &.active { border-color: var(--color-primary); background: var(--el-color-primary-light-9); }
}
.question-section {
  max-width: 680px; margin: 0 auto;
  .el-progress { margin-bottom: 32px; }
}
.question-card {
  background: #fff; padding: 32px; border-radius: var(--border-radius);
  box-shadow: var(--shadow-card);
}
.q-num { color: var(--color-text-secondary); margin-bottom: 16px; font-size: 14px; }
.q-text { font-size: 20px; margin-bottom: 24px; line-height: 1.5; }
.q-options { display: flex; flex-direction: column; gap: 12px; width: 100%; }
.q-option {
  padding: 14px 18px; border: 1px solid var(--color-border); border-radius: var(--border-radius-sm);
  transition: all 0.2s;
  &:hover { border-color: var(--color-primary); }
  &.selected { border-color: var(--color-primary); background: var(--el-color-primary-light-9); }
}
.q-actions { display: flex; justify-content: flex-end; gap: 12px; margin-top: 24px; }
.loading-section {
  text-align: center; padding: 80px 0;
  .loading-icon { color: var(--color-primary); animation: spin 1s linear infinite; margin-bottom: 16px; }
  p { color: var(--color-text-secondary); font-size: 16px; }
}
@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
</style>
