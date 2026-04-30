import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'

// 可选：按需导入 Element Plus 组件，避免影响其他页面
// 如果所有页面都需要 Element Plus，可以保留全局导入
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

// 🔥 新增：用户信息初始化函数
function initializeUserInfo() {
  console.log('=== 用户信息初始化开始 ===')

  const token = localStorage.getItem('token')
  console.log('Token存在:', !!token)

  if (token) {
    try {
      // 解析 JWT token
      const tokenParts = token.split('.')
      if (tokenParts.length === 3) {
        const payload = JSON.parse(atob(tokenParts[1]))
        console.log('JWT Payload:', payload)

        // 确保用户ID存在
        if (payload.userId && !localStorage.getItem('userId')) {
          localStorage.setItem('userId', payload.userId.toString())
          console.log('✅ 保存用户ID:', payload.userId)
        }

        // 确保用户角色存在
        if (payload.role && !localStorage.getItem('role')) {
          localStorage.setItem('role', payload.role)
          console.log('✅ 保存用户角色:', payload.role)
        }

        // 确保用户名存在
        if (payload.sub && !localStorage.getItem('username')) {
          localStorage.setItem('username', payload.sub)
          console.log('✅ 保存用户名:', payload.sub)
        }
      }
    } catch (e) {
      console.error('解析token失败:', e)
    }
  } else {
    console.log('⚠️ 未找到token，用户未登录')
  }

  console.log('当前localStorage内容:')
  for (let i = 0; i < localStorage.length; i++) {
    const key = localStorage.key(i)
    console.log(`${key}: ${localStorage.getItem(key)}`)
  }

  console.log('=== 用户信息初始化结束 ===')
}

const app = createApp(App)
const pinia = createPinia()

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 按正确顺序使用插件
app.use(pinia)        // 先使用 Pinia
app.use(router)       // 再使用路由
app.use(ElementPlus)  // 最后使用 Element Plus

// 🔥 在挂载前初始化用户信息
initializeUserInfo()

// 只挂载一次！
app.mount('#app')

console.log('Vue 应用启动成功')
