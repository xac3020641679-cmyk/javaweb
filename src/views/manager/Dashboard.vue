<template>
  <div class="container mt-4 dashboard-page">
    <div class="page-header mb-4">
      <h2 class="mb-1">餐厅经理控制台</h2>
      <p class="text-muted mb-0">快速导航到常用管理功能</p>
    </div>

    <div class="row g-4">
      <div class="col-md-4 col-sm-6">
        <div class="card feature-card" @click="$router.push('/manager/recipe-management')">
          <div class="card-body">
            <div class="feature-icon">📋</div>
            <h5 class="card-title">食谱管理</h5>
            <p class="card-text">查看和管理餐厅食谱。</p>
            <span class="feature-link">进入 &raquo;</span>
          </div>
        </div>
      </div>

      <div class="col-md-4 col-sm-6">
        <div class="card feature-card" @click="$router.push('/manager/menu-management')">
          <div class="card-body">
            <div class="feature-icon">🍽️</div>
            <h5 class="card-title">菜单管理</h5>
            <p class="card-text">管理每日菜单及菜品。</p>
            <span class="feature-link">进入 &raquo;</span>
          </div>
        </div>
      </div>

      <div class="col-md-4 col-sm-6">
        <div class="card feature-card" @click="$router.push('/manager/employee-order-summary')">
          <div class="card-body">
            <div class="feature-icon">📊</div>
            <h5 class="card-title">订单汇总</h5>
            <p class="card-text">按员工查看月度订单统计。</p>
            <span class="feature-link">进入 &raquo;</span>
          </div>
        </div>
      </div>

      <div class="col-md-4 col-sm-6">
        <div class="card feature-card" @click="$router.push('/manager/sales-report')">
          <div class="card-body">
            <div class="feature-icon">💰</div>
            <h5 class="card-title">销售报表</h5>
            <p class="card-text">查看每月销售汇总报表。</p>
            <span class="feature-link">进入 &raquo;</span>
          </div>
        </div>
      </div>

      <div class="col-md-4 col-sm-6">
        <div class="card feature-card" @click="$router.push('/manager/user-management')">
          <div class="card-body">
            <div class="feature-icon">👥</div>
            <h5 class="card-title">用户管理</h5>
            <p class="card-text">管理系统用户和角色。</p>
            <span class="feature-link">进入 &raquo;</span>
          </div>
        </div>
      </div>

      <div class="col-md-4 col-sm-6">
        <div class="card feature-card" @click="$router.push('/manager/system-config')">
          <div class="card-body">
            <div class="feature-icon">⚙️</div>
            <h5 class="card-title">系统配置</h5>
            <p class="card-text">设置订餐截止时间等系统参数。</p>
            <span class="feature-link">进入 &raquo;</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ManagerDashboard'
}
</script>

<style scoped>
.dashboard-page {
  max-width: 1400px;
  margin: 0 auto;
}

.dashboard-page .page-header {
  text-align: center;
  padding-bottom: 24px;
  margin-bottom: 32px;
  border-bottom: 2px solid rgba(102, 126, 234, 0.1);
}

.dashboard-page .page-header h2 {
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: 8px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.dashboard-page .page-header p {
  color: #718096;
  font-size: 16px;
}

.dashboard-page .feature-card {
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border: 2px solid rgba(102, 126, 234, 0.1);
  border-radius: 20px;
  height: 100%;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  overflow: hidden;
  position: relative;
}

.dashboard-page .feature-card::before {
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

.dashboard-page .feature-card:hover::before {
  transform: scaleX(1);
}

.dashboard-page .feature-card:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: 0 20px 40px rgba(102, 126, 234, 0.25);
  border-color: rgba(102, 126, 234, 0.3);
  background: rgba(255, 255, 255, 1);
}

.dashboard-page .feature-card .card-body {
  padding: 32px 24px;
  text-align: center;
  position: relative;
  z-index: 1;
}

.dashboard-page .feature-icon {
  font-size: 4rem;
  margin-bottom: 20px;
  display: block;
  transition: transform 0.4s ease;
  filter: drop-shadow(0 4px 8px rgba(0, 0, 0, 0.1));
}

.dashboard-page .feature-card:hover .feature-icon {
  transform: scale(1.15) rotate(5deg);
}

.dashboard-page .feature-card .card-title {
  font-size: 1.4rem;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 12px;
  transition: color 0.3s ease;
}

.dashboard-page .feature-card:hover .card-title {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.dashboard-page .feature-card .card-text {
  color: #718096;
  font-size: 15px;
  margin-bottom: 20px;
  min-height: 3rem;
  line-height: 1.6;
}

.dashboard-page .feature-link {
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

.dashboard-page .feature-card:hover .feature-link {
  color: #fff;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}
</style>


