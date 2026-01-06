<template>
  <div class="container mt-4">
    <div class="page-header mb-3">
      <h2 class="mb-1">月度销售统计报表</h2>
      <p class="text-muted mb-0">按菜品维度统计某月销售数量与金额</p>
    </div>

    <div class="card mb-4">
      <div class="card-body">
        <form class="row g-3 align-items-end" @submit.prevent="fetchReport">
          <div class="col-md-4">
            <label for="reportMonth" class="form-label">选择月份</label>
            <input
              id="reportMonth"
              v-model="month"
              type="month"
              class="form-control nice-input"
            />
          </div>
          <div class="col-md-4 d-flex gap-2">
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
          <div v-if="!salesData.length" class="alert alert-info text-center print-show">
            该月份暂无销售数据。
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
                  <th colspan="4">总计</th>
                  <th>¥{{ totalAmount.toFixed(2) }}</th>
                </tr>
              </tfoot>
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
  name: 'ManagerSalesReportPage',
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
        this.salesData = res.data.salesData || []
        this.totalAmount = res.data.totalAmount || 0
        console.log('销售数据条数:', this.salesData.length)
        console.log('总金额:', this.totalAmount)
        if (this.salesData.length === 0) {
          this.message = '该月份暂无销售数据'
          this.messageType = 'info'
        }
      } catch (e) {
        console.error('加载月度销售报表失败:', e)
        this.message = '加载月度销售报表失败: ' + (e.response?.data?.error || e.message)
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
  
  /* 特别确保card-body内的所有内容可见 */
  .sales-report .card-body,
  .sales-report .card-body * {
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
  
  /* 强制显示card-body内的所有div（包括v-else分支） */
  .sales-report .card-body > div {
    display: block !important;
    visibility: visible !important;
    opacity: 1 !important;
  }
  
  /* 强制显示v-else分支的内容 */
  .sales-report .card-body > div > div {
    display: block !important;
    visibility: visible !important;
    opacity: 1 !important;
  }
  
  /* 强制显示表格容器 */
  .sales-report .table-responsive {
    display: block !important;
    width: 100% !important;
    overflow: visible !important;
    visibility: visible !important;
    opacity: 1 !important;
  }
  
  /* 强制显示所有print-show和print-content类的内容 */
  .sales-report .print-show,
  .sales-report .print-content {
    display: block !important;
    visibility: visible !important;
    opacity: 1 !important;
  }
  
  /* 强制显示"暂无数据"的alert */
  .sales-report .alert.print-show {
    display: block !important;
    visibility: visible !important;
    opacity: 1 !important;
    border: 1px solid #333 !important;
    padding: 10px !important;
    margin: 10px 0 !important;
    background-color: #f0f0f0 !important;
    color: #000 !important;
  }
  
  /* 确保所有v-else分支的div可见 */
  .sales-report .card-body > div.print-content {
    display: block !important;
    visibility: visible !important;
    opacity: 1 !important;
  }
  
  .sales-report .card-body > div.print-content > div {
    display: block !important;
    visibility: visible !important;
    opacity: 1 !important;
  }
  
  /* 强制显示所有嵌套的div（无论有多少层） */
  .sales-report .card-body div {
    display: block !important;
    visibility: visible !important;
    opacity: 1 !important;
  }
  
  /* 但表格容器要特殊处理 */
  .sales-report .card-body div.table-responsive {
    display: block !important;
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
  
  /* 确保所有表格行和单元格在打印时可见 */
  .sales-table tbody tr {
    display: table-row !important;
    visibility: visible !important;
    opacity: 1 !important;
  }
  
  .sales-table tbody td {
    display: table-cell !important;
    visibility: visible !important;
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
  
  /* 最终强制显示：确保所有表格内容可见 */
  .sales-report .sales-table tbody tr {
    display: table-row !important;
    visibility: visible !important;
    opacity: 1 !important;
  }
  
  .sales-report .sales-table tbody td {
    display: table-cell !important;
    visibility: visible !important;
    opacity: 1 !important;
    content: attr(data-content) !important;
  }
  
  /* 确保v-else分支的所有内容都显示 */
  .sales-report .card-body > div:not(.btn):not(.form-control) {
    display: block !important;
    visibility: visible !important;
    opacity: 1 !important;
  }
  
  .sales-report .card-body > div > div {
    display: block !important;
    visibility: visible !important;
    opacity: 1 !important;
  }
  
  .sales-report .card-body > div > div > div {
    display: block !important;
    visibility: visible !important;
    opacity: 1 !important;
  }
}
</style>

