<template>
  <div class="pet-detail-page">
    <AppHeader />

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-icon class="is-loading" :size="36"><Loading /></el-icon>
      <p>加载中...</p>
    </div>

    <template v-else-if="pet">
      <div class="page-content">
        <!-- 面包屑 -->
        <el-breadcrumb separator="/" class="breadcrumb">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item :to="{ path: '/pets' }">宠物列表</el-breadcrumb-item>
          <el-breadcrumb-item>{{ pet.name }}</el-breadcrumb-item>
        </el-breadcrumb>

        <!-- 主体：左侧轮播 + 右侧信息 -->
        <div class="detail-main">
          <!-- 左侧图片展示 -->
          <div class="detail-gallery">
            <!-- 多图轮播 -->
            <el-carousel
              v-if="pet.images && pet.images.length > 0"
              :interval="4000"
              type="card"
              height="400px"
              arrow="always"
            >
              <el-carousel-item v-for="(img, idx) in pet.images" :key="idx">
                <el-image
                  :src="getImageUrl(img.url || img)"
                  fit="cover"
                  class="carousel-image"
                >
                  <template #error>
                    <div class="carousel-placeholder">
                      <el-icon :size="48"><PictureFilled /></el-icon>
                    </div>
                  </template>
                </el-image>
              </el-carousel-item>
            </el-carousel>
            <!-- 单张封面图 fallback -->
            <div v-else-if="pet.coverImage" class="single-cover">
              <el-image
                :src="getImageUrl(pet.coverImage)"
                fit="cover"
                class="cover-image"
              >
                <template #error>
                  <div class="carousel-placeholder single">
                    <el-icon :size="64"><PictureFilled /></el-icon>
                    <p>图片加载失败</p>
                  </div>
                </template>
              </el-image>
            </div>
            <!-- 完全无图 -->
            <div v-else class="carousel-placeholder single">
              <el-icon :size="64"><PictureFilled /></el-icon>
              <p>暂无图片</p>
            </div>
          </div>

          <!-- 右侧信息面板 -->
          <div class="detail-info">
            <h1 class="pet-name">{{ pet.name }}</h1>
            <p class="pet-subtitle">{{ pet.categoryName || '其他' }} · {{ pet.breed }}</p>

            <!-- 属性列表 -->
            <div class="info-grid">
              <div class="info-item">
                <span class="info-label">年龄</span>
                <span class="info-value">{{ pet.age || '未知' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">性别</span>
                <span class="info-value">
                  <el-icon :size="14" :color="pet.gender === '公' || pet.gender === 'male' ? '#409eff' : '#f56c6c'">
                    <Male v-if="pet.gender === '公' || pet.gender === 'male'" />
                    <Female v-else />
                  </el-icon>
                  {{ pet.gender || '未知' }}
                </span>
              </div>
              <div class="info-item">
                <span class="info-label">体型</span>
                <span class="info-value">{{ pet.size || '未知' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">毛色</span>
                <span class="info-value">{{ pet.color || '未知' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">健康状况</span>
                <span class="info-value">{{ pet.healthStatus || '未知' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">疫苗接种</span>
                <el-tag :type="pet.vaccination ? 'success' : 'info'" size="small">
                  {{ pet.vaccination ? '已完成' : '未完成' }}
                </el-tag>
              </div>
              <div class="info-item">
                <span class="info-label">绝育情况</span>
                <el-tag :type="pet.sterilization ? 'warning' : 'info'" size="small">
                  {{ pet.sterilization ? '已绝育' : '未绝育' }}
                </el-tag>
              </div>
            </div>

            <!-- 描述 -->
            <div class="info-description">
              <h3>详细介绍</h3>
              <p>{{ pet.description || '暂无详细介绍' }}</p>
            </div>

            <!-- 操作按钮 -->
            <div class="info-actions">
              <el-button
                type="primary"
                size="large"
                @click="goAdopt"
                :disabled="pet.status !== 'AVAILABLE'"
              >
                {{ pet.status === 'AVAILABLE' ? '申请领养' : '已被领养' }}
              </el-button>
              <el-button
                size="large"
                @click="handleToggleFavorite"
                :type="isFavorited ? 'danger' : 'default'"
                :icon="isFavorited ? StarFilled : Star"
                :disabled="!authStore.isLoggedIn"
              >
                {{ isFavorited ? '已收藏' : '收藏' }}
              </el-button>
            </div>
          </div>
        </div>

        <!-- 评论区 -->
        <div class="comments-section">
          <h2 class="section-title">
            评论 {{ commentTotal > 0 ? `(${commentTotal})` : '' }}
          </h2>

          <!-- 评论列表 -->
          <div v-if="commentsLoading" class="loading-mini">
            <el-icon class="is-loading" :size="20"><Loading /></el-icon>
            <span>加载评论中...</span>
          </div>

          <div v-else-if="comments.length === 0" class="no-comments">
            <p>暂无评论，快来发表第一条评论吧！</p>
          </div>

          <div v-else class="comments-list">
            <div
              v-for="comment in comments"
              :key="comment.id"
              class="comment-item"
            >
              <!-- 一级评论 -->
              <div class="comment-main">
                <div class="comment-avatar">
                  <el-avatar :size="40">{{ (comment.username || comment.userName || '用户')[0] }}</el-avatar>
                </div>
                <div class="comment-body">
                  <div class="comment-header">
                    <span class="comment-user">{{ comment.username || comment.userName || '匿名用户' }}</span>
                    <span class="comment-time">{{ formatTime(comment.createTime || comment.createdAt) }}</span>
                  </div>
                  <p class="comment-content">{{ comment.content }}</p>
                  <div class="comment-actions">
                    <el-button
                      v-if="authStore.isLoggedIn"
                      link
                      type="primary"
                      size="small"
                      @click="toggleReply(comment.id)"
                    >
                      <el-icon><ChatLineRound /></el-icon> 回复
                    </el-button>
                  </div>

                  <!-- 回复输入框 -->
                  <div v-if="replyTarget === comment.id" class="reply-form">
                    <el-input
                      v-model="replyContent"
                      type="textarea"
                      :rows="2"
                      placeholder="写下你的回复..."
                      maxlength="500"
                      show-word-limit
                    />
                    <div class="reply-actions">
                      <el-button size="small" @click="cancelReply">取消</el-button>
                      <el-button
                        size="small"
                        type="primary"
                        :loading="replySubmitting"
                        @click="submitReply(comment.id)"
                      >
                        回复
                      </el-button>
                    </div>
                  </div>

                  <!-- 子评论 -->
                  <div v-if="comment.replies && comment.replies.length > 0" class="replies">
                    <div
                      v-for="reply in comment.replies"
                      :key="reply.id"
                      class="reply-item"
                    >
                      <div class="comment-avatar">
                        <el-avatar :size="32">{{ (reply.username || reply.userName || '用户')[0] }}</el-avatar>
                      </div>
                      <div class="comment-body">
                        <div class="comment-header">
                          <span class="comment-user">{{ reply.username || reply.userName || '匿名用户' }}</span>
                          <span class="comment-time">{{ formatTime(reply.createTime || reply.createdAt) }}</span>
                        </div>
                        <p class="comment-content">{{ reply.content }}</p>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 评论区翻页 -->
          <div v-if="commentTotal > commentPageSize" class="comment-pagination">
            <el-pagination
              v-model:current-page="commentPage"
              :page-size="commentPageSize"
              :total="commentTotal"
              layout="prev, pager, next"
              background
              size="small"
              @current-change="fetchComments"
            />
          </div>

          <!-- 评论发表表单 -->
          <div v-if="authStore.isLoggedIn" class="comment-form">
            <h3>发表评论</h3>
            <el-input
              v-model="newComment"
              type="textarea"
              :rows="3"
              placeholder="说说你的想法..."
              maxlength="500"
              show-word-limit
            />
            <div class="comment-submit">
              <el-button
                type="primary"
                :loading="commentSubmitting"
                @click="handleSubmitComment"
              >
                发表评论
              </el-button>
            </div>
          </div>
          <div v-else class="comment-login-tip">
            <router-link :to="{ path: '/login', query: { redirect: $route.fullPath } }">
              登录
            </router-link>
            后参与评论
          </div>
        </div>
      </div>
    </template>

    <!-- 未找到宠物 -->
    <div v-else class="not-found">
      <el-result icon="warning" title="宠物不存在" sub-title="该宠物信息可能已下架或删除">
        <template #extra>
          <el-button type="primary" @click="$router.push('/pets')">返回列表</el-button>
        </template>
      </el-result>
    </div>

    <AppFooter />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Loading, PictureFilled, Male, Female,
  Star, StarFilled, ChatLineRound
} from '@element-plus/icons-vue'
import { getPetDetail } from '@/api/pet'
import { getPetComments, addComment } from '@/api/comment'
import { toggleFavorite, checkFavorite } from '@/api/favorite'
import { useAuthStore } from '@/stores/auth'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'

const props = defineProps({
  id: {
    type: [String, Number],
    required: true
  }
})

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

// 宠物详情
const pet = ref(null)
const loading = ref(true)

// 收藏状态
const isFavorited = ref(false)

// 评论相关
const comments = ref([])
const commentPage = ref(1)
const commentPageSize = ref(10)
const commentTotal = ref(0)
const commentsLoading = ref(false)
const newComment = ref('')
const commentSubmitting = ref(false)

// 回复相关
const replyTarget = ref(null)
const replyContent = ref('')
const replySubmitting = ref(false)

// 获取宠物详情
async function fetchPetDetail() {
  loading.value = true
  try {
    const res = await getPetDetail(props.id || route.params.id)
    pet.value = res.data
    document.title = pet.value?.name ? `${pet.value.name} - 宠物领养平台` : '宠物详情'
  } catch {
    ElMessage.error('获取宠物详情失败')
    pet.value = null
  } finally {
    loading.value = false
  }
}

// 获取评论列表
async function fetchComments(page = 1) {
  commentsLoading.value = true
  try {
    const petId = props.id || route.params.id
    const res = await getPetComments(petId, page, commentPageSize.value)
    const data = res.data
    if (data && data.records) {
      comments.value = data.records
      commentTotal.value = data.total || 0
    } else if (Array.isArray(data)) {
      comments.value = data
      commentTotal.value = data.length
    } else {
      comments.value = data?.records || data?.list || []
      commentTotal.value = data?.total || data?.totalElements || 0
    }
    commentPage.value = page
  } catch {
    // 评论加载失败，静默处理
    comments.value = []
  } finally {
    commentsLoading.value = false
  }
}

// 检查收藏状态
async function fetchFavoriteStatus() {
  if (!authStore.isLoggedIn) return
  try {
    const petId = props.id || route.params.id
    const res = await checkFavorite(petId)
    isFavorited.value = res.data === true || res.data?.favorited === true
  } catch {
    isFavorited.value = false
  }
}

// 切换收藏
async function handleToggleFavorite() {
  if (!authStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }

  try {
    const petId = props.id || route.params.id
    const res = await toggleFavorite(petId)
    isFavorited.value = !isFavorited.value
    ElMessage.success(isFavorited.value ? '已收藏' : '已取消收藏')
  } catch {
    ElMessage.error('操作失败')
  }
}

// 跳转领养申请页
function goAdopt() {
  if (!authStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push({ path: '/login', query: { redirect: `/adopt/${props.id || route.params.id}` } })
    return
  }
  router.push(`/adopt/${props.id || route.params.id}`)
}

// 发表评论
async function handleSubmitComment() {
  if (!newComment.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }

  commentSubmitting.value = true
  try {
    const petId = props.id || route.params.id
    await addComment(petId, newComment.value.trim())
    ElMessage.success('评论发表成功')
    newComment.value = ''
    fetchComments(1)
  } catch {
    ElMessage.error('评论发表失败')
  } finally {
    commentSubmitting.value = false
  }
}

// 切换回复
function toggleReply(commentId) {
  replyTarget.value = replyTarget.value === commentId ? null : commentId
  replyContent.value = ''
}

function cancelReply() {
  replyTarget.value = null
  replyContent.value = ''
}

// 提交回复
async function submitReply(parentId) {
  if (!replyContent.value.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }

  replySubmitting.value = true
  try {
    const petId = props.id || route.params.id
    await addComment(petId, replyContent.value.trim(), parentId)
    ElMessage.success('回复成功')
    cancelReply()
    fetchComments(commentPage.value)
  } catch {
    ElMessage.error('回复失败')
  } finally {
    replySubmitting.value = false
  }
}

// 格式化时间
function formatTime(time) {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 172800000) return '昨天'
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

// 处理图片 URL：相对路径加后端前缀，绝对路径直接使用
function getImageUrl(url) {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://') || url.startsWith('data:')) {
    return url
  }
  if (url.startsWith('/')) {
    return url
  }
  // 相对路径补全
  const baseURL = import.meta.env.PROD ? '' : 'http://localhost:8080'
  return baseURL + '/' + url
}

onMounted(() => {
  fetchPetDetail()
  fetchComments()
  fetchFavoriteStatus()
})
</script>

<style scoped lang="scss">
.pet-detail-page {
  min-height: 100vh;
  background: var(--color-bg);
  display: flex;
  flex-direction: column;
}

// 加载状态
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex: 1;
  padding: 120px 0;
  color: #909399;

  p {
    margin-top: 12px;
  }
}

.page-content {
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
  padding: 24px 20px;
  flex: 1;
}

// 面包屑
.breadcrumb {
  margin-bottom: 20px;
}

// 详情主体
.detail-main {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 32px;
  margin-bottom: 40px;

  @media (max-width: 768px) {
    grid-template-columns: 1fr;
  }
}

// 图片轮播
.detail-gallery {
  .carousel-image {
    width: 100%;
    height: 100%;
    border-radius: 12px;
    overflow: hidden;
  }

  .carousel-placeholder {
    width: 100%;
    height: 400px;
    background: #f0f0f0;
    border-radius: 12px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: #ccc;

    p {
      margin-top: 8px;
      color: #bbb;
    }

    &.single {
      // full width single placeholder
    }
  }
}

// 宠物信息
.detail-info {
  .pet-name {
    font-size: 28px;
    font-weight: 700;
    color: #303133;
    margin: 0 0 8px;
  }

  .pet-subtitle {
    font-size: 15px;
    color: #909399;
    margin: 0 0 24px;
  }

  .info-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 16px 24px;
    margin-bottom: 24px;
    padding: 20px;
    background: #fafafa;
    border-radius: 8px;

    .info-item {
      display: flex;
      align-items: center;
      gap: 8px;

      .info-label {
        font-size: 14px;
        color: #909399;
        white-space: nowrap;
        flex-shrink: 0;
      }

      .info-value {
        font-size: 14px;
        color: #303133;
        display: flex;
        align-items: center;
        gap: 4px;
      }
    }
  }

  .info-description {
    margin-bottom: 24px;

    h3 {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
      margin: 0 0 10px;
    }

    p {
      font-size: 14px;
      color: #606266;
      line-height: 1.8;
      white-space: pre-wrap;
    }
  }

  .info-actions {
    display: flex;
    gap: 16px;
  }
}

// 评论区
.comments-section {
  border-top: 1px solid #ebeef5;
  padding-top: 32px;

  .section-title {
    font-size: 20px;
    font-weight: 600;
    color: #303133;
    margin: 0 0 20px;
  }

  .loading-mini {
    display: flex;
    align-items: center;
    gap: 8px;
    color: #909399;
    padding: 20px 0;
  }

  .no-comments {
    padding: 40px 0;
    text-align: center;
    color: #909399;
  }

  .comments-list {
    .comment-item {
      padding: 16px 0;
      border-bottom: 1px solid #f0f0f0;

      .comment-main {
        display: flex;
        gap: 12px;
      }

      .comment-avatar {
        flex-shrink: 0;
      }

      .comment-body {
        flex: 1;
        min-width: 0;
      }

      .comment-header {
        display: flex;
        align-items: center;
        gap: 12px;
        margin-bottom: 6px;

        .comment-user {
          font-size: 14px;
          font-weight: 500;
          color: #303133;
        }

        .comment-time {
          font-size: 12px;
          color: #c0c4cc;
        }
      }

      .comment-content {
        font-size: 14px;
        color: #606266;
        line-height: 1.7;
        margin: 0 0 8px;
        word-break: break-all;
      }

      .comment-actions {
        margin-bottom: 4px;
      }

      .reply-form {
        margin-top: 12px;
        margin-bottom: 8px;

        .reply-actions {
          display: flex;
          justify-content: flex-end;
          gap: 8px;
          margin-top: 8px;
        }
      }

      .replies {
        margin-top: 12px;
        padding-left: 12px;
        border-left: 2px solid #ebeef5;

        .reply-item {
          display: flex;
          gap: 10px;
          padding: 10px 0;

          &:not(:last-child) {
            border-bottom: 1px solid #fafafa;
          }
        }
      }
    }
  }

  .comment-pagination {
    display: flex;
    justify-content: center;
    margin-top: 16px;
  }

  .comment-form {
    margin-top: 24px;
    padding-top: 24px;
    border-top: 1px solid #ebeef5;

    h3 {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
      margin: 0 0 12px;
    }

    .comment-submit {
      display: flex;
      justify-content: flex-end;
      margin-top: 12px;
    }
  }

  .comment-login-tip {
    margin-top: 24px;
    padding: 24px 0;
    text-align: center;
    font-size: 14px;
    color: #909399;

    a {
      color: #409eff;
      text-decoration: none;

      &:hover {
        text-decoration: underline;
      }
    }
  }
}

// 未找到
.not-found {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
}

</style>
