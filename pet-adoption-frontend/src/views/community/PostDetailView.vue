<template>
  <div class="post-detail-page">
    <AppHeader />
    <div class="container" v-if="post">
      <el-breadcrumb separator="/" style="padding:16px 0"><el-breadcrumb-item :to="{path:'/'}">首页</el-breadcrumb-item><el-breadcrumb-item :to="{path:'/community'}">社区</el-breadcrumb-item><el-breadcrumb-item>{{ post.title }}</el-breadcrumb-item></el-breadcrumb>
      <div class="pd-main">
        <div class="pd-content">
          <h1>{{ post.title }}</h1>
          <div class="pd-meta"><el-avatar :size="24">{{ post.username?.charAt(0) }}</el-avatar> {{ post.username }} · {{ formatTime(post.createdAt) }} · 👁 {{ post.viewCount }}</div>
          <p class="pd-text">{{ post.content }}</p>
          <div class="pd-images" v-if="post.images?.length">
            <el-image v-for="img in post.images" :key="img" :src="'/' + img" fit="cover" style="width:200px;height:200px;border-radius:8px;margin:4px" />
          </div>
          <div class="pd-actions">
            <el-button @click="handleLike" :type="post.isLiked ? 'primary' : 'default'">❤️ {{ post.likeCount }}</el-button>
          </div>

          <!-- 评论区 -->
          <div class="comments-section">
            <h3>评论 ({{ comments.length }})</h3>
            <div class="comment-input" v-if="authStore.isLoggedIn">
              <el-input v-model="newComment" type="textarea" :rows="2" placeholder="写下你的评论..." />
              <el-button type="primary" @click="submitComment" style="margin-top:8px">发表评论</el-button>
            </div>
            <div v-for="c in comments" :key="c.id" class="comment-item">
              <el-avatar :size="28">{{ c.username?.charAt(0) }}</el-avatar>
              <div class="ci-body">
                <div class="ci-header"><span class="ci-name">{{ c.username }}</span><span class="ci-time">{{ formatTime(c.createdAt) }}</span></div>
                <p>{{ c.content }}</p>
                <div v-if="c.replies?.length" class="ci-replies">
                  <div v-for="r in c.replies" :key="r.id" class="reply-item">
                    <span class="ri-name">{{ r.username }}</span>: {{ r.content }}
                  </div>
                </div>
              </div>
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
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCommunityPostDetail, toggleLike, getPostComments, addComment } from '@/api/community'
import { useAuthStore } from '@/stores/auth'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'

const route = useRoute()
const authStore = useAuthStore()
const post = ref(null)
const comments = ref([])
const newComment = ref('')

function formatTime(t) { return t ? new Date(t).toLocaleString() : '' }

async function fetchPost() {
  try { const res = await getCommunityPostDetail(route.params.id); post.value = res.data; await fetchComments() } catch {}
}
async function fetchComments() {
  try { const res = await getPostComments(route.params.id); comments.value = res.data || [] } catch {}
}
async function handleLike() {
  if (!authStore.isLoggedIn) return ElMessage.warning('请先登录')
  try { const res = await toggleLike(post.value.id); if (post.value) post.value.likeCount = res.data } catch {}
}
async function submitComment() {
  if (!newComment.value.trim()) return
  try {
    await addComment({ postId: post.value.id, content: newComment.value })
    newComment.value = ''
    fetchComments()
  } catch {}
}

onMounted(fetchPost)
</script>

<style lang="scss" scoped>
.post-detail-page { min-height: 100vh; background: var(--color-bg); }
.pd-main { max-width: 800px; margin: 0 auto; }
.pd-content { background: #fff; padding: 32px; border-radius: var(--border-radius);
  h1 { font-size: 24px; margin-bottom: 12px; }
}
.pd-meta { display: flex; align-items: center; gap: 8px; color: var(--color-text-secondary); font-size: 14px; margin-bottom: 20px; }
.pd-text { font-size: 16px; line-height: 1.8; margin-bottom: 16px; }
.pd-images { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 16px; }
.pd-actions { margin-bottom: 24px; }
.comments-section { border-top: 1px solid var(--color-border); padding-top: 20px;
  h3 { margin-bottom: 16px; }
}
.comment-input { margin-bottom: 20px; }
.comment-item { display: flex; gap: 10px; padding: 12px 0; border-bottom: 1px solid var(--color-border); }
.ci-body { flex: 1; .ci-header { margin-bottom: 4px; .ci-name { font-weight: 600; margin-right: 8px; } .ci-time { font-size: 12px; color: #999; } } p { font-size: 14px; } }
.ci-replies { background: #f8f9fa; padding: 10px; border-radius: 8px; margin-top: 8px; .reply-item { font-size: 13px; padding: 4px 0; .ri-name { font-weight: 500; color: var(--color-primary); } } }
</style>
