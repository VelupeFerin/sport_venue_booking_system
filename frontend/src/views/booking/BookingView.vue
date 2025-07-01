<template>
  <div class="booking-container">
    <div class="header">
      <div class="header-content">
        <h1>场地预约</h1>
      </div>
    </div>
    
    <div class="main-content">
        <!-- 日期选择器 -->
        <div class="date-selector">
          <div 
            v-for="date in availableDates" 
            :key="date.value"
            class="date-item"
            :class="{ active: selectedDate === date.value }"
            @click="selectDate(date.value)"
          >
            <div class="date-month">{{ date.month }}</div>
            <div class="date-day">{{ date.day }}</div>
            <div class="date-week">{{ date.week }}</div>
          </div>
        </div>
        
        <div class="content-header">
          <h2>{{ selectedDate }} 场次安排</h2>
          <div class="legend">
            <div class="legend-item">
              <div class="legend-color available"></div>
              <span>可预约</span>
            </div>
            <div class="legend-item">
              <div class="legend-color booked"></div>
              <span>已预约</span>
            </div>
            <div class="legend-item">
              <div class="legend-color unavailable"></div>
              <span>不可预约</span>
            </div>
          </div>
        </div>
        
        <div class="session-table-wrapper">
          <SessionTable 
            :sessions="sessions" 
            :selected-sessions="selectedSessions"
            :business-hours="businessHoursConfig"
            @session-select="handleSessionSelect"
          />
        </div>
        
        <!-- 已登录用户显示预约操作 -->
        <div v-if="isLoggedIn" class="booking-actions">
          <!-- 未核验订单提示 -->
          <div v-if="hasPendingOrders" class="pending-order-notice">
            <el-alert
              title="您有未核验的订单"
              description="请等待商家核验完成后再进行新的预约"
              type="warning"
              :closable="false"
              show-icon
            >
              <template #default>
                <div class="notice-actions">
                  <el-button type="primary" size="small" @click="$router.push('/orders')">
                    查看订单
                  </el-button>
                </div>
              </template>
            </el-alert>
          </div>
          
          <div class="selected-info" v-if="selectedSessions.length > 0 && !hasPendingOrders">
            <span>已选择 {{ selectedSessions.length }} 个场次 (最多 {{ maxOrderSessions }} 个)</span>
            <el-button type="primary" @click="confirmBooking" :loading="confirming">
              确认预约
            </el-button>
          </div>
          
          <!-- 预约说明 -->
          <div class="booking-tips">
            <div class="tips-header">
              <h3>预约说明</h3>
            </div>
            <div class="tips-content">
              <ul>
                <li>点击可预约的场次进行选择</li>
                <li>已预约的场次不可重复预约</li>
                <li>不可预约的场次为系统维护或未开放时段</li>
                <li>价格单位为元/小时</li>
                <li>单次订单最多可选择 {{ maxOrderSessions }} 个场次</li>
                <li>有未核验订单时无法进行新的预约</li>
                <li>确认预约后将生成订单</li>
              </ul>
            </div>
          </div>
        </div>
        
        <!-- 未登录用户提示 -->
        <div v-else class="login-notice">
          <el-alert
            title="查看场次信息"
            description="您可以查看场次安排和价格信息。登录后可进行预约操作。"
            type="info"
            :closable="false"
            show-icon
          >
            <template #default>
              <div class="notice-actions">
                <el-button type="primary" size="small" @click="$router.push('/login')">
                  立即登录
                </el-button>
                <el-button type="success" size="small" @click="$router.push('/register')">
                  注册账号
                </el-button>
              </div>
            </template>
          </el-alert>
        </div>
    </div>
    
    <!-- 底部导航栏 -->
    <BottomNav />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import BottomNav from '../../components/BottomNav.vue'
import SessionTable from '../../components/SessionTable.vue'
import { useUserStore } from '../../store/user'
import userApi, { publicApi } from '../../api/user'
import { parseBusinessHours } from '../../utils/timeUtils'

const userStore = useUserStore()

// 检查是否已登录
const isLoggedIn = computed(() => userStore.isLoggedIn)

// 格式化日期为 YYYY-MM-DD 格式（使用本地时间）
const formatDateToLocalString = (date) => {
  const year = date.getFullYear()
  const month = (date.getMonth() + 1).toString().padStart(2, '0')
  const day = date.getDate().toString().padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 选中的日期
const selectedDate = ref('')

// 场次数据
const sessions = ref([])

// 选中的场次
const selectedSessions = ref([])

// 确认预约状态
const confirming = ref(false)

// 营业时间配置
const businessHours = ref({ startHour: 9, endHour: 21 })

// 最大订单场次数配置
const maxOrderSessions = ref(3)

// 是否有未核验订单
const hasPendingOrders = ref(false)

// 计算营业时间配置
const businessHoursConfig = computed(() => {
  return {
    startHour: businessHours.value.startHour,
    endHour: businessHours.value.endHour
  }
})

// 可用日期（今天和明天）
const availableDates = computed(() => {
  const dates = []
  const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  
  for (let i = 0; i < 2; i++) {
    const date = new Date()
    date.setDate(date.getDate() + i)
    
    const dateStr = formatDateToLocalString(date)
    const month = (date.getMonth() + 1).toString().padStart(2, '0')
    const day = date.getDate().toString().padStart(2, '0')
    const week = weekdays[date.getDay()]
    
    dates.push({
      value: dateStr,
      month: `${month}-${day}`,
      day: day,
      week: week
    })
  }
  
  return dates
})

// 初始化日期为今天
onMounted(() => {
  const today = new Date()
  selectedDate.value = formatDateToLocalString(today)
  loadBusinessHours() // loadBusinessHours内部会调用loadSessions
  checkPendingOrders() // 检查是否有未核验的订单
})

// 加载营业时间配置
const loadBusinessHours = async () => {
  try {
    // 使用公开API获取系统配置，这样未登录用户也能获取到
    const response = await publicApi.getSystemConfig()
    
    if (response.code === 200) {
      const config = response.data
      
      if (config.business_hours) {
        const parsedHours = parseBusinessHours(config.business_hours)
        const startHour = parseInt(parsedHours.openTime.split(':')[0])
        const endHour = parseInt(parsedHours.closeTime.split(':')[0])
        
        businessHours.value = {
          startHour: startHour,
          endHour: endHour
        }
      }
      
      // 加载最大订单场次数配置
      if (config.max_order_sessions) {
        maxOrderSessions.value = parseInt(config.max_order_sessions) || 3
      }
    }
  } catch (error) {
    console.warn('加载系统配置失败，使用默认值:', error)
    // 使用默认营业时间
    businessHours.value = { startHour: 9, endHour: 21 }
  }
  
  // 营业时间加载完成后，重新加载场次数据
  loadSessions()
}

// 检查是否有未核验的订单
const checkPendingOrders = async () => {
  if (!isLoggedIn.value) {
    return
  }
  
  try {
    const response = await userApi.checkPendingOrders()
    
    if (response.code === 200) {
      const orders = response.data || []
      // 检查是否有状态为pending的订单
      hasPendingOrders.value = orders.some(order => order.status === 'pending')
    }
  } catch (error) {
    console.warn('检查未核验订单失败:', error)
    // 不影响正常使用，使用默认值
    hasPendingOrders.value = false
  }
}

// 选择日期
const selectDate = (date) => {
  selectedDate.value = date
  selectedSessions.value = [] // 清空选中的场次
  loadSessions() // 重新加载场次数据
}

// 加载场次数据
const loadSessions = async () => {
  try {
    const response = await publicApi.getSessionsByDate(selectedDate.value)
    
    if (response.code === 200) {
      sessions.value = response.data || []
    } else {
      ElMessage.error('加载场次数据失败: ' + response.message)
      sessions.value = []
    }
  } catch (error) {
    console.error('加载场次数据失败:', error)
    ElMessage.error('加载场次数据失败，请稍后重试')
    sessions.value = []
  }
}

// 处理场次选择
const handleSessionSelect = (session) => {
  // 如果未登录，提示用户登录
  if (!isLoggedIn.value) {
    ElMessage.info('请先登录后再进行预约操作')
    return
  }
  
  // 如果有未核验的订单，禁止选择场次
  if (hasPendingOrders.value) {
    ElMessage.warning('您有未核验的订单，请等待核验完成后再进行新的预约')
    return
  }
  
  const index = selectedSessions.value.findIndex(s => s.id === session.id)
  if (index > -1) {
    selectedSessions.value.splice(index, 1)
  } else {
    // 检查是否超过最大场次数限制
    if (selectedSessions.value.length >= maxOrderSessions.value) {
      ElMessage.warning(`单次订单最多只能选择 ${maxOrderSessions.value} 个场次`)
      return
    }
    selectedSessions.value.push(session)
  }
}

// 确认预约
const confirmBooking = async () => {
  if (selectedSessions.value.length === 0) {
    ElMessage.warning('请先选择要预约的场次')
    return
  }
  
  // 检查是否有未核验的订单
  if (hasPendingOrders.value) {
    ElMessage.error('您有未核验的订单，请等待核验完成后再进行新的预约')
    return
  }
  
  // 再次检查场次数量限制（双重保险）
  if (selectedSessions.value.length > maxOrderSessions.value) {
    ElMessage.error(`单次订单最多只能选择 ${maxOrderSessions.value} 个场次`)
    return
  }
  
  confirming.value = true
  try {
    // 提取场次ID列表
    const sessionIds = selectedSessions.value.map(session => session.id)
    
    // 调用创建订单API
    const response = await userApi.createOrder(sessionIds)
    
    if (response.code === 200) {
      ElMessage.success('预约成功！订单已创建')
      selectedSessions.value = [] // 清空选中的场次
      loadSessions() // 重新加载场次数据以更新状态
    } else {
      ElMessage.error('预约失败: ' + response.message)
    }
  } catch (error) {
    console.error('预约失败:', error)
    ElMessage.error('预约失败，请稍后重试')
  } finally {
    confirming.value = false
  }
}
</script>

<style scoped>
.booking-container {
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
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
}

.date-selector {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  justify-content: center;
}

.date-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-width: 80px;
  height: 80px;
  border: 2px solid #e4e7ed;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: white;
}

.date-item:hover {
  border-color: #FF6633;
  background: #fff7f0;
}

.date-item.active {
  border-color: #FF6633;
  background: #FF6633;
  color: white;
}

.date-month {
  font-size: 14px;
  font-weight: 500;
  line-height: 1;
}

.date-day {
  font-size: 20px;
  font-weight: 600;
  line-height: 1;
  margin: 2px 0;
}

.date-week {
  font-size: 12px;
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 16px;
}

.content-header h2 {
  color: #FF6633;
  margin: 0;
  font-size: 24px;
  font-weight: 600;
}

.legend {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.legend-color {
  width: 16px;
  height: 16px;
  border-radius: 4px;
}

.legend-color.available {
  background: #e6f7ff;
  border: 1px solid #1890ff;
}

.legend-color.booked {
  background: #ffe7ba;
  border: 1px solid #fa8c16;
}

.legend-color.unavailable {
  background: #d9d9d9;
  border: 1px solid #666;
}

.session-table-wrapper {
  margin-bottom: 24px;
}

.booking-actions {
  margin-bottom: 24px;
}

.selected-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #f0f9ff;
  border: 1px solid #e6f7ff;
  border-radius: 8px;
  padding: 16px 20px;
  margin-bottom: 16px;
}

.selected-info span {
  color: #1890ff;
  font-weight: 500;
}

.booking-tips {
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}

.tips-header {
  padding: 16px 20px;
  border-bottom: 1px solid #e4e7ed;
}

.tips-header h3 {
  color: #FF6633;
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.tips-content {
  padding: 16px 20px;
}

.tips-content ul {
  margin: 0;
  padding-left: 20px;
}

.tips-content li {
  margin: 4px 0;
  color: #666;
  font-size: 14px;
}

.login-notice {
  margin-bottom: 24px;
}

.pending-order-notice {
  margin-bottom: 16px;
}

.notice-actions {
  display: flex;
  gap: 12px;
  margin-top: 12px;
}

@media (max-width: 768px) {
  .date-selector {
    gap: 8px;
  }
  
  .date-item {
    min-width: 60px;
    height: 60px;
  }
  
  .date-month {
    font-size: 12px;
  }
  
  .date-day {
    font-size: 16px;
  }
  
  .date-week {
    font-size: 10px;
  }
  
  .content-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .legend {
    justify-content: flex-start;
  }
  
  .selected-info {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
  
  .notice-actions {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style> 