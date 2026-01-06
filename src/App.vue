<template>
  <div id="app">
    <!-- 登录页：不包裹全局布局，单独全屏显示 -->
    <div v-if="isLoginPage">
      <router-view />
    </div>

    <!-- 业务页：带顶部导航和侧边栏 -->
    <div v-else>
      <header v-if="!isPrintPage" class="app-header">
        <div class="logo" @click="$router.push('/login')">
          企业餐厅网络点餐系统
        </div>
        <div class="header-right">
          <span v-if="currentUser" class="user-info">
            你好，{{ currentUser.name }}（{{ roleLabel }}）
          </span>
          <button
            v-if="currentUser"
            class="btn btn-sm btn-outline-light ms-2"
            @click="logout"
          >
            退出
          </button>
        </div>
      </header>

      <div class="app-main">
        <aside class="app-sidebar" v-if="currentUser && !isPrintPage">
          <nav>
            <ul>
              <li v-for="item in sidebarMenus" :key="item.path">
                <router-link :to="item.path" class="sidebar-link">
                  {{ item.label }}
                </router-link>
              </li>
            </ul>
          </nav>
        </aside>

        <main class="app-content" :class="{ 'no-sidebar': !currentUser }">
          <router-view />
        </main>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'App',
  data() {
    return {
      currentUser: null
    }
  },
  computed: {
    isLoginPage() {
      return this.$route.path === '/login'
    },
    isPrintPage() {
      return this.$route.path.includes('/print-template')
    },
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
    sidebarMenus() {
      if (!this.currentUser) return []
      const role = this.currentUser.role
      if (role === 'manager') {
        return [
          { path: '/manager/dashboard', label: '经理控制台' },
          { path: '/manager/recipe-management', label: '食谱管理' },
          { path: '/manager/menu-management', label: '菜单管理' },
          { path: '/manager/employee-order-summary', label: '订单汇总' },
          { path: '/manager/sales-report', label: '销售报表' },
          { path: '/manager/user-management', label: '用户管理' },
          { path: '/manager/system-config', label: '系统配置' }
        ]
      }
      if (role === 'kitchen_chief') {
        return [
          { path: '/kitchen/dashboard', label: '厨房控制台' },
          { path: '/kitchen/blanket-order', label: '总括订单' },
          { path: '/manager/recipe-management', label: '食谱管理' }
        ]
      }
      if (role === 'delivery_staff') {
        return [
          { path: '/delivery/dashboard', label: '配餐员控制台' }
        ]
      }
      if (role === 'finance') {
        return [
          { path: '/finance/dashboard', label: '财务控制台' },
          { path: '/finance/monthly-sales-report', label: '月度销售统计' },
          { path: '/finance/employee-order-summary', label: '员工订单统计' }
        ]
      }
      // 默认视为普通员工
      return [
        { path: '/employee/menu', label: '今日菜单' },
        { path: '/employee/new-order', label: '新建订单' },
        { path: '/employee/order-list', label: '我的订单' },
        { path: '/employee/personal-order-summary', label: '个人月度统计' }
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
    },
    logout() {
      localStorage.removeItem('currentUser')
      this.currentUser = null
      this.$router.push('/login')
    }
  },
  watch: {
    // 登录成功后路由变化时，同步最新用户信息，避免必须刷新页面才能看到头部信息
    $route() {
      this.syncCurrentUser()
    }
  }
}
</script>

<style>
html,
body,
#app {
  height: 100%;
  margin: 0;
  padding: 0;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue',
    Arial, 'Noto Sans', sans-serif, 'Microsoft YaHei', 'PingFang SC', 'Hiragino Sans GB';
  background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%);
  background-attachment: fixed;
  color: #1a1a1a;
  line-height: 1.6;
}

.app-header {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.25);
  position: sticky;
  top: 0;
  z-index: 1000;
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
}

.app-header:hover {
  box-shadow: 0 6px 25px rgba(102, 126, 234, 0.35);
}

.app-header .logo {
  font-size: 20px;
  font-weight: 700;
  cursor: pointer;
  letter-spacing: 0.5px;
  transition: transform 0.2s ease;
  display: flex;
  align-items: center;
  gap: 8px;
}

.app-header .logo::before {
  content: '🍽️';
  font-size: 24px;
}

.app-header .logo:hover {
  transform: scale(1.05);
}

.header-right {
  display: flex;
  align-items: center;
  font-size: 14px;
  gap: 16px;
}

.user-info {
  opacity: 0.95;
  font-weight: 500;
  padding: 6px 12px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 20px;
  backdrop-filter: blur(10px);
}

.app-main {
  display: flex;
  height: calc(100% - 64px);
  background: transparent;
}

.app-sidebar {
  width: 240px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-right: 1px solid rgba(0, 0, 0, 0.06);
  padding: 24px 16px;
  box-sizing: border-box;
  box-shadow: 2px 0 10px rgba(0, 0, 0, 0.03);
  transition: all 0.3s ease;
}

.app-sidebar ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.app-sidebar li + li {
  margin-top: 4px;
}

.sidebar-link {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  border-radius: 10px;
  color: #4a5568;
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s ease;
  position: relative;
  overflow: hidden;
}

.sidebar-link::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  width: 3px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  transform: scaleY(0);
  transition: transform 0.2s ease;
}

.sidebar-link:hover {
  background: linear-gradient(90deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.05) 100%);
  color: #667eea;
  transform: translateX(4px);
}

.sidebar-link:hover::before {
  transform: scaleY(1);
}

.sidebar-link.router-link-active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
  font-weight: 600;
}

.sidebar-link.router-link-active::before {
  transform: scaleY(1);
  background: rgba(255, 255, 255, 0.3);
}

.app-content {
  flex: 1;
  padding: 32px;
  overflow-y: auto;
  background: transparent;
}

.app-content.no-sidebar {
  padding-top: 32px;
}

/* 统一按钮美化 - 现代化设计 */
.btn {
  border-radius: 8px;
  padding: 10px 20px;
  font-size: 14px;
  font-weight: 600;
  border-width: 1px;
  border-style: solid;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  letter-spacing: 0.3px;
}

.btn::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  transform: translate(-50%, -50%);
  transition: width 0.6s, height 0.6s;
}

.btn:hover::before {
  width: 300px;
  height: 300px;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-color: transparent;
  color: #fff;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.btn-primary:hover:not(:disabled) {
  background: linear-gradient(135deg, #764ba2 0%, #667eea 100%);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
  transform: translateY(-2px);
}

.btn-primary:active:not(:disabled) {
  transform: translateY(0);
  box-shadow: 0 2px 10px rgba(102, 126, 234, 0.3);
}

.btn-success {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
  border-color: transparent;
  color: #fff;
  box-shadow: 0 4px 15px rgba(17, 153, 142, 0.3);
}

.btn-success:hover:not(:disabled) {
  background: linear-gradient(135deg, #38ef7d 0%, #11998e 100%);
  box-shadow: 0 6px 20px rgba(17, 153, 142, 0.4);
  transform: translateY(-2px);
}

.btn-danger {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  border-color: transparent;
  color: #fff;
  box-shadow: 0 4px 15px rgba(245, 87, 108, 0.3);
}

.btn-danger:hover:not(:disabled) {
  background: linear-gradient(135deg, #f5576c 0%, #f093fb 100%);
  box-shadow: 0 6px 20px rgba(245, 87, 108, 0.4);
  transform: translateY(-2px);
}

.btn-info {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  border-color: transparent;
  color: #fff;
  box-shadow: 0 4px 15px rgba(79, 172, 254, 0.3);
}

.btn-info:hover:not(:disabled) {
  background: linear-gradient(135deg, #00f2fe 0%, #4facfe 100%);
  box-shadow: 0 6px 20px rgba(79, 172, 254, 0.4);
  transform: translateY(-2px);
}

.btn-outline-primary {
  border: 2px solid #667eea;
  color: #667eea;
  background: transparent;
  border-radius: 8px;
}

.btn-outline-primary:hover:not(:disabled) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border-color: transparent;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.btn-outline-success {
  border: 2px solid #11998e;
  color: #11998e;
  background: transparent;
  border-radius: 8px;
}

.btn-outline-success:hover:not(:disabled) {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
  color: #fff;
  border-color: transparent;
  box-shadow: 0 4px 15px rgba(17, 153, 142, 0.3);
}

.btn-outline-light {
  border: 2px solid rgba(255, 255, 255, 0.8);
  color: #fff;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
}

.btn-outline-light:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.25);
  border-color: rgba(255, 255, 255, 1);
  transform: translateY(-2px);
}

.btn-secondary {
  background: #6c757d;
  border-color: #6c757d;
  color: #fff;
}

.btn-secondary:hover:not(:disabled) {
  background: #5a6268;
  border-color: #5a6268;
  box-shadow: 0 4px 15px rgba(108, 117, 125, 0.3);
  transform: translateY(-2px);
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none !important;
}

.btn-sm {
  padding: 6px 14px;
  font-size: 13px;
}

/* 现代化表单元素 */
.form-control, .form-select, input[type="text"], input[type="date"], input[type="month"], 
input[type="password"], input[type="number"], select, textarea {
  border-radius: 8px;
  padding: 10px 16px;
  border: 2px solid #e2e8f0;
  background-color: #ffffff;
  font-size: 14px;
  transition: all 0.2s ease;
  color: #1a1a1a;
}

.form-control:focus, .form-select:focus, input:focus, select:focus, textarea:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
  outline: none;
  background-color: #ffffff;
}

.form-control:hover, .form-select:hover, input:hover, select:hover {
  border-color: #cbd5e0;
}

/* 圆角输入框统一样式（选择员工 / 选择日期 / 选择月份等） */
.nice-input {
  border-radius: 8px;
  padding-inline: 16px;
  border: 2px solid #667eea;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
  transition: all 0.2s ease;
}

.nice-input:focus {
  border-color: #764ba2;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.15);
  outline: none;
  background: #ffffff;
}

/* 统一弹窗现代化设计 */
.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1050;
  animation: fadeIn 0.2s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.modal-dialog {
  max-width: 520px;
  width: 100%;
  margin: 0 16px;
  animation: slideUp 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.modal-content {
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  padding: 24px;
  border: none;
}

.modal-header {
  padding-bottom: 16px;
  border-bottom: 1px solid #e2e8f0;
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-title {
  font-size: 20px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0;
}

.btn-close {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #718096;
  padding: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  transition: all 0.2s ease;
}

.btn-close:hover {
  background: #f7fafc;
  color: #1a1a1a;
}

.modal-footer {
  border-top: 1px solid #e2e8f0;
  padding-top: 20px;
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 现代化卡片样式 */
.card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 16px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  overflow: hidden;
}

.card:hover {
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

.card-header {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  padding: 16px 20px;
  font-weight: 600;
  color: #1a1a1a;
}

.card-body {
  padding: 20px;
}

/* 现代化表格样式 */
.table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
  margin: 0;
}

.table thead th {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.08) 0%, rgba(118, 75, 162, 0.08) 100%);
  color: #1a1a1a;
  font-weight: 600;
  padding: 16px;
  border-bottom: 2px solid rgba(102, 126, 234, 0.1);
  text-align: left;
  font-size: 14px;
  position: sticky;
  top: 0;
  z-index: 10;
}

.table tbody td {
  padding: 16px;
  border-bottom: 1px solid #f1f5f9;
  color: #4a5568;
  font-size: 14px;
  vertical-align: middle;
}

.table tbody tr {
  transition: all 0.2s ease;
}

.table tbody tr:hover {
  background: linear-gradient(90deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
  transform: scale(1.005);
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.1);
}

.table-striped tbody tr:nth-of-type(odd) {
  background: rgba(102, 126, 234, 0.02);
}

.table-striped tbody tr:nth-of-type(odd):hover {
  background: linear-gradient(90deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
}

.table-responsive {
  border-radius: 12px;
  overflow: hidden;
}

/* 现代化警告框 */
.alert {
  padding: 14px 18px;
  border-radius: 12px;
  border: none;
  font-size: 14px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.alert-info {
  background: linear-gradient(135deg, rgba(79, 172, 254, 0.1) 0%, rgba(0, 242, 254, 0.1) 100%);
  color: #0c5460;
  border-left: 4px solid #4facfe;
}

.alert-success {
  background: linear-gradient(135deg, rgba(17, 153, 142, 0.1) 0%, rgba(56, 239, 125, 0.1) 100%);
  color: #155724;
  border-left: 4px solid #11998e;
}

.alert-warning {
  background: linear-gradient(135deg, rgba(255, 193, 7, 0.1) 0%, rgba(255, 152, 0, 0.1) 100%);
  color: #856404;
  border-left: 4px solid #ffc107;
}

.alert-danger {
  background: linear-gradient(135deg, rgba(245, 87, 108, 0.1) 0%, rgba(240, 147, 251, 0.1) 100%);
  color: #721c24;
  border-left: 4px solid #f5576c;
}

/* 标签样式 */
.badge {
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  display: inline-block;
}

.bg-success {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
  color: #fff;
}

.bg-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

/* 统一页面标题样式 */
.page-header {
  margin-bottom: 32px;
  padding-bottom: 24px;
  border-bottom: 2px solid rgba(102, 126, 234, 0.1);
}

.page-header h1, .page-header h2 {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  font-weight: 700;
  margin-bottom: 8px;
  font-size: 2.5rem;
}

.page-header h2 {
  font-size: 2rem;
}

.page-header p.text-muted {
  font-size: 16px;
  color: #718096;
  margin: 0;
}

.text-muted {
  color: #718096 !important;
}

/* 统一容器样式 */
.container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 32px;
}

/* 统一卡片样式增强 */
.card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  overflow: hidden;
}

.card:hover {
  box-shadow: 0 12px 48px rgba(102, 126, 234, 0.12);
  transform: translateY(-2px);
}

.card-header {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.08) 0%, rgba(118, 75, 162, 0.08) 100%);
  border-bottom: 2px solid rgba(102, 126, 234, 0.1);
  padding: 20px 24px;
  font-weight: 600;
  color: #1a1a1a;
}

.card-body {
  padding: 24px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .app-header {
    padding: 0 16px;
    height: 56px;
  }

  .app-header .logo {
    font-size: 16px;
  }

  .app-sidebar {
    width: 200px;
    padding: 16px 12px;
  }

  .app-content {
    padding: 16px;
  }

  .container {
    padding: 16px;
  }

  .page-header {
    margin-bottom: 24px;
    padding-bottom: 20px;
  }

  .page-header h1 {
    font-size: 2rem;
  }

  .page-header h2 {
    font-size: 1.75rem;
  }

  .card {
    border-radius: 16px;
  }

  .card-header {
    padding: 16px 20px;
  }

  .card-body {
    padding: 20px;
  }

  .table thead th,
  .table tbody td {
    padding: 12px;
    font-size: 13px;
  }

  .modal-dialog {
    margin: 0 12px;
  }

  .modal-content {
    padding: 20px;
  }

  .btn {
    padding: 10px 16px;
    font-size: 14px;
  }
}

@media (max-width: 480px) {
  .app-header {
    padding: 0 12px;
    height: 52px;
  }

  .app-header .logo {
    font-size: 14px;
  }

  .app-sidebar {
    width: 180px;
    padding: 12px 8px;
  }

  .sidebar-link {
    padding: 10px 12px;
    font-size: 13px;
  }

  .page-header h1 {
    font-size: 1.75rem;
  }

  .page-header h2 {
    font-size: 1.5rem;
  }

  .table {
    font-size: 12px;
  }

  .table thead th,
  .table tbody td {
    padding: 10px 8px;
  }

  .card-header {
    padding: 12px 16px;
  }

  .card-body {
    padding: 16px;
  }
}

/* 滚动条美化 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.05);
  border-radius: 10px;
}

::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 10px;
}

::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(135deg, #764ba2 0%, #667eea 100%);
}

/* 加载动画 */
@keyframes shimmer {
  0% {
    background-position: -1000px 0;
  }
  100% {
    background-position: 1000px 0;
  }
}

.loading-shimmer {
  animation: shimmer 2s infinite;
  background: linear-gradient(
    to right,
    #f6f7f8 0%,
    #edeef1 20%,
    #f6f7f8 40%,
    #f6f7f8 100%
  );
  background-size: 1000px 100%;
}

/* 工具类 */
.mt-1 { margin-top: 0.25rem; }
.mt-2 { margin-top: 0.5rem; }
.mt-3 { margin-top: 1rem; }
.mt-4 { margin-top: 1.5rem; }
.mb-0 { margin-bottom: 0; }
.mb-1 { margin-bottom: 0.25rem; }
.mb-2 { margin-bottom: 0.5rem; }
.mb-3 { margin-bottom: 1rem; }
.mb-4 { margin-bottom: 1.5rem; }
.me-1 { margin-right: 0.25rem; }
.me-2 { margin-right: 0.5rem; }
.ms-2 { margin-left: 0.5rem; }
.gap-2 { gap: 0.5rem; }
.d-flex { display: flex; }
.justify-content-between { justify-content: space-between; }
.justify-content-center { justify-content: center; }
.align-items-center { align-items: center; }
.text-center { text-align: center; }
.text-start { text-align: left; }
.d-inline-block { display: inline-block; }
.flex-wrap { flex-wrap: wrap; }

/* 全局打印样式 */
@media print {
  /* 首先确保 body 和 html 可见 */
  html,
  body {
    background-color: #fff !important;
    margin: 0 !important;
    padding: 0 !important;
    width: 100% !important;
    height: auto !important;
    overflow: visible !important;
    display: block !important;
    visibility: visible !important;
  }
  
  /* 确保所有内容默认可见 */
  * {
    visibility: visible !important;
  }
  
  /* 隐藏导航和操作按钮 */
  .app-header,
  .app-sidebar,
  .btn,
  .page-header,
  button:not(.print-show) {
    display: none !important;
  }

  /* 隐藏表单元素，但保留报表card */
  .form-control,
  .form-select,
  .form-label,
  .form,
  input[type="month"],
  input[type="date"],
  select {
    display: none !important;
  }
  
  /* 隐藏包含表单的card（查询表单），但保留报表card */
  .card:not(.summary-report):not(.sales-report):not(.employee-order-report):not(.print-container) {
    /* 检查是否只包含表单元素，如果是则隐藏 */
    /* 由于CSS限制，我们通过JavaScript来处理，这里先不隐藏 */
  }

  /* 隐藏非报表内容的alert（错误提示等），但保留报表内的alert */
  .alert:not(.print-show):not(.summary-report .alert):not(.sales-report .alert):not(.employee-order-report .alert):not(.print-container .alert) {
    display: none !important;
  }

  /* 确保容器可见 */
  .app-content,
  .app-main,
  .container {
    display: block !important;
    visibility: visible !important;
    padding: 0 !important;
    margin: 0 !important;
    width: 100% !important;
    max-width: 100% !important;
    background: white !important;
  }
  
  html * {
    max-height: none !important;
    max-width: none !important;
  }

  /* 确保所有报表内容可见 */
  .summary-report,
  .sales-report,
  .employee-order-report,
  .print-container {
    display: block !important;
    visibility: visible !important;
    position: relative !important;
    opacity: 1 !important;
    width: 100% !important;
    max-width: 100% !important;
  }

  /* 确保报表内的所有元素可见 */
  .summary-report *,
  .sales-report *,
  .employee-order-report *,
  .print-container * {
    visibility: visible !important;
    opacity: 1 !important;
    color: #000 !important;
  }
  
  /* 特别处理可能被隐藏的元素 */
  .summary-report [style*="display: none"],
  .sales-report [style*="display: none"],
  .employee-order-report [style*="display: none"],
  .print-container [style*="display: none"] {
    display: block !important;
  }
  
  .summary-report [style*="visibility: hidden"],
  .sales-report [style*="visibility: hidden"],
  .employee-order-report [style*="visibility: hidden"],
  .print-container [style*="visibility: hidden"] {
    visibility: visible !important;
  }
  
  /* 强制显示表格相关元素 */
  .summary-report table,
  .sales-report table,
  .employee-order-report table,
  .print-container table {
    display: table !important;
    visibility: visible !important;
    width: 100% !important;
    border-collapse: collapse !important;
  }
  
  .summary-report thead,
  .sales-report thead,
  .employee-order-report thead,
  .print-container thead {
    display: table-header-group !important;
    visibility: visible !important;
  }
  
  .summary-report tbody,
  .sales-report tbody,
  .employee-order-report tbody,
  .print-container tbody {
    display: table-row-group !important;
    visibility: visible !important;
  }
  
  .summary-report tr,
  .sales-report tr,
  .employee-order-report tr,
  .print-container tr {
    display: table-row !important;
    visibility: visible !important;
  }
  
  .summary-report td,
  .summary-report th,
  .sales-report td,
  .sales-report th,
  .employee-order-report td,
  .employee-order-report th,
  .print-container td,
  .print-container th {
    display: table-cell !important;
    visibility: visible !important;
    border: 1px solid #333 !important;
    padding: 8px !important;
  }

  /* 确保报表内的row和col可见 */
  .summary-report .row,
  .sales-report .row,
  .employee-order-report .row,
  .print-container .row {
    display: flex !important;
    visibility: visible !important;
  }

  .summary-report .col-md-6,
  .sales-report .col-md-6,
  .employee-order-report .col-md-6,
  .print-container .col-md-6 {
    display: block !important;
    visibility: visible !important;
  }

  /* 确保表格在打印时可见 */
  .summary-report table,
  .sales-report table,
  .employee-order-report table,
  .print-container table {
    display: table !important;
    visibility: visible !important;
    width: 100% !important;
  }

  .summary-report thead,
  .sales-report thead,
  .employee-order-report thead,
  .print-container thead {
    display: table-header-group !important;
    visibility: visible !important;
  }

  .summary-report tbody,
  .sales-report tbody,
  .employee-order-report tbody,
  .print-container tbody {
    display: table-row-group !important;
    visibility: visible !important;
  }

  .summary-report tfoot,
  .sales-report tfoot,
  .employee-order-report tfoot,
  .print-container tfoot {
    display: table-footer-group !important;
    visibility: visible !important;
  }

  .summary-report tr,
  .sales-report tr,
  .employee-order-report tr,
  .print-container tr {
    display: table-row !important;
    visibility: visible !important;
  }

  .summary-report td,
  .summary-report th,
  .sales-report td,
  .sales-report th,
  .employee-order-report td,
  .employee-order-report th,
  .print-container td,
  .print-container th {
    display: table-cell !important;
    visibility: visible !important;
  }

  /* 确保文本内容可见 */
  .summary-report p,
  .summary-report h3,
  .summary-report h5,
  .sales-report p,
  .sales-report h3,
  .sales-report h5,
  .employee-order-report p,
  .employee-order-report h3,
  .employee-order-report h5,
  .print-container p,
  .print-container h3,
  .print-container h5 {
    display: block !important;
    visibility: visible !important;
    color: #000 !important;
  }
  
  /* 确保所有文本内容都有颜色 */
  .summary-report *,
  .sales-report *,
  .employee-order-report *,
  .print-container * {
    color: #000 !important;
  }
  
  /* 确保所有span和文本元素可见 */
  .summary-report span,
  .sales-report span,
  .employee-order-report span,
  .print-container span {
    display: inline !important;
    visibility: visible !important;
    color: #000 !important;
  }
}
</style>
