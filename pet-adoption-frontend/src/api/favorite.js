import request from '@/utils/request'

export function toggleFavorite(petId) {
  return request.post(`/api/favorites/${petId}`)
}

export function getMyFavorites(page, size) {
  return request.get('/api/favorites', { params: { page, size } })
}

export function checkFavorite(petId) {
  return request.get(`/api/favorites/check/${petId}`)
}
