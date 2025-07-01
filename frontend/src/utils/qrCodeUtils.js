import QRCode from 'qrcode'

/**
 * 二维码工具函数
 */

/**
 * 生成二维码的Data URL
 * @param {string} text - 要编码的文本（订单号）
 * @param {Object} options - 二维码选项
 * @returns {Promise<string>} 二维码的Data URL
 */
export const generateQRCodeDataURL = async (text, options = {}) => {
  try {
    const defaultOptions = {
      width: 200,
      margin: 2,
      color: {
        dark: '#000000',
        light: '#FFFFFF'
      },
      errorCorrectionLevel: 'M'
    }
    
    const qrOptions = { ...defaultOptions, ...options }
    const dataURL = await QRCode.toDataURL(text, qrOptions)
    return dataURL
  } catch (error) {
    console.error('生成二维码失败:', error)
    throw new Error('生成二维码失败')
  }
}

/**
 * 生成二维码的Canvas元素
 * @param {string} text - 要编码的文本（订单号）
 * @param {Object} options - 二维码选项
 * @returns {Promise<HTMLCanvasElement>} 二维码Canvas元素
 */
export const generateQRCodeCanvas = async (text, options = {}) => {
  try {
    const defaultOptions = {
      width: 200,
      margin: 2,
      color: {
        dark: '#000000',
        light: '#FFFFFF'
      },
      errorCorrectionLevel: 'M'
    }
    
    const qrOptions = { ...defaultOptions, ...options }
    const canvas = await QRCode.toCanvas(text, qrOptions)
    return canvas
  } catch (error) {
    console.error('生成二维码Canvas失败:', error)
    throw new Error('生成二维码Canvas失败')
  }
}

/**
 * 生成订单二维码的Data URL
 * @param {string} orderNumber - 订单号
 * @param {Object} options - 二维码选项
 * @returns {Promise<string>} 二维码的Data URL
 */
export const generateOrderQRCode = async (orderNumber, options = {}) => {
  if (!orderNumber) {
    throw new Error('订单号不能为空')
  }
  
  // 可以在这里添加更多订单信息，比如订单号前缀
  const qrText = `${orderNumber}`
  
  return await generateQRCodeDataURL(qrText, options)
}

/**
 * 生成订单二维码的Canvas元素
 * @param {string} orderNumber - 订单号
 * @param {Object} options - 二维码选项
 * @returns {Promise<HTMLCanvasElement>} 二维码Canvas元素
 */
export const generateOrderQRCodeCanvas = async (orderNumber, options = {}) => {
  if (!orderNumber) {
    throw new Error('订单号不能为空')
  }
  
  // 可以在这里添加更多订单信息，比如订单号前缀
  const qrText = `ORDER:${orderNumber}`
  
  return await generateQRCodeCanvas(qrText, options)
}

/**
 * 下载二维码图片
 * @param {string} dataURL - 二维码的Data URL
 * @param {string} filename - 文件名
 */
export const downloadQRCode = (dataURL, filename = 'qrcode.png') => {
  try {
    const link = document.createElement('a')
    link.href = dataURL
    link.download = filename
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
  } catch (error) {
    console.error('下载二维码失败:', error)
    throw new Error('下载二维码失败')
  }
}

/**
 * 复制二维码到剪贴板
 * @param {string} dataURL - 二维码的Data URL
 * @returns {Promise<void>}
 */
export const copyQRCodeToClipboard = async (dataURL) => {
  try {
    // 将Data URL转换为Blob
    const response = await fetch(dataURL)
    const blob = await response.blob()
    
    // 复制到剪贴板
    await navigator.clipboard.write([
      new ClipboardItem({
        [blob.type]: blob
      })
    ])
  } catch (error) {
    console.error('复制二维码失败:', error)
    throw new Error('复制二维码失败')
  }
} 