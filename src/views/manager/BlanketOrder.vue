<template>
  <div class="container mt-4">
    <h2>总括订单（经理视图）</h2>

    <div v-if="message" class="alert" :class="messageClass">
      {{ message }}
    </div>

    <div class="row mb-3">
      <div class="col-md-6">
        <label class="form-label">选择日期</label>
        <input
          v-model="date"
          type="date"
          class="form-control"
        />
      </div>
      <div class="col-md-6 d-flex align-items-end gap-2">
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
            <p><strong>该日期暂无订单数据。</strong></p>
            <p class="mb-0">请先确保该日期有订单，然后点击"查询"按钮。</p>
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
  name: 'ManagerBlanketOrderPage',
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
        console.log('查询总括订单，日期:', this.date)
        const res = await axios.get('/api/kitchen/blanket-orders', {
          params: { date: this.date }
        })
        console.log('总括订单查询结果:', res.data)
        this.blanketOrders = res.data.blanketOrders || []
        this.totalAmount = res.data.totalAmount || 0
        
        if (this.blanketOrders.length === 0) {
          this.message = `日期 ${this.date} 暂无订单数据。请确认：1) 该日期是否有订单；2) 订单状态是否为已完成`
          this.messageType = 'info'
        } else {
          this.message = `查询成功，共 ${this.blanketOrders.length} 个菜品汇总，总金额 ¥${this.totalAmount.toFixed(2)}`
          this.messageType = 'success'
        }
      } catch (e) {
        console.error('加载总括订单失败:', e)
        this.message = `加载总括订单失败: ${e.response?.data?.message || e.message || '未知错误'}`
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
          responseType: 'blob' // 重要：指定响应类型为blob
        })
        
        // 创建下载链接
        const url = window.URL.createObjectURL(new Blob([response.data], { 
          type: 'text/csv;charset=utf-8' 
        }))
        const link = document.createElement('a')
        link.href = url
        
        // 从响应头获取文件名，如果没有则使用默认名称
        const contentDisposition = response.headers['content-disposition']
        let filename = `总括订单_${this.date}.csv`
        if (contentDisposition) {
          const filenameMatch = contentDisposition.match(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/)
          if (filenameMatch && filenameMatch[1]) {
            filename = filenameMatch[1].replace(/['"]/g, '')
            // 处理URL编码的文件名
            try {
              filename = decodeURIComponent(filename)
            } catch (e) {
              // 如果解码失败，使用原始文件名
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
.page {
  padding: 24px;
}
</style>