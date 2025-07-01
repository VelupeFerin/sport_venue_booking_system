import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../store/user'

const API_BASE_URL = 'http://localhost:8080/api'

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
    // 不要在这里自动清除用户状态，让具体的API调用处理错误
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
  getUserOrders() {
    return apiClient.get('/user/orders')
  },
  
  // 取消订单
  cancelUserOrder(orderId) {
    return apiClient.post(`/user/orders/${orderId}/cancel`)
  },
  
  // 创建订单
  createOrder(sessionIds) {
    return apiClient.post('/user/orders', { sessionIds })
  },
  
  // 获取营业时间配置（公开API）
  getBusinessHours() {
    return apiClient.get('/user/business-hours')
  },
  
  // 获取系统配置（包括最大场次数等）
  getSystemConfig() {
    return apiClient.get('/user/business-hours')
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
  
  // 获取营业时间配置（公开API）
  getBusinessHours() {
    return apiClient.get('/user/business-hours')
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
export const getBusinessHours = userApi.getBusinessHours 