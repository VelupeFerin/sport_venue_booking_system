<template>
  <div class="order-card">
    <div class="order-header">
      <div class="order-info">
        <span class="order-number">订单号：{{ order.orderNumber }}</span>
        <el-tag :type="getStatusType(order.status)" class="status-tag">
          {{ getStatusText(order.status) }}
        </el-tag>
      </div>
      <div class="order-price">
        <span class="price-label">订单总价：</span>
        <span class="price-value">¥{{ order.totalPrice }}</span>
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
        <SessionItem
          v-for="(session, index) in order.sessions"
          :key="index"
          :session="session"
          :session-index="index"
          :venue-name="venueName"
        />
      </div>
    </div>
    
    <div v-if="showActions" class="order-actions">
      <el-button 
        v-if="order.status === 'pending'" 
        type="danger" 
        size="small"
        @click="handleCancelOrder"
      >
        取消订单
      </el-button>
      <el-button 
        v-if="order.status === 'pending'" 
        type="warning" 
        size="small"
        @click="handleShowQRCode"
      >
        查看二维码
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ElMessage, ElMessageBox } from 'element-plus'
import SessionItem from './SessionItem.vue'
import { cancelUserOrder } from '@/api/user'

const props = defineProps({
  order: {
    type: Object,
    required: true
  },
  venueName: {
    type: String,
    default: '运动场馆'
  },
  cancelTimeLimit: {
    type: Number,
    default: 4
  },
  showActions: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['order-cancelled', 'show-qr-code'])

const handleCancelOrder = async () => {
  // 先检查是否可以取消订单
  if (!canCancelOrder()) {
    // 找出具体哪个场次导致无法取消
    const now = new Date()
    const problematicSessions = []
    
    for (const session of props.order.sessions) {
      const sessionStartTime = new Date(session.startTime)
      const hoursUntilStart = (sessionStartTime - now) / (1000 * 60 * 60)
      
      if (hoursUntilStart < props.cancelTimeLimit) {
        let timeDescription
        if (hoursUntilStart < 0) {
          timeDescription = '场次已开始'
        } else {
          timeDescription = `距开始时间不足${props.cancelTimeLimit}小时（当前剩余约${Math.ceil(hoursUntilStart)}小时）`
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
    message += `系统规定：距场次开始时间不足${props.cancelTimeLimit}小时时，无法取消订单。`
    
    ElMessageBox.alert(message, '无法取消订单', {
      confirmButtonText: '我知道了',
      type: 'warning',
      dangerouslyUseHTMLString: false
    })
    return
  }
  
  // 如果可以取消，则执行取消逻辑
  try {
    console.log('准备取消订单，订单信息:', props.order)
    console.log('订单ID (orderNumber):', props.order.orderNumber)
    
    await ElMessageBox.confirm(
      '确定要取消这个订单吗？',
      '确认取消', 
      {
        confirmButtonText: '确定取消',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const response = await cancelUserOrder(props.order.orderNumber)
    if (response.code === 200) {
      ElMessage.success('订单取消成功')
      emit('order-cancelled')
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

const handleShowQRCode = () => {
  emit('show-qr-code', props.order)
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
    case 'pending': return '待核验'
    case 'completed': return '已核验'
    case 'cancelled': return '已取消'
    default: return '未知'
  }
}

const formatDate = (dateString) => {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleString('zh-CN')
}

const formatDateTime = (dateString) => {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleString('zh-CN')
}

const canCancelOrder = () => {
  if (props.order.status !== 'pending') {
    return false
  }
  
  // 检查订单中的每个场次是否在取消时间限制内
  const now = new Date()
  
  for (const session of props.order.sessions) {
    const sessionStartTime = new Date(session.startTime)
    const hoursUntilStart = (sessionStartTime - now) / (1000 * 60 * 60)
    
    if (hoursUntilStart < props.cancelTimeLimit) {
      return false
    }
  }
  
  return true
}
</script>

<style scoped>
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
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  color: #303133;
}

.price-label {
  color: #606266;
  font-size: 14px;
}

.price-value {
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

.order-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  padding-top: 15px;
  border-top: 1px solid #f0f0f0;
}

/* 移动端优化 */
@media (max-width: 768px) {
  .order-card {
    padding: 15px;
  }
  
  .order-header {
    flex-direction: row;
    justify-content: space-between;
    align-items: center;
    gap: 10px;
  }
  
  .order-info {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .order-price {
    flex-direction: row;
    align-items: center;
    justify-content: flex-end;
    gap: 8px;
  }
  
  .order-actions {
    flex-direction: row;
    gap: 10px;
    justify-content: flex-end;
  }
  
  .order-actions .el-button {
    flex: 1;
    max-width: 70px;
  }
}
</style> 