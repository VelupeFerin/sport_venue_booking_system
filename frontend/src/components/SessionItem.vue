<template>
  <div class="session-item">
    <div class="session-header">
      <span class="session-number">场次 {{ sessionIndex + 1 }}</span>
      <span class="venue-court-name">{{ venueName }} {{ session.courtName }}</span>
    </div>
    <div class="session-details">
      <div class="session-info">
        <div class="time-info">
          <div class="time-range">
            <span class="time-label">场次时段</span>
            <span class="time-value">{{ formatTimeRange(session.startTime) }}</span>
          </div>
        </div>
        <div class="session-price">
          <span class="price-label">场次单价</span>
          <span class="price-value">¥{{ session.price }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { formatTimeRange } from '@/utils/timeUtils'

defineProps({
  session: {
    type: Object,
    required: true
  },
  sessionIndex: {
    type: Number,
    required: true
  },
  venueName: {
    type: String,
    default: '运动场馆'
  }
})
</script>

<style scoped>
.session-item {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 12px;
  background: #fafafa;
}

.session-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.session-number {
  font-weight: 600;
  color: #FF6633;
  font-size: 13px;
}

.venue-court-name {
  font-weight: 500;
  color: #303133;
  font-size: 14px;
}

.session-details {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.session-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
  flex: 1;
}

.time-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.time-range {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.time-label {
  color: #606266;
  font-size: 14px;
}

.time-value {
  color: #303133;
  font-size: 14px;
  font-weight: 500;
}

.session-price {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #303133;
  font-size: 14px;
}

.price-label {
  color: #606266;
  font-size: 14px;
}

.price-value {
  color: #FF6633;
  font-weight: 600;
}

/* 移动端优化 */
@media (max-width: 768px) {
  .session-info {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .time-info {
    width: 100%;
  }
  
  .time-range {
    justify-content: space-between;
  }
  
  .session-price {
    width: 100%;
  }
}
</style> 