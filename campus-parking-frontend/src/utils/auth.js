// src/utils/auth.js

// 认证工具函数
export const auth = {
  // 检查是否已登录
  isLoggedIn() {
    const token = this.getToken()
    if (!token) return false

    // 验证token格式
    return this.validateTokenFormat(token)
  },

  // 获取token
  getToken() {
    const token = localStorage.getItem('token')
    // 清理可能的多余引号或空格
    if (token) {
      return token.trim().replace(/^["']|["']$/g, '')
    }
    return null
  },

  // 验证token格式
  validateTokenFormat(token) {
    if (!token || typeof token !== 'string') {
      console.warn('❌ Token为空或不是字符串')
      return false
    }

    // 检查JWT格式：应该有2个点号（3部分）
    const parts = token.split('.')
    if (parts.length !== 3) {
      console.error('❌ Token格式错误，应该有3部分，实际有:', parts.length)
      console.error('Token预览:', token.substring(0, 50) + (token.length > 50 ? '...' : ''))
      return false
    }

    // 检查各部分非空
    if (!parts[0] || !parts[1] || !parts[2]) {
      console.error('❌ Token部分为空')
      return false
    }

    console.log('✅ Token格式验证通过')
    return true
  },

  // 获取用户信息
  getUserInfo() {
    return {
      username: localStorage.getItem('username'),
      role: localStorage.getItem('role')
    }
  },

  // 检查用户角色
  hasRole(requiredRole) {
    const userRole = localStorage.getItem('role')
    if (!userRole) return false

    // 管理员拥有所有权限
    if (userRole === 'ROLE_ADMIN') return true

    // 检查具体角色
    return userRole === requiredRole
  },

  // 检查是否有任意一个角色
  hasAnyRole(roles) {
    const userRole = localStorage.getItem('role')
    if (!userRole) return false

    if (userRole === 'ROLE_ADMIN') return true

    return roles.includes(userRole)
  },

  // 退出登录
  logout() {
    localStorage.removeItem('token')
    localStorage.removeItem('username')
    localStorage.removeItem('role')
    window.location.href = '/login'
  },

  // 设置登录信息（在登录成功后调用）
  setLoginInfo(token, username, role) {
    // 清理并存储token
    const cleanToken = token.trim().replace(/^["']|["']$/g, '')
    localStorage.setItem('token', cleanToken)
    localStorage.setItem('username', username)
    localStorage.setItem('role', role)

    console.log('✅ 登录信息已保存')
    console.log('   Token长度:', cleanToken.length)
    console.log('   Token格式验证:', this.validateTokenFormat(cleanToken))
  }
}

// 请求拦截器 - 自动添加token
export const authFetch = async (url, options = {}) => {
  const token = auth.getToken()

  console.log('🔐 authFetch调试信息:')
  console.log('   请求URL:', url)
  console.log('   当前token:', token ? `有(${token.length}字符)` : '无')
  console.log('   Token格式:', token && auth.validateTokenFormat(token) ? '正确' : '错误')
  console.log('   用户登录状态:', auth.isLoggedIn())

  // 如果token无效，清理并重定向
  if (token && !auth.validateTokenFormat(token)) {
    console.error('❌ Token格式错误，清除并跳转到登录页')
    auth.logout()
    throw new Error('Token格式错误，请重新登录')
  }

  // 1. 判断是否为 FormData
  const isFormData = options.body && options.body instanceof FormData

  // 2. 构建配置
  const config = {
    ...options,
    headers: {
      // 关键：只在不是 FormData 时设置默认 Content-Type
      ...(!isFormData && { 'Content-Type': 'application/json' }),
      ...options.headers,
    }
  }

  if (token && auth.validateTokenFormat(token)) {
    config.headers['Authorization'] = `Bearer ${token}`
    console.log('   添加Authorization头')
  } else {
    console.warn('⚠️ 没有有效的token，请求可能被拒绝')
  }

  try {
    console.log(`🔍 发送API请求: ${url}`)
    console.log('   配置:', {
      method: config.method || 'GET',
      headers: config.headers,
      hasToken: !!(token && auth.validateTokenFormat(token))
    })

    const response = await fetch(url, config)

    console.log(`🔍 API响应: ${url}`, {
      status: response.status,
      statusText: response.statusText,
      ok: response.ok
    })

    // 如果token过期或无效，强制退出登录
    if (response.status === 401) {
      console.error('❌ 401 Unauthorized - Token过期或无效')
      auth.logout()
      throw new Error('认证失败，请重新登录')
    }

    // 检查403禁止访问
    if (response.status === 403) {
      console.error('❌ 403 Forbidden - 权限不足')
      console.error('   当前用户角色:', localStorage.getItem('role'))
      console.error('   请求路径:', url)
      throw new Error('权限不足，无法访问该资源')
    }

    // 检查其他错误状态
    if (!response.ok) {
      let errorMessage = `请求失败: ${response.status} ${response.statusText}`

      try {
        const errorText = await response.text()
        if (errorText) {
          errorMessage += ` - ${errorText}`
          console.error('   错误详情:', errorText)
        }
      } catch (e) {
        // 忽略解析错误
      }

      throw new Error(errorMessage)
    }

    return response
  } catch (error) {
    console.error('❌ API请求错误:')
    console.error('   错误类型:', error.constructor.name)
    console.error('   错误消息:', error.message)
    throw error
  }
}

// 工具函数：检查并重定向
export const checkAuthAndRedirect = () => {
  if (!auth.isLoggedIn()) {
    console.warn('用户未登录，跳转到登录页')
    window.location.href = '/login'
    return false
  }
  return true
}
