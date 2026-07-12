<template>
  <div class="rescue-map-page">
    <AppHeader />
    <div class="map-container">
      <div class="map-sidebar">
        <h3>📍 附近救助信息</h3>
        <div class="map-list">
          <div v-for="item in list" :key="item.id" class="map-list-item" :class="{ active: selectedId === item.id }"
               @click="selectedId = item.id; $router.push(`/rescue/${item.id}`)">
            <div class="mli-header">
              <span>{{ getAnimalIcon(item.animalType) }} {{ item.title }}</span>
              <el-tag :type="emergencyTagType(item.emergencyLevel)" size="small">{{ emergencyText(item.emergencyLevel) }}</el-tag>
            </div>
            <div class="mli-meta">
              <span>📍 {{ item.addressDesc }}</span>
              <span v-if="item.distance">📏 {{ item.distance }}km</span>
            </div>
          </div>
        </div>
      </div>
      <div class="map-main">
        <div class="static-map">
          <div class="map-placeholder">
            <div style="font-size:64px">🗺️</div>
            <h2>救助地图</h2>
            <p>需要 GPS 定位权限以使用实时地图</p>
            <el-button type="primary" @click="getLocation">📍 获取我的位置</el-button>
            <p v-if="userLocation" style="margin-top:12px;color:var(--color-primary)">
              你的位置: {{ userLocation.lat.toFixed(4) }}, {{ userLocation.lng.toFixed(4) }}
            </p>
          </div>
          <div class="map-stats">
            <div class="stat-item" v-for="stat in stats" :key="stat.label">
              <span class="stat-num">{{ stat.value }}</span>
              <span class="stat-label">{{ stat.label }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
        <AppFooter />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getRescueList } from '@/api/rescue'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'

const list = ref([])
const selectedId = ref(null)
const userLocation = ref(null)
const stats = ref([
  { label: '待救助', value: 0 },
  { label: '处理中', value: 0 },
  { label: '已救助', value: 0 }
])

function getAnimalIcon(t) { return t === 'CAT' ? '🐱' : t === 'DOG' ? '🐶' : '🐾' }
function emergencyText(l) { return l === 'CRITICAL' ? '危急' : l === 'URGENT' ? '紧急' : '普通' }
function emergencyTagType(l) { return l === 'CRITICAL' ? 'danger' : l === 'URGENT' ? 'warning' : 'info' }

function getLocation() {
  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(
      pos => {
        userLocation.value = { lat: pos.coords.latitude, lng: pos.coords.longitude }
        fetchNearby()
      },
      () => { alert('无法获取位置，请检查浏览器定位权限') }
    )
  }
}

async function fetchNearby() {
  const params = { page: 1, size: 20 }
  if (userLocation.value) {
    params.latitude = userLocation.value.lat
    params.longitude = userLocation.value.lng
    params.radius = 50
    params.sortBy = 'distance'
  }
  try {
    const res = await getRescueList(params)
    list.value = res.data || []
    stats.value[0].value = list.value.filter(i => i.status === 'PENDING').length
    stats.value[1].value = list.value.filter(i => i.status === 'PROCESSING').length
    stats.value[2].value = list.value.filter(i => i.status === 'RESCUED').length
  } catch { /* ignore */ }
}

onMounted(fetchNearby)
</script>

<style lang="scss" scoped>
.rescue-map-page { min-height: 100vh; background: var(--color-bg); }
.map-container { display: flex; height: 500px; max-width: 1200px; margin: 0 auto; }
.map-sidebar { width: 350px; background: #fff; padding: 16px; overflow-y: auto; border-right: 1px solid var(--color-border);
  h3 { margin-bottom: 12px; } }
.map-list-item { padding: 12px; border-bottom: 1px solid var(--color-border); cursor: pointer;
  &:hover, &.active { background: var(--el-color-primary-light-9); }
  .mli-header { display: flex; justify-content: space-between; margin-bottom: 6px; font-weight: 500; }
  .mli-meta { font-size: 12px; color: var(--color-text-secondary); display: flex; gap: 8px; }
}
.map-main { flex: 1; }
.map-placeholder { display: flex; flex-direction: column; align-items: center; justify-content: center; height: 380px; text-align: center;
  h2 { margin: 12px 0; } p { color: var(--color-text-secondary); margin-bottom: 12px; } }
.map-stats { display: flex; justify-content: center; gap: 32px; padding: 16px; background: #fff; }
.stat-item { text-align: center; .stat-num { font-size: 28px; font-weight: 700; color: var(--color-primary); display: block; } .stat-label { font-size: 13px; color: var(--color-text-secondary); } }
@media (max-width: 768px) { .map-container { flex-direction: column-reverse; height: auto; } .map-sidebar { width: 100%; } }
</style>
