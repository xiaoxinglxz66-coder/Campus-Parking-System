// src/utils/api.js
import request from './request'

const parkingApi = {
  // ==================== 车牌识别接口 ====================
  recognizeLicensePlate(imageFile) {
    const formData = new FormData()
    formData.append('licenseImage', imageFile)

    return request({
      url: '/api/parking/entry/ocr',
      method: 'post',
      data: formData,
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },

  // ==================== 停车业务接口 ====================

  // ✅ 校内用户专用停车接口
  campusStartParking(data) {
    return request({
      url: '/api/parking-records/start',  // 校内用户正确接口
      method: 'post',
      data: data
    })
  },

  // ✅ 校内用户实时停车接口
  campusRealTimeStartParking(data) {
    return request({
      url: '/api/parking-records/real-time/start',  // 校内用户实时接口
      method: 'post',
      data: data
    })
  },

  // ✅ 校外用户临时停车接口
  startTempParking(data) {
    return request({
      url: '/api/parking-records/external/temp-parking',
      method: 'post',
      data: data
    })
  },

  // ⚠️ 兼容性接口（给校外用户用）
  startParking(data) {
    console.warn('🚨 警告：/api/parking/start 接口不存在！')
    console.warn('🚨 正在重定向到正确的接口...')

    // 自动重定向到正确的校外用户接口
    return request({
      url: '/api/parking-records/external/temp-parking', // ✅ 正确的接口
      method: 'post',
      data: data
    })
  },

  // 结束停车
  endParking(recordId) {
    return request({
      url: `/api/parking-records/${recordId}/end`,
      method: 'post'
    })
  },

  // 取消停车
  cancelParking(recordId) {
    return request({
      url: `/api/parking-records/${recordId}/cancel`,
      method: 'post'
    })
  },

  // ==================== 数据查询接口 ====================
  getUserStatistics() {
    return request.get('/api/parking-records/external/statistics')
  },

  getCurrentParking() {
    return request.get('/api/parking-records/my-current')
  },

  getAvailableSpots() {
    return request.get('/api/parking-spots/available')
      .then(response => {
        console.log('🚗 停车位API原始响应:', response)
        return response
      })
      .catch(error => {
        console.error('停车位API错误:', error)
        throw error
      })
  },

  getParkingRecords(params = {}) {
    // 方案1：先尝试当前停车API，因为它存在
    return request.get('/api/parking-records/my-current')
      .then(response => {
        console.log('🔄 使用当前停车API获取记录')
        return response
      })
      .catch(error => {
        console.warn('当前停车API失败，尝试备选方案:', error)
        // 如果当前停车API失败，尝试其他可能的路径
        return request.get('/api/parking-records', { params })
      })
  },

  // ==================== 支付相关接口 ====================
  createPayment(data) {
    return request.post('/api/alipay/create', data)
  },

  testPayment(data) {
    return request.post('/api/alipay/test-payment', data)
  },

  checkPayment(outTradeNo) {
    return request.get(`/api/alipay/check-payment/${outTradeNo}`)
  },

  // ==================== 其他接口（根据你的代码添加） ====================
  // 充值接口（如果存在）
  recharge(data) {
    return request.post('/api/payment/recharge', data)
  }
}

// 默认导出（用于 import parkingApi from '@/utils/api'）
export default parkingApi

// 命名导出（用于按需导入）
export const {
  recognizeLicensePlate,
  campusStartParking,         // ✅ 新增
  campusRealTimeStartParking, // ✅ 新增
  startTempParking,
  startParking,
  endParking,
  cancelParking,
  getUserStatistics,
  getCurrentParking,
  getAvailableSpots,
  getParkingRecords,
  recharge,
  createPayment,
  testPayment,
  checkPayment
} = parkingApi
