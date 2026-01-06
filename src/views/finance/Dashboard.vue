<template>
  <div class="container mt-4 dashboard-page">
    <div class="page-header mb-4">
      <h2 class="mb-1">财务管理控制台</h2>
      <p class="text-muted mb-0">查看财务数据和统计报表</p>
    </div>

    <div class="row g-4">
      <div class="col-md-6 col-sm-6">
        <div class="card feature-card" @click="$router.push('/finance/monthly-sales-report')">
          <div class="card-body">
            <div class="feature-icon">📈</div>
            <h5 class="card-title">月度销售统计</h5>
            <p class="card-text">查看每月的销售统计数据和报表。</p>
            <span class="feature-link">进入 &raquo;</span>
          </div>
        </div>
      </div>

      <div class="col-md-6 col-sm-6">
        <div class="card feature-card" @click="$router.push('/finance/employee-order-summary')">
          <div class="card-body">
            <div class="feature-icon">👤</div>
            <h5 class="card-title">员工订单统计</h5>
            <p class="card-text">查看特定员工的月度订单汇总。</p>
            <span class="feature-link">进入 &raquo;</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'FinanceDashboard'
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


