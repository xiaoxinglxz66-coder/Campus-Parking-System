<template>
  <div class="finance-dashboard">
    <!-- 头部区域 -->
    <div class="dashboard-header">
      <div class="header-left">
        <h1><span class="icon">📊</span> 财务统计中心</h1>
        <p class="subtitle">实时监控校园停车收入与状态</p>
      </div>
      <div class="header-right">
        <div class="refresh-controls">
          <div class="auto-refresh">
            <span class="refresh-status" :class="{ active: autoRefresh }">
              ● {{ autoRefresh ? `自动刷新 (${countdown}s)` : '手动刷新' }}
            </span>
            <button class="btn-toggle" @click="toggleAutoRefresh">
              {{ autoRefresh ? '暂停' : '开启' }}
            </button>
          </div>
          <button class="btn-refresh" @click="manualRefresh" :disabled="loading">
            🔄 立即刷新
          </button>
        </div>
        <div class="current-time">
          {{ formatTime(currentDateTime, 'YYYY年MM月DD日 HH:mm:ss') }}
        </div>
      </div>
    </div>

    <!-- 实时统计卡片 -->
    <div class="stats-grid">
      <!-- 当前停车 -->
      <div class="stat-card">
        <div class="stat-icon active">
          <span>🚗</span>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ overview.currentParkingCount || 0 }}</div>
          <div class="stat-label">当前停车</div>
        </div>
      </div>

      <!-- 今日收入 -->
      <div class="stat-card highlight">
        <div class="stat-icon revenue">
          <span>💰</span>
        </div>
        <div class="stat-content">
          <div class="stat-value">¥{{ formatCurrency(overview.todayRevenue) }}</div>
          <div class="stat-label">今日收入</div>
        </div>
      </div>

      <!-- 本月收入 -->
      <div class="stat-card">
        <div class="stat-icon month">
          <span>📅</span>
        </div>
        <div class="stat-content">
          <div class="stat-value">¥{{ formatCurrency(overview.monthRevenue) }}</div>
          <div class="stat-label">本月收入</div>
        </div>
      </div>

      <!-- 今日停车次数 -->
      <div class="stat-card">
        <div class="stat-icon count">
          <span>📈</span>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ overview.todayParkingCount || 0 }}</div>
          <div class="stat-label">今日停车</div>
        </div>
      </div>
    </div>

    <!-- 图表区域 - 两个可视化 -->
    <div class="charts-section">
      <!-- 图表1：今日收入趋势（折线图） -->
      <div class="chart-card revenue-chart">
        <div class="chart-header">
          <div class="chart-title">
            <span class="icon">📈</span>
            <h3>收入趋势分析</h3>
          </div>
          <div class="chart-controls">
            <div class="time-range">
              <button
                v-for="range in timeRanges"
                :key="range.value"
                :class="{ active: timeRange === range.value }"
                @click="changeTimeRange(range.value)"
                class="range-btn"
              >
                {{ range.label }}
              </button>
            </div>
            <div class="date-range" v-if="timeRange === 'custom'">
              <div class="date-input-group">
                <input
                  type="date"
                  v-model="customStartDate"
                  @change="handleCustomDateChange"
                  :max="customEndDate || formatTime(new Date(), 'YYYY-MM-DD')"
                  class="date-input"
                >
                <span>至</span>
                <input
                  type="date"
                  v-model="customEndDate"
                  @change="handleCustomDateChange"
                  :min="customStartDate"
                  :max="formatTime(new Date(), 'YYYY-MM-DD')"
                  class="date-input"
                >
              </div>
              <!-- 加载状态提示 -->
              <div v-if="customDateLoading" class="loading-indicator">
                <div class="mini-spinner"></div>
                <span>加载中...</span>
              </div>
              <!-- 错误提示 -->
              <div v-if="customDateError" class="error-text">
                {{ customDateError }}
              </div>
            </div>
          </div>
        </div>

        <div class="chart-container" :class="{ 'has-data': revenueTrend.length > 0 }">
          <!-- 优化版折线图 -->
          <div class="simple-line-chart" v-if="revenueTrend.length > 0">
            <div class="chart-wrapper">
              <div class="y-axis">
                <div v-for="tick in revenueYTicks" :key="tick" class="y-tick">
                  ¥{{ formatCompactNumber(tick) }}
                </div>
              </div>

              <div class="chart-area">
                <div class="grid-lines">
                  <div
                    v-for="(tick, index) in revenueYTicks"
                    :key="index"
                    class="grid-line"
                    :style="{ top: (index / (revenueYTicks.length - 1)) * 100 + '%' }"
                  ></div>
                </div>

                <!-- 连接线 -->
                <div class="line-path">
                  <svg width="100%" height="100%" viewBox="0 0 100 100" preserveAspectRatio="none">
                    <path :d="revenueLinePath" class="line" />
                  </svg>
                </div>

                <!-- 数据点 -->
                <div class="data-points">
                  <div
                    v-for="(item, index) in filteredRevenueData"
                    :key="index"
                    class="data-point"
                    :style="{
                      left: (index * 100 / (filteredRevenueData.length - 1)) + '%',
                      bottom: (item.revenue / revenueMax * 95) + '%'
                    }"
                    @mouseenter="showRevenueTooltip(item, index)"
                  >
                    <div class="point-value">¥{{ formatCompactNumber(item.revenue) }}</div>
                  </div>
                </div>

                <!-- X轴标签 - 优化显示 -->
                <div class="x-axis">
                  <div
                    v-for="(item, index) in filteredXAxisLabels"
                    :key="index"
                    class="x-tick"
                    :style="{ left: (index * 100 / (filteredXAxisLabels.length - 1)) + '%' }"
                  >
                    {{ formatCompactDate(item) }}
                  </div>
                </div>
              </div>
            </div>

            <div class="chart-summary">
              <div class="summary-item">
                <div class="summary-label">最高收入</div>
                <div class="summary-value">¥{{ formatCompactNumber(revenueMax) }}</div>
                <div class="summary-date">{{ revenueMaxDate }}</div>
              </div>
              <div class="summary-item">
                <div class="summary-label">平均收入</div>
                <div class="summary-value">¥{{ formatCompactNumber(revenueAvg) }}</div>
              </div>
              <div class="summary-item">
                <div class="summary-label">总收入</div>
                <div class="summary-value">¥{{ formatCompactNumber(revenueTotal) }}</div>
              </div>
            </div>
          </div>

          <div v-else class="chart-empty">
            <div class="empty-icon">📊</div>
            <p>{{ timeRange === 'custom' ? '请选择日期范围' : '暂无收入数据' }}</p>
            <button v-if="timeRange !== 'custom'" @click="loadRevenueTrend" class="btn-load">
              刷新数据
            </button>
          </div>
        </div>
      </div>

      <!-- 图表2：停车场月收入排名 -->
      <div class="chart-card parking-rank-chart">
        <div class="chart-header">
          <div class="chart-title">
            <span class="icon">🏆</span>
            <h3>停车场月收入排名</h3>
          </div>
          <div class="chart-subtitle">{{ currentMonth }}月收入前5停车场</div>
        </div>

        <div class="chart-container">
          <div class="simple-rank-container" v-if="parkingRank.length > 0">
            <!-- 左侧扇形图 -->
            <div class="simple-pie-chart">
              <svg width="160" height="160" viewBox="0 0 160 160">
                <circle
                  cx="80"
                  cy="80"
                  r="60"
                  fill="none"
                  stroke="#e2e8f0"
                  stroke-width="30"
                />

                <!-- 收入占比扇形 -->
                <circle
                  v-for="(item, index) in pieSegments"
                  :key="index"
                  cx="80"
                  cy="80"
                  r="45"
                  fill="none"
                  :stroke="getRankColor(index)"
                  stroke-width="25"
                  :stroke-dasharray="`${item.percentage * 2.83}, 283`"
                  stroke-linecap="round"
                  :transform="`rotate(${item.startAngle} 80 80)`"
                  class="rank-segment"
                />

                <!-- 中心文字 -->
                <text x="80" y="75" text-anchor="middle" class="pie-center-text">
                  {{ currentMonth }}月
                </text>
                <text x="80" y="95" text-anchor="middle" class="pie-center-subtext">
                  总收入 ¥{{ formatCompactNumber(totalMonthlyRevenue) }}
                </text>
              </svg>
            </div>

            <!-- 右侧排名列表 -->
            <div class="simple-rank-list">
              <div
                v-for="(item, index) in parkingRank"
                :key="item.id"
                class="rank-row"
                :class="`rank-${index + 1}`"
              >
                <div class="rank-order">
                  <span class="order-number">{{ index + 1 }}</span>
                  <span class="order-dot"></span>
                </div>

                <div class="rank-name">{{ item.name }}</div>

                <div class="rank-percent">{{ item.percentage }}%</div>

                <div class="rank-bar">
                  <div
                    class="bar-fill"
                    :style="{
                      width: item.percentage + '%',
                      backgroundColor: getRankColor(index)
                    }"
                  ></div>
                </div>

                <!-- 显示月收入金额 -->
                <div class="rank-revenue">
                  ¥{{ formatCurrency(item.revenue) }}
                </div>
              </div>

              <!-- 月度统计 - 显示真实数据 -->
              <div class="month-summary" v-if="totalMonthlyRevenue > 0">
                <div class="summary-item">
                  <span class="summary-label">总停车次数</span>
                  <span class="summary-value">{{ totalMonthlyCount }}</span>
                </div>
                <div class="summary-item">
                  <span class="summary-label">总收入</span>
                  <span class="summary-value">¥{{ formatCurrency(totalMonthlyRevenue) }}</span>
                </div>
              </div>
              <div v-else class="month-summary">
                <div class="summary-item full-width">
                  <span class="summary-label">状态</span>
                  <span class="summary-value text-warning">
                    {{ loading ? '数据加载中...' : '暂无本月停车收入数据' }}
                  </span>
                </div>
              </div>
            </div>
          </div>

          <div v-else class="chart-empty">
            <div class="empty-icon">🏆</div>
            <p>加载停车场排名数据中...</p>
            <button @click="loadParkingRank" class="btn-load">加载数据</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 停车记录表格 -->
    <div class="records-section">
      <div class="section-header">
        <div class="section-title">
          <span class="icon">📋</span>
          <h2>停车记录明细</h2>
          <span class="record-count">共 {{ pagination.total }} 条记录</span>
        </div>

        <div class="section-filters">
          <div class="search-box">
            <input
              v-model="searchParams.plateNumber"
              type="text"
              placeholder="搜索车牌号..."
              @keyup.enter="searchRecords"
              class="search-input"
            >
            <span class="search-icon">🔍</span>
          </div>

          <div class="filter-group">
            <select v-model="searchParams.userType" @change="searchRecords" class="filter-select">
              <option value="">所有用户</option>
              <option value="EXTERNAL_USER">校外用户</option>
              <option value="STUDENT">学生</option>
              <option value="TEACHER">教师</option>
              <option value="STAFF">职工</option>
            </select>

            <select v-model="searchParams.status" @change="searchRecords" class="filter-select">
              <option value="">所有状态</option>
              <option value="PARKING">停车中</option>
              <option value="COMPLETED">已完成</option>
              <option value="CANCELLED">已取消</option>
            </select>

            <div class="date-range">
              <input
                type="date"
                v-model="searchParams.startDate"
                @change="searchRecords"
                class="date-input"
              >
              <span>至</span>
              <input
                type="date"
                v-model="searchParams.endDate"
                @change="searchRecords"
                class="date-input"
              >
            </div>

            <button @click="searchRecords" class="btn-search">搜索</button>
            <button @click="resetFilters" class="btn-reset">重置</button>
          </div>
        </div>
      </div>

      <div class="records-table">
        <div class="table-container">
          <table>
            <thead>
            <tr>
              <th class="col-index">#</th>
              <th class="col-time">开始时间</th>
              <th class="col-duration">停车时长</th>
              <th class="col-plate">车牌号</th>
              <th class="col-user">用户信息</th>
              <th class="col-spot">停车区域</th>
              <th class="col-fee">费用</th>
              <th class="col-status">状态</th>
            </tr>
            </thead>
            <tbody>
            <tr
              v-for="(record, index) in parkingRecords"
              :key="record.id"
              :class="{
                  'row-active': record.status === 'PARKING',
                  'row-completed': record.status === 'COMPLETED'
                }"
            >
              <td class="col-index">
                {{ (pagination.page - 1) * pagination.size + index + 1 }}
              </td>

              <td class="col-time">
                <div class="time-display">
                  <div class="time-date">{{ formatTime(record.startTime, 'MM-DD') }}</div>
                  <div class="time-clock">{{ formatTime(record.startTime, 'HH:mm') }}</div>
                </div>
              </td>

              <td class="col-duration">
                <div class="duration-display">
                  {{ calculateDuration(record) }}
                  <span v-if="record.status === 'PARKING'" class="live-badge">
                      <span class="live-dot"></span>
                      实时
                    </span>
                </div>
              </td>

              <td class="col-plate">
                <span class="plate-badge">{{ record.plateNumber }}</span>
              </td>

              <td class="col-user">
                <div class="user-info">
                  <div class="user-name">{{ record.userName || '-' }}</div>
                  <div class="user-type" :class="record.userType?.toLowerCase()">
                    {{ getUserTypeText(record.userType) }}
                  </div>
                </div>
              </td>

              <td class="col-spot">{{ record.parkingLotName || '-' }}</td>

              <td class="col-fee">
                <div v-if="record.status === 'COMPLETED' && record.fee">
                  <div class="fee-amount">¥{{ record.fee.toFixed(2) }}</div>
                  <div v-if="record.userType === 'EXTERNAL_USER'" class="fee-rate">
                    5元/小时
                  </div>
                  <div v-else class="fee-free">校内免费</div>
                </div>
                <div v-else-if="record.status === 'PARKING'" class="fee-estimated">
                  预计: ¥{{ calculateEstimatedFee(record) }}
                </div>
                <div v-else class="fee-none">-</div>
              </td>

              <td class="col-status">
                  <span :class="['status-badge', record.status?.toLowerCase()]">
                    {{ getStatusText(record.status) }}
                  </span>
                <div v-if="record.status === 'PARKING'" class="status-time">
                  已停 {{ formatLiveDuration(record.startTime) }}
                </div>
              </td>
            </tr>

            <tr v-if="parkingRecords.length === 0 && !loading">
              <td colspan="8" class="empty-row">
                <div class="empty-state">
                  <span class="empty-icon">🚗</span>
                  <p>暂无停车记录</p>
                </div>
              </td>
            </tr>

            <tr v-if="loading">
              <td colspan="8" class="loading-row">
                <div class="loading-state">
                  <div class="loading-spinner"></div>
                  <span>加载中...</span>
                </div>
              </td>
            </tr>
            </tbody>
          </table>
        </div>

        <!-- 分页 -->
        <div v-if="pagination.total > 0" class="pagination">
          <div class="pagination-info">
            显示 {{ pagination.start }} 到 {{ pagination.end }} 条，共 {{ pagination.total }} 条
          </div>

          <div class="pagination-controls">
            <select v-model="pagination.size" @change="handlePageSizeChange" class="page-size">
              <option value="10">10 条/页</option>
              <option value="20">20 条/页</option>
              <option value="50">50 条/页</option>
            </select>

            <button
              @click="prevPage"
              :disabled="pagination.page === 1"
              class="page-btn prev"
            >
              ‹
            </button>

            <div class="page-numbers">
              <span class="current-page">{{ pagination.page }}</span>
              <span class="page-separator">/</span>
              <span class="total-pages">{{ pagination.totalPages }}</span>
            </div>

            <button
              @click="nextPage"
              :disabled="pagination.page >= pagination.totalPages"
              class="page-btn next"
            >
              ›
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 全局加载状态 -->
    <div v-if="loading" class="loading-overlay">
      <div class="loading-content">
        <div class="loading-spinner-large"></div>
        <div class="loading-text">数据加载中...</div>
      </div>
    </div>

    <!-- 更新通知 -->
    <transition name="slide-up">
      <div v-if="showUpdateNotification" class="update-notification">
        <div class="notification-content">
          <span class="notification-icon">🔄</span>
          数据已更新 {{ lastUpdateTime }}
        </div>
        <button @click="showUpdateNotification = false" class="notification-close">
          ×
        </button>
      </div>
    </transition>
  </div>
</template>

<script>
import { ref, reactive, onMounted, onUnmounted, computed, watch } from 'vue'
import { debounce } from 'lodash-es'

export default {
  name: 'FinanceDashboard',

  setup() {
    // ========== 状态管理 ==========
    const loading = ref(false)
    const autoRefresh = ref(true)
    const countdown = ref(30)
    const currentDateTime = ref(new Date())
    const timeRange = ref('today')
    const showUpdateNotification = ref(false)
    const lastUpdateTime = ref('')
    const customDateLoading = ref(false)
    const customDateError = ref('')

    // 财务概览数据
    const overview = reactive({
      todayRevenue: 0,
      monthRevenue: 0,
      yearRevenue: 0,
      todayParkingCount: 0,
      currentParkingCount: 0,
      externalRevenue: 0,
      campusRevenue: 0,
      externalCount: 0,
      campusCount: 0
    })

    // 收入趋势数据
    const revenueTrend = ref([])

    // 停车场排名数据
    const parkingRank = ref([])

    // 停车记录数据
    const parkingRecords = ref([])

    // 搜索参数
    const searchParams = reactive({
      plateNumber: '',
      userType: '',
      status: '',
      startDate: '',
      endDate: ''
    })

    // 分页配置
    const pagination = reactive({
      page: 1,
      size: 10,
      total: 0,
      get totalPages() {
        return Math.ceil(this.total / this.size)
      },
      get start() {
        return (this.page - 1) * this.size + 1
      },
      get end() {
        return Math.min(this.page * this.size, this.total)
      }
    })

    // 时间范围选项
    const timeRanges = [
      { label: '今日', value: 'today' },
      { label: '本周', value: 'week' },
      { label: '本月', value: 'month' },
      { label: '本年', value: 'year' },
      { label: '自定义', value: 'custom' }
    ]

    const customStartDate = ref('')
    const customEndDate = ref('')

    // ========== 定时器管理 ==========
    let refreshTimer = null
    let countdownTimer = null
    let timeUpdateTimer = null

    // ========== 计算属性 ==========
    const currentMonth = computed(() => {
      return new Date().getMonth() + 1
    })

    // 停车场排名相关计算属性
    const totalMonthlyRevenue = computed(() => {
      if (!Array.isArray(parkingRank.value)) return 0
      return parkingRank.value.reduce((sum, item) => sum + (Number(item.revenue) || 0), 0)
    })

    const totalMonthlyCount = computed(() => {
      if (!Array.isArray(parkingRank.value)) return 0
      return parkingRank.value.reduce((sum, item) => sum + (Number(item.count) || 0), 0)
    })

    const averageMonthlyRevenue = computed(() => {
      const validItems = parkingRank.value.filter(item => (Number(item.revenue) || 0) > 0)
      return validItems.length > 0 ? totalMonthlyRevenue.value / validItems.length : 0
    })

    const pieSegments = computed(() => {
      if (!Array.isArray(parkingRank.value) || parkingRank.value.length === 0) return []

      let currentAngle = -90 // 从顶部开始
      const segments = []

      parkingRank.value.forEach((item) => {
        const angle = (item.percentage / 100) * 360

        segments.push({
          percentage: item.percentage,
          startAngle: currentAngle,
          endAngle: currentAngle + angle
        })

        currentAngle += angle
      })

      return segments
    })

    // 收入趋势相关计算属性
    const revenueYTicks = computed(() => {
      if (revenueTrend.value.length === 0) return [0]
      const revenues = revenueTrend.value.map(r => r.revenue)
      const max = Math.max(...revenues)

      // 智能计算刻度
      if (max === 0) return [0]

      let step
      if (max >= 10000) {
        step = Math.ceil(max / 5 / 1000) * 1000
      } else if (max >= 1000) {
        step = Math.ceil(max / 5 / 100) * 100
      } else {
        step = Math.ceil(max / 5 / 10) * 10
      }

      const ticks = []
      for (let i = 5; i >= 0; i--) {
        ticks.push(step * i)
      }
      return ticks
    })

    const revenueLinePath = computed(() => {
      if (revenueTrend.value.length < 2) return ''

      const points = revenueTrend.value.map((item, index) => {
        const x = (index / (revenueTrend.value.length - 1)) * 100
        const maxRevenue = Math.max(...revenueTrend.value.map(r => r.revenue))
        const y = maxRevenue > 0 ? 100 - (item.revenue / maxRevenue * 95) : 100
        return `${x},${y}`
      })

      return `M ${points.join(' L ')}`
    })

    const filteredRevenueData = computed(() => {
      if (revenueTrend.value.length === 0) return []

      // 如果数据点太多，进行抽样显示
      const maxPoints = 12 // 最多显示12个点
      if (revenueTrend.value.length <= maxPoints) {
        return revenueTrend.value
      }

      const step = Math.ceil(revenueTrend.value.length / maxPoints)
      return revenueTrend.value.filter((_, index) => index % step === 0)
    })

    const filteredXAxisLabels = computed(() => {
      if (revenueTrend.value.length === 0) return []

      // 根据数据点数量调整X轴标签密度
      const maxLabels = 6 // 最多显示6个标签
      if (revenueTrend.value.length <= maxLabels) {
        return revenueTrend.value.map(item => item.date)
      }

      const step = Math.ceil(revenueTrend.value.length / maxLabels)
      return revenueTrend.value
        .filter((_, index) => index % step === 0)
        .map(item => item.date)
    })

    const revenueMax = computed(() => {
      if (revenueTrend.value.length === 0) return 0
      return Math.max(...revenueTrend.value.map(r => r.revenue))
    })

    const revenueMaxDate = computed(() => {
      if (revenueTrend.value.length === 0) return ''
      const maxItem = revenueTrend.value.reduce((max, curr) =>
        curr.revenue > max.revenue ? curr : max
      )
      return formatCompactDate(maxItem.date)
    })

    const revenueAvg = computed(() => {
      if (revenueTrend.value.length === 0) return 0
      const sum = revenueTrend.value.reduce((total, r) => total + r.revenue, 0)
      return Math.round(sum / revenueTrend.value.length)
    })

    const revenueTotal = computed(() => {
      return revenueTrend.value.reduce((total, r) => total + r.revenue, 0)
    })

    // ========== 工具函数 ==========
    const formatCurrency = (amount) => {
      if (amount === null || amount === undefined || amount === '') return '0.00'
      const num = parseFloat(amount)
      if (isNaN(num)) return '0.00'
      return num.toLocaleString('zh-CN', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
      })
    }

    const formatCompactNumber = (num) => {
      if (num === null || num === undefined) return '0'
      const number = parseFloat(num)
      if (isNaN(number)) return '0'

      if (number >= 1000000) {
        return (number / 1000000).toFixed(1) + 'M'
      }

      if (number >= 10000) {
        return (number / 10000).toFixed(1) + '万'
      }

      if (number >= 1000) {
        return (number / 1000).toFixed(1) + '千'
      }

      return Math.round(number).toString()
    }

    const formatNumber = (num) => {
      if (num === null || num === undefined) return '0'
      const number = parseInt(num)
      if (isNaN(number)) return '0'
      return number.toLocaleString('zh-CN')
    }

    const formatTime = (dateTime, format = 'YYYY-MM-DD HH:mm') => {
      if (!dateTime) return '-'

      try {
        const date = new Date(dateTime)
        if (isNaN(date.getTime())) return '-'

        const year = date.getFullYear()
        const month = String(date.getMonth() + 1).padStart(2, '0')
        const day = String(date.getDate()).padStart(2, '0')
        const hours = String(date.getHours()).padStart(2, '0')
        const minutes = String(date.getMinutes()).padStart(2, '0')
        const seconds = String(date.getSeconds()).padStart(2, '0')

        switch (format) {
          case 'YYYY-MM-DD':
            return `${year}-${month}-${day}`
          case 'MM-DD':
            return `${month}-${day}`
          case 'HH:mm':
            return `${hours}:${minutes}`
          case 'HH:mm:ss':
            return `${hours}:${minutes}:${seconds}`
          case 'YYYY年MM月DD日 HH:mm:ss':
            return `${year}年${month}月${day}日 ${hours}:${minutes}:${seconds}`
          default:
            return `${year}-${month}-${day} ${hours}:${minutes}`
        }
      } catch (e) {
        console.error('时间格式化错误:', e)
        return '-'
      }
    }

    const formatCompactDate = (dateString) => {
      try {
        const date = new Date(dateString)
        if (isNaN(date.getTime())) return ''

        const month = date.getMonth() + 1
        const day = date.getDate()

        // 如果是今天
        const today = new Date()
        if (date.toDateString() === today.toDateString()) {
          return '今天'
        }

        // 如果是昨天
        const yesterday = new Date(today)
        yesterday.setDate(today.getDate() - 1)
        if (date.toDateString() === yesterday.toDateString()) {
          return '昨天'
        }

        return `${month}/${day}`
      } catch (e) {
        console.error('日期格式化错误:', e)
        return ''
      }
    }

    const calculateDuration = (record) => {
      if (!record.startTime) return '-'

      const start = new Date(record.startTime)
      const end = record.endTime ? new Date(record.endTime) : new Date()

      const diffMs = end - start
      const diffMins = Math.floor(diffMs / 60000)
      const diffHours = Math.floor(diffMins / 60)
      const remainingMins = diffMins % 60

      if (diffHours > 0) {
        return `${diffHours}小时${remainingMins}分钟`
      }
      return `${diffMins}分钟`
    }

    const formatLiveDuration = (startTime) => {
      const duration = calculateDuration({ startTime })
      return duration.replace('小时', 'h').replace('分钟', 'm')
    }

    const calculateEstimatedFee = (record) => {
      if (record.userType !== 'EXTERNAL_USER') return '0.00'

      const start = new Date(record.startTime)
      const now = new Date()
      const diffMs = now - start
      const diffHours = Math.ceil(diffMs / 3600000)

      return (diffHours * 5).toFixed(2)
    }

    const getRankColor = (index) => {
      const colors = [
        '#f59e0b', // 第1名 - 橙色
        '#94a3b8', // 第2名 - 灰色
        '#92400e', // 第3名 - 棕色
        '#3b82f6', // 第4名 - 蓝色
        '#10b981'  // 第5名 - 绿色
      ]
      return colors[index] || '#64748b'
    }

    const getUserTypeText = (userType) => {
      const map = {
        'EXTERNAL_USER': '校外用户',
        'STUDENT': '学生',
        'TEACHER': '教师',
        'STAFF': '职工',
        'ADMIN': '管理员'
      }
      return map[userType] || userType
    }

    const getStatusText = (status) => {
      const map = {
        'PARKING': '停车中',
        'COMPLETED': '已完成',
        'CANCELLED': '已取消'
      }
      return map[status] || status
    }

    // ========== API 客户端 ==========
    const apiClient = {
      async get(url, params = {}) {
        try {
          // 构建查询参数
          const queryParams = new URLSearchParams()
          Object.keys(params).forEach(key => {
            if (params[key] !== undefined && params[key] !== null && params[key] !== '') {
              queryParams.append(key, params[key])
            }
          })

          const queryString = queryParams.toString() ? `?${queryParams}` : ''
          const fullUrl = `/api${url}${queryString}`

          const token = localStorage.getItem('token')
          if (!token) {
            throw new Error('用户未登录')
          }

          const response = await fetch(fullUrl, {
            headers: {
              'Authorization': `Bearer ${token}`,
              'Accept': 'application/json'
            }
          })

          if (!response.ok) {
            throw new Error(`HTTP ${response.status}`)
          }

          return await response.json()
        } catch (error) {
          console.error(`API请求失败: ${url}`, error)
          throw error
        }
      }
    }

    // ========== 自定义日期筛选处理 ==========
    const handleCustomDateChange = async () => {
      console.log('📅 自定义日期变化:', customStartDate.value, '至', customEndDate.value)

      // 验证日期是否完整
      if (!customStartDate.value || !customEndDate.value) {
        console.log('❌ 日期不完整，跳过加载')
        customDateError.value = '请选择完整的日期范围'
        revenueTrend.value = []
        return
      }

      // 验证日期范围合理性
      const start = new Date(customStartDate.value)
      const end = new Date(customEndDate.value)

      if (start > end) {
        console.log('❌ 开始日期不能晚于结束日期')
        customDateError.value = '开始日期不能晚于结束日期'
        revenueTrend.value = []
        return
      }

      // 限制查询范围（可选）
      const diffTime = Math.abs(end - start)
      const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))

      if (diffDays > 90) {
        console.log('❌ 日期范围不能超过90天')
        customDateError.value = '日期范围不能超过90天'
        revenueTrend.value = []
        return
      }

      customDateError.value = ''
      customDateLoading.value = true

      try {
        console.log('⏱️ 开始加载自定义日期范围数据...')
        await loadRevenueTrend()
        console.log('✅ 自定义日期范围数据加载完成')
      } catch (error) {
        console.error('❌ 加载失败:', error)
        customDateError.value = error.message || '加载失败'
        revenueTrend.value = []
      } finally {
        customDateLoading.value = false
      }
    }

    // ========== 数据加载方法 ==========
    const loadFinanceOverview = async () => {
      try {
        const response = await apiClient.get('/admin/finance/overview')
        if (response.success && response.data) {
          const data = response.data
          Object.assign(overview, {
            todayRevenue: data.todayRevenue || 0,
            monthRevenue: data.monthRevenue || 0,
            yearRevenue: data.yearRevenue || 0,
            todayParkingCount: data.todayParkingCount || 0,
            currentParkingCount: data.currentParkingCount || 0,
            externalRevenue: data.externalRevenue || 0,
            campusRevenue: data.campusRevenue || 0,
            externalCount: data.externalCount || 0,
            campusCount: data.campusCount || 0
          })
        }
      } catch (error) {
        console.error('加载财务概览失败:', error)
      }
    }

    const loadRevenueTrend = async () => {
      try {
        let startDate, endDate
        const today = new Date()

        switch (timeRange.value) {
          case 'today':
            startDate = today.toISOString().split('T')[0]
            endDate = startDate
            break
          case 'week':
            const weekAgo = new Date(today)
            weekAgo.setDate(today.getDate() - 7)
            startDate = weekAgo.toISOString().split('T')[0]
            endDate = today.toISOString().split('T')[0]
            break
          case 'month':
            const monthAgo = new Date(today)
            monthAgo.setDate(1) // 本月第一天
            startDate = monthAgo.toISOString().split('T')[0]
            endDate = today.toISOString().split('T')[0]
            break
          case 'year':
            const yearStart = new Date(today.getFullYear(), 0, 1)
            startDate = yearStart.toISOString().split('T')[0]
            endDate = today.toISOString().split('T')[0]
            break
          case 'custom':
            // 直接使用已选择的日期
            startDate = customStartDate.value
            endDate = customEndDate.value
            break
          default:
            startDate = today.toISOString().split('T')[0]
            endDate = startDate
        }

        console.log('加载收入趋势数据:', { startDate, endDate, timeRange: timeRange.value })

        const response = await apiClient.get('/admin/finance/revenue-trend', {
          startDate,
          endDate
        })

        if (response.success && response.data) {
          revenueTrend.value = response.data.map(item => ({
            date: item.date || item.day,
            revenue: item.revenue || 0,
            parkingCount: item.parkingCount || 0
          }))
          console.log('收入趋势数据加载成功:', revenueTrend.value.length, '条记录')
        } else {
          revenueTrend.value = []
          console.log('收入趋势数据为空')
        }
      } catch (error) {
        console.error('加载收入趋势失败:', error)
        revenueTrend.value = []
        throw error
      }
    }

    const loadParkingRank = async () => {
      try {
        console.log('开始加载停车场月收入排名数据...')

        // 获取当前月份
        const now = new Date()
        const year = now.getFullYear()
        const month = now.getMonth() + 1
        const firstDay = `${year}-${String(month).padStart(2, '0')}-01`
        const lastDay = new Date(year, month, 0).toISOString().split('T')[0]

        console.log(`日期范围: ${firstDay} 至 ${lastDay}`)

        const response = await apiClient.get('/admin/finance/external-records', {
          startDate: firstDay,
          endDate: lastDay,
          status: 'COMPLETED',
          page: 0,
          size: 1000
        })

        if (response.success && response.data?.records) {
          const records = response.data.records

          // 只统计有费用的记录
          const validRecords = records.filter(record =>
            record.userType === 'EXTERNAL_USER' &&
            record.status === 'COMPLETED' &&
            parseFloat(record.fee) > 0
          )

          console.log(`有效记录数: ${validRecords.length}`)

          // 按停车场统计
          const parkingStats = {}
          let totalRevenue = 0

          validRecords.forEach(record => {
            let parkingName = record.parkingLotName || '未知区域'
            const fee = parseFloat(record.fee) || 0

            if (!parkingStats[parkingName]) {
              parkingStats[parkingName] = {
                name: parkingName,
                revenue: 0,
                count: 0
              }
            }

            parkingStats[parkingName].revenue += fee
            parkingStats[parkingName].count += 1
            totalRevenue += fee
          })

          // 转换为数组并按收入排序
          const statsArray = Object.values(parkingStats)

          if (statsArray.length > 0) {
            const sortedStats = statsArray
              .sort((a, b) => b.revenue - a.revenue)
              .slice(0, 5)

            // 计算百分比
            const revenueSum = sortedStats.reduce((sum, stat) => sum + stat.revenue, 0)

            parkingRank.value = sortedStats.map((stat, index) => ({
              id: index + 1,
              name: stat.name,
              percentage: revenueSum > 0 ? Number(((stat.revenue / revenueSum) * 100).toFixed(1)) : 0,
              revenue: Number(stat.revenue.toFixed(2)),
              count: stat.count
            }))

            console.log('停车场排名数据:', parkingRank.value)
          } else {
            parkingRank.value = []
          }
        } else {
          parkingRank.value = []
        }
      } catch (error) {
        console.error('加载停车场排名失败:', error)
        parkingRank.value = []
      }
    }

    const loadParkingRecords = async () => {
      try {
        const params = {
          page: pagination.page - 1,
          size: pagination.size
        }

        if (searchParams.plateNumber) {
          params.plateNumber = searchParams.plateNumber.trim()
        }

        if (searchParams.userType) {
          params.userType = searchParams.userType
        }

        if (searchParams.status) {
          params.status = searchParams.status
        }

        if (searchParams.startDate) {
          params.startDate = searchParams.startDate
        }

        if (searchParams.endDate) {
          params.endDate = searchParams.endDate
        }

        const response = await apiClient.get('/admin/finance/external-records', params)

        if (response.success && response.data) {
          const data = response.data
          parkingRecords.value = data.records || []
          pagination.total = data.totalElements || data.total || 0

          // 如果返回的页码是0-based，转换为1-based
          if (data.currentPage !== undefined) {
            pagination.page = data.currentPage + 1
          }
        } else {
          parkingRecords.value = []
          pagination.total = 0
        }
      } catch (error) {
        console.error('加载停车记录失败:', error)
        parkingRecords.value = []
        pagination.total = 0
      }
    }

    // ========== 交互方法 ==========
    const manualRefresh = async () => {
      loading.value = true
      try {
        // 并行加载所有数据
        await Promise.all([
          loadFinanceOverview(),
          loadRevenueTrend(),
          loadParkingRank(),
          loadParkingRecords()
        ])

        showUpdateNotification.value = true
        lastUpdateTime.value = new Date().toLocaleTimeString('zh-CN', {
          hour: '2-digit',
          minute: '2-digit',
          second: '2-digit'
        })

        setTimeout(() => {
          showUpdateNotification.value = false
        }, 3000)
      } finally {
        loading.value = false
      }
    }

    const toggleAutoRefresh = () => {
      autoRefresh.value = !autoRefresh.value
      if (autoRefresh.value) {
        startAutoRefresh()
      } else {
        stopAutoRefresh()
      }
    }

    const changeTimeRange = async (range) => {
      console.log('🔄 切换时间范围到:', range)
      timeRange.value = range

      if (range === 'custom') {
        // 设置默认日期（最近7天）
        const today = new Date()
        const weekAgo = new Date(today)
        weekAgo.setDate(today.getDate() - 7)

        customStartDate.value = formatTime(weekAgo, 'YYYY-MM-DD')
        customEndDate.value = formatTime(today, 'YYYY-MM-DD')

        console.log('📅 设置默认自定义日期:', customStartDate.value, '至', customEndDate.value)

        // 清空现有数据，准备加载
        revenueTrend.value = []

        // 立即触发加载
        setTimeout(() => {
          handleCustomDateChange()
        }, 100)

        return
      }

      // 其他时间范围立即加载
      await loadRevenueTrend()
    }

    const searchRecords = () => {
      pagination.page = 1
      loadParkingRecords()
    }

    const debouncedSearch = debounce(searchRecords, 500)

    const resetFilters = () => {
      searchParams.plateNumber = ''
      searchParams.userType = ''
      searchParams.status = ''
      searchParams.startDate = ''
      searchParams.endDate = ''
      pagination.page = 1
      loadParkingRecords()
    }

    const handlePageSizeChange = () => {
      pagination.page = 1
      loadParkingRecords()
    }

    const prevPage = () => {
      if (pagination.page > 1) {
        pagination.page--
        loadParkingRecords()
      }
    }

    const nextPage = () => {
      if (pagination.page < pagination.totalPages) {
        pagination.page++
        loadParkingRecords()
      }
    }

    const showRevenueTooltip = (item, index) => {
      console.log(`收入: ¥${item.revenue}, 日期: ${item.date}`)
    }

    // ========== 定时器控制 ==========
    const startAutoRefresh = () => {
      stopAutoRefresh()
      countdown.value = 30

      // 每30秒刷新数据
      refreshTimer = setInterval(async () => {
        await loadFinanceOverview()
        await loadParkingRank()
      }, 30000)

      // 倒计时显示
      countdownTimer = setInterval(() => {
        countdown.value--
        if (countdown.value <= 0) {
          countdown.value = 30
        }
      }, 1000)
    }

    const stopAutoRefresh = () => {
      if (refreshTimer) {
        clearInterval(refreshTimer)
        refreshTimer = null
      }
      if (countdownTimer) {
        clearInterval(countdownTimer)
        countdownTimer = null
      }
    }

    // ========== 生命周期 ==========
    onMounted(() => {
      // 更新时间显示
      const updateCurrentTime = () => {
        currentDateTime.value = new Date()
      }
      updateCurrentTime()
      timeUpdateTimer = setInterval(updateCurrentTime, 1000)

      // 设置默认日期
      const today = new Date()
      const weekAgo = new Date(today)
      weekAgo.setDate(today.getDate() - 7)

      // 设置停车记录默认日期
      searchParams.startDate = weekAgo.toISOString().split('T')[0]
      searchParams.endDate = today.toISOString().split('T')[0]

      // 初始化加载数据
      manualRefresh()

      // 启动自动刷新
      if (autoRefresh.value) {
        startAutoRefresh()
      }
    })

    onUnmounted(() => {
      stopAutoRefresh()
      if (timeUpdateTimer) {
        clearInterval(timeUpdateTimer)
      }
    })

    // ========== 返回所有变量和方法 ==========
    return {
      // 状态变量
      loading,
      autoRefresh,
      countdown,
      currentDateTime,
      timeRange,
      showUpdateNotification,
      lastUpdateTime,
      customDateLoading,
      customDateError,

      // 数据
      overview,
      revenueTrend,
      parkingRank,
      parkingRecords,
      searchParams,
      pagination,
      timeRanges,
      customStartDate,
      customEndDate,

      // 计算属性
      currentMonth,
      totalMonthlyRevenue,
      totalMonthlyCount,
      averageMonthlyRevenue,
      pieSegments,
      revenueYTicks,
      revenueLinePath,
      filteredRevenueData,
      filteredXAxisLabels,
      revenueMax,
      revenueMaxDate,
      revenueAvg,
      revenueTotal,

      // 工具方法
      formatCurrency,
      formatCompactNumber,
      formatNumber,
      formatTime,
      formatCompactDate,
      calculateDuration,
      formatLiveDuration,
      calculateEstimatedFee,
      getRankColor,
      getUserTypeText,
      getStatusText,

      // 交互方法
      manualRefresh,
      toggleAutoRefresh,
      handleCustomDateChange,
      changeTimeRange,
      searchRecords,
      debouncedSearch,
      resetFilters,
      handlePageSizeChange,
      prevPage,
      nextPage,
      showRevenueTooltip
    }
  }
}
</script>

<style scoped>
/* 简洁排名样式 */
.simple-rank-container {
  display: flex;
  align-items: center;
  gap: 20px;
  height: 100%;
  padding: 10px;
}

.simple-pie-chart {
  flex-shrink: 0;
}

.rank-segment {
  transition: stroke-dasharray 0.5s ease;
}

.pie-center-text {
  font-size: 14px;
  font-weight: 700;
  fill: #1e293b;
}

.pie-center-subtext {
  font-size: 12px;
  fill: #64748b;
}

/* 排名列表 */
.simple-rank-list {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 200px;
}

.rank-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  border-radius: 8px;
  background: white;
  transition: all 0.2s ease;
}

.rank-row:hover {
  background: #f8fafc;
  transform: translateX(4px);
}

.rank-order {
  display: flex;
  align-items: center;
  gap: 6px;
}

.order-number {
  font-size: 14px;
  font-weight: 800;
  color: white;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
}

.order-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
}

/* 不同排名的颜色 */
.rank-1 .order-number { background: #f59e0b; }
.rank-1 .order-dot { color: #f59e0b; }

.rank-2 .order-number { background: #94a3b8; }
.rank-2 .order-dot { color: #94a3b8; }

.rank-3 .order-number { background: #92400e; }
.rank-3 .order-dot { color: #92400e; }

.rank-4 .order-number { background: #3b82f6; }
.rank-4 .order-dot { color: #3b82f6; }

.rank-5 .order-number { background: #10b981; }
.rank-5 .order-dot { color: #10b981; }

.rank-name {
  flex: 1;
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.rank-percent {
  font-size: 13px;
  font-weight: 700;
  color: #3b82f6;
  min-width: 40px;
  text-align: right;
}

.rank-bar {
  width: 80px;
  height: 6px;
  background: #e2e8f0;
  border-radius: 3px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  border-radius: 3px;
  transition: width 0.5s ease;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .simple-rank-container {
    flex-direction: column;
    gap: 15px;
  }

  .simple-rank-list {
    width: 100%;
  }
}
/* 基础样式 */
.finance-dashboard {
  padding: 24px;
  min-height: 100vh;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
}

/* 头部样式 */
.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
  padding: 20px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.header-left h1 {
  margin: 0;
  font-size: 1.8em;
  font-weight: 700;
  color: #1e293b;
  display: flex;
  align-items: center;
  gap: 10px;
}

.subtitle {
  margin: 8px 0 0;
  color: #64748b;
  font-size: 0.95em;
}

.header-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 12px;
}

.refresh-controls {
  display: flex;
  align-items: center;
  gap: 16px;
}

.auto-refresh {
  display: flex;
  align-items: center;
  gap: 8px;
}

.refresh-status {
  font-size: 0.9em;
  color: #94a3b8;
  display: flex;
  align-items: center;
  gap: 6px;
}

.refresh-status.active {
  color: #10b981;
}

.refresh-status.active::before {
  content: '●';
  color: #10b981;
  animation: pulse 1s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.btn-toggle, .btn-refresh {
  padding: 8px 16px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: white;
  color: #475569;
  font-size: 0.9em;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-toggle:hover, .btn-refresh:hover {
  background: #f8fafc;
  border-color: #cbd5e1;
}

.btn-refresh:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.current-time {
  font-family: 'SF Mono', monospace;
  font-size: 0.9em;
  color: #64748b;
  padding: 4px 8px;
  background: #f8fafc;
  border-radius: 6px;
}

/* 统计卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.stat-card.highlight {
  border: 2px solid #3b82f6;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.05), white);
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.8em;
}

.stat-icon.active {
  background: linear-gradient(135deg, #3b82f6, #1d4ed8);
  color: white;
}

.stat-icon.revenue {
  background: linear-gradient(135deg, #10b981, #059669);
  color: white;
}

.stat-icon.month {
  background: linear-gradient(135deg, #8b5cf6, #7c3aed);
  color: white;
}

.stat-icon.count {
  background: linear-gradient(135deg, #f59e0b, #d97706);
  color: white;
}

.stat-value {
  font-size: 1.8em;
  font-weight: 800;
  color: #1e293b;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 0.9em;
  color: #64748b;
  margin-bottom: 8px;
}

.stat-trend {
  font-size: 0.85em;
  font-weight: 500;
}

.stat-trend.positive {
  color: #10b981;
}

.stat-trend.negative {
  color: #ef4444;
}

.stat-breakdown, .stat-details {
  display: flex;
  gap: 8px;
  font-size: 0.85em;
  color: #64748b;
  margin-top: 8px;
}

/* 图表区域布局优化 */
.charts-section {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 24px;
  margin-bottom: 24px;
  min-height: 380px;
}

.chart-card {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  height: 100%;
}

.revenue-chart {
  grid-column: 1;
}

.heatmap-chart {
  grid-column: 2;
}

.chart-header {
  padding: 20px;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.chart-title h3 {
  margin: 0;
  font-size: 1.2em;
  font-weight: 600;
  color: #1e293b;
}

.chart-controls {
  display: flex;
  align-items: center;
  gap: 12px;
}

.time-range {
  display: flex;
  gap: 4px;
}

.range-btn {
  padding: 6px 12px;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
  border-radius: 6px;
  font-size: 0.85em;
  color: #475569;
  cursor: pointer;
  transition: all 0.2s ease;
}

.range-btn:hover {
  background: #f1f5f9;
}

.range-btn.active {
  background: #3b82f6;
  color: white;
  border-color: #3b82f6;
}

.date-range {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.85em;
  color: #64748b;
}

.date-range input {
  padding: 6px 10px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  font-size: 0.85em;
  width: 130px;
  transition: all 0.2s ease;
}

.date-range input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.chart-legend {
  display: flex;
  gap: 16px;
  font-size: 0.85em;
  color: #64748b;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.legend-color {
  width: 12px;
  height: 12px;
  border-radius: 3px;
}

.legend-color.low {
  background: #10b981;
}

.legend-color.medium {
  background: #f59e0b;
}

.legend-color.high {
  background: #ef4444;
}

.chart-container {
  flex: 1;
  padding: 20px;
  display: flex;
  flex-direction: column;
  min-height: 300px;
}

.chart-container.has-data {
  min-height: 320px;
}

/* 收入趋势图表优化 */
.simple-line-chart {
  flex: 1;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.chart-wrapper {
  flex: 1;
  display: flex;
  position: relative;
  min-height: 200px;
}

/* 优化Y轴标签 */
.y-axis {
  width: 60px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding-right: 12px;
  border-right: 1px solid #e2e8f0;
  flex-shrink: 0;
}

.y-tick {
  font-size: 0.8em;
  color: #64748b;
  text-align: right;
  font-family: 'SF Mono', monospace;
  white-space: nowrap;
}

/* 图表区域填满 */
.chart-area {
  flex: 1;
  position: relative;
  padding: 0 20px 30px 0;
}

/* 网格线定位 */
.grid-lines {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 30px;
}

.grid-line {
  position: absolute;
  left: 0;
  right: 0;
  border-top: 1px dashed #e2e8f0;
}

/* 连接线 */
.line-path {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 30px;
}

.line {
  fill: none;
  stroke: #3b82f6;
  stroke-width: 2;
  stroke-linecap: round;
  stroke-linejoin: round;
}

/* 数据点定位优化 */
.data-points {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 30px;
}

.data-point {
  position: absolute;
  width: 10px;
  height: 10px;
  background: #3b82f6;
  border-radius: 50%;
  border: 2px solid white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  transform: translate(-50%, 50%);
  cursor: pointer;
  transition: all 0.2s ease;
  z-index: 2;
}

.data-point:hover {
  width: 14px;
  height: 14px;
  background: #1d4ed8;
}

.point-value {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 0.8em;
  font-weight: 600;
  color: #1e293b;
  background: white;
  padding: 2px 6px;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  opacity: 0;
  transition: opacity 0.2s ease;
  white-space: nowrap;
  z-index: 3;
}

.data-point:hover .point-value {
  opacity: 1;
}

/* X轴标签优化 */
.x-axis {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 20px;
  height: 30px;
  overflow: hidden;
}

.x-tick {
  position: absolute;
  font-size: 0.8em;
  color: #64748b;
  transform: translateX(-50%);
  white-space: nowrap;
  text-align: center;
  min-width: 40px;
}

/* 停车场热力图 */
.parking-heatmap {
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.heatmap-grid {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.heatmap-item {
  padding: 16px;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.heatmap-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.heatmap-item.low {
  background: rgba(16, 185, 129, 0.1);
  border: 1px solid rgba(16, 185, 129, 0.3);
}

.heatmap-item.medium {
  background: rgba(245, 158, 11, 0.1);
  border: 1px solid rgba(245, 158, 11, 0.3);
}

.heatmap-item.high {
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.3);
}

.zone-name {
  font-weight: 600;
  color: #1e293b;
  font-size: 0.95em;
}

.zone-stats {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.zone-utilization {
  font-size: 1.4em;
  font-weight: 700;
  font-family: 'SF Mono', monospace;
}

.heatmap-item.low .zone-utilization {
  color: #10b981;
}

.heatmap-item.medium .zone-utilization {
  color: #f59e0b;
}

.heatmap-item.high .zone-utilization {
  color: #ef4444;
}

.zone-count {
  font-size: 0.85em;
  color: #64748b;
}

.utilization-bar {
  height: 6px;
  background: #e2e8f0;
  border-radius: 3px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  transition: width 0.3s ease;
}

.heatmap-item.low .bar-fill {
  background: #10b981;
}

.heatmap-item.medium .bar-fill {
  background: #f59e0b;
}

.heatmap-item.high .bar-fill {
  background: #ef4444;
}

/* 图表摘要 */
.chart-summary, .heatmap-summary {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 12px;
  margin-top: 20px;
}

.summary-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.summary-label {
  font-size: 0.85em;
  color: #64748b;
  margin-bottom: 4px;
}

.summary-value {
  font-size: 1.1em;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 2px;
}

.summary-date, .summary-rate {
  font-size: 0.8em;
  color: #94a3b8;
}

/* 空状态 */
.chart-empty, .empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex: 1;
  height: 100%;
  color: #94a3b8;
  gap: 12px;
  padding: 40px 20px;
}

.empty-icon {
  font-size: 3em;
  opacity: 0.5;
}

.btn-load {
  padding: 8px 20px;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 0.9em;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-load:hover {
  background: #2563eb;
}

/* 停车记录表格 */
.records-section {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.section-header {
  padding: 24px;
  border-bottom: 1px solid #e2e8f0;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.section-title h2 {
  margin: 0;
  font-size: 1.5em;
  font-weight: 700;
  color: #1e293b;
}

.record-count {
  font-size: 0.9em;
  color: #64748b;
  background: #f8fafc;
  padding: 4px 10px;
  border-radius: 12px;
}

.section-filters {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.search-box {
  position: relative;
  flex: 1;
  max-width: 300px;
}

.search-input {
  width: 100%;
  padding: 10px 16px 10px 40px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 0.95em;
  color: #475569;
  background: white;
  transition: all 0.2s ease;
}

.search-input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.search-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #94a3b8;
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 12px;
}

.filter-select {
  padding: 10px 12px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: white;
  color: #475569;
  font-size: 0.9em;
  min-width: 120px;
  cursor: pointer;
}

.date-range {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.9em;
  color: #64748b;
}

.date-input {
  padding: 8px 12px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  font-size: 0.9em;
  width: 130px;
}

.btn-search, .btn-reset {
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  font-size: 0.9em;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-search {
  background: #3b82f6;
  color: white;
}

.btn-search:hover {
  background: #2563eb;
}

.btn-reset {
  background: #f1f5f9;
  color: #475569;
}

.btn-reset:hover {
  background: #e2e8f0;
}

/* 表格样式 */
.records-table {
  display: flex;
  flex-direction: column;
}

.table-container {
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
}

thead {
  background: #f8fafc;
}

th {
  padding: 16px 20px;
  text-align: left;
  font-weight: 600;
  color: #475569;
  font-size: 0.9em;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  border-bottom: 2px solid #e2e8f0;
  white-space: nowrap;
}

tbody tr {
  transition: all 0.2s ease;
  border-bottom: 1px solid #f1f5f9;
}

tbody tr:hover {
  background: #f8fafc;
}

tbody tr.row-active {
  background: rgba(59, 130, 246, 0.05);
  border-left: 4px solid #3b82f6;
}

tbody tr.row-completed {
  background: rgba(16, 185, 129, 0.05);
}

td {
  padding: 16px 20px;
  color: #475569;
  font-size: 0.95em;
  vertical-align: middle;
}

/* 各列特定样式 */
.col-index {
  text-align: center;
  color: #64748b;
  font-weight: 500;
  font-family: 'SF Mono', monospace;
}

.time-display {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.time-date {
  font-size: 0.85em;
  color: #64748b;
}

.time-clock {
  font-weight: 600;
  color: #1e293b;
  font-family: 'SF Mono', monospace;
}

.duration-display {
  font-weight: 600;
  color: #1e293b;
}

.live-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  margin-left: 8px;
  padding: 2px 6px;
  background: rgba(59, 130, 246, 0.1);
  color: #3b82f6;
  border-radius: 10px;
  font-size: 0.8em;
  font-weight: 500;
}

.live-dot {
  width: 6px;
  height: 6px;
  background: #3b82f6;
  border-radius: 50%;
  animation: pulse 1s infinite;
}

.plate-badge {
  font-family: monospace;
  font-weight: 600;
  color: #1e293b;
  background: #f1f5f9;
  padding: 4px 10px;
  border-radius: 6px;
  display: inline-block;
  letter-spacing: 0.5px;
}

.user-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.user-name {
  font-weight: 600;
  color: #1e293b;
}

.user-type {
  font-size: 0.85em;
  padding: 2px 8px;
  border-radius: 12px;
  display: inline-block;
  width: fit-content;
  font-weight: 500;
}

.user-type.external_user {
  background: #fef3c7;
  color: #92400e;
}

.user-type.student {
  background: #d1fae5;
  color: #065f46;
}

.user-type.teacher {
  background: #dbeafe;
  color: #1e40af;
}

.user-type.staff {
  background: #ede9fe;
  color: #5b21b6;
}

.fee-amount {
  font-weight: 700;
  color: #10b981;
  font-family: 'SF Mono', monospace;
}

.fee-rate {
  font-size: 0.85em;
  color: #64748b;
}

.fee-free {
  font-size: 0.85em;
  color: #10b981;
  font-weight: 500;
}

.fee-estimated {
  font-weight: 600;
  color: #f59e0b;
}

.fee-none {
  color: #94a3b8;
  font-style: italic;
}

.status-badge {
  padding: 6px 12px;
  border-radius: 12px;
  font-size: 0.85em;
  font-weight: 600;
  display: inline-block;
  min-width: 70px;
  text-align: center;
}

.status-badge.parking {
  background: #dbeafe;
  color: #1e40af;
}

.status-badge.completed {
  background: #d1fae5;
  color: #065f46;
}

.status-badge.cancelled {
  background: #f3f4f6;
  color: #374151;
}

.status-time {
  font-size: 0.85em;
  color: #64748b;
  margin-top: 4px;
}

.empty-row, .loading-row {
  text-align: center;
  padding: 40px 20px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  color: #94a3b8;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  color: #64748b;
}

.loading-spinner {
  width: 24px;
  height: 24px;
  border: 2px solid #f1f5f9;
  border-top: 2px solid #3b82f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-top: 1px solid #e2e8f0;
  background: #f8fafc;
}

.pagination-info {
  font-size: 0.9em;
  color: #64748b;
}

.pagination-controls {
  display: flex;
  align-items: center;
  gap: 16px;
}

.page-size {
  padding: 8px 12px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  background: white;
  color: #475569;
  font-size: 0.9em;
  cursor: pointer;
}

.page-btn {
  width: 36px;
  height: 36px;
  border: 1px solid #e2e8f0;
  background: white;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #475569;
  font-size: 1.2em;
  transition: all 0.2s ease;
}

.page-btn:hover:not(:disabled) {
  background: #f1f5f9;
  border-color: #cbd5e1;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-numbers {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 0.95em;
  color: #475569;
}

.current-page {
  font-weight: 700;
  color: #1e293b;
  font-size: 1.1em;
}

.page-separator {
  color: #cbd5e1;
  margin: 0 2px;
}

/* 全局加载状态 */
.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(4px);
}

.loading-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.loading-spinner-large {
  width: 48px;
  height: 48px;
  border: 3px solid #f1f5f9;
  border-top: 3px solid #3b82f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.loading-text {
  font-size: 1em;
  color: #475569;
  font-weight: 500;
}

/* 更新通知 */
.update-notification {
  position: fixed;
  bottom: 24px;
  right: 24px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  z-index: 1001;
  animation: slideInUp 0.3s ease-out;
}

@keyframes slideInUp {
  from {
    transform: translateY(100%);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.notification-content {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.95em;
  color: #475569;
}

.notification-icon {
  font-size: 1.2em;
  color: #10b981;
}

.notification-close {
  background: none;
  border: none;
  color: #94a3b8;
  font-size: 1.5em;
  cursor: pointer;
  padding: 0;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  transition: all 0.2s ease;
}

.notification-close:hover {
  background: #f1f5f9;
  color: #475569;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .charts-section {
    grid-template-columns: 1fr;
    gap: 20px;
  }

  .revenue-chart,
  .heatmap-chart {
    grid-column: 1;
  }
}

@media (max-width: 768px) {
  .dashboard-header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }

  .header-right {
    width: 100%;
    align-items: flex-start;
  }

  .refresh-controls {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .section-filters {
    flex-direction: column;
    align-items: stretch;
  }

  .search-box {
    max-width: 100%;
  }

  .filter-group {
    flex-wrap: wrap;
  }

  th, td {
    padding: 12px 16px;
  }

  .pagination {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }

  .pagination-controls {
    justify-content: center;
  }

  .chart-controls {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .time-range {
    flex-wrap: wrap;
  }

  .date-range {
    flex-direction: column;
    align-items: flex-start;
  }

  .date-range input {
    width: 100%;
  }

  .x-tick {
    font-size: 0.7em;
  }
}

@media (max-width: 480px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }

  .chart-summary, .heatmap-summary {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .heatmap-grid {
    grid-template-columns: 1fr;
  }

  table {
    font-size: 0.9em;
  }

  .status-badge {
    min-width: 60px;
    padding: 4px 8px;
  }
}
/* 月收入排名新增样式 */
.rank-revenue {
  font-size: 12px;
  font-weight: 600;
  color: #10b981;
  font-family: 'SF Mono', monospace;
  min-width: 70px;
  text-align: right;
  margin-left: 8px;
}

.month-summary {
  display: flex;
  justify-content: space-between;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px dashed #e2e8f0;
}

.summary-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
}

.summary-label {
  font-size: 11px;
  color: #64748b;
  margin-bottom: 2px;
}

.summary-value {
  font-size: 13px;
  font-weight: 700;
  color: #1e293b;
}

.pie-center-subtext {
  font-size: 11px;
  fill: #64748b;
}

/* 调整排名列表高度以适应更多内容 */
.simple-rank-list {
  min-height: 260px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .rank-revenue {
    font-size: 11px;
    min-width: 60px;
  }

  .month-summary {
    flex-direction: column;
    gap: 8px;
  }
}
.btn-load {
  margin: 10px auto;
  padding: 8px 16px;
  background-color: #3b82f6;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  display: inline-block;  /* 改为 inline-block */
  text-align: center;
  min-width: 100px;
}

/* 确保按钮在父容器中居中 */
.chart-empty .btn-load {
  display: block;
  margin: 10px auto;
}.chart-empty

.btn-load:hover {
  background-color: #2563eb;
}

.btn-load:disabled {
  background-color: #9ca3af;
  cursor: not-allowed;
}

/* 自定义范围的加载按钮 */
.btn-load-custom {
  margin-left: 10px;
  padding: 6px 12px;
  background-color: #10b981;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.btn-load-custom:hover {
  background-color: #059669;
}

/* 日期选择器样式 */
.date-range {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 10px;
  padding: 10px;
  background-color: #f9fafb;
  border-radius: 6px;
}

.date-input-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.date-range input[type="date"] {
  padding: 6px 10px;
  border: 1px solid #d1d5db;
  border-radius: 4px;
  font-size: 14px;
}
</style>
