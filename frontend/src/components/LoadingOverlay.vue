<template>
  <div v-if="visible" class="loading-overlay">
    <div class="loading-content">
      <el-icon class="loading-icon" :size="32">
        <Loading />
      </el-icon>
      <p class="loading-text">{{ message }}</p>
    </div>
  </div>
</template>

<script setup>
import { Loading } from '@element-plus/icons-vue'

defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  message: {
    type: String,
    default: '正在验证权限...'
  }
})
</script>

<style scoped>
.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(255, 255, 255, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.loading-content {
  text-align: center;
  padding: 40px;
  border-radius: 12px;
  background: white;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.loading-icon {
  color: #FF6633;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

.loading-text {
  color: #666;
  margin: 0;
  font-size: 16px;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style> 