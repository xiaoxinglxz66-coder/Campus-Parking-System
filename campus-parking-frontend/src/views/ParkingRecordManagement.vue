<template>
  <!-- 保持原有的template内容完全不变 -->
  <div class="parking-record-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-text">
          <h1>停车记录管理</h1>
          <p>查看和管理所有停车记录信息</p>
        </div>
        <div class="header-stats">
          <div class="stat-item">
            <div class="stat-value">{{ parkingRecords.length }}</div>
            <div class="stat-label">总记录数</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ getParkingCount('PARKING') }}</div>
            <div class="stat-label">进行中</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 操作工具栏 -->
    <div class="toolbar-section">
      <div class="toolbar-left">
        <div class="search-box">
          <input
            v-model="filters.search"
            type="text"
            placeholder="搜索车牌号、用户名..."
            @input="handleSearch"
            class="search-input"
          />
          <span class="search-icon">🔍</span>
        </div>
      </div>
      <div class="toolbar-right">
        <button @click="refreshData" class="refresh-btn">
          <span class="btn-icon">🔄</span>
          刷新
        </button>
      </div>
    </div>

    <!-- 筛选卡片 -->
    <div class="filter-card">
      <div class="filter-header">
        <h3>筛选条件</h3>
        <button @click="resetFilters" class="clear-filters">清除筛选</button>
      </div>
      <div class="filter-grid">
        <div class="filter-item">
          <label>停车场</label>
          <select v-model="filters.parkingLot" @change="loadParkingRecords" class="filter-select">
            <option value="">全部停车场</option>
            <option v-for="lot in parkingLots" :key="lot" :value="lot">{{ lot }}</option>
          </select>
        </div>

        <div class="filter-item">
          <label>状态</label>
          <select v-model="filters.status" @change="loadParkingRecords" class="filter-select">
            <option value="">全部状态</option>
            <option value="PARKING">停车中</option>
            <option value="COMPLETED">已完成</option>
            <option value="CANCELLED">已取消</option>
          </select>
        </div>

        <div class="filter-item">
          <label>用户类型</label>
          <select v-model="filters.userType" @change="loadParkingRecords" class="filter-select">
            <option value="">全部用户</option>
            <option value="EXTERNAL_USER">校外用户</option>
            <option value="CAMPUS_USER">校内用户</option>
          </select>
        </div>
      </div>
    </div>

    <!-- 数据表格区域 -->
    <div class="table-section">
      <div class="table-card">
        <!-- 加载状态 -->
        <div v-if="loading" class="loading-state">
          <div class="spinner"></div>
          <p>正在加载停车记录...</p>
        </div>

        <!-- 错误状态 -->
        <div v-else-if="error" class="error-state">
          <div class="error-icon">❌</div>
          <div class="error-content">
            <h4>加载失败</h4>
            <p>{{ error }}</p>
            <button @click="loadParkingRecords" class="retry-btn">重新加载</button>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-else-if="parkingRecords.length === 0" class="empty-state">
          <div class="empty-icon">📊</div>
          <h4>暂无停车记录</h4>
          <p>当前没有找到匹配的停车记录</p>
        </div>

        <!-- 数据表格 -->
        <div v-else class="table-wrapper">
          <table class="data-table">
            <thead>
            <tr>
              <th>记录ID</th>
              <th>车牌号</th>
              <th>用户信息</th>
              <th>停车场</th>
              <th>时间信息</th>
              <th>费用状态</th>
              <th>操作</th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="record in parkingRecords" :key="record.id" class="table-row">
              <td class="record-id">{{ record.id || '-' }}</td>
              <td class="plate-number">
                <span class="plate-badge">{{ record.plateNumber || '未知车牌' }}</span>
              </td>
              <!-- 在 ParkingRecordManagement.vue 中，确保用户类型显示正确 -->
              <td class="user-info">
                <div class="user-name">{{ record.userName || '未知用户' }}</div>
                <div class="user-type">
    <span :class="['user-type-badge', (record.userType || '').toLowerCase()]">
      {{ getUserTypeText(record.userType) }}
    </span>
                </div>
              </td>
              <td class="parking-lot">
                {{ record.parkingLotName || '未知停车场' }}
              </td>
              <td class="time-info">
                <div class="start-time">{{ formatDateTime(record.startTime) }}</div>
                <div v-if="record.endTime" class="end-time">
                  结束: {{ formatDateTime(record.endTime) }}
                </div>
                <div v-else class="duration">
                  已停: {{ calculateDuration(record.startTime, record.endTime) }}
                </div>
              </td>
              <td class="fee-status">
                <div class="status-badge-wrapper">
                    <span :class="['status-badge', (record.status || '').toLowerCase()]">
                      {{ getStatusText(record.status) }}
                    </span>
                </div>
                <div v-if="record.fee !== null && record.fee !== undefined" class="fee-amount">
                  ¥{{ formatCurrency(record.fee) }}
                </div>
                <div v-else class="no-fee">-</div>
              </td>
              <td class="actions">
                <button
                  v-if="record.status === 'PARKING'"
                  @click="endParking(record.id)"
                  class="action-btn end-btn"
                  title="结束停车"
                >
                  <span class="btn-icon">🅿️</span>
                  结束
                </button>
                <button
                  v-else
                  @click="viewDetails(record)"
                  class="action-btn view-btn"
                  title="查看详情"
                >
                  <span class="btn-icon">👁️</span>
                  详情
                </button>
              </td>
            </tr>
            </tbody>
          </table>
        </div>

        <!-- 分页控件 -->
        <div v-if="parkingRecords.length > 0" class="pagination-section">
          <div class="pagination-info">
            显示第 {{ (currentPage - 1) * pageSize + 1 }} - {{ Math.min(currentPage * pageSize, totalRecords) }} 条，共 {{ totalRecords }} 条记录
          </div>
          <div class="pagination-controls">
            <button
              :disabled="currentPage === 1"
              @click="changePage(currentPage - 1)"
              class="page-btn prev-btn"
            >
              上一页
            </button>
            <div class="page-numbers">
              <span class="current-page">第 {{ currentPage }} 页</span>
              <span class="total-pages">共 {{ totalPages }} 页</span>
            </div>
            <button
              :disabled="currentPage >= totalPages"
              @click="changePage(currentPage + 1)"
              class="page-btn next-btn"
            >
              下一页
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 详情弹窗 -->
    <div v-if="showDetailModal" class="modal-overlay" @click.self="showDetailModal = false">
      <div class="modal-content">
        <div class="modal-header">
          <h3>停车记录详情</h3>
          <button @click="showDetailModal = false" class="close-btn">×</button>
        </div>
        <div class="modal-body" v-if="selectedRecord">
          <div class="detail-sections">
            <div class="detail-section">
              <h4>基本信息</h4>
              <div class="detail-grid">
                <div class="detail-item">
                  <label>记录ID:</label>
                  <span>{{ selectedRecord.id || '-' }}</span>
                </div>
                <div class="detail-item">
                  <label>车牌号:</label>
                  <span class="plate-value">{{ selectedRecord.plateNumber || '未知车牌' }}</span>
                </div>
                <div class="detail-item">
                  <label>用户名:</label>
                  <span>{{ selectedRecord.userName || '未知用户' }}</span>
                </div>
                <div class="detail-item">
                  <label>用户类型:</label>
                  <span :class="['user-type-badge', (selectedRecord.userType || '').toLowerCase()]">
                    {{ getUserTypeText(selectedRecord.userType) }}
                  </span>
                </div>
              </div>
            </div>

            <div class="detail-section">
              <h4>停车信息</h4>
              <div class="detail-grid">
                <div class="detail-item">
                  <label>停车场:</label>
                  <span>{{ selectedRecord.parkingLotName || '未知停车场' }}</span>
                </div>
                <div class="detail-item">
                  <label>开始时间:</label>
                  <span>{{ formatDateTime(selectedRecord.startTime) }}</span>
                </div>
                <div class="detail-item">
                  <label>结束时间:</label>
                  <span>{{ selectedRecord.endTime ? formatDateTime(selectedRecord.endTime) : '进行中' }}</span>
                </div>
                <div class="detail-item">
                  <label>停车时长:</label>
                  <span>{{ calculateDuration(selectedRecord.startTime, selectedRecord.endTime) }}</span>
                </div>
              </div>
            </div>

            <div class="detail-section">
              <h4>费用状态</h4>
              <div class="detail-grid">
                <div class="detail-item">
                  <label>状态:</label>
                  <span :class="['status-badge', (selectedRecord.status || '').toLowerCase()]">
                    {{ getStatusText(selectedRecord.status) }}
                  </span>
                </div>
                <div class="detail-item">
                  <label>费用:</label>
                  <span class="fee-amount">
                    {{ selectedRecord.fee !== null && selectedRecord.fee !== undefined ? '¥' + formatCurrency(selectedRecord.fee) : '未计算' }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="showDetailModal = false" class="btn btn-primary">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<!-- script部分保持原有代码完全不变 -->
<script setup>
import { ref, onMounted, computed } from 'vue'
import { authFetch } from '@/utils/auth'
import { getCurrentUserRole } from '@/utils/roleHelper'
// 数据
const parkingRecords = ref([])
const parkingLots = ref([])
const loading = ref(false)
const error = ref('')
const showDetailModal = ref(false)
const selectedRecord = ref(null)

// 筛选条件
const filters = ref({
  parkingLot: '',
  status: '',
  userType: '',
  search: ''
})

// 分页
const currentPage = ref(1)
const pageSize = ref(10)
const totalRecords = ref(0)

const totalPages = computed(() => {
  return Math.ceil(totalRecords.value / pageSize.value)
})

// 计算属性：统计进行中的停车记录
const getParkingCount = (status) => {
  return parkingRecords.value.filter(record => record.status === status).length
}
// 在setup函数外面或里面添加
const getUserRole = () => {
  try {
    // 尝试从 localStorage 获取用户信息
    let userInfo = localStorage.getItem('userInfo');

    if (!userInfo || userInfo === 'undefined' || userInfo === 'null') {
      console.warn('⚠️ localStorage中没有userInfo，尝试从token解析');

      // 尝试从token解析用户信息（如果JWT中包含）
      const token = localStorage.getItem('token');
      if (token) {
        // JWT token的格式是 header.payload.signature
        const payload = token.split('.')[1];
        if (payload) {
          try {
            const decodedPayload = JSON.parse(atob(payload));
            console.log('🔍 从token解析的用户信息:', decodedPayload);

            // 保存解析的用户信息到localStorage
            const userInfoToSave = {
              id: decodedPayload.userId || decodedPayload.id,
              username: decodedPayload.sub || decodedPayload.username,
              role: decodedPayload.role || 'UNKNOWN'
            };

            localStorage.setItem('userInfo', JSON.stringify(userInfoToSave));
            return userInfoToSave.role;
          } catch (e) {
            console.error('❌ 解析token失败:', e);
          }
        }
      }

      // 如果还是没有，尝试调用API获取用户信息
      return fetchUserInfoFromAPI();
    }

    // 正常解析userInfo
    userInfo = JSON.parse(userInfo);
    console.log('✅ 从localStorage获取的用户信息:', userInfo);

    return userInfo.role || '';
  } catch (error) {
    console.error('❌ 获取用户角色失败:', error);
    return '';
  }
}
// 从API获取用户信息的方法
const fetchUserInfoFromAPI = async () => {
  try {
    const response = await authFetch('/api/users/me');
    if (response.ok) {
      const userData = await response.json();
      if (userData.success && userData.data) {
        const userInfo = {
          id: userData.data.id,
          username: userData.data.username,
          role: userData.data.role,
          // 其他用户信息
        };
        localStorage.setItem('userInfo', JSON.stringify(userInfo));
        return userData.data.role;
      }
    }
  } catch (error) {
    console.error('❌ 从API获取用户信息失败:', error);
  }
  return '';
}
// 工具函数 - 添加空值检查
const formatCurrency = (amount) => {
  if (amount === null || amount === undefined) return '0.00'
  return Number(amount).toFixed(2)
}

const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return '-'
  try {
    const date = new Date(dateTimeStr)
    return date.toLocaleString('zh-CN')
  } catch (e) {
    return '无效日期'
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

const getUserTypeText = (userType) => {
  const typeMap = {
    'EXTERNAL_USER': '校外用户',
    'STUDENT': '学生',
    'TEACHER': '教师',
    'STAFF': '职工',
    'ADMIN': '管理员',
    'CAMPUS_USER': '校内用户'
  }
  return typeMap[userType] || userType || '未知用户类型'
}

const calculateDuration = (startTime, endTime) => {
  if (!startTime) return '-'

  try {
    const start = new Date(startTime)
    const end = endTime ? new Date(endTime) : new Date()

    const duration = end - start
    const hours = Math.floor(duration / (1000 * 60 * 60))
    const minutes = Math.floor((duration % (1000 * 60 * 60)) / (1000 * 60))

    if (hours > 0) {
      return `${hours}小时${minutes}分钟`
    } else {
      return `${minutes}分钟`
    }
  } catch (e) {
    return '计算错误'
  }
}

const loadParkingRecords = async () => {
  loading.value = true
  error.value = ''

  try {
    // ========== 替换这部分代码开始 ==========
    // 删除原来的复杂角色获取逻辑，替换为：
    console.log('🔄 开始加载停车记录...');

    // 使用统一的角色获取方法
    const role = getCurrentUserRole();
    console.log('🎯 获取到的用户角色:', role);

    if (!role) {
      console.error('❌ 无法获取用户角色');
      throw new Error('无法获取用户角色信息，请重新登录');
    }

    const upperRole = role.toUpperCase().trim();
    console.log('🎯 处理后的角色:', upperRole);

    let baseUrl;

    // 根据角色选择接口
    if (upperRole === 'ADMIN') {
      baseUrl = '/api/parking-records/admin/page';
      console.log('👑 管理员 -> 使用管理员接口');
    } else if (upperRole === 'EXTERNAL_USER') {
      baseUrl = '/api/parking-records/external/my-records/page';
      console.log('👤 校外用户 -> 使用校外接口');
    } else if (['STUDENT', 'TEACHER', 'STAFF'].includes(upperRole)) {
      baseUrl = '/api/parking-records/campus/my-records/page';
      console.log('🎓 校内用户 -> 使用校内接口');
    } else {
      console.error('❌ 无法识别的角色:', upperRole);
      throw new Error(`用户角色"${upperRole}"无法识别`);
    }

    console.log('📡 最终使用的API:', baseUrl);

    // 构建查询参数
    const params = new URLSearchParams()
    params.append('page', (currentPage.value - 1).toString())
    params.append('size', pageSize.value.toString())

    // 添加筛选参数
    if (filters.value.parkingLot) {
      params.append('parkingLot', filters.value.parkingLot)
    }
    if (filters.value.status) {
      params.append('status', filters.value.status)
    }
    // 只有管理员可以筛选用户类型
    if (filters.value.userType && upperRole === 'ADMIN') {
      params.append('userType', filters.value.userType)
    } else if (filters.value.userType) {
      console.log('⚠️ 非管理员用户，忽略userType筛选');
    }
    if (filters.value.search) {
      params.append('search', filters.value.search)
    }

    const url = `${baseUrl}?${params.toString()}`
    console.log('📡 请求停车记录URL:', url)

    const response = await authFetch(url)

    if (!response) {
      throw new Error('请求失败：未获得响应')
    }

    if (!response.ok) {
      const errorText = await response.text()
      console.error('❌ API错误响应:', errorText)
      throw new Error(`请求失败: ${response.status}`)
    }

    const responseText = await response.text()
    console.log('📥 API响应:', responseText)

    let data
    try {
      data = JSON.parse(responseText)
    } catch (parseError) {
      console.error('❌ JSON解析错误:', parseError)
      throw new Error('服务器返回了无效的JSON格式')
    }

    console.log('📊 解析后的数据:', data)

    // 处理响应数据 - 统一处理格式
    let records = []
    let total = 0

    if (data && data.success) {
      // 标准格式: { success: true, data: { records: [...], total: 100 } }
      if (data.data && data.data.records && Array.isArray(data.data.records)) {
        records = data.data.records
        total = data.data.total || 0
        console.log('✅ 标准格式，找到记录数:', records.length, '总数:', total)
      }
      // 兼容格式: { success: true, data: [...] }
      else if (data.data && Array.isArray(data.data)) {
        records = data.data
        total = data.data.length
        console.log('⚠️ 兼容格式，找到记录数:', records.length)
      }
    }
    // 直接数组格式: [...]
    else if (data && Array.isArray(data)) {
      records = data
      total = records.length
      console.log('⚠️ 直接数组格式，找到记录数:', records.length)
    } else {
      console.warn('⚠️ 未知的数据格式:', data)
      throw new Error('服务器返回了未知的数据格式')
    }

    // 确保每条记录都有必要的字段
    parkingRecords.value = records.map(record => ({
      id: record.id || 0,
      plateNumber: record.plateNumber || '未知车牌',
      userName: record.userName || '未知用户',
      userType: record.userType || 'UNKNOWN',
      parkingLotName: record.parkingLotName || '未知停车场',
      startTime: record.startTime,
      endTime: record.endTime,
      status: record.status || 'UNKNOWN',
      fee: record.fee
    }))

    totalRecords.value = total

    console.log('✅ 成功处理记录数:', parkingRecords.value.length)

  } catch (err) {
    console.error('❌ 加载停车记录失败:', err)

    // 显示更友好的错误信息
    if (err.message.includes('重新登录')) {
      error.value = '用户信息已过期，请重新登录';
      // 可选：跳转到登录页面
      // router.push('/login');
    } else if (err.message.includes('无法识别')) {
      error.value = '用户权限信息异常，请联系管理员';
    } else if (err.message.includes('权限不足')) {
      error.value = '权限不足，无法访问该页面';
    } else {
      error.value = err.message || '加载停车记录失败';
    }

    parkingRecords.value = [];
    totalRecords.value = 0;
  } finally {
    loading.value = false;
  }
}

const loadParkingLots = async () => {
  try {
    console.log('🔄 开始加载停车场列表...');

    // 使用统一的角色获取方法
    const role = getCurrentUserRole();
    const upperRole = (role || '').toUpperCase().trim();
    console.log('👤 当前用户角色:', upperRole);

    let url;

    // 根据用户角色选择不同的接口
    if (upperRole === 'ADMIN') {
      url = '/api/parking-records/admin/parking-lots';
      console.log('👑 管理员，使用管理员停车场接口');
    } else if (upperRole === 'EXTERNAL_USER') {
      url = '/api/parking-records/campus/parking-lots';
      console.log('👤 校外用户，使用校内停车场接口');
    } else if (['STUDENT', 'TEACHER', 'STAFF'].includes(upperRole)) {
      url = '/api/parking-records/campus/parking-lots';
      console.log('🎓 校内用户，使用校内停车场接口');
    } else {
      console.error('❌ 未知用户角色:', upperRole);
      url = '/api/parking-records/campus/parking-lots'; // 默认使用校内接口
    }

    console.log('📡 请求停车场URL:', url);
    const response = await authFetch(url);

    if (response && response.ok) {
      const data = await response.json();
      console.log('📊 停车场数据响应:', data);

      // 处理不同的响应格式
      if (Array.isArray(data)) {
        // 直接是数组
        parkingLots.value = data;
      } else if (data && data.data && Array.isArray(data.data)) {
        // 格式: { data: [...] }
        parkingLots.value = data.data;
      } else if (data && data.success && data.data && Array.isArray(data.data)) {
        // 格式: { success: true, data: [...] }
        parkingLots.value = data.data;
      } else if (data && data.success && Array.isArray(data.data)) {
        // 格式: { success: true, data: [...] }
        parkingLots.value = data.data;
      } else {
        console.warn('⚠️ 未知的停车场数据格式:', data);
        parkingLots.value = [];
      }

      console.log(`✅ 加载了 ${parkingLots.value.length} 个停车场`);
    } else {
      console.error('❌ 加载停车场列表失败:', response);
      parkingLots.value = [];
    }
  } catch (err) {
    console.error('💥 加载停车场列表异常:', err);
    parkingLots.value = [];
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadParkingRecords()
}

// 在 resetFilters 中重置 userType
const resetFilters = () => {
  filters.value = {
    parkingLot: '',
    status: '',
    userType: '',
    search: ''
  }
  currentPage.value = 1
  loadParkingRecords()
}

const refreshData = () => {
  loadParkingRecords()
  loadParkingLots()
}

const changePage = (page) => {
  console.log('🚀 点击分页按钮，跳转到第', page, '页');
  currentPage.value = page;
  loadParkingRecords();  // ✅ 正确：调用停车记录加载函数
};

// 结束停车并跳转到支付宝支付
const endParking = async (recordId) => {
  if (!confirm('确定要结束停车吗？系统将计算停车费用并跳转到支付宝支付页面。')) {
    return
  }

  try {
    const token = localStorage.getItem('token')
    console.log('🔍 开始结束停车流程，记录ID:', recordId)

    // 1. 先结束停车，获取费用信息
    const endResponse = await fetch(`/api/parking-records/${recordId}/end`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })

    if (endResponse.ok) {
      const record = await endResponse.json()
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
      const paymentResponse = await fetch('/api/alipay/create', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          amount: fee,
          subject: '校园停车费',
          description: `停车记录 ${recordId}`,
          recordId: recordId
        })
      })

      const paymentResult = await paymentResponse.json()
      console.log('💰 支付订单创建结果:', paymentResult)

      if (paymentResult.success) {
        // 3. 处理支付宝返回的表单
        console.log('🚀 处理支付宝支付表单...')

        // 创建临时div来解析表单
        const tempDiv = document.createElement('div')
        tempDiv.innerHTML = paymentResult.form

        // 查找表单
        const form = tempDiv.querySelector('form')
        if (form && form.action) {
          console.log('✅ 找到支付宝表单，跳转到:', form.action)

          // 方法1: 直接跳转到支付宝支付页面
          // window.location.href = form.action

          // 方法2: 在新窗口打开支付宝支付
          const alipayWindow = window.open(form.action, '_blank', 'width=800,height=600')

          if (!alipayWindow) {
            alert('请允许弹出窗口以跳转到支付宝支付页面')
            // 如果弹窗被阻止，使用方法1
            window.location.href = form.action
          }

        } else {
          console.error('❌ 支付宝表单解析失败')
          console.log('📄 返回的HTML:', paymentResult.form)

          // 如果表单解析失败，显示备用方案
          alert(`停车结束！费用：¥${formatCurrency(fee)}。支付页面加载异常，请稍后手动支付。`)
        }

        // 刷新数据
        await refreshData()

      } else {
        console.error('❌ 创建支付订单失败:', paymentResult.message)

        // 如果支付宝支付失败，使用测试支付作为备用
        if (confirm(`支付宝支付暂时不可用：${paymentResult.message}\n是否使用测试支付继续？`)) {
          await useTestPayment(recordId, fee, token)
        } else {
          alert(`停车结束！费用：¥${formatCurrency(fee)}。请稍后完成支付。`)
        }
      }
    } else {
      const errorText = await endResponse.text()
      alert(`结束停车失败：${errorText}`)
    }
  } catch (error) {
    console.error('💥 结束停车异常:', error)
    alert('结束停车失败，请检查网络连接')
  }
}

// 备用测试支付方法
const useTestPayment = async (recordId, fee, token) => {
  console.log('🔄 使用测试支付...')
  const paymentResponse = await fetch('/api/alipay/test-payment', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      amount: fee,
      subject: '校园停车费',
      description: `停车记录 ${recordId}`,
      recordId: recordId
    })
  })

  if (paymentResponse.ok) {
    const testHtml = await paymentResponse.text()
    const newWindow = window.open('', '_blank', 'width=600,height=700')
    newWindow.document.write(testHtml)
    newWindow.document.close()
  } else {
    alert('测试支付页面加载失败')
  }
}

const viewDetails = (record) => {
  selectedRecord.value = record
  showDetailModal.value = true
}

// 初始化
onMounted(() => {
  loadParkingRecords()
  loadParkingLots()
})
</script>

<!-- 只修改style部分，升级UI为高级感 -->
<style scoped>
.parking-record-management {
  min-height: 100vh;
  background: linear-gradient(135deg, #f8fafc 0%, #edf2f7 100%);
  padding: 24px;
  font-family: 'Segoe UI', 'SF Pro Display', -apple-system, BlinkMacSystemFont, sans-serif;
}

/* 页面头部 - 高级卡片设计 */
.page-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20px;
  padding: 32px 40px;
  margin-bottom: 24px;
  box-shadow: 0 10px 40px rgba(102, 126, 234, 0.2);
  color: white;
  position: relative;
  overflow: hidden;
}

.page-header::before {
  content: '';
  position: absolute;
  top: 0;
  right: 0;
  width: 200px;
  height: 200px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
  transform: translate(50%, -50%);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
  z-index: 1;
}

.header-text h1 {
  font-size: 2.25rem;
  font-weight: 800;
  margin: 0 0 12px 0;
  letter-spacing: -0.5px;
  background: linear-gradient(to right, #ffffff, #e2e8f0);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.header-text p {
  font-size: 1.125rem;
  opacity: 0.9;
  margin: 0;
  font-weight: 400;
}

.header-stats {
  display: flex;
  gap: 32px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  padding: 20px 32px;
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.stat-item {
  text-align: center;
  min-width: 120px;
}

.stat-value {
  font-size: 2.5rem;
  font-weight: 800;
  margin-bottom: 8px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.stat-label {
  font-size: 0.875rem;
  opacity: 0.85;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  font-weight: 500;
}

/* 操作工具栏 */
.toolbar-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: white;
  border-radius: 16px;
  padding: 20px 28px;
  margin-bottom: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  border: 1px solid #e5e7eb;
}

.search-box {
  position: relative;
  width: 400px;
}

.search-input {
  width: 100%;
  padding: 14px 52px 14px 20px;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  font-size: 1rem;
  transition: all 0.3s ease;
  background: #f8fafc;
  color: #2d3748;
}

.search-input:focus {
  outline: none;
  border-color: #667eea;
  background: white;
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1);
}

.search-icon {
  position: absolute;
  right: 20px;
  top: 50%;
  transform: translateY(-50%);
  color: #667eea;
  font-size: 1.25rem;
}

.refresh-btn {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 24px;
  border: none;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  cursor: pointer;
  font-weight: 600;
  font-size: 1rem;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.refresh-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.btn-icon {
  font-size: 1.125rem;
}

/* 筛选卡片 - 玻璃态效果 */
.filter-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  padding: 28px;
  margin-bottom: 24px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.filter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 2px solid #f1f5f9;
}

.filter-header h3 {
  font-size: 1.375rem;
  font-weight: 700;
  color: #2d3748;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.filter-header h3::before {
  content: '⚡';
  font-size: 1.25rem;
}

.clear-filters {
  background: #f8fafc;
  border: 2px solid #e5e7eb;
  border-radius: 10px;
  color: #64748b;
  cursor: pointer;
  font-weight: 600;
  font-size: 0.875rem;
  padding: 10px 20px;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
}

.clear-filters:hover {
  background: #f1f5f9;
  color: #475569;
  border-color: #cbd5e1;
}

.clear-filters::before {
  content: '🗑️';
}

.filter-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 24px;
}

.filter-item label {
  display: block;
  margin-bottom: 12px;
  font-weight: 600;
  color: #475569;
  font-size: 0.875rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.filter-select {
  width: 100%;
  padding: 14px 20px;
  padding-right: 50px; /* 为箭头留出空间 */
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  background: white;
  font-size: 1rem;
  transition: all 0.3s ease;
  color: #2d3748;
  appearance: none;
  position: relative;
  cursor: pointer;
}

/* 创建自定义下拉箭头 */
.filter-select::after {
  content: "";
  position: absolute;
  right: 20px;
  top: 50%;
  transform: translateY(-50%);
  width: 0;
  height: 0;
  border-left: 6px solid transparent;
  border-right: 6px solid transparent;
  border-top: 8px solid #667eea;
  pointer-events: none;
  transition: border-top-color 0.3s ease;
}

.filter-select:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.filter-select:focus::after {
  border-top-color: #764ba2;
}

.filter-select:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

/* 表格区域 */
.table-section {
  background: white;
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  border: 1px solid #e5e7eb;
}

.table-card {
  padding: 0;
}

/* 加载状态 */
.loading-state {
  padding: 80px 20px;
  text-align: center;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
}

.spinner {
  width: 60px;
  height: 60px;
  border: 4px solid rgba(102, 126, 234, 0.1);
  border-top: 4px solid #667eea;
  border-radius: 50%;
  animation: spin 1s cubic-bezier(0.68, -0.55, 0.27, 1.55) infinite;
  margin: 0 auto 24px;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.2);
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.loading-state p {
  color: #667eea;
  font-size: 1.125rem;
  font-weight: 600;
  letter-spacing: 0.5px;
}

/* 错误状态 */
.error-state {
  padding: 60px 20px;
  text-align: center;
  background: linear-gradient(135deg, #fff5f5 0%, #fed7d7 100%);
}

.error-icon {
  font-size: 4rem;
  margin-bottom: 24px;
  opacity: 0.8;
}

.error-content h4 {
  margin: 0 0 16px 0;
  font-size: 1.5rem;
  color: #dc2626;
  font-weight: 700;
}

.error-content p {
  margin: 0 0 24px 0;
  color: #7f1d1d;
  font-size: 1.125rem;
  max-width: 400px;
  margin-left: auto;
  margin-right: auto;
}

.retry-btn {
  padding: 14px 32px;
  background: linear-gradient(135deg, #dc2626 0%, #b91c1c 100%);
  color: white;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  font-weight: 600;
  font-size: 1rem;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(220, 38, 38, 0.3);
}

.retry-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(220, 38, 38, 0.4);
}

/* 空状态 */
.empty-state {
  padding: 100px 20px;
  text-align: center;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
}

.empty-icon {
  font-size: 5rem;
  margin-bottom: 24px;
  opacity: 0.5;
  filter: drop-shadow(0 4px 6px rgba(0, 0, 0, 0.1));
}

.empty-state h4 {
  margin: 0 0 16px 0;
  font-size: 1.75rem;
  color: #2d3748;
  font-weight: 700;
}

.empty-state p {
  margin: 0;
  color: #64748b;
  font-size: 1.125rem;
  max-width: 400px;
  margin-left: auto;
  margin-right: auto;
}

/* 表格样式 */
.table-wrapper {
  overflow-x: auto;
  padding: 20px;
}

.data-table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
  font-size: 0.9375rem;
}

.data-table th {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 18px 20px;
  text-align: left;
  font-weight: 700;
  color: white;
  white-space: nowrap;
  position: sticky;
  top: 0;
  z-index: 10;
  border: none;
}

.data-table th:first-child {
  border-top-left-radius: 12px;
}

.data-table th:last-child {
  border-top-right-radius: 12px;
}

.data-table td {
  padding: 18px 20px;
  border-bottom: 1px solid #f1f5f9;
  vertical-align: middle;
}

.table-row {
  transition: all 0.3s ease;
  border-left: 4px solid transparent;
}

.table-row:hover {
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-left-color: #667eea;
  transform: translateX(4px);
}

/* 表格单元格样式 */
.record-id {
  font-family: 'Monaco', 'Consolas', 'SF Mono', monospace;
  color: #64748b;
  font-size: 0.8125rem;
  font-weight: 500;
}

.plate-badge {
  background: linear-gradient(135deg, #2d3748 0%, #4a5568 100%);
  color: white;
  padding: 8px 16px;
  border-radius: 10px;
  font-weight: 700;
  font-size: 0.9375rem;
  letter-spacing: 1px;
  display: inline-block;
  box-shadow: 0 2px 8px rgba(45, 55, 72, 0.2);
}

.user-info {
  min-width: 140px;
}

.user-name {
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 8px;
  font-size: 1rem;
}

.user-type-badge {
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  display: inline-block;
}

.user-type-badge.external_user {
  background: linear-gradient(135deg, #fca5a5 0%, #ef4444 100%);
  color: white;
}

.user-type-badge.student {
  background: linear-gradient(135deg, #93c5fd 0%, #3b82f6 100%);
  color: white;
}

.user-type-badge.teacher {
  background: linear-gradient(135deg, #86efac 0%, #16a34a 100%);
  color: white;
}

.user-type-badge.staff {
  background: linear-gradient(135deg, #fcd34d 0%, #f59e0b 100%);
  color: white;
}

.user-type-badge.admin {
  background: linear-gradient(135deg, #c4b5fd 0%, #8b5cf6 100%);
  color: white;
}

.parking-lot {
  color: #475569;
  font-weight: 500;
  font-size: 0.9375rem;
}

.time-info {
  min-width: 160px;
}

.start-time {
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 6px;
  font-size: 0.9375rem;
}

.end-time, .duration {
  font-size: 0.8125rem;
  color: #64748b;
  font-weight: 400;
}

.fee-status {
  text-align: center;
  min-width: 120px;
}

.status-badge {
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  display: inline-block;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.status-badge.parking {
  background: linear-gradient(135deg, #93c5fd 0%, #3b82f6 100%);
  color: white;
}

.status-badge.completed {
  background: linear-gradient(135deg, #86efac 0%, #16a34a 100%);
  color: white;
}

.status-badge.cancelled {
  background: linear-gradient(135deg, #fca5a5 0%, #ef4444 100%);
  color: white;
}

.fee-amount {
  font-weight: 800;
  color: #dc2626;
  margin-top: 8px;
  font-size: 1.125rem;
}

.no-fee {
  color: #94a3b8;
  font-size: 0.8125rem;
  margin-top: 8px;
  font-style: italic;
}

.actions {
  text-align: center;
  min-width: 100px;
}

.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  font-size: 0.875rem;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.end-btn {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
  color: white;
}

.end-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(239, 68, 68, 0.3);
}

.view-btn {
  background: linear-gradient(135deg, #64748b 0%, #475569 100%);
  color: white;
}

.view-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(100, 116, 139, 0.3);
}

/* 分页样式 */
.pagination-section {
  padding: 24px 28px;
  border-top: 2px solid #f1f5f9;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #f8fafc;
}

.pagination-info {
  color: #64748b;
  font-size: 0.9375rem;
  font-weight: 500;
}

.pagination-controls {
  display: flex;
  align-items: center;
  gap: 24px;
}

.page-numbers {
  display: flex;
  gap: 12px;
  font-size: 0.9375rem;
  align-items: center;
}

.current-page {
  color: #2d3748;
  font-weight: 700;
  background: white;
  padding: 8px 16px;
  border-radius: 10px;
  border: 2px solid #e5e7eb;
}

.total-pages {
  color: #64748b;
  font-weight: 500;
}

.page-btn {
  padding: 10px 24px;
  border: 2px solid #e5e7eb;
  background: white;
  border-radius: 10px;
  cursor: pointer;
  font-size: 0.9375rem;
  font-weight: 600;
  transition: all 0.3s ease;
  color: #475569;
  min-width: 100px;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-btn:not(:disabled):hover {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-color: transparent;
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

/* 弹窗样式 - 高级玻璃态 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
  backdrop-filter: blur(8px);
}

.modal-content {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.98) 0%, rgba(248, 250, 252, 0.98) 100%);
  border-radius: 24px;
  width: 90%;
  max-width: 700px;
  max-height: 85vh;
  overflow: hidden;
  box-shadow: 0 25px 80px rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(20px);
}

.modal-header {
  padding: 28px 32px;
  border-bottom: 2px solid #f1f5f9;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.modal-header h3 {
  margin: 0;
  font-size: 1.625rem;
  font-weight: 800;
  letter-spacing: -0.5px;
}

.close-btn {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  font-size: 2rem;
  color: white;
  cursor: pointer;
  padding: 0;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: rotate(90deg);
}

.modal-body {
  padding: 32px;
  max-height: 60vh;
  overflow-y: auto;
}

.detail-sections {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.detail-section h4 {
  margin: 0 0 24px 0;
  font-size: 1.25rem;
  font-weight: 700;
  color: #2d3748;
  padding-bottom: 16px;
  border-bottom: 2px solid #f1f5f9;
  position: relative;
}

.detail-section h4::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 60px;
  height: 3px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 3px;
}

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 12px;
  border: 2px solid #e5e7eb;
  transition: all 0.3s ease;
}

.detail-item:hover {
  background: white;
  border-color: #667eea;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.1);
}

.detail-item label {
  font-weight: 600;
  color: #475569;
  font-size: 0.875rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.detail-item span {
  color: #2d3748;
  font-size: 1rem;
  font-weight: 500;
}

.plate-value {
  font-weight: 700;
  color: #2d3748;
  font-size: 1.125rem;
  background: linear-gradient(135deg, #f8fafc 0%, #e5e7eb 100%);
  padding: 8px 16px;
  border-radius: 10px;
  display: inline-block;
  width: fit-content;
}

.modal-footer {
  padding: 24px 32px;
  border-top: 2px solid #f1f5f9;
  text-align: right;
  background: #f8fafc;
}

.btn {
  padding: 14px 32px;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  font-weight: 600;
  font-size: 1rem;
  transition: all 0.3s ease;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .filter-grid {
    grid-template-columns: 1fr;
  }

  .detail-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .parking-record-management {
    padding: 16px;
  }

  .page-header {
    padding: 24px;
  }

  .header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 24px;
  }

  .header-stats {
    width: 100%;
    justify-content: space-around;
  }

  .toolbar-section {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }

  .search-box {
    width: 100%;
  }

  .pagination-section {
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }

  .data-table {
    font-size: 0.875rem;
  }

  .data-table th,
  .data-table td {
    padding: 12px 16px;
  }

  .modal-content {
    width: 95%;
    margin: 10px;
  }
}

@media (max-width: 480px) {
  .header-stats {
    flex-direction: column;
    gap: 20px;
  }

  .stat-item {
    min-width: auto;
  }

  .pagination-controls {
    flex-direction: column;
    gap: 16px;
  }

  .page-btn {
    min-width: 120px;
  }
}
</style>
