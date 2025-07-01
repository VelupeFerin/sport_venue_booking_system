import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref(localStorage.getItem('token') || '')
  const username = ref(localStorage.getItem('username') || '')
  const isAdmin = ref(localStorage.getItem('isAdmin') === 'true')

  // 计算属性
  const isLoggedIn = computed(() => !!token.value)

  // 方法
  const setUserInfo = (userInfo) => {
    token.value = userInfo.token || ''
    username.value = userInfo.username || ''
    isAdmin.value = userInfo.isAdmin || false
    
    // 同步到localStorage
    if (userInfo.token) {
      localStorage.setItem('token', userInfo.token)
      localStorage.setItem('username', userInfo.username)
      localStorage.setItem('isAdmin', userInfo.isAdmin)
    }
  }

  const clearUserInfo = () => {
    token.value = ''
    username.value = ''
    isAdmin.value = false
    
    // 清除localStorage
    localStorage.removeItem('token')
    localStorage.removeItem('username')
    localStorage.removeItem('isAdmin')
  }

  const updateUsername = (newUsername) => {
    username.value = newUsername
    localStorage.setItem('username', newUsername)
  }

  return {
    // 状态
    token,
    username,
    isAdmin,
    // 计算属性
    isLoggedIn,
    // 方法
    setUserInfo,
    clearUserInfo,
    updateUsername
  }
}) 