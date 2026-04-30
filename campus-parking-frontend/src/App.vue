<template>
  <div id="app">
    <header v-if="showHeader" class="app-header">
      <div class="header-content">
        <div class="header-left">
          <h1>🚗 校园停车管理系统</h1>
        </div>
        <nav class="main-nav">
          <!-- 根据用户角色显示不同的主导航 -->
          <RouterLink v-if="isAdmin" to="/admin-dashboard">管理员面板</RouterLink>
          <RouterLink v-else-if="isExternalUser" to="/external-user">用户中心</RouterLink>
          <RouterLink v-else to="/campus-user">用户中心</RouterLink>

          <RouterLink v-if="isAdmin" to="/users">用户管理</RouterLink>
          <RouterLink to="/vehicles">车辆管理</RouterLink>
          <RouterLink to="/parking-spots">停车位管理</RouterLink>
          <RouterLink to="/parking-records">停车记录</RouterLink>
          <RouterLink to="/about">关于</RouterLink>
          <RouterLink v-if="isAdmin" to="/settings">系统设置</RouterLink>
        </nav>
        <div class="user-info">
          <span class="welcome-text">欢迎，{{ username }}</span>
          <button @click="handleLogout" class="logout-btn">退出登录</button>
        </div>
      </div>
    </header>

    <!-- 简化 class 绑定 -->
    <main :class="mainClass">
      <RouterView />
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { auth } from './utils/auth'

const router = useRouter()
const route = useRoute()

const isLoggedIn = computed(() => auth.isLoggedIn())
const username = computed(() => localStorage.getItem('username') || '')
const isAdmin = computed(() => localStorage.getItem('role') === 'ADMIN')
const isExternalUser = computed(() => localStorage.getItem('role') === 'EXTERNAL_USER')

// 判断是否显示 header - 排除登录页
const showHeader = computed(() => {
  return isLoggedIn.value && route.path !== '/' && route.path !== '/login'
})

const mainClass = computed(() => {
  return showHeader.value ? 'with-header' : 'full-height'
})

const handleLogout = () => {
  if (confirm('确定要退出登录吗？')) {
    auth.logout()
    router.push('/')
  }
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body {
  height: 100%;
}

body {
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #333;
  line-height: 1.6;
}

#app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.app-header {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  color: #2c3e50;
  box-shadow: 0 2px 20px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 1000;
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 70px;
}

.header-left h1 {
  font-size: 1.6em;
  font-weight: 700;
  color: #3498db; /* 使用蓝色，看起来像渐变的主色 */
  margin: 0;
  /* 完全移除背景相关属性 */
}

.main-nav {
  display: flex;
  gap: 8px;
  flex: 1;
  justify-content: center;
  margin: 0 40px;
}

.main-nav a {
  color: #5a6c7d;
  text-decoration: none;
  padding: 10px 20px;
  border-radius: 8px;
  transition: all 0.3s ease;
  font-weight: 500;
  font-size: 0.95em;
  white-space: nowrap;
}

.main-nav a:hover {
  background: rgba(52, 152, 219, 0.1);
  color: #3498db;
  transform: translateY(-1px);
}

.main-nav a.router-link-exact-active {
  background: linear-gradient(135deg, #3498db, #2980b9);
  color: white;
  box-shadow: 0 4px 12px rgba(52, 152, 219, 0.3);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.welcome-text {
  color: #5a6c7d;
  font-weight: 500;
}

.logout-btn {
  background: linear-gradient(135deg, #e74c3c, #c0392b);
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-weight: 500;
  font-size: 0.9em;
}

.logout-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(231, 76, 60, 0.3);
}

/* 主要内容区域 */
main {
  flex: 1;
  width: 100%;
}

main.with-header {
  padding-top: 0;
  min-height: calc(100vh - 70px);
}

main.full-height {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 0;
}

/* 通用页面容器 */
.page-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 30px 24px;
  width: 100%;
}

.page-header {
  margin-bottom: 30px;
  text-align: center;
}

.page-header h1 {
  font-size: 2.5em;
  font-weight: 700;
  color: white;
  margin-bottom: 10px;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
}

.page-header p {
  font-size: 1.2em;
  color: rgba(255, 255, 255, 0.9);
  max-width: 600px;
  margin: 0 auto;
}

/* 卡片通用样式 */
.card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 30px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  margin-bottom: 24px;
}

.card-header {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 2px solid #f8f9fa;
}

.card-header h2 {
  font-size: 1.5em;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
}

/* 网格布局 */
.grid {
  display: grid;
  gap: 24px;
}

.grid-2 {
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
}

.grid-3 {
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
}

.grid-4 {
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
}

/* 按钮样式 */
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  font-size: 0.95em;
  cursor: pointer;
  transition: all 0.3s ease;
  text-decoration: none;
  white-space: nowrap;
}

.btn-primary {
  background: linear-gradient(135deg, #3498db, #2980b9);
  color: white;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(52, 152, 219, 0.4);
}

.btn-success {
  background: linear-gradient(135deg, #27ae60, #229954);
  color: white;
}

.btn-success:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(39, 174, 96, 0.4);
}

.btn-warning {
  background: linear-gradient(135deg, #f39c12, #e67e22);
  color: white;
}

.btn-warning:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(243, 156, 18, 0.4);
}

.btn-danger {
  background: linear-gradient(135deg, #e74c3c, #c0392b);
  color: white;
}

.btn-danger:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(231, 76, 60, 0.4);
}

/* 表单样式 */
.form-group {
  margin-bottom: 20px;
}

.form-label {
  display: block;
  margin-bottom: 8px;
  font-weight: 600;
  color: #2c3e50;
}

.form-control {
  width: 100%;
  padding: 12px 16px;
  border: 2px solid #e9ecef;
  border-radius: 8px;
  font-size: 1em;
  transition: all 0.3s ease;
  background: white;
}

.form-control:focus {
  outline: none;
  border-color: #3498db;
  box-shadow: 0 0 0 3px rgba(52, 152, 219, 0.1);
}

/* 表格样式 */
.data-table {
  width: 100%;
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.data-table th {
  background: linear-gradient(135deg, #3498db, #2980b9);
  color: white;
  padding: 16px;
  text-align: left;
  font-weight: 600;
}

.data-table td {
  padding: 16px;
  border-bottom: 1px solid #f8f9fa;
}

.data-table tr:hover {
  background: #f8f9fa;
}

/* 统计卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 25px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  text-align: center;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

.stat-icon {
  font-size: 3em;
  margin-bottom: 15px;
}

.stat-value {
  font-size: 2.5em;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 5px;
}

.stat-label {
  color: #7f8c8d;
  font-size: 1em;
  font-weight: 500;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .header-content {
    padding: 0 20px;
  }

  .main-nav {
    gap: 6px;
    margin: 0 20px;
  }

  .main-nav a {
    padding: 8px 16px;
    font-size: 0.9em;
  }
}

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    height: auto;
    padding: 15px 20px;
    gap: 15px;
  }

  .header-left {
    order: 1;
  }

  .main-nav {
    order: 3;
    flex-wrap: wrap;
    justify-content: center;
    margin: 0;
    gap: 5px;
  }

  .user-info {
    order: 2;
    flex-direction: column;
    gap: 10px;
  }

  .page-container {
    padding: 20px 16px;
  }

  .page-header h1 {
    font-size: 2em;
  }

  .grid-2,
  .grid-3,
  .grid-4 {
    grid-template-columns: 1fr;
  }

  .stats-grid {
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  }
}

@media (max-width: 480px) {
  .card {
    padding: 20px;
    margin-bottom: 16px;
  }

  .stat-card {
    padding: 20px;
  }

  .stat-value {
    font-size: 2em;
  }

  .btn {
    padding: 10px 20px;
    font-size: 0.9em;
  }
}
</style>
