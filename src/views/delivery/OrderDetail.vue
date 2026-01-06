<template>
  <div class="container mt-4">
    <h1 class="text-center mb-4">订单详情</h1>

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
      <div class="card mb-4">
        <div class="card-header">
          <h3>订单信息</h3>
        </div>
        <div class="card-body">
          <p><strong>订单编号:</strong> {{ order.id }}</p>
          <p><strong>员工姓名:</strong> {{ order.userName }}</p>
          <p><strong>联系电话:</strong> {{ order.phone }}</p>
          <p><strong>下单时间:</strong> {{ formatDateTime(order.orderTime) }}</p>
          <p><strong>用餐日期:</strong> {{ formatDate(order.mealDate) }}</p>
          <p><strong>总金额:</strong> ¥{{ order.totalPrice }}</p>
        </div>
      </div>

      <div class="card mb-4">
        <div class="card-header">
          <h3>订单详情</h3>
        </div>
        <div class="card-body">
          <div class="table-responsive">
            <table class="table table-striped">
              <thead>
                <tr>
                  <th>菜名</th>
                  <th>单位</th>
                  <th>数量</th>
                  <th>单价</th>
                  <th>小计</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in orderItems" :key="item.id">
                  <td>{{ item.name }}</td>
                  <td>{{ item.unit }}</td>
                  <td>{{ item.quantity }}</td>
                  <td>¥{{ item.price }}</td>
                  <td>¥{{ item.subtotal }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <div class="text-center">
        <button class="btn btn-secondary" @click="$router.push('/delivery/dashboard')">
          返回配送主页
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'DeliveryOrderDetail',
  props: {
    id: {
      type: String,
      required: false
    }
  },
  data() {
    return {
      order: null,
      orderItems: [],
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
    this.fetchDetail()
  },
  methods: {
    async fetchDetail() {
      const oid = this.id || this.$route.params.id
      if (!oid) {
        this.message = '缺少订单编号'
        this.messageType = 'error'
        return
      }
      this.loading = true
      try {
        const res = await axios.get(`/api/delivery/orders/${oid}`)
        this.order = res.data.order
        this.orderItems = res.data.orderItems || []
      } catch (e) {
        this.message = '加载订单详情失败'
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
</style>


