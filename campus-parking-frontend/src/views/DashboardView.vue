
<template>
  <div class="dashboard">
    <h1>📊 系统仪表盘</h1>

    <!-- 系统概览 -->
    <div class="overview-section">
      <h2>系统概览</h2>
      <div class="overview-cards">
        <div class="overview-card users">
          <div class="card-icon">👥</div>
          <div class="card-content">
            <h3>{{ overview.totalUsers }}</h3>
            <p>总用户数</p>
            <div class="card-details">
              <span>学生: {{ overview.studentUsers }}</span>
              <span>教师: {{ overview.teacherUsers }}</span>
            </div>
          </div>
        </div>

        <div class="overview-card vehicles">
          <div class="card-icon">🚗</div>
          <div class="card-content">
            <h3>{{ overview.totalVehicles }}</h3>
            <p>总车辆数</p>
            <div class="card-details">
              <span>汽车: {{ overview.carVehicles }}</span>
              <span>摩托车: {{ overview.motorcycleVehicles }}</span>
            </div>
          </div>
        </div>

        <div class="overview-card parking-spots">
          <div class="card-icon">🅿️</div>
          <div class="card-content">
            <h3>{{ overview.totalSpots }}</h3>
            <p>停车位总数</p>
            <div class="card-details">
              <span>空闲: {{ overview.availableSpots }}</span>
              <span>占用: {{ overview.occupiedSpots }}</span>
            </div>
            <div class="utilization-rate">
              利用率: {{ overview.utilizationRate?.toFixed(1) }}%
            </div>
          </div>
        </div>

        <div class="overview-card revenue">
          <div class="card-icon">💰</div>
          <div class="card-content">
            <h3>¥{{ overview.todayRevenue }}</h3>
            <p>今日收入</p>
            <div class="card-details">
              <span>总记录: {{ overview.totalRecords }}</span>
              <span>当前停车: {{ overview.currentParking }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 今日统计 -->
    <div class="today-section">
      <h2>今日统计</h2>
      <div class="today-cards">
        <div class="today-card">
          <h3>{{ todayStats.todayRecords }}</h3>
          <p>今日停车记录</p>
        </div>
        <div class="today-card">
          <h3>{{ todayStats.todayCompleted }}</h3>
          <p>今日完成停车</p>
        </div>
        <div class="today-card">
          <h3>¥{{ todayStats.todayRevenue }}</h3>
          <p>今日收入</p>
        </div>
      </div>
    </div>

    <!-- 收入统计 -->
    <div class="revenue-section">
      <h2>收入统计</h2>
      <div class="revenue-cards">
        <div class="revenue-card total">
          <h3>¥{{ revenueStats.totalRevenue }}</h3>
          <p>历史总收入</p>
        </div>
        <div class="revenue-chart">
          <h4>近7天收入趋势</h4>
          <div class="chart-bars">
            <div v-for="(revenue, date) in revenueStats.last7DaysRevenue"
                 :key="date" class="chart-bar">
              <div class="bar-label">{{ formatDateShort(date) }}</div>
              <div class="bar-container">
                <div class="bar-fill" :style="{ height: getBarHeight(revenue) + '%' }"></div>
              </div>
              <div class="bar-value">¥{{ revenue }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 快速操作 -->
    <div class="quick-actions">
      <h2>快速操作</h2>
      <div class="action-buttons">
        <button @click="navigateTo('/parking-records')" class="action-btn start-parking">
          🅿️ 开始停车
        </button>
        <button @click="navigateTo('/parking-spots')" class="action-btn manage-spots">
          📍 管理停车位
        </button>
        <button @click="navigateTo('/vehicles')" class="action-btn manage-vehicles">
          🚗 管理车辆
        </button>
        <button @click="navigateTo('/users')" class="action-btn manage-users">
          👥 管理用户
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { authFetch } from '../utils/auth'
const router = useRouter()

const overview = ref({})
const todayStats = ref({})
const revenueStats = ref({
  last7DaysRevenue: {},
  totalRevenue: 0
})

onMounted(() => {
  loadStatistics()
})

const loadStatistics = async () => {
  try {
    // 加载系统概览
    const overviewResponse = await authFetch('/api/statistics/overview')
    if (overviewResponse.ok) {
      overview.value = await overviewResponse.json()
    }

    // 加载今日统计
    const todayResponse = await authFetch('/api/statistics/today')
    if (todayResponse.ok) {
      todayStats.value = await todayResponse.json()
    }

    // 加载收入统计
    const revenueResponse = await authFetch('/api/statistics/revenue')
    if (revenueResponse.ok) {
      revenueStats.value = await revenueResponse.json()
    }
  } catch (error) {
    console.error('加载统计信息失败:', error)
    // 使用模拟数据
    overview.value = getMockOverview()
    todayStats.value = getMockTodayStats()
    revenueStats.value = getMockRevenueStats()
  }
}

const navigateTo = (path) => {
  router.push(path)
}

const formatDateShort = (dateString) => {
  const date = new Date(dateString)
  return `${date.getMonth() + 1}/${date.getDate()}`
}

const getBarHeight = (revenue) => {
  const maxRevenue = Math.max(...Object.values(revenueStats.value.last7DaysRevenue).map(r => parseFloat(r)))
  return maxRevenue > 0 ? (parseFloat(revenue) / maxRevenue) * 100 : 0
}

// 模拟数据
const getMockOverview = () => ({
  totalUsers: 156,
  studentUsers: 120,
  teacherUsers: 36,
  totalVehicles: 89,
  carVehicles: 67,
  motorcycleVehicles: 22,
  totalSpots: 50,
  availableSpots: 32,
  occupiedSpots: 18,
  utilizationRate: 36.0,
  totalRecords: 1247,
  currentParking: 18,
  completedRecords: 1229,
  todayRevenue: 285.50
})

const getMockTodayStats = () => ({
  todayRecords: 23,
  todayCompleted: 18,
  todayRevenue: 285.50
})

const getMockRevenueStats = () => ({
  last7DaysRevenue: {
    '2024-01-12': 120.00,
    '2024-01-13': 185.50,
    '2024-01-14': 210.00,
    '2024-01-15': 165.75,
    '2024-01-16': 195.25,
    '2024-01-17': 230.00,
    '2024-01-18': 285.50
  },
  totalRevenue: 12560.75
})
</script>

<style scoped>
.dashboard {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

h1 {
  color: #2c3e50;
  margin-bottom: 30px;
  text-align: center;
}

h2 {
  color: #34495e;
  margin: 30px 0 20px 0;
  border-bottom: 2px solid #ecf0f1;
  padding-bottom: 10px;
}

/* 系统概览样式 */
.overview-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.overview-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  display: flex;
  align-items: center;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.overview-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.15);
}

.overview-card.users { border-left: 4px solid #3498db; }
.overview-card.vehicles { border-left: 4px solid #e74c3c; }
.overview-card.parking-spots { border-left: 4px solid #2ecc71; }
.overview-card.revenue { border-left: 4px solid #f39c12; }

.card-icon {
  font-size: 3em;
  margin-right: 20px;
}

.card-content h3 {
  font-size: 2.2em;
  margin: 0 0 8px 0;
  color: #2c3e50;
}

.card-content p {
  margin: 0 0 12px 0;
  color: #7f8c8d;
  font-weight: 500;
}

.card-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.card-details span {
  font-size: 0.9em;
  color: #95a5a6;
}

.utilization-rate {
  margin-top: 8px;
  padding: 4px 8px;
  background: #ecf0f1;
  border-radius: 12px;
  font-size: 0.85em;
  color: #2c3e50;
  display: inline-block;
}

/* 今日统计样式 */
.today-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 30px;
}

.today-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  border-top: 4px solid #9b59b6;
}

.today-card h3 {
  font-size: 2em;
  margin: 0 0 8px 0;
  color: #2c3e50;
}

.today-card p {
  margin: 0;
  color: #7f8c8d;
  font-weight: 500;
}

/* 收入统计样式 */
.revenue-cards {
  display: grid;
  grid-template-columns: 300px 1fr;
  gap: 30px;
  margin-bottom: 30px;
}

.revenue-card.total {
  background: white;
  border-radius: 8px;
  padding: 30px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  border-top: 4px solid #27ae60;
}

.revenue-card.total h3 {
  font-size: 2.5em;
  margin: 0 0 12px 0;
  color: #27ae60;
}

.revenue-card.total p {
  margin: 0;
  color: #7f8c8d;
  font-size: 1.1em;
}

.revenue-chart {
  background: white;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.revenue-chart h4 {
  margin: 0 0 20px 0;
  color: #2c3e50;
  text-align: center;
}

.chart-bars {
  display: flex;
  justify-content: space-around;
  align-items: flex-end;
  height: 200px;
  gap: 10px;
}

.chart-bar {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
}

.bar-label {
  font-size: 0.8em;
  color: #7f8c8d;
  margin-bottom: 8px;
}

.bar-container {
  width: 30px;
  height: 150px;
  background: #ecf0f1;
  border-radius: 4px;
  position: relative;
  overflow: hidden;
}

.bar-fill {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(to top, #3498db, #2980b9);
  border-radius: 4px;
  transition: height 0.5s ease;
}

.bar-value {
  font-size: 0.8em;
  color: #2c3e50;
  margin-top: 8px;
  font-weight: 500;
}

/* 快速操作样式 */
.quick-actions {
  background: white;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.action-buttons {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.action-btn {
  padding: 16px 20px;
  border: none;
  border-radius: 8px;
  font-size: 1.1em;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.action-btn.start-parking {
  background: linear-gradient(135deg, #2ecc71, #27ae60);
  color: white;
}

.action-btn.manage-spots {
  background: linear-gradient(135deg, #3498db, #2980b9);
  color: white;
}

.action-btn.manage-vehicles {
  background: linear-gradient(135deg, #e74c3c, #c0392b);
  color: white;
}

.action-btn.manage-users {
  background: linear-gradient(135deg, #9b59b6, #8e44ad);
  color: white;
}

.action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.2);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .revenue-cards {
    grid-template-columns: 1fr;
  }

  .overview-cards {
    grid-template-columns: 1fr;
  }

  .action-buttons {
    grid-template-columns: 1fr;
  }
}
</style>
