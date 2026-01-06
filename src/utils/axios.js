import axios from 'axios'

// 创建axios实例
const instance = axios.create({
  baseURL: '',
  timeout: 10000,
  withCredentials: true // 重要：允许携带Cookie（Session ID）
})

// 请求拦截器
instance.interceptors.request.use(
  config => {
    // 可以在这里添加token等
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
instance.interceptors.response.use(
  response => {
    return response
  },
  error => {
    // 处理401未授权错误
    if (error.response && error.response.status === 401) {
      // 清除本地存储的用户信息
      localStorage.removeItem('currentUser')
      // 跳转到登录页
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  }
)

export default instance





