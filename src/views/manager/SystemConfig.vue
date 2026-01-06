<template>
  <div class="container mt-4">
    <div class="page-header mb-3">
      <h2 class="mb-1">系统配置</h2>
      <p class="text-muted mb-0">设置订餐截止时间与配餐开始时间，用于控制订餐与打印行为</p>
    </div>

    <div v-if="message" class="alert" :class="messageClass">
      {{ message }}
    </div>

    <div class="card">
      <div class="card-header">
        系统时间设置
      </div>
      <div class="card-body">
        <form @submit.prevent="saveConfig">
          <div class="row g-3">
            <div class="col-md-6">
            <label class="form-label">订餐截止时间</label>
            <input
              v-model="config.order_deadline"
              type="time"
              class="form-control nice-input"
              required
            />
              <div class="form-text">员工在此时间之前可以为当前订餐日下单</div>
            </div>
            <div class="col-md-6">
            <label class="form-label">配餐开始时间</label>
            <input
              v-model="config.delivery_start_time"
              type="time"
              class="form-control nice-input"
              required
            />
              <div class="form-text">到达该时间后，开启配送并自动切换为下一订餐日</div>
            </div>
          </div>
          <button type="submit" class="btn btn-primary mt-3" :disabled="saving">
            {{ saving ? '保存中...' : '保存配置' }}
          </button>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'ManagerSystemConfig',
  data() {
    return {
      config: {
        order_deadline: '09:00',
        delivery_start_time: '11:30'
      },
      message: '',
      messageType: 'success',
      saving: false
    }
  },
  computed: {
    messageClass() {
      return this.messageType === 'success' ? 'alert alert-success' : 'alert alert-danger'
    }
  },
  created() {
    this.loadConfig()
  },
  methods: {
    async loadConfig() {
      try {
        const res = await axios.get('/api/config')
        this.config = {
          order_deadline: res.data.order_deadline || '09:00',
          delivery_start_time: res.data.delivery_start_time || '11:30'
        }
      } catch (e) {
        this.message = '加载系统配置失败'
        this.messageType = 'error'
      }
    },
    async saveConfig() {
      this.saving = true
      this.message = ''
      try {
        await axios.post('/api/config', this.config)
        this.message = '系统配置更新成功'
        this.messageType = 'success'
      } catch (e) {
        this.message = '保存系统配置失败'
        this.messageType = 'error'
      } finally {
        this.saving = false
      }
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


