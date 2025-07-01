<template>
  <div class="config-view">
    <div class="main-content">
      <div class="header">
        <h1>系统配置</h1>
        <p>管理系统基本配置和参数设置</p>
      </div>
      
      <div class="config-section">
        <div class="section-header">
          <h2>系统配置</h2>
        </div>
        
        <el-form :model="configForm" :rules="configRules" ref="configFormRef" label-width="150px" class="config-form">
          <el-form-item label="场馆名称" prop="venueName">
            <el-input v-model="configForm.venueName" placeholder="请输入场馆名称" />
            <div class="form-help">场馆的显示名称</div>
          </el-form-item>
          
          <el-form-item label="营业时间" prop="businessHours">
            <div class="time-range">
              <el-select 
                v-model="configForm.openTime" 
                placeholder="开始时间"
                style="width: 45%"
              >
                <el-option 
                  v-for="hour in 24" 
                  :key="hour - 1" 
                  :label="`${(hour - 1).toString().padStart(2, '0')}:00`" 
                  :value="`${(hour - 1).toString().padStart(2, '0')}:00`" 
                />
              </el-select>
              <span class="time-separator">至</span>
              <el-select 
                v-model="configForm.closeTime" 
                placeholder="结束时间"
                style="width: 45%"
              >
                <el-option 
                  v-for="hour in 24" 
                  :key="hour - 1" 
                  :label="`${(hour - 1).toString().padStart(2, '0')}:00`" 
                  :value="`${(hour - 1).toString().padStart(2, '0')}:00`" 
                />
              </el-select>
            </div>
            <div class="form-help">场馆的营业时间范围</div>
          </el-form-item>
          
          <el-form-item label="订单最大场次数" prop="maxOrderSessions">
            <el-input-number 
              v-model="configForm.maxOrderSessions" 
              :min="1" 
              :max="10"
              style="width: 100%"
              placeholder="请输入最大场次数"
            />
            <div class="form-help">单次订单最多可预订的场次数量</div>
          </el-form-item>
          
          <el-form-item label="退订时间要求(小时)" prop="cancelTimeLimit">
            <el-input-number 
              v-model="configForm.cancelTimeLimit" 
              :min="1" 
              :max="24"
              style="width: 100%"
              placeholder="请输入退订时间要求"
            />
            <div class="form-help">距开始时间多少小时内禁止退订</div>
          </el-form-item>
        </el-form>
        
        <div class="form-actions">
          <el-button @click="resetForm" size="large">重置</el-button>
          <el-button @click="saveConfig" type="primary" size="large">
            <el-icon><Check /></el-icon>
            保存配置
          </el-button>
        </div>
      </div>
    </div>
    
    <AdminNav />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Check } from '@element-plus/icons-vue'
import AdminNav from '@/components/AdminNav.vue'
import { getConfig, updateConfig } from '@/api/admin'
import { parseBusinessHours } from '@/utils/timeUtils'

const configFormRef = ref()

const configForm = ref({
  venueName: '',
  openTime: '00:00',
  closeTime: '23:00',
  maxOrderSessions: 5,
  cancelTimeLimit: 4
})

const configRules = {
  venueName: [
    { required: true, message: '请输入场馆名称', trigger: 'blur' }
  ],
  openTime: [
    { required: true, message: '请选择开始时间', trigger: 'change' }
  ],
  closeTime: [
    { required: true, message: '请选择结束时间', trigger: 'change' }
  ],
  maxOrderSessions: [
    { required: true, message: '请输入最大场次数', trigger: 'blur' },
    { type: 'number', min: 1, max: 10, message: '场次数必须在1-10之间', trigger: 'blur' }
  ],
  cancelTimeLimit: [
    { required: true, message: '请输入退订时间要求', trigger: 'blur' },
    { type: 'number', min: 1, max: 24, message: '时间要求必须在1-24小时之间', trigger: 'blur' }
  ]
}

onMounted(async () => {
  await loadConfig()
})

const loadConfig = async () => {
  try {
    const response = await getConfig()
    const config = response.data
    
    // 解析营业时间
    const businessHours = parseBusinessHours(config.business_hours)
    
    configForm.value = {
      venueName: config.venue_name || '运动场馆',
      openTime: businessHours.openTime,
      closeTime: businessHours.closeTime,
      maxOrderSessions: parseInt(config.max_order_sessions) || 5,
      cancelTimeLimit: parseInt(config.cancel_time_limit) || 4
    }
  } catch (error) {
    ElMessage.error('加载配置失败')
  }
}

const saveConfig = async () => {
  try {
    await configFormRef.value.validate()
    
    // 验证营业时间
    const openHour = parseInt(configForm.value.openTime.split(':')[0])
    const closeHour = parseInt(configForm.value.closeTime.split(':')[0])
    
    if (openHour >= closeHour) {
      ElMessage.error('结束时间必须晚于开始时间')
      return
    }
    
    const configData = {
      venue_name: configForm.value.venueName,
      business_hours: `${configForm.value.openTime}-${configForm.value.closeTime}`,
      max_order_sessions: configForm.value.maxOrderSessions.toString(),
      cancel_time_limit: configForm.value.cancelTimeLimit.toString()
    }
    
    await updateConfig(configData)
    ElMessage.success('配置保存成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('保存失败')
    }
  }
}

const resetForm = () => {
  loadConfig()
}
</script>

<style scoped>
.config-view {
  min-height: 100vh;
  background: #f5f7fa;
  padding-bottom: 80px;
}

.main-content {
  max-width: 600px;
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

.config-section {
  background: white;
  padding: 30px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.section-header {
  margin-bottom: 30px;
  padding-bottom: 15px;
  border-bottom: 2px solid #f0f0f0;
}

.section-header h2 {
  margin: 0;
  color: #303133;
  font-size: 18px;
  font-weight: 600;
}

.config-form {
  margin-bottom: 30px;
}

.time-range {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
}

.time-separator {
  color: #606266;
  font-weight: 500;
}

.form-help {
  margin-top: 8px;
  color: #909399;
  font-size: 12px;
  line-height: 1.4;
}

.form-actions {
  display: flex;
  gap: 15px;
  justify-content: center;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
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
  
  .config-section {
    padding: 20px;
  }
  
  .section-header h2 {
    font-size: 16px;
  }
  
  .time-range {
    flex-direction: column;
    gap: 10px;
  }
  
  .time-range .el-select {
    width: 100% !important;
  }
  
  .form-actions {
    flex-direction: column;
  }
}
</style> 