<template>
  <div class="calculator-page">
    <AppHeader />
    <div class="container">
      <div class="page-header"><h1 class="page-title">💰 养宠成本计算器</h1><p class="page-subtitle">提前了解养宠开销，做好经济准备</p></div>
      <div class="calc-layout">
        <div class="calc-form">
          <el-form label-width="140px">
            <el-form-item label="宠物类型"><el-select v-model="calc.type"><el-option label="猫咪" value="CAT" /><el-option label="小型犬" value="SMALL_DOG" /><el-option label="中型犬" value="MED_DOG" /><el-option label="大型犬" value="LARGE_DOG" /></el-select></el-form-item>
            <el-form-item label="饮食档次"><el-radio-group v-model="calc.foodLevel"><el-radio-button value="basic">基础</el-radio-button><el-radio-button value="mid">中等</el-radio-button><el-radio-button value="premium">高端</el-radio-button></el-radio-group></el-form-item>
            <el-form-item label="猫砂/尿垫"><el-input-number v-model="calc.litter" :min="0" :max="500" /> 元/月</el-form-item>
            <el-form-item label="美容/洗护"><el-input-number v-model="calc.grooming" :min="0" :max="1000" /> 元/月</el-form-item>
            <el-form-item label="零食/玩具"><el-input-number v-model="calc.treats" :min="0" :max="500" /> 元/月</el-form-item>
            <el-form-item label="年度医疗预算"><el-input-number v-model="calc.medical" :min="0" :max="20000" /> 元/年</el-form-item>
          </el-form>
        </div>
        <div class="calc-result">
          <div class="result-card">
            <h3>📊 月度费用估算</h3>
            <div class="cost-breakdown">
              <div class="cost-row"><span>食品</span><span>{{ foodCost }} 元</span></div>
              <div class="cost-row"><span>猫砂/尿垫</span><span>{{ calc.litter }} 元</span></div>
              <div class="cost-row"><span>美容/洗护</span><span>{{ calc.grooming }} 元</span></div>
              <div class="cost-row"><span>零食/玩具</span><span>{{ calc.treats }} 元</span></div>
              <div class="cost-row"><span>医疗均摊</span><span>{{ medicalMonthly }} 元</span></div>
              <div class="cost-row total"><span>月度合计</span><span>{{ monthlyTotal }} 元</span></div>
              <div class="cost-row total"><span>年度合计</span><span>{{ yearlyTotal }} 元</span></div>
            </div>
            <el-alert :title="costAdvice" :type="monthlyTotal > 800 ? 'warning' : 'success'" :closable="false" style="margin-top:16px" />
          </div>
        </div>
      </div>
    </div>
    <AppFooter />
  </div>
</template>

<script setup>
import { reactive, computed } from 'vue'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'

const calc = reactive({ type: 'CAT', foodLevel: 'mid', litter: 50, grooming: 0, treats: 30, medical: 1000 })

const foodCostTable = { CAT: { basic: 80, mid: 150, premium: 300 }, SMALL_DOG: { basic: 100, mid: 200, premium: 400 }, MED_DOG: { basic: 180, mid: 350, premium: 600 }, LARGE_DOG: { basic: 300, mid: 500, premium: 900 } }
const foodCost = computed(() => foodCostTable[calc.type]?.[calc.foodLevel] || 150)
const medicalMonthly = computed(() => Math.round(calc.medical / 12))
const monthlyTotal = computed(() => foodCost.value + calc.litter + calc.grooming + calc.treats + medicalMonthly.value)
const yearlyTotal = computed(() => monthlyTotal.value * 12)

const costAdvice = computed(() => {
  if (monthlyTotal.value > 1000) return '预算充足，可以给宠物提供高质量生活'
  if (monthlyTotal.value > 500) return '中等预算，能满足宠物基本需求'
  return '建议适当提高预算，确保宠物基本医疗和营养需求'
})
</script>

<style lang="scss" scoped>
.calculator-page { min-height: 100vh; background: var(--color-bg); }
.calc-layout { display: flex; gap: 32px; max-width: 900px; margin: 0 auto; }
.calc-form { flex: 1; background: #fff; padding: 32px; border-radius: var(--border-radius); }
.calc-result { width: 350px; }
.result-card { background: #fff; padding: 24px; border-radius: var(--border-radius); position: sticky; top: 80px; }
.cost-breakdown { margin: 16px 0; }
.cost-row { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid var(--color-border); font-size: 15px; &.total { font-weight: 700; font-size: 17px; color: var(--color-primary); } }
@media (max-width: 768px) { .calc-layout { flex-direction: column; } .calc-result { width: 100%; } }
</style>
