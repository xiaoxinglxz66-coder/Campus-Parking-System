<template>
  <div class="vehicle-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1 class="page-title">
        <span class="title-icon">🚗</span>
        车辆管理系统
        <span v-if="loading" class="sync-status syncing">🔄 同步中...</span>
        <span v-else class="sync-status synced">✅ 已同步数据库</span>
      </h1>
      <p class="page-subtitle">
        车辆审核与信息管理 - 当前角色: {{ currentUserRole }}
        <span v-if="currentUserRole === '管理员'" class="role-badge">校内用户专属</span>
      </p>
    </div>

    <div class="global-actions">
      <button class="btn-refresh" @click="refreshAllData" :disabled="loading">
        <span class="btn-icon">🔄</span>
        {{ loading ? '同步中...' : '同步数据' }}
      </button>

      <!-- 🔧 添加调试按钮（仅开发环境显示） -->
      <template v-if="isDevelopment">
        <button class="btn-debug" @click="showDebugTools = !showDebugTools">
          <span class="btn-icon">🔧</span>
          调试工具
        </button>

        <div v-if="showDebugTools" class="debug-tools">
          <button class="btn-small" @click="checkVehicleConstraints">检查约束</button>
          <button class="btn-small" @click="fixAllConstraints">修复所有约束</button>
        </div>
      </template>
    </div>

    <!-- 主要内容区域 - 上下结构 -->
    <div class="main-content">
      <!-- 上半部分：车辆审核框架（仅管理员可见） -->
      <div class="approval-section" v-if="currentUserRole === '管理员'">
        <div class="section-header">
          <h2 class="section-title">车辆审核管理</h2>
          <div class="section-stats">
            <span class="stat-item">待审核: {{ approvalStats.pending }}</span>
            <span class="stat-item">已通过: {{ approvalStats.approved }}</span>
            <span class="stat-item">已拒绝: {{ approvalStats.rejected }}</span>
          </div>
        </div>

        <!-- 审核状态标签 -->
        <div class="approval-tabs">
          <button
            v-for="tab in approvalTabs"
            :key="tab.value"
            class="tab-button"
            :class="{ active: currentApprovalTab === tab.value }"
            @click="switchApprovalTab(tab.value)"
          >
            <span class="tab-label">{{ tab.label }}</span>
            <span class="tab-count" v-if="tab.count > 0">{{ tab.count }}</span>
          </button>
        </div>

        <!-- 审核车辆列表 -->
        <div class="approval-list" v-if="filteredApprovalVehicles.length > 0">
          <div v-for="vehicle in filteredApprovalVehicles" :key="vehicle.id" class="approval-card">
            <div class="vehicle-basic-info">
              <div class="plate-number">{{ vehicle.plateNumber }}</div>
              <div class="vehicle-type">{{ getVehicleTypeText(vehicle.vehicleType) }}</div>
              <div class="status-badge" :class="getStatusClass(vehicle.status)">
                {{ getStatusText(vehicle.status) }}
              </div>
              <div v-if="vehicle.isTemporary" class="temp-indicator">临时</div>
            </div>

            <div class="vehicle-details">
              <div class="detail-row">
                <div class="detail-item">
                  <span class="label">品牌：</span>
                  <span class="value">{{ vehicle.brand || '未设置' }}</span>
                </div>
                <div class="detail-item">
                  <span class="label">颜色：</span>
                  <span class="value">{{ vehicle.color || '未设置' }}</span>
                </div>
              </div>
              <div class="detail-row">
                <div class="detail-item">
                  <span class="label">车主：</span>
                  <span class="value">{{ vehicle.userRealName || vehicle.userName || '未知' }}</span>
                </div>
                <div class="detail-item">
                  <span class="label">用户类型：</span>
                  <span class="value">{{ getUserTypeText(vehicle.userType) }}</span>
                </div>
              </div>
              <div class="time-info">
                <span class="time-text">提交时间：{{ formatDate(vehicle.createdAt) }}</span>
              </div>
            </div>

            <!-- 审核操作 -->
            <div class="approval-actions" v-if="vehicle.status === 'PENDING'">
              <button class="btn-approve" @click="approveVehicle(vehicle.id, true)">
                <span class="btn-icon">✅</span>
                通过审核
              </button>
              <button class="btn-reject" @click="approveVehicle(vehicle.id, false)">
                <span class="btn-icon">❌</span>
                拒绝审核
              </button>
            </div>

            <div class="approval-result" v-else>
              <span :class="vehicle.status === 'APPROVED' ? 'approved-text' : 'rejected-text'">
                {{ vehicle.status === 'APPROVED' ? '✅ 已通过审核' : '❌ 已拒绝审核' }}
              </span>
              <span class="result-time">{{ formatDate(vehicle.updatedAt) }}</span>
            </div>
          </div>
        </div>

        <!-- 审核空状态 -->
        <div v-else class="empty-approval">
          <div class="empty-icon">
            <span v-if="currentApprovalTab === 'PENDING'">⏳</span>
            <span v-else-if="currentApprovalTab === 'APPROVED'">✅</span>
            <span v-else>❌</span>
          </div>
          <h3>{{ getApprovalEmptyTitle() }}</h3>
          <p>{{ getApprovalEmptyDescription() }}</p>
        </div>
      </div>

      <!-- 下半部分：车辆信息管理框架 -->
      <div class="management-section">
        <div class="section-header">
          <h2 class="section-title">
            {{ currentUserRole === '管理员' ? '校内用户车辆管理' : '我的车辆管理' }}
          </h2>
          <div class="section-actions">
            <button class="btn-primary" @click="showAddModal = true" v-if="currentUserRole !== '管理员'">
              <span class="btn-icon">➕</span>
              添加车辆
            </button>
            <button class="btn-refresh" @click="refreshManagementData" :disabled="loading">
              <span class="btn-icon">🔄</span>
              刷新数据
            </button>
          </div>
        </div>

        <!-- 数据信息提示 -->
        <div v-if="currentUserRole === '管理员'" class="data-info">
          <div class="info-card">
            <div class="info-icon">📋</div>
            <div class="info-content">
              <h4>数据说明</h4>
              <p>• 仅显示校内用户（学生、教师、职工）的车辆</p>
              <p>• 校外用户和临时车辆已自动过滤</p>
              <p>• 删除车辆时会自动删除相关停车记录</p>
              <p>• 数据实时同步数据库，无模拟数据</p>
            </div>
          </div>
        </div>

        <!-- 搜索和筛选（管理员专用） -->
        <div class="management-filters" v-if="currentUserRole === '管理员'">
          <div class="filter-group">
            <input
              v-model="searchKeyword"
              type="text"
              placeholder="搜索车牌号、车主姓名..."
              class="search-input"
              @input="handleSearch"
            >
            <select v-model="filterStatus" @change="handleFilter" class="filter-select">
              <option value="">全部状态</option>
              <option value="PENDING">待审核</option>
              <option value="APPROVED">已通过</option>
              <option value="REJECTED">已拒绝</option>
            </select>
            <select v-model="filterUserType" @change="handleFilter" class="filter-select">
              <option value="">全部用户类型</option>
              <option value="STUDENT">学生</option>
              <option value="TEACHER">教师</option>
              <option value="STAFF">职工</option>
              <!-- 已移除校外用户选项 -->
            </select>
          </div>
        </div>

        <!-- 数据表格区域 -->
        <div class="table-section">
          <!-- 加载状态 -->
          <div v-if="loading && userVehicles.length === 0" class="loading-state">
            <div class="spinner"></div>
            <p>正在从数据库加载车辆数据...</p>
          </div>

          <!-- 错误状态 -->
          <div v-else-if="error" class="error-state">
            <div class="error-icon">❌</div>
            <div class="error-content">
              <h4>加载失败</h4>
              <p>{{ error }}</p>
              <div class="error-actions">
                <button @click="loadUserVehicles" class="retry-btn">重新加载</button>
                <button @click="checkDatabaseStatus" class="debug-btn">检查数据库</button>
              </div>
            </div>
          </div>

          <!-- 空状态 -->
          <div v-else-if="filteredManagementVehicles.length === 0" class="empty-state">
            <div class="empty-icon">🚗</div>
            <h4>{{ currentUserRole === '管理员' ? '暂无校内用户车辆数据' : '您暂无车辆' }}</h4>
            <p>{{ currentUserRole === '管理员' ? '数据库中暂无校内用户车辆数据' : '您还没有添加任何车辆，点击上方按钮添加第一辆车' }}</p>
            <button v-if="currentUserRole === '管理员'" @click="checkDatabaseStatus" class="check-db-btn">
              检查数据库状态
            </button>
          </div>

          <!-- 数据表格 -->
          <div v-else class="table-wrapper">
            <table class="data-table">
              <thead>
              <tr>
                <th>车牌号</th>
                <th>车辆类型</th>
                <th v-if="currentUserRole === '管理员'">车主信息</th>
                <th>品牌/颜色</th>
                <th>状态</th>
                <th>注册时间</th>
                <th>操作</th>
              </tr>
              </thead>
              <tbody>
              <tr v-for="vehicle in filteredManagementVehicles" :key="vehicle.id" class="table-row">
                <!-- 车牌号 -->
                <td class="plate-number">
                  <span class="plate-badge">{{ vehicle.plateNumber || '未知车牌' }}</span>
                  <span v-if="vehicle.isTemporary" class="temp-indicator">临时</span>
                </td>

                <!-- 车辆类型 -->
                <td class="vehicle-type-cell">
                  <span class="vehicle-type-badge">{{ getVehicleTypeText(vehicle.vehicleType) }}</span>
                </td>

                <!-- 车主信息（仅管理员可见） -->
                <td v-if="currentUserRole === '管理员'" class="owner-info">
                  <div class="user-name">{{ vehicle.userRealName || vehicle.userName || '未知用户' }}</div>
                  <div class="user-type">
                    <span :class="['user-type-badge', (vehicle.userType || '').toLowerCase()]">
                      {{ getUserTypeText(vehicle.userType) }}
                    </span>
                  </div>
                </td>

                <!-- 品牌/颜色 -->
                <td class="vehicle-details">
                  <div class="brand">{{ vehicle.brand || '未设置' }}</div>
                  <div class="color">{{ vehicle.color || '未设置' }}</div>
                </td>

                <!-- 状态 -->
                <td class="status-cell">
                  <div class="status-badge-wrapper">
                    <span :class="['status-badge', (vehicle.status || '').toLowerCase()]">
                      {{ getStatusText(vehicle.status) }}
                    </span>
                  </div>
                </td>

                <!-- 注册时间 -->
                <td class="time-info">
                  <div class="register-time">{{ formatDate(vehicle.createdAt) }}</div>
                  <div v-if="vehicle.updatedAt && vehicle.status !== 'PENDING'" class="update-time">
                    更新: {{ formatDate(vehicle.updatedAt) }}
                  </div>
                </td>

                <!-- 操作 -->
                <td class="actions">
                  <!-- 普通用户的操作 -->
                  <template v-if="currentUserRole !== '管理员'">
                    <button class="action-btn edit-btn" @click="editVehicle(vehicle)"
                            :disabled="vehicle.status !== 'APPROVED'" title="编辑">
                      <span class="btn-icon">✏️</span>
                    </button>
                    <button class="action-btn delete-btn" @click="deleteVehicle(vehicle.id)"
                            :disabled="vehicle.status === 'PENDING' || isVehicleInParking(vehicle.id)"
                            title="删除">
                      <span class="btn-icon">🗑️</span>
                    </button>
                  </template>

                  <!-- 管理员的操作 -->
                  <template v-else>
                    <button class="action-btn view-btn" @click="viewVehicleDetails(vehicle)" title="查看详情">
                      <span class="btn-icon">👁️</span>
                    </button>
                    <button class="action-btn edit-btn" @click="editVehicle(vehicle)" title="编辑">
                      <span class="btn-icon">✏️</span>
                    </button>
                    <!-- 管理员可以审核待审核车辆 -->
                    <button v-if="vehicle.status === 'PENDING'"
                            class="action-btn approve-btn" @click="approveVehicle(vehicle.id, true)" title="通过审核">
                      <span class="btn-icon">✅</span>
                    </button>
                    <button v-if="vehicle.status === 'PENDING'"
                            class="action-btn reject-btn" @click="approveVehicle(vehicle.id, false)" title="拒绝审核">
                      <span class="btn-icon">❌</span>
                    </button>
                    <button class="action-btn delete-btn" @click="deleteVehicle(vehicle.id)"
                            :disabled="isVehicleInParking(vehicle.id)" title="删除（连带停车记录）">
                      <span class="btn-icon">🗑️</span>
                    </button>
                  </template>
                </td>
              </tr>
              </tbody>
            </table>
          </div>

          <!-- 分页控件 -->
          <div v-if="filteredManagementVehicles.length > 0" class="pagination-section">
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
    </div>

    <!-- 添加/编辑车辆弹窗 -->
    <div v-if="showAddModal || showEditModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>{{ showEditModal ? '编辑车辆' : '添加车辆' }}</h3>
          <button class="close-btn" @click="closeModal">×</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="submitVehicle">
            <div class="form-group">
              <label>车牌号 *</label>
              <input v-model="currentVehicle.plateNumber" type="text" required
                     placeholder="请输入车牌号" :disabled="showEditModal">
              <div class="form-hint">格式：汉字+字母+5位数字（如：粤A12345）</div>
            </div>
            <div class="form-group">
              <label>车辆类型</label>
              <select v-model="currentVehicle.vehicleType">
                <option value="CAR">小型汽车</option>
                <option value="MOTORCYCLE">摩托车</option>
                <option value="ELECTRIC_CAR">电动车</option>
              </select>
            </div>
            <div class="form-group">
              <label>品牌</label>
              <input v-model="currentVehicle.brand" type="text" placeholder="请输入车辆品牌">
            </div>
            <div class="form-group">
              <label>颜色</label>
              <input v-model="currentVehicle.color" type="text" placeholder="请输入车辆颜色">
            </div>
          </form>
        </div>
        <div class="modal-footer">
          <button class="btn-secondary" @click="closeModal">取消</button>
          <button class="btn-primary" @click="submitVehicle" :disabled="loading">
            {{ loading ? '处理中...' : (showEditModal ? '更新' : '添加') }}
          </button>
        </div>
      </div>
    </div>

    <!-- 数据库状态弹窗 -->
    <div v-if="showDatabaseDialog" class="modal-overlay" @click.self="showDatabaseDialog = false">
      <div class="modal-content">
        <div class="modal-header">
          <h3>数据库状态</h3>
          <button class="close-btn" @click="showDatabaseDialog = false">×</button>
        </div>
        <div class="modal-body">
          <div v-if="databaseStatus.loading" class="loading-status">
            <div class="spinner small"></div>
            <p>正在检查数据库状态...</p>
          </div>
          <div v-else-if="databaseStatus.error" class="error-status">
            <div class="error-icon">❌</div>
            <h4>数据库检查失败</h4>
            <p>{{ databaseStatus.error }}</p>
          </div>
          <div v-else class="database-stats">
            <div class="stat-item">
              <div class="stat-label">总车辆数</div>
              <div class="stat-value">{{ databaseStatus.totalVehicles || 0 }}</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">校内用户车辆</div>
              <div class="stat-value">{{ databaseStatus.campusVehicles || 0 }}</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">校外用户车辆</div>
              <div class="stat-value">{{ databaseStatus.externalVehicles || 0 }}</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">停车记录数</div>
              <div class="stat-value">{{ databaseStatus.totalParkingRecords || 0 }}</div>
            </div>
            <div class="stat-info">
              <p>当前页面显示：<strong>{{ userVehicles.length }}</strong> 辆校内用户车辆</p>
              <p>校外用户车辆已自动过滤</p>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-secondary" @click="showDatabaseDialog = false">关闭</button>
          <button class="btn-primary" @click="refreshDatabaseStatus">刷新</button>
        </div>
      </div>
    </div>

    <!-- 全局加载状态 -->
    <div v-if="loading && showLoadingOverlay" class="loading-overlay">
      <div class="loading-spinner"></div>
      <p>正在同步数据库...</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

// ========== 响应式数据 ==========
const userVehicles = ref([])           // 用户车辆数据（完全来自数据库）
const approvalVehicles = ref([])       // 待审核车辆数据（仅管理员）
const loading = ref(false)             // 加载状态
const showAddModal = ref(false)        // 添加弹窗
const showEditModal = ref(false)       // 编辑弹窗
const currentApprovalTab = ref('PENDING') // 当前审核标签
const searchKeyword = ref('')          // 搜索关键词
const filterStatus = ref('')           // 状态筛选
const filterUserType = ref('')         // 用户类型筛选
const currentPage = ref(1)             // 当前页码
const pageSize = ref(10)               // 每页数量
const totalRecords = ref(0)            // 总记录数
const error = ref(null)                // 错误信息
const showLoadingOverlay = ref(false)  // 显示全局加载
const showDatabaseDialog = ref(false)  // 数据库状态弹窗
const databaseStatus = ref({           // 数据库状态
  loading: false,
  totalVehicles: 0,
  campusVehicles: 0,
  externalVehicles: 0,
  totalParkingRecords: 0,
  error: null
})

// 当前编辑的车辆信息
const currentVehicle = ref({
  plateNumber: '',
  vehicleType: 'CAR',
  brand: '',
  color: ''
})

// 审核标签配置
const approvalTabs = ref([
  { label: '待审核', value: 'PENDING', count: 0 },
  { label: '已通过', value: 'APPROVED', count: 0 },
  { label: '已拒绝', value: 'REJECTED', count: 0 }
])

// ========== 计算属性 ==========
const currentUserRole = computed(() => {
  const role = localStorage.getItem('userRole') || localStorage.getItem('role')
  const roleMap = {
    'ADMIN': '管理员',
    'STUDENT': '学生',
    'TEACHER': '教师',
    'STAFF': '职工',
    'EXTERNAL_USER': '校外用户'
  }
  return roleMap[role] || role || '未知'
})

const filteredApprovalVehicles = computed(() => {
  return approvalVehicles.value.filter(vehicle => vehicle.status === currentApprovalTab.value)
})

const filteredManagementVehicles = computed(() => {
  let vehicles = userVehicles.value

  // 管理员筛选逻辑
  if (currentUserRole.value === '管理员') {
    if (filterStatus.value) {
      vehicles = vehicles.filter(v => v.status === filterStatus.value)
    }
    if (filterUserType.value) {
      vehicles = vehicles.filter(v => v.userType === filterUserType.value)
    }
    if (searchKeyword.value) {
      const keyword = searchKeyword.value.toLowerCase()
      vehicles = vehicles.filter(v =>
        v.plateNumber.toLowerCase().includes(keyword) ||
        (v.userRealName && v.userRealName.toLowerCase().includes(keyword)) ||
        (v.userName && v.userName.toLowerCase().includes(keyword))
      )
    }
  }

  // 更新总记录数
  totalRecords.value = vehicles.length

  // 前端分页
  const startIndex = (currentPage.value - 1) * pageSize.value
  const endIndex = startIndex + pageSize.value

  return vehicles.slice(startIndex, endIndex)
})

const approvalStats = computed(() => {
  const pending = approvalVehicles.value.filter(v => v.status === 'PENDING').length
  const approved = approvalVehicles.value.filter(v => v.status === 'APPROVED').length
  const rejected = approvalVehicles.value.filter(v => v.status === 'REJECTED').length

  // 更新标签计数
  approvalTabs.value.forEach(tab => {
    if (tab.value === 'PENDING') tab.count = pending
    if (tab.value === 'APPROVED') tab.count = approved
    if (tab.value === 'REJECTED') tab.count = rejected
  })

  return { pending, approved, rejected }
})

const totalPages = computed(() => {
  return Math.ceil(totalRecords.value / pageSize.value)
})

// ========== 工具方法 ==========
const getVehicleTypeText = (type) => {
  const typeMap = {
    'CAR': '小型汽车',
    'MOTORCYCLE': '摩托车',
    'ELECTRIC_CAR': '电动车'
  }
  return typeMap[type] || type || '未知类型'
}

const getStatusText = (status) => {
  const statusMap = {
    'PENDING': '待审核',
    'APPROVED': '已通过',
    'REJECTED': '已拒绝'
  }
  return statusMap[status] || status || '未知状态'
}

const getStatusClass = (status) => {
  return status ? status.toLowerCase() : ''
}

const getUserTypeText = (userType) => {
  const userTypeMap = {
    'STUDENT': '学生',
    'TEACHER': '教师',
    'STAFF': '职工',
    'ADMIN': '管理员',
    'EXTERNAL_USER': '校外用户'
  }
  return userTypeMap[userType] || userType || '未知用户类型'
}

const formatDate = (dateString) => {
  if (!dateString) return '未知时间'
  try {
    const date = new Date(dateString)
    return date.toLocaleString('zh-CN')
  } catch {
    return '时间格式错误'
  }
}

const getApprovalEmptyTitle = () => {
  const titleMap = {
    'PENDING': '暂无待审核车辆',
    'APPROVED': '暂无已通过车辆',
    'REJECTED': '暂无已拒绝车辆'
  }
  return titleMap[currentApprovalTab.value] || '暂无数据'
}

const getApprovalEmptyDescription = () => {
  const descMap = {
    'PENDING': '当前没有需要审核的车辆申请',
    'APPROVED': '没有已通过审核的车辆记录',
    'REJECTED': '没有已拒绝的车辆记录'
  }
  return descMap[currentApprovalTab.value] || ''
}

// ========== API 请求方法 ==========
const makeApiRequest = async (url, method = 'GET', body = null) => {
  try {
    const token = localStorage.getItem('token')

    const headers = {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    }

    const options = {
      method,
      headers
    }

    if (body && (method === 'POST' || method === 'PUT' || method === 'DELETE')) {
      options.body = JSON.stringify(body)
    }

    console.log(`📡 API请求: ${method} ${url}`)

    const response = await fetch(url, options)

    if (!response.ok) {
      const errorText = await response.text()
      console.error(`❌ API请求失败 ${response.status}:`, errorText)
      return {
        success: false,
        error: `请求失败: ${response.status} ${response.statusText}`,
        status: response.status,
        errorData: errorText
      }
    }

    // 处理204 No Content
    if (response.status === 204) {
      return { success: true, data: null, status: 204 }
    }

    const contentType = response.headers.get('content-type') || ''
    if (contentType.includes('application/json')) {
      const data = await response.json()
      return { success: true, data, status: response.status }
    } else {
      const text = await response.text()
      return { success: true, data: text, status: response.status }
    }

  } catch (error) {
    console.error('💥 API请求异常:', error)
    return {
      success: false,
      error: `网络请求失败: ${error.message || '请检查网络连接'}`
    }
  }
}
const showDebugTools = ref(false)
const debugVehicleId = ref('')
const constraintResult = ref(null)
const isDevelopment = ref(process.env.NODE_ENV === 'development')

// 调试方法
const checkVehicleConstraints = async () => {
  try {
    if (!debugVehicleId.value) {
      alert('请输入车辆ID')
      return
    }

    const result = await makeApiRequest(`/api/debug/check-vehicle-constraints/${debugVehicleId.value}`, 'GET')

    if (result.success) {
      constraintResult.value = result.data
      console.log('🔧 约束检查结果:', result.data)
    } else {
      alert('检查失败: ' + result.error)
    }
  } catch (error) {
    console.error('❌ 约束检查失败:', error)
    alert('检查失败: ' + error.message)
  }
}

const fixAllConstraints = async () => {
  if (!confirm('确定要修复所有约束问题吗？这可能需要一些时间。')) {
    return
  }

  try {
    const result = await makeApiRequest('/api/debug/fix-all-constraints', 'GET')

    if (result.success) {
      console.log('🔧 约束修复结果:', result.data)
      alert(`修复完成！\n总计: ${result.data.totalVehicles} 辆车\n修复: ${result.data.fixedCount} 处\n错误: ${result.data.errorCount} 处`)
    } else {
      alert('修复失败: ' + result.error)
    }
  } catch (error) {
    console.error('❌ 约束修复失败:', error)
    alert('修复失败: ' + error.message)
  }
}

const forceDeleteVehicle = async (vehicleId) => {
  if (!confirm('⚠️ 警告：强制删除会忽略外键约束，可能导致数据不一致！\n\n确定要强制删除车辆 ' + vehicleId + ' 吗？')) {
    return
  }

  try {
    const result = await makeApiRequest(`/api/debug/force-delete-vehicle/${vehicleId}`, 'DELETE')

    if (result.success) {
      console.log('🔧 强制删除结果:', result.data)
      alert(`强制删除成功！\n删除记录: ${result.data.deletedRecords || 0} 条`)

      // 刷新数据
      await loadUserVehicles()
    } else {
      alert('强制删除失败: ' + result.error)
    }
  } catch (error) {
    console.error('❌ 强制删除失败:', error)
    alert('强制删除失败: ' + error.message)
  }
}

// ========== 核心数据加载方法 ==========
// 修改第311-400行的代码，替换为：

const loadUserVehicles = async () => {
  try {
    console.log('🔄 开始加载车辆数据')
    loading.value = true
    error.value = null

    const token = localStorage.getItem('token')
    const userRole = localStorage.getItem('userRole') || localStorage.getItem('role')

    console.log('👤 用户角色:', userRole)

    // 🔧 直接调用API，不进行复杂过滤
    let result
    if (userRole === 'ADMIN') {
      console.log('👮 管理员模式')
      result = await makeApiRequest('/api/vehicles/all-dto', 'GET')
    } else {
      console.log('👤 普通用户模式')
      result = await makeApiRequest('/api/vehicles/my-vehicles', 'GET')
    }

    // 🔧 直接使用数据，不进行任何过滤
    if (result.success && Array.isArray(result.data)) {
      console.log('📦 原始数据:', result.data.length, '条')

      // 调试：显示每条数据的关键信息
      result.data.forEach((item, index) => {
        console.log(`  ${index+1}. ${item.plateNumber} - ${item.status} - ${item.userType}`)
      })

      // 🔧 关键修复：直接赋值，不进行过滤
      userVehicles.value = result.data

      // 如果是管理员，加载待审核车辆
      if (userRole === 'ADMIN') {
        const pendingResult = await makeApiRequest('/api/vehicles/pending', 'GET')
        if (pendingResult.success && Array.isArray(pendingResult.data)) {
          approvalVehicles.value = pendingResult.data
          console.log('📋 待审核车辆:', approvalVehicles.value.length, '条')
        }
      }
    } else {
      console.error('❌ 数据加载失败:', result)
      error.value = result.error || '加载失败'
    }

  } catch (err) {
    console.error('💥 加载异常:', err)
    error.value = '加载异常: ' + err.message
  } finally {
    loading.value = false
    showLoadingOverlay.value = false
  }
}

// ========== 数据库状态检查 ==========
const checkDatabaseStatus = async () => {
  try {
    showDatabaseDialog.value = true
    databaseStatus.value = { loading: true, error: null }

    const result = await makeApiRequest('/api/vehicles/debug/check-database', 'GET')

    if (result.success) {
      databaseStatus.value = {
        ...result.data,
        loading: false,
        error: null
      }
      console.log('📊 数据库状态:', result.data)
    } else {
      databaseStatus.value = {
        loading: false,
        error: result.error || '获取数据库状态失败'
      }
    }
  } catch (err) {
    console.error('❌ 检查数据库状态失败:', err)
    databaseStatus.value = {
      loading: false,
      error: `检查失败: ${err.message}`
    }
  }
}

const refreshDatabaseStatus = async () => {
  await checkDatabaseStatus()
}

// ========== 业务操作方法 ==========
const switchApprovalTab = (tab) => {
  currentApprovalTab.value = tab
}

const refreshAllData = async () => {
  console.log('🔄 刷新所有数据...')
  currentPage.value = 1
  searchKeyword.value = ''
  filterStatus.value = ''
  filterUserType.value = ''
  await loadUserVehicles()
}

const refreshManagementData = async () => {
  console.log('🔄 刷新管理数据...')
  currentPage.value = 1
  await loadUserVehicles()
}

const changePage = (page) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
  }
}

const handleSearch = () => {
  currentPage.value = 1
}

const handleFilter = () => {
  currentPage.value = 1
}

// ========== 车辆操作方法 ==========
const approveVehicle = async (vehicleId, approve) => {
  try {
    const vehicle = userVehicles.value.find(v => v.id === vehicleId) ||
      approvalVehicles.value.find(v => v.id === vehicleId)

    if (!vehicle) {
      alert('未找到车辆信息')
      return
    }

    const action = approve ? '通过' : '拒绝'
    const confirmMessage = `确定要${action}审核车牌号为 "${vehicle.plateNumber}" 的车辆吗？`

    if (!confirm(confirmMessage)) {
      return
    }

    console.log(`📋 审核车辆 ${vehicleId}: ${action}`)

    const result = await makeApiRequest(`/api/vehicles/${vehicleId}/approve`, 'POST', {
      approved: approve,
      reviewComment: `${action}审核 - ${new Date().toLocaleString('zh-CN')}`
    })

    if (result.success) {
      console.log('✅ 审核成功:', result.data)
      alert(`✅ 车辆 "${vehicle.plateNumber}" ${action}审核成功！`)

      // 更新本地数据
      updateVehicleInLists(result.data)

      // 刷新数据
      setTimeout(() => {
        refreshAllData()
      }, 500)
    } else {
      alert(`❌ 审核失败: ${result.error}`)
    }
  } catch (error) {
    console.error('💥 审核异常:', error)
    alert('💥 审核过程中发生异常，请稍后重试')
  }
}

const addVehicle = async () => {
  try {
    if (!currentVehicle.value.plateNumber || !currentVehicle.value.plateNumber.trim()) {
      alert('请输入车牌号')
      return
    }

    // 车牌号格式验证
    const plateRegex = /^[\u4e00-\u9fa5][A-Za-z][A-Za-z0-9]{5}$/
    if (!plateRegex.test(currentVehicle.value.plateNumber.trim())) {
      alert('请输入正确的车牌号格式（如：粤A12345）')
      return
    }

    loading.value = true

    const vehicleData = {
      plateNumber: currentVehicle.value.plateNumber.trim(),
      vehicleType: currentVehicle.value.vehicleType,
      brand: currentVehicle.value.brand.trim() || '',
      color: currentVehicle.value.color.trim() || ''
    }

    console.log('➕ 添加车辆:', vehicleData)

    let result
    if (currentUserRole.value === '管理员') {
      result = await makeApiRequest('/api/vehicles', 'POST', vehicleData)
    } else {
      result = await makeApiRequest('/api/vehicles/my-vehicles', 'POST', vehicleData)
    }

    if (result.success) {
      console.log('✅ 车辆添加成功:', result.data)
      alert('✅ 车辆添加成功！' + (currentUserRole.value !== '管理员' ? '等待管理员审核。' : ''))
      closeModal()
      await loadUserVehicles()
    } else {
      throw new Error(result.error)
    }
  } catch (error) {
    console.error('❌ 添加车辆失败:', error)
    alert(`❌ 添加失败: ${error.message}`)
  } finally {
    loading.value = false
  }
}

const updateVehicle = async () => {
  try {
    if (!currentVehicle.value.id) {
      throw new Error('车辆ID不存在')
    }

    if (!currentVehicle.value.plateNumber || !currentVehicle.value.plateNumber.trim()) {
      alert('请输入车牌号')
      return
    }

    const plateRegex = /^[\u4e00-\u9fa5][A-Za-z][A-Za-z0-9]{5}$/
    if (!plateRegex.test(currentVehicle.value.plateNumber.trim())) {
      alert('请输入正确的车牌号格式（如：粤A12345）')
      return
    }

    loading.value = true

    const vehicleData = {
      plateNumber: currentVehicle.value.plateNumber.trim(),
      vehicleType: currentVehicle.value.vehicleType,
      brand: currentVehicle.value.brand.trim() || '',
      color: currentVehicle.value.color.trim() || ''
    }

    console.log(`✏️ 更新车辆: ID=${currentVehicle.value.id}`, vehicleData)

    const result = await makeApiRequest(`/api/vehicles/${currentVehicle.value.id}`, 'PUT', vehicleData)

    if (result.success) {
      console.log('✅ 车辆更新成功:', result.data)
      updateVehicleInLists(result.data)
      alert('✅ 车辆信息更新成功！')
      closeModal()
      await loadUserVehicles()
    } else {
      throw new Error(result.error)
    }
  } catch (error) {
    console.error('❌ 更新车辆失败:', error)
    alert(`❌ 更新失败: ${error.message}`)
  } finally {
    loading.value = false
  }
}

// ========== 删除车辆方法（整合版） ==========
// ========== 删除车辆方法（修复版） ==========
const deleteVehicle = async (vehicleId) => {
  try {
    console.log('🗑️ 开始删除车辆，ID:', vehicleId);

    // 获取车辆信息用于确认
    const vehicle = userVehicles.value.find(v => v.id === vehicleId) ||
      approvalVehicles.value.find(v => v.id === vehicleId);

    if (!vehicle) {
      alert('未找到要删除的车辆');
      return;
    }

    // 确认删除
    const confirmMsg = `确定要删除车牌号为 "${vehicle.plateNumber}" 的车辆吗？\n\n注意：删除操作将同时删除该车辆的所有停车记录！`;
    if (!confirm(confirmMsg)) {
      return;
    }

    // 🔧 修复：使用 makeApiRequest 而不是 authFetch
    const result = await makeApiRequest(`/api/vehicles/${vehicleId}`, 'DELETE');

    if (result.success) {
      console.log('✅ 车辆删除成功');
      alert(`✅ 车辆 "${vehicle.plateNumber}" 删除成功！`);

      // 从本地列表中移除车辆
      const userIndex = userVehicles.value.findIndex(v => v.id === vehicleId);
      if (userIndex !== -1) {
        userVehicles.value.splice(userIndex, 1);
      }

      const approvalIndex = approvalVehicles.value.findIndex(v => v.id === vehicleId);
      if (approvalIndex !== -1) {
        approvalVehicles.value.splice(approvalIndex, 1);
      }

      // 如果当前页没有数据了，且不是第一页，返回上一页
      if (userVehicles.value.length === 0 && currentPage.value > 1) {
        currentPage.value--;
      }

    } else {
      console.error('❌ 删除失败:', result);

      // 处理不同类型的错误
      if (result.status === 403) {
        alert('❌ 权限不足，无法删除该车辆');
      } else if (result.status === 404) {
        alert('❌ 车辆不存在或已被删除');
      } else if (result.status === 409) {
        alert('❌ 该车辆有进行中的停车记录，请先结束停车再删除');
      } else if (result.errorData && result.errorData.includes('外键约束')) {
        // 外键约束错误，提供更多选项
        const action = await showConstraintErrorDialog(vehicle);

        switch (action) {
          case 'check':
            await checkVehicleConstraintsAPI(vehicleId, vehicle.plateNumber);
            break;
          case 'force':
            await performForceDeleteVehicle(vehicleId, vehicle.plateNumber);
            break;
          case 'view':
            await viewVehicleParkingRecords(vehicleId, vehicle.plateNumber);
            break;
          case 'cancel':
          default:
            // 用户取消
            break;
        }
      } else {
        alert(`❌ 删除失败: ${result.error || '未知错误'}`);
      }
    }

  } catch (error) {
    console.error('💥 删除车辆异常:', error);
    alert(`💥 删除过程中发生异常: ${error.message}`);
  }
};

// 🔧 显示约束错误对话框
const showConstraintErrorDialog = (vehicle) => {
  return new Promise((resolve) => {
    const dialogHtml = `
      <div style="padding: 20px;">
        <h3 style="color: #e74c3c; margin-top: 0;">🚨 删除失败：外键约束错误</h3>
        <p><strong>车辆信息：</strong>${vehicle.plateNumber} (${getUserTypeText(vehicle.userType)})</p>
        <p>该车辆存在关联的停车记录，无法直接删除。</p>

        <div style="margin: 20px 0;">
          <button id="checkBtn" style="margin: 5px; padding: 10px 20px; background: #3498db; color: white; border: none; border-radius: 4px; cursor: pointer;">
            🔍 检查约束详情
          </button>
          <button id="forceBtn" style="margin: 5px; padding: 10px 20px; background: #e74c3c; color: white; border: none; border-radius: 4px; cursor: pointer;">
            ⚡ 强制删除（推荐）
          </button>
          <button id="viewBtn" style="margin: 5px; padding: 10px 20px; background: #2ecc71; color: white; border: none; border-radius: 4px; cursor: pointer;">
            📋 查看停车记录
          </button>
          <button id="cancelBtn" style="margin: 5px; padding: 10px 20px; background: #95a5a6; color: white; border: none; border-radius: 4px; cursor: pointer;">
            取消
          </button>
        </div>
      </div>
    `

    const dialog = document.createElement('div')
    dialog.innerHTML = dialogHtml
    dialog.style.cssText = `
      position: fixed;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      background: white;
      border-radius: 8px;
      box-shadow: 0 4px 20px rgba(0,0,0,0.3);
      z-index: 10000;
      min-width: 500px;
    `

    const overlay = document.createElement('div')
    overlay.style.cssText = `
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(0,0,0,0.5);
      z-index: 9999;
    `

    document.body.appendChild(overlay)
    document.body.appendChild(dialog)

    dialog.querySelector('#checkBtn').addEventListener('click', () => {
      document.body.removeChild(overlay)
      document.body.removeChild(dialog)
      resolve('check')
    })

    dialog.querySelector('#forceBtn').addEventListener('click', () => {
      document.body.removeChild(overlay)
      document.body.removeChild(dialog)
      resolve('force')
    })

    dialog.querySelector('#viewBtn').addEventListener('click', () => {
      document.body.removeChild(overlay)
      document.body.removeChild(dialog)
      resolve('view')
    })

    dialog.querySelector('#cancelBtn').addEventListener('click', () => {
      document.body.removeChild(overlay)
      document.body.removeChild(dialog)
      resolve('cancel')
    })
  })
}

// 🔧 执行强制删除（重命名为避免冲突）
const performForceDeleteVehicle = async (vehicleId, plateNumber) => {
  const confirmForce = confirm(`⚠️ 强制删除确认\n\n将强制删除车辆 "${plateNumber}" 及其所有停车记录。\n此操作不可恢复！\n\n确定要继续吗？`)

  if (!confirmForce) return

  try {
    console.log(`⚡ 执行强制删除: ID=${vehicleId}`)

    const result = await makeApiRequest(`/api/vehicles/${vehicleId}/force-delete`, 'POST')

    if (result.success) {
      console.log('✅ 强制删除成功:', result.data)
      alert(`✅ 车辆 "${plateNumber}" 强制删除成功！\n已删除 ${result.data.deletedRecords || 0} 条停车记录。`)

      // 刷新数据
      await refreshAllData()
    } else {
      alert(`❌ 强制删除失败: ${result.error}`)
    }
  } catch (error) {
    console.error('❌ 强制删除异常:', error)
    alert('❌ 强制删除失败，请稍后重试')
  }
}

// 🔧 检查车辆约束（重命名为避免冲突）
const checkVehicleConstraintsAPI = async (vehicleId, plateNumber) => {
  try {
    console.log(`🔍 检查车辆约束: ID=${vehicleId}`)

    const result = await makeApiRequest(`/api/vehicles/${vehicleId}/check-constraints`, 'GET')

    if (result.success) {
      const constraints = result.data
      console.log('🔧 约束检查结果:', constraints)

      let message = `🔍 车辆 "${plateNumber}" 约束检查结果：\n\n`
      message += `• 是否存在: ${constraints.existsInVehicles ? '是' : '否'}\n`
      message += `• 停车记录数: ${constraints.parkingRecordsCount} 条\n`
      message += `• 备份表记录数: ${constraints.parkingRecordsCopy2Count} 条\n\n`

      if (constraints.parkingRecordsCount > 0 || constraints.parkingRecordsCopy2Count > 0) {
        message += '⚠️ 发现关联记录，建议使用"强制删除"功能。\n\n是否现在执行强制删除？'

        if (confirm(message)) {
          await performForceDeleteVehicle(vehicleId, plateNumber)
        }
      } else {
        alert(message + '✅ 未发现约束问题，可以重新尝试删除。')
      }
    } else {
      alert(`❌ 检查约束失败: ${result.error}`)
    }
  } catch (error) {
    console.error('❌ 检查约束异常:', error)
    alert('❌ 检查约束失败，请稍后重试')
  }
}

// 🔧 查看车辆停车记录
const viewVehicleParkingRecords = async (vehicleId, plateNumber) => {
  alert(`查看车辆 "${plateNumber}" 的停车记录功能开发中...\n车辆ID: ${vehicleId}`)
  // 这里可以跳转到停车记录管理页面，过滤该车辆
  // router.push(`/parking-records?vehicleId=${vehicleId}`)
}

const editVehicle = (vehicle) => {
  console.log('编辑车辆:', vehicle)
  currentVehicle.value = {
    id: vehicle.id,
    plateNumber: vehicle.plateNumber || '',
    vehicleType: vehicle.vehicleType || 'CAR',
    brand: vehicle.brand || '',
    color: vehicle.color || ''
  }
  showEditModal.value = true
}

const viewVehicleDetails = (vehicle) => {
  const details = `
车牌号: ${vehicle.plateNumber}
车辆类型: ${getVehicleTypeText(vehicle.vehicleType)}
品牌: ${vehicle.brand || '未设置'}
颜色: ${vehicle.color || '未设置'}
状态: ${getStatusText(vehicle.status)}
用户类型: ${getUserTypeText(vehicle.userType)}
车主: ${vehicle.userRealName || vehicle.userName || '未知'}
注册时间: ${formatDate(vehicle.createdAt)}
最后更新: ${formatDate(vehicle.updatedAt)}
  `.trim()

  alert(details)
}

const isVehicleInParking = (vehicleId) => {
  // TODO: 实现检查车辆是否在停车中
  return false
}

const updateVehicleInLists = (updatedVehicle) => {
  const completeVehicle = {
    ...updatedVehicle,
    userRealName: updatedVehicle.userRealName || updatedVehicle.user?.realName || '',
    userName: updatedVehicle.userName || updatedVehicle.user?.username || '',
    userType: updatedVehicle.userType || updatedVehicle.user?.userType || '',
    createdAt: updatedVehicle.createdAt || new Date().toISOString(),
    updatedAt: updatedVehicle.updatedAt || new Date().toISOString()
  }

  const userIndex = userVehicles.value.findIndex(v => v.id === completeVehicle.id)
  if (userIndex !== -1) {
    userVehicles.value.splice(userIndex, 1, completeVehicle)
  }

  const approvalIndex = approvalVehicles.value.findIndex(v => v.id === completeVehicle.id)
  if (approvalIndex !== -1) {
    approvalVehicles.value.splice(approvalIndex, 1, completeVehicle)
  }
}

const closeModal = () => {
  showAddModal.value = false
  showEditModal.value = false
  currentVehicle.value = {
    plateNumber: '',
    vehicleType: 'CAR',
    brand: '',
    color: ''
  }
}

const submitVehicle = async () => {
  if (showEditModal.value) {
    await updateVehicle()
  } else {
    await addVehicle()
  }
}

// ========== 生命周期 ==========
onMounted(async () => {
  console.log('🚀 VehicleManagement 页面加载开始')

  // 检查登录状态
  const token = localStorage.getItem('token')
  if (!token) {
    error.value = '请先登录'
    return
  }

  await loadUserVehicles()
  console.log('🏁 页面加载完成')
})
</script>

<style scoped>
.vehicle-management {
  background: white;
  min-height: 100vh;
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 32px;
  text-align: center;
}

.page-title {
  font-size: 32px;
  font-weight: 600;
  color: #1a1a1a;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-bottom: 8px;
}

.title-icon {
  font-size: 36px;
}

.page-subtitle {
  color: #666;
  font-size: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.role-badge {
  background: #1890ff;
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.global-actions {
  margin: 20px 0;
  text-align: center;
  display: flex;
  gap: 12px;
  justify-content: center;
}

.btn-refresh {
  padding: 10px 20px;
  background: #1890ff;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  transition: all 0.2s ease;
}

.btn-refresh:hover:not(:disabled) {
  background: #40a9ff;
}

.btn-refresh:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 数据信息提示 */
.data-info {
  margin: 16px 0;
}

.info-card {
  background: #f0f9ff;
  border: 1px solid #91d5ff;
  border-radius: 8px;
  padding: 16px;
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.info-icon {
  font-size: 24px;
  color: #1890ff;
}

.info-content h4 {
  margin: 0 0 8px 0;
  color: #096dd9;
  font-size: 14px;
}

.info-content p {
  margin: 4px 0;
  font-size: 12px;
  color: #666;
  line-height: 1.4;
}

/* 数据库状态弹窗 */
.database-stats {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin: 16px 0;
}

.stat-item {
  background: #f5f5f5;
  border-radius: 6px;
  padding: 12px;
  text-align: center;
}

.stat-label {
  font-size: 12px;
  color: #666;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 20px;
  font-weight: 600;
  color: #1890ff;
}

.stat-info {
  grid-column: span 2;
  background: #f6ffed;
  border: 1px solid #b7eb8f;
  border-radius: 6px;
  padding: 12px;
  margin-top: 8px;
}

.stat-info p {
  margin: 4px 0;
  font-size: 12px;
  color: #52c41a;
}

.loading-status, .error-status {
  text-align: center;
  padding: 40px 20px;
}

.spinner.small {
  width: 30px;
  height: 30px;
  margin: 0 auto 16px;
}

.error-actions {
  display: flex;
  gap: 8px;
  margin-top: 12px;
  justify-content: center;
}

.debug-btn, .check-db-btn {
  padding: 6px 12px;
  background: #f5f5f5;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
}

.check-db-btn {
  margin-top: 12px;
}

.form-hint {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.vehicle-management {
  background: white;
  min-height: 100vh;
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 32px;
  text-align: center;
}
/* 分页样式 */
.pagination-section {
  padding: 20px;
  border-top: 1px solid #e2e8f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}

.pagination-info {
  color: #718096;
  font-size: 0.875rem;
  flex: 1;
}

.pagination-controls {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.page-numbers {
  display: flex;
  gap: 4px;
}

.page-btn {
  padding: 8px 16px;
  border: 1px solid #e2e8f0;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.875rem;
  transition: all 0.2s;
  min-width: 70px;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-btn:not(:disabled):hover {
  background: #f7fafc;
  border-color: #cbd5e0;
}

.page-number-btn {
  padding: 8px 12px;
  border: 1px solid #e2e8f0;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.875rem;
  min-width: 40px;
  transition: all 0.2s;
}

.page-number-btn.active {
  background: #3182ce;
  color: white;
  border-color: #3182ce;
}

.page-number-btn:not(.active):hover:not(:disabled) {
  background: #f7fafc;
  border-color: #cbd5e0;
}

.page-number-btn:disabled {
  cursor: default;
  background: transparent;
  border-color: transparent;
}

.page-size-select {
  padding: 8px 12px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  background: white;
  font-size: 0.875rem;
  margin-left: 8px;
}

.page-size-select:focus {
  outline: none;
  border-color: #3182ce;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .pagination-section {
    flex-direction: column;
    gap: 12px;
    text-align: center;
  }

  .pagination-controls {
    width: 100%;
    justify-content: center;
  }

  .page-numbers {
    order: 3;
    width: 100%;
    justify-content: center;
    margin-top: 8px;
  }

  .page-btn {
    min-width: 60px;
    padding: 6px 12px;
  }

  .page-number-btn {
    min-width: 36px;
    padding: 6px 8px;
  }
}
.page-title {
  font-size: 32px;
  font-weight: 600;
  color: #1a1a1a;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-bottom: 8px;
}

.title-icon {
  font-size: 36px;
}

.page-subtitle {
  color: #666;
  font-size: 16px;
}

.global-actions {
  margin: 20px 0;
  text-align: center;
}

.btn-refresh {
  padding: 10px 20px;
  background: #1890ff;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  transition: all 0.2s ease;
}

.btn-refresh:hover:not(:disabled) {
  background: #40a9ff;
}

.btn-refresh:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 主要内容区域 */
.main-content {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

/* 通用区域样式 */
.approval-section,
.management-section {
  background: #fafafa;
  border-radius: 12px;
  padding: 24px;
  border: 1px solid #e0e0e0;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a1a;
}

.section-stats {
  display: flex;
  gap: 16px;
  font-size: 14px;
  color: #666;
}

.stat-item {
  padding: 4px 8px;
  background: white;
  border-radius: 4px;
  border: 1px solid #e0e0e0;
}

.section-actions {
  display: flex;
  gap: 12px;
}

/* 按钮样式 */
.btn-primary, .btn-secondary {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.2s ease;
}

.btn-primary {
  background: #1890ff;
  color: white;
}

.btn-primary:hover {
  background: #40a9ff;
}

.btn-secondary {
  background: #f5f5f5;
  color: #666;
  border: 1px solid #d9d9d9;
}

.btn-secondary:hover {
  background: #e8e8e8;
}

/* 审核标签样式 */
.approval-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
  padding: 4px;
  background: #f0f0f0;
  border-radius: 8px;
  width: fit-content;
}

.tab-button {
  padding: 8px 16px;
  border: none;
  background: transparent;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #666;
  transition: all 0.2s ease;
}

.tab-button.active {
  background: white;
  color: #1890ff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.tab-count {
  background: #ff4d4f;
  color: white;
  padding: 2px 6px;
  border-radius: 10px;
  font-size: 12px;
  min-width: 18px;
  text-align: center;
}

/* 管理筛选样式 */
.management-filters {
  margin-bottom: 20px;
}

.filter-group {
  display: flex;
  gap: 12px;
  align-items: center;
}

.search-input {
  padding: 8px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  font-size: 14px;
  width: 250px;
}

.filter-select {
  padding: 8px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  font-size: 14px;
  background: white;
}

/* 卡片列表样式 */
.approval-list,

.approval-card,

.approval-card:hover,
.management-card:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border-color: #1890ff;
}

/* 审核卡片特定样式 */
.vehicle-basic-info {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.plate-number {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
}

.vehicle-type {
  background: #f0f0f0;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  color: #666;
}

.status-badge {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.pending {
  background: #fff7e6;
  color: #fa8c16;
}

.status-badge.approved {
  background: #f6ffed;
  color: #52c41a;
}

.status-badge.rejected {
  background: #fff2f0;
  color: #ff4d4f;
}

.vehicle-details {
  margin-bottom: 12px;
}

.detail-row {
  display: flex;
  gap: 16px;
  margin-bottom: 8px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
}

.label {
  color: #666;
}

.value {
  color: #1a1a1a;
  font-weight: 500;
}

.time-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #999;
}

.temp-badge {
  background: #e6f7ff;
  color: #1890ff;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 11px;
}

.owner-info {
  font-size: 12px;
  color: #666;
  background: #f5f5f5;
  padding: 2px 6px;
  border-radius: 4px;
}

/* 审核操作样式 */
.approval-actions {
  display: flex;
  gap: 8px;
}

.btn-approve, .btn-reject {
  padding: 6px 12px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 4px;
  transition: all 0.2s ease;
}

.btn-approve {
  background: #52c41a;
  color: white;
}

.btn-approve:hover {
  background: #73d13d;
}

.btn-reject {
  background: #ff4d4f;
  color: white;
}

.btn-reject:hover {
  background: #ff7875;
}

.approval-result {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.approved-text {
  color: #52c41a;
  font-weight: 500;
  font-size: 14px;
}

.rejected-text {
  color: #ff4d4f;
  font-weight: 500;
  font-size: 14px;
}

.result-time {
  font-size: 12px;
  color: #999;
}



.vehicle-plate {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
}

.vehicle-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
}

.detail-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.btn-edit, .btn-delete {
  padding: 6px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  background: white;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.2s ease;
}

.btn-edit {
  color: #1890ff;
  border-color: #1890ff;
}

.btn-edit:hover:not(:disabled) {
  background: #e6f7ff;
}

.btn-edit:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-delete {
  color: #ff4d4f;
  border-color: #ff4d4f;
}

.btn-delete:hover:not(:disabled) {
  background: #fff2f0;
}

.btn-delete:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 同步状态 */
.sync-status {
  margin-left: 10px;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: normal;
}

.sync-status.syncing {
  background: #fff3cd;
  color: #856404;
  border: 1px solid #ffeaa7;
}

.sync-status.synced {
  background: #d1ecf1;
  color: #0c5460;
  border: 1px solid #bee5eb;
}

/* 空状态样式 */
.empty-approval,
.empty-management {
  text-align: center;
  padding: 40px 20px;
  color: #999;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-approval h3,
.empty-management h3 {
  font-size: 16px;
  margin-bottom: 8px;
  color: #666;
}

.empty-approval p,
.empty-management p {
  font-size: 14px;
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
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 8px;
  width: 400px;
  max-width: 90vw;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #e8e8e8;
}

.modal-header h3 {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
}

.close-btn {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: #999;
}

.close-btn:hover {
  color: #666;
}

.modal-body {
  padding: 20px;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  margin-bottom: 6px;
  font-size: 14px;
  color: #1a1a1a;
  font-weight: 500;
}

.form-group input,
.form-group select {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 14px;
}

.form-group input:focus,
.form-group select:focus {
  outline: none;
  border-color: #1890ff;
}

.modal-footer {
  padding: 16px 20px;
  border-top: 1px solid #e8e8e8;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

/* 加载状态 */
.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.8);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  z-index: 9999;
}

.loading-spinner {
  border: 3px solid #f3f4f6;
  border-top: 3px solid #1890ff;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .vehicle-management {
    padding: 16px;
  }

  .approval-list,
  .management-list {
    grid-template-columns: 1fr;
  }

  .section-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }

  .section-actions {
    width: 100%;
    justify-content: flex-end;
  }

  .detail-row {
    flex-direction: column;
    gap: 4px;
  }

  .filter-group {
    flex-direction: column;
    align-items: stretch;
  }

  .search-input {
    width: 100%;
  }
}
</style>
