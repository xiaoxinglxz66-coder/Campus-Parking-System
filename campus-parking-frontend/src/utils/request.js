import axios from 'axios'
import { auth } from './auth'

// 创建axios实例
const service = axios.create({
  baseURL: 'http://localhost:8081', // 端口需与后端一致
  timeout: 10000
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    const token = auth.getToken()
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    // 处理401未授权
    if (error.response?.status === 401) {
      auth.logout()
      return Promise.reject({
        success: false,
        message: '登录已过期，请重新登录',
        data: null
      })
    }

    // 处理其他错误
    let message = '请求失败'
    if (error.response) {
      const errorData = error.response.data
      message = errorData?.message ||
        (error.response.status === 403 ? '权限不足' :
          error.response.status === 404 ? '接口不存在' :
            error.response.status === 500 ? '服务器内部错误' :
              `网络错误: ${error.response.status}`)
    } else if (error.request) {
      message = '网络连接失败'
    }

    return Promise.reject({
      success: false,
      message: message,
      data: null
    })
  }
)

export default service
