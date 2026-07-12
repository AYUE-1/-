import request from '@/utils/request'

export function getPetComments(petId, page, size) {
  return request.get(`/api/pets/${petId}/comments`, { params: { page, size } })
}

export function addComment(petId, content, parentId) {
  return request.post(`/api/pets/${petId}/comments`, { content, parentId })
}

export function deleteComment(id) {
  return request.delete(`/api/comments/${id}`)
}
