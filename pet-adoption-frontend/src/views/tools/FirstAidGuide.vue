<template>
  <div class="firstaid-page">
    <AppHeader />
    <div class="container">
      <div class="page-header"><h1 class="page-title">🚑 宠物急救指南</h1><p class="page-subtitle">紧急情况下的基本处理方法，关键时刻能救命</p></div>
      <div class="guide-content">
        <el-collapse v-model="activeGuides">
          <el-collapse-item v-for="g in guides" :key="g.title" :title="g.title" :name="g.title">
            <div class="guide-body">
              <h4>症状识别</h4><p>{{ g.symptoms }}</p>
              <h4>急救步骤</h4><ol><li v-for="(step, i) in g.steps" :key="i">{{ step }}</li></ol>
              <el-alert type="warning" :closable="false" title="⚠️ 紧急处理后请尽快送往宠物医院" />
            </div>
          </el-collapse-item>
        </el-collapse>
      </div>
    </div>
    <AppFooter />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'

const activeGuides = ref([])
const guides = [
  { title: '🐶 中暑/热射病', symptoms: '体温升高(>40℃)、呼吸急促、牙龈发红、虚弱或昏迷', steps: ['立即转移到阴凉通风处', '用凉水(不是冰水)打湿毛巾擦拭身体', '提供少量饮水', '尽快送医'] },
  { title: '🩸 外伤出血', symptoms: '明显伤口、持续流血', steps: ['用干净纱布或毛巾直接按压伤口止血', '抬高受伤部位', '如果持续出血超过10分钟，立即就医', '不要使用止血带（除非大动脉出血）'] },
  { title: '🤮 误食中毒', symptoms: '呕吐、腹泻、抽搐、流口水、精神萎靡', steps: ['立即联系宠物医院或中毒控制中心', '不要自行催吐', '保留可疑毒物样本', '尽快送医'] },
  { title: '🦴 骨折/外伤', symptoms: '跛行、肢体变形、触碰时惨叫', steps: ['限制宠物活动，不要强行移动', '用硬纸板或杂志做临时夹板固定', '小心搬运，保持受伤部位稳定', '立即送医'] },
  { title: '💓 心肺复苏(CPR)', symptoms: '无呼吸、无心跳、牙龈苍白', steps: ['检查气道，清除口腔异物', '人工呼吸：合住嘴部，对着鼻孔轻轻吹气(每4-5秒一次)', '胸外按压：侧卧，按压胸部（100-120次/分钟）', '持续CPR直到恢复或到达医院'] }
]
</script>

<style lang="scss" scoped>
.firstaid-page { min-height: 100vh; background: var(--color-bg); }
.guide-content { max-width: 800px; margin: 0 auto; }
.guide-body {
  h4 { color: var(--color-primary); margin: 12px 0 8px; }
  p { line-height: 1.7; }
  ol { padding-left: 20px; li { padding: 4px 0; } }
}
</style>
