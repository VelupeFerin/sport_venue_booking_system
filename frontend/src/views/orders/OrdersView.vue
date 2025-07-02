<template>
  <div class="orders-container">
    <div class="header">
      <div class="header-content">
        <h1>我的订单</h1>
      </div>
    </div>
    
    <div class="main-content">
      <div v-if="loading && orders.length === 0" class="loading-container">
        <el-icon class="is-loading"><Loading /></el-icon>
        <p>加载中...</p>
      </div>
      
      <div v-else-if="orders.length === 0" class="empty-container">
        <el-empty description="暂无订单" />
        <el-button type="primary" @click="$router.push('/booking')">
          立即预约
        </el-button>
      </div>
      
      <div v-else class="orders-list">
        <OrderCard
          v-for="order in orders"
          :key="order.id"
          :order="order"
          :venue-name="venueName"
          :cancel-time-limit="cancelTimeLimit"
          @order-cancelled="loadOrders"
          @show-qr-code="showQRCode"
        />
        
        <!-- 加载更多区域 -->
        <div v-if="hasMore" class="load-more-container">
          <div v-if="loadingMore" class="loading-more">
            <el-icon class="is-loading"><Loading /></el-icon>
            <p>加载更多订单中...</p>
          </div>
          <el-button v-else @click="loadMore" type="primary" plain>
            加载更多订单
          </el-button>
        </div>
        
        <!-- 没有更多数据提示 -->
        <div v-else-if="orders.length > 0" class="no-more-container">
          <p>已加载全部订单</p>
        </div>
      </div>
    </div>
    
    <!-- 底部导航栏 -->
    <BottomNav />
    
    <!-- 二维码模态框 -->
    <QRCodeModal
      v-model:visible="qrCodeModalVisible"
      :order-number="selectedOrderNumber"
      :status="selectedOrderStatus"
      title="订单二维码"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import BottomNav from '../../components/BottomNav.vue'
import QRCodeModal from '../../components/QRCodeModal.vue'
import OrderCard from '../../components/OrderCard.vue'
import { getUserOrders } from '@/api/user'
import { publicApi } from '@/api/user'

const orders = ref([])
const loading = ref(true)
const loadingMore = ref(false)
const qrCodeModalVisible = ref(false)
const selectedOrderNumber = ref('')
const selectedOrderStatus = ref('pending')
const cancelTimeLimit = ref(4) // 默认4小时
const venueName = ref('运动场馆') // 场馆名称

// 分页相关状态
const currentPage = ref(0)
const pageSize = ref(10)
const hasMore = ref(true)
const totalPages = ref(0)

// 防抖函数
let scrollTimeout = null

onMounted(async () => {
  // 设置页面标题
  document.title = '我的订单'
  
  await Promise.all([
    loadOrders(),
    loadCancelTimeLimit(),
    loadVenueName()
  ])
  
  // 添加滚动监听
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  // 移除滚动监听
  window.removeEventListener('scroll', handleScroll)
  if (scrollTimeout) {
    clearTimeout(scrollTimeout)
  }
})

const loadOrders = async () => {
  try {
    loading.value = true
    currentPage.value = 0
    orders.value = []
    
    const response = await getUserOrders(currentPage.value, pageSize.value)
    if (response.code === 200) {
      if (response.data && response.data.content) {
        // 分页数据
        orders.value = response.data.content || []
        totalPages.value = response.data.totalPages || 0
        hasMore.value = currentPage.value < totalPages.value - 1
      } else {
        // 兼容旧版本API（返回数组）
        orders.value = response.data || []
        hasMore.value = false
      }
    } else {
      ElMessage.error('获取订单列表失败: ' + response.message)
      orders.value = []
      hasMore.value = false
    }
  } catch (error) {
    console.error('获取订单列表失败:', error)
    ElMessage.error('获取订单列表失败')
    orders.value = []
    hasMore.value = false
  } finally {
    loading.value = false
  }
}

const loadMore = async () => {
  if (loadingMore.value || !hasMore.value) return
  
  try {
    loadingMore.value = true
    currentPage.value++
    
    const response = await getUserOrders(currentPage.value, pageSize.value)
    if (response.code === 200) {
      if (response.data && response.data.content) {
        // 分页数据
        const newOrders = response.data.content || []
        orders.value.push(...newOrders)
        hasMore.value = currentPage.value < response.data.totalPages - 1
      } else {
        // 兼容旧版本API
        const newOrders = response.data || []
        orders.value.push(...newOrders)
        hasMore.value = false
      }
    } else {
      ElMessage.error('加载更多订单失败: ' + response.message)
      currentPage.value-- // 回退页码
    }
  } catch (error) {
    console.error('加载更多订单失败:', error)
    ElMessage.error('加载更多订单失败')
    currentPage.value-- // 回退页码
  } finally {
    loadingMore.value = false
  }
}

// 滚动监听，实现自动加载
const handleScroll = () => {
  if (scrollTimeout) {
    clearTimeout(scrollTimeout)
  }
  
  scrollTimeout = setTimeout(() => {
    const scrollTop = window.pageYOffset || document.documentElement.scrollTop
    const windowHeight = window.innerHeight
    const documentHeight = document.documentElement.scrollHeight
    
    // 当滚动到距离底部100px时，自动加载更多
    if (scrollTop + windowHeight >= documentHeight - 100) {
      if (hasMore.value && !loadingMore.value && !loading.value) {
        loadMore()
      }
    }
  }, 200) // 200ms防抖
}

const loadCancelTimeLimit = async () => {
  try {
    const response = await publicApi.getSystemConfig()
    if (response.code === 200 && response.data) {
      const limit = response.data.cancel_time_limit
      if (limit) {
        cancelTimeLimit.value = parseInt(limit) || 4
      }
    }
  } catch (error) {
    console.error('获取取消时间限制失败:', error)
    // 使用默认值
    cancelTimeLimit.value = 4
  }
}

const loadVenueName = async () => {
  try {
    const response = await publicApi.getSystemConfig()
    if (response.code === 200 && response.data) {
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

const showQRCode = (order) => {
  selectedOrderNumber.value = order.orderNumber
  selectedOrderStatus.value = order.status
  qrCodeModalVisible.value = true
}
</script>

<style scoped>
.orders-container {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 80px;
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
  padding: 20px;
}

.loading-container {
  text-align: center;
  padding: 40px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.loading-container .el-icon {
  font-size: 24px;
  color: #FF6633;
  margin-bottom: 10px;
}

.empty-container {
  text-align: center;
  padding: 40px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.orders-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 懒加载相关样式 */
.load-more-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px 0;
  margin-top: 20px;
}

.loading-more {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  color: #606266;
}

.loading-more .el-icon {
  font-size: 20px;
  color: #FF6633;
}

.loading-more p {
  margin: 0;
  font-size: 14px;
}

.no-more-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px 0;
  margin-top: 20px;
  color: #909399;
  font-size: 14px;
}

.no-more-container p {
  margin: 0;
}

/* 移动端优化 */
@media (max-width: 768px) {
  .main-content {
    padding: 15px;
  }
}
</style> 