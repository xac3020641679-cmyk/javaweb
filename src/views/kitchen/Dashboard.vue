<template>
  <div class="container mt-4 dashboard-page">
    <div class="page-header mb-4">
      <h2 class="mb-1">厨房主管控制台</h2>
      <p class="text-muted mb-0">围绕出菜与备料的核心功能入口</p>
    </div>

    <div class="row g-4">
      <div class="col-md-6 col-sm-6">
        <div class="card feature-card" @click="$router.push('/kitchen/blanket-order')">
          <div class="card-body">
            <div class="feature-icon">📦</div>
            <h5 class="card-title">总括订单</h5>
            <p class="card-text">生成和查看当日菜品汇总，用于食材采购和备菜准备。</p>
            <span class="feature-link">进入 &raquo;</span>
          </div>
        </div>
      </div>

      <div class="col-md-6 col-sm-6">
        <div class="card feature-card" @click="$router.push('/manager/recipe-management')">
          <div class="card-body">
            <div class="feature-icon">📋</div>
            <h5 class="card-title">食谱管理</h5>
            <p class="card-text">维护菜品基础信息，为菜单与总括订单提供来源。</p>
            <span class="feature-link">进入 &raquo;</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'KitchenDashboard'
}
</script>

<style scoped>
.dashboard-page {
  max-width: 1200px;
}

.dashboard-page .page-header {
  text-align: center;
  padding-bottom: 1rem;
  border-bottom: 2px solid #e9ecef;
}

.dashboard-page .page-header h2 {
  font-size: 2rem;
  font-weight: 600;
  color: #212529;
}

.dashboard-page .feature-card {
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid #dee2e6;
  border-radius: 8px;
  height: 100%;
  background: #fff;
}

.dashboard-page .feature-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.15);
  border-color: #007bff;
}

.dashboard-page .feature-card .card-body {
  padding: 1.5rem;
  text-align: center;
}

.dashboard-page .feature-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
  display: block;
}

.dashboard-page .feature-card .card-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: #212529;
  margin-bottom: 0.75rem;
}

.dashboard-page .feature-card .card-text {
  color: #6c757d;
  font-size: 0.95rem;
  margin-bottom: 1rem;
  min-height: 2.5rem;
}

.dashboard-page .feature-link {
  color: #007bff;
  font-size: 0.95rem;
  font-weight: 500;
  display: inline-block;
  margin-top: 0.5rem;
}

.dashboard-page .feature-card:hover .feature-link {
  color: #0056b3;
}
</style>


