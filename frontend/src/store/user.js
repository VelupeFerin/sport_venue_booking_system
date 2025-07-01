import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref(localStorage.getItem('token') || '')
  const username = ref(localStorage.getItem('username') || '')
  const isAdmin = ref(localStorage.getItem('isAdmin') === 'true')

  // 计算属性
  const isLoggedIn = computed(() => !!token.value)

  // 验证token字符
  const validateTokenCharacters = (tokenStr) => {
    if (!tokenStr) return true
    
    // JWT token应该以'eyJ'开头
    if (!tokenStr.startsWith('eyJ')) {
      console.error('Token does not start with "eyJ" (invalid JWT format)')
      return false
    }
    
    for (let i = 0; i < tokenStr.length; i++) {
      const c = tokenStr.charAt(i)
      if (c > 127) {
        console.error(`Token contains non-ASCII character at position ${i}: ${c} (code: ${c.charCodeAt(0)})`)
        return false
      }
      // JWT token应该只包含字母、数字、-、_、.、=
      if (!/[a-zA-Z0-9\-_.=]/.test(c)) {
        console.error(`Token contains invalid character at position ${i}: ${c} (code: ${c.charCodeAt(0)})`)
        return false
      }
    }
    return true
  }

  // 方法
  const setUserInfo = (userInfo) => {
    console.log('Setting user info:', userInfo)
    
    // 验证token字符
    if (userInfo.token && !validateTokenCharacters(userInfo.token)) {
      console.error('Invalid token characters detected, clearing token')
      userInfo.token = ''
    }
    
    token.value = userInfo.token || ''
    username.value = userInfo.username || ''
    isAdmin.value = userInfo.isAdmin || false
    
    // 同步到localStorage
    if (userInfo.token) {
      console.log('Storing token in localStorage:', userInfo.token.substring(0, 20) + '...')
      localStorage.setItem('token', userInfo.token)
      localStorage.setItem('username', userInfo.username)
      localStorage.setItem('isAdmin', userInfo.isAdmin)
    } else {
      // 如果token为空，清除localStorage
      console.log('Clearing localStorage')
      localStorage.removeItem('token')
      localStorage.removeItem('username')
      localStorage.removeItem('isAdmin')
    }
  }

  const clearUserInfo = () => {
    console.log('Clearing user info')
    token.value = ''
    username.value = ''
    isAdmin.value = false
    
    // 清除localStorage
    localStorage.removeItem('token')
    localStorage.removeItem('username')
    localStorage.removeItem('isAdmin')
  }

  const updateUsername = (newUsername) => {
    console.log('Updating username:', newUsername)
    username.value = newUsername
    localStorage.setItem('username', newUsername)
  }

  // 验证当前token
  if (token.value && !validateTokenCharacters(token.value)) {
    console.error('Invalid token found in localStorage, clearing')
    clearUserInfo()
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