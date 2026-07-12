import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginAPI, register as registerAPI } from '@/api/auth'
import router from '@/router'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')
  const isShelter = computed(() => user.value?.role === 'SHELTER')

  function hasRole(role) {
    return user.value?.role === role
  }

  async function login(credentials) {
    const res = await loginAPI(credentials)
    token.value = res.data.token
    user.value = res.data.user
    localStorage.setItem('token', token.value)
    localStorage.setItem('user', JSON.stringify(user.value))
    return res.data
  }

  async function register(data) {
    const res = await registerAPI(data)
    token.value = res.data.token
    user.value = res.data.user
    localStorage.setItem('token', token.value)
    localStorage.setItem('user', JSON.stringify(user.value))
    return res.data
  }

  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    router.push('/login')
  }

  function updateUser(userData) {
    user.value = { ...user.value, ...userData }
    localStorage.setItem('user', JSON.stringify(user.value))
  }

  return { token, user, isLoggedIn, isAdmin, isShelter, hasRole, login, register, logout, updateUser }
})
