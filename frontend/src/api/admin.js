import axios from 'axios'
import {ElMessage} from 'element-plus'
import {useUserStore} from '../store/user'

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
const adminApi = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器，添加token
adminApi.interceptors.request.use(
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
adminApi.interceptors.response.use(
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

// 获取统计数据
export const getStats = async () => {
  try {
    return await adminApi.get('/admin/stats')
  } catch (error) {
    throw error
  }
}

// 获取订单详情用于核验
export const getOrderForVerification = async (orderId) => {
  try {
    return await adminApi.get(`/admin/order/${orderId}`)
  } catch (error) {
    throw error
  }
}

// 核验订单
export const verifyOrder = async (orderId) => {
  try {
    return await adminApi.post(`/admin/order/${orderId}/verify`)
  } catch (error) {
    throw error
  }
}

// 获取场次模板列表
export const getTemplates = async () => {
  try {
    return await adminApi.get('/admin/templates')
  } catch (error) {
    throw error
  }
}

// 创建场次模板
export const createTemplate = async (templateData) => {
  try {
    return await adminApi.post('/admin/templates', templateData)
  } catch (error) {
    throw error
  }
}

// 更新场次模板
export const updateTemplate = async (id, templateData) => {
  try {
    return await adminApi.put(`/admin/templates/${id}`, templateData)
  } catch (error) {
    throw error
  }
}

// 删除场次模板
export const deleteTemplate = async (id) => {
  try {
    return await adminApi.delete(`/admin/templates/${id}`)
  } catch (error) {
    throw error
  }
}

// 获取系统配置
export const getConfig = async () => {
  try {
    return await adminApi.get('/config')
  } catch (error) {
    throw error
  }
}

// 更新系统配置
export const updateConfig = async (configData) => {
  try {
    return await adminApi.put('/config', configData)
  } catch (error) {
    throw error
  }
}