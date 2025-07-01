<template>
  <el-dialog v-model:visible="visibleLocal" title="扫码录入订单号" width="350px" @close="onClose">
    <div class="scan-container">
      <div v-if="!scanning && !scanResult">
        <el-button type="primary" @click="startScan">开始扫码</el-button>
      </div>
      <div v-if="scanning" class="camera-preview">
        <div id="qr-video" style="width: 100%; height: 220px; background: #000;"></div>
        <el-button type="danger" @click="stopScan" style="margin-top: 10px;">停止扫码</el-button>
      </div>
      <div v-if="scanResult" class="scan-result">
        <el-alert type="success" :closable="false">扫码结果：{{ scanResult }}</el-alert>
        <el-button type="primary" @click="confirmResult" style="margin-top: 10px;">使用此订单号</el-button>
        <el-button @click="resetScan" style="margin-top: 10px; margin-left: 10px;">重新扫码</el-button>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, watch, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Html5Qrcode } from 'html5-qrcode'

const props = defineProps({
  visible: Boolean
})
const emits = defineEmits(['update:visible', 'scanned'])

const visibleLocal = ref(props.visible)
watch(() => props.visible, (val) => {
  visibleLocal.value = val
})
watch(visibleLocal, (val) => {
  if (!val) emits('update:visible', false)
})

const scanning = ref(false)
const scanResult = ref('')
let html5QrCode = null

const startScan = async () => {
  scanning.value = true
  scanResult.value = ''
  if (!html5QrCode) {
    html5QrCode = new Html5Qrcode("qr-video")
  }
  try {
    await html5QrCode.start(
      { facingMode: "environment" },
      { fps: 10, qrbox: 250 },
      (decodedText) => {
        scanResult.value = decodedText
        stopScan()
      },
      (errorMessage) => {
        // 可选：处理扫描错误
      }
    )
  } catch (e) {
    ElMessage.error('摄像头启动失败，请检查权限')
    scanning.value = false
  }
}

const stopScan = async () => {
  if (html5QrCode && scanning.value) {
    await html5QrCode.stop()
  }
  scanning.value = false
}

const confirmResult = () => {
  emits('scanned', scanResult.value)
  visibleLocal.value = false
  resetScan()
}

const resetScan = () => {
  scanResult.value = ''
  scanning.value = false
}

const onClose = () => {
  stopScan()
  visibleLocal.value = false
}

onUnmounted(() => {
  stopScan()
})
</script>

<style scoped>
.scan-container {
  text-align: center;
}
.camera-preview {
  margin: 10px 0;
}
.scan-result {
  margin-top: 20px;
}
</style> 