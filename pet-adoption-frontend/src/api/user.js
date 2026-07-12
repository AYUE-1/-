import request from '@/utils/request'

export function getProfile() {
  return request.get('/api/users/profile')
}

export function updateProfile(data) {
  return request.put('/api/users/profile', data)
}

export function updatePassword(data) {
  return request.put('/api/users/password', data)
}

export function updateAvatar(avatar) {
  return request.put('/api/users/avatar', { avatar })
}

export function getUserList(page, size) {
  return request.get('/api/admin/users', { params: { page, size } })
}

export function updateUserStatus(id, status) {
  return request.put(`/api/admin/users/${id}/status`, null, { params: { status } })
}
