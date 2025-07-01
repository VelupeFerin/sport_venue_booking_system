<template>
  <div class="orders-container">
    <div class="header">
      <div class="header-content">
        <h1>我的订单</h1>
      </div>
    </div>
    
    <div class="main-content">
      <div v-if="loading" class="loading-container">
        <el-loading-spinner />
        <p>加载中...</p>
      </div>
      
      <div v-else-if="orders.length === 0" class="empty-container">
        <el-empty description="暂无订单" />
        <el-button type="primary" @click="$router.push('/booking')">
          立即预约
        </el-button>
      </div>
      
      <div v-else class="orders-list">
        <div v-for="order in orders" :key="order.id" class="order-card">
          <div class="order-header">
            <div class="order-info">
              <span class="order-number">订单号：{{ order.orderNumber }}</span>
              <el-tag :type="getStatusType(order.status)" class="status-tag">
                {{ getStatusText(order.status) }}
              </el-tag>
            </div>
            <div class="order-price">
              ¥{{ order.totalPrice }}
            </div>
          </div>
          
          <div class="order-details">
            <div class="detail-row">
              <span class="label">创建时间：</span>
              <span class="value">{{ formatDate(order.createTime) }}</span>
            </div>
            <div v-if="order.verifyTime" class="detail-row">
              <span class="label">核验时间：</span>
              <span class="value">{{ formatDate(order.verifyTime) }}</span>
            </div>
          </div>
          
          <div class="sessions-section">
            <h4>场次信息</h4>
            <div class="sessions-list">
              <div v-for="(session, index) in order.sessions" :key="index" class="session-item">
                <div class="session-header">
                  <span class="session-number">场次 {{ index + 1 }}</span>
                </div>
                <div class="session-details">
                  <div class="session-info">
                    <span class="court-name">{{ session.courtName }}</span>
                    <div class="time-info">
                      <div class="time-row">
                        <span class="time-label">开始:</span>
                        <span class="time-value">{{ formatDateTime(session.startTime) }}</span>
                      </div>
                      <div class="time-row">
                        <span class="time-label">结束:</span>
                        <span class="time-value">{{ formatDateTime(getEndTime(session.startTime)) }}</span>
                      </div>
                    </div>
                    <span class="session-price">¥{{ session.price }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <div class="order-actions">
            <el-button 
              v-if="order.status === 'pending'" 
              type="danger" 
              size="small"
              @click="handleCancelOrder(order)"
            >
              取消订单
            </el-button>
            <el-button 
              v-if="order.status === 'pending'" 
              type="warning" 
              size="small"
              @click="showQRCode(order)"
            >
              查看二维码
            </el-button>
          </div>
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
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Promotion } from '@element-plus/icons-vue'
import BottomNav from '../../components/BottomNav.vue'
import QRCodeModal from '../../components/QRCodeModal.vue'
import { getUserOrders, cancelUserOrder, getBusinessHours } from '@/api/user'
import { formatDateTime, getEndTime } from '@/utils/timeUtils'

const orders = ref([])
const loading = ref(true)
const qrCodeModalVisible = ref(false)
const selectedOrderNumber = ref('')
const selectedOrderStatus = ref('pending')
const cancelTimeLimit = ref(4) // 默认4小时

onMounted(async () => {
  await Promise.all([
    loadOrders(),
    loadCancelTimeLimit()
  ])
})

const loadOrders = async () => {
  try {
    loading.value = true
    const response = await getUserOrders()
    if (response.code === 200) {
      orders.value = response.data || []
    } else {
      ElMessage.error('获取订单列表失败: ' + response.message)
      orders.value = []
    }
  } catch (error) {
    console.error('获取订单列表失败:', error)
    ElMessage.error('获取订单列表失败')
    orders.value = []
  } finally {
    loading.value = false
  }
}

const loadCancelTimeLimit = async () => {
  try {
    const response = await getBusinessHours()
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

const handleCancelOrder = async (order) => {
  // 先检查是否可以取消订单
  if (!canCancelOrder(order)) {
    // 找出具体哪个场次导致无法取消
    const now = new Date()
    const problematicSessions = []
    
    for (const session of order.sessions) {
      const sessionStartTime = new Date(session.startTime)
      const hoursUntilStart = (sessionStartTime - now) / (1000 * 60 * 60)
      
      if (hoursUntilStart < cancelTimeLimit.value) {
        let timeDescription
        if (hoursUntilStart < 0) {
          timeDescription = '场次已开始'
        } else {
          timeDescription = `距开始时间不足${cancelTimeLimit.value}小时（当前剩余约${Math.ceil(hoursUntilStart)}小时）`
        }
        
        problematicSessions.push({
          courtName: session.courtName,
          startTime: formatDateTime(session.startTime),
          timeDescription: timeDescription
        })
      }
    }
    
    // 构建提示信息
    let message = `无法取消订单，原因如下：\n\n`
    for (const session of problematicSessions) {
      message += `• ${session.courtName} (${session.startTime})\n`
      message += `  ${session.timeDescription}\n\n`
    }
    message += `系统规定：距场次开始时间不足${cancelTimeLimit.value}小时时，无法取消订单。`
    
    ElMessageBox.alert(message, '无法取消订单', {
      confirmButtonText: '我知道了',
      type: 'warning',
      dangerouslyUseHTMLString: false
    })
    return
  }
  
  // 如果可以取消，则执行取消逻辑
  try {
    console.log('准备取消订单，订单信息:', order)
    console.log('订单ID (orderNumber):', order.orderNumber)
    
    await ElMessageBox.confirm(
      '确定要取消这个订单吗？取消后相关场次将重新开放预订。', 
      '确认取消', 
      {
        confirmButtonText: '确定取消',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const response = await cancelUserOrder(order.orderNumber)
    if (response.code === 200) {
      ElMessage.success('订单取消成功')
      await loadOrders() // 重新加载订单列表
    } else {
      ElMessage.error('取消订单失败: ' + response.message)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消订单失败:', error)
      if (error.response && error.response.data && error.response.data.message) {
        ElMessage.error('取消订单失败: ' + error.response.data.message)
      } else {
        ElMessage.error('取消订单失败，请稍后重试')
      }
    }
  }
}

const showQRCode = (order) => {
  selectedOrderNumber.value = order.orderNumber
  selectedOrderStatus.value = order.status
  qrCodeModalVisible.value = true
}

const getStatusType = (status) => {
  switch (status) {
    case 'pending': return 'warning'
    case 'completed': return 'success'
    case 'cancelled': return 'info'
    default: return 'info'
  }
}

const getStatusText = (status) => {
  switch (status) {
    case 'pending': return '待确认'
    case 'completed': return '已确认'
    case 'cancelled': return '已取消'
    default: return '未知'
  }
}

const formatDate = (dateString) => {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleString('zh-CN')
}

const canCancelOrder = (order) => {
  if (order.status !== 'pending') {
    return false
  }
  
  // 检查订单中的每个场次是否在取消时间限制内
  const now = new Date()
  
  for (const session of order.sessions) {
    const sessionStartTime = new Date(session.startTime)
    const hoursUntilStart = (sessionStartTime - now) / (1000 * 60 * 60)
    
    if (hoursUntilStart < cancelTimeLimit.value) {
      return false
    }
  }
  
  return true
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

.order-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 15px;
  border-bottom: 1px solid #f0f0f0;
}

.order-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.order-number {
  font-weight: 600;
  color: #303133;
  font-size: 16px;
}

.status-tag {
  font-size: 12px;
}

.order-price {
  font-size: 18px;
  font-weight: 600;
  color: #FF6633;
}

.order-details {
  margin-bottom: 20px;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.label {
  color: #606266;
  font-size: 14px;
}

.value {
  color: #303133;
  font-size: 14px;
}

.sessions-section {
  margin-bottom: 20px;
}

.sessions-section h4 {
  margin: 0 0 15px 0;
  color: #303133;
  font-size: 16px;
  font-weight: 600;
}

.sessions-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.session-item {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 12px;
  background: #fafafa;
}

.session-header {
  margin-bottom: 8px;
}

.session-number {
  font-weight: 600;
  color: #FF6633;
  font-size: 13px;
}

.session-details {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.session-info {
  display: flex;
  align-items: flex-start;
  gap: 15px;
  flex: 1;
}

.court-name {
  font-weight: 500;
  color: #303133;
  min-width: 60px;
}

.time-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.time-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.time-label {
  color: #909399;
  font-size: 12px;
  min-width: 30px;
}

.time-value {
  color: #303133;
  font-size: 13px;
  font-weight: 500;
}

.session-price {
  color: #FF6633;
  font-weight: 600;
  font-size: 14px;
  min-width: 60px;
  text-align: right;
}

.order-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  padding-top: 15px;
  border-top: 1px solid #f0f0f0;
}

/* 移动端优化 */
@media (max-width: 768px) {
  .main-content {
    padding: 15px;
  }
  
  .order-card {
    padding: 15px;
  }
  
  .order-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .order-info {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .session-info {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .time-info {
    width: 100%;
  }
  
  .time-row {
    justify-content: space-between;
  }
  
  .session-price {
    align-self: flex-end;
  }
  
  .order-actions {
    flex-direction: column;
    gap: 8px;
  }
  
  .order-actions .el-button {
    width: 100%;
  }
}
</style> 