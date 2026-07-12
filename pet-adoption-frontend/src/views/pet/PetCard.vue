<template>
  <router-link :to="'/pets/' + pet.id" class="pet-card-link">
    <el-card class="pet-card" shadow="hover" :body-style="{ padding: '0' }">
      <div class="pet-cover">
        <el-image
          :src="getCoverUrl(pet.coverImage)"
          fit="cover"
          class="cover-image"
          v-if="pet.coverImage"
        >
          <template #error>
            <div class="image-placeholder">
              <el-icon :size="48"><PictureFilled /></el-icon>
            </div>
          </template>
        </el-image>
        <div v-else class="image-placeholder">
          <el-icon :size="48"><PictureFilled /></el-icon>
          <span>暂无图片</span>
        </div>
        <el-tag class="size-tag" size="small" type="info">{{ pet.size || '未知' }}</el-tag>
      </div>
      <div class="pet-info">
        <h3 class="pet-name">{{ pet.name }}</h3>
        <p class="pet-breed">{{ pet.categoryName || '' }} {{ pet.breed }}</p>
        <div class="pet-meta">
          <el-tag size="small" type="warning" class="age-badge">
            {{ pet.age }}
          </el-tag>
          <span class="gender-icon" :class="pet.gender === '公' || pet.gender === 'male' ? 'male' : 'female'">
            <el-icon :size="16">
              <Male v-if="pet.gender === '公' || pet.gender === 'male'" />
              <Female v-else />
            </el-icon>
            {{ pet.gender || '未知' }}
          </span>
        </div>
      </div>
    </el-card>
  </router-link>
</template>

<script setup>
import { PictureFilled, Male, Female } from '@element-plus/icons-vue'

defineProps({
  pet: {
    type: Object,
    required: true,
    default: () => ({})
  }
})

function getCoverUrl(url) {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://') || url.startsWith('data:')) return url
  if (url.startsWith('/')) return url
  const baseURL = import.meta.env.PROD ? '' : 'http://localhost:8080'
  return baseURL + '/' + url
}
</script>

<style scoped lang="scss">
.pet-card-link {
  text-decoration: none;
  display: block;
}

.pet-card {
  cursor: pointer;
  border-radius: 12px;
  overflow: hidden;
  transition: transform 0.3s ease, box-shadow 0.3s ease;

  &:hover {
    transform: translateY(-4px);
  }

  .pet-cover {
    position: relative;
    width: 100%;
    height: 200px;
    overflow: hidden;
    background: #f5f5f5;

    .cover-image {
      width: 100%;
      height: 100%;
    }

    .image-placeholder {
      width: 100%;
      height: 100%;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      color: #ccc;
      background: #f5f5f5;

      span {
        margin-top: 8px;
        font-size: 13px;
        color: #bbb;
      }
    }

    .size-tag {
      position: absolute;
      top: 10px;
      right: 10px;
    }
  }

  .pet-info {
    padding: 14px 16px 18px;

    .pet-name {
      margin: 0 0 6px;
      font-size: 16px;
      font-weight: 600;
      color: #303133;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .pet-breed {
      margin: 0 0 10px;
      font-size: 13px;
      color: #909399;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .pet-meta {
      display: flex;
      align-items: center;
      gap: 8px;

      .age-badge {
        flex-shrink: 0;
      }

      .gender-icon {
        display: flex;
        align-items: center;
        gap: 4px;
        font-size: 13px;
        color: #909399;

        &.male {
          color: #409eff;
        }
        &.female {
          color: #f56c6c;
        }
      }
    }
  }
}
</style>
