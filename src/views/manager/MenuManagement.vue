<template>
  <div class="menu-management-page">
    <div class="page-header">
      <div class="header-content">
        <h1>菜单管理</h1>
        <p class="text-muted">维护每日菜单及可订餐菜品</p>
      </div>
      <button class="btn btn-primary" @click="openCreateModal">
        <span>添加新菜单</span>
        <span class="btn-icon">+</span>
      </button>
    </div>

    <div v-if="message" class="alert" :class="messageTypeClass">
      {{ message }}
    </div>

    <div class="info-card">
      <div class="info-icon">💡</div>
      <div class="info-content">
        <strong>提示：</strong>要让菜单在"今日菜单"中显示，需要满足以下条件：
        <ul>
          <li>菜单日期设置为今天或未来日期</li>
          <li>勾选"设为当前活动菜单"复选框</li>
          <li>菜单中至少添加一个菜品</li>
        </ul>
      </div>
    </div>

    <div class="menus-card">
      <div class="card-header">
        <h5 class="mb-0">菜单列表</h5>
        <span v-if="!loading && menus.length > 0" class="menu-count">共 {{ menus.length }} 个菜单</span>
      </div>
      <div class="card-body">
        <div v-if="loading" class="loading-container">
          <div class="loading-spinner"></div>
          <p>加载中...</p>
        </div>
        <div v-else>
          <div v-if="menus.length === 0" class="empty-state">
            <div class="empty-icon">🍽️</div>
            <h3>暂无菜单数据</h3>
            <p>点击"添加新菜单"按钮创建第一个菜单</p>
          </div>
          <div v-else class="table-responsive">
            <table class="table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>菜单名称</th>
                  <th>菜单日期</th>
                  <th>状态</th>
                  <th>创建时间</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="menu in menus" :key="menu.id">
                  <td>
                    <span class="menu-id">#{{ menu.id }}</span>
                  </td>
                  <td>
                    <strong>{{ menu.name }}</strong>
                  </td>
                  <td>{{ formatDate(menu.date) }}</td>
                  <td>
                    <span :class="menu.active ? 'badge-active' : 'badge-inactive'">
                      {{ menu.active ? '✓ 激活' : '未激活' }}
                    </span>
                  </td>
                  <td>{{ formatDateTime(menu.createdAt) }}</td>
                  <td>
                    <div class="action-buttons">
                      <button class="btn btn-sm btn-primary" @click="openEditModal(menu)">编辑</button>
                      <button class="btn btn-sm btn-info" @click="viewMenu(menu)">查看</button>
                      <button class="btn btn-sm btn-danger" @click="deleteMenu(menu)">删除</button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>

    <!-- 简单弹窗：新增/编辑菜单 -->
    <div v-if="showModal" class="modal-backdrop">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">{{ editMenu.id ? '编辑菜单' : '添加菜单' }}</h5>
            <button type="button" class="btn-close" @click="closeModal"></button>
          </div>
          <div class="modal-body">
            <div class="mb-3">
              <label class="form-label">菜单名称</label>
              <input v-model="editMenu.name" type="text" class="form-control">
            </div>
            <div class="mb-3">
              <label class="form-label">菜单日期</label>
              <input v-model="editMenu.date" type="date" class="form-control">
            </div>
            <div class="form-check mb-3">
              <input
                id="active"
                v-model="editMenu.active"
                type="checkbox"
                class="form-check-input"
              >
              <label class="form-check-label" for="active">设为当前活动菜单</label>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" @click="closeModal">取消</button>
            <button class="btn btn-primary" @click="saveMenu">保存</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'ManagerMenuManagement',
  data() {
    return {
      loading: false,
      menus: [],
      message: '',
      messageType: 'success',
      showModal: false,
      editMenu: {
        id: null,
        name: '',
        date: '',
        active: false
      }
    }
  },
  computed: {
    messageTypeClass() {
      return this.messageType === 'success' ? 'alert alert-success' : 'alert alert-danger'
    }
  },
  created() {
    this.fetchMenus()
  },
  methods: {
    async fetchMenus() {
      this.loading = true
      try {
        const res = await axios.get('/api/menus')
        this.menus = res.data || []
      } catch (e) {
        this.message = '加载菜单失败'
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
    },
    openCreateModal() {
      this.editMenu = {
        id: null,
        name: '',
        date: '',
        active: false
      }
      this.showModal = true
    },
    openEditModal(menu) {
      this.editMenu = {
        id: menu.id,
        name: menu.name,
        date: this.formatDate(menu.date),
        active: !!menu.active
      }
      this.showModal = true
    },
    closeModal() {
      this.showModal = false
    },
    async saveMenu() {
      try {
        if (!this.editMenu.name || !this.editMenu.date) {
          this.message = '请填写完整菜单信息'
          this.messageType = 'error'
          return
        }
        const payload = {
          name: this.editMenu.name,
          date: this.editMenu.date,
          active: this.editMenu.active
        }
        if (this.editMenu.id) {
          await axios.put(`/api/menus/${this.editMenu.id}`, payload)
          this.message = '菜单更新成功'
          this.messageType = 'success'
          this.showModal = false
          this.fetchMenus()
        } else {
          const res = await axios.post('/api/menus', payload)
          const menuId = res.data.id
          this.message = '菜单创建成功'
          this.messageType = 'success'
          this.showModal = false
          // 跳转到编辑页面以便添加菜品
          this.$router.push(`/manager/menu-form/${menuId}`)
        }
      } catch (e) {
        this.message = '保存菜单失败'
        this.messageType = 'error'
      }
    },
    async deleteMenu(menu) {
      if (!confirm('确定要删除这个菜单吗？')) return
      try {
        await axios.delete(`/api/menus/${menu.id}`)
        this.message = '菜单删除成功'
        this.messageType = 'success'
        this.fetchMenus()
      } catch (e) {
        this.message = '删除菜单失败'
        this.messageType = 'error'
      }
    },
    viewMenu(menu) {
      // 跳转到菜单详情页
      this.$router.push(`/manager/menu-detail/${menu.id}`)
    }
  }
}
</script>

<style scoped>
.menu-management-page {
  max-width: 1400px;
  margin: 0 auto;
  padding: 32px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 32px;
  padding-bottom: 24px;
  border-bottom: 2px solid rgba(102, 126, 234, 0.1);
  flex-wrap: wrap;
  gap: 16px;
}

.header-content {
  flex: 1;
}

.page-header h1 {
  font-size: 2.5rem;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 8px;
}

.page-header .text-muted {
  font-size: 16px;
  color: #718096;
}

.page-header .btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  font-size: 15px;
  font-weight: 600;
  border-radius: 12px;
}

.page-header .btn-icon {
  font-size: 18px;
  transition: transform 0.3s ease;
}

.page-header .btn:hover .btn-icon {
  transform: scale(1.2);
}

/* 信息卡片 */
.info-card {
  background: linear-gradient(135deg, rgba(79, 172, 254, 0.1) 0%, rgba(0, 242, 254, 0.1) 100%);
  border: 2px solid rgba(79, 172, 254, 0.2);
  border-radius: 16px;
  padding: 20px 24px;
  margin-bottom: 24px;
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.info-icon {
  font-size: 2rem;
  flex-shrink: 0;
}

.info-content {
  flex: 1;
  color: #0c5460;
  font-size: 14px;
  line-height: 1.8;
}

.info-content strong {
  color: #0c5460;
  font-weight: 700;
  display: block;
  margin-bottom: 8px;
}

.info-content ul {
  margin: 8px 0 0 0;
  padding-left: 20px;
}

.info-content li {
  margin-bottom: 4px;
}

/* 菜单卡片 */
.menus-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.menus-card .card-header {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.08) 0%, rgba(118, 75, 162, 0.08) 100%);
  padding: 20px 24px;
  border-bottom: 2px solid rgba(102, 126, 234, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.menus-card .card-header h5 {
  font-size: 1.25rem;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0;
}

.menu-count {
  font-size: 14px;
  color: #667eea;
  font-weight: 600;
  background: rgba(102, 126, 234, 0.1);
  padding: 4px 12px;
  border-radius: 12px;
}

.menus-card .card-body {
  padding: 24px;
}

/* 加载状态 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid rgba(102, 126, 234, 0.1);
  border-top-color: #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 20px;
  display: block;
}

.empty-state h3 {
  font-size: 1.5rem;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 12px;
}

.empty-state p {
  color: #718096;
  font-size: 16px;
}

/* 表格样式优化 */
.menu-id {
  font-weight: 600;
  color: #667eea;
  font-family: 'Courier New', monospace;
}

.badge-active {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
  color: #fff;
}

.badge-inactive {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  background: #e2e8f0;
  color: #718096;
}

.action-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.action-buttons .btn {
  padding: 6px 14px;
  font-size: 13px;
  font-weight: 600;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.action-buttons .btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .menu-management-page {
    padding: 16px;
  }

  .page-header {
    flex-direction: column;
    align-items: stretch;
  }

  .page-header h1 {
    font-size: 2rem;
  }

  .page-header .btn {
    width: 100%;
    justify-content: center;
  }

  .menus-card .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .info-card {
    flex-direction: column;
    text-align: center;
  }

  .action-buttons {
    flex-direction: column;
  }

  .action-buttons .btn {
    width: 100%;
  }
}

.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1050;
}

.modal-dialog {
  max-width: 500px;
  width: 100%;
}
</style>


