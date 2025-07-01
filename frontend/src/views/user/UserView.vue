<template>
  <div class="user-container">
    <div class="header">
      <div class="header-content">
        <h1>个人中心</h1>
      </div>
    </div>
    
    <div class="main-content">
      <!-- 用户信息卡片 -->
      <div class="user-info-card">
        <div class="card-header">
          <h2>个人信息</h2>
          <el-button 
            :type="isEditing ? 'success' : 'primary'"
            @click="handleEditToggle"
            :loading="updating"
          >
            {{ isEditing ? '保存' : '编辑' }}
          </el-button>
        </div>

        <!-- 只读模式 -->
        <div v-if="!isEditing" class="info-display">
          <div class="info-item">
            <span class="label">用户名：</span>
            <span class="value">{{ userInfo.username }}</span>
          </div>
          <div class="info-item">
            <span class="label">手机号：</span>
            <span class="value">{{ userInfo.phone || '未设置' }}</span>
          </div>
          <div class="info-item">
            <span class="label">用户类型：</span>
            <el-tag :type="userInfo.isAdmin ? 'danger' : 'primary'">
              {{ userInfo.isAdmin ? '商家(管理员)' : '普通用户' }}
            </el-tag>
          </div>
        </div>

        <!-- 编辑模式 -->
        <div v-else class="edit-mode">
          <el-form 
            ref="userFormRef" 
            :model="userForm" 
            :rules="rules" 
            label-width="100px"
            class="user-form"
          >
            <el-form-item label="用户名" prop="username">
              <el-input 
                v-model="userForm.username" 
                placeholder="请输入用户名"
                :prefix-icon="User"
                clearable
              />
            </el-form-item>
            
            <el-form-item label="手机号" prop="phone">
              <el-input 
                v-model="userForm.phone" 
                placeholder="请输入手机号"
                :prefix-icon="Phone"
                clearable
              />
            </el-form-item>
            
            <el-form-item label="用户类型">
              <el-tag :type="userInfo.isAdmin ? 'danger' : 'primary'">
                {{ userInfo.isAdmin ? '商家(管理员)' : '普通用户' }}
              </el-tag>
            </el-form-item>

            <!-- 密码修改区域 -->
            <el-divider content-position="left">修改密码（可选）</el-divider>
            
            <el-form-item label="旧密码" prop="oldPassword">
              <el-input 
                v-model="userForm.oldPassword" 
                type="password"
                placeholder="请输入旧密码"
                :prefix-icon="Lock"
                show-password
                clearable
              />
            </el-form-item>
            
            <el-form-item label="新密码" prop="newPassword">
              <el-input 
                v-model="userForm.newPassword" 
                type="password"
                placeholder="请输入新密码"
                :prefix-icon="Lock"
                show-password
                clearable
              />
            </el-form-item>
            
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input 
                v-model="userForm.confirmPassword" 
                type="password"
                placeholder="请再次输入新密码"
                :prefix-icon="Lock"
                show-password
                clearable
              />
            </el-form-item>
          </el-form>
        </div>
      </div>
      
      <!-- 管理员入口 -->
      <div v-if="userInfo.isAdmin" class="admin-entry-card">
        <h2>管理员功能</h2>
        <p>您是管理员用户，可以访问管理控制台</p>
        <el-button 
          type="danger" 
          size="large"
          @click="$router.push('/admin')"
        >
          进入管理控制台
        </el-button>
      </div>
    </div>
    
    <!-- 底部导航栏 -->
    <BottomNav />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Phone, Lock } from '@element-plus/icons-vue'
import BottomNav from '../../components/BottomNav.vue'
import userApi from '../../api/user'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../store/user'

const isEditing = ref(false)
const updating = ref(false)
const userFormRef = ref()
const router = useRouter()
const userStore = useUserStore()

// 用户信息（只读显示）
const userInfo = reactive({
  username: '',
  phone: '',
  isAdmin: false
})

// 用户信息表单（编辑时使用）
const userForm = reactive({
  username: '',
  phone: '',
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 手机号验证函数
const validatePhone = (rule, value, callback) => {
  if (!value) {
    callback() // 手机号可以为空
    return
  }
  const phoneRegex = /^1[3-9]\d{9}$/
  if (!phoneRegex.test(value)) {
    callback(new Error('请输入正确的手机号格式'))
  } else {
    callback()
  }
}

// 新密码验证函数
const validateNewPassword = (rule, value, callback) => {
  if (!value && !userForm.oldPassword) {
    callback() // 如果旧密码和新密码都为空，则不验证
    return
  }
  if (userForm.oldPassword && !value) {
    callback(new Error('请输入新密码'))
    return
  }
  if (value && value.length < 6) {
    callback(new Error('密码长度不能少于6位'))
    return
  }
  callback()
}

// 确认密码验证函数
const validateConfirmPassword = (rule, value, callback) => {
  if (!userForm.newPassword && !value) {
    callback() // 如果新密码和确认密码都为空，则不验证
    return
  }
  if (userForm.newPassword && !value) {
    callback(new Error('请确认新密码'))
    return
  }
  if (value !== userForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
    return
  }
  callback()
}

// 旧密码验证函数
const validateOldPassword = (rule, value, callback) => {
  if (!value && !userForm.newPassword) {
    callback() // 如果旧密码和新密码都为空，则不验证
    return
  }
  if (userForm.newPassword && !value) {
    callback(new Error('请输入旧密码'))
    return
  }
  callback()
}

// 表单验证规则
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度必须在3-20个字符之间', trigger: 'blur' }
  ],
  phone: [
    { validator: validatePhone, trigger: 'blur' }
  ],
  oldPassword: [
    { validator: validateOldPassword, trigger: 'blur' }
  ],
  newPassword: [
    { validator: validateNewPassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 获取用户信息
const loadUserInfo = async () => {
  try {
    const response = await userApi.getUserInfo()
    if (response.success) {
      const user = response.data
      userInfo.username = user.username
      userInfo.phone = user.phone
      userInfo.isAdmin = user.isAdmin
    }
  } catch (error) {
    console.error('Error loading user info:', error)
    // API拦截器已经处理了401错误，这里只需要处理其他错误
    if (error.response && error.response.status !== 401) {
      ElMessage.error('获取用户信息失败')
    }
  }
}

// 编辑/保存切换
const handleEditToggle = async () => {
  if (!isEditing.value) {
    // 进入编辑模式
    isEditing.value = true
    userForm.username = userInfo.username
    userForm.phone = userInfo.phone
    // 清空密码字段
    userForm.oldPassword = ''
    userForm.newPassword = ''
    userForm.confirmPassword = ''
  } else {
    // 保存模式
    await handleSaveInfo()
  }
}

// 保存用户信息
const handleSaveInfo = async () => {
  try {
    await userFormRef.value.validate()
    updating.value = true
    
    // 构建更新数据
    const updateData = {
      username: userForm.username,
      phone: userForm.phone
    }
    
    // 如果填写了密码相关字段，则添加到更新数据中
    if (userForm.oldPassword && userForm.newPassword) {
      updateData.oldPassword = userForm.oldPassword
      updateData.newPassword = userForm.newPassword
    }
    
    const response = await userApi.updateUserInfo(updateData)
    
    if (response.success) {
      // 更新显示信息
      userInfo.username = userForm.username
      userInfo.phone = userForm.phone
      
      // 只有当用户名真正改变时才更新store中的用户名
      if (userForm.username !== userStore.username) {
        userStore.updateUsername(userForm.username)
      }
      
      // 检查是否返回了新token（用户名更新时）
      if (response.data && typeof response.data === 'string' && response.data.startsWith('eyJ')) {
        // 只有以'eyJ'开头的字符串才是JWT token
        console.log('Received new token, updating user info')
        userStore.setUserInfo({
          token: response.data,
          username: userForm.username,
          isAdmin: userStore.isAdmin
        })
        ElMessage.success('用户名更新成功')
      } else {
        // 用户名没有改变，只更新显示信息，不更新token
        console.log('No token received, keeping existing token')
        ElMessage.success('个人信息更新成功')
      }
      
      // 退出编辑模式
      isEditing.value = false
    } else {
      ElMessage.error(response.message)
    }
  } catch (error) {
    if (error.response && error.response.data) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error('更新用户信息失败')
    }
  } finally {
    updating.value = false
  }
}

onMounted(() => {
  // 清理可能损坏的localStorage数据
  const storedToken = localStorage.getItem('token')
  if (storedToken) {
    let hasInvalidChar = false
    
    // 检查JWT格式
    if (!storedToken.startsWith('eyJ')) {
      console.warn('Token does not start with "eyJ" (invalid JWT format)')
      hasInvalidChar = true
    }
    
    for (let i = 0; i < storedToken.length; i++) {
      const c = storedToken.charAt(i)
      if (c > 127 || !/[a-zA-Z0-9\-_.=]/.test(c)) {
        hasInvalidChar = true
        break
      }
    }
    
    if (hasInvalidChar) {
      console.warn('Invalid token found in localStorage, clearing all user data')
      localStorage.removeItem('token')
      localStorage.removeItem('username')
      localStorage.removeItem('isAdmin')
      userStore.clearUserInfo()
      ElMessage.warning('检测到登录状态异常，已自动清理，请重新登录')
      router.push('/login')
      return
    }
  }
  
  loadUserInfo()
})
</script>

<style scoped>
.user-container {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 80px; /* 为底部导航栏留出空间 */
}

.header {
  background: #FF6633;
  color: white;
  padding: 16px 0;
  box-shadow: 0 2px 8px rgba(255, 102, 51, 0.2);
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.header h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
}

.main-content {
  max-width: 800px;
  margin: 0 auto;
  padding: 40px 20px;
}

.user-info-card,
.admin-entry-card {
  background: white;
  border-radius: 12px;
  padding: 30px;
  margin-bottom: 20px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.card-header h2 {
  color: #FF6633;
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.info-display {
  padding: 10px 0;
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.info-item:last-child {
  border-bottom: none;
  margin-bottom: 0;
}

.info-item .label {
  font-weight: 600;
  color: #666;
  min-width: 100px;
  font-size: 16px;
}

.info-item .value {
  color: #333;
  font-size: 16px;
  flex: 1;
}

.edit-mode {
  padding: 10px 0;
}

.user-form {
  max-width: 500px;
}

.admin-entry-card {
  text-align: center;
}

.admin-entry-card h2 {
  color: #FF6633;
  margin: 0 0 20px 0;
  font-size: 20px;
  font-weight: 600;
}

.admin-entry-card p {
  color: #666;
  margin: 0 0 20px 0;
  font-size: 16px;
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
}

:deep(.el-button) {
  border-radius: 8px;
}

:deep(.el-form-item) {
  margin-bottom: 20px;
}

:deep(.el-divider__text) {
  color: #FF6633;
  font-weight: 600;
}
</style> 