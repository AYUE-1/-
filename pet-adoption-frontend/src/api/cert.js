import request from '@/utils/request'

export function applyCert(data) {
  return request.post('/api/users/cert', data)
}

export function getCertList(params) {
  return request.get('/api/admin/certs', { params })
}

export function reviewCert(userId, data) {
  return request.put(`/api/admin/certs/${userId}/review`, data)
}
