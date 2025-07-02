<template>
  <div class="admin-view">
    <div class="main-content">
      <div class="welcome-section">
        <h1>管理员控制台</h1>
        <div class="back-button-container">
          <el-button 
            type="primary" 
            @click="$router.push('/')" 
            class="back-button"
            :icon="ArrowLeft"
          >
            返回主页
          </el-button>
        </div>
        <p>欢迎使用预约系统管理后台</p>
      </div>
      
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-icon">
            <el-icon><Calendar /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ stats.totalOrders || 0 }}</div>
            <div class="stat-label">总订单数</div>
          </div>
        </div>
        
        <div class="stat-card">
          <div class="stat-icon">
            <el-icon><User /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ stats.totalUsers || 0 }}</div>
            <div class="stat-label">注册用户</div>
          </div>
        </div>
        
        <div class="stat-card">
          <div class="stat-icon">
            <el-icon><Clock /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ stats.todayOrders || 0 }}</div>
            <div class="stat-label">今日订单</div>
          </div>
        </div>
        
        <div class="stat-card">
          <div class="stat-icon">
            <el-icon><Check /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ stats.pendingVerification || 0 }}</div>
            <div class="stat-label">待核验</div>
          </div>
        </div>
      </div>
      
      <div class="quick-actions">
        <h2>快速操作</h2>
        <div class="action-grid">
          <div class="action-card" @click="$router.push('/admin/verification')">
            <el-icon><Check /></el-icon>
            <span>订单核验</span>
          </div>
          <div class="action-card" @click="$router.push('/admin/templates')">
            <el-icon><Grid /></el-icon>
            <span>场地模板</span>
          </div>
          <div class="action-card" @click="$router.push('/admin/config')">
            <el-icon><Setting /></el-icon>
            <span>系统配置</span>
          </div>
        </div>
      </div>
    </div>
    
    <AdminNav />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Calendar, User, Clock, Check, ArrowLeft } from '@element-plus/icons-vue'
import AdminNav from '@/components/AdminNav.vue'
import { getStats } from '@/api/admin'

const stats = ref({})

onMounted(async () => {
  // 设置页面标题
  document.title = '管理员控制台'
  
  try {
    const response = await getStats()
    stats.value = response.data
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
})
</script>

<style scoped>
.admin-view {
  min-height: 100vh;
  background: #f5f7fa;
  padding-bottom: 80px;
}

.main-content {
  max-width: 800px;
  margin: 0 auto;
  padding: 40px 20px;
}

.welcome-section {
  text-align: center;
  margin-bottom: 30px;
  padding: 30px 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.welcome-section h1 {
  margin: 0 0 10px 0;
  color: #303133;
  font-size: 24px;
  font-weight: 600;
}

.welcome-section p {
  margin: 0;
  color: #606266;
  font-size: 16px;
}

.back-button-container {
  margin-bottom: 15px;
  display: flex;
  justify-content: center;
}

.back-button {
  background: #FF6633;
  border-color: #FF6633;
  border-radius: 8px;
  padding: 8px 16px;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.back-button:hover {
  background: #e55a2b;
  border-color: #e55a2b;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(255, 102, 51, 0.3);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
  margin-bottom: 30px;
}

.stat-card {
  background: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 15px;
}

.stat-icon {
  width: 50px;
  height: 50px;
  background: #FF6633;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
}

.stat-content {
  flex: 1;
}

.stat-number {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #606266;
}

.quick-actions {
  background: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.quick-actions h2 {
  margin: 0 0 20px 0;
  color: #303133;
  font-size: 18px;
  font-weight: 600;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 15px;
}

.action-card {
  padding: 20px;
  border: 2px solid #e4e7ed;
  border-radius: 12px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.action-card:hover {
  border-color: #FF6633;
  background: #fff5f2;
}

.action-card .el-icon {
  font-size: 24px;
  color: #FF6633;
}

.action-card span {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

/* 移动端优化 */
@media (max-width: 768px) {
  .main-content {
    padding: 15px;
  }
  
  .welcome-section {
    padding: 20px 15px;
  }
  
  .welcome-section h1 {
    font-size: 20px;
  }
  
  .welcome-section p {
    font-size: 14px;
  }
  
  .stats-grid {
    gap: 12px;
  }
  
  .stat-card {
    padding: 15px;
  }
  
  .stat-icon {
    width: 40px;
    height: 40px;
    font-size: 20px;
  }
  
  .stat-number {
    font-size: 20px;
  }
  
  .action-grid {
    gap: 12px;
  }
  
  .action-card {
    padding: 15px;
  }
}
</style> 