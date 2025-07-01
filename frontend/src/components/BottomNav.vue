<template>
  <div class="bottom-nav">
    <div class="nav-item" @click="navigateTo('/home')" :class="{ active: currentRoute === '/home' }">
      <el-icon class="nav-icon"><House /></el-icon>
      <span class="nav-text">主页</span>
    </div>
    <div class="nav-item" @click="navigateTo('/booking')" :class="{ active: currentRoute === '/booking' }">
      <el-icon class="nav-icon"><Calendar /></el-icon>
      <span class="nav-text">预约</span>
    </div>
    <div class="nav-item" @click="navigateTo('/orders')" :class="{ active: currentRoute === '/orders' }">
      <el-icon class="nav-icon"><Document /></el-icon>
      <span class="nav-text">订单</span>
    </div>
    <div class="nav-item" @click="navigateTo('/user')" :class="{ active: currentRoute === '/user' }">
      <el-icon class="nav-icon"><User /></el-icon>
      <span class="nav-text">我的</span>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { House, Calendar, Document, User } from '@element-plus/icons-vue'
import { useUserStore } from '../store/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const currentRoute = ref('/home')

onMounted(() => {
  currentRoute.value = route.path
})

watch(() => route.path, (newPath) => {
  currentRoute.value = newPath
})

const navigateTo = (path) => {
  // 检查是否需要登录
  if ((path === '/orders' || path === '/user') && !userStore.isLoggedIn) {
    router.push('/login')
  } else {
    router.push(path)
  }
}
</script>

<style scoped>
.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 60px;
  background: white;
  border-top: 1px solid #e4e7ed;
  display: flex;
  justify-content: space-around;
  align-items: center;
  z-index: 1000;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.1);
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex: 1;
  height: 100%;
  cursor: pointer;
  transition: all 0.3s ease;
  padding: 8px 0;
}

.nav-item:hover {
  background: #f5f7fa;
}

.nav-item.active {
  color: #FF6633;
}

.nav-icon {
  font-size: 20px;
  margin-bottom: 4px;
}

.nav-text {
  font-size: 12px;
  font-weight: 500;
}

/* 移动端优化 */
@media (max-width: 768px) {
  .bottom-nav {
    height: 70px;
  }
  
  .nav-icon {
    font-size: 22px;
  }
  
  .nav-text {
    font-size: 11px;
  }
}
</style> 