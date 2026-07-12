import request from '@/utils/request'

export function uploadFile(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/api/files/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function addPetImage(data) {
  return request.post('/api/files/pet-image', data)
}

export function deletePetImage(imageId) {
  return request.delete(`/api/files/pet-image/${imageId}`)
}
