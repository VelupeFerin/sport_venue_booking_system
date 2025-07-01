import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../store/user'

const API_BASE_URL = 'http://localhost:8080/api'

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

// 创建axios实例
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器，添加token
apiClient.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    if (userStore.token) {
      // 验证token字符
      if (!validateTokenCharacters(userStore.token)) {
        console.error('Invalid token detected in request interceptor, clearing user info')
        userStore.clearUserInfo()
        ElMessage.error('登录状态异常，请重新登录')
        return Promise.reject(new Error('Invalid token'))
      }
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器，处理认证错误
apiClient.interceptors.response.use(
  (response) => {
    return response.data
  },
  (error) => {
    if (error.response && error.response.status === 401) {
      // Token无效或过期，清除用户状态
      const userStore = useUserStore()
      userStore.clearUserInfo()
      ElMessage.error('登录已过期，请重新登录')
    }
    console.error('API请求错误:', error)
    return Promise.reject(error)
  }
)

const userApi = {
  // 获取用户信息
  getUserInfo() {
    return apiClient.get('/user/info')
  },

  // 更新用户信息
  updateUserInfo(data) {
    return apiClient.put('/user/update', data)
  },
  
  // 获取用户订单列表
  getUserOrders(page = null, size = null) {
    const params = {}
    if (page !== null) params.page = page
    if (size !== null) params.size = size
    return apiClient.get('/user/orders', { params })
  },
  
  // 取消订单
  cancelUserOrder(orderId) {
    return apiClient.post(`/user/orders/${orderId}/cancel`)
  },
  
  // 创建订单
  createOrder(sessionIds) {
    return apiClient.post('/user/orders', { sessionIds })
  },
  
  // 检查是否有未核验的订单
  checkPendingOrders() {
    return apiClient.get('/user/orders')
  }
}

// 公开API（无需认证）
const publicApi = {
  // 根据日期获取场次
  getSessionsByDate(date) {
    return apiClient.get(`/sessions/date/${date}`)
  },
  
  // 根据日期获取可预订场次
  getAvailableSessionsByDate(date) {
    return apiClient.get(`/sessions/available/date/${date}`)
  },
  
  // 获取系统配置（公开API）
  getSystemConfig() {
    return apiClient.get('/config')
  }
}

// 导出默认API（保持向后兼容）
export default userApi

// 导出公开API
export { publicApi }

// 导出具体方法（保持向后兼容）
export const getUserInfo = userApi.getUserInfo
export const updateUserInfo = userApi.updateUserInfo
export const getUserOrders = userApi.getUserOrders
export const cancelUserOrder = userApi.cancelUserOrder
export const getSystemConfig = publicApi.getSystemConfig 