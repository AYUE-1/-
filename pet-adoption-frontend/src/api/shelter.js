import request from '@/utils/request'

export function getShelterDashboard() {
  return request.get('/api/shelter/dashboard')
}

export function getShelterProfile(userId) {
  return request.get(`/api/shelter/profile/${userId}`)
}
