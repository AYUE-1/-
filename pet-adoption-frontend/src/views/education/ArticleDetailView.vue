<template>
  <div class="article-detail-page">
    <AppHeader />
    <div class="container" v-if="article">
      <el-breadcrumb separator="/" style="padding:16px 0"><el-breadcrumb-item :to="{path:'/'}">首页</el-breadcrumb-item><el-breadcrumb-item :to="{path:'/education'}">养宠科普</el-breadcrumb-item><el-breadcrumb-item>{{ article.title }}</el-breadcrumb-item></el-breadcrumb>
      <article class="article-content">
        <h1>{{ article.title }}</h1>
        <div class="article-meta">
          <span>{{ article.authorName }}</span>
          <span>{{ formatTime(article.createdAt) }}</span>
          <span>👁 {{ article.viewCount }}</span>
          <span>{{ article.categoryName }}</span>
        </div>
        <div class="article-tags" v-if="article.tags?.length">
          <el-tag v-for="tag in article.tags" :key="tag" size="small" effect="plain">{{ tag }}</el-tag>
        </div>
        <div class="article-body" v-html="article.content || article.summary"></div>
      </article>

      <!-- 评论区 -->
      <div class="comments-section">
        <h2 class="section-title">
          评论 {{ commentTotal > 0 ? `(${commentTotal})` : '' }}
        </h2>

        <div v-if="commentsLoading" class="loading-mini">
          <el-icon class="is-loading" :size="20"><Loading /></el-icon>
          <span>加载评论中...</span>
        </div>

        <div v-else-if="comments.length === 0" class="no-comments">
          <p>暂无评论，快来发表第一条评论吧！</p>
        </div>

        <div v-else class="comments-list">
          <div v-for="comment in comments" :key="comment.id" class="comment-item">
            <div class="comment-main">
              <div class="comment-avatar">
                <el-avatar :size="40">{{ (comment.username || '用户')[0] }}</el-avatar>
              </div>
              <div class="comment-body">
                <div class="comment-header">
                  <span class="comment-user">{{ comment.nickname || comment.username || '匿名用户' }}</span>
                  <span class="comment-time">{{ formatTime(comment.createdAt) }}</span>
                </div>
                <p class="comment-content">{{ comment.content }}</p>
                <div class="comment-actions">
                  <el-button v-if="authStore.isLoggedIn" link type="primary" size="small" @click="toggleReply(comment.id)">
                    <el-icon><ChatLineRound /></el-icon> 回复
                  </el-button>
                  <el-button v-if="authStore.user?.id === comment.userId || authStore.isAdmin" link type="danger" size="small" @click="handleDeleteComment(comment.id)">
                    删除
                  </el-button>
                </div>

                <!-- 回复输入框 -->
                <div v-if="replyTarget === comment.id" class="reply-form">
                  <el-input v-model="replyContent" type="textarea" :rows="2" placeholder="写下你的回复..." maxlength="500" show-word-limit />
                  <div class="reply-actions">
                    <el-button size="small" @click="cancelReply">取消</el-button>
                    <el-button size="small" type="primary" :loading="replySubmitting" @click="submitReply(comment.id)">回复</el-button>
                  </div>
                </div>

                <!-- 子评论 -->
                <div v-if="comment.replies && comment.replies.length > 0" class="replies">
                  <div v-for="reply in comment.replies" :key="reply.id" class="reply-item">
                    <div class="comment-avatar">
                      <el-avatar :size="32">{{ (reply.username || '用户')[0] }}</el-avatar>
                    </div>
                    <div class="comment-body">
                      <div class="comment-header">
                        <span class="comment-user">{{ reply.nickname || reply.username || '匿名用户' }}</span>
                        <span class="comment-time">{{ formatTime(reply.createdAt) }}</span>
                      </div>
                      <p class="comment-content">{{ reply.content }}</p>
                      <div class="comment-actions">
                        <el-button v-if="authStore.user?.id === reply.userId || authStore.isAdmin" link type="danger" size="small" @click="handleDeleteComment(reply.id)">删除</el-button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 分页 -->
        <div v-if="commentTotal > commentPageSize" class="comment-pagination">
          <el-pagination v-model:current-page="commentPage" :page-size="commentPageSize" :total="commentTotal" layout="prev, pager, next" background size="small" @current-change="fetchComments" />
        </div>

        <!-- 发表评论 -->
        <div v-if="authStore.isLoggedIn" class="comment-form">
          <h3>发表评论</h3>
          <el-input v-model="newComment" type="textarea" :rows="3" placeholder="说说你的想法..." maxlength="500" show-word-limit />
          <div class="comment-submit">
            <el-button type="primary" :loading="commentSubmitting" @click="handleSubmitComment">发表评论</el-button>
          </div>
        </div>
        <div v-else class="comment-login-tip">
          <router-link :to="{ path: '/login', query: { redirect: $route.fullPath } }">登录</router-link>
          后参与评论
        </div>
      </div>
    </div>
    <AppFooter />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading, ChatLineRound } from '@element-plus/icons-vue'
import { getArticleDetail, getArticleComments, addArticleComment, deleteArticleComment } from '@/api/article'
import { useAuthStore } from '@/stores/auth'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'
import 'quill/dist/quill.snow.css'

const route = useRoute()
const authStore = useAuthStore()
const article = ref(null)

// 评论相关
const comments = ref([])
const commentPage = ref(1)
const commentPageSize = ref(10)
const commentTotal = ref(0)
const commentsLoading = ref(false)
const newComment = ref('')
const commentSubmitting = ref(false)
const replyTarget = ref(null)
const replyContent = ref('')
const replySubmitting = ref(false)

function formatTime(t) {
  if (!t) return ''
  const date = new Date(t)
  const now = new Date()
  const diff = now - date
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 172800000) return '昨天'
  return date.toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
}

async function fetchComments(page = 1) {
  commentsLoading.value = true
  try {
    const res = await getArticleComments(route.params.id, page, commentPageSize.value)
    const data = res.data
    if (data && data.records) {
      comments.value = data.records
      commentTotal.value = data.total || 0
    } else if (Array.isArray(data)) {
      comments.value = data
      commentTotal.value = data.length
    } else {
      comments.value = data?.records || []
      commentTotal.value = data?.total || 0
    }
    commentPage.value = page
  } catch {
    comments.value = []
  } finally {
    commentsLoading.value = false
  }
}

async function handleSubmitComment() {
  if (!newComment.value.trim()) return ElMessage.warning('请输入评论内容')
  commentSubmitting.value = true
  try {
    await addArticleComment(route.params.id, newComment.value.trim())
    ElMessage.success('评论发表成功')
    newComment.value = ''
    fetchComments(1)
  } catch {
    ElMessage.error('评论发表失败')
  } finally {
    commentSubmitting.value = false
  }
}

function toggleReply(commentId) {
  replyTarget.value = replyTarget.value === commentId ? null : commentId
  replyContent.value = ''
}

function cancelReply() {
  replyTarget.value = null
  replyContent.value = ''
}

async function submitReply(parentId) {
  if (!replyContent.value.trim()) return ElMessage.warning('请输入回复内容')
  replySubmitting.value = true
  try {
    await addArticleComment(route.params.id, replyContent.value.trim(), parentId)
    ElMessage.success('回复成功')
    cancelReply()
    fetchComments(commentPage.value)
  } catch {
    ElMessage.error('回复失败')
  } finally {
    replySubmitting.value = false
  }
}

async function handleDeleteComment(id) {
  try {
    await deleteArticleComment(id)
    ElMessage.success('已删除')
    fetchComments(commentPage.value)
  } catch {
    ElMessage.error('删除失败')
  }
}

onMounted(async () => {
  try { const res = await getArticleDetail(route.params.id); article.value = res.data } catch {}
  fetchComments()
})
</script>

<style lang="scss" scoped>
.article-detail-page { min-height: 100vh; background: var(--color-bg); }
.article-content {
  max-width: 800px; margin: 0 auto; background: #fff; padding: 40px; border-radius: var(--border-radius);
  h1 { font-size: 28px; margin-bottom: 16px; }
}
.article-meta { color: var(--color-text-secondary); font-size: 14px; display: flex; gap: 16px; margin-bottom: 16px; }
.article-tags { display: flex; gap: 8px; margin-bottom: 24px; }
.article-body {
  font-size: 16px; line-height: 1.9; color: #333;
  :deep(h1) { font-size: 24px; margin: 24px 0 12px; }
  :deep(h2) { font-size: 20px; margin: 20px 0 10px; }
  :deep(h3) { font-size: 17px; margin: 16px 0 8px; }
  :deep(p) { margin: 0 0 12px; }
  :deep(ul), :deep(ol) { padding-left: 24px; margin: 8px 0 16px; }
  :deep(li) { margin-bottom: 4px; }
  :deep(blockquote) {
    border-left: 4px solid var(--color-primary);
    padding: 8px 16px; margin: 16px 0;
    background: #f8f9fa; color: #666;
  }
  :deep(pre) { background: #f5f5f5; padding: 16px; border-radius: 8px; overflow-x: auto; }
  :deep(code) { background: #f0f0f0; padding: 2px 6px; border-radius: 4px; font-size: 14px; }
  :deep(img) { max-width: 100%; border-radius: 8px; margin: 12px 0; }
  :deep(a) { color: var(--color-primary); }
  :deep(strong) { font-weight: 600; }
}

// ===== 评论区 =====
.comments-section {
  max-width: 800px;
  margin: 0 auto;
  border-top: 1px solid #ebeef5;
  padding-top: 32px;
  margin-top: 32px;

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
</style>
