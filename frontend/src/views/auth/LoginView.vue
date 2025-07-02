<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <h2>登录</h2>
        <p>请登录您的账户</p>
      </div>
      
      <el-form 
        ref="loginFormRef" 
        :model="loginForm" 
        :rules="rules" 
        class="login-form"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            :prefix-icon="User"
            size="large"
            clearable
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            size="large"
            show-password
            clearable
          />
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="login-button"
            :loading="loading"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="login-footer">
        <span>还没有账户？</span>
        <el-link type="primary" @click="$router.push('/register')">立即注册</el-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import authApi from '../../api/auth'
import { useUserStore } from '@/store/user.js'

const router = useRouter()
const userStore = useUserStore()
const loginForm = reactive({
  username: '',
  password: ''
})

const loading = ref(false)
const loginFormRef = ref()

// 设置页面标题
onMounted(() => {
  document.title = '登录'
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  try {
    await loginFormRef.value.validate()
    loading.value = true
    
    const response = await authApi.login(loginForm)
    
    if (response.success) {
      console.log('Login successful, token received:', response.data.token.substring(0, 20) + '...')
      
      // 验证token字符
      const token = response.data.token
      let hasInvalidChar = false
      
      // 检查JWT格式
      if (!token.startsWith('eyJ')) {
        console.error('Token does not start with "eyJ" (invalid JWT format)')
        hasInvalidChar = true
      }
      
      for (let i = 0; i < token.length; i++) {
        const c = token.charAt(i)
        if (c > 127) {
          console.error(`Token contains non-ASCII character at position ${i}: ${c} (code: ${c.charCodeAt(0)})`)
          hasInvalidChar = true
        }
        if (!/[a-zA-Z0-9\-_.=]/.test(c)) {
          console.error(`Token contains invalid character at position ${i}: ${c} (code: ${c.charCodeAt(0)})`)
          hasInvalidChar = true
        }
      }
      
      if (hasInvalidChar) {
        ElMessage.error('登录失败：token格式异常')
        return
      }
      
      userStore.setUserInfo({
        token: response.data.token,
        username: response.data.username,
        isAdmin: response.data.isAdmin
      })
      
      ElMessage.success('登录成功')
      router.push('/home')
    } else {
      ElMessage.error(response.message)
    }
  } catch (error) {
    console.error('Login error:', error)
    if (error.response && error.response.data) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error('登录失败，请稍后重试')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #FF6633 0%, #FF8C42 100%);
  padding: 20px;
}

.login-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(255, 102, 51, 0.2);
  padding: 40px;
  width: 100%;
  max-width: 400px;
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h2 {
  color: #FF6633;
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
}

.login-header p {
  color: #666;
  margin: 0;
  font-size: 14px;
}

.login-form {
  margin-bottom: 20px;
}

.login-button {
  width: 100%;
  background: #FF6633;
  border-color: #FF6633;
  height: 48px;
  font-size: 16px;
  font-weight: 500;
}

.login-button:hover {
  background: #FF8C42;
  border-color: #FF8C42;
}

.login-footer {
  text-align: center;
  color: #666;
  font-size: 14px;
}

.login-footer .el-link {
  color: #FF6633;
  margin-left: 4px;
}

.login-footer .el-link:hover {
  color: #FF8C42;
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
}

:deep(.el-button--primary) {
  border-radius: 8px;
}

/* 修复输入框在电脑端的显示问题 */
:deep(.el-input__inner) {
  font-size: 14px;
  line-height: 1.5;
}

:deep(.el-input__wrapper.is-focus) {
  border-color: #FF6633;
  box-shadow: 0 0 0 1px #FF6633;
}
</style> 