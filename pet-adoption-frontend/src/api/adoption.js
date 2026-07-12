import request from '@/utils/request'

export function applyAdoption(data) {
  return request.post('/api/adoptions', data)
}

export function getMyApplications(page, size) {
  return request.get('/api/adoptions/my', { params: { page, size } })
}

export function getApplicationDetail(id) {
  return request.get(`/api/adoptions/${id}`)
}

export function cancelApplication(id) {
  return request.put(`/api/adoptions/${id}/cancel`)
}

export function getAdminApplications(page, size, status) {
  return request.get('/api/adoptions/admin/list', { params: { page, size, status } })
}

export function reviewApplication(id, data) {
  return request.put(`/api/adoptions/${id}/review`, data)
}

export function ownerReviewApplication(id, data) {
  return request.put(`/api/adoptions/${id}/owner-review`, data)
}

export function getOwnerApplications(page, size, status) {
  return request.get('/api/adoptions/owner', { params: { page, size, status } })
}
