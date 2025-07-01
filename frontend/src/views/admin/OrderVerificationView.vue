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
        <!-- 核验操作面板 -->
        <div class="detail-card">
          <h3>核验操作</h3>
          <div class="action-buttons">
            <el-button 
              v-if="!order.verifyTime && order.status === 'pending'" 
              @click="verifyOrder" 
              type="success" 
              size="default"
              class="primary-action"
            >
              <el-icon><Check /></el-icon>
              确认核验
            </el-button>
            <el-button 
              v-if="order.verifyTime && order.status === 'completed'" 
              type="success" 
              size="default"
              disabled
              class="status-button"
            >
              <el-icon><Check /></el-icon>
              订单已核验
            </el-button>
            <el-button 
              v-if="order.status === 'cancelled'" 
              type="info" 
              size="default" 
              disabled
            >
              订单已退订
            </el-button>
          </div>
        </div>
        
        <!-- 用户信息面板 -->
        <div class="detail-card">
          <h3>用户信息</h3>
          <div class="user-info-list" v-if="order.user">
            <div class="user-info-item">
              <span class="label">用户名:</span>
              <span class="value">{{ order.user.username }}</span>
            </div>
            <div class="user-info-item">
              <span class="label">手机号:</span>
              <span class="value">{{ order.user.phone }}</span>
            </div>
          </div>
          <div v-else class="no-data">
            <p>用户信息不存在</p>
          </div>
        </div>
        
        <!-- 订单信息面板 -->
        <div class="detail-card">
          <h3>订单信息</h3>
          <OrderCard
            :order="order"
            :venue-name="venueName"
            :cancel-time-limit="4"
            :show-actions="false"
          />
        </div>
      </div>
      
      <div v-else-if="searched" class="no-result">
        <el-empty description="未找到该订单" />
      </div>
    </div>
    
    <AdminNav />
    
    <ScanQRCodeModal
      v-model:visible="scanModalVisible"
      @scanned="handleScan"
    />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Check } from '@element-plus/icons-vue'
import AdminNav from '@/components/AdminNav.vue'
import ScanQRCodeModal from '@/components/ScanQRCodeModal.vue'
import OrderCard from '@/components/OrderCard.vue'
import { getOrderForVerification, verifyOrder as verifyOrderApi } from '@/api/admin'
import { formatDateTime, getEndTime } from '@/utils/timeUtils'

const orderNumber = ref('')
const order = ref(null)
const searched = ref(false)
const scanModalVisible = ref(false)
const venueName = ref('运动场馆') // 场馆名称

const searchOrder = async () => {
  if (!orderNumber.value.trim()) {
    ElMessage.warning('请输入订单号')
    return
  }
  
  try {
    const response = await getOrderForVerification(orderNumber.value)
    
    // 检查响应是否成功
    if (response.success) {
      // 后端返回的是 ApiResponse 包装的数据，需要访问 data 字段
      order.value = response.data
      searched.value = true
      ElMessage.success('订单查询成功')
    } else {
      // 当 success 为 false 时，显示后端返回的具体错误信息
      order.value = null
      searched.value = true
      ElMessage.error(response.message || '订单查询失败')
    }
  } catch (error) {
    console.error('订单查询失败:', error)
    order.value = null
    searched.value = true
    ElMessage.error(error.response?.data?.message || '订单查询失败')
  }
}

const verifyOrder = async () => {
  try {
    const response = await verifyOrderApi(orderNumber.value)
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



const handleScan = (scannedOrderNumber) => {
  orderNumber.value = scannedOrderNumber
  scanModalVisible.value = false
  searchOrder()
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

.user-info-list {
  display: flex;
  flex-direction: column;
}

.user-info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4px 0;
}

.label {
  font-weight: 500;
  color: #606266;
  min-width: 80px;
  text-align: left;
}

.value {
  color: #303133;
  font-size: 14px;
  text-align: right;
  flex: 1;
}

.price {
  color: #FF6633;
  font-weight: 600;
}

.action-buttons {
  display: flex;
  gap: 15px;
  justify-content: center;
  flex-wrap: wrap;
}

.action-buttons .el-button {
  width: 100%;
  min-width: auto;
  height: 40px;
  font-size: 14px;
  font-weight: 500;
  border-radius: 6px;
  transition: all 0.3s ease;
}

.primary-action {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(103, 194, 58, 0.3);
}

.primary-action:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(103, 194, 58, 0.4);
}

.status-button {
  background: #f0f9ff;
  border-color: #b3d8ff;
  color: #409eff;
  cursor: not-allowed;
}

.status-button:hover {
  background: #f0f9ff;
  border-color: #b3d8ff;
  color: #409eff;
  transform: none;
  box-shadow: none;
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
    min-width: 60px;
    text-align: left;
  }
  
  .value {
    font-size: 14px;
    text-align: right;
    flex: 1;
  }
  

  
  .action-buttons {
    flex-direction: column;
    gap: 12px;
  }
  
  .action-buttons .el-button {
    width: 100%;
    min-width: auto;
    height: 40px;
    font-size: 14px;
  }
  
  .status-info {
    text-align: center;
    justify-content: center;
  }
}


</style> 