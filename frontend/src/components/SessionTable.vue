<template>
  <div class="session-table-container">
    <!-- 当没有场次数据时显示提示信息 -->
    <div v-if="!hasSessions" class="no-sessions">
      <el-empty description="暂无场次" :image-size="120">
        <template #image>
          <el-icon class="empty-icon"><Calendar /></el-icon>
        </template>
      </el-empty>
    </div>
    
    <!-- 有场次数据时显示表格 -->
    <div v-else class="table-wrapper">
      <table class="session-table">
        <thead>
          <tr>
            <th class="time-header">时间段</th>
            <th v-for="court in courts" :key="court" class="court-header">
              {{ court }}
            </th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="timeSlot in timeSlots" :key="timeSlot.key">
            <td class="time-name">{{ timeSlot.display }}</td>
            <td 
              v-for="court in courts" 
              :key="`${timeSlot.key}-${court}`"
              class="session-cell"
              :class="[
                getSessionClass(court, timeSlot.startTime),
                { 'admin-mode': isAdminMode }
              ]"
              @click="handleCellClick(court, timeSlot.startTime)"
            >
              <div class="session-content">
                <div class="session-status">
                  {{ getSessionStatus(court, timeSlot.startTime) }}
                </div>
                <div v-if="getSessionPrice(court, timeSlot.startTime)" class="session-price">
                  ¥{{ getSessionPrice(court, timeSlot.startTime) }}
                </div>
                <div v-if="getSessionNote(court, timeSlot.startTime)" class="session-note">
                  {{ getSessionNote(court, timeSlot.startTime) }}
                </div>
                <div v-if="isSessionSelected(court, timeSlot.startTime)" class="selected-indicator">
                  <el-icon><Check /></el-icon>
                </div>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Check, Calendar } from '@element-plus/icons-vue'

const props = defineProps({
  sessions: {
    type: Array,
    default: () => []
  },
  selectedSessions: {
    type: Array,
    default: () => []
  },
  isAdminMode: {
    type: Boolean,
    default: false
  },
  // 新增：营业时间配置
  businessHours: {
    type: Object,
    default: () => ({
      startHour: 0,
      endHour: 23
    })
  }
})

const emit = defineEmits(['session-select'])

// 判断是否有场次数据
const hasSessions = computed(() => {
  return props.sessions && props.sessions.length > 0
})

const timeSlots = computed(() => {
  // 完全依赖营业时间配置
  if (!props.businessHours || props.businessHours.startHour === undefined || props.businessHours.endHour === undefined) {
    return []
  }
  
  const minHour = props.businessHours.startHour
  const maxHour = props.businessHours.endHour
  
  const slots = []
  for (let hour = minHour; hour < maxHour; hour++) {
    const nextHour = hour + 1
    slots.push({
      key: hour,
      startTime: hour,
      display: `${hour.toString().padStart(2, '0')}:00-${nextHour.toString().padStart(2, '0')}:00`
    })
  }
  
  return slots
})

const courts = computed(() => {
  if (!props.sessions || props.sessions.length === 0) {
    return []
  }
  
  const courtSet = new Set(props.sessions.map(session => session.court_name))
  return Array.from(courtSet).sort()
})

const getSession = (courtName, startTime) => {
  if (!props.sessions || props.sessions.length === 0) {
    return null
  }
  
  return props.sessions.find(session => 
    session.court_name === courtName && 
    new Date(session.start_time).getHours() === startTime
  )
}

const getSessionStatus = (courtName, startTime) => {
  const session = getSession(courtName, startTime)
  if (!session) return '不可预约'
  if (session.is_booked) return '已预约'
  if (!session.is_active) return '不可预约'
  return '可预约'
}

const getSessionPrice = (courtName, startTime) => {
  const session = getSession(courtName, startTime)
  // 管理员模式下显示所有价格，用户模式下显示可预约和已预约的价格
  if (props.isAdminMode) {
    if (session) {
      return session.price || null
    } else {
      // 对于不存在的场次，检查是否有选中的虚拟场次
      const virtualSessionId = `virtual-${courtName}-${startTime}`
      const selectedVirtual = props.selectedSessions.find(selected => selected.id === virtualSessionId)
      return selectedVirtual?.price || null
    }
  }
  // 用户模式下显示可预约和已预约的价格，不显示不可预约的价格
  if (!session || !session.is_active) return null
  return session.price
}

const getSessionNote = (courtName, startTime) => {
  const session = getSession(courtName, startTime)
  if (session) {
    return session.note || null
  } else {
    // 对于不存在的场次，检查是否有选中的虚拟场次
    const virtualSessionId = `virtual-${courtName}-${startTime}`
    const selectedVirtual = props.selectedSessions.find(selected => selected.id === virtualSessionId)
    return selectedVirtual?.note || null
  }
}

const getSessionClass = (courtName, startTime) => {
  const session = getSession(courtName, startTime)
  
  // 首先检查是否被选中，如果被选中则统一使用selected样式
  if (isSessionSelected(courtName, startTime)) {
    return 'selected'
  }
  
  // 然后根据场次状态返回对应的样式
  if (!session) {
    return 'unavailable'
  }
  if (session.is_booked) return 'booked'
  if (!session.is_active) return 'unavailable'
  return 'available'
}

const isSessionSelected = (courtName, startTime) => {
  const session = getSession(courtName, startTime)
  if (session) {
    return props.selectedSessions.some(selected => selected.id === session.id)
  } else {
    // 对于不存在的场次，检查是否有对应的虚拟场次被选中
    const virtualSessionId = `virtual-${courtName}-${startTime}`
    return props.selectedSessions.some(selected => selected.id === virtualSessionId)
  }
}

const handleCellClick = (courtName, startTime) => {
  const session = getSession(courtName, startTime)
  // 管理员模式下所有场次都可以选中，用户模式下只有可预约的场次可以选中
  if (props.isAdminMode) {
    if (session) {
      emit('session-select', session)
    } else {
      // 对于不存在的场次，创建一个虚拟的session对象用于选中
      const virtualSession = {
        id: `virtual-${courtName}-${startTime}`,
        court_name: courtName,
        start_time: `2024-01-01T${startTime.toString().padStart(2, '0')}:00:00`,
        price: 0,
        is_active: false,
        is_booked: false,
        note: '',
        isVirtual: true
      }
      emit('session-select', virtualSession)
    }
  } else {
    if (session && session.is_active && !session.is_booked) {
      emit('session-select', session)
    }
  }
}
</script>

<style scoped>
.session-table-container {
  width: 100%;
  overflow-x: auto;
}

.no-sessions {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.empty-icon {
  font-size: 60px;
  color: #c0c4cc;
}

.table-wrapper {
  min-width: 100%;
}

.session-table {
  width: 100%;
  border-collapse: collapse;
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.session-table th,
.session-table td {
  border: 1px solid #e4e7ed;
  padding: 12px 8px;
  text-align: center;
  vertical-align: middle;
}

.session-table th {
  background: #f5f7fa;
  font-weight: 600;
  color: #333;
  font-size: 14px;
}

.time-header {
  min-width: 120px;
  background: #FF6633 !important;
  color: white !important;
}

.court-header {
  min-width: 100px;
  font-size: 12px;
}

.time-name {
  font-weight: 600;
  color: #333;
  background: #f8f9fa;
}

.session-cell {
  height: 80px;
  padding: 8px !important;
  position: relative;
  cursor: pointer;
  transition: all 0.3s ease;
}

.session-cell:hover {
  transform: scale(1.02);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.session-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
  height: 100%;
  justify-content: center;
  align-items: center;
  position: relative;
  padding: 4px;
}

.session-status {
  font-size: 14px; /* 可以调整这个值来改变字体大小 */
  font-weight: 500;
  padding: 2px 6px;
  border-radius: 4px;
  display: inline-block;
}

.session-price {
  font-size: 14px;
  font-weight: 600;
  color: #FF6633;
}

.session-note {
  font-size: 12px; /* 可以调整这个值来改变字体大小 */
  color: #666;
  line-height: 1.2;
  max-width: 100%;
  word-wrap: break-word;
  word-break: break-all;
  text-align: center;
  padding: 2px 4px;
  border-radius: 3px;
  margin: 0 auto;
}

.selected-indicator {
  position: absolute;
  top: 4px;
  right: 4px;
  background: #52c41a;
  color: white;
  border-radius: 50%;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  z-index: 10;
  box-shadow: 0 2px 4px rgba(82, 196, 26, 0.3);
}

.available {
  background: #f0f9ff;
}

.available .session-status {
  background: #e6f7ff;
  color: #1890ff;
}

.available:hover {
  background: #e6f7ff;
}

.selected {
  background: #f6ffed !important;
  position: relative;
  box-shadow: 0 0 0 1px #52c41a;
}

.selected .session-status {
  background: #d9f7be !important;
  color: #52c41a !important;
}

.selected:hover {
  background: #f6ffed !important;
  transform: scale(1.02);
  box-shadow: 0 2px 8px rgba(82, 196, 26, 0.2), 0 0 0 1px #52c41a;
}

.booked {
  background: #fff7e6;
}

.booked .session-status {
  background: #ffe7ba;
  color: #fa8c16;
}

.booked .session-price {
  color: #999;
  text-decoration: line-through;
}

.booked {
  cursor: not-allowed;
}

.booked:hover {
  transform: none;
  box-shadow: none;
}

.unavailable {
  background: #f5f5f5;
}

.unavailable .session-status {
  background: #d9d9d9;
  color: #666;
}

.unavailable .session-price {
  color: #999;
  font-style: italic;
}

.unavailable {
  cursor: not-allowed;
}

.unavailable:hover {
  transform: none;
  box-shadow: none;
}

/* 管理员模式下的不可预约场次样式 */
.session-cell.unavailable.admin-mode {
  cursor: pointer;
}

.session-cell.unavailable.admin-mode:hover {
  transform: scale(1.02);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  background: #f0f0f0;
}

/* 管理员模式下不可预约场次的价格样式 */
.session-cell.unavailable.admin-mode .session-price {
  color: #666;
  font-style: italic;
  opacity: 0.8;
}

/* 确保选中状态覆盖所有其他状态 */
.session-cell.selected {
  cursor: pointer !important;
}

.session-cell.selected:hover {
  transform: scale(1.02) !important;
  box-shadow: 0 2px 8px rgba(82, 196, 26, 0.2) !important;
}

/* 管理员模式下选中场次的特殊处理 */
.session-cell.selected.admin-mode {
  cursor: pointer !important;
}

.session-cell.selected.admin-mode:hover {
  transform: scale(1.02) !important;
  box-shadow: 0 2px 8px rgba(82, 196, 26, 0.2) !important;
}

@media (max-width: 768px) {
  .session-table th,
  .session-table td {
    padding: 8px 4px;
    font-size: 12px;
  }
  
  .time-header {
    min-width: 80px;
    font-size: 10px;
  }
  
  .court-header {
    min-width: 80px;
    font-size: 10px;
  }
  
  .session-cell {
    height: 60px;
    min-width: 80px;
    padding: 4px !important;
  }
  
  .session-status {
    font-size: 12px; /* 移动端场地状态字体大小 */
    padding: 1px 4px;
  }
  
  .session-price {
    font-size: 12px;
  }
  
  .session-note {
    font-size: 11px; /* 移动端场地备注字体大小 */
    max-width: 100%;
    padding: 1px 2px;
  }
  
  .selected-indicator {
    width: 16px;
    height: 16px;
    font-size: 10px;
    top: 2px;
    right: 2px;
    z-index: 10;
    box-shadow: 0 1px 3px rgba(82, 196, 26, 0.3);
  }
}
</style> 