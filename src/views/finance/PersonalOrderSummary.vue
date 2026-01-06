<template>
  <div class="container mt-4">
    <h2>个人月度订单统计</h2>

    <div class="row mb-3">
      <div class="col-md-4">
        <label class="form-label">选择年月</label>
        <input v-model="month" type="month" class="form-control" />
      </div>
      <div class="col-md-4 d-flex align-items-end">
        <button class="btn btn-primary me-2" @click="fetchReport">查询</button>
        <button class="btn btn-success" @click="printPage">打印统计</button>
      </div>
    </div>

    <div v-if="message" class="alert" :class="messageClass">
      {{ message }}
    </div>

    <div class="card" v-if="user">
      <div class="card-header">
        员工：{{ user.name }} ({{ formattedMonth }} 个人订单统计)
      </div>
      <div class="card-body">
        <div class="row mb-3">
          <div class="col-md-6">
            <p><strong>联系电话：</strong>{{ user.phone }}</p>
          </div>
          <div class="col-md-6">
            <p><strong>统计月份：</strong>{{ formattedMonth }}</p>
          </div>
        </div>

        <table class="table table-striped">
          <thead>
          <tr>
            <th>菜名</th>
            <th>单位</th>
            <th>总数量</th>
            <th>单价</th>
            <th>小计</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="item in salesData" :key="item.itemName + '-' + item.unit">
            <td>{{ item.itemName }}</td>
            <td>{{ item.unit }}</td>
            <td>{{ item.quantity }}</td>
            <td>¥{{ item.price.toFixed(2) }}</td>
            <td>¥{{ item.subtotal.toFixed(2) }}</td>
          </tr>
          </tbody>
          <tfoot>
          <tr>
            <th colspan="4">合计金额</th>
            <th>¥{{ totalAmount.toFixed(2) }}</th>
          </tr>
          </tfoot>
        </table>

        <div class="row mt-4">
          <div class="col-md-6">
            <p><strong>员工签名：</strong>___________</p>
          </div>
          <div class="col-md-6 text-end">
            <p><strong>打印时间：</strong>{{ now }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import { printPage, checkDataReady } from '@/utils/print.js'

export default {
  name: 'FinancePersonalOrderSummary',
  data() {
    const now = new Date()
    const ym = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
    return {
      month: ym,
      user: null,
      salesData: [],
      totalAmount: 0,
      now: '',
      message: '',
      messageType: 'success'
    }
  },
  computed: {
    messageClass() {
      return this.messageType === 'success' ? 'alert alert-success' : 'alert alert-danger'
    },
    formattedMonth() {
      if (!this.month) return ''
      const [y, m] = this.month.split('-')
      return `${y}年${m}月`
    }
  },
  created() {
    this.fetchReport()
  },
  methods: {
    async fetchReport() {
      const userJson = localStorage.getItem('currentUser')
      if (!userJson) {
        this.message = '未登录或登录信息已失效，请重新登录'
        this.messageType = 'error'
        this.$router.push('/login')
        return
      }
      const currentUser = JSON.parse(userJson)
      this.message = ''
      try {
        const res = await axios.get('/api/reports/personal-summary', {
          params: {
            userId: currentUser.id,
            month: this.month
          }
        })
        this.user = res.data.user
        this.salesData = res.data.salesData || []
        this.totalAmount = res.data.totalAmount || 0
        this.now = res.data.now
      } catch (e) {
        this.message = '加载个人月度订单统计失败'
        this.messageType = 'error'
      }
    },
    printPage() {
      console.log('=== 开始打印个人月度订单统计 ===')
      
      if (!this.user) {
        alert('请先查询数据')
        return
      }
      
      if (!checkDataReady(this.salesData, '销售数据')) {
        alert('该月份暂无订单数据')
        return
      }
      
      printPage({
        selector: '.summary-report',
        delay: 300,
        onError: (error) => {
          console.error('打印失败:', error)
          alert('打印失败: ' + error.message)
        }
      })
    }
  }
}
</script>

<style scoped>
</style>


