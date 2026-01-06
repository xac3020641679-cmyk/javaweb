0import axios from 'axios'

/**
 * 获取当前登录用户
 * 优先从Session获取，如果失败则从localStorage获取（作为fallback）
 */
export async function getCurrentUser() {
  try {
    const res = await axios.get('/api/auth/current', {
      withCredentials: true
    })
    if (res.data && res.data.id) {
      // 同步到localStorage作为缓存
      localStorage.setItem('currentUser', JSON.stringify(res.data))
      return res.data
    }
  } catch (e) {
    // Session无效，尝试从localStorage获取（可能已过期）
    const userJson = localStorage.getItem('currentUser')
    if (userJson) {
      try {
        return JSON.parse(userJson)
      } catch (parseError) {
        localStorage.removeItem('currentUser')
      }
    }
  }
  return null
}

/**
 * 同步获取当前用户（从localStorage，用于快速显示）
 * 注意：这可能不是最新的，应该优先使用getCurrentUser
 */
export function getCurrentUserSync() {
  const userJson = localStorage.getItem('currentUser')
  if (userJson) {
    try {
      return JSON.parse(userJson)
    } catch (e) {
      localStorage.removeItem('currentUser')
    }
  }
  return null
}





