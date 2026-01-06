g<template>
  <div class="container mt-4">
    <div class="page-header mb-3">
      <h2 class="mb-1">员工月度订单汇总</h2>
      <p class="text-muted mb-0">按员工维度查看某月菜品消费总计和订单明细</p>
    </div>

    <div class="card mb-3">
      <div class="card-body">
        <div class="row g-3 align-items-end">
          <div class="col-md-4">
            <label class="form-label">选择员工</label>
            <select v-model="selectedUserId" class="form-select nice-input">
              <option value="">请选择员工</option>
              <option
                v-for="emp in employees"
                :key="emp.id"
                :value="emp.id"
              >
                {{ emp.name }}
              </option>
            </select>
          </div>
          <div class="col-md-4">
            <label class="form-label">选择年月</label>
            <input v-model="month" type="month" class="form-control nice-input" />
          </div>
          <div class="col-md-4 d-flex gap-2 justify-content-md-end">
            <button class="btn btn-primary" @click="fetchReport">查询</button>
            <button class="btn btn-success" @click="printPage">打印汇总</button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="message" class="alert" :class="messageClass">
      {{ message }}
    </div>

    <div v-if="employee" class="card mb-4 employee-order-report">
      <div class="card-body">
        <!-- 标题 -->
        <h3 class="text-center mb-4"><strong>员工月度订单汇总</strong></h3>

        <!-- 头部信息 -->
        <div class="report-header mb-4">
          <div class="header-row">
            <span class="header-label">员工：</span>
            <span class="header-value">{{ employee.name }}</span>
          </div>
          <div class="header-row">
            <span class="header-label">联系电话：</span>
            <span class="header-value">{{ employee.phone || '（未填写）' }}</span>
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
            <span class="footer-label">操作员：</span>
            <span class="footer-value">{{ currentUser ? currentUser.name + '(财务人员)' : '系统' }}</span>
          </div>
          <div class="footer-row">
            <span class="footer-label">操作时间：</span>
            <span class="footer-value">{{ currentTime }}</span>
          </div>
        </div>

        <div class="order-details-section">
          <h5 class="section-title">订单明细列表</h5>
          <div v-if="!monthlyOrders.length" class="alert alert-info">
            该员工在所选月份暂无订单记录。
          </div>
          <div v-else class="table-responsive">
            <table class="table table-bordered">
              <thead>
                <tr>
                  <th>订单ID</th>
                  <th>用餐日期</th>
                  <th>下单时间</th>
                  <th>订单总额</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="o in monthlyOrders" :key="o.id">
                  <td>{{ o.id }}</td>
                  <td>{{ formatDate(o.mealDate) }}</td>
                  <td>{{ formatDateTime(o.orderTime) }}</td>
                  <td>¥{{ o.totalPrice.toFixed(2) }}</td>
                </tr>
              </tbody>
            </table>
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
  name: 'FinanceEmployeeOrderSummary',
  data() {
    const now = new Date()
    const ym = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
    return {
      employees: [],
      selectedUserId: '',
      month: ym,
      employee: null,
      salesData: [],
      totalAmount: 0,
      monthlyOrders: [],
      message: '',
      messageType: 'success',
      currentUser: null,
      currentTime: ''
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
    this.loadEmployees()
    this.loadCurrentUser()
    this.updateCurrentTime()
  },
  methods: {
    async loadEmployees() {
      try {
        const res = await axios.get('/api/users')
        const list = res.data || []
        this.employees = list.filter(u => u.role === 'employee')
      } catch (e) {
        this.message = '加载员工列表失败'
        this.messageType = 'error'
      }
    },
    formatDate(d) {
      if (!d) return ''
      return String(d).substring(0, 10)
    },
    formatDateTime(d) {
      if (!d) return ''
      return String(d).replace('T', ' ').substring(0, 19)
    },
    loadCurrentUser() {
      try {
        const userJson = localStorage.getItem('currentUser')
        if (userJson) {
          this.currentUser = JSON.parse(userJson)
        }
      } catch (e) {
        console.warn('加载当前用户信息失败:', e)
      }
    },
    updateCurrentTime() {
      const now = new Date()
      const y = now.getFullYear()
      const m = String(now.getMonth() + 1).padStart(2, '0')
      const d = String(now.getDate()).padStart(2, '0')
      const hh = String(now.getHours()).padStart(2, '0')
      const mm = String(now.getMinutes()).padStart(2, '0')
      const ss = String(now.getSeconds()).padStart(2, '0')
      this.currentTime = `${y}-${m}-${d} ${hh}:${mm}:${ss}`
    },
    async fetchReport() {
      if (!this.selectedUserId) {
        this.message = '请选择员工'
        this.messageType = 'error'
        return
      }
      this.message = ''
      this.updateCurrentTime() // 更新操作时间
      try {
        const res = await axios.get('/api/reports/employee-summary', {
          params: {
            userId: this.selectedUserId,
            month: this.month
          }
        })
        this.employee = res.data.employee
        this.salesData = res.data.salesData || []
        this.totalAmount = res.data.totalAmount || 0
        this.monthlyOrders = res.data.monthlyOrders || []
      } catch (e) {
        this.message = '加载员工月度订单汇总失败'
        this.messageType = 'error'
      }
    },
    printPage() {
      console.log('=== 开始打印员工订单汇总 ===')
      
      if (!this.employee) {
        alert('请先选择员工并查询数据')
        return
      }
      
      if (!checkDataReady(this.salesData, '销售数据')) {
        alert('该员工在所选月份暂无订单数据')
        return
      }
      
      printPage({
        selector: '.employee-order-report',
        delay: 300,
        onError: (error) => {
          console.error('打印失败:', error)
          alert('打印失败: ' + error.message)
        },
        filename: `员工订单汇总_${this.employee.name}_${this.month.replace('-', '')}`
      })
    }
  }
}
</script>

<style scoped>
.page-header {
  margin-bottom: 32px;
  padding-bottom: 20px;
  border-bottom: 2px solid rgba(102, 126, 234, 0.1);
}

.page-header h2 {
  font-size: 2rem;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 8px;
}

.nice-input {
  border-radius: 8px;
  padding-inline: 16px;
}

.text-center {
  text-align: center;
}

/* 汇总报表样式 - 与员工页面保持一致 */
.employee-order-report {
  max-width: 900px;
  margin: 0 auto;
}

.employee-order-report h3 {
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

.order-details-section {
  margin-top: 40px;
  padding-top: 24px;
  border-top: 2px solid rgba(102, 126, 234, 0.1);
}

.order-details-section .table {
  border-radius: 12px;
  overflow: hidden;
}

.order-details-section .table thead th {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  font-weight: 600;
  color: #1a1a1a;
  border: 1px solid rgba(102, 126, 234, 0.2);
}

.order-details-section .table tbody tr:hover {
  background: linear-gradient(90deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
}
</style>

<style>
/* 打印样式 - 与员工页面保持一致 */
@media print {
  .page-header,
  .card:not(.employee-order-report),
  .btn,
  .alert:not(.employee-order-report .alert) {
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
  .employee-order-report .row {
    display: flex !important;
    visibility: visible !important;
  }

  .employee-order-report .col-md-6 {
    display: block !important;
    visibility: visible !important;
  }

  .employee-order-report {
    max-width: 100%;
    margin: 0;
    box-shadow: none;
    border: none;
    page-break-inside: avoid;
  }

  .employee-order-report .card-body {
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

  /* 打印时隐藏订单明细列表 */
  .employee-order-report .order-details-section {
    display: none !important;
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


