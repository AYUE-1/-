import request from '@/utils/request'

export function getHealthRecords(petId) {
  return request.get(`/api/health/pets/${petId}/records`)
}

export function createHealthRecord(data, images) {
  return request.post('/api/health/records', data, { params: { images: images?.join(',') } })
}

export function updateHealthRecord(id, data) {
  return request.put(`/api/health/records/${id}`, data)
}

export function deleteHealthRecord(id) {
  return request.delete(`/api/health/records/${id}`)
}

export function getReminders() {
  return request.get('/api/health/reminders')
}
