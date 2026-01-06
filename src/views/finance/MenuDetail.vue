<template>
  <div class="container mt-4">
    <h2>菜单详情（财务视图）</h2>

    <div v-if="message" class="alert" :class="messageClass">
      {{ message }}
    </div>

    <div v-if="loading" class="alert alert-info text-center">
      加载中...
    </div>

    <div v-else-if="!menu" class="alert alert-warning text-center">
      未找到该菜单。
    </div>

    <div v-else>
      <div class="card mb-4">
        <div class="card-header">
          菜单基本信息
        </div>
        <div class="card-body">
          <p><strong>ID：</strong>{{ menu.id }}</p>
          <p><strong>名称：</strong>{{ menu.name }}</p>
          <p><strong>日期：</strong>{{ formatDate(menu.date) }}</p>
          <p><strong>是否激活：</strong>{{ menu.active ? '是' : '否' }}</p>
          <p><strong>创建时间：</strong>{{ formatDateTime(menu.createdAt) }}</p>
        </div>
      </div>

      <div class="card mb-4">
        <div class="card-header">
          菜单菜品
        </div>
        <div class="card-body">
          <div v-if="!items.length" class="alert alert-info text-center">
            该菜单暂无菜品。
          </div>
          <div v-else class="table-responsive">
            <table class="table table-striped">
              <thead>
                <tr>
                  <th>菜名</th>
                  <th>单位</th>
                  <th>单价</th>
                  <th>图片</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in items" :key="item.id">
                  <td>{{ item.name }}</td>
                  <td>{{ item.unit }}</td>
                  <td>¥{{ item.price.toFixed(2) }}</td>
                  <td>
                    <img
                      v-if="item.image"
                      :src="item.image"
                      alt="图片"
                      style="max-width: 80px; max-height: 80px; object-fit: cover"
                    />
                    <span v-else>无</span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <div class="text-center">
        <button class="btn btn-secondary" @click="$router.push('/finance/dashboard')">
          返回财务主页
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'FinanceMenuDetailPage',
  props: {
    id: {
      type: String,
      required: false
    }
  },
  data() {
    return {
      menu: null,
      items: [],
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
    this.loadDetail()
  },
  methods: {
    async loadDetail() {
      const id = this.id || this.$route.params.id
      if (!id) {
        this.message = '缺少菜单 ID'
        this.messageType = 'error'
        return
      }
      this.loading = true
      try {
        const res = await axios.get(`/api/menus/${id}`)
        this.menu = res.data.menu
        this.items = res.data.items || []
      } catch (e) {
        this.message = '加载菜单详情失败'
        this.messageType = 'error'
      } finally {
        this.loading = false
      }
    },
    formatDate(d) {
      if (!d) return ''
      return String(d).substring(0, 10)
    },
    formatDateTime(d) {
      if (!d) return ''
      return String(d).replace('T', ' ').substring(0, 19)
    }
  }
}
</script>

<style scoped>
.page {
  padding: 24px;
}
</style>