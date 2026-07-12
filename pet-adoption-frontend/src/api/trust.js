import request from '@/utils/request'

export function createReview(data) {
  return request.post('/api/trust/reviews', data)
}

export function getUserReviews(userId) {
  return request.get(`/api/trust/reviews/user/${userId}`)
}

export function getMyReviews() {
  return request.get('/api/trust/reviews/my')
}

export function addBlacklist(targetId, reason) {
  return request.post('/api/trust/blacklist', { targetId, reason })
}

export function getBlacklist() {
  return request.get('/api/trust/blacklist')
}

export function removeBlacklist(id) {
  return request.delete(`/api/trust/blacklist/${id}`)
}

export function applyVolunteer(data) {
  return request.post('/api/welfare/volunteer/apply', data)
}

export function getMyVolunteer() {
  return request.get('/api/welfare/volunteer/my')
}

export function getVolunteers() {
  return request.get('/api/welfare/volunteers')
}

export function getWelfareStats() {
  return request.get('/api/welfare/stats')
}

export function getVolunteerDetail(id) {
  return request.get(`/api/welfare/volunteer/${id}`)
}

export function updateMyVolunteer(data) {
  return request.put('/api/welfare/volunteer/my', data)
}
