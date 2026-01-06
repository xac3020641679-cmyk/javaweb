<template>
  <div class="container mt-4">
    <h1 class="text-center mb-4">月度销售统计报表</h1>

    <div class="card mb-4">
      <div class="card-header">
        <h3>筛选条件</h3>
      </div>
      <div class="card-body">
        <form class="row g-3" @submit.prevent="fetchReport">
          <div class="col-md-4">
            <label for="reportMonth" class="form-label">选择月份</label>
            <input
              id="reportMonth"
              v-model="month"
              type="month"
              class="form-control"
            />
          </div>
          <div class="col-md-4 d-flex align-items-end gap-2">
            <button type="submit" class="btn btn-primary">查询</button>
            <button class="btn btn-success" @click.prevent="printPage">打印报表</button>
          </div>
        </form>
      </div>
    </div>

    <div v-if="message" class="alert" :class="messageClass">
      {{ message }}
    </div>

    <div class="card mb-4 sales-report">
      <div class="card-header">
        <h5 class="mb-0">{{ month || currentMonth }} 月份销售统计</h5>
      </div>
      <div class="card-body">
        <div v-if="loading" class="alert alert-info text-center print-show">
          加载中...
        </div>

        <div v-else class="print-content">
          <div v-if="!salesData || salesData.length === 0" class="alert alert-info text-center print-show">
            <p class="mb-0">该月份（{{ month || currentMonth }}）暂无销售数据。</p>
            <p class="mb-0 mt-2 small">请检查：</p>
            <ul class="text-start small mt-2" style="display: inline-block;">
              <li>该月份是否有订单记录</li>
              <li>订单的用餐日期是否在该月份范围内</li>
              <li>订单状态是否为已完成</li>
            </ul>
          </div>

          <div v-else class="table-responsive print-content">
            <table class="table table-striped sales-table">
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
              <tr v-for="item in salesData" :key="(item.itemName || '') + '-' + (item.unit || '')">
                <td>{{ item.itemName || '未知' }}</td>
                <td>{{ item.unit || '-' }}</td>
                <td>{{ item.quantity || 0 }}</td>
                <td>¥{{ (item.price || 0).toFixed(2) }}</td>
                <td>¥{{ (item.subtotal || 0).toFixed(2) }}</td>
              </tr>
              </tbody>
              <tfoot>
              <tr>
                <th colspan="4">总计</th>
                <th>¥{{ (totalAmount || 0).toFixed(2) }}</th>
              </tr>
              </tfoot>
            </table>
          </div>
        </div>
      </div>
    </div>

    <div class="text-center">
      <button class="btn btn-secondary" @click="$router.push('/finance/dashboard')">
        返回财务主页
      </button>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import { printPage, checkDataReady } from '@/utils/print.js'

export default {
  name: 'FinanceMonthlySalesReport',
  data() {
    const now = new Date()
    const ym = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
    return {
      month: ym,
      currentMonth: ym,
      salesData: [],
      totalAmount: 0,
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
    this.fetchReport()
  },
  methods: {
    async fetchReport() {
      this.loading = true
      this.message = ''
      try {
        console.log('开始获取月度销售报表，月份:', this.month)
        const res = await axios.get('/api/reports/monthly-sales', {
          params: { month: this.month }
        })
        console.log('月度销售报表响应:', res.data)
        console.log('响应数据类型:', typeof res.data)
        console.log('响应数据内容:', JSON.stringify(res.data))
        
        this.salesData = res.data.salesData || []
        this.totalAmount = res.data.totalAmount || 0
        console.log('销售数据条数:', this.salesData.length)
        console.log('总金额:', this.totalAmount)
        
        if (this.salesData.length === 0) {
          this.message = '该月份暂无销售数据'
          this.messageType = 'info'
        } else {
          this.message = ''
        }
      } catch (e) {
        console.error('加载月度销售报表失败:', e)
        console.error('错误详情:', e.response?.data)
        this.message = '加载月度销售报表失败: ' + (e.response?.data?.error || e.response?.data || e.message)
        this.messageType = 'error'
        this.salesData = []
        this.totalAmount = 0
      } finally {
        this.loading = false
      }
    },
    printPage() {
      console.log('=== 开始打印月度销售报表 ===')
      
      if (this.loading) {
        this.message = '数据加载中，请稍候再试'
        this.messageType = 'error'
        return
      }
      
      if (!checkDataReady(this.salesData, '销售数据')) {
        this.message = '该月份暂无销售数据，无法打印'
        this.messageType = 'error'
        alert('该月份暂无销售数据，无法打印')
        return
      }
      
      console.log('准备打印，数据条数:', this.salesData.length)
      console.log('总金额:', this.totalAmount)
      
      printPage({
        selector: '.sales-report',
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
.nice-input {
  border-radius: 999px;
  padding-inline: 14px;
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
  .card:not(.sales-report),
  .btn,
  .form-control,
  .form-label,
  .form,
  .d-flex,
  .gap-2 {
    display: none !important;
  }
  
  /* 只隐藏非报表内的alert */
  .alert:not(.sales-report .alert) {
    display: none !important;
  }
  
  /* 但报表内的alert要显示（包括"加载中"和"暂无数据"） */
  .sales-report .alert {
    display: block !important;
    visibility: visible !important;
    opacity: 1 !important;
    border: 1px solid #333 !important;
    padding: 10px !important;
    margin: 10px 0 !important;
    background-color: #f0f0f0 !important;
    color: #000 !important;
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
  .sales-report {
    display: block !important;
    visibility: visible !important;
    border: none !important;
    box-shadow: none !important;
    margin: 0 !important;
    padding: 20px !important;
    width: 100% !important;
    page-break-inside: avoid;
  }
  
  .sales-report * {
    visibility: visible !important;
    opacity: 1 !important;
  }
  
  .sales-report .card-header {
    border-bottom: 2px solid #000 !important;
    padding: 15px 0 !important;
    margin-bottom: 15px !important;
    background-color: transparent !important;
    display: block !important;
  }
  
  .sales-report .card-header h5 {
    font-size: 20px !important;
    font-weight: bold !important;
    margin: 0 !important;
    text-align: center !important;
    display: block !important;
  }
  
  .sales-report .card-body {
    padding: 0 !important;
    display: block !important;
    visibility: visible !important;
  }
  
  .sales-report .card-body > div {
    display: block !important;
    visibility: visible !important;
    opacity: 1 !important;
  }
  
  .sales-report .table-responsive {
    display: block !important;
    width: 100% !important;
    overflow: visible !important;
    visibility: visible !important;
    opacity: 1 !important;
  }
  
  .sales-report .print-show,
  .sales-report .print-content {
    display: block !important;
    visibility: visible !important;
    opacity: 1 !important;
  }
  
  /* 表格样式 */
  .sales-table {
    width: 100% !important;
    border-collapse: collapse !important;
    margin: 0 !important;
    display: table !important;
    page-break-inside: avoid;
  }
  
  .sales-table thead {
    display: table-header-group !important;
    background-color: #f5f5f5 !important;
  }
  
  .sales-table tbody {
    display: table-row-group !important;
    visibility: visible !important;
  }
  
  .sales-table tfoot {
    display: table-footer-group !important;
    visibility: visible !important;
  }
  
  .sales-table tr {
    display: table-row !important;
    visibility: visible !important;
    page-break-inside: avoid;
  }
  
  .sales-table th,
  .sales-table td {
    display: table-cell !important;
    visibility: visible !important;
    border: 1px solid #333 !important;
    padding: 10px !important;
    text-align: left !important;
    font-size: 14px !important;
    color: #000 !important;
    background: white !important;
    opacity: 1 !important;
  }
  
  .sales-table th {
    font-weight: bold !important;
    background-color: #f5f5f5 !important;
  }
  
  .sales-table tfoot th {
    background-color: #e9ecef !important;
    font-weight: bold !important;
    font-size: 16px !important;
  }
}
</style>


