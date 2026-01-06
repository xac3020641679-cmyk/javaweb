<template>
  <div class="container mt-4">
    <div class="page-header mb-3 text-center">
      <h2 class="mb-1">订单确认</h2>
      <p class="text-muted mb-0">以下是您本次提交的订单详情，请确认信息无误</p>
    </div>

    <div v-if="message" class="alert" :class="messageClass">
      {{ message }}
    </div>

    <div v-if="loading" class="alert alert-info text-center">
      加载中...
    </div>

    <div v-else-if="!order" class="alert alert-warning text-center">
      未找到该订单。
    </div>

    <div v-else>
      <div class="order-form">
        <div class="order-header">
          <h3 class="order-title">订单表单</h3>
        </div>
        
        <div class="order-info-section">
          <div class="info-item">
            <span class="info-label">员工姓名：</span>
            <span class="info-value">{{ order.userName }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">联系电话：</span>
            <span class="info-value">{{ order.phone }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">下单时间：</span>
            <span class="info-value">{{ formatDateTime(order.orderTime) }}</span>
          </div>
        </div>

        <div class="order-items-section">
          <h4 class="section-title">点餐表</h4>
          <table class="order-items-table">
            <thead>
              <tr>
                <th>菜名</th>
                <th>单位</th>
                <th>分量</th>
                <th>单价</th>
                <th>合计价格</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in items" :key="item.id">
                <td>{{ item.name }}</td>
                <td>{{ item.unit || '-' }}</td>
                <td>{{ item.quantity }}</td>
                <td>¥{{ item.price.toFixed(2) }}</td>
                <td>¥{{ item.subtotal.toFixed(2) }}</td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="order-total-section">
          <div class="total-row">
            <span class="total-label">总计价格：</span>
            <span class="total-value">¥{{ order.totalPrice.toFixed(2) }}</span>
          </div>
        </div>
      </div>

      <div class="text-center">
        <button class="btn btn-primary me-2" @click="$router.push('/employee/order-list')">
          查看我的订单
        </button>
        <button class="btn btn-secondary" @click="$router.push('/employee/menu')">
          返回今日菜单
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'EmployeeOrderConfirmationPage',
  props: {
    id: {
      type: String,
      required: false
    }
  },
  data() {
    return {
      order: null,
      items: [],
      loading: false,
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
    this.loadOrder()
  },
  methods: {
    async loadOrder() {
      const id = this.id || this.$route.params.id
      if (!id) {
        this.message = '缺少订单 ID'
        this.messageType = 'error'
        return
      }
      this.loading = true
      try {
        const res = await axios.get(`/api/orders/${id}`)
        this.order = res.data.order
        this.items = res.data.items || []
        
        // 调试：检查订单数据
        console.log('订单确认页面 - 订单数据:', this.order)
        console.log('订单确认页面 - 工位信息:', this.order?.workLocation)
      } catch (e) {
        this.message = '加载订单信息失败'
        this.messageType = 'error'
      } finally {
        this.loading = false
      }
    },
    formatDate(d) {
      if (!d) return ''
      return String(d).substring(0, 10)
    },
    formatDateTime(d) {
      if (!d) return ''
      return String(d).replace('T', ' ').substring(0, 19)
    }
  }
}
</script>

<style scoped>
.order-form {
  max-width: 800px;
  margin: 0 auto;
  background: #fff;
  border: 1px solid #dee2e6;
  border-radius: 8px;
  padding: 30px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.order-header {
  text-align: center;
  margin-bottom: 30px;
  padding-bottom: 15px;
  border-bottom: 2px solid #007bff;
}

.order-title {
  font-size: 24px;
  font-weight: 600;
  color: #212529;
  margin: 0;
}

.order-info-section {
  margin-bottom: 30px;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 6px;
}

.info-item {
  margin-bottom: 12px;
  font-size: 16px;
  line-height: 1.8;
}

.info-item:last-child {
  margin-bottom: 0;
}

.info-label {
  font-weight: 600;
  color: #495057;
  margin-right: 8px;
}

.info-value {
  color: #212529;
}

.order-items-section {
  margin-bottom: 30px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #212529;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #dee2e6;
}

.order-items-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 15px;
}

.order-items-table thead {
  background-color: #007bff;
  color: #fff;
}

.order-items-table th {
  padding: 12px;
  text-align: left;
  font-weight: 600;
  border: 1px solid #0056b3;
}

.order-items-table tbody tr {
  border-bottom: 1px solid #dee2e6;
}

.order-items-table tbody tr:hover {
  background-color: #f8f9fa;
}

.order-items-table td {
  padding: 12px;
  border: 1px solid #dee2e6;
  color: #212529;
}

.order-items-table tbody tr:last-child {
  border-bottom: 2px solid #dee2e6;
}

.order-total-section {
  text-align: right;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 6px;
  border-top: 2px solid #007bff;
}

.total-row {
  display: inline-flex;
  align-items: center;
  gap: 15px;
}

.total-label {
  font-size: 18px;
  font-weight: 600;
  color: #495057;
}

.total-value {
  font-size: 24px;
  font-weight: 700;
  color: #007bff;
}
</style>