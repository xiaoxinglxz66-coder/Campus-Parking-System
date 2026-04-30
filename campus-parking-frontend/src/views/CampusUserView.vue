<template>
  <div class="campus-user-view">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="user-info">
          <div class="user-avatar">
            <span class="avatar-icon">🎓</span>
          </div>
          <div class="user-details">
            <h1 class="user-name">{{ username }}</h1>
            <p class="user-welcome">欢迎回到校内用户中心</p>
            <div class="user-badges">
              <span class="badge free-parking">🆓 免费停车特权</span>
              <span class="badge campus-user">🎯 校内用户专属</span>
            </div>
          </div>
        </div>
        <div class="header-actions">
          <!-- 修改：添加调试ID -->
          <button id="profile-btn-header" class="action-btn profile" @click="testButtonClick('header')">
            <span class="btn-icon">👤</span>
            <span class="btn-text">个人中心</span>
          </button>
        </div>
      </div>
    </div>

    <!-- 主要内容区域 - 横屏布局 -->
    <div class="main-content">
      <!-- 左侧统计和状态区域 -->
      <div class="left-sidebar">
        <div class="stats-section">
          <h3 class="section-title">📊 我的状态</h3>
          <div class="stats-cards">
            <div class="stat-card free">
              <div class="stat-content">
                <div class="stat-icon">🆓</div>
                <div class="stat-info">
                  <div class="stat-value">免费停车</div>
                  <div class="stat-label">校内用户专属特权</div>
                </div>
              </div>
              <div class="stat-badge">特权</div>
            </div>

            <div class="stat-card vehicles">
              <div class="stat-content">
                <div class="stat-icon">🚗</div>
                <div class="stat-info">
                  <div class="stat-value">{{ myVehiclesCount }}</div>
                  <div class="stat-label">我的车辆</div>
                </div>
              </div>
              <div class="stat-trend">
                <span class="trend-text">已注册</span>
              </div>
            </div>

            <div class="stat-card records">
              <div class="stat-content">
                <div class="stat-icon">📅</div>
                <div class="stat-info">
                  <div class="stat-value">{{ parkingRecordsCount }}</div>
                  <div class="stat-label">停车记录</div>
                </div>
              </div>
              <div class="stat-trend">
                <span class="trend-text">历史总数</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 快速操作 -->
        <div class="quick-actions">
          <h3 class="section-title">⚡ 快速操作</h3>
          <div class="action-buttons">
            <button class="quick-btn" @click="navigateTo('/campus-user/vehicles')">
              <span class="btn-icon">🚗</span>
              <span class="btn-text">管理车辆</span>
            </button>
            <button class="quick-btn" @click="navigateTo('/campus-parking-records')">
              <span class="btn-icon">📊</span>
              <span class="btn-text">查看记录</span>
            </button>
            <!-- 添加调试ID -->
            <button id="profile-btn-quick" class="quick-btn" @click="testButtonClick('quick')">
              <span class="btn-icon">👤</span>
              <span class="btn-text">个人中心</span>
            </button>
          </div>
        </div>
      </div>

      <!-- 右侧主要功能区域 -->
      <div class="right-main">
        <!-- 停车服务 -->
        <div class="service-section">
          <div class="section-header">
            <h2 class="section-title">🅿️ 停车服务</h2>
            <p class="section-description">便捷的校园停车管理</p>
          </div>
          <div class="service-cards">
            <div class="service-card primary" @click="navigateTo('/parking')">
              <div class="service-content">
                <div class="service-icon">🚗</div>
                <div class="service-info">
                  <h3 class="service-title">开始停车</h3>
                  <p class="service-description">选择空闲车位开始停车，享受免费停车特权</p>
                </div>
                <div class="service-badge free">免费</div>
              </div>
              <div class="service-arrow">→</div>
            </div>

            <div class="service-card" @click="navigateTo('/campus-parking-records')">
              <div class="service-content">
                <div class="service-icon">📊</div>
                <div class="service-info">
                  <h3 class="service-title">停车记录</h3>
                  <p class="service-description">查看历史停车记录和详细信息</p>
                </div>
                <div class="service-stats">{{ parkingRecordsCount }} 条记录</div>
              </div>
              <div class="service-arrow">→</div>
            </div>
          </div>
        </div>

        <!-- 个人信息管理 -->
        <div class="profile-section">
          <div class="section-header">
            <h2 class="section-title">👤 个人信息</h2>
            <p class="section-description">管理您的账户和车辆信息</p>
          </div>
          <div class="profile-cards">
            <!-- 添加调试ID -->
            <div id="profile-card-main" class="profile-card" @click="testButtonClick('card')">
              <div class="profile-content">
                <div class="profile-icon">👤</div>
                <div class="profile-info">
                  <h3 class="profile-title">个人中心</h3>
                  <p class="profile-description">查看和编辑个人信息、修改密码</p>
                  <div class="profile-status">
                    <span class="status-badge verified">已验证</span>
                  </div>
                </div>
              </div>
              <div class="profile-arrow">→</div>
            </div>

            <div class="profile-card" @click="navigateTo('/campus-user/vehicles')">
              <div class="profile-content">
                <div class="profile-icon">🚗</div>
                <div class="profile-info">
                  <h3 class="profile-title">我的车辆</h3>
                  <p class="profile-description">管理注册车辆信息</p>
                  <div class="profile-status">
                    <span class="status-badge count">{{ myVehiclesCount }} 辆车</span>
                  </div>
                </div>
              </div>
              <div class="profile-arrow">→</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const username = ref(localStorage.getItem('username') || '用户')
const myVehiclesCount = ref(0)
const parkingRecordsCount = ref(0)

console.log('=== CampusUserView 初始化 ===')
console.log('Router实例:', router)
console.log('当前路径:', window.location.pathname)

// 测试按钮点击
// CampusUserView.vue 中的 testButtonClick 函数
const testButtonClick = (source) => {
  console.log('=== 🎯 CampusUserView 个人中心点击开始 ===')
  console.log(`来源按钮: ${source}`)

  // 打印所有localStorage内容
  console.log('=== 点击前的localStorage ===')
  for (let i = 0; i < localStorage.length; i++) {
    const key = localStorage.key(i)
    const value = localStorage.getItem(key)
    // 不打印完整的token
    if (key !== 'token') {
      console.log(`${key}: ${value}`)
    } else {
      console.log(`${key}: 存在 (长度: ${value.length})`)
    }
  }

  // 获取当前用户ID
  const userId = localStorage.getItem('userId')
  console.log('当前用户ID:', userId)

  // 如果没有userId，显示警告
  if (!userId) {
    console.error('❌ 严重问题：localStorage中没有userId！')
    console.log('尝试从token解析...')

    const token = localStorage.getItem('token')
    if (token) {
      try {
        const payload = JSON.parse(atob(token.split('.')[1]))
        console.log('JWT Payload:', payload)

        if (payload.userId) {
          const parsedUserId = payload.userId.toString()
          console.log('✅ 从token解析到用户ID:', parsedUserId)

          // 立即保存并重定向
          localStorage.setItem('userId', parsedUserId)
          console.log(`🔄 跳转到: /profile/${parsedUserId}`)
          router.push(`/profile/${parsedUserId}`)
          return
        } else {
          console.error('❌ JWT token中没有userId字段！')
          console.log('Payload内容:', payload)
        }
      } catch (e) {
        console.error('解析token失败:', e)
      }
    }

    alert('无法获取用户信息，请重新登录')
    router.push('/login')
    return
  }

  console.log(`尝试路由跳转到 /profile/${userId}`)
  router.push(`/profile/${userId}`)
  console.log('=== 🎯 CampusUserView 个人中心点击结束 ===')
}

// 手动测试函数
const manualTest = () => {
  console.log('=== 手动测试开始 ===')

  // 测试1: 直接调用路由
  console.log('测试1: 直接调用 router.push')
  router.push('/profile')

  // 测试2: 使用 window.location
  setTimeout(() => {
    console.log('测试2: 使用 window.location')
    window.location.href = '/profile'
  }, 1000)

  // 测试3: 创建新标签页
  setTimeout(() => {
    console.log('测试3: 创建新标签页')
    window.open('/profile', '_blank')
  }, 2000)
}

// 修改 navigateTo 函数
const navigateTo = (path) => {
  console.log(`导航到: ${path}`)
  router.push(path)
}

// 监听页面点击事件
onMounted(() => {
  console.log('CampusUserView 页面加载完成')

  // 添加全局点击监听
  document.addEventListener('click', (e) => {
    if (e.target.closest('.action-btn.profile') ||
      e.target.closest('#profile-btn-quick') ||
      e.target.closest('#profile-card-main')) {
      console.log('📌 检测到个人中心相关元素点击:', e.target)
    }
  })

  loadUserData()
})

const loadUserData = async () => {
  try {
    console.log('开始加载用户数据...')
    const token = localStorage.getItem('token')
    console.log('Token存在:', !!token)

    // 加载车辆数据
    const vehiclesResponse = await fetch('/api/vehicles/my-vehicles', {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    })

    console.log('车辆API响应状态:', vehiclesResponse.status)

    if (vehiclesResponse.ok) {
      const vehicles = await vehiclesResponse.json()
      console.log('获取到车辆数据:', vehicles)
      myVehiclesCount.value = vehicles.length
      console.log('车辆数量:', myVehiclesCount.value)
    } else {
      console.error('获取车辆数据失败:', vehiclesResponse.status)
    }
  } catch (error) {
    console.error('加载用户数据失败:', error)
  }
}
</script>


<style scoped>
.campus-user-view {
  min-height: 100vh;
  background: white;
  padding: 0;
}

/* 页面头部 */
.page-header {
  background: linear-gradient(135deg, #27ae60 0%, #229954 100%);
  color: white;
  padding: 30px 0;
  border-bottom: 1px solid #e9ecef;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 40px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 3px solid rgba(255, 255, 255, 0.3);
}

.avatar-icon {
  font-size: 2.5em;
}

.user-details {
  flex: 1;
}

.user-name {
  font-size: 2em;
  font-weight: 700;
  margin: 0 0 5px 0;
}

.user-welcome {
  font-size: 1.1em;
  opacity: 0.9;
  margin: 0 0 15px 0;
}

.user-badges {
  display: flex;
  gap: 10px;
}

.badge {
  background: rgba(255, 255, 255, 0.2);
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 0.85em;
  font-weight: 500;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.action-btn.profile {
  background: rgba(255, 255, 255, 0.15);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.3);
  padding: 10px 20px;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
}

.action-btn.profile:hover {
  background: rgba(255, 255, 255, 0.25);
  transform: translateY(-2px);
}

/* 主要内容区域 - 横屏布局 */
.main-content {
  max-width: 1200px;
  margin: 30px auto;
  padding: 0 40px;
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 30px;
  align-items: start;
}

/* 左侧边栏 */
.left-sidebar {
  display: flex;
  flex-direction: column;
  gap: 25px;
}

.section-title {
  font-size: 1.2em;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 20px 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 统计卡片 */
.stats-cards {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.stat-card {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 20px;
  border-left: 4px solid transparent;
  transition: all 0.3s ease;
  position: relative;
}

.stat-card.free { border-left-color: #27ae60; }
.stat-card.vehicles { border-left-color: #3498db; }
.stat-card.records { border-left-color: #9b59b6; }

.stat-card:hover {
  transform: translateX(5px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 15px;
}

.stat-icon {
  font-size: 2em;
  width: 50px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 1.4em;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 4px;
}

.stat-label {
  color: #7f8c8d;
  font-size: 0.9em;
}

.stat-badge {
  position: absolute;
  top: 15px;
  right: 15px;
  background: #27ae60;
  color: white;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 0.8em;
  font-weight: 500;
}

.stat-trend {
  position: absolute;
  top: 15px;
  right: 15px;
  color: #95a5a6;
  font-size: 0.85em;
}

/* 快速操作 */
.quick-actions {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 20px;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.quick-btn {
  background: white;
  border: 2px solid #e9ecef;
  border-radius: 8px;
  padding: 15px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 12px;
  font-weight: 500;
  color: #2c3e50;
}

.quick-btn:hover {
  border-color: #27ae60;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(39, 174, 96, 0.15);
}

/* 右侧主要内容 */
.right-main {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.service-section,
.profile-section {
  background: white;
  border-radius: 16px;
  padding: 30px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid #f1f2f6;
}

.section-header {
  margin-bottom: 25px;
}

.section-title {
  font-size: 1.6em;
  font-weight: 700;
  color: #2c3e50;
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.section-description {
  font-size: 1.1em;
  color: #7f8c8d;
  margin: 0;
}

/* 服务卡片 */
.service-cards,
.profile-cards {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.service-card,
.profile-card {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 25px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid transparent;
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: relative;
  overflow: hidden;
}

.service-card::before,
.profile-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(39, 174, 96, 0.1), transparent);
  transition: left 0.5s ease;
}

.service-card:hover::before,
.profile-card:hover::before {
  left: 100%;
}

.service-card:hover,
.profile-card:hover {
  transform: translateY(-3px);
  border-color: #27ae60;
  box-shadow: 0 8px 25px rgba(39, 174, 96, 0.15);
}

.service-card.primary {
  background: linear-gradient(135deg, #27ae60, #229954);
  color: white;
}

.service-card.primary:hover {
  border-color: #fff;
  box-shadow: 0 8px 25px rgba(39, 174, 96, 0.3);
}

.service-content,
.profile-content {
  display: flex;
  align-items: center;
  gap: 20px;
  flex: 1;
}

.service-icon,
.profile-icon {
  font-size: 2.2em;
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  background: rgba(39, 174, 96, 0.1);
}

.service-card.primary .service-icon {
  background: rgba(255, 255, 255, 0.2);
}

.service-info,
.profile-info {
  flex: 1;
}

.service-title,
.profile-title {
  font-size: 1.3em;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 8px 0;
}

.service-card.primary .service-title {
  color: white;
}

.service-description,
.profile-description {
  color: #7f8c8d;
  margin: 0 0 12px 0;
  font-size: 0.95em;
  line-height: 1.4;
}

.service-card.primary .service-description {
  color: rgba(255, 255, 255, 0.9);
}

.service-badge {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  padding: 6px 12px;
  border-radius: 15px;
  font-size: 0.85em;
  font-weight: 500;
  margin-right: 15px;
}

.service-badge.free {
  background: rgba(255, 255, 255, 0.3);
}

.service-stats {
  color: #7f8c8d;
  font-size: 0.9em;
  font-weight: 500;
  margin-right: 15px;
}

.profile-status {
  display: flex;
  gap: 8px;
}

.status-badge {
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 0.8em;
  font-weight: 500;
}

.status-badge.verified {
  background: rgba(39, 174, 96, 0.1);
  color: #27ae60;
}

.status-badge.count {
  background: rgba(52, 152, 219, 0.1);
  color: #3498db;
}

.service-arrow,
.profile-arrow {
  font-size: 1.4em;
  color: #bdc3c7;
  transition: all 0.3s ease;
}

.service-card:hover .service-arrow,
.profile-card:hover .profile-arrow {
  color: #27ae60;
  transform: translateX(5px);
}

.service-card.primary .service-arrow,
.service-card.primary:hover .service-arrow {
  color: white;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .main-content {
    grid-template-columns: 1fr;
    gap: 20px;
  }

  .left-sidebar {
    order: 2;
  }

  .right-main {
    order: 1;
  }

  .stats-cards {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 15px;
  }

  .action-buttons {
    flex-direction: row;
  }
}

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    gap: 20px;
    text-align: center;
  }

  .user-info {
    flex-direction: column;
    text-align: center;
  }

  .user-badges {
    justify-content: center;
  }

  .main-content {
    padding: 0 20px;
  }

  .stats-cards {
    grid-template-columns: 1fr;
  }

  .action-buttons {
    flex-direction: column;
  }

  .service-section,
  .profile-section {
    padding: 20px;
  }
}

@media (max-width: 480px) {
  .header-content {
    padding: 0 20px;
  }

  .main-content {
    padding: 0 15px;
  }

  .service-card,
  .profile-card {
    flex-direction: column;
    text-align: center;
    gap: 15px;
  }

  .service-content,
  .profile-content {
    flex-direction: column;
    text-align: center;
  }
}
</style>
