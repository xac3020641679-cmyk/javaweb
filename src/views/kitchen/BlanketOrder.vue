<template>
  <div class="container mt-4">
    <div class="page-header mb-3">
      <h2 class="mb-1">总括订单</h2>
      <p class="text-muted mb-0">按菜品汇总某日所有订单，用于采购与备菜</p>
    </div>

    <div v-if="message" class="alert" :class="messageClass">
      {{ message }}
    </div>

    <div class="card mb-3">
      <div class="card-body">
        <div class="row g-3 align-items-end">
          <div class="col-md-6">
            <label class="form-label">选择日期</label>
            <input
              v-model="date"
              type="date"
              class="form-control"
            />
          </div>
          <div class="col-md-6 d-flex gap-2 justify-content-md-end">
            <button 
              class="btn btn-primary" 
              @click="fetchData"
              :disabled="loading"
            >
              {{ loading ? '查询中...' : '查询' }}
            </button>
            <button
              class="btn btn-success"
              @click="downloadBlanketOrder"
              :disabled="!blanketOrders.length || downloading"
            >
              {{ downloading ? '下载中...' : '下载总括订单' }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <div class="card">
      <div class="card-header">
        {{ formattedDate }} 总括订单
      </div>
      <div class="card-body">
        <div v-if="loading" class="alert alert-info text-center">
          加载中...
        </div>

        <div v-else>
          <div v-if="!blanketOrders.length" class="alert alert-info text-center">
            <p>该日期暂无订单数据。</p>
            <p>请先确保该日期有订单，然后点击"查询"按钮。</p>
          </div>

          <div v-else class="table-responsive">
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
                <tr v-for="item in blanketOrders" :key="item.id || item.itemName">
                  <td>{{ item.itemName }}</td>
                  <td>{{ item.unit }}</td>
                  <td>{{ item.totalQuantity }}</td>
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

export default {
  name: 'KitchenBlanketOrder',
  data() {
    const today = new Date()
    const y = today.getFullYear()
    const m = String(today.getMonth() + 1).padStart(2, '0')
    const d = String(today.getDate()).padStart(2, '0')
    return {
      date: `${y}-${m}-${d}`,
      blanketOrders: [],
      totalAmount: 0,
      loading: false,
      downloading: false,
      message: '',
      messageType: 'success'
    }
  },
  computed: {
    messageClass() {
      return this.messageType === 'success' ? 'alert alert-success' : 'alert alert-danger'
    },
    formattedDate() {
      return this.date
    },
  },
  created() {
    this.fetchData()
  },
  methods: {
    async fetchData() {
      this.loading = true
      this.message = ''
      try {
        const res = await axios.get('/api/kitchen/blanket-orders', {
          params: { date: this.date }
        })
        this.blanketOrders = res.data.blanketOrders || []
        this.totalAmount = res.data.totalAmount || 0
        
        if (this.blanketOrders.length === 0) {
          this.message = `日期 ${this.date} 暂无订单数据`
          this.messageType = 'info'
        } else {
          this.message = `查询成功，共 ${this.blanketOrders.length} 个菜品汇总，总金额 ¥${this.totalAmount.toFixed(2)}`
          this.messageType = 'success'
        }
      } catch (e) {
        this.message = '加载总括订单失败'
        this.messageType = 'error'
      } finally {
        this.loading = false
      }
    },
    async downloadBlanketOrder() {
      if (!this.date) {
        this.message = '请先选择日期'
        this.messageType = 'error'
        return
      }
      
      if (this.blanketOrders.length === 0) {
        this.message = '该日期暂无订单数据，无法下载'
        this.messageType = 'warning'
        return
      }
      
      this.downloading = true
      this.message = ''
      try {
        const response = await axios.get('/api/downloads/blanket-order', {
          params: { date: this.date },
          responseType: 'blob'
        })
        
        const url = window.URL.createObjectURL(new Blob([response.data], { 
          type: 'text/csv;charset=utf-8' 
        }))
        const link = document.createElement('a')
        link.href = url
        
        const contentDisposition = response.headers['content-disposition']
        let filename = `总括订单_${this.date}.csv`
        if (contentDisposition) {
          const filenameMatch = contentDisposition.match(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/)
          if (filenameMatch && filenameMatch[1]) {
            filename = filenameMatch[1].replace(/['"]/g, '')
            try {
              filename = decodeURIComponent(filename)
            } catch (e) {
              // 忽略解码错误
            }
          }
        }
        
        link.setAttribute('download', filename)
        document.body.appendChild(link)
        link.click()
        link.remove()
        window.URL.revokeObjectURL(url)
        
        this.message = '总括订单下载成功'
        this.messageType = 'success'
      } catch (e) {
        console.error('下载总括订单失败:', e)
        this.message = `下载失败: ${e.response?.data?.message || e.message || '未知错误'}`
        this.messageType = 'error'
      } finally {
        this.downloading = false
      }
    }
  }
}
</script>

<style scoped>
</style>


