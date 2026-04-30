<template>
  <div class="external-parking-records-view">
    <!-- 顶部导航栏 -->
    <header class="app-header">
      <div class="header-content">
        <div class="header-left">
          <button class="back-btn" @click="goBack">
            ← 返回
          </button>
          <h1 class="logo">停车记录</h1>
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
      <!-- 调试工具栏 -->
      <div class="debug-toolbar" v-if="debugMode">
        <button @click="toggleDebug" class="debug-btn">🔍 关闭调试</button>
        <button @click="testWithMockData" class="debug-btn">🧪 测试数据</button>
        <button @click="checkDataStructure" class="debug-btn">📊 检查数据</button>
        <span class="debug-status">调试模式已开启</span>
      </div>

      <!-- 页面标题和统计 -->
      <div class="page-header">
        <h2 class="page-title">停车记录详情</h2>
        <div class="page-stats">
          <div class="stat-badge">
            <span class="stat-label">总记录数</span>
            <span class="stat-value">{{ records.length }}</span>
          </div>
          <div class="stat-badge">
            <span class="stat-label">进行中</span>
            <span class="stat-value">{{ currentParkingCount }}</span>
          </div>
          <div class="stat-badge">
            <span class="stat-label">累计消费</span>
            <span class="stat-value">¥{{ formatCurrency(totalSpent) }}</span>
          </div>
        </div>
      </div>

      <!-- 数据状态提示 -->
      <div v-if="debugMode && records.length > 0" class="data-status">
        <h4>🔍 数据状态监控</h4>
        <div class="status-grid">
          <div class="status-item">
            <span class="label">数据来源:</span>
            <span class="value">{{ dataSource }}</span>
          </div>
          <div class="status-item">
            <span class="label">记录条数:</span>
            <span class="value">{{ records.length }}</span>
          </div>
          <div class="status-item">
            <span class="label">Vue响应式:</span>
            <span class="value">{{ isReactive ? '是' : '否' }}</span>
          </div>
          <div class="status-item">
            <span class="label">模板渲染:</span>
            <span class="value">{{ isRendered ? '是' : '否' }}</span>
          </div>
        </div>
        <button @click="logDataDetails" class="btn btn-small">📝 打印详情</button>
      </div>

      <!-- 筛选和搜索 -->
      <div class="filter-section">
        <div class="filter-left">
          <div class="filter-group">
            <label class="filter-label">状态筛选:</label>
            <select v-model="filters.status" class="filter-select" @change="applyFilters">
              <option value="">全部状态</option>
              <option value="PARKING">进行中</option>
              <option value="COMPLETED">已完成</option>
              <option value="CANCELLED">已取消</option>
            </select>
          </div>
        </div>
        <div class="filter-right">
          <button class="btn btn-secondary" @click="refreshData">
            🔄 刷新数据
          </button>
        </div>
      </div>

      <!-- 记录表格 -->
      <div class="records-table-container">
        <!-- 加载状态 -->
        <div v-if="loading" class="loading-state">
          <div class="spinner"></div>
          <p>加载停车记录中...</p>
        </div>

        <!-- 空状态 -->
        <div v-else-if="filteredRecords.length === 0" class="empty-state">
          <div class="empty-icon">🚗</div>
          <h4>暂无停车记录</h4>
          <p v-if="records.length === 0">开始您的第一次停车吧</p>
          <p v-else>当前筛选条件下没有匹配的记录</p>
          <div class="empty-actions">
            <button class="btn btn-primary" @click="goToParking">
              开始停车
            </button>
            <button v-if="records.length === 0 && debugMode" class="btn btn-secondary" @click="testWithMockData">
              使用测试数据
            </button>
          </div>

          <!-- 调试信息 -->
          <div v-if="debugMode" class="debug-info">
            <h5>🔍 调试信息</h5>
            <p>原始数据条数: {{ records.length }}</p>
            <p>筛选条件: {{ filters.status || '无' }}</p>
            <p>过滤后条数: {{ filteredRecords.length }}</p>
            <p v-if="records.length > 0">第一条数据预览: {{ JSON.stringify(records[0]).substring(0, 100) }}...</p>
          </div>
        </div>

        <!-- 记录表格 -->
        <div v-else class="records-table-wrapper">
          <div class="table-header-info">
            显示 {{ filteredRecords.length }} 条停车记录
            <span v-if="debugMode" class="debug-hint">（共 {{ records.length }} 条原始数据）</span>
          </div>

          <table class="records-table">
            <thead>
            <tr>
              <th>#</th>
              <th>车牌号</th>
              <th>停车区域</th>
              <th>开始时间</th>
              <th>结束时间</th>
              <th>停车时长</th>
              <th>停车费用</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
            </thead>
            <tbody>
            <tr
              v-for="(record, index) in filteredRecords"
              :key="record.id || index"
              :class="getStatusClass(record.status)"
              @click="debugMode ? selectRecord(record) : null"
              :style="debugMode ? 'cursor: pointer;' : ''"
            >
              <td class="index-cell">{{ index + 1 }}</td>
              <td class="plate-cell">
                <span class="plate-number">{{ record.plateNumber || '未知车牌' }}</span>
                <span v-if="debugMode" class="debug-id">ID: {{ record.id }}</span>
              </td>
              <td class="location-cell">
                {{ getParkingArea(record) }}
                <span v-if="record.parkingSpot?.spotNumber" class="spot-number">
                    ({{ record.parkingSpot.spotNumber }})
                  </span>
              </td>
              <td class="time-cell">{{ formatDateTime(record.startTime) }}</td>
              <td class="time-cell">
                {{ record.endTime ? formatDateTime(record.endTime) : '-' }}
              </td>
              <td class="duration-cell">
                {{ calculateDuration(record.startTime, record.endTime) }}
              </td>
              <td class="fee-cell" :class="{ 'calculating': record.status === 'PARKING' && !record.fee }">
                <template v-if="record.fee !== null && record.fee !== undefined">
                  ¥{{ formatCurrency(record.fee) }}
                </template>
                <template v-else-if="record.status === 'PARKING'">
                  计费中...
                </template>
                <template v-else>
                  -
                </template>
              </td>
              <td class="status-cell">
                  <span class="status-badge" :class="record.status?.toLowerCase()">
                    {{ getStatusText(record.status) }}
                  </span>
              </td>
              <td class="actions-cell">
                <template v-if="record.status === 'PARKING'">
                  <button class="btn-action end-parking" @click="endParking(record.id)">
                    结束停车
                  </button>
                </template>
                <button class="btn-action view-details" @click="viewRecordDetails(record)">
                  详情
                </button>
              </td>
            </tr>
            </tbody>
          </table>
        </div>
      </div>
    </main>

    <!-- 记录详情弹窗 -->
    <div v-if="showDetailModal" class="modal-overlay" @click.self="showDetailModal = false">
      <div class="modal-content large">
        <div class="modal-header">
          <h3>停车记录详情</h3>
          <button class="close-btn" @click="showDetailModal = false">×</button>
        </div>
        <div v-if="selectedRecord" class="modal-body">
          <div class="record-detail-view">
            <div class="detail-header">
              <div class="detail-title">
                <h4>车牌号: {{ selectedRecord.plateNumber }}</h4>
                <span class="status-badge" :class="selectedRecord.status?.toLowerCase()">
                  {{ getStatusText(selectedRecord.status) }}
                </span>
              </div>
              <div class="detail-meta">
                <span>记录ID: {{ selectedRecord.id }}</span>
                <span>创建时间: {{ formatDateTime(selectedRecord.createdTime) }}</span>
              </div>
            </div>

            <div class="detail-grid">
              <div class="detail-card">
                <h5>停车信息</h5>
                <div class="detail-item">
                  <span class="label">停车区域:</span>
                  <span class="value">{{ getParkingArea(selectedRecord) }}</span>
                </div>
                <div class="detail-item">
                  <span class="label">车位号:</span>
                  <span class="value">{{ selectedRecord.parkingSpot?.spotNumber || '未知' }}</span>
                </div>
                <div class="detail-item">
                  <span class="label">停车费率:</span>
                  <span class="value">¥{{ selectedRecord.parkingSpot?.rate || 5 }}/小时</span>
                </div>
              </div>

              <div class="detail-card">
                <h5>时间信息</h5>
                <div class="detail-item">
                  <span class="label">开始时间:</span>
                  <span class="value">{{ formatDateTime(selectedRecord.startTime) }}</span>
                </div>
                <div class="detail-item">
                  <span class="label">结束时间:</span>
                  <span class="value">{{ selectedRecord.endTime ? formatDateTime(selectedRecord.endTime) : '-' }}</span>
                </div>
                <div class="detail-item">
                  <span class="label">停车时长:</span>
                  <span class="value">{{ calculateDuration(selectedRecord.startTime, selectedRecord.endTime) }}</span>
                </div>
              </div>

              <div class="detail-card">
                <h5>费用信息</h5>
                <div class="detail-item">
                  <span class="label">停车费用:</span>
                  <span class="value fee">¥{{ formatCurrency(selectedRecord.fee) }}</span>
                </div>
                <div class="detail-item">
                  <span class="label">支付状态:</span>
                  <span class="value">
                    {{ selectedRecord.paymentStatus || (selectedRecord.fee > 0 ? '待支付' : '免费') }}
                  </span>
                </div>
                <div class="detail-item">
                  <span class="label">用户:</span>
                  <span class="value">{{ selectedRecord.userName || username }}</span>
                </div>
              </div>
            </div>

            <div v-if="debugMode" class="debug-details">
              <h5>🔍 原始数据</h5>
              <pre>{{ JSON.stringify(selectedRecord, null, 2) }}</pre>
            </div>

            <div v-if="selectedRecord.status === 'PARKING'" class="detail-actions">
              <button class="btn btn-primary" @click="endParking(selectedRecord.id)">
                结束停车
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useRoute } from 'vue-router'
import parkingApi from '@/utils/api'

const router = useRouter()
const route = useRoute()

// 调试模式
const debugMode = ref(true)

// 用户信息
const username = ref('校外用户')

// 数据状态
const loading = ref(false)
const records = ref([])
const dataSource = ref('未加载')
const currentParkingCount = ref(0)
const totalSpent = ref(0)

// 筛选条件
const filters = ref({
  status: ''
})

// 详情弹窗
const showDetailModal = ref(false)
const selectedRecord = ref(null)

// 计算属性
const filteredRecords = computed(() => {
  if (!filters.value.status) {
    return records.value
  }
  return records.value.filter(record => record.status === filters.value.status)
})

const isReactive = computed(() => {
  return records.value && typeof records.value === 'object' && '__v_isRef' in records
})

const isRendered = computed(() => {
  return records.value.length > 0
})

// 初始化
onMounted(() => {
  console.log('🚀 ExternalParkingRecords 组件已挂载')
  initUserInfo()
  loadRecords()
})

// 方法
const initUserInfo = () => {
  const storedUsername = localStorage.getItem('username')
  if (storedUsername) {
    username.value = storedUsername
  }
  console.log('👤 用户信息:', username.value)
}

const loadRecords = async () => {
  loading.value = true
  console.log('🔄 开始加载停车记录...')

  try {
    // 修改这里：使用正确的接口
    console.log('🔧 修改：使用 /api/parking-records/my-records 接口')
    const token = localStorage.getItem('token')

    const response = await fetch('http://localhost:8081/api/parking-records/my-records', {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    })

    if (response.ok) {
      const data = await response.json()
      console.log('📡 我的所有停车记录响应:', data)

      if (data.success && Array.isArray(data.data)) {
        records.value = data.data.map(transformRecordData)
        records.value.sort((a, b) => {
          const timeA = new Date(a.startTime || 0).getTime()
          const timeB = new Date(b.startTime || 0).getTime()
          return timeB - timeA // 最新的在前
        })
        dataSource.value = 'my-records API'
        updateStatistics()
        console.log('✅ 获取所有停车记录成功')
      }
    }
  } catch (error) {
    console.error('💥 加载失败:', error)
  } finally {
    loading.value = false
  }
}

// 数据转换函数
const transformRecordData = (record) => {
  console.log('🔄 转换原始记录:', record)

  // 尝试匹配不同的字段名
  const transformed = {
    // ID 字段
    id: record.id || record.recordId || record.parkingRecordId || Math.random(),

    // 车牌号字段
    plateNumber: record.plateNumber || record.licensePlate || record.carNumber ||
      record.vehicleNumber || record.plate || '未知车牌',

    // 停车区域字段
    parkingLotName: record.parkingLotName || record.parkingArea || record.zone ||
      record.area || record.location || '未知区域',

    // 停车位信息
    parkingSpot: {
      zone: record.zone || record.parkingZone || record.area ||
        record.parkingSpot?.zone || '未知区',
      spotNumber: record.spotNumber || record.parkingSpotNumber ||
        record.spot || record.parkingSpot?.spotNumber || '未知',
      rate: record.rate || record.feeRate || record.parkingSpot?.rate || 5
    },

    // 时间字段
    startTime: record.startTime || record.beginTime || record.entryTime ||
      record.startDate || record.createdAt,
    endTime: record.endTime || record.finishTime || record.exitTime ||
      record.endDate || record.completedAt,

    // 状态字段
    status: (record.status || record.recordStatus || 'UNKNOWN').toUpperCase(),

    // 费用字段
    fee: record.fee || record.totalFee || record.amount || record.parkingFee,

    // 其他字段
    duration: record.duration || record.parkingDuration,
    userName: record.userName || record.username || record.user?.username,
    createdTime: record.createdTime || record.createTime || record.createdAt,

    // 保留原始数据用于调试
    _raw: debugMode.value ? record : undefined
  }

  console.log('✅ 转换结果:', transformed)
  return transformed
}

// 更新统计
const updateStatistics = () => {
  console.log('📊 更新统计数据...')

  // 当前停车数量
  currentParkingCount.value = records.value.filter(r => r.status === 'PARKING').length
  console.log('🚗 当前停车数量:', currentParkingCount.value)

  // 累计消费
  totalSpent.value = records.value
    .filter(r => r.status === 'COMPLETED' && r.fee !== null && r.fee !== undefined)
    .reduce((sum, r) => {
      const fee = Number(r.fee) || 0
      return sum + fee
    }, 0)
  console.log('💰 累计消费:', totalSpent.value)
}

// 工具函数
const formatCurrency = (amount) => {
  if (amount === null || amount === undefined) return '0.00'
  const num = Number(amount)
  return isNaN(num) ? '0.00' : num.toFixed(2)
}

const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return '-'
  try {
    const date = new Date(dateTimeStr)
    if (isNaN(date.getTime())) return '无效时间'
    return date.toLocaleString('zh-CN')
  } catch (e) {
    return '时间格式错误'
  }
}

const calculateDuration = (startTime, endTime) => {
  if (!startTime) return '-'

  try {
    const start = new Date(startTime)
    const end = endTime ? new Date(endTime) : new Date()

    if (isNaN(start.getTime()) || isNaN(end.getTime())) {
      return '时间错误'
    }

    const diffMs = end - start
    if (diffMs < 0) return '0分钟'

    const hours = Math.floor(diffMs / (1000 * 60 * 60))
    const minutes = Math.floor((diffMs % (1000 * 60 * 60)) / (1000 * 60))

    if (hours > 0) {
      return `${hours}小时${minutes}分钟`
    } else {
      return `${minutes}分钟`
    }
  } catch (e) {
    return '计算错误'
  }
}

const getStatusText = (status) => {
  if (!status) return '未知状态'

  const statusMap = {
    'PARKING': '进行中',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }

  const upperStatus = status.toUpperCase()
  return statusMap[upperStatus] || status
}

const getStatusClass = (status) => {
  if (!status) return ''
  return status.toLowerCase()
}

const getParkingArea = (record) => {
  if (record.parkingLotName && record.parkingLotName !== '未知区域') {
    return record.parkingLotName
  }
  if (record.parkingSpot?.zone) {
    return record.parkingSpot.zone
  }
  return '未知区域'
}

// 操作函数
const refreshData = () => {
  console.log('🔄 手动刷新数据')
  loadRecords()
}

const applyFilters = () => {
  console.log('🔍 应用筛选:', filters.value)
}

const endParking = async (recordId) => {
  if (!confirm('确定要结束停车吗？系统将计算停车费用并跳转到支付宝支付页面。')) {
    return
  }

  try {
    console.log('🔍 ===== 开始结束停车流程 =====')
    console.log('📋 记录ID:', recordId)

    // 1. 先结束停车，获取费用信息
    console.log('🔄 调用结束停车API...')
    const response = await parkingApi.endParking(recordId)
    console.log('✅ 结束停车响应:', response)

    // 关键：打印完整的响应结构
    console.log('📊 响应完整结构:', JSON.stringify(response, null, 2))

    // 获取费用 - 从 response.data.fee
    const fee = response?.data?.fee
    console.log('💰 解析出的费用:', fee)
    console.log('💡 费用类型:', typeof fee)
    console.log('💡 费用是否为0:', fee === 0)
    console.log('💡 费用是否为空:', fee === null || fee === undefined)

    // 如果费用为0或空，直接完成
    if (fee === 0 || fee === null || fee === undefined) {
      console.log('⚠️ 费用为0/空，跳过支付')
      alert('停车结束！本次停车免费。')
      refreshData()
      showDetailModal.value = false
      return
    }

    console.log('✅ 需要支付，费用:', fee)

    // 2. 创建支付宝支付订单
    console.log('🔄 创建支付宝支付订单...')
    const paymentRequest = {
      amount: fee,
      subject: '校园停车费',
      description: `停车记录 ${recordId}`,
      recordId: recordId
    }
    console.log('📦 支付请求参数:', paymentRequest)

    const paymentResult = await parkingApi.createPayment(paymentRequest)
    console.log('💰 支付订单创建结果:', paymentResult)
    console.log('📊 支付响应完整结构:', JSON.stringify(paymentResult, null, 2))

    if (paymentResult?.success) {
      console.log('✅ 支付订单创建成功')

      // 获取表单HTML
      let formHtml = paymentResult.form
      console.log('📄 表单数据 (form字段):', formHtml ? `有，长度${formHtml.length}` : '无')

      if (!formHtml && paymentResult.data?.form) {
        formHtml = paymentResult.data.form
        console.log('🔄 从 data.form 获取表单，长度:', formHtml.length)
      }

      if (formHtml) {
        console.log('📝 表单预览（前300字符）:', formHtml.substring(0, 300))

        // 检查是否是有效的HTML表单
        if (formHtml.includes('<form') && formHtml.includes('action="')) {
          console.log('✅ 表单包含有效的form标签和action属性')

          const tempDiv = document.createElement('div')
          tempDiv.innerHTML = formHtml
          document.body.appendChild(tempDiv)

          const form = tempDiv.querySelector('form')
          if (form && form.action) {
            console.log('🚀 准备提交表单到:', form.action)

            // 显示表单详情
            console.log('🔍 表单详情:', {
              action: form.action,
              method: form.method,
              inputs: Array.from(form.querySelectorAll('input')).map(i => ({
                name: i.name,
                value: i.value,
                type: i.type
              }))
            })

            form.style.display = 'none'
            form.style.visibility = 'hidden'
            form.style.position = 'absolute'
            form.style.top = '-1000px'
            document.body.appendChild(form)

            // 立即提交
            console.log('🚀 提交表单...')
            form.submit()

            // 更新数据
            refreshData()
            showDetailModal.value = false
            return
          } else {
            console.error('❌ 表单元素无效')
            alert('支付页面异常')
          }
        } else {
          console.error('❌ 表单HTML不完整')
          console.error('表单内容:', formHtml)
          alert('支付页面数据异常')
        }
      } else {
        console.error('❌ 未获取到支付表单')
        alert('支付订单创建失败，未获取到支付页面。')
      }
    } else {
      console.error('❌ 创建支付订单失败')
      console.error('错误信息:', paymentResult?.message)

      // 使用测试支付
      if (confirm(`支付宝支付失败：${paymentResult?.message}\n是否使用测试支付继续？`)) {
        console.log('🔄 切换到测试支付...')
        // 测试支付逻辑
        const testHtml = await parkingApi.testPayment({
          amount: fee,
          subject: '校园停车费',
          description: `停车记录 ${recordId}`,
          recordId: recordId
        })

        const newWindow = window.open('', '_blank', 'width=600,height=700')
        newWindow.document.write(testHtml)
        newWindow.document.close()
      } else {
        alert(`停车结束！费用：¥${formatCurrency(fee)}。请稍后完成支付。`)
      }
    }

    // 更新数据
    refreshData()
    showDetailModal.value = false
    console.log('🔚 ===== 结束停车流程完成 =====')

  } catch (error) {
    console.error('💥 结束停车异常:', error)
    console.error('错误堆栈:', error.stack)
    alert('操作失败: ' + error.message)
  }
}

const selectRecord = (record) => {
  if (debugMode.value) {
    console.log('🔍 选择记录:', record)
    selectedRecord.value = record
    showDetailModal.value = true
  }
}

const viewRecordDetails = (record) => {
  selectedRecord.value = record
  showDetailModal.value = true
}

// 调试函数
const toggleDebug = () => {
  debugMode.value = !debugMode.value
  console.log('🔧 调试模式:', debugMode.value ? '开启' : '关闭')
}

const testWithMockData = () => {
  console.log('🧪 使用测试数据')

  const mockData = [
    {
      id: 1,
      plateNumber: '粤A12345',
      parkingLotName: 'A区停车场',
      parkingSpot: { zone: 'A区', spotNumber: 'A001', rate: 5 },
      startTime: new Date().toISOString(),
      endTime: null,
      status: 'PARKING',
      fee: null,
      userName: username.value,
      createdTime: new Date().toISOString()
    },
    {
      id: 2,
      plateNumber: '粤B67890',
      parkingLotName: 'B区停车场',
      parkingSpot: { zone: 'B区', spotNumber: 'B002', rate: 5 },
      startTime: new Date(Date.now() - 2 * 60 * 60 * 1000).toISOString(),
      endTime: new Date().toISOString(),
      status: 'COMPLETED',
      fee: 25.50,
      userName: username.value,
      createdTime: new Date(Date.now() - 2 * 60 * 60 * 1000).toISOString()
    },
    {
      id: 3,
      plateNumber: '粤C24680',
      parkingLotName: 'C区停车场',
      parkingSpot: { zone: 'C区', spotNumber: 'C003', rate: 5 },
      startTime: new Date(Date.now() - 24 * 60 * 60 * 1000).toISOString(),
      endTime: new Date(Date.now() - 22 * 60 * 60 * 1000).toISOString(),
      status: 'COMPLETED',
      fee: 15.00,
      userName: username.value,
      createdTime: new Date(Date.now() - 24 * 60 * 60 * 1000).toISOString()
    }
  ]

  records.value = mockData.map(transformRecordData)
  dataSource.value = '测试数据'
  updateStatistics()

  console.log('✅ 测试数据已加载:', records.value)
  alert('测试数据已加载，请检查页面显示')
}

const checkDataStructure = () => {
  console.log('📊 数据结构检查')
  console.log('1. records 类型:', typeof records.value)
  console.log('2. records 是数组:', Array.isArray(records.value))
  console.log('3. records 长度:', records.value.length)
  console.log('4. Vue 响应式检查:', isReactive.value)

  if (records.value.length > 0) {
    const firstRecord = records.value[0]
    console.log('5. 第一条记录:', firstRecord)
    console.log('6. 字段完整性:', {
      id: firstRecord.id,
      plateNumber: firstRecord.plateNumber,
      status: firstRecord.status,
      fee: firstRecord.fee,
      startTime: firstRecord.startTime,
      parkingSpot: firstRecord.parkingSpot
    })
  }
}

const logDataDetails = () => {
  console.log('📝 详细数据日志')
  console.log('所有记录:', records.value)
  console.log('过滤后记录:', filteredRecords.value)
  console.log('筛选条件:', filters.value)
}

// 导航函数
const goBack = () => {
  router.push('/external-user')
}

const goToParking = () => {
  router.push('/external-user')
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
/* 调试工具栏 */
.debug-toolbar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 10px 20px;
  border-radius: 10px;
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
  align-items: center;
  color: white;
}

.debug-btn {
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: white;
  padding: 8px 15px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.debug-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

.debug-status {
  margin-left: auto;
  font-weight: bold;
}

/* 数据状态监控 */
.data-status {
  background: #f8f9fa;
  border: 2px dashed #3498db;
  border-radius: 10px;
  padding: 20px;
  margin-bottom: 20px;
}

.data-status h4 {
  margin-top: 0;
  color: #3498db;
}

.status-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 15px;
  margin: 15px 0;
}

.status-item {
  background: white;
  padding: 12px;
  border-radius: 6px;
  border-left: 4px solid #3498db;
}

.status-item .label {
  font-weight: bold;
  color: #666;
  display: block;
  margin-bottom: 5px;
}

.status-item .value {
  font-size: 1.2em;
  color: #2c3e50;
}

/* 调试信息 */
.debug-info {
  background: #fff3cd;
  border: 1px solid #ffeaa7;
  border-radius: 8px;
  padding: 15px;
  margin-top: 20px;
}

.debug-info h5 {
  margin-top: 0;
  color: #856404;
}

/* 表格调试 */
.debug-hint {
  font-size: 0.9em;
  color: #666;
  margin-left: 10px;
}

.debug-id {
  display: block;
  font-size: 0.8em;
  color: #666;
  margin-top: 2px;
}

.table-header-info {
  padding: 15px 20px;
  background: #f8f9fa;
  border-bottom: 1px solid #dee2e6;
  font-weight: 500;
  color: #495057;
}

/* 其他样式保持原有不变 */
.external-parking-records-view {
  min-height: 100vh;
  background: #f8f9fa;
}

.app-header {
  background: white;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  padding: 0 20px;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 70px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.back-btn {
  padding: 8px 15px;
  border: 1px solid #ddd;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.back-btn:hover {
  background: #f0f0f0;
}

.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 30px 20px;
}

.page-header {
  background: white;
  padding: 25px;
  border-radius: 12px;
  margin-bottom: 20px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.08);
}

.page-title {
  margin: 0 0 20px 0;
  font-size: 1.8em;
  font-weight: 700;
  color: #2c3e50;
}

.page-stats {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.stat-badge {
  display: flex;
  flex-direction: column;
  background: #f8f9fa;
  padding: 12px 20px;
  border-radius: 8px;
  min-width: 120px;
}

.stat-label {
  font-size: 0.85em;
  color: #6c757d;
  margin-bottom: 5px;
}

.stat-value {
  font-size: 1.4em;
  font-weight: 700;
  color: #2c3e50;
}

/* 筛选区域 */
.filter-section {
  background: white;
  border-radius: 12px;
  padding: 20px 30px;
  margin-bottom: 25px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.08);
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
}

.filter-left {
  display: flex;
  gap: 20px;
  align-items: center;
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 10px;
}

.filter-label {
  font-weight: 600;
  color: #495057;
  white-space: nowrap;
}

.filter-select {
  padding: 8px 15px;
  border: 2px solid #dee2e6;
  border-radius: 6px;
  background: white;
  color: #495057;
  min-width: 120px;
  transition: all 0.3s ease;
}

.filter-select:focus {
  outline: none;
  border-color: #3498db;
  box-shadow: 0 0 0 3px rgba(52, 152, 219, 0.1);
}

/* 记录表格 */
.records-table-container {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(0,0,0,0.08);
}

.loading-state, .empty-state {
  padding: 60px 20px;
  text-align: center;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #3498db;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
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
  color: #6c757d;
}

.empty-actions {
  display: flex;
  gap: 10px;
  justify-content: center;
}

/* 表格样式 */
.records-table-wrapper {
  overflow-x: auto;
}

.records-table {
  width: 100%;
  border-collapse: collapse;
}

.records-table thead {
  background: #f8f9fa;
}

.records-table th {
  padding: 15px 20px;
  text-align: left;
  font-weight: 600;
  color: #495057;
  border-bottom: 2px solid #dee2e6;
  white-space: nowrap;
}

.records-table tbody tr {
  border-bottom: 1px solid #e9ecef;
  transition: all 0.2s ease;
}

.records-table tbody tr:hover {
  background: #f8f9fa;
}

.records-table tbody tr.parking {
  background: #f0f9ff;
}

.records-table tbody tr.completed {
  background: #f6fff6;
}

.records-table tbody tr.cancelled {
  background: #fff5f5;
}

.records-table td {
  padding: 15px 20px;
  color: #495057;
}

.index-cell {
  font-weight: 600;
  color: #666;
}

.plate-cell .plate-number {
  font-weight: 600;
  color: #2c3e50;
  font-size: 1.1em;
}

.location-cell {
  min-width: 150px;
}

.spot-number {
  color: #6c757d;
  font-size: 0.9em;
}

.time-cell {
  white-space: nowrap;
}

.duration-cell {
  white-space: nowrap;
}

.fee-cell {
  font-weight: 600;
  color: #e74c3c;
}

.fee-cell.calculating {
  color: #f39c12;
  font-style: italic;
}

/* 状态徽章 */
.status-badge {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 0.8em;
  font-weight: 600;
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

/* 操作按钮 */
.actions-cell {
  white-space: nowrap;
}

.btn-action {
  padding: 6px 12px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.85em;
  font-weight: 500;
  margin-right: 8px;
  transition: all 0.3s ease;
}

.btn-action.end-parking {
  background: #27ae60;
  color: white;
}

.btn-action.end-parking:hover {
  background: #219653;
}

.btn-action.view-details {
  background: #3498db;
  color: white;
}

.btn-action.view-details:hover {
  background: #2980b9;
}

/* 详情弹窗 */
.debug-details {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 15px;
  margin-top: 20px;
  max-height: 300px;
  overflow-y: auto;
}

.debug-details h5 {
  margin-top: 0;
  color: #666;
}

.debug-details pre {
  margin: 0;
  font-size: 12px;
  white-space: pre-wrap;
  word-wrap: break-word;
}

/* 按钮样式 */
.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s ease;
}

.btn-small {
  padding: 8px 16px;
  font-size: 0.9em;
}

.btn-primary {
  background: #3498db;
  color: white;
}

.btn-primary:hover {
  background: #2980b9;
}

.btn-secondary {
  background: #95a5a6;
  color: white;
}

.btn-secondary:hover {
  background: #7f8c8d;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .debug-toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .debug-btn {
    width: 100%;
  }

  .filter-section {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-left, .filter-right {
    width: 100%;
  }

  .status-grid {
    grid-template-columns: 1fr;
  }
}
</style>
