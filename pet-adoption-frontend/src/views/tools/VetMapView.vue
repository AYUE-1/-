<template>
  <div class="vet-map-page">
    <AppHeader />
    <div class="container">
      <div class="page-header"><h1 class="page-title">🏥 附近宠物医院</h1><p class="page-subtitle">找到最近的专业宠物医疗资源</p></div>
      <div class="map-placeholder" style="text-align:center;padding:60px;background:#fff;border-radius:16px">
        <span style="font-size:64px">🏥</span>
        <h2>宠物医院地图</h2>
        <p style="color:var(--color-text-secondary);margin:12px 0">需要接入地图 API 以显示附近宠物医院</p>
        <el-button type="primary" @click="getLocation">📍 获取我的位置并搜索</el-button>
        <p v-if="location" style="margin-top:12px;color:var(--color-primary)">位置: {{ location.lat.toFixed(4) }}, {{ location.lng.toFixed(4) }}</p>
      </div>
      <div class="hospital-list" style="margin-top:24px">
        <h3 class="section-title">示例宠物医院（北京市）</h3>
        <div class="hospital-grid">
          <div v-for="h in sampleHospitals" :key="h.name" class="hospital-card card-hover">
            <h4>🏥 {{ h.name }}</h4>
            <p>📍 {{ h.address }}</p>
            <p>📞 {{ h.phone }}</p>
            <el-tag size="small">{{ h.hours }}</el-tag>
          </div>
        </div>
      </div>
    </div>
    <AppFooter />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'

const location = ref(null)

const sampleHospitals = [
  { name: '北京芭比堂动物医院', address: '朝阳区望京西路', phone: '010-84781234', hours: '24小时' },
  { name: '北京农大动物医院', address: '海淀区圆明园西路2号', phone: '010-62731234', hours: '8:00-20:00' },
  { name: '美联众合动物医院', address: '东城区东直门内大街', phone: '010-64071234', hours: '9:00-21:00' },
  { name: '宠爱国际动物医院', address: '朝阳区百子湾路', phone: '010-67781234', hours: '24小时' }
]

function getLocation() {
  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(
      pos => { location.value = { lat: pos.coords.latitude, lng: pos.coords.longitude } },
      () => { alert('无法获取位置') }
    )
  }
}
</script>

<style lang="scss" scoped>
.vet-map-page { min-height: 100vh; background: var(--color-bg); }
.hospital-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16px; @media (max-width: 576px) { grid-template-columns: 1fr; } }
.hospital-card { background: #fff; padding: 20px; border-radius: var(--border-radius);
  h4 { margin-bottom: 8px; } p { font-size: 13px; color: var(--color-text-secondary); margin-bottom: 4px; }
}
</style>
