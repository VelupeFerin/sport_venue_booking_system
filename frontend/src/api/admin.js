import axios from 'axios'
import { useUserStore } from '../store/user'

const API_BASE_URL = 'http://localhost:8080/api/admin'

// 创建axios实例
const adminApi = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器 - 添加JWT token
adminApi.interceptors.request.use(
  config => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
adminApi.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    console.error('API请求错误:', error)
    return Promise.reject(error)
  }
)

// 获取统计数据
export const getStats = async () => {
  try {
    const response = await adminApi.get('/stats')
    return response
  } catch (error) {
    throw error
  }
}

// 获取订单信息用于核验
export const verifyOrder = async (orderNumber, verificationData = null) => {
  try {
    if (verificationData) {
      const response = await adminApi.post(`/order/${orderNumber}/verify`, verificationData)
      return response
    } else {
      const response = await adminApi.get(`/order/${orderNumber}`)
      return response
    }
  } catch (error) {
    throw error
  }
}

// 获取场地模板列表
export const getTemplates = async () => {
  try {
    const response = await adminApi.get('/templates')
    return response
  } catch (error) {
    throw error
  }
}

// 创建场地模板
export const createTemplate = async (templateData) => {
  try {
    const response = await adminApi.post('/templates', templateData)
    return response
  } catch (error) {
    throw error
  }
}

// 更新场地模板
export const updateTemplate = async (id, templateData) => {
  try {
    const response = await adminApi.put(`/templates/${id}`, templateData)
    return response
  } catch (error) {
    throw error
  }
}

// 删除场地模板
export const deleteTemplate = async (id) => {
  try {
    const response = await adminApi.delete(`/templates/${id}`)
    return response
  } catch (error) {
    throw error
  }
}

// 获取系统配置
export const getConfig = async () => {
  try {
    const response = await adminApi.get('/config')
    return response
  } catch (error) {
    throw error
  }
}

// 更新系统配置
export const updateConfig = async (configData) => {
  try {
    const response = await adminApi.put('/config', configData)
    return response
  } catch (error) {
    throw error
  }
}

// 导出默认对象，方便使用
export default {
  getStats,
  verifyOrder,
  getTemplates,
  createTemplate,
  updateTemplate,
  deleteTemplate,
  getConfig,
  updateConfig
} 