/* eslint-disable vue/multi-word-component-names */
<template>
  <div class="home-page">
    <div class="welcome-section">
      <div class="welcome-content">
        <h1 class="welcome-title">
          <span class="welcome-icon">🍽️</span>
          企业餐厅网络点餐系统
        </h1>
        <p class="welcome-subtitle" v-if="currentUser">
          欢迎回来，<strong>{{ currentUser.name }}</strong>（{{ roleLabel }}）
        </p>
        <p class="welcome-subtitle" v-else>
          请选择左侧菜单进入相应功能模块
        </p>
      </div>
    </div>

    <div class="quick-access-section" v-if="currentUser && quickAccessMenus.length > 0">
      <div class="section-header">
        <h2>快捷入口</h2>
        <p class="text-muted">快速访问常用功能</p>
      </div>
      <div class="quick-access-grid">
        <div
          v-for="item in quickAccessMenus"
          :key="item.path"
          class="quick-access-card"
          @click="$router.push(item.path)"
        >
          <div class="quick-access-icon">{{ item.icon }}</div>
          <h5 class="quick-access-title">{{ item.label }}</h5>
          <span class="quick-access-link">进入 &raquo;</span>
        </div>
      </div>
    </div>

    <div class="info-section" v-if="!currentUser">
      <div class="info-card">
        <div class="info-icon">📋</div>
        <h3>系统功能</h3>
        <p>请先登录以访问系统功能</p>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'HomePage',
  data() {
    return {
      currentUser: null
    }
  },
  computed: {
    roleLabel() {
      if (!this.currentUser || !this.currentUser.role) return '未分配角色'
      const map = {
        manager: '餐厅经理',
        kitchen_chief: '厨房主管',
        delivery_staff: '配餐员',
        finance: '财务人员',
        employee: '普通员工'
      }
      return map[this.currentUser.role] || this.currentUser.role
    },
    quickAccessMenus() {
      if (!this.currentUser) return []
      const role = this.currentUser.role
      
      if (role === 'manager') {
        return [
          { path: '/manager/dashboard', label: '经理控制台', icon: '📊' },
          { path: '/manager/menu-management', label: '菜单管理', icon: '🍽️' },
          { path: '/manager/user-management', label: '用户管理', icon: '👥' }
        ]
      }
      if (role === 'kitchen_chief') {
        return [
          { path: '/kitchen/dashboard', label: '厨房控制台', icon: '👨‍🍳' },
          { path: '/kitchen/blanket-order', label: '总括订单', icon: '📋' }
        ]
      }
      if (role === 'delivery_staff') {
        return [
          { path: '/delivery/dashboard', label: '配餐员控制台', icon: '🚚' }
        ]
      }
      if (role === 'finance') {
        return [
          { path: '/finance/dashboard', label: '财务控制台', icon: '💰' },
          { path: '/finance/monthly-sales-report', label: '月度销售统计', icon: '📈' }
        ]
      }
      // 默认视为普通员工
      return [
        { path: '/employee/menu', label: '今日菜单', icon: '🍽️' },
        { path: '/employee/new-order', label: '新建订单', icon: '➕' },
        { path: '/employee/order-list', label: '我的订单', icon: '📋' }
      ]
    }
  },
  created() {
    this.syncCurrentUser()
  },
  methods: {
    syncCurrentUser() {
      const userJson = localStorage.getItem('currentUser')
      if (userJson) {
        try {
          this.currentUser = JSON.parse(userJson)
        } catch (e) {
          this.currentUser = null
        }
      } else {
        this.currentUser = null
      }
    }
  },
  watch: {
    $route() {
      this.syncCurrentUser()
    }
  }
}
</script>

<style scoped>
.home-page {
  max-width: 1400px;
  margin: 0 auto;
  padding: 40px 32px;
}

.welcome-section {
  margin-bottom: 48px;
}

.welcome-content {
  text-align: center;
  padding: 48px 32px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  position: relative;
  overflow: hidden;
}

.welcome-content::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 6px;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
}

.welcome-title {
  font-size: 3rem;
  font-weight: 700;
  margin-bottom: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: 1px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
}

.welcome-icon {
  font-size: 3.5rem;
  filter: drop-shadow(0 4px 8px rgba(102, 126, 234, 0.3));
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-10px);
  }
}

.welcome-subtitle {
  font-size: 18px;
  color: #718096;
  line-height: 1.8;
  margin: 0;
}

.welcome-subtitle strong {
  color: #667eea;
  font-weight: 600;
}

.quick-access-section {
  margin-top: 48px;
}

.section-header {
  text-align: center;
  margin-bottom: 32px;
  padding-bottom: 24px;
  border-bottom: 2px solid rgba(102, 126, 234, 0.1);
}

.section-header h2 {
  font-size: 2rem;
  font-weight: 700;
  margin-bottom: 8px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.quick-access-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 24px;
  margin-top: 32px;
}

.quick-access-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  border: 2px solid rgba(102, 126, 234, 0.1);
  padding: 32px 24px;
  text-align: center;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.quick-access-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  transform: scaleX(0);
  transition: transform 0.4s ease;
}

.quick-access-card:hover::before {
  transform: scaleX(1);
}

.quick-access-card:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: 0 20px 40px rgba(102, 126, 234, 0.25);
  border-color: rgba(102, 126, 234, 0.3);
  background: rgba(255, 255, 255, 1);
}

.quick-access-icon {
  font-size: 4rem;
  margin-bottom: 20px;
  display: block;
  transition: transform 0.4s ease;
  filter: drop-shadow(0 4px 8px rgba(0, 0, 0, 0.1));
}

.quick-access-card:hover .quick-access-icon {
  transform: scale(1.15) rotate(5deg);
}

.quick-access-title {
  font-size: 1.4rem;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 12px;
  transition: color 0.3s ease;
}

.quick-access-card:hover .quick-access-title {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.quick-access-link {
  color: #667eea;
  font-size: 15px;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-top: 8px;
  transition: all 0.3s ease;
  padding: 8px 16px;
  border-radius: 20px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
}

.quick-access-card:hover .quick-access-link {
  color: #fff;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.info-section {
  margin-top: 48px;
  display: flex;
  justify-content: center;
}

.info-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  border: 2px solid rgba(102, 126, 234, 0.1);
  padding: 48px 32px;
  text-align: center;
  max-width: 500px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
}

.info-icon {
  font-size: 4rem;
  margin-bottom: 20px;
  display: block;
  filter: drop-shadow(0 4px 8px rgba(0, 0, 0, 0.1));
}

.info-card h3 {
  font-size: 1.5rem;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.info-card p {
  color: #718096;
  font-size: 16px;
  line-height: 1.6;
  margin: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .home-page {
    padding: 24px 16px;
  }

  .welcome-title {
    font-size: 2rem;
    flex-direction: column;
    gap: 12px;
  }

  .welcome-icon {
    font-size: 2.5rem;
  }

  .welcome-content {
    padding: 32px 20px;
  }

  .quick-access-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .section-header h2 {
    font-size: 1.5rem;
  }
}
</style>