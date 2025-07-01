<template>
  <el-dialog
    :model-value="visible"
    @update:model-value="$emit('update:visible', $event)"
    :title="title"
    width="400px"
    :close-on-click-modal="false"
    :close-on-press-escape="true"
    center
  >
    <div class="qrcode-container">
      <div v-if="loading" class="loading-container">
        <el-loading-spinner />
        <p>生成二维码中...</p>
      </div>
      
      <div v-else-if="error" class="error-container">
        <el-icon class="error-icon"><Warning /></el-icon>
        <p>{{ error }}</p>
        <el-button @click="generateQRCode" type="primary" size="small">
          重试
        </el-button>
      </div>
      
      <div v-else class="qrcode-content">
        <div class="qrcode-image-container">
          <img 
            v-if="qrCodeDataURL" 
            :src="qrCodeDataURL" 
            :alt="`订单 ${orderNumber} 二维码`"
            class="qrcode-image"
          />
        </div>
        
        <div class="order-info">
          <p class="order-number">订单号：{{ orderNumber }}</p>
          <p class="order-status">
            <el-tag :type="getStatusType(status)" size="small">
              {{ getStatusText(status) }}
            </el-tag>
          </p>
        </div>
        
        <div class="qrcode-actions">
          <el-button 
            @click="downloadQRCode" 
            type="primary" 
            size="small"
            :icon="Download"
          >
            下载二维码
          </el-button>
          <el-button 
            @click="copyQRCode" 
            type="success" 
            size="small"
            :icon="CopyDocument"
          >
            复制二维码
          </el-button>
        </div>
      </div>
    </div>
    
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="closeModal">关闭</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Warning, Download, CopyDocument } from '@element-plus/icons-vue'
import { generateOrderQRCode, downloadQRCode as downloadQRCodeUtil, copyQRCodeToClipboard } from '@/utils/qrCodeUtils'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  orderNumber: {
    type: String,
    required: true
  },
  status: {
    type: String,
    default: 'pending'
  },
  title: {
    type: String,
    default: '订单二维码'
  }
})

const emit = defineEmits(['update:visible'])

const loading = ref(false)
const error = ref('')
const qrCodeDataURL = ref('')

// 监听visible变化，当显示时生成二维码
watch(() => props.visible, (newVal) => {
  if (newVal && props.orderNumber) {
    generateQRCode()
  }
})

// 监听订单号变化，重新生成二维码
watch(() => props.orderNumber, (newVal) => {
  if (props.visible && newVal) {
    generateQRCode()
  }
})

const generateQRCode = async () => {
  if (!props.orderNumber) {
    error.value = '订单号不能为空'
    return
  }
  
  try {
    loading.value = true
    error.value = ''
    
    // 生成二维码
    const dataURL = await generateOrderQRCode(props.orderNumber, {
      width: 250,
      margin: 3,
      color: {
        dark: '#FF6633',
        light: '#FFFFFF'
      }
    })
    
    qrCodeDataURL.value = dataURL
  } catch (err) {
    console.error('生成二维码失败:', err)
    error.value = '生成二维码失败，请重试'
  } finally {
    loading.value = false
  }
}

const downloadQRCode = () => {
  if (!qrCodeDataURL.value) {
    ElMessage.warning('二维码未生成')
    return
  }
  
  try {
    const filename = `订单${props.orderNumber}_二维码.png`
    downloadQRCodeUtil(qrCodeDataURL.value, filename)
    ElMessage.success('二维码下载成功')
  } catch (err) {
    console.error('下载二维码失败:', err)
    ElMessage.error('下载二维码失败')
  }
}

const copyQRCode = async () => {
  if (!qrCodeDataURL.value) {
    ElMessage.warning('二维码未生成')
    return
  }
  
  try {
    await copyQRCodeToClipboard(qrCodeDataURL.value)
    ElMessage.success('二维码已复制到剪贴板')
  } catch (err) {
    console.error('复制二维码失败:', err)
    ElMessage.error('复制二维码失败')
  }
}

const closeModal = () => {
  emit('update:visible', false)
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
</script>

<style scoped>
.qrcode-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-height: 300px;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 300px;
  gap: 15px;
}

.loading-container p {
  color: #606266;
  font-size: 14px;
}

.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 300px;
  gap: 15px;
}

.error-icon {
  font-size: 48px;
  color: #f56c6c;
}

.error-container p {
  color: #606266;
  font-size: 14px;
  text-align: center;
}

.qrcode-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  width: 100%;
}

.qrcode-image-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}

.qrcode-image {
  max-width: 250px;
  max-height: 250px;
  border-radius: 4px;
}

.order-info {
  text-align: center;
  width: 100%;
}

.order-number {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 10px 0;
}

.order-status {
  margin: 0;
}

.qrcode-actions {
  display: flex;
  gap: 10px;
  justify-content: center;
  width: 100%;
}

.dialog-footer {
  text-align: center;
}

/* 移动端优化 */
@media (max-width: 768px) {
  .qrcode-actions {
    flex-direction: column;
  }
  
  .qrcode-actions .el-button {
    width: 100%;
  }
  
  .qrcode-image {
    max-width: 200px;
    max-height: 200px;
  }
}
</style> 