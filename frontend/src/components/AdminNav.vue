<template>
  <div class="admin-nav">
    <div class="nav-item" @click="$router.push('/admin')" :class="{ active: currentRoute === '/admin' }">
      <el-icon class="nav-icon"><House /></el-icon>
      <span class="nav-text">主页</span>
    </div>
    <div class="nav-item" @click="$router.push('/admin/verification')" :class="{ active: currentRoute === '/admin/verification' }">
      <el-icon class="nav-icon"><Check /></el-icon>
      <span class="nav-text">核验</span>
    </div>
    <div class="nav-item" @click="$router.push('/admin/templates')" :class="{ active: currentRoute === '/admin/templates' }">
      <el-icon class="nav-icon"><Grid /></el-icon>
      <span class="nav-text">模板</span>
    </div>
    <div class="nav-item" @click="$router.push('/admin/config')" :class="{ active: currentRoute === '/admin/config' }">
      <el-icon class="nav-icon"><Setting /></el-icon>
      <span class="nav-text">配置</span>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { House, Check, Grid, Setting } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const currentRoute = ref('/admin')

onMounted(() => {
  currentRoute.value = route.path
})

watch(() => route.path, (newPath) => {
  currentRoute.value = newPath
})
</script>

<style scoped>
.admin-nav {
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
  .admin-nav {
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