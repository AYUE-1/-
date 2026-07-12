import request from '@/utils/request'

export function getPetList(params) {
  return request.get('/api/pets', { params })
}

export function getPetDetail(id) {
  return request.get(`/api/pets/${id}`)
}

export function createPet(data) {
  return request.post('/api/pets', data)
}

export function updatePet(id, data) {
  return request.put(`/api/pets/${id}`, data)
}

export function deletePet(id) {
  return request.delete(`/api/pets/${id}`)
}

export function updatePetStatus(id, status) {
  return request.put(`/api/pets/${id}/status`, null, { params: { status } })
}

export function getMyPets(page, size) {
  return request.get('/api/pets/my', { params: { page, size } })
}
