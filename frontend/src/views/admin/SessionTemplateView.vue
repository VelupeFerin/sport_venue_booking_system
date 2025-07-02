<template>
  <div class="template-view">
    <div class="main-content">
      <div class="header">
        <h1>场次模板管理</h1>
        <p>配置场次模板，设置价格、开放时间和备注信息</p>
      </div>
      
      <div class="template-section">
        <div class="section-header">
          <h2>场次模板配置</h2>
          <div class="header-actions">
            <el-checkbox v-model="showAllTimeSlots" class="time-slots-checkbox">
              显示全部时段
            </el-checkbox>
            <div class="action-buttons">
              <el-button @click="showImportDialog = true" type="info" size="small">
                <el-icon><Upload /></el-icon>
                导入模板
              </el-button>
              <el-button @click="exportTemplates" type="success" size="small">
                <el-icon><Download /></el-icon>
                导出模板
              </el-button>
              <el-button @click="showAddCourtDialog = true" type="primary" size="small">
                <el-icon><Plus /></el-icon>
                添加场地
              </el-button>
            </div>
          </div>
        </div>
        
        <!-- 使用SessionTable组件展示模板 -->
        <div class="template-table-container">
          <SessionTable 
            :sessions="templateSessions" 
            :selected-sessions="selectedTemplates"
            :is-admin-mode="true"
            :business-hours="timeSlotsConfig"
            @session-select="handleTemplateSelect"
          />
        </div>
        
        <!-- 操作 -->
        <div v-if="selectedTemplates.length > 0" class="batch-actions">
          <h3>{{ selectedTemplates.length === 1 ? '操作' : '批量操作' }} (已选择 {{ selectedTemplates.length }} 个模板)</h3>
          <div class="batch-buttons">
            <el-button @click="batchEdit" type="primary">{{ selectedTemplates.length === 1 ? '编辑' : '批量编辑' }}</el-button>
          </div>
        </div>
        
        <!-- 操作提示 -->
        <div class="operation-tips">
          <p>💡 操作提示：</p>
          <ul>
            <li>单击场次可选择/取消选择</li>
            <li>选择场次后点击"批量编辑"进行编辑</li>
            <li>编辑单个场次时会自动加载当前信息</li>
            <li>编辑多个场次时可选择删除选中场次</li>
          </ul>
        </div>
      </div>
    </div>
    
    <!-- 添加场地对话框 -->
    <el-dialog 
      v-model="showAddCourtDialog" 
      title="添加场地"
      width="400px"
    >
      <el-form :model="courtForm" :rules="courtRules" ref="courtFormRef" label-width="100px">
        <el-form-item label="场地名称" prop="courtName">
          <el-input v-model="courtForm.courtName" placeholder="请输入场地名称" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showAddCourtDialog = false">取消</el-button>
        <el-button @click="saveCourt" type="primary">保存</el-button>
      </template>
    </el-dialog>
    
    <!-- 编辑场次模板对话框 -->
    <el-dialog 
      v-model="showEditDialog" 
      :title="editingTemplate && editingTemplate.selectedTemplates && editingTemplate.selectedTemplates.length === 1 ? '编辑场次模板' : '批量编辑场次模板'"
      width="500px"
    >
      <el-form :model="editForm" :rules="editRules" ref="editFormRef" label-width="100px">
        <!-- 单个场次编辑时显示正常表单 -->
        <template v-if="!isBatchEdit">
          <el-form-item label="价格" prop="price">
            <el-input-number 
              v-model="editForm.price" 
              :min="0" 
              :precision="2" 
              placeholder="请输入价格"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="状态">
            <el-switch v-model="editForm.isActive" />
          </el-form-item>
          <el-form-item label="备注">
            <el-input 
              v-model="editForm.note" 
              type="textarea" 
              :rows="3"
              placeholder="请输入备注信息（可选）"
            />
          </el-form-item>
        </template>
        
        <!-- 批量编辑时显示条件编辑表单 -->
        <template v-else>
          <el-form-item label="价格">
            <div v-if="!editPrice" class="edit-button-container">
              <el-button @click="editPrice = true" type="primary" size="small">
                <el-icon><Edit /></el-icon>
                编辑价格
              </el-button>
              <span class="current-value">{{ shouldEditPrice ? `当前：¥${editForm.price}` : '当前：不修改' }}</span>
            </div>
            <div v-else class="edit-field-container">
              <el-input-number 
                v-model="editForm.price" 
                :min="0" 
                :precision="2" 
                placeholder="请输入价格"
                style="width: 100%"
              />
              <div class="field-actions">
                <el-button @click="editPrice = false" size="small">取消</el-button>
                <el-button @click="confirmPriceEdit" type="primary" size="small">确定</el-button>
              </div>
            </div>
          </el-form-item>
          
          <el-form-item label="状态">
            <div v-if="!editStatus" class="edit-button-container">
              <el-button @click="editStatus = true" type="primary" size="small">
                <el-icon><Edit /></el-icon>
                编辑状态
              </el-button>
              <span class="current-value">{{ shouldEditStatus ? (editForm.isActive ? '当前：启用' : '当前：禁用') : '当前：不修改' }}</span>
            </div>
            <div v-else class="edit-field-container">
              <el-switch v-model="editForm.isActive" />
              <div class="field-actions">
                <el-button @click="editStatus = false" size="small">取消</el-button>
                <el-button @click="confirmStatusEdit" type="primary" size="small">确定</el-button>
              </div>
            </div>
          </el-form-item>
          
          <el-form-item label="备注">
            <div v-if="!editNote" class="edit-button-container">
              <el-button @click="editNote = true" type="primary" size="small">
                <el-icon><Edit /></el-icon>
                编辑备注
              </el-button>
              <span class="current-value">{{ shouldEditNote ? `当前：${editForm.note || '无'}` : '当前：不修改' }}</span>
            </div>
            <div v-else class="edit-field-container">
              <el-input 
                v-model="editForm.note" 
                type="textarea" 
                :rows="3"
                placeholder="请输入备注信息（可选）"
              />
              <div class="field-actions">
                <el-button @click="editNote = false" size="small">取消</el-button>
                <el-button @click="confirmNoteEdit" type="primary" size="small">确定</el-button>
              </div>
            </div>
          </el-form-item>
        </template>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer custom-footer">
          <el-button @click="saveEdit" type="primary">保存</el-button>
          <el-button @click="showEditDialog = false">取消</el-button>
          <el-button 
            v-if="editingTemplate && editingTemplate.selectedTemplates && editingTemplate.selectedTemplates.length > 0" 
            @click="handleDeleteTemplate" 
            type="danger"
          >
            删除选中场次
          </el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- 导入模板对话框 -->
    <el-dialog v-model="showImportDialog" title="导入模板" width="500px">
      <el-upload
        ref="uploadRef"
        :auto-upload="false"
        :on-change="handleFileChange"
        :show-file-list="false"
        accept=".json,.csv"
      >
        <el-button type="primary">选择文件</el-button>
        <template #tip>
          <div class="el-upload__tip">
            支持 JSON 和 CSV 格式，文件大小不超过 5MB<br>
            CSV格式：场地名称,开始时间(0-23),价格,是否启用(true/false),备注<br>
            时间格式：0-23（小时）或 HH:00 格式
          </div>
        </template>
      </el-upload>
      
      <template #footer>
        <el-button @click="showImportDialog = false">关闭</el-button>
      </template>
    </el-dialog>
    
    <AdminNav />
  </div>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Download, Plus, Upload, Edit} from '@element-plus/icons-vue'
import AdminNav from '@/components/AdminNav.vue'
import SessionTable from '@/components/SessionTable.vue'
import {createTemplate, deleteTemplate, getTemplates, updateTemplate} from '@/api/admin'
import {publicApi} from '@/api/user'
import {parseBusinessHours} from '@/utils/timeUtils'

const templates = ref([])
const selectedTemplates = ref([])
const showAddCourtDialog = ref(false)
const showImportDialog = ref(false)
const showEditDialog = ref(false)
const editingTemplate = ref(null)
const businessHours = ref({ startHour: 0, endHour: 23 })
const showAllTimeSlots = ref(false)

// 批量编辑相关状态
const editPrice = ref(false)
const editStatus = ref(false)
const editNote = ref(false)
const shouldEditPrice = ref(false)
const shouldEditStatus = ref(false)
const shouldEditNote = ref(false)

const courtFormRef = ref()
const editFormRef = ref()
const uploadRef = ref()

const courtForm = ref({
  courtName: ''
})

const editForm = ref({
  price: 0,
  isActive: true,
  note: ''
})

const courtRules = {
  courtName: [
    { required: true, message: '请输入场地名称', trigger: 'blur' }
  ]
}

const editRules = {
  price: [
    { required: true, message: '请输入价格', trigger: 'blur' }
  ]
}

// 将模板数据转换为SessionTable需要的格式
const templateSessions = computed(() => {
  return templates.value.map(template => {
    // 确保时间格式正确
    let startTime = template.start_time
    if (startTime && !startTime.includes('T')) {
      // 如果时间格式是 "HH:00:00"，转换为完整的日期时间
      startTime = `2024-01-01T${startTime}`
    }

    return {
      id: template.id,
      court_name: template.court_name, // 使用后端返回的下划线格式
      start_time: startTime, // 使用转换后的时间格式
      price: template.price,
      is_active: template.is_active, // 使用后端返回的下划线格式
      is_booked: false,
      note: template.note
    }
  })
})

// 计算营业时间范围
const businessHoursConfig = computed(() => {
  return {
    startHour: businessHours.value.startHour,
    endHour: businessHours.value.endHour
  }
})

// 计算时间范围配置（根据是否显示全部时段决定）
const timeSlotsConfig = computed(() => {
  if (showAllTimeSlots.value) {
    // 显示全部时段：0-24小时（包含23:00-24:00）
    return {
      startHour: 0,
      endHour: 24
    }
  } else {
    // 显示营业时间
    return businessHoursConfig.value
  }
})

// 判断是否为批量编辑模式
const isBatchEdit = computed(() => {
  return editingTemplate.value && 
         editingTemplate.value.selectedTemplates && 
         editingTemplate.value.selectedTemplates.length > 1
})

onMounted(async () => {
  await loadTemplates()
  await loadBusinessHours()
  document.title = '场次模板管理'
})

const loadTemplates = async () => {
  try {
    const response = await getTemplates()
    
    if (response.code === 200) {
      templates.value = response.data || []
    } else {
      ElMessage.error('加载模板失败: ' + response.message)
      templates.value = []
    }
  } catch (error) {
    console.error('加载模板失败:', error)
    ElMessage.error('加载模板失败')
    templates.value = []
  }
}

const loadBusinessHours = async () => {
  try {
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
    } else {
      console.warn('加载系统配置失败，使用默认值')
      businessHours.value = { startHour: 9, endHour: 21 }
    }
  } catch (error) {
    console.warn('加载系统配置失败，使用默认值:', error)
    businessHours.value = { startHour: 9, endHour: 21 }
  }
}

const handleTemplateSelect = (session) => {
  const index = selectedTemplates.value.findIndex(t => t.id === session.id)
  if (index > -1) {
    selectedTemplates.value.splice(index, 1)
  } else {
    selectedTemplates.value.push(session)
  }
}

const saveCourt = async () => {
  try {
    await courtFormRef.value.validate()
    
    // 为营业时间内的每个时间段创建模板
    const promises = []
    for (let hour = businessHours.value.startHour; hour < businessHours.value.endHour; hour++) {
      const startTime = `${hour.toString().padStart(2, '0')}:00:00`
      const templateData = {
        courtName: courtForm.value.courtName,
        startTime: startTime,
        price: 0, // 默认价格
        isActive: true, // 默认启用
        note: ''
      }
      promises.push(createTemplate(templateData))
    }
    
    await Promise.all(promises)
    ElMessage.success('场地添加成功')
    showAddCourtDialog.value = false
    courtForm.value.courtName = ''
    await loadTemplates()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('添加场地失败')
    }
  }
}

const batchEdit = () => {
  if (selectedTemplates.value.length === 0) {
    ElMessage.warning('请先选择要编辑的模板')
    return
  }
  
  // 设置编辑模式为批量编辑
  editingTemplate.value = { isBatchEdit: true, selectedTemplates: selectedTemplates.value }
  
  // 重置编辑状态
  editPrice.value = false
  editStatus.value = false
  editNote.value = false
  shouldEditPrice.value = false
  shouldEditStatus.value = false
  shouldEditNote.value = false
  
  // 如果只选中一个场次，自动加载其信息
  if (selectedTemplates.value.length === 1) {
    const template = selectedTemplates.value[0]
    editForm.value = {
      price: template.price || 0,
      isActive: template.is_active !== undefined ? template.is_active : true,
      note: template.note || ''
    }
  } else {
    // 多个场次时重置编辑表单
    editForm.value = {
      price: 0,
      isActive: true,
      note: ''
    }
  }
  
  showEditDialog.value = true
}

const exportTemplates = () => {
  const data = templates.value.map(template => ({
    courtName: template.court_name,
    startTime: template.start_time,
    price: template.price,
    isActive: template.is_active,
    note: template.note || ''
  }))
  
  const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `templates_${new Date().toISOString().split('T')[0]}.json`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
  
  ElMessage.success('模板导出成功')
}

const handleFileChange = (file) => {
  const reader = new FileReader()
  reader.onload = async (e) => {
    try {
      let importData = []
      
      if (file.name.endsWith('.json')) {
        importData = JSON.parse(e.target.result)
      } else if (file.name.endsWith('.csv')) {
        // 简单的CSV解析
        const lines = e.target.result.split('\n')
        importData = lines.slice(1).filter(line => line.trim()).map(line => {
          const values = line.split(',')
          let timeValue = values[1]
          // 确保时间格式为HH:00:00
          if (timeValue && !timeValue.includes(':')) {
            const hour = parseInt(timeValue)
            if (hour >= 0 && hour <= 23) {
              timeValue = `${hour.toString().padStart(2, '0')}:00:00`
            }
          } else if (timeValue && timeValue.includes(':') && !timeValue.includes(':')) {
            const parts = timeValue.split(':')
            if (parts.length >= 1) {
              const hour = parseInt(parts[0])
              if (hour >= 0 && hour <= 23) {
                timeValue = `${hour.toString().padStart(2, '0')}:00:00`
              }
            }
          }
          
          return {
            courtName: values[0],
            startTime: timeValue,
            price: parseFloat(values[2]),
            isActive: values[3] === 'true',
            note: values[4] || ''
          }
        })
      }
      
      // 导入数据
      if (importData.length > 0) {
        const promises = importData.map(data => createTemplate(data))
        await Promise.all(promises)
        ElMessage.success(`成功导入 ${importData.length} 个模板`)
        showImportDialog.value = false
        await loadTemplates()
      } else {
        ElMessage.error('文件格式错误或没有有效数据')
      }
    } catch (error) {
      ElMessage.error('文件格式错误')
    }
  }
  reader.readAsText(file.raw)
}

const saveEdit = async () => {
  try {
    // 单个场次编辑时进行表单验证
    if (!isBatchEdit.value) {
      await editFormRef.value.validate()
    }
    
    // 统一使用批量编辑模式
    const promises = editingTemplate.value.selectedTemplates.map(async (template) => {
      const templateData = {
        courtName: template.court_name,
        startTime: template.start_time.split('T')[1]
      }
      
      // 批量编辑时，根据条件决定是否更新各个字段
      if (isBatchEdit.value) {
        // 价格字段：如果修改了使用新值，否则使用原值
        if (shouldEditPrice.value) {
          templateData.price = editForm.value.price
        } else {
          templateData.price = template.price
        }
        
        // 状态字段：如果修改了使用新值，否则使用原值
        if (shouldEditStatus.value) {
          templateData.isActive = editForm.value.isActive
        } else {
          templateData.isActive = template.is_active
        }
        
        // 备注字段：如果修改了使用新值，否则使用原值
        if (shouldEditNote.value) {
          templateData.note = editForm.value.note
        } else {
          templateData.note = template.note
        }
      } else {
        // 单个场次编辑时，更新所有字段
        templateData.price = editForm.value.price
        templateData.isActive = editForm.value.isActive
        templateData.note = editForm.value.note
      }
      
      if (template.isVirtual) {
        // 虚拟场次，创建新场次
        return createTemplate(templateData)
      } else {
        // 现有场次，更新
        return updateTemplate(template.id, templateData)
      }
    })
    
    await Promise.all(promises)
    
    // 根据编辑的场次数量显示不同的成功消息
    const count = editingTemplate.value.selectedTemplates.length
    if (count === 1) {
      ElMessage.success('场次编辑成功')
    } else {
      ElMessage.success('批量编辑成功')
    }
    
    selectedTemplates.value = []
    showEditDialog.value = false
    editingTemplate.value = null
    // 重置编辑状态
    editPrice.value = false
    editStatus.value = false
    editNote.value = false
    shouldEditPrice.value = false
    shouldEditStatus.value = false
    shouldEditNote.value = false
    await loadTemplates()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('保存失败')
    }
  }
}

const confirmPriceEdit = () => {
  shouldEditPrice.value = true
  editPrice.value = false
}

const confirmStatusEdit = () => {
  shouldEditStatus.value = true
  editStatus.value = false
}

const confirmNoteEdit = () => {
  shouldEditNote.value = true
  editNote.value = false
}

const handleDeleteTemplate = async () => {
  try {
    // 显示删除确认对话框
    await ElMessageBox.confirm(
      `确定要删除选中的 ${editingTemplate.value.selectedTemplates.length} 个场次模板吗？`,
      '确认删除',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        dangerouslyUseHTMLString: false
      }
    )
    
    // 过滤出真实存在的场次（排除虚拟场次）
    const realTemplates = editingTemplate.value.selectedTemplates.filter(template => template.id !== null && !template.isVirtual)
    
    if (realTemplates.length === 0) {
      ElMessage.warning('选中的场次模板不存在或已被删除')
      showEditDialog.value = false
      editingTemplate.value = null
      selectedTemplates.value = []
      return
    }
    
    // 批量删除真实场次
    const promises = realTemplates.map(template => deleteTemplate(template.id))
    await Promise.all(promises)
    
    ElMessage.success(`成功删除 ${realTemplates.length} 个场次模板`)
    showEditDialog.value = false
    editingTemplate.value = null
    selectedTemplates.value = []
    await loadTemplates()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}
</script>

<style scoped>
.template-view {
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

.template-section {
  background: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 2px solid #f0f0f0;
}

.section-header h2 {
  margin: 0;
  color: #303133;
  font-size: 18px;
  font-weight: 600;
}

.header-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
  white-space: nowrap;
  align-items: center;
}

.time-slots-checkbox {
  margin-right: 8px;
}

.template-table-container {
  margin-bottom: 20px;
}

.batch-actions {
  margin-top: 20px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.batch-actions h3 {
  margin: 0 0 15px 0;
  color: #495057;
  font-size: 16px;
}

.batch-buttons {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.batch-buttons .el-button {
  min-width: 100px;
}

.dialog-footer {
  display: flex;
  flex-direction: row;
  justify-content: center;
  gap: 12px;
}

.dialog-footer .el-button {
  margin-left: 0;
}

.import-preview {
  margin-top: 20px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
}

.import-preview h4 {
  margin: 0 0 10px 0;
  color: #495057;
}

.preview-note {
  margin-top: 10px;
  font-size: 12px;
  color: #6c757d;
  text-align: center;
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
  
  .template-section {
    padding: 15px;
  }
  
  .section-header {
    flex-direction: column;
    gap: 15px;
    align-items: stretch;
  }
  
  .header-actions {
    gap: 12px;
    flex-direction: column;
    width: 100%;
  }
  
  .time-slots-checkbox {
    margin-right: 0;
    margin-bottom: 0;
    align-self: flex-start;
  }
  
  .action-buttons {
    display: flex;
    gap: 6px;
    width: 100%;
  }
  
  .action-buttons .el-button {
    flex: 1;
    font-size: 12px;
    padding: 8px 12px;
    min-width: 0;
  }
  
  .section-header h2 {
    font-size: 16px;
  }
  
  .batch-buttons {
    justify-content: center;
    gap: 8px;
  }
  
  .batch-buttons .el-button {
    min-width: 80px;
    font-size: 14px;
  }
  
  .dialog-footer {
    flex-direction: column;
    gap: 10px;
  }
  
  .dialog-footer .el-button {
    width: 100%;
  }
  
  .operation-tips {
    margin-top: 15px;
    padding: 12px;
  }
  
  .operation-tips p {
    font-size: 14px;
  }
  
  .operation-tips li {
    font-size: 12px;
    margin-bottom: 3px;
  }
  
  /* 移动端弹窗宽度优化 */
  :deep(.el-dialog) {
    width: 90% !important;
    max-width: 500px !important;
  }
}

.operation-tips {
  margin-top: 20px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.operation-tips p {
  margin: 0 0 10px 0;
  color: #495057;
  font-size: 16px;
  font-weight: 600;
}

.operation-tips ul {
  margin: 0;
  padding-left: 20px;
}

.operation-tips li {
  margin-bottom: 5px;
  color: #6c757d;
  font-size: 14px;
  line-height: 1.5;
}

/* 批量编辑相关样式 */
.edit-button-container {
  display: flex;
  align-items: center;
  gap: 10px;
}

.current-value {
  color: #909399;
  font-size: 12px;
}

.edit-field-container {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.field-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

.custom-footer {
  display: flex;
  flex-direction: row;
  justify-content: center;
  gap: 12px;
}
</style> 