<template>
  <div class="verification-view">
    <div class="main-content">
      <div class="header">
        <h1>订单核验</h1>
        <p>输入订单号查看订单详情并进行核验</p>
      </div>
      
      <div class="search-section">
        <div class="search-box">
          <el-input
            v-model="orderNumber"
            placeholder="请输入订单号"
            clearable
            @keyup.enter="searchOrder"
          >
            <template #append>
              <el-button @click="searchOrder" type="primary">
                <el-icon><Search /></el-icon>
                查询
              </el-button>
              <el-button @click="() => scanModalVisible = true" type="success" style="margin-left: 8px;">
                扫码录入
              </el-button>
            </template>
          </el-input>
        </div>
      </div>
      
      <div v-if="order" class="order-details">
        <div class="detail-card">
          <h3>订单信息</h3>
          <div class="info-grid">
            <div class="info-item">
              <span class="label">订单号:</span>
              <span class="value">{{ order.orderNumber }}</span>
            </div>
            <div class="info-item">
              <span class="label">订单状态:</span>
              <span class="value">
                <el-tag :type="getStatusType(order.status)">
                  {{ getStatusText(order.status) }}
                </el-tag>
              </span>
            </div>
            <div class="info-item">
              <span class="label">创建时间:</span>
              <span class="value">{{ formatDate(order.createTime) }}</span>
            </div>
            <div v-if="order.verifyTime" class="info-item">
              <span class="label">核验时间:</span>
              <span class="value">{{ formatDate(order.verifyTime) }}</span>
            </div>
            <div class="info-item">
              <span class="label">订单金额:</span>
              <span class="value price">¥{{ order.totalPrice }}</span>
            </div>
          </div>
        </div>
        
        <div class="detail-card">
          <h3>用户信息</h3>
          <div class="info-grid" v-if="order.user">
            <div class="info-item">
              <span class="label">用户名:</span>
              <span class="value">{{ order.user.username }}</span>
            </div>
            <div class="info-item">
              <span class="label">手机号:</span>
              <span class="value">{{ order.user.phone }}</span>
            </div>
          </div>
          <div v-else class="no-data">
            <p>用户信息不存在</p>
          </div>
        </div>
        
        <div class="detail-card">
          <h3>场次信息</h3>
          <div v-if="order.sessions && order.sessions.length > 0" class="sessions-list">
            <div v-for="(session, index) in order.sessions" :key="index" class="session-item">
              <div class="session-header">
                <span class="session-number">场次 {{ index + 1 }}</span>
              </div>
              <div class="info-grid">
                <div class="info-item">
                  <span class="label">场地:</span>
                  <span class="value">{{ session.courtName }}</span>
                </div>
                <div class="info-item">
                  <span class="label">开始时间:</span>
                  <span class="value">{{ formatDateTime(session.startTime) }}</span>
                </div>
                <div class="info-item">
                  <span class="label">结束时间:</span>
                  <span class="value">{{ formatDateTime(getEndTime(session.startTime)) }}</span>
                </div>
                <div class="info-item">
                  <span class="label">价格:</span>
                  <span class="value price">¥{{ session.price }}</span>
                </div>
              </div>
            </div>
          </div>
          <div v-else class="no-data">
            <p>场次信息不存在</p>
          </div>
        </div>
        
        <div class="verification-actions">
          <h3>核验操作</h3>
          <div class="action-buttons">
            <el-button 
              v-if="!order.verifyTime && order.status === 'pending'" 
              @click="verifyOrder" 
              type="success" 
              size="large"
            >
              <el-icon><Check /></el-icon>
              确认核验
            </el-button>
            <el-button 
              v-if="!order.verifyTime && order.status === 'pending'" 
              @click="showQRCode" 
              type="warning" 
              size="large"
            >
              查看二维码
            </el-button>
            <div v-if="order.verifyTime && order.status === 'completed'" class="status-info">
              <el-icon><Check /></el-icon>
              <span>订单已核验</span>
            </div>
            <el-button 
              v-if="order.status === 'cancelled'" 
              type="info" 
              size="large" 
              disabled
            >
              订单已退订
            </el-button>
          </div>
        </div>
      </div>
      
      <div v-else-if="searched" class="no-result">
        <el-empty description="未找到该订单" />
      </div>
    </div>
    
    <AdminNav />
    
    <!-- 二维码模态框 -->
    <QRCodeModal
      v-model:visible="qrCodeModalVisible"
      :order-number="orderNumber"
      :status="order?.status || 'pending'"
      title="订单二维码"
    />
    <ScanQRCodeModal
      v-model:visible="scanModalVisible"
      @scanned="handleScan"
    />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Check, Promotion } from '@element-plus/icons-vue'
import AdminNav from '@/components/AdminNav.vue'
import QRCodeModal from '@/components/QRCodeModal.vue'
import ScanQRCodeModal from '@/components/ScanQRCodeModal.vue'
import { verifyOrder as verifyOrderApi } from '@/api/admin'
import { formatDateTime, getEndTime } from '@/utils/timeUtils'

const orderNumber = ref('')
const order = ref(null)
const searched = ref(false)
const qrCodeModalVisible = ref(false)
const scanModalVisible = ref(false)

const searchOrder = async () => {
  if (!orderNumber.value.trim()) {
    ElMessage.warning('请输入订单号')
    return
  }
  
  try {
    const response = await verifyOrderApi(orderNumber.value)
    console.log('订单查询返回：', response)
    console.log('response.data:', response.data)
    console.log('response.data.user:', response.data?.user)
    console.log('response.data.sessions:', response.data?.sessions)
    // 后端返回的是 ApiResponse 包装的数据，需要访问 data 字段
    order.value = response.data
    searched.value = true
    ElMessage.success('订单查询成功')
  } catch (error) {
    console.error('订单查询失败:', error)
    order.value = null
    searched.value = true
    ElMessage.error(error.response?.data?.message || '订单查询失败')
  }
}

const verifyOrder = async () => {
  try {
    const response = await verifyOrderApi(orderNumber.value, { verified: true })
    console.log('核验响应：', response)
    if (response.success) {
      // 核验成功后重新查询订单以获取最新状态
      await searchOrder()
      ElMessage.success('订单核验成功')
    } else {
      ElMessage.error(response.message || '核验失败')
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '核验失败')
  }
}

const showQRCode = () => {
  if (!order.value) {
    ElMessage.warning('请先查询订单')
    return
  }
  qrCodeModalVisible.value = true
}

const handleScan = (scannedOrderNumber) => {
  orderNumber.value = scannedOrderNumber
  scanModalVisible.value = false
  searchOrder()
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
    case 'cancelled': return '已退订'
    default: return '未知'
  }
}

const formatDate = (dateString) => {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleString('zh-CN')
}
</script>

<style scoped>
.verification-view {
  min-height: 100vh;
  background: #f5f7fa;
  padding-bottom: 80px;
}

.main-content {
  max-width: 800px;
  margin: 0 auto;
  padding: 40px 20px;
}

.header {
  text-align: center;
  margin-bottom: 30px;
  padding: 30px 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.header h1 {
  margin: 0 0 10px 0;
  color: #303133;
  font-size: 24px;
  font-weight: 600;
}

.header p {
  margin: 0;
  color: #606266;
  font-size: 16px;
}

.search-section {
  margin-bottom: 30px;
}

.search-box {
  background: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.order-details {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.detail-card {
  background: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.detail-card h3 {
  margin: 0 0 20px 0;
  color: #303133;
  font-size: 18px;
  font-weight: 600;
  border-bottom: 2px solid #f0f0f0;
  padding-bottom: 10px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 15px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.label {
  font-weight: 500;
  color: #606266;
  min-width: 70px;
}

.value {
  color: #303133;
  flex: 1;
  font-size: 14px;
}

.price {
  color: #FF6633;
  font-weight: 600;
}

.verification-actions {
  background: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.verification-actions h3 {
  margin: 0 0 20px 0;
  color: #303133;
  font-size: 18px;
  font-weight: 600;
  border-bottom: 2px solid #f0f0f0;
  padding-bottom: 10px;
}

.action-buttons {
  display: flex;
  gap: 10px;
}

.action-buttons .el-button {
  width: 100%;
}

.status-info {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #909399;
  font-size: 14px;
}

.status-info .el-icon {
  color: #67c23a;
}

.no-result {
  background: white;
  padding: 40px 20px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  text-align: center;
}

.no-data {
  text-align: center;
  color: #606266;
  font-size: 14px;
}

/* 移动端优化 */
@media (max-width: 768px) {
  .main-content {
    padding: 15px;
  }
  
  .header {
    padding: 20px 15px;
  }
  
  .header h1 {
    font-size: 20px;
  }
  
  .header p {
    font-size: 14px;
  }
  
  .search-box {
    margin-bottom: 20px;
  }
  
  .detail-card {
    padding: 15px;
    margin-bottom: 15px;
  }
  
  .detail-card h3 {
    font-size: 16px;
  }
  
  .info-grid {
    gap: 8px;
  }
  
  .info-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
  
  .label {
    font-size: 12px;
  }
  
  .value {
    font-size: 14px;
  }
  
  .action-buttons {
    flex-direction: column;
    gap: 10px;
  }
  
  .action-buttons .el-button {
    width: 100%;
  }
}

/* 场次列表样式 */
.sessions-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.session-item {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 15px;
  background: #fafafa;
}

.session-header {
  margin-bottom: 10px;
  padding-bottom: 8px;
  border-bottom: 1px solid #e4e7ed;
}

.session-number {
  font-weight: 600;
  color: #FF6633;
  font-size: 14px;
}
</style> 