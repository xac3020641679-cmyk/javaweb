<template>
  <div class="container mt-4">
    <div class="page-header mb-3">
      <h2 class="mb-1">订单汇总（按员工）</h2>
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
      <div class="card-header">
        <h3 class="report-title">员工月度订单汇总</h3>
      </div>
      <div class="card-body">
        <div class="report-info mb-4">
          <div class="info-row">
            <span class="info-label">员工：</span>
            <span class="info-value">{{ employee.name }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">联系电话：</span>
            <span class="info-value">{{ employee.phone || '（未填写）' }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">统计月份：</span>
            <span class="info-value">{{ formattedMonth }}</span>
          </div>
        </div>

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
            <tr v-if="!salesData.length">
              <td colspan="5" class="text-center">暂无数据</td>
            </tr>
            <tr v-else v-for="item in salesData" :key="item.itemName + '-' + item.unit">
              <td>{{ item.itemName }}</td>
              <td>{{ item.unit }}</td>
              <td>{{ item.quantity }}</td>
              <td>¥{{ item.price.toFixed(2) }}</td>
              <td>¥{{ item.subtotal.toFixed(2) }}</td>
            </tr>
          </tbody>
          <tfoot>
            <tr class="total-row">
              <td colspan="4" class="total-label">合计金额</td>
              <td class="total-value">¥{{ totalAmount.toFixed(2) }}</td>
            </tr>
          </tfoot>
        </table>

        <div class="report-footer mt-4">
          <div class="footer-row">
            <span class="footer-label">操作员：</span>
            <span class="footer-value">{{ currentUser ? currentUser.name + '(餐厅经理)' : '系统' }}</span>
          </div>
          <div class="footer-row">
            <span class="footer-label">操作时间：</span>
            <span class="footer-value">{{ currentTime }}</span>
          </div>
        </div>

        <!-- 订单明细列表（在打印/PDF中隐藏） -->
        <div class="order-details-section">
          <h5 class="mt-4">订单明细列表</h5>
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
  name: 'ManagerEmployeeOrderSummaryPage',
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
      
      console.log('准备打印，员工:', this.employee.name)
      console.log('销售数据条数:', this.salesData.length)
      console.log('订单明细条数:', this.monthlyOrders.length)
      
      printPage({
        selector: '.employee-order-report',
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

/* 现代化报表样式 */
.employee-order-report {
  font-family: "Microsoft YaHei", "SimSun", -apple-system, BlinkMacSystemFont, sans-serif;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
}

.employee-order-report .card-header {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  border-bottom: 2px solid rgba(102, 126, 234, 0.2);
  padding: 24px;
}

.report-title {
  font-size: 24px;
  font-weight: 700;
  text-align: center;
  margin: 0;
  padding: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: 0.5px;
}

.report-info {
  margin-bottom: 28px;
  padding: 20px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.03) 0%, rgba(118, 75, 162, 0.03) 100%);
  border-radius: 12px;
  border-left: 4px solid #667eea;
}

.info-row {
  margin-bottom: 12px;
  line-height: 2;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}

.info-label {
  font-weight: 600;
  margin-right: 12px;
  min-width: 100px;
  color: #4a5568;
  font-size: 14px;
}

.info-value {
  color: #1a1a1a;
  font-weight: 500;
  font-size: 15px;
}

.summary-table {
  width: 100%;
  margin: 24px 0;
  border-collapse: separate;
  border-spacing: 0;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.summary-table th {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  font-weight: 700;
  text-align: center;
  padding: 16px 12px;
  border: 1px solid rgba(102, 126, 234, 0.2);
  color: #1a1a1a;
  font-size: 14px;
  letter-spacing: 0.3px;
}

.summary-table td {
  text-align: center;
  padding: 14px 12px;
  border: 1px solid #e2e8f0;
  color: #4a5568;
  font-size: 14px;
  transition: background-color 0.2s ease;
}

.summary-table tbody tr {
  background-color: #ffffff;
}

.summary-table tbody tr:nth-child(even) {
  background: linear-gradient(90deg, rgba(102, 126, 234, 0.02) 0%, rgba(118, 75, 162, 0.02) 100%);
}

.summary-table tbody tr:hover {
  background: linear-gradient(90deg, rgba(102, 126, 234, 0.08) 0%, rgba(118, 75, 162, 0.08) 100%);
  transform: scale(1.01);
  transition: all 0.2s ease;
}

.total-row {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.15) 0%, rgba(118, 75, 162, 0.15) 100%);
  font-weight: 700;
  border-top: 2px solid #667eea;
}

.total-label {
  text-align: right;
  padding-right: 24px;
  font-size: 16px;
  color: #1a1a1a;
}

.total-value {
  text-align: center;
  font-size: 18px;
  color: #f5576c;
  font-weight: 700;
  text-shadow: 0 1px 2px rgba(245, 87, 108, 0.1);
}

.report-footer {
  margin-top: 32px;
  padding-top: 20px;
  border-top: 2px solid rgba(102, 126, 234, 0.1);
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 16px;
}

.footer-row {
  margin-bottom: 0;
  line-height: 1.8;
  display: flex;
  align-items: center;
}

.footer-label {
  font-weight: 600;
  margin-right: 8px;
  min-width: 80px;
  color: #718096;
  font-size: 13px;
}

.footer-value {
  color: #4a5568;
  font-size: 14px;
  font-weight: 500;
}

.order-details-section {
  margin-top: 40px;
  padding-top: 24px;
  border-top: 2px solid rgba(102, 126, 234, 0.1);
}

.order-details-section h5 {
  font-size: 18px;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
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
/* 打印样式 */
@media print {
  @page {
    margin: 2cm;
    size: A4;
  }
  
  body {
    margin: 0 !important;
    padding: 0 !important;
    font-size: 12pt !important;
    background: white !important;
  }
  
  /* 隐藏不需要的元素 */
  .page-header,
  .card:not(.employee-order-report),
  .btn,
  .form-control,
  .form-label,
  .form-select,
  .d-flex,
  .gap-2,
  .alert:not(.employee-order-report .alert) {
    display: none !important;
  }
  
  /* 确保容器可见 */
  .container {
    display: block !important;
    visibility: visible !important;
    padding: 0 !important;
    margin: 0 !important;
    width: 100% !important;
    max-width: 100% !important;
  }
  
  /* 报表样式 */
  .employee-order-report {
    display: block !important;
    visibility: visible !important;
    border: none !important;
    box-shadow: none !important;
    margin: 0 !important;
    padding: 20px !important;
    width: 100% !important;
    page-break-inside: avoid;
  }
  
  .employee-order-report * {
    visibility: visible !important;
  }
  
  .employee-order-report .card-header {
    border-bottom: 2px solid #000 !important;
    padding: 15px 0 !important;
    margin-bottom: 15px !important;
    background-color: transparent !important;
    display: block !important;
  }
  
  .employee-order-report .report-title {
    font-size: 20px !important;
    font-weight: bold !important;
    text-align: center !important;
    margin: 0 !important;
  }
  
  .employee-order-report .report-info {
    margin-bottom: 20px !important;
  }
  
  .employee-order-report .info-row {
    margin-bottom: 10px !important;
    line-height: 1.8 !important;
  }
  
  .employee-order-report .info-label,
  .employee-order-report .footer-label {
    font-weight: bold !important;
    margin-right: 8px !important;
  }
  
  .employee-order-report .report-footer {
    margin-top: 30px !important;
    padding-top: 15px !important;
    border-top: 1px solid #000 !important;
  }
  
  .employee-order-report .footer-row {
    margin-bottom: 8px !important;
    line-height: 1.8 !important;
  }
  
  .employee-order-report .total-row {
    background-color: #f0f0f0 !important;
    font-weight: bold !important;
  }
  
  .employee-order-report .total-value {
    font-size: 16px !important;
    color: #000 !important;
    font-weight: bold !important;
  }
  
  /* 打印时隐藏订单明细列表 */
  .employee-order-report .order-details-section {
    display: none !important;
  }
  
  .employee-order-report .card-body {
    padding: 0 !important;
    display: block !important;
  }
  
  .employee-order-report .row {
    display: flex !important;
  }
  
  .employee-order-report .col-md-6 {
    display: block !important;
  }
  
  .employee-order-report h5 {
    font-size: 16px !important;
    font-weight: bold !important;
    margin: 15px 0 10px 0 !important;
    display: block !important;
  }
  
  .employee-order-report p {
    display: block !important;
    margin: 5px 0 !important;
  }
  
  .employee-order-report .table-responsive {
    display: block !important;
    width: 100% !important;
    overflow: visible !important;
  }
  
  /* 表格样式 */
  .employee-order-report table {
    width: 100% !important;
    border-collapse: collapse !important;
    margin: 15px 0 !important;
    display: table !important;
  }
  
  .employee-order-report thead {
    display: table-header-group !important;
    background-color: #f5f5f5 !important;
    color: #000 !important;
  }
  
  .employee-order-report tbody {
    display: table-row-group !important;
  }
  
  .employee-order-report tfoot {
    display: table-footer-group !important;
  }
  
  .employee-order-report tr {
    display: table-row !important;
    page-break-inside: avoid;
  }
  
  .employee-order-report th,
  .employee-order-report td {
    display: table-cell !important;
    border: 1px solid #000 !important;
    padding: 10px !important;
    text-align: left !important;
    font-size: 14px !important;
    color: #000 !important;
    background: white !important;
  }
  
  .employee-order-report th {
    font-weight: bold !important;
    background-color: #f5f5f5 !important;
    color: #000 !important;
  }
  
  .employee-order-report .summary-table th {
    background-color: #f5f5f5 !important;
    border: 1px solid #000 !important;
    color: #000 !important;
  }
  
  .employee-order-report .summary-table td {
    border: 1px solid #000 !important;
    color: #000 !important;
    background: white !important;
  }
  
  .employee-order-report .summary-table tbody tr {
    background-color: white !important;
  }
  
  .employee-order-report .summary-table tbody tr:nth-child(even) {
    background-color: #f9f9f9 !important;
  }
  
  .employee-order-report .summary-table .total-row {
    background-color: #f0f0f0 !important;
  }
  
  .employee-order-report .summary-table .total-label {
    color: #000 !important;
  }
  
  .employee-order-report .summary-table .total-value {
    color: #000 !important;
    font-weight: bold !important;
  }
}
</style>
