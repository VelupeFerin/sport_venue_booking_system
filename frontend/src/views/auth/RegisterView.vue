<template>
  <div class="register-container">
    <div class="register-card">
      <div class="register-header">
        <h2>注册</h2>
        <p>创建您的账户</p>
      </div>
      
      <el-form 
        ref="registerFormRef" 
        :model="registerForm" 
        :rules="rules" 
        class="register-form"
        @submit.prevent="handleRegister"
      >
        <el-form-item prop="username">
          <el-input
            v-model="registerForm.username"
            placeholder="请输入用户名（3-20个字符）"
            :prefix-icon="User"
            size="large"
            clearable
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码（至少6位）"
            :prefix-icon="Lock"
            size="large"
            show-password
            clearable
          />
        </el-form-item>
        
        <el-form-item prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            :prefix-icon="Lock"
            size="large"
            show-password
            clearable
          />
        </el-form-item>
        
        <el-form-item prop="phone">
          <el-input
            v-model="registerForm.phone"
            placeholder="请输入手机号"
            :prefix-icon="Phone"
            size="large"
            clearable
          />
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="register-button"
            :loading="loading"
            @click="handleRegister"
          >
            注册
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="register-footer">
        <span>已有账户？</span>
        <el-link type="primary" @click="$router.push('/login')">立即登录</el-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Phone } from '@element-plus/icons-vue'
import authApi from '../../api/auth'

const router = useRouter()
const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  phone: ''
})

const loading = ref(false)
const registerFormRef = ref()

// 设置页面标题
onMounted(() => {
  document.title = '用户注册'
})

// 手机号验证函数
const validatePhone = (rule, value, callback) => {
  const phoneRegex = /^1[3-9]\d{9}$/
  if (!value) {
    callback(new Error('请输入手机号'))
  } else if (!phoneRegex.test(value)) {
    callback(new Error('请输入正确的手机号格式'))
  } else {
    callback()
  }
}

// 确认密码验证函数
const validateConfirmPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度必须在3-20个字符之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ],
  phone: [
    { required: true, validator: validatePhone, trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  try {
    await registerFormRef.value.validate()
    loading.value = true
    
    const response = await authApi.register(registerForm)
    
    if (response.success) {
      ElMessage.success('注册成功，请登录')
      router.push('/login')
    } else {
      ElMessage.error(response.message)
    }
  } catch (error) {
    if (error.response && error.response.data) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error('注册失败，请稍后重试')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #FF6633 0%, #FF8C42 100%);
  padding: 20px;
}

.register-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(255, 102, 51, 0.2);
  padding: 40px;
  width: 100%;
  max-width: 400px;
}

.register-header {
  text-align: center;
  margin-bottom: 30px;
}

.register-header h2 {
  color: #FF6633;
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
}

.register-header p {
  color: #666;
  margin: 0;
  font-size: 14px;
}

.register-form {
  margin-bottom: 20px;
}

.register-button {
  width: 100%;
  background: #FF6633;
  border-color: #FF6633;
  height: 48px;
  font-size: 16px;
  font-weight: 500;
}

.register-button:hover {
  background: #FF8C42;
  border-color: #FF8C42;
}

.register-footer {
  text-align: center;
  color: #666;
  font-size: 14px;
}

.register-footer .el-link {
  color: #FF6633;
  margin-left: 4px;
}

.register-footer .el-link:hover {
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