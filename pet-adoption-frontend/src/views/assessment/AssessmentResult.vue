<template>
  <div class="result-page">
    <AppHeader />
    <div class="container" v-if="result">
      <div class="page-header">
        <h1 class="page-title">🎉 你的专属养宠报告</h1>
        <p class="page-subtitle">综合评分 {{ result.overallScore }} 分</p>
      </div>

      <!-- 推荐品种 -->
      <section class="section">
        <h2 class="section-title">最适合你的品种 Top {{ result.recommendations?.length }}</h2>
        <el-row :gutter="16">
          <el-col v-for="rec in result.recommendations" :key="rec.breedName" :xs="24" :sm="12" :md="6">
            <div class="breed-card card-hover">
              <div class="match-badge">{{ rec.matchScore }}%</div>
              <h3>{{ rec.breedName }}</h3>
              <p class="breed-desc">{{ rec.breedDesc }}</p>
              <p class="breed-reason">💡 {{ rec.suitableReason }}</p>
              <div class="breed-tags">
                <el-tag v-for="tag in rec.tags" :key="tag" size="small" effect="plain">{{ tag }}</el-tag>
              </div>
              <el-progress :percentage="rec.matchScore" :stroke-width="6" :color="'#FF7F50'" />
            </div>
          </el-col>
        </el-row>
      </section>

      <!-- 养护建议 -->
      <section class="section">
        <h2 class="section-title">个性化养护建议</h2>
        <div class="tips-card">
          <div v-for="(tip, idx) in result.careTips" :key="idx" class="tip-item">{{ tip }}</div>
        </div>
      </section>

      <!-- Checklist -->
      <section class="section">
        <h2 class="section-title">养前准备度评估</h2>
        <div class="checklist-card" :class="{ ready: result.checklist?.ready }">
          <div class="checklist-score">
            <el-progress type="circle" :percentage="result.checklist?.score || 0" :width="120" :color="result.checklist?.ready ? '#67C23A' : '#FF7F50'" />
            <p class="checklist-label">{{ result.checklist?.ready ? '准备充分！' : '还需准备' }}</p>
          </div>
          <div class="checklist-items">
            <div v-for="item in result.checklist?.passedItems" :key="item" class="check-item passed">✅ {{ item }}</div>
            <div v-for="item in result.checklist?.warningItems" :key="item" class="check-item warning">⚠️ {{ item }}</div>
          </div>
        </div>
      </section>

      <div class="action-bar">
        <el-button size="large" @click="$router.push('/pets')">去看看待领养宠物</el-button>
        <el-button size="large" type="primary" @click="$router.push('/assessment')">重新测评</el-button>
      </div>
    </div>

    <!-- 空结果 — 显示默认示例 -->
    <div class="container" v-else>
      <div class="page-header">
        <h1 class="page-title">🎉 养宠测评示例报告</h1>
        <p class="page-subtitle">以下是基于典型用户画像的测评结果，完成测评后你将看到专属报告</p>
      </div>
      <section class="section">
        <h2 class="section-title">示例推荐品种</h2>
        <el-row :gutter="16">
          <el-col v-for="rec in defaultResult.recommendations" :key="rec.breedName" :xs="24" :sm="12" :md="6">
            <div class="breed-card card-hover">
              <div class="match-badge">{{ rec.matchScore }}%</div>
              <h3>{{ rec.breedName }}</h3>
              <p class="breed-desc">{{ rec.breedDesc }}</p>
              <p class="breed-reason">💡 {{ rec.suitableReason }}</p>
              <div class="breed-tags">
                <el-tag v-for="tag in rec.tags" :key="tag" size="small" effect="plain">{{ tag }}</el-tag>
              </div>
              <el-progress :percentage="rec.matchScore" :stroke-width="6" :color="'#FF7F50'" />
            </div>
          </el-col>
        </el-row>
      </section>
      <section class="section">
        <h2 class="section-title">示例养护建议</h2>
        <div class="tips-card">
          <div v-for="(tip, idx) in defaultResult.careTips" :key="idx" class="tip-item">{{ tip }}</div>
        </div>
      </section>
      <div class="action-bar">
        <el-button size="large" type="primary" @click="$router.push('/assessment')">开始我的专属测评</el-button>
      </div>
    </div>
    <AppFooter />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getAssessmentResult } from '@/api/assessment'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'

const route = useRoute()
const result = ref(null)

// 硬编码默认示例结果 — 基于"上班族、小空间、预算中等"的典型用户
const defaultResult = {
  overallScore: 78.5,
  dimensionScores: {
    living_space: 0.55,
    time_availability: 0.55,
    experience: 0.60,
    budget: 0.65,
    family_composition: 0.80,
    activity_level: 0.50,
    pet_preference: 0.65
  },
  recommendations: [
    {
      breedName: '英短蓝猫',
      breedDesc: '性格温顺安静，圆脸可爱，适合公寓饲养',
      matchScore: 92.3,
      suitableReason: '在居住条件方面与您高度匹配',
      tags: ['公寓友好', '安静陪伴', '适合新手']
    },
    {
      breedName: '中华田园猫',
      breedDesc: '适应力强、体质好、聪明独立，是新手的最佳选择',
      matchScore: 88.7,
      suitableReason: '在预算水平方面与您高度匹配',
      tags: ['经济实惠', '亲人和善', '适合新手']
    },
    {
      breedName: '橘猫',
      breedDesc: '性格温厚、贪吃可爱，是中华田园猫中的明星',
      matchScore: 85.5,
      suitableReason: '在居住条件方面与您高度匹配',
      tags: ['公寓友好', '亲人和善', '经济实惠']
    },
    {
      breedName: '法国斗牛犬',
      breedDesc: '安静温顺、适合公寓，但需注意呼吸道健康',
      matchScore: 81.2,
      suitableReason: '在家庭环境方面与您高度匹配',
      tags: ['公寓友好', '安静陪伴', '亲人和善']
    },
    {
      breedName: '比熊',
      breedDesc: '温顺粘人、不掉毛，适合公寓饲养但需要定期美容',
      matchScore: 78.9,
      suitableReason: '在家庭环境方面与您高度匹配',
      tags: ['公寓友好', '亲人和善']
    }
  ],
  careTips: [
    '🏠 居住空间适中，建议选择体型中小型、活动需求适中的宠物',
    '📚 新手养宠建议从适应力强的品种开始（如中华田园猫/犬），领养前多学习养护知识',
    '💰 中等预算完全可以给宠物提供优质生活，建议选择体质好的品种减少医疗开销',
    '💡 领养代替购买，给流浪动物一个温暖的家'
  ],
  checklist: {
    ready: true,
    score: 83,
    passedItems: ['居住空间满足基本养宠需求', '有足够时间陪伴宠物', '经济条件能满足宠物基本开销', '家庭环境适合养宠'],
    warningItems: ['建议在领养前学习基础养护知识（饮食、疫苗、疾病预防）']
  }
}

onMounted(async () => {
  // 先从 sessionStorage 读取（刚从测评页跳转过来）
  const cached = sessionStorage.getItem('assessmentResult')
  if (cached) {
    try {
      result.value = JSON.parse(cached)
      sessionStorage.removeItem('assessmentResult')
      return
    } catch { /* ignore */ }
  }

  // 再从 URL 参数加载
  const id = route.params.id
  if (id && id !== 'latest' && id !== 'undefined') {
    try {
      const res = await getAssessmentResult(id)
      if (res.data) {
        result.value = res.data
        return
      }
    } catch { /* ignore */ }
  }

  // 没有任何结果时，result 保持 null，模板会显示默认示例
})
</script>

<style lang="scss" scoped>
.result-page { min-height: 100vh; background: var(--color-bg); }
.breed-card {
  background: #fff; padding: 24px; border-radius: var(--border-radius);
  text-align: center; position: relative; margin-bottom: 16px;
  .match-badge {
    position: absolute; top: 12px; right: 12px;
    background: var(--color-primary); color: #fff; padding: 4px 12px;
    border-radius: 20px; font-size: 14px; font-weight: 700;
  }
  h3 { font-size: 18px; margin: 8px 0; }
  .breed-desc { font-size: 13px; color: var(--color-text-secondary); margin-bottom: 8px; }
  .breed-reason { font-size: 13px; color: var(--color-primary-dark); margin-bottom: 8px; }
  .breed-tags { display: flex; gap: 4px; flex-wrap: wrap; justify-content: center; margin-bottom: 12px; }
}
.tips-card {
  background: #fff; padding: 24px; border-radius: var(--border-radius);
  .tip-item { padding: 8px 0; font-size: 15px; line-height: 1.6; border-bottom: 1px solid var(--color-border); &:last-child { border: none; } }
}
.checklist-card {
  background: #fff; padding: 32px; border-radius: var(--border-radius); display: flex; gap: 40px; align-items: center;
  .checklist-score { text-align: center; .checklist-label { margin-top: 8px; font-weight: 600; } }
  .checklist-items { flex: 1; }
  .check-item { padding: 6px 0; font-size: 14px; }
}
.action-bar { text-align: center; padding: 40px 0 60px; display: flex; gap: 16px; justify-content: center; }
@media (max-width: 768px) { .checklist-card { flex-direction: column; } }
</style>
