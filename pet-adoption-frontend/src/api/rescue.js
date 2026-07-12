import request from '@/utils/request'

export function createRescue(data) {
  return request.post('/api/rescue', data)
}

export function getRescueList(params) {
  return request.get('/api/rescue', { params })
}

export function getRescueDetail(id) {
  return request.get(`/api/rescue/${id}`)
}

export function updateRescue(id, data) {
  return request.put(`/api/rescue/${id}`, data)
}

export function deleteRescue(id) {
  return request.delete(`/api/rescue/${id}`)
}

export function claimRescue(id) {
  return request.post(`/api/rescue/${id}/claim`)
}

export function updateRescueStatus(id, status) {
  return request.put(`/api/rescue/${id}/status`, { status })
}

export function addFollowUp(id, content) {
  return request.post(`/api/rescue/${id}/follow-up`, { content })
}

export function getMyRescues() {
  return request.get('/api/rescue/my')
}

export function getMyClaims() {
  return request.get('/api/rescue/my-claims')
}
