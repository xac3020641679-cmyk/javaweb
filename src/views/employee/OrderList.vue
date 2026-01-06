<template>
  <div class="order-list-page">
    <div class="page-header">
      <h1>我的订单</h1>
      <p class="text-muted">查看历史订单并进入详情</p>
    </div>

    <div v-if="message" class="alert" :class="messageClass">
      {{ message }}
    </div>

    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p>加载中...</p>
    </div>

    <div v-else>
      <div v-if="orders.length === 0" class="empty-state">
        <div class="empty-icon">📋</div>
        <h3>暂无订单信息</h3>
        <p>您还没有创建任何订单。</p>
        <button class="btn btn-primary" @click="$router.push('/employee/new-order')">
          <span>开始点餐</span>
          <span class="btn-icon">→</span>
        </button>
      </div>

      <div v-else>
        <div class="orders-card">
          <div class="card-header">
            <h5 class="mb-0">订单列表</h5>
            <span class="order-count">共 {{ orders.length }} 个订单</span>
          </div>
          <div class="card-body">
            <div class="table-responsive">
              <table class="table">
                <thead>
                  <tr>
                    <th>订单编号</th>
                    <th>用餐日期</th>
                    <th>下单时间</th>
                    <th>总金额</th>
                    <th>操作</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="o in orders" :key="o.id">
                    <td>
                      <span class="order-id">#{{ o.id }}</span>
                    </td>
                    <td>{{ formatDate(o.mealDate) }}</td>
                    <td>{{ formatDateTime(o.orderTime) }}</td>
                    <td>
                      <span class="price-text">¥{{ o.totalPrice }}</span>
                    </td>
                    <td>
                      <button
                        class="btn btn-sm btn-outline-primary"
                        @click="$router.push('/employee/order-detail/' + o.id)"
                      >
                        查看详情
                      </button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>

        <div class="action-buttons">
          <button class="btn btn-primary" @click="$router.push('/employee/new-order')">
            <span>新建订单</span>
            <span class="btn-icon">+</span>
          </button>
          <button class="btn btn-success" @click="$router.push('/employee/personal-order-summary')">
            <span>查看并打印个人月度统计</span>
            <span class="btn-icon">📊</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'EmployeeOrderList',
  data() {
    return {
      loading: false,
      orders: [],
      message: '',
      messageType: 'success'
    }
  },
  computed: {
    messageClass() {
      return this.messageType === 'success' ? 'alert alert-success' : 'alert alert-danger'
    }
  },
  created() {
    this.fetchOrders()
  },
  methods: {
    formatDate(d) {
      if (!d) return ''
      return String(d).substring(0, 10)
    },
    formatDateTime(d) {
      if (!d) return ''
      return String(d).replace('T', ' ').substring(0, 19)
    },
    async fetchOrders() {
      const userJson = localStorage.getItem('currentUser')
      if (!userJson) {
        this.message = '未登录或登录信息已失效，请重新登录'
        this.messageType = 'error'
        this.$router.push('/login')
        return
      }
      const user = JSON.parse(userJson)

      this.loading = true
      try {
        const res = await axios.get('/api/orders', {
          params: {
            role: 'employee',
            userId: user.id
          }
        })
        this.orders = res.data || []
      } catch (e) {
        this.message = '加载订单列表失败'
        this.messageType = 'error'
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.order-list-page {
  max-width: 1400px;
  margin: 0 auto;
  padding: 32px;
}

.page-header {
  text-align: center;
  margin-bottom: 32px;
  padding-bottom: 24px;
  border-bottom: 2px solid rgba(102, 126, 234, 0.1);
}

.page-header h1 {
  font-size: 2.5rem;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 8px;
}

.page-header .text-muted {
  font-size: 16px;
  color: #718096;
}

/* 加载状态 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  text-align: center;
}

.loading-spinner {
  width: 50px;
  height: 50px;
  border: 4px solid rgba(102, 126, 234, 0.1);
  border-top-color: #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 20px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  border: 2px solid rgba(102, 126, 234, 0.1);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 20px;
  display: block;
}

.empty-state h3 {
  font-size: 1.5rem;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 12px;
}

.empty-state p {
  color: #718096;
  margin-bottom: 24px;
  font-size: 16px;
}

/* 订单卡片 */
.orders-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  margin-bottom: 24px;
  overflow: hidden;
}

.orders-card .card-header {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.08) 0%, rgba(118, 75, 162, 0.08) 100%);
  padding: 20px 24px;
  border-bottom: 2px solid rgba(102, 126, 234, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.orders-card .card-header h5 {
  font-size: 1.25rem;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0;
}

.order-count {
  font-size: 14px;
  color: #667eea;
  font-weight: 600;
  background: rgba(102, 126, 234, 0.1);
  padding: 4px 12px;
  border-radius: 12px;
}

.orders-card .card-body {
  padding: 24px;
}

.order-id {
  font-weight: 600;
  color: #667eea;
  font-family: 'Courier New', monospace;
}

.price-text {
  font-weight: 700;
  color: #667eea;
  font-size: 16px;
}

/* 操作按钮组 */
.action-buttons {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  justify-content: center;
}

.action-buttons .btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  font-size: 15px;
  font-weight: 600;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.action-buttons .btn-icon {
  font-size: 18px;
  transition: transform 0.3s ease;
}

.action-buttons .btn:hover .btn-icon {
  transform: scale(1.2);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .order-list-page {
    padding: 16px;
  }

  .page-header h1 {
    font-size: 2rem;
  }

  .orders-card .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .action-buttons {
    flex-direction: column;
  }

  .action-buttons .btn {
    width: 100%;
    justify-content: center;
  }
}
</style>


