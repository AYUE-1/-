<template>
  <div class="health-page">
    <AppHeader />
    <div class="container">
      <div class="page-header"><h1 class="page-title">💊 健康档案</h1><p class="page-subtitle">记录宠物的每一次成长和健康时刻</p></div>

      <!-- 提醒 -->
      <div v-if="reminders.length" class="reminder-bar">
        <el-alert v-for="r in reminders" :key="r.id" :title="`⏰ ${r.petName}: ${r.title}`" type="warning" :closable="false" style="margin-bottom:8px" />
      </div>

      <!-- 我的宠物卡片 -->
      <section class="section">
        <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
          <h2 class="section-title" style="margin:0;border:none;padding:0">我的宠物</h2>
          <el-button type="primary" @click="showAddPet = true">+ 添加宠物</el-button>
        </div>

        <el-row :gutter="16" v-if="myPets.length > 0">
          <el-col v-for="pet in myPets" :key="pet.id" :xs="24" :sm="12" :md="8">
            <div class="pet-health-card" :class="{ active: selectedPetId === pet.id }" @click="selectPet(pet.id)">
              <div class="phc-header">
                <el-image v-if="pet.coverImage" :src="getCoverUrl(pet.coverImage)" fit="cover" style="width:48px;height:48px;border-radius:50%" />
                <el-avatar v-else :size="48" style="font-size:24px">{{ getPetEmoji(pet.categoryId) }}</el-avatar>
                <div class="phc-info">
                  <h4>{{ pet.name }}</h4>
                  <span class="phc-breed">{{ pet.breed || pet.categoryName }}</span>
                </div>
                <el-tag :type="pet.status === 'AVAILABLE' ? 'warning' : 'success'" size="small">
                  {{ pet.source || (pet.status === 'AVAILABLE' ? '待领养' : '已领养') }}
                </el-tag>
              </div>
              <div class="phc-records" v-if="pet.recordCount > 0">
                📋 {{ pet.recordCount }} 条健康记录
              </div>
            </div>
          </el-col>
        </el-row>
        <el-empty v-else description="还没有添加宠物，点击上方按钮添加你的宠物" />
      </section>

      <!-- 选中宠物的健康记录 -->
      <section class="section" v-if="selectedPetId && selectedPet">
        <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
          <h2 class="section-title" style="margin:0;border:none;padding:0">
            {{ selectedPet.name }} 的健康记录
          </h2>
          <el-button type="primary" size="small" @click="showAddRecord = true">+ 添加记录</el-button>
        </div>

        <div v-if="records.length === 0" style="text-align:center;padding:40px;color:#999">
          🐾 还没有健康记录，点击「添加记录」开始记录吧
        </div>
        <div v-else class="records-timeline">
          <div v-for="r in records" :key="r.id" class="record-card" @click="editRecord(r)">
            <div class="rc-type">{{ getRecordEmoji(r.recordType) }} {{ r.recordTypeName }}</div>
            <h4>{{ r.title }}</h4>
            <p v-if="r.description">{{ r.description }}</p>
            <div class="rc-meta">
              <span>📅 {{ r.recordDate }}</span>
              <span v-if="r.nextDate">⏰ 下次: {{ r.nextDate }}</span>
              <el-tag v-if="r.reminderEnabled" size="small" type="warning">已设提醒</el-tag>
            </div>
          </div>
        </div>
      </section>

      <!-- 添加宠物弹窗 -->
      <el-dialog v-model="showAddPet" title="添加我的宠物" width="520px">
        <el-form :model="newPet" label-width="80px">
          <el-form-item label="名字" required><el-input v-model="newPet.name" placeholder="给宠物取个名字" /></el-form-item>
          <el-form-item label="类型" required>
            <el-select v-model="newPet.categoryId"><el-option label="🐱 猫咪" :value="1" /><el-option label="🐶 狗狗" :value="2" /><el-option label="🐾 其他" :value="3" /></el-select>
          </el-form-item>
          <el-form-item label="品种"><el-input v-model="newPet.breed" placeholder="如: 橘猫、金毛" /></el-form-item>
          <el-form-item label="年龄"><el-input v-model="newPet.age" placeholder="如: 1岁、6个月" /></el-form-item>
          <el-form-item label="性别">
            <el-radio-group v-model="newPet.gender"><el-radio value="MALE">♂ 公</el-radio><el-radio value="FEMALE">♀ 母</el-radio></el-radio-group>
          </el-form-item>
          <el-form-item label="来源" required>
            <el-select v-model="newPet.source" placeholder="宠物的来源">
              <el-option label="🏠 领养" value="领养" />
              <el-option label="💰 购买" value="购买" />
              <el-option label="🆘 救助" value="救助" />
              <el-option label="🎁 朋友赠送" value="朋友赠送" />
              <el-option label="🐣 自家繁殖" value="自家繁殖" />
              <el-option label="📦 其他" value="其他" />
            </el-select>
          </el-form-item>
          <el-form-item label="照片">
            <div class="upload-area">
              <el-upload
                :auto-upload="false"
                :show-file-list="false"
                :on-change="handleImageSelect"
                accept="image/*"
                drag
              >
                <div v-if="!uploadPreview" class="upload-placeholder">
                  <el-icon :size="32"><Plus /></el-icon>
                  <p>点击或拖拽上传照片</p>
                </div>
                <img v-else :src="uploadPreview" class="upload-preview" />
              </el-upload>
              <el-button v-if="uploadPreview" size="small" type="danger" style="margin-top:8px" @click="clearImage">移除照片</el-button>
            </div>
          </el-form-item>
          <el-form-item label="备注"><el-input v-model="newPet.description" type="textarea" :rows="2" placeholder="描述、性格特点等" /></el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="cancelAddPet">取消</el-button>
          <el-button type="primary" @click="handleAddPet" :loading="addingPet">添加</el-button>
        </template>
      </el-dialog>

      <!-- 添加健康记录弹窗 -->
      <el-dialog v-model="showAddRecord" title="添加健康记录" width="500px">
        <el-form :model="newRecord" label-width="80px">
          <el-form-item label="类型" required>
            <el-select v-model="newRecord.recordType">
              <el-option label="💉 疫苗" value="VACCINE" />
              <el-option label="💊 驱虫" value="DEWORM" />
              <el-option label="🏥 就诊" value="MEDICAL" />
              <el-option label="📝 成长日记" value="DIARY" />
            </el-select>
          </el-form-item>
          <el-form-item label="标题" required><el-input v-model="newRecord.title" placeholder="如: 狂犬疫苗、驱虫药" /></el-form-item>
          <el-form-item label="描述"><el-input v-model="newRecord.description" type="textarea" :rows="2" /></el-form-item>
          <el-form-item label="日期" required>
            <el-date-picker v-model="newRecord.recordDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" />
          </el-form-item>
          <el-form-item label="下次提醒">
            <el-date-picker v-model="newRecord.nextDate" type="date" placeholder="可选" value-format="YYYY-MM-DD" />
          </el-form-item>
          <el-form-item label="开启提醒">
            <el-switch v-model="newRecord.reminderEnabled" :active-value="1" :inactive-value="0" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="showAddRecord = false">取消</el-button>
          <el-button type="primary" @click="handleAddRecord" :loading="addingRecord">保存</el-button>
        </template>
      </el-dialog>
    </div>
    <AppFooter />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyPets, createPet } from '@/api/pet'
import { getHealthRecords, getReminders, createHealthRecord } from '@/api/health'
import { uploadFile } from '@/api/upload'
import { Plus } from '@element-plus/icons-vue'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'

const myPets = ref([])
const selectedPetId = ref(null)
const records = ref([])
const reminders = ref([])

const showAddPet = ref(false)
const showAddRecord = ref(false)
const addingPet = ref(false)
const addingRecord = ref(false)

const newPet = ref({ name: '', categoryId: 1, breed: '', age: '', gender: 'MALE', source: '领养', description: '' })
const newRecord = ref({ recordType: 'VACCINE', title: '', description: '', recordDate: '', nextDate: '', reminderEnabled: 0 })
const selectedFile = ref(null)
const uploadPreview = ref('')
const uploading = ref(false)

const selectedPet = computed(() => myPets.value.find(p => p.id === selectedPetId.value))

function getPetEmoji(catId) { return catId === 1 ? '🐱' : catId === 2 ? '🐶' : '🐾' }
function getRecordEmoji(t) { return t === 'VACCINE' ? '💉' : t === 'DEWORM' ? '💊' : t === 'MEDICAL' ? '🏥' : '📝' }
function getCoverUrl(url) {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://') || url.startsWith('data:') || url.startsWith('blob:')) return url
  if (url.startsWith('/')) return url
  return 'http://localhost:8080/' + url
}

function selectPet(id) {
  selectedPetId.value = id
  fetchRecords(id)
}

async function fetchRecords(petId) {
  try { const res = await getHealthRecords(petId || selectedPetId.value); records.value = res.data || [] } catch {}
}

function handleImageSelect(file) {
  selectedFile.value = file.raw
  uploadPreview.value = URL.createObjectURL(file.raw)
}

function clearImage() {
  selectedFile.value = null
  uploadPreview.value = ''
}

function cancelAddPet() {
  showAddPet.value = false
  clearImage()
}

async function handleAddPet() {
  if (!newPet.value.name) return ElMessage.warning('请输入宠物名字')
  addingPet.value = true
  try {
    // 先上传图片
    let coverImage = ''
    if (selectedFile.value) {
      const res = await uploadFile(selectedFile.value)
      coverImage = res.data || ''
    }
    await createPet({ ...newPet.value, coverImage, status: 'OWNED' })
    ElMessage.success('宠物添加成功')
    showAddPet.value = false
    newPet.value = { name: '', categoryId: 1, breed: '', age: '', gender: 'MALE', source: '领养', description: '' }
    clearImage()
    // 刷新列表
    const res = await getMyPets(1, 50)
    myPets.value = res.data?.records || []
  } catch {
    ElMessage.error('添加失败')
  } finally { addingPet.value = false }
}

async function handleAddRecord() {
  if (!newRecord.value.title) return ElMessage.warning('请输入标题')
  if (!newRecord.value.recordDate) return ElMessage.warning('请选择日期')
  addingRecord.value = true
  try {
    await createHealthRecord({ ...newRecord.value, petId: selectedPetId.value })
    ElMessage.success('记录添加成功')
    showAddRecord.value = false
    newRecord.value = { recordType: 'VACCINE', title: '', description: '', recordDate: '', nextDate: '', reminderEnabled: 0 }
    fetchRecords(selectedPetId.value)
  } catch {
    ElMessage.error('添加失败')
  } finally { addingRecord.value = false }
}

onMounted(async () => {
  try { const res = await getMyPets(1, 50); myPets.value = res.data?.records || [] } catch {}
  try { const res = await getReminders(); reminders.value = res.data || [] } catch {}
})
</script>

<style lang="scss" scoped>
.health-page { min-height: 100vh; background: var(--color-bg); }

.pet-health-card {
  background: #fff; padding: 16px; border-radius: var(--border-radius); cursor: pointer;
  border: 2px solid transparent; margin-bottom: 12px; transition: all 0.2s;
  &:hover { box-shadow: var(--shadow-hover); }
  &.active { border-color: var(--color-primary); background: var(--el-color-primary-light-9); }
  .phc-header { display: flex; align-items: center; gap: 12px; }
  .phc-info { flex: 1; h4 { margin: 0 0 2px; font-size: 16px; } .phc-breed { font-size: 13px; color: var(--color-text-secondary); } }
  .phc-records { margin-top: 10px; font-size: 13px; color: var(--color-primary); }
}

.upload-area { width: 100%; }
.upload-placeholder { padding: 24px; text-align: center; color: #999; p { margin-top: 8px; font-size: 13px; } }
.upload-preview { width: 100%; max-height: 200px; object-fit: cover; border-radius: 8px; }
.records-timeline { max-width: 700px; }
.record-card {
  background: #fff; padding: 16px; border-radius: var(--border-radius); margin-bottom: 12px;
  border-left: 4px solid var(--color-primary); cursor: pointer;
  &:hover { box-shadow: var(--shadow-card); }
  .rc-type { font-weight: 600; color: var(--color-primary); margin-bottom: 4px; }
  h4 { margin-bottom: 4px; }
  p { color: var(--color-text-secondary); font-size: 14px; }
  .rc-meta { margin-top: 8px; font-size: 13px; color: var(--color-text-secondary); display: flex; gap: 12px; align-items: center; flex-wrap: wrap; }
}
</style>
