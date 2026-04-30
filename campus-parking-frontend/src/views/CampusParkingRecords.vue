<template>
  <div class="campus-parking-records">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-text">
          <h1>我的停车记录</h1>
          <p>查看和管理您的校园停车记录</p>
        </div>
        <div class="header-stats">
          <div class="stat-item" @click="filterByStatus('PARKING')">
            <div class="stat-value">{{ stats.currentParking }}</div>
            <div class="stat-label">进行中</div>
          </div>
          <div class="stat-item" @click="filterByStatus('COMPLETED')">
            <div class="stat-value">{{ stats.completed }}</div>
            <div class="stat-label">已完成</div>
          </div>
          <div class="stat-item" @click="filterByStatus('CANCELLED')">
            <div class="stat-value">{{ stats.cancelled }}</div>
            <div class="stat-label">已取消</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 操作工具栏 -->
    <div class="toolbar-section">
      <div class="toolbar-left">
        <div class="user-badge">
          <span class="badge-icon">🎓</span>
          <span class="badge-text">校内用户专属</span>
        </div>
        <div class="search-box">
          <input
            v-model="filters.search"
            type="text"
            placeholder="搜索车牌号、停车场..."
            @input="handleSearch"
            class="search-input"
          />
          <span class="search-icon">🔍</span>
        </div>
      </div>
      <div class="toolbar-right">
        <button @click="startParking" class="action-btn primary-btn" v-if="stats.currentParking === 0">
          <span class="btn-icon">🅿️</span>
          开始停车
        </button>
        <button @click="refreshData" class="action-btn">
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
          <label>时间范围</label>
          <select v-model="filters.timeRange" @change="handleTimeRangeChange" class="filter-select">
            <option value="">全部时间</option>
            <option value="today">今日</option>
            <option value="week">本周</option>
            <option value="month">本月</option>
            <option value="year">今年</option>
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
          <div class="empty-icon">🚗</div>
          <h4>暂无停车记录</h4>
          <p v-if="filters.status || filters.search || filters.parkingLot">
            没有找到匹配的停车记录，请尝试修改筛选条件
          </p>
          <p v-else>
            您还没有停车记录，点击"开始停车"按钮开始使用吧！
          </p>
          <button @click="startParking" class="start-parking-btn" v-if="!filters.status && !filters.search && !filters.parkingLot">
            <span class="btn-icon">🅿️</span>
            开始停车
          </button>
        </div>

        <!-- 数据表格 -->
        <div v-else class="table-wrapper">
          <table class="data-table">
            <thead>
            <tr>
              <th>车牌号</th>
              <th>停车场</th>
              <th>开始时间</th>
              <th>结束时间</th>
              <th>停车时长</th>
              <th>费用</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="record in parkingRecords" :key="record.id" class="table-row">
              <td class="plate-number">
                <span class="plate-badge">{{ record.plateNumber || '未知车牌' }}</span>
              </td>
              <td class="parking-lot">
                {{ record.parkingLotName || '未知停车场' }}
              </td>
              <td class="time-info">
                <div class="time-value">{{ formatDateTime(record.startTime) }}</div>
              </td>
              <td class="time-info">
                <div class="time-value">{{ record.endTime ? formatDateTime(record.endTime) : '进行中' }}</div>
              </td>
              <td class="duration">
                {{ calculateDuration(record.startTime, record.endTime) }}
              </td>
              <td class="fee-amount">
                  <span v-if="record.fee !== null && record.fee !== undefined" class="fee-text">
                    ¥{{ formatCurrency(record.fee) }}
                  </span>
                <span v-else class="no-fee">免费</span>
              </td>
              <td class="status-cell">
                  <span :class="['status-badge', (record.status || '').toLowerCase()]">
                    {{ getStatusText(record.status) }}
                  </span>
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
                <button
                  v-if="record.status === 'PARKING'"
                  @click="cancelParking(record.id)"
                  class="action-btn cancel-btn"
                  title="取消停车"
                >
                  <span class="btn-icon">❌</span>
                  取消
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
                  <span class="detail-value">{{ selectedRecord.id || '-' }}</span>
                </div>
                <div class="detail-item">
                  <label>车牌号:</label>
                  <span class="detail-value plate-value">{{ selectedRecord.plateNumber || '未知车牌' }}</span>
                </div>
                <div class="detail-item">
                  <label>车辆类型:</label>
                  <span class="detail-value">{{ selectedRecord.vehicleType || '未知' }}</span>
                </div>
              </div>
            </div>

            <div class="detail-section">
              <h4>停车信息</h4>
              <div class="detail-grid">
                <div class="detail-item">
                  <label>停车场:</label>
                  <span class="detail-value">{{ selectedRecord.parkingLotName || '未知停车场' }}</span>
                </div>
                <div class="detail-item">
                  <label>开始时间:</label>
                  <span class="detail-value">{{ formatDateTime(selectedRecord.startTime) }}</span>
                </div>
                <div class="detail-item">
                  <label>结束时间:</label>
                  <span class="detail-value">{{ selectedRecord.endTime ? formatDateTime(selectedRecord.endTime) : '进行中' }}</span>
                </div>
                <div class="detail-item">
                  <label>停车时长:</label>
                  <span class="detail-value">{{ calculateDuration(selectedRecord.startTime, selectedRecord.endTime) }}</span>
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
                  <span class="detail-value fee-amount">
                    {{ selectedRecord.fee !== null && selectedRecord.fee !== undefined ? '¥' + formatCurrency(selectedRecord.fee) : '免费' }}
                  </span>
                </div>
                <div class="detail-item" v-if="selectedRecord.fee === 0">
                  <label>费用说明:</label>
                  <span class="detail-value free-badge">校内用户免费停车</span>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="showDetailModal = false" class="btn btn-primary">关闭</button>
          <button
            v-if="selectedRecord && selectedRecord.status === 'PARKING'"
            @click="endParking(selectedRecord.id)"
            class="btn btn-danger"
          >
            结束停车
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { authFetch } from '@/utils/auth'

const router = useRouter()

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
  timeRange: '',
  search: ''
})

// 分页
const currentPage = ref(1)
const pageSize = ref(10)
const totalRecords = ref(0)

// 统计信息
const stats = ref({
  currentParking: 0,
  completed: 0,
  cancelled: 0,
  total: 0
})

const totalPages = computed(() => {
  return Math.ceil(totalRecords.value / pageSize.value)
})

// 工具函数
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

// 核心功能
const loadParkingRecords = async () => {
  loading.value = true
  error.value = ''

  try {
    // 获取用户角色 - 修正优先级：token.role > localStorage.role > userInfo.role
    let userRole = '';

    // 方式1：从token获取（最准确，代表当前登录用户）
    const token = localStorage.getItem('token');
    if (token) {
      try {
        const payload = token.split('.')[1];
        if (payload) {
          const decoded = JSON.parse(atob(payload));
          userRole = decoded.role || '';
          console.log('🔍 从token获取当前登录用户角色:', userRole);
        }
      } catch (e) {
        console.error('解码token失败:', e);
      }
    }

    // 方式2：如果token没有角色，从localStorage.role获取
    if (!userRole) {
      userRole = localStorage.getItem('role') || '';
      console.log('🔍 从localStorage.role获取角色:', userRole);
    }

    // 方式3：最后从userInfo获取（可能有旧数据）
    if (!userRole) {
      const userInfoStr = localStorage.getItem('userInfo');
      if (userInfoStr) {
        try {
          const userInfo = JSON.parse(userInfoStr);
          userRole = userInfo.role || '';
          console.log('🔍 从userInfo获取角色:', userRole);
        } catch (e) {
          console.error('解析userInfo失败:', e);
        }
      }
    }

    console.log('👤 最终确定的用户角色:', userRole);
    console.log('👤 角色类型:', typeof userRole);

    // 显示所有来源的角色值，便于调试
    console.log('🔍 各来源的角色值对比:');
    console.log('- token.role:', token ? JSON.parse(atob(token.split('.')[1])).role : '无token');
    console.log('- localStorage.role:', localStorage.getItem('role'));
    console.log('- userInfo.role:', JSON.parse(localStorage.getItem('userInfo') || '{}').role);

    // 角色检查 - 使用更宽松的检查
    const upperRole = (userRole || '').toUpperCase().trim();
    const campusRoles = ['STUDENT', 'TEACHER', 'STAFF'];

    if (!campusRoles.includes(upperRole)) {
      console.error('❌ 用户角色不符合校内用户要求');
      console.error('❌ 当前角色:', upperRole);

      // 提供更多调试信息
      console.log('🔍 详细调试信息:');
      console.log('完整token:', token);
      if (token) {
        try {
          const payload = token.split('.')[1];
          console.log('token payload:', payload);
          console.log('token payload解码:', atob(payload));
        } catch (e) {
          console.error('解析token payload失败:', e);
        }
      }

      // 显示用户友好的错误信息
      if (upperRole === 'ADMIN') {
        // 检查是否真的是管理员，还是旧数据
        const tokenRole = token ? JSON.parse(atob(token.split('.')[1])).role : '';
        if (tokenRole === 'STUDENT') {
          error.value = '检测到用户信息冲突，请清除浏览器缓存后重新登录';
          console.error('⚠️ 用户信息冲突：token是STUDENT，但其他来源显示ADMIN');
        } else {
          error.value = '管理员请访问管理员停车记录页面 (/parking-records)';
        }
      } else if (upperRole === 'EXTERNAL_USER') {
        error.value = '校外用户请访问校外用户停车记录页面';
      } else if (!userRole) {
        error.value = '无法获取用户角色信息，请重新登录';
      } else {
        error.value = `您的用户角色"${userRole}"无法访问校内用户页面`;
      }

      // 不再抛出错误，而是设置错误信息
      console.error('❌ 角色检查失败:', error.value);
      parkingRecords.value = [];
      totalRecords.value = 0;
      loading.value = false;
      return; // 直接返回，不继续执行
    }

    console.log('✅ 角色验证通过，开始加载数据...');

    // 构建查询参数
    const params = new URLSearchParams()
    params.append('page', (currentPage.value - 1).toString())
    params.append('size', pageSize.value.toString())

    if (filters.value.parkingLot) {
      params.append('parkingLot', filters.value.parkingLot)
    }
    if (filters.value.status) {
      params.append('status', filters.value.status)
    }
    if (filters.value.search) {
      params.append('search', filters.value.search)
    }

    const url = `/api/parking-records/campus/my-records/page?${params.toString()}`
    console.log('📡 请求校内用户停车记录:', url)

    const response = await authFetch(url)

    if (!response.ok) {
      const errorText = await response.text();
      console.error('❌ API响应错误:', response.status, errorText);

      if (response.status === 403) {
        throw new Error('权限不足：无法访问校内用户数据');
      } else if (response.status === 401) {
        throw new Error('登录已过期，请重新登录');
      } else {
        throw new Error(`请求失败: ${response.status} - ${errorText}`);
      }
    }

    const data = await response.json()
    console.log('📊 校内用户停车记录数据:', data)

    if (data.success && data.data) {
      parkingRecords.value = data.data.records || []
      totalRecords.value = data.data.total || 0

      // 更新统计信息
      updateStats()

      console.log(`✅ 成功加载 ${parkingRecords.value.length} 条记录，总计 ${totalRecords.value} 条`);
    } else {
      throw new Error(data.message || '获取数据失败')
    }
  } catch (err) {
    console.error('❌ 加载校内用户停车记录失败:', err)
    error.value = err.message || '加载停车记录失败'
    parkingRecords.value = []
    totalRecords.value = 0
  } finally {
    loading.value = false
  }
}
// 在组件中添加清理函数
const cleanupUserInfo = () => {
  console.log('🧹 清理用户信息冲突...');

  // 获取当前token中的角色
  const token = localStorage.getItem('token');
  let currentRole = '';

  if (token) {
    try {
      const payload = token.split('.')[1];
      if (payload) {
        const decoded = JSON.parse(atob(payload));
        currentRole = decoded.role || '';
        console.log('🔍 当前token中的角色:', currentRole);
      }
    } catch (e) {
      console.error('解码token失败:', e);
    }
  }

  // 如果当前token有角色，更新所有存储为一致
  if (currentRole) {
    console.log('🔄 更新用户信息为一致的角色:', currentRole);

    // 更新localStorage.role
    localStorage.setItem('role', currentRole);

    // 更新userInfo中的角色
    const userInfoStr = localStorage.getItem('userInfo');
    if (userInfoStr) {
      try {
        const userInfo = JSON.parse(userInfoStr);
        userInfo.role = currentRole;
        localStorage.setItem('userInfo', JSON.stringify(userInfo));
      } catch (e) {
        console.error('更新userInfo失败:', e);
      }
    } else {
      // 创建新的userInfo
      localStorage.setItem('userInfo', JSON.stringify({
        id: 11, // 从token中获取
        username: '学生', // 从token中获取
        role: currentRole
      }));
    }

    console.log('✅ 用户信息已清理并更新为:', currentRole);
    return true;
  }

  return false;
}
// 在组件中添加这个调试函数
const debugUserInfo = () => {
  console.log('🔍 用户信息调试:');
  console.log('1. localStorage.userInfo:', localStorage.getItem('userInfo'));
  console.log('2. localStorage.role:', localStorage.getItem('role'));
  console.log('3. localStorage.token:', localStorage.getItem('token'));

  // 解析 userInfo
  const userInfoStr = localStorage.getItem('userInfo');
  if (userInfoStr) {
    try {
      const userInfo = JSON.parse(userInfoStr);
      console.log('4. 解析的userInfo对象:', userInfo);
      console.log('5. userInfo.role:', userInfo.role);
      console.log('6. userInfo.username:', userInfo.username);
    } catch (e) {
      console.error('解析userInfo失败:', e);
    }
  }

  // 解析 token
  const token = localStorage.getItem('token');
  if (token) {
    try {
      const payload = token.split('.')[1];
      if (payload) {
        const decoded = JSON.parse(atob(payload));
        console.log('7. token解码信息:', decoded);
        console.log('8. token中的role:', decoded.role);
        console.log('9. token中的sub:', decoded.sub);
      }
    } catch (e) {
      console.error('解码token失败:', e);
    }
  }

  console.log('🔍 页面权限检查:');
  const role = localStorage.getItem('role') || (JSON.parse(localStorage.getItem('userInfo') || '{}')).role || '';
  console.log('10. 综合角色:', role);
  console.log('11. 是否校内用户:', ['STUDENT', 'TEACHER', 'STAFF'].includes(role.toUpperCase()));
}
const loadParkingLots = async () => {
  try {
    const response = await authFetch('/api/parking-records/campus/parking-lots')
    if (response.ok) {
      const data = await response.json()
      if (data.success && data.data) {
        parkingLots.value = data.data
      }
    }
  } catch (err) {
    console.error('加载停车场列表失败:', err)
  }
}

const updateStats = () => {
  stats.value = {
    currentParking: parkingRecords.value.filter(r => r.status === 'PARKING').length,
    completed: parkingRecords.value.filter(r => r.status === 'COMPLETED').length,
    cancelled: parkingRecords.value.filter(r => r.status === 'CANCELLED').length,
    total: totalRecords.value
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadParkingRecords()
}

const handleTimeRangeChange = () => {
  // 这里可以根据时间范围筛选
  currentPage.value = 1
  loadParkingRecords()
}

const filterByStatus = (status) => {
  filters.value.status = status
  currentPage.value = 1
  loadParkingRecords()
}

const resetFilters = () => {
  filters.value = {
    parkingLot: '',
    status: '',
    timeRange: '',
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
  currentPage.value = page
  loadParkingRecords()
}

const startParking = () => {
  router.push('/parking')
}

const endParking = async (recordId) => {
  if (!confirm('确定要结束停车吗？')) {
    return
  }

  try {
    const response = await authFetch(`/api/parking-records/${recordId}/end`, {
      method: 'POST'
    })

    if (response.ok) {
      alert('停车已结束')
      refreshData()
    } else {
      throw new Error('结束停车失败')
    }
  } catch (err) {
    console.error('结束停车失败:', err)
    alert('结束停车失败: ' + err.message)
  }
}

const cancelParking = async (recordId) => {
  if (!confirm('确定要取消停车吗？取消后无法恢复。')) {
    return
  }

  try {
    const response = await authFetch(`/api/parking-records/${recordId}/cancel`, {
      method: 'POST'
    })

    if (response.ok) {
      alert('停车已取消')
      refreshData()
    } else {
      throw new Error('取消停车失败')
    }
  } catch (err) {
    console.error('取消停车失败:', err)
    alert('取消停车失败: ' + err.message)
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

<style scoped>
.campus-parking-records {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 30px;
  padding: 25px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  color: white;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-text h1 {
  font-size: 2.2rem;
  margin: 0 0 10px 0;
  font-weight: 700;
}

.header-text p {
  font-size: 1rem;
  opacity: 0.9;
  margin: 0;
}

.header-stats {
  display: flex;
  gap: 25px;
}

.stat-item {
  text-align: center;
  padding: 15px 25px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  min-width: 100px;
}

.stat-item:hover {
  background: rgba(255, 255, 255, 0.25);
  transform: translateY(-2px);
}

.stat-value {
  font-size: 2rem;
  font-weight: 700;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 0.9rem;
  opacity: 0.9;
}

.toolbar-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
  padding: 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.08);
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-badge {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 50px;
  font-weight: 600;
}

.badge-icon {
  font-size: 1.2rem;
}

.search-box {
  position: relative;
}

.search-input {
  padding: 12px 20px 12px 45px;
  border: 2px solid #e2e8f0;
  border-radius: 50px;
  width: 300px;
  font-size: 1rem;
  transition: all 0.3s ease;
}

.search-input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.search-icon {
  position: absolute;
  left: 15px;
  top: 50%;
  transform: translateY(-50%);
  color: #94a3b8;
}

.toolbar-right {
  display: flex;
  gap: 15px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: #f8fafc;
  border: 2px solid #e2e8f0;
  border-radius: 50px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.action-btn:hover {
  background: #f1f5f9;
  border-color: #cbd5e1;
}

.primary-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
}

.primary-btn:hover {
  background: linear-gradient(135deg, #5a6fd8 0%, #6a4190 100%);
}

.filter-card {
  background: white;
  border-radius: 12px;
  padding: 25px;
  margin-bottom: 25px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.08);
}

.filter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.filter-header h3 {
  margin: 0;
  font-size: 1.3rem;
  color: #1e293b;
}

.clear-filters {
  background: none;
  border: none;
  color: #667eea;
  cursor: pointer;
  font-weight: 600;
  padding: 8px 16px;
  border-radius: 6px;
  transition: all 0.3s ease;
}

.clear-filters:hover {
  background: #f8fafc;
}

.filter-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
}

.filter-item label {
  display: block;
  margin-bottom: 8px;
  font-weight: 600;
  color: #475569;
}

.filter-select {
  width: 100%;
  padding: 12px 16px;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  font-size: 1rem;
  background: white;
  transition: all 0.3s ease;
}

.filter-select:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.table-section {
  margin-bottom: 30px;
}

.table-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0,0,0,0.1);
}

.loading-state,
.error-state,
.empty-state {
  padding: 60px 20px;
  text-align: center;
}

.spinner {
  width: 50px;
  height: 50px;
  border: 4px solid #e2e8f0;
  border-top: 4px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.error-icon,
.empty-icon {
  font-size: 3rem;
  margin-bottom: 20px;
}

.retry-btn {
  padding: 10px 24px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  margin-top: 15px;
  transition: background 0.3s ease;
}

.retry-btn:hover {
  background: #5a6fd8;
}

.start-parking-btn {
  padding: 12px 30px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 50px;
  font-size: 1.1rem;
  font-weight: 600;
  cursor: pointer;
  margin-top: 20px;
  transition: all 0.3s ease;
}

.start-parking-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(102, 126, 234, 0.3);
}

.table-wrapper {
  overflow-x: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table thead {
  background: #f8fafc;
  border-bottom: 2px solid #e2e8f0;
}

.data-table th {
  padding: 18px 16px;
  text-align: left;
  font-weight: 700;
  color: #475569;
  font-size: 0.95rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.data-table tbody tr {
  border-bottom: 1px solid #f1f5f9;
  transition: background 0.3s ease;
}

.data-table tbody tr:hover {
  background: #f8fafc;
}

.data-table td {
  padding: 16px;
  color: #334155;
}

.plate-badge {
  display: inline-block;
  padding: 6px 12px;
  background: #dbeafe;
  color: #1e40af;
  border-radius: 6px;
  font-weight: 600;
  font-family: 'Courier New', monospace;
}

.status-badge {
  display: inline-block;
  padding: 6px 12px;
  border-radius: 50px;
  font-size: 0.85rem;
  font-weight: 600;
}

.status-badge.parking {
  background: #fef3c7;
  color: #92400e;
}

.status-badge.completed {
  background: #d1fae5;
  color: #065f46;
}

.status-badge.cancelled {
  background: #fee2e2;
  color: #991b1b;
}

.fee-text {
  font-weight: 600;
  color: #059669;
}

.no-fee {
  color: #94a3b8;
  font-style: italic;
}

.actions {
  display: flex;
  gap: 8px;
}

.end-btn {
  background: #fef3c7;
  color: #92400e;
  border: 1px solid #fbbf24;
}

.view-btn {
  background: #dbeafe;
  color: #1e40af;
  border: 1px solid #93c5fd;
}

.cancel-btn {
  background: #fee2e2;
  color: #991b1b;
  border: 1px solid #fca5a5;
}

.action-btn {
  padding: 6px 12px;
  border-radius: 6px;
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.action-btn:hover {
  transform: translateY(-1px);
  opacity: 0.9;
}

.pagination-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: #f8fafc;
  border-top: 1px solid #e2e8f0;
}

.pagination-info {
  color: #64748b;
  font-size: 0.9rem;
}

.pagination-controls {
  display: flex;
  align-items: center;
  gap: 20px;
}

.page-btn {
  padding: 10px 20px;
  background: white;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.3s ease;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-btn:not(:disabled):hover {
  border-color: #667eea;
  color: #667eea;
}

.page-numbers {
  display: flex;
  gap: 10px;
  color: #64748b;
  font-weight: 600;
}

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
}

.modal-content {
  background: white;
  border-radius: 16px;
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 25px;
  border-bottom: 1px solid #e2e8f0;
}

.modal-header h3 {
  margin: 0;
  font-size: 1.5rem;
  color: #1e293b;
}

.close-btn {
  background: none;
  border: none;
  font-size: 2rem;
  color: #64748b;
  cursor: pointer;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.3s ease;
}

.close-btn:hover {
  background: #f1f5f9;
  color: #475569;
}

.modal-body {
  padding: 25px;
}

.detail-sections {
  display: flex;
  flex-direction: column;
  gap: 25px;
}

.detail-section h4 {
  margin: 0 0 15px 0;
  color: #475569;
  font-size: 1.1rem;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 15px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.detail-item label {
  font-size: 0.9rem;
  color: #64748b;
  font-weight: 500;
}

.detail-value {
  font-size: 1rem;
  color: #1e293b;
  font-weight: 600;
}

.plate-value {
  color: #1e40af;
  font-family: 'Courier New', monospace;
}

.fee-amount {
  color: #059669;
  font-weight: 700;
}

.free-badge {
  background: #d1fae5;
  color: #065f46;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 0.9rem;
}

.modal-footer {
  padding: 20px 25px;
  border-top: 1px solid #e2e8f0;
  display: flex;
  justify-content: flex-end;
  gap: 15px;
}

.btn {
  padding: 12px 24px;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-primary {
  background: #667eea;
  color: white;
  border: none;
}

.btn-primary:hover {
  background: #5a6fd8;
}

.btn-danger {
  background: #ef4444;
  color: white;
  border: none;
}

.btn-danger:hover {
  background: #dc2626;
}
</style>
