import request from '@/utils/request'

export function getCommunityPosts(params) {
  return request.get('/api/community/posts', { params })
}

export function getCommunityPostDetail(id) {
  return request.get(`/api/community/posts/${id}`)
}

export function createCommunityPost(data, images) {
  return request.post('/api/community/posts', data, { params: { images: images?.join(',') } })
}

export function updateCommunityPost(id, data) {
  return request.put(`/api/community/posts/${id}`, data)
}

export function deleteCommunityPost(id) {
  return request.delete(`/api/community/posts/${id}`)
}

export function toggleLike(postId) {
  return request.post(`/api/community/posts/${postId}/like`)
}

export function getPostComments(postId, page = 1, size = 20) {
  return request.get(`/api/community/comments/${postId}`, { params: { page, size } })
}

export function addComment(data) {
  return request.post('/api/community/comments', data)
}

export function deleteComment(id) {
  return request.delete(`/api/community/comments/${id}`)
}
