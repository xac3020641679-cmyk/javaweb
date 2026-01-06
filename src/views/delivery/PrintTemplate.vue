<template>
  <div class="print-container">
    <div v-if="loading" class="loading-message">
      <p>正在加载打印数据...</p>
    </div>
    
    <div v-else-if="error" class="error-message">
      <p>{{ error }}</p>
    </div>
    
    <div v-else-if="ordersToPrint.length === 0" class="empty-message">
      <p>没有可打印的订单数据</p>
    </div>
    
    <div v-else>
      <div
        v-for="(o, index) in ordersToPrint"
        :key="o.order ? o.order.id : index"
        class="order-sheet"
      >
        <div class="order-info">
          <div class="info-row-first">
            <span class="label">员工：</span>
            <span class="value">{{ o.order ? o.order.userName : '未知' }}</span>
            <span class="spacer"></span>
            <span class="label">联系电话：</span>
            <span class="value">{{ o.order && o.order.phone ? o.order.phone : '（未填写）' }}</span>
          </div>
          <div class="info-row">
            <span class="label">工位信息：</span>
            <span class="value">{{ o.order && o.order.workLocation ? o.order.workLocation : '（待补充）' }}</span>
          </div>
        </div>

        <table class="items-table">
          <thead>
            <tr>
              <th>菜名</th>
              <th>单位</th>
              <th>分量</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="!o.items || o.items.length === 0">
              <td colspan="3" class="text-center">（无订单项）</td>
            </tr>
            <tr v-else v-for="item in o.items" :key="item.id || item.name">
              <td>{{ item.name || '未知' }}</td>
              <td>{{ item.unit || '-' }}</td>
              <td>{{ item.quantity || 0 }}</td>
            </tr>
          </tbody>
        </table>

        <div class="order-footer">
          <div class="footer-row">
            <span class="label">送餐员：</span>
            <span class="value">__________</span>
          </div>
          <div class="footer-row">
            <span class="label">打印时间：</span>
            <span class="value">{{ printTime }}</span>
          </div>
        </div>

        <div v-if="index < ordersToPrint.length - 1" class="divider" />
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import { printPage, checkDataReady } from '@/utils/print.js'

export default {
  name: 'DeliveryPrintTemplate',
  props: {
    id: {
      type: String,
      required: false
    }
  },
  data() {
    return {
      ordersToPrint: [],
      printTime: '',
      loading: true,
      error: ''
    }
  },
  async created() {
    console.log('=== PrintTemplate 开始加载数据 ===')
    const id = this.id || this.$route.params.id
    const date = this.$route.query.date
    console.log('打印参数 - id:', id, 'date:', date)
    
    try {
      await this.loadPrintData(id, date)
      
      // 设置打印时间
      const now = new Date()
      const y = now.getFullYear()
      const m = String(now.getMonth() + 1).padStart(2, '0')
      const d = String(now.getDate()).padStart(2, '0')
      const hh = String(now.getHours()).padStart(2, '0')
      const mm = String(now.getMinutes()).padStart(2, '0')
      const ss = String(now.getSeconds()).padStart(2, '0')
      this.printTime = `${y}-${m}-${d} ${hh}:${mm}:${ss}`

      // 检查数据
      if (!checkDataReady(this.ordersToPrint, '订单数据')) {
        this.error = '没有可打印的订单数据，请检查日期或订单ID是否正确'
        this.loading = false
        return
      }

      console.log('✓ 数据加载完成，订单数量:', this.ordersToPrint.length)
      
      // 等待DOM渲染后打印
      await this.$nextTick()
      
      // 使用打印工具函数
      printPage({
        selector: '.print-container',
        delay: 800,
        onError: (error) => {
          console.error('打印失败:', error)
          alert('打印失败: ' + error.message)
        }
      })
    } catch (error) {
      console.error('加载打印数据失败:', error)
      this.error = '加载打印数据失败: ' + (error.response?.data?.message || error.message)
      this.loading = false
    }
  },
  methods: {
    async loadPrintData(id, date) {
      // 批量打印模式
      if (date && id === 'batch') {
        console.log('批量打印模式：开始加载订单列表')
        const res = await axios.get('/api/delivery/orders', {
          params: { date: date }
        })
        const orders = res.data.orders || []
        console.log('获取到订单列表，数量:', orders.length)
        
        if (orders.length === 0) {
          throw new Error('该日期没有订单数据')
        }
        
        // 获取每个订单的详细信息
        this.ordersToPrint = []
        for (const order of orders) {
          try {
            const orderRes = await axios.get(`/api/delivery/orders/${order.id}`)
            this.ordersToPrint.push({
              order: orderRes.data.order,
              items: orderRes.data.orderItems || []
            })
            console.log('订单', order.id, '加载成功')
          } catch (e) {
            console.error(`加载订单 ${order.id} 失败:`, e)
          }
        }
        console.log('批量打印数据加载完成')
      }
      // 单个订单打印模式
      else if (id && id !== 'batch') {
        console.log('单个订单打印模式：加载订单', id)
        const res = await axios.get(`/api/delivery/orders/${id}`)
        this.ordersToPrint = [
          {
            order: res.data.order,
            items: res.data.orderItems || []
          }
        ]
        console.log('订单', id, '加载成功')
      } else {
        throw new Error('打印参数错误，请重新选择要打印的订单')
      }
      
      this.loading = false
    }
  }
}
</script>

<style scoped>
.print-container {
  font-family: "宋体", "SimSun", serif;
  font-size: 12pt;
  line-height: 1.6;
  padding: 20px;
  max-width: 210mm;
  margin: 0 auto;
  background: #fff;
}

.loading-message,
.error-message,
.empty-message {
  text-align: center;
  padding: 50px 20px;
  font-size: 16pt;
}

.error-message {
  color: #d32f2f;
}

.order-sheet {
  margin-bottom: 30px;
  page-break-inside: avoid;
}

.order-info {
  margin-bottom: 15px;
}

.info-row-first {
  margin-bottom: 8px;
  line-height: 1.8;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}

.info-row {
  margin-bottom: 8px;
  line-height: 1.8;
}

.info-row-first .label,
.info-row .label {
  font-weight: bold;
  margin-right: 5px;
}

.info-row-first .value,
.info-row .value {
  color: #000;
  margin-right: 20px;
}

.info-row-first .spacer {
  flex: 1;
  min-width: 20px;
}

.items-table {
  width: 100%;
  border-collapse: collapse;
  margin: 15px 0;
  font-size: 12pt;
}

.items-table thead {
  background-color: #f5f5f5;
}

.items-table th {
  border: 1px solid #333;
  padding: 8px 12px;
  text-align: center;
  font-weight: bold;
  background-color: #f5f5f5;
}

.items-table td {
  border: 1px solid #333;
  padding: 8px 12px;
  text-align: center;
}

.items-table tbody tr:nth-child(even) {
  background-color: #fafafa;
}

.order-footer {
  margin-top: 15px;
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
}

.footer-row {
  margin-bottom: 8px;
  line-height: 1.8;
}

.footer-row .label {
  font-weight: bold;
  margin-right: 5px;
}

.footer-row .value {
  color: #000;
}

.divider {
  border-top: 2px dashed #666;
  margin: 20px 0;
  height: 0;
}

.text-center {
  text-align: center;
}
</style>

<style>
/* 打印样式 */
@media print {
  @page {
    margin: 20mm;
    size: A4;
  }
  
  * {
    visibility: visible !important;
  }
  
  body {
    margin: 0 !important;
    padding: 0 !important;
    background: white !important;
    font-size: 12pt !important;
  }
  
  /* 隐藏不需要的元素 */
  .app-header,
  .app-sidebar,
  .no-print,
  button,
  .btn,
  .loading-message,
  .error-message,
  .empty-message {
    display: none !important;
  }
  
  .app-content {
    padding: 0 !important;
    margin: 0 !important;
  }
  
  .print-container {
    display: block !important;
    visibility: visible !important;
    width: 100% !important;
    padding: 0 !important;
    margin: 0 !important;
    font-family: "宋体", "SimSun", serif;
    font-size: 12pt;
    line-height: 1.6;
  }
  
  .print-container * {
    visibility: visible !important;
  }
  
  .order-sheet {
    page-break-inside: avoid;
    margin-bottom: 20px;
    display: block !important;
  }
  
  .order-info,
  .order-footer {
    display: block !important;
  }
  
  .info-row-first {
    display: flex !important;
  }
  
  .info-row,
  .footer-row {
    display: block !important;
  }
  
  .items-table {
    display: table !important;
    width: 100% !important;
    border-collapse: collapse !important;
    margin: 15px 0 !important;
    visibility: visible !important;
  }
  
  .items-table thead {
    display: table-header-group !important;
    background-color: #f5f5f5 !important;
    visibility: visible !important;
  }
  
  .items-table tbody {
    display: table-row-group !important;
    visibility: visible !important;
  }
  
  .items-table tr {
    display: table-row !important;
    visibility: visible !important;
  }
  
  .items-table th,
  .items-table td {
    display: table-cell !important;
    border: 1px solid #333 !important;
    padding: 8px 12px !important;
    text-align: center !important;
    visibility: visible !important;
    color: #000 !important;
    background: white !important;
  }
  
  .items-table th {
    background-color: #f5f5f5 !important;
    font-weight: bold !important;
  }
  
  .divider {
    display: block !important;
    margin: 15px 0 !important;
  }
}
</style>
