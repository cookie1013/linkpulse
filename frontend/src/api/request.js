import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '',
  timeout: 10000
})

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('linkpulse_token')

  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }

  return config
})

request.interceptors.response.use(
  (response) => {
    return response.data
  },
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('linkpulse_token')
      localStorage.removeItem('linkpulse_user')
      ElMessage.error('登录已失效，请重新登录')
      window.location.href = '/login'
      return Promise.reject(error)
    }

    const message =
      error.response?.data?.message ||
      error.message ||
      '请求失败'

    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default request