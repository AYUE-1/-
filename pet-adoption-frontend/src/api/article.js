import request from '@/utils/request'

export function getArticles(params) {
  return request.get('/api/articles', { params })
}

export function getArticleDetail(id) {
  return request.get(`/api/articles/${id}`)
}

export function createArticle(data, tagIds) {
  return request.post('/api/articles', data, { params: { tagIds: tagIds?.join(',') } })
}

export function updateArticle(id, data) {
  return request.put(`/api/articles/${id}`, data)
}

export function deleteArticle(id) {
  return request.delete(`/api/articles/${id}`)
}

export function getTags() {
  return request.get('/api/articles/tags')
}

export function createTag(name) {
  return request.post('/api/articles/tags', { name })
}

export function getChecklist() {
  return request.get('/api/articles/checklist')
}

// ===== 文章评论 =====
export function getArticleComments(articleId, page = 1, size = 10) {
  return request.get(`/api/articles/${articleId}/comments`, { params: { page, size } })
}

export function addArticleComment(articleId, content, parentId) {
  return request.post(`/api/articles/${articleId}/comments`, { content, parentId })
}

export function deleteArticleComment(id) {
  return request.delete(`/api/articles/comments/${id}`)
}
