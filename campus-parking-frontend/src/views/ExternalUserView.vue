<template>
  <div class="external-user-view">
    <!-- 顶部导航栏 -->
    <header class="app-header">
      <div class="header-content">
        <div class="header-left">
          <h1 class="logo">🚗 校园停车管理系统</h1>
        </div>
        <div class="header-right">
          <div class="user-welcome">
            <span class="welcome-text">欢迎，</span>
            <span class="username">{{ username }}</span>
            <span class="user-role">（校外用户）</span>
          </div>
          <button class="logout-btn" @click="handleLogout">退出登录</button>
        </div>
      </div>
    </header>

    <!-- 主要内容区域 -->
    <main class="main-content">
      <!-- 用户概览卡片 -->
      <div class="overview-section">
        <div class="overview-card">
          <div class="user-info">
            <div class="avatar">👤</div>
            <div class="user-details">
              <h2>{{ username }}</h2>
              <p>校外用户 · 手机号: {{ userPhone || '未绑定' }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 数据统计 -->
      <div class="stats-section">
        <h3 class="section-title">数据概览</h3>
        <div class="stats-grid">
          <div class="stat-card">
            <div class="stat-icon total">
              <span>📊</span>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ parkingRecordsCount }}</div>
              <div class="stat-label">总停车记录</div>
            </div>
          </div>

          <div class="stat-card">
            <div class="stat-icon current">
              <span>📍</span>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ currentParkingCount }}</div>
              <div class="stat-label">当前停车</div>
            </div>
          </div>

          <div class="stat-card">
            <div class="stat-icon spent">
              <span>💰</span>
            </div>
            <div class="stat-content">
              <div class="stat-value">¥{{ formatCurrency(totalSpent) }}</div>
              <div class="stat-label">累计消费</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 功能服务 -->
      <div class="services-section">
        <h3 class="section-title">停车服务</h3>
        <div class="services-grid">
          <!-- 开始停车 -->
          <div class="service-card" @click="showTempParking">
            <div class="service-icon start">
              <span>🚗</span>
            </div>
            <div class="service-info">
              <h4>开始停车</h4>
              <p>输入车牌号开始新的停车</p>
            </div>
            <div class="service-action">
              <span class="arrow">→</span>
            </div>
          </div>

          <!-- 停车记录（改为跳转到新页面） -->
          <div class="service-card" @click="goToParkingRecords">
            <div class="service-icon records">
              <span>📋</span>
            </div>
            <div class="service-info">
              <h4>停车记录</h4>
              <p>查看历史停车记录和费用</p>
            </div>
            <div class="service-action">
              <span class="arrow">→</span>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- 临时停车弹窗 -->
    <div v-if="showTempParkingModal" class="modal-overlay" @click.self="showTempParkingModal = false">
      <div class="modal-content">
        <div class="modal-header">
          <h3>临时停车</h3>
          <button class="close-btn" @click="showTempParkingModal = false">×</button>
        </div>
        <div class="modal-body">
          <!-- 替换前：原来的手动输入车牌部分 -->
          <div class="form-group">
            <label for="plateNumber">车牌号码</label>
            <input
              id="plateNumber"
              v-model="tempParkingForm.plateNumber"
              type="text"
              placeholder="请输入车牌号，如：粤A12345"
              class="form-input"
              @input="validatePlateNumber"
            />
            <div v-if="plateNumberError" class="error-text">{{ plateNumberError }}</div>
          </div>

          <!-- 替换为：车牌识别上传组件 -->
          <div class="form-group">
            <label>车牌识别</label>

            <!-- 上传区域 -->
            <div class="upload-area" @click="triggerFileInput" :class="{ 'has-image': imagePreview }">
              <input
                ref="fileInput"
                type="file"
                accept="image/*"
                @change="handleImageUpload"
                hidden
              >

              <div v-if="!imagePreview" class="upload-placeholder">
                <span class="upload-icon">📁</span>
                <div class="upload-text">
                  <p>点击上传车牌照片</p>
                  <small>支持 JPG、PNG 格式，建议图片清晰</small>
                </div>
              </div>

              <div v-else class="preview-container">
                <img :src="imagePreview" alt="车牌预览" class="preview-image">
                <button @click.stop="clearUploadedImage" class="clear-preview-btn">×</button>
              </div>
            </div>

            <!-- 识别按钮 -->
            <button
              @click="recognizeFromImage"
              :disabled="!uploadedFile || isRecognizing"
              class="recognize-btn"
              style="margin-top: 10px; width: 100%;"
            >
              {{ isRecognizing ? '识别中...' : '识别车牌' }}
            </button>

            <!-- 识别结果显示 -->
            <div v-if="recognizedPlateNumber" class="recognition-result success">
              <span class="result-icon">✅</span>
              <span class="result-text">识别成功：</span>
              <strong class="plate-result">{{ recognizedPlateNumber }}</strong>
            </div>

            <div v-if="recognitionError" class="recognition-result error">
              <span class="result-icon">❌</span>
              <span class="result-text">{{ recognitionError }}</span>
            </div>

            <!-- 备用手动输入（识别失败时可用） -->
            <div v-if="showManualInput" class="manual-fallback" style="margin-top: 15px; padding-top: 15px; border-top: 1px solid #eee;">
              <p style="margin-bottom: 10px; color: #666; font-size: 0.9em;">识别失败？手动输入：</p>
              <input
                v-model="tempParkingForm.plateNumber"
                type="text"
                placeholder="请输入车牌号，如：粤A12345"
                class="form-input"
                @input="validatePlateNumber"
                style="margin-bottom: 5px;"
              />
              <div v-if="plateNumberError" class="error-text">{{ plateNumberError }}</div>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" @click="showTempParkingModal = false">取消</button>
          <button
            class="btn btn-primary"
            @click="handleTempParking"
            :disabled="!isFormValid || isSubmitting"
          >
            <span v-if="isSubmitting" class="loading"></span>
            {{ isSubmitting ? '处理中...' : '开始停车' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 充值弹窗 -->
    <div v-if="showRecharge" class="modal-overlay" @click.self="showRecharge = false">
      <div class="modal-content">
        <div class="modal-header">
          <h3>账户充值</h3>
          <button class="close-btn" @click="showRecharge = false">×</button>
        </div>
        <div class="modal-body">
          <div class="recharge-options">
            <div
              v-for="amount in rechargeAmounts"
              :key="amount"
              class="recharge-option"
              :class="{ 'selected': selectedAmount === amount }"
              @click="selectedAmount = amount"
            >
              <div class="option-amount">¥{{ amount }}</div>
            </div>
          </div>
          <div class="custom-amount">
            <label>自定义金额</label>
            <input
              v-model="customAmount"
              type="number"
              placeholder="输入其他金额"
              class="form-input"
              @input="handleCustomAmount"
              min="1"
              max="10000"
            />
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" @click="showRecharge = false">取消</button>
          <button class="btn btn-primary" @click="handleRecharge">
            确认充值 ¥{{ selectedAmount }}
          </button>
        </div>
      </div>
    </div>
  </div>
  <div class="external-user-view">
    <!-- [保持你原有的 template 代码不变，只修改临时停车弹窗部分] -->

    <!-- 临时停车弹窗 - 修改这部分 -->
    <div v-if="showTempParkingModal" class="modal-overlay" @click.self="showTempParkingModal = false">
      <div class="modal-content">
        <div class="modal-header">
          <h3>临时停车</h3>
          <button class="close-btn" @click="showTempParkingModal = false">×</button>
        </div>
        <div class="modal-body">
          <!-- 车牌识别上传区域 -->
          <div class="form-group">
            <label>车牌识别</label>

            <!-- 上传区域 -->
            <div class="upload-area" @click="triggerFileInput" :class="{ 'has-image': imagePreview }">
              <input
                ref="fileInput"
                type="file"
                accept="image/*"
                @change="handleImageUpload"
                hidden
              >

              <div v-if="!imagePreview" class="upload-placeholder">
                <span class="upload-icon">📁</span>
                <div class="upload-text">
                  <p>点击上传车牌照片</p>
                  <small>支持 JPG、PNG 格式，建议图片清晰</small>
                </div>
              </div>

              <div v-else class="preview-container">
                <img :src="imagePreview" alt="车牌预览" class="preview-image">
                <button @click.stop="clearUploadedImage" class="clear-preview-btn">×</button>
              </div>
            </div>

            <!-- 识别按钮 -->
            <button
              @click="recognizeFromImage"
              :disabled="!uploadedFile || isRecognizing"
              class="recognize-btn"
              style="margin-top: 10px; width: 100%;"
            >
              {{ isRecognizing ? '识别中...' : '识别车牌' }}
            </button>

            <!-- 识别结果显示 -->
            <div v-if="recognizedPlateNumber" class="recognition-result success">
              <span class="result-icon">✅</span>
              <span class="result-text">识别成功：</span>
              <strong class="plate-result">{{ recognizedPlateNumber }}</strong>
            </div>

            <div v-if="recognitionError" class="recognition-result error">
              <span class="result-icon">❌</span>
              <span class="result-text">{{ recognitionError }}</span>
            </div>

            <!-- 备用手动输入 -->
            <div v-if="showManualInput" class="manual-fallback" style="margin-top: 15px; padding-top: 15px; border-top: 1px solid #eee;">
              <p style="margin-bottom: 10px; color: #666; font-size: 0.9em;">识别失败？手动输入：</p>
              <input
                v-model="tempParkingForm.plateNumber"
                type="text"
                placeholder="请输入车牌号，如：粤A12345"
                class="form-input"
                @input="validatePlateNumber"
                style="margin-bottom: 5px;"
              />
              <div v-if="plateNumberError" class="error-text">{{ plateNumberError }}</div>
            </div>

            <!-- 如果未触发手动输入，也显示手动输入框作为备用 -->
            <div v-if="!showManualInput && !recognizedPlateNumber" class="manual-input-fallback" style="margin-top: 15px;">
              <input
                v-model="tempParkingForm.plateNumber"
                type="text"
                placeholder="或直接输入车牌号，如：粤A12345"
                class="form-input"
                @input="validatePlateNumber"
              />
              <div v-if="plateNumberError" class="error-text">{{ plateNumberError }}</div>
            </div>
          </div>

          <!-- 停车位选择（保持原样） -->
          <div class="form-group">
            <label for="spotSelect">选择停车位</label>
            <select
              id="spotSelect"
              v-model="tempParkingForm.spotId"
              class="form-select"
              required
            >
              <option value="">请选择停车位</option>
              <option v-for="spot in availableSpots" :key="spot.id" :value="spot.id">
                {{ spot.zone }} - {{ spot.spotNumber }} (¥{{ spot.rate || 5 }}/小时)
              </option>
            </select>
          </div>

          <!-- 信息提示（保持原样） -->
          <div class="info-box">
            <div class="info-item">
              <span class="info-icon">💡</span>
              <div class="info-content">
                <strong>停车费率</strong>
                <span>¥5/小时，每日封顶¥50</span>
              </div>
            </div>
            <div class="info-item">
              <span class="info-icon">⏰</span>
              <div class="info-content">
                <strong>计费规则</strong>
                <span>停车时间从确认开始后计算</span>
              </div>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" @click="showTempParkingModal = false">取消</button>
          <button
            class="btn btn-primary"
            @click="handleTempParking"
            :disabled="!isFormValid || isSubmitting"
          >
            <span v-if="isSubmitting" class="loading"></span>
            {{ isSubmitting ? '处理中...' : '开始停车' }}
          </button>
        </div>
      </div>
    </div>

    <!-- [其他弹窗和内容保持原样] -->
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import parkingApi from '@/utils/api' // 导入API模块
import { auth } from '@/utils/auth'
const router = useRouter()

// 用户信息
const username = ref('校外用户')
const userPhone = ref('')
const userBalance = ref(100.00)

// 页面状态
const showRecharge = ref(false)
const showTempParkingModal = ref(false)
const isSubmitting = ref(false)

// 数据
const availableSpots = ref([])
const selectedAmount = ref(50)
const customAmount = ref('')
const rechargeAmounts = [50, 100, 200, 500]

// 统计数据
const parkingRecordsCount = ref(0)
const currentParkingCount = ref(0)
const totalSpent = ref(0)

// 车牌识别相关状态
const uploadedFile = ref(null)
const imagePreview = ref('')
const recognizedPlateNumber = ref('')
const recognitionError = ref('')
const isRecognizing = ref(false)
const showManualInput = ref(false)
const fileInput = ref(null) // 文件输入引用

// 表单和验证
const tempParkingForm = ref({
  plateNumber: '',
  spotId: ''
})
const plateNumberError = ref('')

// 计算属性
const isFormValid = computed(() => {
  return tempParkingForm.value.plateNumber &&
    tempParkingForm.value.spotId &&
    !plateNumberError.value
})

// ==================== 车牌识别函数 ====================
const triggerFileInput = () => {
  fileInput.value?.click()
}

// ==================== 页面导航函数 ====================
const goToParkingRecords = () => {
  console.log('🚀 跳转到停车记录页面')
  router.push('/external-user/records')
}

const goToCurrentParking = () => {
  console.log('🚀 跳转到当前停车页面')
  router.push('/external-user/records?filter=current')
}

const handleImageUpload = (event) => {
  const file = event.target.files[0]
  if (!file) return

  // 验证文件类型和大小
  if (!file.type.startsWith('image/')) {
    recognitionError.value = '请上传图片文件'
    return
  }
  if (file.size > 5 * 1024 * 1024) {
    recognitionError.value = '图片大小不能超过5MB'
    return
  }

  uploadedFile.value = file
  imagePreview.value = URL.createObjectURL(file)
  recognizedPlateNumber.value = ''
  recognitionError.value = ''
  showManualInput.value = false
}

const recognizeFromImage = async () => {
  if (!uploadedFile.value) return

  isRecognizing.value = true
  recognitionError.value = ''

  try {
    console.log('🔍 开始识别，文件详情:', {
      name: uploadedFile.value.name,
      size: uploadedFile.value.size,
      type: uploadedFile.value.type
    })

    // 直接使用 fetch 测试，绕过 axios
    const formData = new FormData()
    formData.append('licenseImage', uploadedFile.value)

    const token = auth.getToken()
    console.log('🔑 Token:', token ? '存在' : '不存在')

    const response = await fetch('http://localhost:8081/api/parking/entry/ocr', {
      method: 'POST',
      headers: token ? {
        'Authorization': `Bearer ${token}`
        // 注意：不要设置 Content-Type，FormData 会自动设置
      } : {},
      body: formData
    })

    console.log('📡 响应状态:', response.status, response.statusText)

    const result = await response.json()
    console.log('📦 响应数据:', result)

    if (result.success) {
      recognizedPlateNumber.value = result.data
      tempParkingForm.value.plateNumber = result.data
      validatePlateNumber()
      showManualInput.value = false
    } else {
      recognitionError.value = result.message || '识别失败'
      showManualInput.value = true
    }
  } catch (error) {
    console.error('💥 识别异常:', error)
    recognitionError.value = error.message || '网络连接失败'
    showManualInput.value = true
  } finally {
    isRecognizing.value = false
  }
}

const clearUploadedImage = () => {
  uploadedFile.value = null
  imagePreview.value = ''
  recognizedPlateNumber.value = ''
  recognitionError.value = ''
  showManualInput.value = false
  tempParkingForm.value.plateNumber = ''
}

// ==================== 页面功能函数 ====================
const showTempParking = () => {
  showTempParkingModal.value = true
  // 尝试从本地存储获取上次使用的车牌号
  const lastPlateNumber = localStorage.getItem('lastPlateNumber')
  tempParkingForm.value = {
    plateNumber: lastPlateNumber || '',
    spotId: ''
  }
  plateNumberError.value = ''

  // 重置识别状态
  clearUploadedImage()
}

const initUserInfo = async () => {
  const storedUsername = localStorage.getItem('username')
  const storedPhone = localStorage.getItem('phone')

  if (storedUsername) {
    username.value = storedUsername
  }
  if (storedPhone) {
    userPhone.value = storedPhone
  }

  await refreshData()
}

const refreshData = async () => {
  await Promise.all([
    loadUserStatistics(),
    loadAvailableSpots(),
    loadCurrentParking()
  ])
}

// ==================== 数据加载函数（使用API模块） ====================
const loadUserStatistics = async () => {
  try {
    const response = await parkingApi.getUserStatistics()
    if (response.success) {
      const stats = response.data
      parkingRecordsCount.value = stats.totalRecords || 0
      totalSpent.value = stats.totalSpent || 0
      userBalance.value = stats.currentBalance || 100.00
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const loadCurrentParking = async () => {
  try {
    const response = await parkingApi.getCurrentParking()
    console.log('🔍 当前停车记录数据:', response)

    if (response.success && Array.isArray(response.data)) {
      // 只统计状态为 PARKING 的记录
      const currentParkingRecords = response.data.filter(record =>
        record.status === 'PARKING'
      )
      currentParkingCount.value = currentParkingRecords.length
      console.log('✅ 当前停车数量:', currentParkingCount.value)
    } else {
      currentParkingCount.value = 0
    }
  } catch (error) {
    console.error('💥 加载当前停车记录异常:', error)
    currentParkingCount.value = 0
  }
}

const loadAvailableSpots = async () => {
  try {
    console.log('🔄 加载可用停车位...')
    const response = await parkingApi.getAvailableSpots()

    console.log('🔍 停车位API响应:', response)

    // 处理不同的API响应格式
    if (Array.isArray(response)) {
      // 格式1: 直接返回数组
      availableSpots.value = response
      console.log('✅ 获取停车位成功 (直接数组):', availableSpots.value.length)
    } else if (response && Array.isArray(response.data)) {
      // 格式2: {success: true, data: [...]}
      availableSpots.value = response.data
      console.log('✅ 获取停车位成功 (包装格式):', availableSpots.value.length)
    } else if (response && response.success && Array.isArray(response.data?.content)) {
      // 格式3: 分页格式 {success: true, data: {content: [...]}}
      availableSpots.value = response.data.content
      console.log('✅ 获取停车位成功 (分页格式):', availableSpots.value.length)
    } else {
      // API返回格式不正确
      console.warn('停车位API返回格式不正确:', response)
      availableSpots.value = []
    }
  } catch (error) {
    console.error('💥 加载停车位失败:', error)
    availableSpots.value = []  // 失败时使用空数组
  }
}

// ==================== 停车业务函数（使用API模块） ====================
const handleTempParking = async () => {
  if (!isFormValid.value) return

  isSubmitting.value = true
  try {
    console.log('🚗 开始临时停车请求:', tempParkingForm.value)

    const response = await parkingApi.startTempParking(tempParkingForm.value)

    console.log('📡 停车响应:', response)

    if (response.success) {
      // 成功提示
      localStorage.setItem('lastPlateNumber', tempParkingForm.value.plateNumber)
      showSuccessMessage('停车开始成功！系统已开始计费')
      showTempParkingModal.value = false
      tempParkingForm.value = { plateNumber: '', spotId: '' }
      clearUploadedImage() // 清除识别状态
      await refreshData()
    } else {
      const errorMessage = response.message || '停车操作失败'
      console.error('❌ 停车失败详情:', errorMessage)

      // 友好的错误提示映射
      const errorMapping = {
        '用户信息不存在': '登录信息已过期，请重新登录',
        '请重新登录': '登录信息已过期，请重新登录',
        '停车位不存在': '选择的停车位不存在，请刷新页面重新选择',
        '停车位已被占用': '该停车位刚被其他用户占用，请选择其他可用车位',
        '已有车辆在停车中': '您已有车辆正在停车中，请先结束当前停车再开始新的停车',
        '车牌号当前正在停车中': '该车牌号当前正在停车中，请确认车牌号输入是否正确',
        '已注册为正式车辆': '该车牌号已注册为正式车辆，如需使用请联系管理员',
        '车牌号创建失败': '系统繁忙，请稍后重试',
        'Duplicate entry': '该车牌号暂不可用，请稍后重试或使用其他车牌号',
        '车牌号已存在': '该车牌号已存在，请使用其他车牌号'
      }

      // 查找匹配的错误提示
      let friendlyMessage = '停车失败，请检查信息后重试'
      for (const [key, value] of Object.entries(errorMapping)) {
        if (errorMessage.includes(key)) {
          friendlyMessage = value
          break
        }
      }

      showErrorMessage(friendlyMessage)
    }
  } catch (error) {
    console.error('💥 停车请求异常:', error)
    showErrorMessage(error.message || '网络连接异常，请检查网络后重试')
  } finally {
    isSubmitting.value = false
  }
}

// ==================== 工具函数（保持原样） ====================
const formatCurrency = (amount) => {
  if (amount === null || amount === undefined) return '0.00'
  if (typeof amount === 'object') {
    return Number(amount.toString()).toFixed(2)
  }
  return Number(amount).toFixed(2)
}

const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return '-'
  const date = new Date(dateTimeStr)
  return date.toLocaleString('zh-CN')
}

const calculateDuration = (startTime, endTime) => {
  if (!startTime) return '-'

  const start = new Date(startTime)
  const end = endTime ? new Date(endTime) : new Date()
  const diffMs = end - start

  const hours = Math.floor(diffMs / (1000 * 60 * 60))
  const minutes = Math.floor((diffMs % (1000 * 60 * 60)) / (1000 * 60))

  if (hours > 0) {
    return `${hours}小时${minutes}分钟`
  } else {
    return `${minutes}分钟`
  }
}

const getStatusText = (status) => {
  const statusMap = {
    'PARKING': '停车中',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return statusMap[status] || status || '未知状态'
}

const getStatusClass = (status) => {
  const statusClassMap = {
    'PARKING': 'parking',
    'COMPLETED': 'completed',
    'CANCELLED': 'cancelled'
  }
  return statusClassMap[status] || 'unknown'
}

const validatePlateNumber = () => {
  const plateNumber = tempParkingForm.value.plateNumber
  const plateRegex = /^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领A-Z]{1}[A-Z]{1}[A-Z0-9]{4,5}[A-Z0-9挂学警港澳]{1}$/

  if (!plateNumber) {
    plateNumberError.value = ''
  } else if (!plateRegex.test(plateNumber)) {
    plateNumberError.value = '车牌号格式不正确'
  } else {
    plateNumberError.value = ''
  }
}

const handleCustomAmount = () => {
  if (customAmount.value) {
    selectedAmount.value = parseInt(customAmount.value)
  }
}

// ==================== 消息提示函数 ====================
const showSuccessMessage = (message) => {
  alert(`✅ ${message}`)
}

const showErrorMessage = (message) => {
  alert(`❌ ${message}`)
}

// ==================== 支付相关函数 ====================
const endParking = async (recordId) => {
  if (!confirm('确定要结束停车吗？系统将计算停车费用并跳转到支付宝支付页面。')) {
    return
  }

  try {
    console.log('🔍 开始结束停车流程，记录ID:', recordId)

    // 1. 先结束停车，获取费用信息
    const record = await parkingApi.endParking(recordId)
    console.log('✅ 结束停车成功:', record)

    const fee = record.fee || record.data?.fee
    console.log('💰 停车费用:', fee)

    // 如果费用为0，直接完成
    if (!fee || fee === 0) {
      alert('停车结束！本次停车免费。')
      await refreshData()
      return
    }

    // 2. 创建支付宝支付订单
    console.log('🔄 创建支付宝支付订单...')
    const paymentResult = await parkingApi.createPayment({
      amount: fee,
      subject: '校园停车费',
      description: `停车记录 ${recordId}`,
      recordId: recordId
    })

    console.log('💰 支付订单创建结果:', paymentResult)

    if (paymentResult.success) {
      // 3. 处理支付宝返回的表单
      console.log('🚀 处理支付宝支付表单...')
      console.log('📄 支付宝返回的表单数据:', paymentResult.form)

      const tempDiv = document.createElement('div')
      tempDiv.innerHTML = paymentResult.form
      document.body.appendChild(tempDiv)

      const form = tempDiv.querySelector('form')
      if (form && form.action) {
        console.log('✅ 找到支付宝表单，准备提交...')
        console.log('📍 表单action:', form.action)

        form.style.display = 'none'
        form.style.visibility = 'hidden'
        form.style.position = 'absolute'
        form.style.top = '-1000px'
        document.body.appendChild(form)
        form.submit()
      } else {
        console.error('❌ 未找到有效的支付宝表单')
        alert('支付页面加载异常，请稍后重试或联系管理员。')
      }
    } else {
      console.error('❌ 创建支付订单失败:', paymentResult.message)

      if (confirm(`支付宝支付暂时不可用：${paymentResult.message}\n是否使用测试支付继续？`)) {
        await useTestPayment(recordId, fee)
      } else {
        alert(`停车结束！费用：¥${formatCurrency(fee)}。请稍后完成支付。`)
      }
    }
  } catch (error) {
    console.error('💥 结束停车异常:', error)
    alert('结束停车失败，请检查网络连接')
  }
}

const useTestPayment = async (recordId, fee) => {
  console.log('🔄 使用测试支付...')
  const testHtml = await parkingApi.testPayment({
    amount: fee,
    subject: '校园停车费',
    description: `停车记录 ${recordId}`,
    recordId: recordId
  })

  const newWindow = window.open('', '_blank', 'width=600,height=700')
  newWindow.document.write(testHtml)
  newWindow.document.close()
}

// ==================== 其他函数 ====================
const showPaymentHistory = () => {
  alert('消费记录功能开发中...')
}

const checkPaymentStatus = async (outTradeNo, recordId) => {
  try {
    const result = await parkingApi.checkPayment(outTradeNo)
    if (result.paid) {
      showSuccessMessage('支付成功！停车记录已完成。')
      await refreshData()
    }
  } catch (error) {
    console.error('检查支付状态失败:', error)
  }
}

// ==================== 生命周期和事件处理 ====================
onMounted(() => {
  initUserInfo()

  // 检查是否有支付返回参数
  const urlParams = new URLSearchParams(window.location.search)
  const recordId = urlParams.get('recordId')
  if (recordId) {
    console.log('🔄 支付返回，记录ID:', recordId)
    showSuccessMessage('支付处理完成！')
    refreshData()
  }
})

const handleRecharge = () => {
  userBalance.value += selectedAmount.value
  showRecharge.value = false
  alert(`充值成功！¥${selectedAmount.value} 已到账`)
  selectedAmount.value = 50
  customAmount.value = ''
}

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  localStorage.removeItem('phone')
  localStorage.removeItem('role')
  router.push('/login')
}
</script>

<style scoped>
.external-user-view {
  min-height: 100vh;
  background: #f8f9fa;
}
/*车牌识别开始*/
/* 车牌识别上传样式 */
.upload-area {
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
  padding: 30px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  background: #fafafa;
  margin-bottom: 10px;
}

.upload-area:hover {
  border-color: #3498db;
  background: #f0f7ff;
}

.upload-area.has-image {
  border-style: solid;
  padding: 15px;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.upload-icon {
  font-size: 2.5em;
  color: #bdc3c7;
}

.upload-text p {
  margin: 0 0 5px 0;
  font-weight: 500;
  color: #5a6c7d;
}

.upload-text small {
  color: #95a5a6;
  font-size: 0.85em;
}

.preview-container {
  position: relative;
}

.preview-image {
  max-width: 100%;
  max-height: 200px;
  border-radius: 6px;
  display: block;
  margin: 0 auto;
}

.clear-preview-btn {
  position: absolute;
  top: -10px;
  right: -10px;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #e74c3c;
  color: white;
  border: none;
  cursor: pointer;
  font-size: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
}

.recognize-btn {
  background: #27ae60;
  color: white;
  border: none;
  padding: 10px 15px;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s ease;
}

.recognize-btn:hover:not(:disabled) {
  background: #219653;
}

.recognize-btn:disabled {
  background: #95a5a6;
  cursor: not-allowed;
}

.recognition-result {
  margin-top: 10px;
  padding: 10px 15px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.95em;
}

.recognition-result.success {
  background: #d5edda;
  color: #155724;
  border: 1px solid #c3e6cb;
}

.recognition-result.error {
  background: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}

.result-icon {
  font-size: 1.1em;
}

.plate-result {
  font-size: 1.1em;
  letter-spacing: 1px;
}
/*车牌识别结束*/
/* 顶部导航栏 */
.app-header {
  background: white;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 70px;
}

.logo {
  font-size: 1.5em;
  font-weight: 700;
  color: #2c3e50;
  margin: 0;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-welcome {
  color: #5a6c7d;
  font-size: 0.95em;
}

.username {
  font-weight: 600;
  color: #2c3e50;
}

.user-role {
  color: #7f8c8d;
  font-size: 0.9em;
}

.logout-btn {
  background: #e74c3c;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s ease;
}

.logout-btn:hover {
  background: #c0392b;
  transform: translateY(-1px);
}

/* 主要内容区域 */
.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 30px 24px;
}

/* 用户概览 */
.overview-section {
  margin-bottom: 30px;
}

.overview-card {
  background: white;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 20px;
}

.avatar {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #3498db, #2980b9);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5em;
}

.user-details h2 {
  margin: 0 0 5px 0;
  color: #2c3e50;
  font-size: 1.5em;
}

.user-details p {
  margin: 0;
  color: #7f8c8d;
  font-size: 0.95em;
}

.balance-info {
  display: flex;
  align-items: center;
  gap: 20px;
}

.balance {
  text-align: right;
}

.balance .label {
  display: block;
  color: #7f8c8d;
  font-size: 0.9em;
  margin-bottom: 5px;
}

.balance .amount {
  font-size: 1.8em;
  font-weight: 700;
  color: #27ae60;
}

.recharge-btn {
  background: #3498db;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s ease;
}

.recharge-btn:hover {
  background: #2980b9;
  transform: translateY(-1px);
}

/* 区块标题 */
.section-title {
  font-size: 1.4em;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 20px 0;
}

/* 数据统计 */
.stats-section {
  margin-bottom: 40px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 25px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  gap: 20px;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.12);
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5em;
}

.stat-icon.total { background: #e3f2fd; }
.stat-icon.current { background: #fff3e0; }
.stat-icon.spent { background: #ffebee; }
.stat-icon.balance { background: #e8f5e8; }

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 2em;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 5px;
}

.stat-label {
  color: #7f8c8d;
  font-size: 0.95em;
}

/* 功能服务 */
.services-section {
  margin-bottom: 40px;
}

.services-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
}

.service-card {
  background: white;
  border-radius: 12px;
  padding: 25px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  gap: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid transparent;
}

.service-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.12);
  border-color: #3498db;
}

.service-icon {
  width: 50px;
  height: 50px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.3em;
}

.service-icon.start { background: #e3f2fd; }
.service-icon.records { background: #f3e5f5; }
.service-icon.current { background: #fff3e0; }
.service-icon.payment { background: #e8f5e8; }

.service-info {
  flex: 1;
}

.service-info h4 {
  margin: 0 0 5px 0;
  color: #2c3e50;
  font-size: 1.2em;
  font-weight: 600;
}

.service-info p {
  margin: 0;
  color: #7f8c8d;
  font-size: 0.9em;
}

.service-action .arrow {
  font-size: 1.2em;
  color: #bdc3c7;
  transition: all 0.3s ease;
}

.service-card:hover .service-action .arrow {
  transform: translateX(3px);
  color: #3498db;
}

/* 快速操作 */
.quick-actions {
  margin-bottom: 40px;
}

.action-buttons {
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
}

.action-btn {
  background: white;
  border: 2px solid #e9ecef;
  padding: 15px 25px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 500;
  color: #2c3e50;
}

.action-btn:hover {
  border-color: #3498db;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(52, 152, 219, 0.15);
}

.action-btn.primary {
  background: #3498db;
  border-color: #3498db;
  color: white;
}

.action-btn.primary:hover {
  background: #2980b9;
  border-color: #2980b9;
}

.btn-icon {
  font-size: 1.2em;
}

.btn-text {
  font-weight: 500;
}

/* 弹窗样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.modal-overlay.large {
  align-items: flex-start;
  padding-top: 50px;
}

.modal-content {
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  max-width: 500px;
  width: 100%;
  max-height: 90vh;
  overflow: hidden;
}

.modal-overlay.large .modal-content {
  max-width: 700px;
  max-height: 80vh;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 20px 25px;
  border-bottom: 1px solid #e9ecef;
}

.modal-title-section h3 {
  margin: 0 0 5px 0;
  font-size: 1.3em;
  font-weight: 600;
  color: #2c3e50;
}

.modal-subtitle {
  margin: 0;
  color: #7f8c8d;
  font-size: 0.9em;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5em;
  cursor: pointer;
  color: #7f8c8d;
  transition: color 0.3s ease;
  padding: 5px;
}

.close-btn:hover {
  color: #e74c3c;
}

/* 视图切换样式 */
.view-toggle {
  display: flex;
  border-bottom: 1px solid #e9ecef;
  padding: 0 25px;
}

.toggle-btn {
  flex: 1;
  background: none;
  border: none;
  padding: 15px 20px;
  cursor: pointer;
  font-weight: 500;
  color: #7f8c8d;
  border-bottom: 3px solid transparent;
  transition: all 0.3s ease;
}

.toggle-btn:hover {
  color: #3498db;
  background: #f8f9fa;
}

.toggle-btn.active {
  color: #3498db;
  border-bottom-color: #3498db;
  background: #f8f9fa;
}

.modal-body {
  padding: 25px;
  max-height: calc(90vh - 140px);
  overflow-y: auto;
}

.modal-overlay.large .modal-body {
  max-height: calc(80vh - 140px);
}

.modal-footer {
  padding: 20px 25px;
  border-top: 1px solid #e9ecef;
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

/* 表单样式 */
.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 600;
  color: #2c3e50;
}

.form-input, .form-select {
  width: 100%;
  padding: 12px 15px;
  border: 2px solid #e9ecef;
  border-radius: 8px;
  font-size: 1em;
  transition: all 0.3s ease;
  background: white;
}

.form-input:focus, .form-select:focus {
  outline: none;
  border-color: #3498db;
  box-shadow: 0 0 0 3px rgba(52, 152, 219, 0.1);
}

.error-text {
  color: #e74c3c;
  font-size: 0.85em;
  margin-top: 5px;
}

/* 信息框 */
.info-box {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 15px;
  margin-top: 20px;
}

.info-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 12px;
}

.info-item:last-child {
  margin-bottom: 0;
}

.info-icon {
  font-size: 1.1em;
  margin-top: 2px;
}

.info-content {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.info-content strong {
  color: #2c3e50;
  font-size: 0.9em;
}

.info-content span {
  color: #7f8c8d;
  font-size: 0.85em;
}

/* 按钮样式 */
.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s ease;
  font-size: 0.95em;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.btn-primary {
  background: #3498db;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #2980b9;
  transform: translateY(-1px);
}

.btn-secondary {
  background: #95a5a6;
  color: white;
}

.btn-secondary:hover {
  background: #7f8c8d;
}

.btn-outline {
  background: transparent;
  border: 2px solid #bdc3c7;
  color: #7f8c8d;
}

.btn-outline:hover {
  border-color: #95a5a6;
  color: #2c3e50;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none !important;
}

.btn-small {
  padding: 6px 12px;
  font-size: 0.85em;
}

.loading {
  width: 16px;
  height: 16px;
  border: 2px solid transparent;
  border-top: 2px solid currentColor;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 充值选项 */
.recharge-options {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin-bottom: 20px;
}

.recharge-option {
  background: #f8f9fa;
  border: 2px solid #e9ecef;
  border-radius: 8px;
  padding: 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.recharge-option:hover {
  border-color: #3498db;
}

.recharge-option.selected {
  background: #3498db;
  border-color: #3498db;
  color: white;
}

.option-amount {
  font-size: 1.2em;
  font-weight: 600;
}

.custom-amount {
  margin-top: 15px;
}

/* 停车记录样式 */
.records-container {
  min-height: 300px;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #7f8c8d;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #3498db;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 15px;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #7f8c8d;
}

.empty-icon {
  font-size: 4em;
  margin-bottom: 20px;
  opacity: 0.5;
}

.empty-state h4 {
  margin: 0 0 10px 0;
  color: #2c3e50;
}

.empty-state p {
  margin: 0 0 20px 0;
}

.records-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.record-item {
  background: #f8f9fa;
  border-radius: 10px;
  padding: 20px;
  transition: all 0.3s ease;
  border-left: 4px solid #bdc3c7;
}

.record-item:hover {
  background: #e9ecef;
}

.record-item.parking {
  border-left-color: #3498db;
  background: linear-gradient(135deg, #f8f9fa 0%, #e3f2fd 100%);
}

.record-item.parking:hover {
  background: linear-gradient(135deg, #e9ecef 0%, #bbdefb 100%);
}

.record-item.completed {
  border-left-color: #27ae60;
}

.record-item.cancelled {
  border-left-color: #e74c3c;
}

.record-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 15px;
}

.record-title {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.plate-number {
  font-size: 1.1em;
  font-weight: 600;
  color: #2c3e50;
}

.parking-lot {
  font-size: 0.9em;
  color: #7f8c8d;
}

.status-badge {
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 0.8em;
  font-weight: 600;
  white-space: nowrap;
}

.status-badge.parking {
  background: #e3f2fd;
  color: #1976d2;
}

.status-badge.completed {
  background: #e8f5e8;
  color: #2e7d32;
}

.status-badge.cancelled {
  background: #ffebee;
  color: #c62828;
}

.record-details {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 15px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.detail-item .label {
  color: #5a6c7d;
  font-size: 0.9em;
}

.detail-item .value {
  color: #2c3e50;
  font-size: 0.9em;
}

.detail-item .value.fee {
  color: #e74c3c;
  font-weight: 600;
}

.detail-item .value.calculating {
  color: #f39c12;
  font-style: italic;
}

.record-actions {
  display: flex;
  gap: 10px;
  padding-top: 15px;
  border-top: 1px solid #dee2e6;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header-content {
    padding: 0 16px;
    flex-direction: column;
    height: auto;
    gap: 15px;
    padding: 15px 16px;
  }

  .main-content {
    padding: 20px 16px;
  }

  .overview-card {
    flex-direction: column;
    gap: 20px;
    text-align: center;
  }

  .user-info {
    flex-direction: column;
    text-align: center;
  }

  .balance-info {
    flex-direction: column;
    gap: 15px;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .services-grid {
    grid-template-columns: 1fr;
  }

  .action-buttons {
    flex-direction: column;
  }

  .action-btn {
    justify-content: center;
  }

  .modal-content {
    margin: 10px;
  }

  .recharge-options {
    grid-template-columns: 1fr;
  }

  .record-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .record-actions {
    flex-direction: column;
  }

  .modal-footer {
    flex-direction: column;
  }

  .btn {
    width: 100%;
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .logo {
    font-size: 1.3em;
  }

  .header-right {
    flex-direction: column;
    gap: 10px;
    align-items: center;
  }

  .overview-card {
    padding: 20px;
  }

  .stat-card {
    padding: 20px;
  }

  .service-card {
    padding: 20px;
  }

  .modal-body {
    padding: 20px;
  }
}
</style>
