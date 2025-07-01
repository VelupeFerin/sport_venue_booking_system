<template>
  <div class="home-container">
    <div class="header">
      <div class="header-content">
        <h1>{{ venueName }}预订系统</h1>
        <div class="user-info" v-if="userStore.isLoggedIn">
          <span>欢迎，{{ userStore.username }}</span>
          <el-button class="logout-btn" size="small" @click="handleLogout">退出登录</el-button>
        </div>
      </div>
    </div>
    
    <div class="main-content">
      <!-- 统一显示欢迎页面 -->
      <div class="welcome-card">
        <h2>欢迎使用{{ venueName }}预订系统</h2>
        <p>专业的运动场馆在线预约平台，为您提供便捷的场地预订服务</p>
        <div class="feature-list">
          <div class="feature-item">
            <el-icon class="feature-icon"><Calendar /></el-icon>
            <div class="feature-content">
              <h3>在线预约</h3>
              <p>便捷的在线预约系统，随时随地预订您喜欢的运动场地</p>
            </div>
          </div>
          <div class="feature-item">
            <el-icon class="feature-icon"><Document /></el-icon>
            <div class="feature-content">
              <h3>订单管理</h3>
              <p>查看和管理您的所有预约订单，轻松掌握运动安排</p>
            </div>
          </div>
          <div class="feature-item">
            <el-icon class="feature-icon"><User /></el-icon>
            <div class="feature-content">
              <h3>个人中心</h3>
              <p>管理个人信息，查看运动记录，享受个性化服务</p>
            </div>
          </div>
        </div>

        <!-- 未登录用户显示登录注册按钮 -->
        <div v-if="!userStore.isLoggedIn" class="cta-buttons">
          <el-button type="primary" size="large" @click="$router.push('/login')">
            立即登录
          </el-button>
          <el-button type="success" size="large" @click="$router.push('/register')">
            注册账号
          </el-button>
        </div>
      </div>
    </div>
    
    <!-- 底部导航栏 -->
    <BottomNav />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Calendar, Document, User } from '@element-plus/icons-vue'
import BottomNav from '../components/BottomNav.vue'
import { useUserStore } from '../store/user'
import { publicApi } from '../api/user'

const router = useRouter()
const userStore = useUserStore()
const venueName = ref('运动场馆')

const handleLogout = () => {
  userStore.clearUserInfo()
  ElMessage.success('已退出登录')
  router.push('/home')
}

// 获取系统配置中的场馆名称
const loadVenueName = async () => {
  try {
    const response = await publicApi.getSystemConfig()
    if (response.success && response.data) {
      // 从系统配置中获取场馆名称
      const configs = response.data
      if (configs.venue_name) {
        venueName.value = configs.venue_name
      }
    }
  } catch (error) {
    console.error('获取场馆名称失败:', error)
    // 使用默认名称
    venueName.value = '运动场馆'
  }
}

onMounted(() => {
  loadVenueName()
})
</script>

<style scoped>
.home-container {
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
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.logout-btn {
  background: white !important;
  color: #FF6633 !important;
  border: 1px solid white !important;
  transition: all 0.3s ease !important;
}

.logout-btn:hover {
  background: #f0f0f0 !important;
  color: #e55a2b !important;
  border-color: #f0f0f0 !important;
}

.user-info span {
  font-size: 14px;
}

.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
}

.welcome-card {
  background: white;
  border-radius: 12px;
  padding: 40px;
  text-align: center;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.welcome-card h2 {
  color: #FF6633;
  margin: 0 0 16px 0;
  font-size: 28px;
  font-weight: 600;
}

.welcome-card p {
  color: #666;
  margin: 0 0 32px 0;
  font-size: 16px;
}

.user-details {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 24px;
  text-align: left;
  max-width: 400px;
  margin: 0 auto;
}

.user-details p {
  margin: 8px 0;
  color: #333;
}

.user-details strong {
  color: #FF6633;
}

.feature-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 24px;
  margin: 32px 0;
}

.feature-item {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 24px;
  background: #f8f9fa;
  border-radius: 12px;
  text-align: left;
}

.feature-icon {
  font-size: 32px;
  color: #FF6633;
  flex-shrink: 0;
}

.feature-content h3 {
  margin: 0 0 8px 0;
  color: #333;
  font-size: 18px;
  font-weight: 600;
}

.feature-content p {
  margin: 0;
  color: #666;
  font-size: 14px;
  line-height: 1.5;
}

.cta-buttons {
  display: flex;
  gap: 16px;
  justify-content: center;
  margin-top: 32px;
  flex-wrap: wrap;
}

@media (max-width: 768px) {
  .feature-list {
    grid-template-columns: 1fr;
  }
  
  .cta-buttons {
    flex-direction: row;
    align-items: center;
    gap: 12px;
  }
}
</style>
