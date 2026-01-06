<template>
  <div class="container mt-4 dashboard-page">
    <div class="page-header mb-4">
      <h2 class="mb-1">配餐员控制台</h2>
      <p class="text-muted mb-0">管理配送订单和打印配送单</p>
    </div>

    <div class="row mb-3">
      <div class="col-md-6">
        <label class="form-label">选择配送日期</label>
        <input
          v-model="date"
          type="date"
          class="form-control"
        />
      </div>
      <div class="col-md-6 d-flex align-items-end">
        <button class="btn btn-primary me-2" @click="fetchOrders">
          查询
        </button>
        <button
          class="btn btn-success me-2"
          :disabled="!canPrint || !orders.length"
          @click="printPage"
        >
          批量打印订单
        </button>
      </div>
    </div>

    <div v-if="message" class="alert" :class="messageClass">
      {{ message }}
    </div>

    <div class="card">
      <div class="card-header">
        {{ formattedDate }} 订单列表
      </div>
      <div class="card-body">
        <div v-if="loading" class="alert alert-info text-center">
          加载中...
        </div>

        <div v-else>
          <div v-if="!orders.length" class="alert alert-info text-center">
            <p>该日期暂无配送订单。</p>
          </div>

          <div v-else class="table-responsive">
            <table class="table table-striped">
              <thead>
              <tr>
                <th>员工姓名</th>
                <th>联系电话</th>
                <th>工位信息</th>
                <th>订单总额</th>
                <th>下单时间</th>
                <th>操作</th>
              </tr>
              </thead>
              <tbody>
              <tr v-for="order in orders" :key="order.id">
                <td>{{ order.userName }}</td>
                <td>{{ order.phone }}</td>
                <td>{{ order.workLocation || '（待补充）' }}</td>
                <td>¥{{ order.totalPrice.toFixed(2) }}</td>
                <td>{{ formatDateTime(order.orderTime) }}</td>
                <td>
                  <button
                    class="btn btn-sm btn-info me-1"
                    @click="$router.push('/delivery/order-detail/' + order.id)"
                  >
                    查看详情
                  </button>
                  <button
                    class="btn btn-sm btn-success"
                    :disabled="!canPrint"
                    @click="$router.push('/delivery/print-template/' + order.id)"
                  >
                    打印
                  </button>
                </td>
              </tr>
              </tbody>
            </table>
          </div>

          <div v-if="!canPrint" class="alert alert-warning mt-3">
            配餐开始时间未到，无法打印订单。
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'DeliveryDashboard',
  data() {
    const today = new Date()
    const y = today.getFullYear()
    const m = String(today.getMonth() + 1).padStart(2, '0')
    const d = String(today.getDate()).padStart(2, '0')
    return {
      date: `${y}-${m}-${d}`,
      orders: [],
      canPrint: false,
      loading: false,
      message: '',
      messageType: 'success'
    }
  },
  computed: {
    messageClass() {
      return this.messageType === 'success'
        ? 'alert alert-success'
        : 'alert alert-danger'
    },
    formattedDate() {
      return this.date
    },
  },
  created() {
    this.fetchOrders()
  },
  methods: {
    async fetchOrders() {
      this.loading = true
      this.message = ''
      try {
        const res = await axios.get('/api/delivery/orders', {
          params: { date: this.date }
        })
        this.orders = res.data.orders || []
        this.canPrint = !!res.data.canPrint
        
        // 调试：检查工位信息
        console.log('订单列表数据:', this.orders)
        this.orders.forEach((order, index) => {
          console.log(`订单 ${index + 1}:`, {
            id: order.id,
            userName: order.userName,
            workLocation: order.workLocation
          })
        })
      } catch (e) {
        this.message = '加载配送订单列表失败'
        this.messageType = 'error'
      } finally {
        this.loading = false
      }
    },
    formatDateTime(d) {
      if (!d) return ''
      return String(d).replace('T', ' ').substring(0, 16)
    },
    printPage() {
      // 跳转到批量打印页面
      this.$router.push(`/delivery/print-template/batch?date=${this.date}`)
    }
  }
}
</script>

<style scoped>
.dashboard-page {
  max-width: 1400px;
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

.dashboard-page .card {
  border-radius: 8px;
  border: 1px solid #dee2e6;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.dashboard-page .card-header {
  background-color: #f8f9fa;
  border-bottom: 1px solid #dee2e6;
  font-weight: 600;
  padding: 1rem 1.25rem;
}
</style>


