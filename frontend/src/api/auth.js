import axios from 'axios'
import { ElMessage } from 'element-plus'

const API_BASE_URL = 'http://localhost:8080/api'

// 创建axios实例
const authApiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 响应拦截器
authApiClient.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    console.error('认证API请求错误:', error)
    return Promise.reject(error)
  }
)

const authApi = {
  // 注册
  register(data) {
    return authApiClient.post('/auth/register', data)
  },

  // 登录
  login(data) {
    return authApiClient.post('/auth/login', data)
  }
}

export default authApi 