/**
 * 时间工具函数
 */

/**
 * 格式化日期时间，显示到分钟精度
 * @param {string} dateString - 日期时间字符串
 * @returns {string} 格式化后的日期时间字符串
 */
export const formatDateTime = (dateString) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

/**
 * 格式化日期，显示完整日期时间
 * @param {string} dateString - 日期时间字符串
 * @returns {string} 格式化后的日期时间字符串
 */
export const formatDate = (dateString) => {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleString('zh-CN')
}

/**
 * 根据开始时间计算结束时间（每个场次1小时）
 * @param {string} startTime - 开始时间字符串
 * @returns {string} 结束时间字符串
 */
export const getEndTime = (startTime) => {
  if (!startTime) return null
  const endTime = new Date(startTime)
  endTime.setHours(endTime.getHours() + 1) // 每个场次1小时
  return endTime.toISOString()
}

/**
 * 格式化时间段显示（开始时间 - 结束时间）
 * @param {string} startTime - 开始时间字符串
 * @returns {string} 格式化的时间段字符串
 */
export const formatTimeRange = (startTime) => {
  if (!startTime) return '-'
  const start = formatDateTime(startTime)
  const end = formatDateTime(getEndTime(startTime))
  return `${start} - ${end}`
}

// 验证营业时间格式
export const validateBusinessHours = (businessHours) => {
  if (!businessHours) return false
  
  const pattern = /^([01]?[0-9]|2[0-3]):00-([01]?[0-9]|2[0-3]):00$/
  if (!pattern.test(businessHours)) return false
  
  const [startTime, endTime] = businessHours.split('-')
  const startHour = parseInt(startTime.split(':')[0])
  const endHour = parseInt(endTime.split(':')[0])
  
  return startHour < endHour
}

// 解析营业时间
export const parseBusinessHours = (businessHours) => {
  if (!businessHours) return { openTime: '09:00', closeTime: '21:00' }
  
  const hours = businessHours.split('-')
  if (hours.length !== 2) return { openTime: '09:00', closeTime: '21:00' }
  
  return {
    openTime: hours[0],
    closeTime: hours[1]
  }
}

// 格式化营业时间显示
export const formatBusinessHours = (businessHours) => {
  if (!businessHours) return '09:00-21:00'
  return businessHours
} 