<template>
  <div class="container mt-4">
    <div class="page-header mb-3">
      <h2 class="mb-1">订单详情</h2>
      <p class="text-muted mb-0">查看本次订餐的详细信息</p>
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
      <!-- 订单基本信息 -->
      <div class="card mb-4">
        <div class="card-header">
          <h3>订单信息</h3>
        </div>
        <div class="card-body">
          <dl class="row">
            <dt class="col-sm-3">订单编号:</dt>
            <dd class="col-sm-9">{{ order.id }}</dd>

            <dt class="col-sm-3">员工姓名:</dt>
            <dd class="col-sm-9">{{ order.userName }}</dd>

            <dt class="col-sm-3">联系电话:</dt>
            <dd class="col-sm-9">{{ order.phone }}</dd>

            <dt class="col-sm-3">用餐日期:</dt>
            <dd class="col-sm-9">{{ formatDate(order.mealDate) }}</dd>

            <dt class="col-sm-3">下单时间:</dt>
            <dd class="col-sm-9">{{ formatDateTime(order.orderTime) }}</dd>

            <dt class="col-sm-3">总金额:</dt>
            <dd class="col-sm-9">¥{{ order.totalPrice }}</dd>
          </dl>
        </div>
      </div>

      <!-- 订单项列表 -->
      <div class="card mb-4">
        <div class="card-header">
          <h3>订单详情</h3>
        </div>
        <div class="card-body">
          <div v-if="!items.length" class="alert alert-info text-center">
            该订单暂无详细信息。
          </div>
          <div v-else class="table-responsive">
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
              <tr v-for="item in items" :key="item.id">
                <td>{{ item.name }}</td>
                <td>{{ item.unit }}</td>
                <td>{{ item.quantity }}</td>
                <td>¥{{ item.price }}</td>
                <td>¥{{ item.subtotal }}</td>
              </tr>
              </tbody>
              <tfoot>
              <tr>
                <th colspan="4">总计</th>
                <th>¥{{ order.totalPrice }}</th>
              </tr>
              </tfoot>
            </table>
          </div>
        </div>
      </div>

      <div class="text-center">
        <button class="btn btn-primary" @click="$router.push('/employee/order-list')">
          返回订单列表
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'EmployeeOrderDetail',
  props: {
    id: {
      type: String,
      required: false
    }
  },
  data() {
    return {
      loading: false,
      order: null,
      items: [],
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
    formatDate(d) {
      if (!d) return ''
      return String(d).substring(0, 10)
    },
    formatDateTime(d) {
      if (!d) return ''
      return String(d).replace('T', ' ').substring(0, 19)
    },
    async fetchDetail() {
      const orderId = this.id || this.$route.params.id
      if (!orderId) {
        this.message = '缺少订单编号'
        this.messageType = 'error'
        return
      }
      this.loading = true
      try {
        const res = await axios.get(`/api/orders/${orderId}`)
        this.order = res.data.order
        this.items = res.data.items || []
      } catch (e) {
        if (e.response && e.response.status === 404) {
          this.message = '订单不存在'
        } else {
          this.message = '加载订单详情失败'
        }
        this.messageType = 'error'
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
</style>


