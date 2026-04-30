<template>
  <div class="modern-admin-dashboard">
    <!-- 艺术背景装饰 -->
    <div class="artistic-background">
      <div class="bg-gradient-1"></div>
      <div class="bg-gradient-2"></div>
      <div class="bg-pattern"></div>
    </div>

    <!-- 顶部导航栏 -->
    <header class="dashboard-header">
      <div class="header-content">
        <!-- Logo区域 -->
        <div class="logo-area">
          <div class="logo-icon">🚗</div>
          <div class="logo-text">
            <h1 class="logo-title">ParkSync</h1>
            <p class="logo-subtitle">智慧停车管理系统</p>
          </div>
        </div>

        <!-- 搜索和时间区域 -->
        <div class="header-center">
          <div class="search-box">
            <span class="search-icon">🔍</span>
            <input type="text" placeholder="搜索用户、车辆、记录..." class="search-input" v-model="searchQuery" @keyup.enter="performSearch">
          </div>
          <div class="time-display">
            <div class="current-time">{{ currentTime }}</div>
            <div class="current-date">{{ currentDate }}</div>
          </div>
        </div>

        <!-- 用户和操作区域 -->
        <div class="header-right">
          <button class="notification-btn" @click="showNotifications" :title="`${pendingApprovalCount}个待处理`">
            <span class="notification-icon">🔔</span>
            <span class="notification-badge" v-if="pendingApprovalCount > 0">{{ pendingApprovalCount }}</span>
          </button>
          <div class="user-profile">
            <div class="avatar-wrapper">
              <div class="user-avatar">
                <span class="avatar-text">{{ username.charAt(0) }}</span>
              </div>
              <div class="user-status online"></div>
            </div>
            <div class="user-info">
              <h3 class="user-name">{{ username }}</h3>
              <p class="user-role">系统管理员</p>
            </div>
          </div>
        </div>
      </div>
    </header>

    <!-- 主内容区域 -->
    <main class="dashboard-main">
      <!-- 轮播图区域 -->
      <div class="carousel-section">
        <div class="carousel-container">
          <div class="carousel" ref="carousel">
            <div class="carousel-track" :style="{ transform: `translateX(-${currentSlide * 100}%)` }">
              <div
                v-for="(slide, index) in slides"
                :key="index"
                class="carousel-slide"
                :class="{ active: currentSlide === index }"
                :style="{ backgroundImage: `url(${slide.image})` }"
              >
                <div class="slide-content">
                  <h3>{{ slide.title }}</h3>
                  <p>{{ slide.description }}</p>
                </div>
              </div>
            </div>
          </div>
          <div class="carousel-controls">
            <button class="carousel-prev" @click="prevSlide">‹</button>
            <div class="carousel-indicators">
              <span v-for="i in 3" :key="i" :class="{ active: currentSlide === i - 1 }" @click="goToSlide(i - 1)"></span>
            </div>
            <button class="carousel-next" @click="nextSlide">›</button>
          </div>
        </div>
      </div>

      <!-- 左侧统计面板 -->
      <div class="left-panel">
        <!-- 欢迎面板 -->
        <div class="welcome-card">
          <div class="welcome-content">
            <h2 class="welcome-title">欢迎回来，{{ username }}！</h2>
            <p class="welcome-text">您有 <span class="highlight-text">{{ pendingApprovalCount }}</span> 个待处理事项</p>
            <div class="welcome-stats">
              <div class="stat-item">
                <span class="stat-number">{{ systemHealth }}%</span>
                <span class="stat-label">系统正常</span>
              </div>
              <div class="stat-item">
                <span class="stat-number">{{ todayVisits }}</span>
                <span class="stat-label">今日访问</span>
              </div>
            </div>
          </div>
          <div class="welcome-icon" :class="{ 'loading': loading.stats }">
            <span v-if="!loading.stats">👋</span>
            <span v-else class="spinner">⟳</span>
          </div>
        </div>

        <!-- 核心统计数据 -->
        <div class="stats-grid">
          <!-- 用户统计 - 点击跳转到用户管理页面 -->
          <div class="stat-card floating" @click="navigateTo('/users')" :class="{ 'loading': loading.stats }">
            <div class="card-decoration"></div>
            <div class="card-content">
              <div class="stat-icon user-icon">
                <span>👥</span>
              </div>
              <h3 class="stat-value">{{ loading.stats ? '--' : userCount }}</h3>
              <p class="stat-label">总用户数</p>
              <div class="stat-trend" :class="userTrend >= 0 ? 'trend-up' : 'trend-down'">
                <span v-if="userTrend >= 0">↑ {{ userTrend }}%</span>
                <span v-else>↓ {{ Math.abs(userTrend) }}%</span>
                <span class="trend-text">较上月</span>
              </div>
            </div>
            <div class="card-hint">点击管理用户 →</div>
          </div>

          <!-- 车辆统计 - 点击跳转到车辆管理页面 -->
          <div class="stat-card floating" @click="navigateTo('/vehicles')" :class="{ 'loading': loading.stats }">
            <div class="card-decoration"></div>
            <div class="card-content">
              <div class="stat-icon vehicle-icon">
                <span>🚗</span>
              </div>
              <h3 class="stat-value">{{ loading.stats ? '--' : vehicleCount }}</h3>
              <p class="stat-label">注册车辆</p>
              <div class="stat-trend" :class="vehicleTrend >= 0 ? 'trend-up' : 'trend-down'">
                <span v-if="vehicleTrend >= 0">↑ {{ vehicleTrend }}%</span>
                <span v-else>↓ {{ Math.abs(vehicleTrend) }}%</span>
                <span class="trend-text">较上月</span>
              </div>
            </div>
            <div class="card-hint">点击管理车辆 →</div>
          </div>

          <!-- 车位统计 - 点击跳转到停车位管理页面 -->
          <div class="stat-card floating" @click="navigateTo('/parking-spots')" :class="{ 'loading': loading.stats }">
            <div class="card-decoration"></div>
            <div class="card-content">
              <div class="stat-icon parking-icon">
                <span>🅿️</span>
              </div>
              <h3 class="stat-value">{{ loading.stats ? '--' : parkingSpotCount }}</h3>
              <p class="stat-label">停车位</p>
              <div class="stat-trend">
                <span class="occupancy-rate" :style="{ color: occupancyColor }">
                  {{ loading.stats ? '--' : occupancyRate }}% 占用
                </span>
              </div>
            </div>
            <div class="card-hint">点击管理车位 →</div>
          </div>

          <!-- 收入统计 - 点击跳转到财务页面 -->
          <div class="stat-card floating" @click="navigateTo('/admin/finance')" :class="{ 'loading': loading.finance }">
            <div class="card-decoration"></div>
            <div class="card-content">
              <div class="stat-icon revenue-icon">
                <span>💰</span>
              </div>
              <h3 class="stat-value">{{ loading.finance ? '--' : `¥${formatCurrency(totalRevenue)}` }}</h3>
              <p class="stat-label">年度收入</p>
              <div class="stat-trend" :class="revenueTrend >= 0 ? 'trend-up' : 'trend-down'">
                <span v-if="revenueTrend >= 0">↑ {{ revenueTrend }}%</span>
                <span v-else>↓ {{ Math.abs(revenueTrend) }}%</span>
                <span class="trend-text">较上月</span>
              </div>
            </div>
            <div class="card-hint">点击查看财务 →</div>
          </div>
        </div>

        <!-- 财务快览 -->
        <div class="finance-preview">
          <div class="section-header">
            <h3 class="section-title">
              <span class="title-icon">📊</span>
              财务快览
            </h3>
            <button class="view-all-btn" @click="navigateTo('/admin/finance')" :disabled="loading.finance">
              详细报表 →
            </button>
          </div>
          <div class="finance-content">
            <div class="finance-item">
              <div class="finance-icon today">📅</div>
              <div class="finance-info">
                <h4 class="finance-value">{{ loading.finance ? '--' : `¥${formatCurrency(todayRevenue)}` }}</h4>
                <p class="finance-label">今日收入</p>
              </div>
              <div class="finance-graph">
                <div v-for="(value, index) in dailyRevenueData" :key="index"
                     class="graph-bar"
                     :style="{ height: `${Math.min(value / maxDailyRevenue * 100, 100)}%` }"
                     :title="`第${index + 1}天: ¥${value}`"></div>
              </div>
            </div>
            <div class="finance-item">
              <div class="finance-icon month">📈</div>
              <div class="finance-info">
                <h4 class="finance-value">{{ loading.finance ? '--' : `¥${formatCurrency(monthRevenue)}` }}</h4>
                <p class="finance-label">本月收入</p>
              </div>
              <div class="finance-progress">
                <div class="progress-bar" :style="{ width: `${monthProgress}%` }" :title="`完成度: ${monthProgress}%`"></div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧功能面板 -->
      <div class="right-panel">
        <!-- 快速操作 -->
        <div class="quick-actions">
          <div class="section-header">
            <h2 class="section-title">⚡ 快速操作</h2>
            <p class="section-subtitle">常用功能一键直达</p>
          </div>
          <div class="actions-grid">
            <!-- 用户管理 - 跳转到用户管理 -->
            <button class="action-btn primary" @click="navigateTo('/users')"
                    :class="{ 'has-notification': pendingApprovalCount > 0, 'loading': loading.stats }"
                    :disabled="loading.stats">
              <div class="action-icon">
                <span v-if="!loading.stats">👥</span>
                <span v-else class="spinner-small">⟳</span>
                <span class="badge" v-if="pendingApprovalCount > 0 && !loading.stats">{{ pendingApprovalCount }}</span>
              </div>
              <span class="action-text">用户管理</span>
            </button>

            <!-- 车辆管理 - 跳转到车辆管理 -->
            <button class="action-btn success" @click="navigateTo('/vehicles')" :class="{ 'loading': loading.stats }" :disabled="loading.stats">
              <div class="action-icon">
                <span v-if="!loading.stats">🚗</span>
                <span v-else class="spinner-small">⟳</span>
              </div>
              <span class="action-text">车辆管理</span>
            </button>

            <!-- 停车记录 - 跳转到停车记录管理 -->
            <button class="action-btn warning" @click="navigateTo('/parking-records')">
              <div class="action-icon">
                <span>📝</span>
              </div>
              <span class="action-text">停车记录</span>
            </button>

            <!-- 停车位管理 - 跳转到停车位管理 -->
            <button class="action-btn info" @click="navigateTo('/parking-spots')">
              <div class="action-icon">
                <span>🅿️</span>
              </div>
              <span class="action-text">停车位管理</span>
            </button>
          </div>
        </div>

        <!-- 系统管理入口 -->
        <div class="management-portal">
          <div class="section-header">
            <h2 class="section-title">🚀 系统管理</h2>
            <p class="section-subtitle">全面管理校园停车系统</p>
          </div>
          <div class="portal-grid">
            <!-- 用户管理 -->
            <div class="portal-card" @click="navigateTo('/users')" :class="{ 'loading': loading.stats }">
              <div class="portal-icon">
                <span v-if="!loading.stats">👥</span>
                <span v-else class="spinner-small">⟳</span>
              </div>
              <div class="portal-content">
                <h3 class="portal-title">用户管理</h3>
                <p class="portal-desc">管理用户账户和权限</p>
                <div class="portal-stats">
                  <span class="stat">{{ loading.stats ? '--' : userCount }} 用户</span>
                  <span class="stat alert" v-if="pendingApprovalCount > 0 && !loading.stats">{{ pendingApprovalCount }} 待审</span>
                </div>
              </div>
              <div class="portal-arrow">→</div>
            </div>

            <!-- 车辆管理 -->
            <div class="portal-card" @click="navigateTo('/vehicles')" :class="{ 'loading': loading.stats }">
              <div class="portal-icon">
                <span v-if="!loading.stats">🚗</span>
                <span v-else class="spinner-small">⟳</span>
              </div>
              <div class="portal-content">
                <h3 class="portal-title">车辆管理</h3>
                <p class="portal-desc">审核和管理车辆信息</p>
                <div class="portal-stats">
                  <span class="stat">{{ loading.stats ? '--' : vehicleCount }} 车辆</span>
                  <span class="stat">{{ loading.stats ? '--' : approvedVehicles }} 已审</span>
                </div>
              </div>
              <div class="portal-arrow">→</div>
            </div>

            <!-- 停车位管理 -->
            <div class="portal-card" @click="navigateTo('/parking-spots')" :class="{ 'loading': loading.stats }">
              <div class="portal-icon">
                <span v-if="!loading.stats">🅿️</span>
                <span v-else class="spinner-small">⟳</span>
              </div>
              <div class="portal-content">
                <h3 class="portal-title">停车场管理</h3>
                <p class="portal-desc">监控车位状态</p>
                <div class="portal-stats">
                  <span class="stat">{{ loading.stats ? '--' : parkingSpotCount }} 车位</span>
                  <span class="stat">{{ loading.stats ? '--' : availableSpots }} 空闲</span>
                </div>
              </div>
              <div class="portal-arrow">→</div>
            </div>

            <!-- 停车记录 -->
            <div class="portal-card" @click="navigateTo('/parking-records')">
              <div class="portal-icon">
                <span>📊</span>
              </div>
              <div class="portal-content">
                <h3 class="portal-title">停车记录</h3>
                <p class="portal-desc">查看历史记录</p>
                <div class="portal-stats">
                  <span class="stat">{{ todayParkingCount }} 今日</span>
                  <span class="stat">{{ monthlyParkingCount }} 本月</span>
                </div>
              </div>
              <div class="portal-arrow">→</div>
            </div>
          </div>
        </div>

        <!-- 系统状态 -->
        <div class="system-status">
          <div class="section-header">
            <h3 class="section-title">🔧 系统状态</h3>
            <div class="refresh-btn" @click="refreshAllData" title="刷新所有数据" :class="{ 'refreshing': isRefreshing }">
              <span v-if="!isRefreshing">🔄</span>
              <span v-else class="spinner">⟳</span>
            </div>
          </div>
          <div class="status-cards">
            <div class="status-card">
              <div class="status-icon database">🗄️</div>
              <div class="status-info">
                <span class="status-name">数据库</span>
                <span class="status-value" :class="dbStatus.class">{{ dbStatus.text }}</span>
              </div>
            </div>
            <div class="status-card">
              <div class="status-icon api">🔌</div>
              <div class="status-info">
                <span class="status-name">API服务</span>
                <span class="status-value" :class="apiStatus.class">{{ apiStatus.text }}</span>
              </div>
            </div>
            <div class="status-card">
              <div class="status-icon storage">💾</div>
              <div class="status-info">
                <span class="status-name">存储空间</span>
                <span class="status-value" :class="storageStatus.class">{{ storageStatus.text }}</span>
              </div>
            </div>
            <div class="status-card">
              <div class="status-icon network">🌐</div>
              <div class="status-info">
                <span class="status-name">网络状态</span>
                <span class="status-value" :class="networkStatus.class">{{ networkStatus.text }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- 浮动刷新按钮 -->
    <button class="floating-refresh-btn" @click="refreshAllData" title="刷新所有数据" :class="{ 'refreshing': isRefreshing }">
      <span class="refresh-icon" v-if="!isRefreshing">🔄</span>
      <span class="spinner" v-else>⟳</span>
    </button>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 轮播图相关
const currentSlide = ref(0)
const carousel = ref(null)
let autoSlideInterval = null

// 轮播图数据
const slides = [
  {
    image: 'https://images.unsplash.com/photo-1542362567-b07e54358753?auto=format&fit=crop&w=1200&q=80',
    title: '智慧停车管理系统',
    description: '高效管理校园停车资源'
  },
  {
    image: 'https://images.unsplash.com/photo-1503376780353-7e6692767b70?auto=format&fit=crop&w=1200&q=80',
    title: '实时数据监控',
    description: '掌握停车场使用情况'
  },
  {
    image: 'https://images.unsplash.com/photo-1544620347-c4fd4a3d5957?auto=format&fit=crop&w=1200&q=80',
    title: '多角色权限管理',
    description: '满足不同用户需求'
  }
]

// 轮播图控制方法
const nextSlide = () => {
  currentSlide.value = (currentSlide.value + 1) % 3
}

const prevSlide = () => {
  currentSlide.value = (currentSlide.value - 1 + 3) % 3
}

const goToSlide = (index) => {
  currentSlide.value = index
}

// 自动轮播
const startAutoSlide = () => {
  autoSlideInterval = setInterval(nextSlide, 5000)
}

const stopAutoSlide = () => {
  if (autoSlideInterval) {
    clearInterval(autoSlideInterval)
    autoSlideInterval = null
  }
}

// 用户名和当前时间
const username = ref(localStorage.getItem('username') || '管理员')
const currentTime = ref('')
const searchQuery = ref('')

// 统计数据 - 全部初始化为0
const userCount = ref(0)
const vehicleCount = ref(0)
const parkingSpotCount = ref(0)
const totalRevenue = ref(0)
const todayRevenue = ref(0)
const monthRevenue = ref(0)
const pendingApprovalCount = ref(0)
const availableSpots = ref(0)
const occupiedSpots = ref(0)
const approvedVehicles = ref(0)
const todayParkingCount = ref(0)
const monthlyParkingCount = ref(0)
const todayVisits = ref(0)

// 趋势数据
const userTrend = ref(0)
const vehicleTrend = ref(0)
const revenueTrend = ref(0)

// 财务图表数据
const dailyRevenueData = ref([0, 0, 0, 0, 0, 0, 0])
const maxDailyRevenue = ref(1)

// 加载状态
const loading = ref({
  stats: false,
  finance: false,
  approvals: false
})
const isRefreshing = ref(false)

// 系统状态
const systemHealth = ref(100)
const dbStatus = ref({ text: '正常', class: 'online' })
const apiStatus = ref({ text: '正常', class: 'online' })
const networkStatus = ref({ text: '良好', class: 'online' })
const storageStatus = ref({ text: '82%', class: '' })

// 计算属性
const currentDate = computed(() => {
  return new Date().toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long'
  })
})

const occupancyRate = computed(() => {
  if (parkingSpotCount.value === 0) return 0
  const occupied = parkingSpotCount.value - availableSpots.value
  return Math.round((occupied / parkingSpotCount.value) * 100)
})

const occupancyColor = computed(() => {
  if (occupancyRate.value < 50) return '#10b981' // 绿色
  if (occupancyRate.value < 80) return '#f59e0b' // 黄色
  return '#ef4444' // 红色
})

const monthProgress = computed(() => {
  const today = new Date()
  const daysInMonth = new Date(today.getFullYear(), today.getMonth() + 1, 0).getDate()
  const currentDay = today.getDate()
  return Math.round((currentDay / daysInMonth) * 100)
})

// 更新时间显示
const updateCurrentTime = () => {
  const now = new Date()
  currentTime.value = now.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 路由导航
const navigateTo = (path) => {
  router.push(path)
}

// 显示通知
const showNotifications = () => {
  if (pendingApprovalCount.value > 0) {
    navigateTo('/user-approval')
  }
}

// 搜索功能
const performSearch = () => {
  if (searchQuery.value.trim()) {
    console.log('执行搜索:', searchQuery.value)
    // 这里可以添加搜索逻辑
    searchQuery.value = ''
  }
}

// 格式化货币
const formatCurrency = (amount) => {
  return amount.toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })
}
// 在仪表盘中添加token检查
onMounted(() => {
  // 检查token是否存在
  const token = localStorage.getItem('token')
  if (!token) {
    alert('请先登录')
    router.push('/login')
    return
  }

  // 初始化时间和数据
  updateCurrentTime()
  setInterval(updateCurrentTime, 1000)

  refreshAllData()
})
// 加载统计数据的API
// 修改后的 loadStatistics 方法
const loadStatistics = async () => {
  try {
    loading.value.stats = true
    const token = localStorage.getItem('token')

    // 1. 加载停车位数据（与 parking-spots 页面使用相同的API）
    const spotsResponse = await fetch('/api/parking-spots', {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    })

    if (spotsResponse.ok) {
      const parkingSpots = await spotsResponse.json()
      console.log('停车位数据加载成功:', parkingSpots.length, '条记录')

      // 计算停车位统计数据（与 parking-spots 页面相同逻辑）
      parkingSpotCount.value = parkingSpots.length

      // 按照 parking-spots 页面的逻辑计算各种状态的车位数量
      const availableSpotsCount = parkingSpots.filter(s => s.status === 'AVAILABLE').length
      const occupiedSpotsCount = parkingSpots.filter(s => s.status === 'OCCUPIED').length
      const maintenanceSpotsCount = parkingSpots.filter(s => s.status === 'MAINTENANCE').length

      availableSpots.value = availableSpotsCount

      // 计算使用率
      console.log(`停车位统计: 总数=${parkingSpotCount.value}, 空闲=${availableSpotsCount}, 占用=${occupiedSpotsCount}, 维护=${maintenanceSpotsCount}`)
    } else {
      console.warn('停车位数据加载失败:', spotsResponse.status)
    }

    // 2. 加载用户数据
    const usersResponse = await fetch('/api/users', {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    })

    if (usersResponse.ok) {
      const users = await usersResponse.json()
      userCount.value = users.length

      // 计算待审核用户数量（校内用户中状态为PENDING的）
      const pendingUsers = users.filter(user =>
        user.userStatus === 'PENDING' &&
        user.userType !== 'ADMIN' &&
        user.userType !== 'EXTERNAL_USER'
      )
      pendingApprovalCount.value = pendingUsers.length
      console.log('用户统计: 总数=', userCount.value, '待审核=', pendingApprovalCount.value)
    }

    // 3. 加载车辆数据
    const vehiclesResponse = await fetch('/api/vehicles', {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    })

    if (vehiclesResponse.ok) {
      const vehicles = await vehiclesResponse.json()
      vehicleCount.value = vehicles.length

      // 计算已审核车辆数量
      const approvedVehiclesCount = vehicles.filter(v => v.status === 'APPROVED').length
      approvedVehicles.value = approvedVehiclesCount
      console.log('车辆统计: 总数=', vehicleCount.value, '已审核=', approvedVehiclesCount)
    }

    // 4. 加载今日访问统计（可以根据需要添加）
    todayVisits.value = 42 // 暂时使用模拟数据，后续可替换为真实API

    // 5. 计算趋势数据（可根据实际情况从后端获取或前端计算）
    // 这里使用简单的固定值，实际项目中应该从后端获取历史数据对比
    userTrend.value = 5
    vehicleTrend.value = 12

  } catch (error) {
    console.error('加载统计数据失败:', error)
  } finally {
    loading.value.stats = false
  }
}

// 加载财务数据
const loadFinanceData = async () => {
  try {
    loading.value.finance = true
    const token = localStorage.getItem('token')

    // 调用财务概览API
    const response = await fetch('/api/admin/finance/overview', {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    })

    if (response.ok) {
      const result = await response.json()
      if (result.success) {
        const data = result.data
        todayRevenue.value = data.todayRevenue || 0
        monthRevenue.value = data.monthRevenue || 0
        totalRevenue.value = data.yearRevenue || 0
        todayParkingCount.value = data.todayParkingCount || 0
        monthlyParkingCount.value = data.todayParkingCount * 30 || 0 // 简单估算

        // 计算收入趋势（根据实际情况调整）
        revenueTrend.value = monthRevenue.value > 0 ? 15 : 0

        // 生成模拟的每日收入数据（实际项目中应该从后端获取）
        generateDailyRevenueData()
      }
    } else {
      console.warn('财务数据API返回错误状态:', response.status)
      // 使用默认数据或显示错误
    }
  } catch (error) {
    console.error('加载财务数据失败:', error)
    // 可以显示错误提示，但使用默认值让页面正常显示
    todayRevenue.value = 345.00
    monthRevenue.value = 4560.00
    totalRevenue.value = 12560.50
    todayParkingCount.value = 45
  } finally {
    loading.value.finance = false
  }
}

// 生成每日收入数据（模拟）
const generateDailyRevenueData = () => {
  dailyRevenueData.value = []
  for (let i = 0; i < 7; i++) {
    const value = Math.random() * 500 + 100 // 100-600之间的随机数
    dailyRevenueData.value.push(Number(value.toFixed(2)))
  }
  maxDailyRevenue.value = Math.max(...dailyRevenueData.value, 1)
}

// 加载待审核用户数量
const loadPendingApprovalCount = async () => {
  try {
    loading.value.approvals = true
    const token = localStorage.getItem('token')

    // 调用待审核用户API
    const response = await fetch('/api/users/pending/campus', {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    })

    if (response.ok) {
      const pendingUsers = await response.json()
      pendingApprovalCount.value = pendingUsers.length || 0
    } else {
      console.warn('待审核用户API返回错误状态:', response.status)
      pendingApprovalCount.value = 0
    }
  } catch (error) {
    console.error('加载待审核用户数量失败:', error)
    pendingApprovalCount.value = 0
  } finally {
    loading.value.approvals = false
  }
}

// 检查系统状态
const checkSystemStatus = async () => {
  try {
    const token = localStorage.getItem('token')

    // 检查数据库连接
    const dbResponse = await fetch('/api/admin/health/db', {
      headers: { 'Authorization': `Bearer ${token}` }
    }).catch(() => null)

    dbStatus.value = dbResponse && dbResponse.ok
      ? { text: '正常', class: 'online' }
      : { text: '异常', class: 'error' }

    // 检查API服务
    const apiResponse = await fetch('/api/admin/finance/test', {
      headers: { 'Authorization': `Bearer ${token}` }
    }).catch(() => null)

    apiStatus.value = apiResponse && apiResponse.ok
      ? { text: '正常', class: 'online' }
      : { text: '异常', class: 'error' }

    // 检查网络
    networkStatus.value = navigator.onLine
      ? { text: '良好', class: 'online' }
      : { text: '断开', class: 'error' }

    // 计算系统健康度
    const checks = [dbStatus.value, apiStatus.value, networkStatus.value]
    const healthyChecks = checks.filter(check => check.class === 'online').length
    systemHealth.value = Math.round((healthyChecks / checks.length) * 100)

  } catch (error) {
    console.error('检查系统状态失败:', error)
  }
}

// 刷新所有数据
const refreshAllData = async () => {
  if (isRefreshing.value) return

  isRefreshing.value = true

  try {
    // 并行加载所有数据
    await Promise.all([
      loadStatistics(),
      loadFinanceData(),
      loadPendingApprovalCount(),
      checkSystemStatus()
    ])

    console.log('所有数据刷新完成')
  } catch (error) {
    console.error('刷新数据失败:', error)
  } finally {
    isRefreshing.value = false
  }
}

// 初始化加载数据
onMounted(() => {
  // 初始化时间
  updateCurrentTime()
  setInterval(updateCurrentTime, 1000) // 每秒更新一次时间

  // 初始化加载数据
  refreshAllData()

  // 每5分钟自动刷新一次数据
  setInterval(() => {
    if (!isRefreshing.value) {
      loadStatistics()
      loadFinanceData()
      loadPendingApprovalCount()
    }
  }, 5 * 60 * 1000)
})
</script>

<style scoped>
.modern-admin-dashboard {
  min-height: 100vh;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  position: relative;
  overflow-x: hidden;
}
/* 加载状态样式 */
.loading {
  position: relative;
  opacity: 0.7;
}

.loading::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 20px;
  height: 20px;
  border: 2px solid rgba(99, 102, 241, 0.3);
  border-top-color: #6366f1;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.spinner {
  animation: spin 1s linear infinite;
  display: inline-block;
}

.spinner-small {
  animation: spin 1s linear infinite;
  font-size: 1.2em;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.refreshing {
  animation: spin 1s linear infinite;
}

/* 趋势颜色 */
.trend-down {
  color: #ef4444 !important;
}

/* 错误状态 */
.status-value.error {
  color: #ef4444;
  font-weight: 600;
}

/* 禁用状态 */
button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none !important;
}

button:disabled:hover {
  transform: none !important;
  box-shadow: none !important;
}

/* 已有的其他样式保持不变 */
.modern-admin-dashboard {
  min-height: 100vh;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  position: relative;
  overflow-x: hidden;
}
/* 艺术背景装饰 */
.artistic-background {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  z-index: 0;
}

.bg-gradient-1 {
  position: absolute;
  top: -50%;
  right: -20%;
  width: 60%;
  height: 100%;
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.08) 0%, transparent 70%);
  border-radius: 50%;
  filter: blur(80px);
}

.bg-gradient-2 {
  position: absolute;
  bottom: -50%;
  left: -20%;
  width: 60%;
  height: 100%;
  background: linear-gradient(135deg, rgba(14, 165, 233, 0.08) 0%, transparent 70%);
  border-radius: 50%;
  filter: blur(80px);
}

.bg-pattern {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image:
    radial-gradient(circle at 10% 20%, rgba(99, 102, 241, 0.03) 0%, transparent 20%),
    radial-gradient(circle at 90% 80%, rgba(14, 165, 233, 0.03) 0%, transparent 20%);
}

/* 顶部导航栏 */
.dashboard-header {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(226, 232, 240, 0.8);
  position: sticky;
  top: 0;
  z-index: 100;
  padding: 0 32px;
  box-shadow: 0 4px 30px rgba(0, 0, 0, 0.03);
}

.header-content {
  max-width: 1600px;
  margin: 0 auto;
  padding: 20px 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

/* Logo区域 */
.logo-area {
  display: flex;
  align-items: center;
  gap: 16px;
  position: relative;
}

.logo-icon {
  font-size: 2.5em;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow:
    0 8px 24px rgba(99, 102, 241, 0.2),
    inset 0 2px 4px rgba(255, 255, 255, 0.2);
  animation: float 6s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.logo-text {
  position: relative;
}

.logo-title {
  font-size: 1.8em;
  font-weight: 800;
  margin: 0;
  background: linear-gradient(135deg, #1e293b, #475569);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  letter-spacing: -0.5px;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.05);
  position: relative;
}

.logo-title::after {
  content: '';
  position: absolute;
  bottom: -4px;
  left: 0;
  width: 40px;
  height: 3px;
  background: linear-gradient(90deg, #6366f1, transparent);
  border-radius: 2px;
}

.logo-subtitle {
  font-size: 0.85em;
  color: #64748b;
  margin: 4px 0 0 0;
  font-weight: 500;
  letter-spacing: 0.3px;
}

/* 搜索和时间区域 */
.header-center {
  display: flex;
  align-items: center;
  gap: 24px;
}

.search-box {
  position: relative;
  width: 300px;
}

.search-icon {
  position: absolute;
  left: 16px;
  top: 50%;
  transform: translateY(-50%);
  color: #94a3b8;
  font-size: 1.1em;
}

.search-input {
  width: 100%;
  padding: 12px 20px 12px 44px;
  background: rgba(241, 245, 249, 0.8);
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  font-size: 0.95em;
  color: #1e293b;
  transition: all 0.3s ease;
}

.search-input:focus {
  outline: none;
  background: white;
  border-color: #6366f1;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}

.search-input::placeholder {
  color: #94a3b8;
}

.time-display {
  display: flex;
  align-items: center;
  gap: 12px;
}

.current-time {
  font-size: 1.4em;
  font-weight: 700;
  color: #1e293b;
  font-family: 'SF Mono', monospace;
}

.current-date {
  font-size: 0.9em;
  color: #64748b;
  padding: 4px 12px;
  background: rgba(241, 245, 249, 0.8);
  border-radius: 8px;
}

/* 用户和操作区域 */
.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.notification-btn {
  position: relative;
  background: none;
  border: none;
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 1.2em;
  color: #64748b;
  transition: all 0.3s ease;
}

.notification-btn:hover {
  background: rgba(241, 245, 249, 0.8);
  color: #6366f1;
  transform: translateY(-2px);
}

.notification-badge {
  position: absolute;
  top: 6px;
  right: 6px;
  background: linear-gradient(135deg, #ef4444, #dc2626);
  color: white;
  font-size: 0.7em;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(239, 68, 68, 0.3);
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar-wrapper {
  position: relative;
}

.user-avatar {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 700;
  font-size: 1.2em;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
}

.user-status {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 12px;
  height: 12px;
  background: #10b981;
  border: 2px solid white;
  border-radius: 50%;
  box-shadow: 0 2px 6px rgba(16, 185, 129, 0.3);
}

.user-info {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-size: 1em;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
}

.user-role {
  font-size: 0.85em;
  color: #64748b;
  margin: 2px 0 0 0;
  font-weight: 500;
}

/* 主内容区域 */
.dashboard-main {
  position: relative;
  z-index: 1;
  max-width: 1600px;
  margin: 0 auto;
  padding: 32px;
  display: grid;
  grid-template-columns: 380px 1fr;
  gap: 24px;
}

/* 左侧面板 */
.left-panel {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 欢迎卡片 */
.welcome-card {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95), rgba(255, 255, 255, 0.8));
  border-radius: 20px;
  padding: 28px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.04),
    0 2px 12px rgba(0, 0, 0, 0.02);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.4);
  position: relative;
  overflow: hidden;
}

.welcome-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #6366f1, #8b5cf6);
}

.welcome-content {
  flex: 1;
}

.welcome-title {
  font-size: 1.5em;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 8px 0;
  background: linear-gradient(135deg, #1e293b, #475569);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.welcome-text {
  color: #64748b;
  margin: 0 0 20px 0;
  font-size: 0.95em;
}

.highlight-text {
  background: linear-gradient(135deg, #ef4444, #f97316);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  font-weight: 700;
}

.welcome-stats {
  display: flex;
  gap: 20px;
}

.stat-item {
  display: flex;
  flex-direction: column;
}

.stat-number {
  font-size: 1.2em;
  font-weight: 700;
  color: #6366f1;
}

.stat-label {
  font-size: 0.85em;
  color: #64748b;
  margin-top: 4px;
}

.welcome-icon {
  font-size: 3em;
  opacity: 0.8;
  animation: bounce 3s ease-in-out infinite;
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

/* 统计卡片网格 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.stat-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  position: relative;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow:
    0 4px 20px rgba(0, 0, 0, 0.04),
    0 1px 6px rgba(0, 0, 0, 0.02);
  border: 1px solid rgba(226, 232, 240, 0.8);
  overflow: hidden;
}

.stat-card.floating {
  animation: float-card 6s ease-in-out infinite;
}

@keyframes float-card {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-5px); }
}

.stat-card:hover {
  transform: translateY(-8px);
  box-shadow:
    0 16px 40px rgba(0, 0, 0, 0.08),
    0 8px 20px rgba(0, 0, 0, 0.04);
  border-color: rgba(99, 102, 241, 0.3);
}

.card-decoration {
  position: absolute;
  top: 0;
  right: 0;
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, transparent 50%, rgba(99, 102, 241, 0.05) 50%);
  border-radius: 0 16px 0 80px;
}

.card-content {
  position: relative;
  z-index: 1;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.8em;
  margin-bottom: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.user-icon { background: linear-gradient(135deg, #6366f1, #8b5cf6); color: white; }
.vehicle-icon { background: linear-gradient(135deg, #10b981, #34d399); color: white; }
.parking-icon { background: linear-gradient(135deg, #f59e0b, #fbbf24); color: white; }
.revenue-icon { background: linear-gradient(135deg, #8b5cf6, #a78bfa); color: white; }

.stat-value {
  font-size: 1.8em;
  font-weight: 800;
  color: #1e293b;
  margin: 0 0 4px 0;
  font-family: 'SF Mono', monospace;
}

.stat-label {
  font-size: 0.9em;
  color: #64748b;
  margin: 0 0 12px 0;
  font-weight: 500;
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.85em;
}

.trend-up {
  color: #10b981;
  font-weight: 600;
}

.trend-text {
  color: #94a3b8;
}

.occupancy-rate {
  color: #f59e0b;
  font-weight: 600;
}

.card-hint {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 12px 24px;
  background: linear-gradient(transparent, rgba(99, 102, 241, 0.05));
  font-size: 0.8em;
  color: #64748b;
  opacity: 0;
  transform: translateY(10px);
  transition: all 0.3s ease;
}

.stat-card:hover .card-hint {
  opacity: 1;
  transform: translateY(0);
}

/* 财务快览 */
.finance-preview {
  background: white;
  border-radius: 20px;
  padding: 24px;
  box-shadow:
    0 4px 20px rgba(0, 0, 0, 0.04),
    0 1px 6px rgba(0, 0, 0, 0.02);
  border: 1px solid rgba(226, 232, 240, 0.8);
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.section-title {
  font-size: 1.2em;
  font-weight: 700;
  color: #1e293b;
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
}

.title-icon {
  font-size: 1.2em;
}

.view-all-btn {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 0.9em;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.view-all-btn:hover {
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
}

.finance-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.finance-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: rgba(241, 245, 249, 0.5);
  border-radius: 12px;
  transition: all 0.3s ease;
}

.finance-item:hover {
  background: rgba(241, 245, 249, 0.8);
  transform: translateX(4px);
}

.finance-icon {
  font-size: 1.5em;
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.finance-icon.today {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.1), rgba(99, 102, 241, 0.2));
  color: #6366f1;
}

.finance-icon.month {
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.1), rgba(16, 185, 129, 0.2));
  color: #10b981;
}

.finance-info {
  flex: 1;
}

.finance-value {
  font-size: 1.3em;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 4px 0;
}

.finance-label {
  font-size: 0.85em;
  color: #64748b;
  margin: 0;
}

.finance-graph {
  display: flex;
  align-items: flex-end;
  gap: 4px;
  height: 40px;
}

.graph-bar {
  width: 8px;
  background: linear-gradient(to top, #6366f1, #8b5cf6);
  border-radius: 4px;
  transition: height 0.3s ease;
}

.finance-progress {
  width: 80px;
  height: 6px;
  background: rgba(226, 232, 240, 0.8);
  border-radius: 3px;
  overflow: hidden;
}

.progress-bar {
  height: 100%;
  background: linear-gradient(90deg, #10b981, #34d399);
  border-radius: 3px;
  transition: width 1s ease;
}

/* 右侧面板 */
.right-panel {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 快速操作 */
.quick-actions {
  background: white;
  border-radius: 20px;
  padding: 28px;
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.04),
    0 2px 12px rgba(0, 0, 0, 0.02);
  border: 1px solid rgba(226, 232, 240, 0.8);
}

.section-subtitle {
  font-size: 0.95em;
  color: #64748b;
  margin: 4px 0 24px 0;
}

.actions-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 20px 16px;
  border: none;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: rgba(241, 245, 249, 0.5);
  position: relative;
}

.action-btn:hover {
  transform: translateY(-4px);
}

.action-btn.primary {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.1), rgba(99, 102, 241, 0.2));
}

.action-btn.primary:hover {
  box-shadow: 0 8px 24px rgba(99, 102, 241, 0.2);
}

.action-btn.success {
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.1), rgba(16, 185, 129, 0.2));
}

.action-btn.success:hover {
  box-shadow: 0 8px 24px rgba(16, 185, 129, 0.2);
}

.action-btn.warning {
  background: linear-gradient(135deg, rgba(245, 158, 11, 0.1), rgba(245, 158, 11, 0.2));
}

.action-btn.warning:hover {
  box-shadow: 0 8px 24px rgba(245, 158, 11, 0.2);
}

.action-btn.info {
  background: linear-gradient(135deg, rgba(14, 165, 233, 0.1), rgba(14, 165, 233, 0.2));
}

.action-btn.info:hover {
  box-shadow: 0 8px 24px rgba(14, 165, 233, 0.2);
}

.action-btn.has-notification {
  border: 2px solid rgba(239, 68, 68, 0.3);
}

.action-icon {
  font-size: 1.8em;
  position: relative;
}

.badge {
  position: absolute;
  top: -8px;
  right: -8px;
  background: linear-gradient(135deg, #ef4444, #dc2626);
  color: white;
  font-size: 0.7em;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
}

.action-text {
  font-size: 0.9em;
  font-weight: 600;
  color: #1e293b;
}

/* 系统管理入口 */
.management-portal {
  background: white;
  border-radius: 20px;
  padding: 28px;
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.04),
    0 2px 12px rgba(0, 0, 0, 0.02);
  border: 1px solid rgba(226, 232, 240, 0.8);
}

.portal-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.portal-card {
  background: rgba(241, 245, 249, 0.5);
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.portal-card:hover {
  background: white;
  transform: translateY(-4px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.08);
}

.portal-card:hover .portal-arrow {
  transform: translateX(4px);
  color: #6366f1;
}

.portal-icon {
  font-size: 1.8em;
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.1), rgba(99, 102, 241, 0.2));
  color: #6366f1;
}

.portal-content {
  flex: 1;
}

.portal-title {
  font-size: 1.1em;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 4px 0;
}

.portal-desc {
  font-size: 0.85em;
  color: #64748b;
  margin: 0 0 12px 0;
}

.portal-stats {
  display: flex;
  gap: 12px;
}

.portal-stats .stat {
  font-size: 0.85em;
  padding: 4px 8px;
  background: white;
  border-radius: 6px;
  color: #64748b;
}

.portal-stats .stat.alert {
  color: #ef4444;
  background: rgba(239, 68, 68, 0.1);
}

.portal-arrow {
  color: #94a3b8;
  font-size: 1.2em;
  transition: all 0.3s ease;
}

/* 系统状态 */
.system-status {
  background: white;
  border-radius: 20px;
  padding: 24px;
  box-shadow:
    0 4px 20px rgba(0, 0, 0, 0.04),
    0 1px 6px rgba(0, 0, 0, 0.02);
  border: 1px solid rgba(226, 232, 240, 0.8);
}

.refresh-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: rgba(241, 245, 249, 0.8);
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.refresh-btn:hover {
  background: #6366f1;
  color: white;
  transform: rotate(180deg);
}

.status-cards {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.status-card {
  background: rgba(241, 245, 249, 0.5);
  border-radius: 12px;
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  transition: all 0.3s ease;
}

.status-card:hover {
  background: rgba(241, 245, 249, 0.8);
  transform: translateY(-2px);
}

.status-icon {
  font-size: 1.5em;
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.status-icon.database { background: rgba(99, 102, 241, 0.1); color: #6366f1; }
.status-icon.api { background: rgba(16, 185, 129, 0.1); color: #10b981; }
.status-icon.storage { background: rgba(245, 158, 11, 0.1); color: #f59e0b; }
.status-icon.network { background: rgba(14, 165, 233, 0.1); color: #0ea5e9; }

.status-info {
  flex: 1;
}

.status-name {
  display: block;
  font-size: 0.9em;
  color: #64748b;
  margin-bottom: 2px;
}

.status-value {
  font-size: 1em;
  font-weight: 600;
  color: #1e293b;
}

.status-value.online {
  color: #10b981;
}

/* 浮动刷新按钮 */
.floating-refresh-btn {
  position: fixed;
  bottom: 32px;
  right: 32px;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border: none;
  color: white;
  font-size: 1.4em;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow:
    0 8px 24px rgba(99, 102, 241, 0.3),
    0 4px 12px rgba(99, 102, 241, 0.2);
  transition: all 0.3s ease;
  z-index: 100;
}

.floating-refresh-btn:hover {
  transform: scale(1.1) rotate(180deg);
  box-shadow:
    0 12px 32px rgba(99, 102, 241, 0.4),
    0 6px 16px rgba(99, 102, 241, 0.3);
}

/* 响应式设计 */
@media (max-width: 1400px) {
  .dashboard-main {
    grid-template-columns: 1fr;
  }

  .actions-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .portal-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 1024px) {
  .dashboard-main {
    padding: 24px;
  }

  .header-content {
    flex-direction: column;
    gap: 20px;
  }

  .header-center {
    width: 100%;
    justify-content: space-between;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .dashboard-header {
    padding: 0 20px;
  }

  .dashboard-main {
    padding: 20px;
  }

  .search-box {
    width: 200px;
  }

  .actions-grid {
    grid-template-columns: 1fr;
  }

  .status-cards {
    grid-template-columns: 1fr;
  }

  .floating-refresh-btn {
    bottom: 20px;
    right: 20px;
  }
}

@media (max-width: 480px) {
  .dashboard-header {
    padding: 0 16px;
  }

  .dashboard-main {
    padding: 16px;
  }

  .header-center {
    flex-direction: column;
    gap: 12px;
  }

  .search-box {
    width: 100%;
  }

  .logo-title {
    font-size: 1.4em;
  }
}

/* 轮播图样式 */
.carousel-section {
  grid-column: 1 / -1;
  margin-bottom: 24px;
}

.carousel-container {
  position: relative;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.carousel {
  position: relative;
  width: 100%;
  height: 300px;
  overflow: hidden;
}

.carousel-track {
  display: flex;
  transition: transform 0.5s ease-in-out;
  height: 100%;
}

.carousel-slide {
  flex: 0 0 100%;
  height: 100%;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.carousel-slide::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(255, 182, 193, 0.7) 0%, rgba(255, 192, 203, 0.7) 100%);
  z-index: 1;
}

.slide-content {
  position: relative;
  z-index: 2;
  text-align: center;
  color: white;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.slide-content h3 {
  font-size: 2.5em;
  font-weight: 700;
  margin-bottom: 10px;
}

.slide-content p {
  font-size: 1.2em;
  opacity: 0.9;
}

.carousel-controls {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  gap: 15px;
  z-index: 3;
}

.carousel-prev,
.carousel-next {
  background: rgba(255, 255, 255, 0.9);
  border: none;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  font-size: 1.5em;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  color: #ff69b4;
}

.carousel-prev:hover,
.carousel-next:hover {
  background: white;
  transform: scale(1.1);
}

.carousel-indicators {
  display: flex;
  gap: 8px;
}

.carousel-indicators span {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.5);
  cursor: pointer;
  transition: all 0.3s ease;
}

.carousel-indicators span.active {
  background: white;
  transform: scale(1.2);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .carousel {
    height: 200px;
  }

  .slide-content h3 {
    font-size: 1.8em;
  }

  .slide-content p {
    font-size: 1em;
  }
}
</style>
