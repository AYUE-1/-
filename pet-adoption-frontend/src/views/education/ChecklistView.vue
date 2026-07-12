<template>
  <div class="checklist-page">
    <AppHeader />
    <div class="container">
      <div class="page-header"><h1 class="page-title">✅ 养前Checklist</h1><p class="page-subtitle">在决定领养之前，请确认你已经准备好</p></div>
      <div class="checklist-content">
        <el-checkbox-group v-model="checked">
          <div v-for="(item, idx) in checklistItems" :key="idx" class="cl-item">
            <el-checkbox :value="idx">{{ item }}</el-checkbox>
          </div>
        </el-checkbox-group>
        <div class="cl-progress">
          <el-progress :percentage="progress" :color="progress === 100 ? '#67C23A' : '#FF7F50'" />
          <p>{{ progress === 100 ? '🎉 你已经完全准备好了！' : progress >= 70 ? '👍 基本准备好了，再检查一下遗漏的项目' : '📝 还需要更多准备，建议仔细阅读攻略' }}</p>
        </div>
      </div>
    </div>
    <AppFooter />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'

const checklistItems = [
  '我已确认房东/物业允许养宠',
  '所有家庭成员都同意并欢迎新成员',
  '我有稳定的收入来源，能承担宠物食品、医疗等费用（每月至少200-500元）',
  '我每天有足够的时间陪伴和照顾宠物（至少1-2小时）',
  '我已经了解基础的养宠知识（饮食、疫苗、行为训练）',
  '我做好了长期承诺的准备（猫狗寿命10-15年）',
  '我已准备好应对可能的意外（宠物生病、搬家、出差等情况）',
  '我了解领养流程，并愿意接受回访',
  '我已为宠物准备好基本用品（食物、水盆、猫砂/尿垫、玩具等）',
  '我承诺不因任何理由遗弃宠物，如无法继续饲养会寻找可靠下家'
]
const checked = ref([])
const progress = computed(() => Math.round((checked.value.length / checklistItems.length) * 100))
</script>

<style lang="scss" scoped>
.checklist-page { min-height: 100vh; background: var(--color-bg); }
.checklist-content { max-width: 700px; margin: 0 auto; background: #fff; padding: 32px; border-radius: var(--border-radius); }
.cl-item { padding: 10px 0; border-bottom: 1px solid var(--color-border); font-size: 15px; }
.cl-progress { margin-top: 24px; text-align: center; p { margin-top: 12px; color: var(--color-text-secondary); } }
</style>
