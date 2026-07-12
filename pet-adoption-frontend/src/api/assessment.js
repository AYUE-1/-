import request from '@/utils/request'

export function getQuestions(category) {
  return request.get('/api/assessment/questions', { params: { category } })
}

export function submitAssessment(data) {
  return request.post('/api/assessment/submit', data)
}

export function getAssessmentResult(id) {
  return request.get(`/api/assessment/result/${id}`)
}

export function getAssessmentHistory() {
  return request.get('/api/assessment/history')
}
