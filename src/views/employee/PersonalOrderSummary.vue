<template>
  <div class="container mt-4">
    <div class="page-header mb-3">
      <h2 class="mb-1">个人月度订单统计</h2>
      <p class="text-muted mb-0">查看自己在某月的点餐总金额和菜品构成，可打印签名确认</p>
    </div>

    <div class="card mb-3">
      <div class="card-body">
        <div class="row g-3 align-items-end">
          <div class="col-md-4">
            <label class="form-label">选择年月</label>
            <input v-model="month" type="month" class="form-control nice-input" />
          </div>
          <div class="col-md-4 d-flex gap-2">
            <button class="btn btn-primary" @click="fetchReport">查询</button>
            <button class="btn btn-success" @click="printPage">打印统计</button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="message" class="alert" :class="messageClass">
      {{ message }}
    </div>

    <div class="card summary-report" v-if="user">
      <div class="card-body">
        <!-- 标题 -->
        <h3 class="text-center mb-4"><strong>员工月度订单汇总</strong></h3>

        <!-- 头部信息 -->
        <div class="report-header mb-4">
          <div class="header-row">
            <span class="header-label">员工：</span>
            <span class="header-value">{{ user.name }}</span>
          </div>
          <div class="header-row">
            <span class="header-label">联系电话：</span>
            <span class="header-value">{{ user.phone || '（未填写）' }}</span>
          </div>
          <div class="header-row">
            <span class="header-label">统计月份：</span>
            <span class="header-value">{{ formattedMonth }}</span>
          </div>
        </div>

        <!-- 汇总表格 -->
        <div class="table-responsive">
          <table class="table table-bordered summary-table">
            <thead>
              <tr>
                <th>菜名</th>
                <th>单位</th>
                <th>分量</th>
                <th>单价</th>
                <th>合计</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in salesData" :key="item.itemName + '-' + item.unit">
                <td>{{ item.itemName }}</td>
                <td>{{ item.unit || '-' }}</td>
                <td>{{ item.quantity }}</td>
                <td>¥{{ item.price.toFixed(2) }}</td>
                <td>¥{{ item.subtotal.toFixed(2) }}</td>
              </tr>
              <tr v-if="salesData.length === 0">
                <td colspan="5" class="text-center text-muted">暂无数据</td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- 底部信息 -->
        <div class="report-footer mt-4">
          <div class="footer-row">
            <span class="footer-label">合计金额：</span>
            <span class="footer-value">¥{{ totalAmount.toFixed(2) }}</span>
          </div>
          <div class="footer-row">
            <span class="footer-label">员工签名：</span>
            <span class="footer-value">__________</span>
          </div>
          <div class="footer-row">
            <span class="footer-label">打印时间：</span>
            <span class="footer-value">{{ formattedPrintTime }}</span>
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
  name: 'EmployeePersonalOrderSummaryPage',
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
    },
    formattedPrintTime() {
      if (!this.now) {
        const now = new Date()
        const year = now.getFullYear()
        const month = String(now.getMonth() + 1).padStart(2, '0')
        const day = String(now.getDate()).padStart(2, '0')
        const hours = String(now.getHours()).padStart(2, '0')
        const minutes = String(now.getMinutes()).padStart(2, '0')
        const seconds = String(now.getSeconds()).padStart(2, '0')
        return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
      }
      // 处理 ISO 格式时间字符串
      const date = new Date(this.now)
      if (isNaN(date.getTime())) {
        return this.now
      }
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      const hours = String(date.getHours()).padStart(2, '0')
      const minutes = String(date.getMinutes()).padStart(2, '0')
      const seconds = String(date.getSeconds()).padStart(2, '0')
      return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
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
        // 更新打印时间为当前时间
        const now = new Date()
        this.now = now.toISOString()
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
      
      // 更新打印时间
      const now = new Date()
      this.now = now.toISOString()
      
      // 使用统一的打印工具
      this.$nextTick(() => {
        printPage({
          selector: '.summary-report',
          delay: 300,
          onError: (error) => {
            console.error('打印失败:', error)
            alert('打印失败: ' + error.message)
          }
        })
      })
    }
  }
}
</script>

<style scoped>
.page {
  padding: 24px;
}

.nice-input {
  border-radius: 999px;
  padding-inline: 14px;
}

/* 汇总报表样式 */
.summary-report {
  max-width: 900px;
  margin: 0 auto;
}

.summary-report h3 {
  font-size: 20px;
  font-weight: bold;
  margin-bottom: 20px;
}

.report-header {
  margin-bottom: 20px;
}

.header-row {
  margin-bottom: 8px;
  font-size: 14px;
}

.header-label {
  font-weight: bold;
  margin-right: 8px;
}

.header-value {
  color: #333;
}

.summary-table {
  width: 100%;
  margin-bottom: 20px;
  border-collapse: collapse;
}

.summary-table th {
  background-color: #f5f5f5;
  font-weight: bold;
  text-align: center;
  padding: 10px;
  border: 1px solid #ddd;
}

.summary-table td {
  text-align: center;
  padding: 8px;
  border: 1px solid #ddd;
}

.summary-table tbody tr:nth-child(even) {
  background-color: #f9f9f9;
}

.report-footer {
  margin-top: 20px;
  font-size: 14px;
}

.footer-row {
  margin-bottom: 8px;
}

.footer-label {
  font-weight: bold;
  margin-right: 8px;
}

.footer-value {
  color: #333;
}

/* 打印样式 */
@media print {
  .page-header,
  .card:not(.summary-report),
  .btn,
  .alert:not(.summary-report .alert) {
    display: none !important;
  }

  /* 确保容器可见 */
  .container {
    display: block !important;
    visibility: visible !important;
    max-width: 100% !important;
    padding: 0 !important;
    margin: 0 !important;
    width: 100% !important;
  }

  /* 确保报表内的row可见 */
  .summary-report .row {
    display: flex !important;
    visibility: visible !important;
  }

  .summary-report .col-md-6 {
    display: block !important;
    visibility: visible !important;
  }

  .summary-report {
    max-width: 100%;
    margin: 0;
    box-shadow: none;
    border: none;
    page-break-inside: avoid;
  }

  .summary-report .card-body {
    padding: 20px;
  }

  .summary-table {
    page-break-inside: avoid;
    width: 100%;
  }

  .report-header {
    page-break-inside: avoid;
    margin-bottom: 15px;
  }

  .report-footer {
    page-break-inside: avoid;
    margin-top: 20px;
  }

  .footer-total {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  /* 确保打印时字体清晰 */
  body {
    font-size: 12pt;
    line-height: 1.5;
  }

  .summary-table th,
  .summary-table td {
    font-size: 11pt;
  }
}
</style>