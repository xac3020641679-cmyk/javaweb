<template>
  <div class="container mt-4">
    <div class="page-header d-flex justify-content-between align-items-center mb-3">
      <div>
        <h2 class="mb-1">用户管理</h2>
        <p class="text-muted mb-0">维护系统用户、角色和联系方式</p>
      </div>
      <div>
        <button class="btn btn-primary me-2" @click="openAdd">添加用户</button>
        <button class="btn btn-success" @click="openBulk">批量导入用户</button>
      </div>
    </div>

    <div v-if="message" class="alert" :class="messageClass">
      {{ message }}
    </div>

    <div class="card">
      <div class="card-body">
        <div v-if="!users.length" class="alert alert-info text-center">
          暂无用户数据，请先添加或批量导入。
        </div>
        <div v-else class="table-responsive">
          <table class="table table-striped align-middle">
            <thead>
            <tr>
              <th>姓名</th>
              <th>登录名</th>
              <th>联系电话</th>
              <th>部门</th>
              <th>工位</th>
              <th>角色</th>
              <th style="width: 150px">操作</th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="u in users" :key="u.id">
              <td>{{ u.name }}</td>
              <td>{{ u.loginName }}</td>
              <td>{{ u.phone }}</td>
              <td>{{ u.department }}</td>
              <td>{{ u.workstation }}</td>
              <td>{{ roleLabel(u.role) }}</td>
              <td>
                <button class="btn btn-sm btn-outline-primary me-1" @click="openEdit(u)">编辑</button>
                <button class="btn btn-sm btn-outline-danger" @click="removeUser(u)">删除</button>
              </td>
            </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- 添加/编辑用户弹窗 -->
    <div v-if="showForm" class="modal-backdrop">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">{{ editingUser.id ? '编辑用户' : '添加用户' }}</h5>
            <button type="button" class="btn-close" @click="closeForm"></button>
          </div>
          <div class="modal-body">
            <div class="mb-3">
              <label class="form-label">姓名</label>
              <input v-model="editingUser.name" type="text" class="form-control nice-input" required>
            </div>
            <div class="mb-3">
              <label class="form-label">登录名</label>
              <input v-model="editingUser.loginName" type="text" class="form-control nice-input" required>
            </div>
            <div class="mb-3">
              <label class="form-label">密码</label>
              <input v-model="editingUser.password" type="password" class="form-control nice-input">
            </div>
            <div class="mb-3">
              <label class="form-label">联系电话</label>
              <input v-model="editingUser.phone" type="text" class="form-control nice-input">
            </div>
            <div class="mb-3">
              <label class="form-label">部门</label>
              <input v-model="editingUser.department" type="text" class="form-control nice-input">
            </div>
            <div class="mb-3">
              <label class="form-label">工位</label>
              <input v-model="editingUser.workstation" type="text" class="form-control nice-input">
            </div>
            <div class="mb-3">
              <label class="form-label">角色</label>
              <select v-model="editingUser.role" class="form-select nice-input">
                <option value="employee">员工</option>
                <option 
                  value="manager" 
                  :disabled="editingUser.role !== 'manager' && hasExistingManager"
                >
                  餐厅经理{{ editingUser.role !== 'manager' && hasExistingManager ? ' (已存在)' : '' }}
                </option>
                <option 
                  value="kitchen_chief"
                  :disabled="editingUser.role !== 'kitchen_chief' && hasExistingKitchenChief"
                >
                  厨房主管{{ editingUser.role !== 'kitchen_chief' && hasExistingKitchenChief ? ' (已存在)' : '' }}
                </option>
                <option value="delivery_staff">配餐员</option>
                <option value="finance">财务管理</option>
              </select>
              <div v-if="editingUser.role === 'manager' && hasExistingManager" class="form-text text-warning">
                ⚠️ 系统中已存在餐厅经理，只能有一个餐厅经理
              </div>
              <div v-if="editingUser.role === 'kitchen_chief' && hasExistingKitchenChief" class="form-text text-warning">
                ⚠️ 系统中已存在厨房主管，只能有一个厨房主管
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" @click="closeForm">取消</button>
            <button class="btn btn-primary" @click="saveUser">保存</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 批量导入弹窗 -->
    <div v-if="showBulk" class="modal-backdrop">
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">批量导入用户</h5>
            <button type="button" class="btn-close" @click="closeBulk"></button>
          </div>
          <div class="modal-body">
            <div class="mb-3">
              <label class="form-label">用户数据（每行一个用户，使用逗号分隔）</label>
              <textarea
                v-model="bulkData"
                class="form-control"
                rows="8"
                placeholder="姓名,登录名,密码,电话,部门,工位,角色&#10;例如：&#10;张三,zhangsan,123456,13800000001,技术部,3楼301,employee"
              />
              <div class="form-text">
                角色可选值：employee（员工）、manager（餐厅经理）、kitchen_chief（厨房主管）、delivery_staff（配餐员）、finance（财务管理）。
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" @click="closeBulk">取消</button>
            <button class="btn btn-primary" @click="submitBulk">开始导入</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'ManagerUserManagement',
  data() {
    return {
      users: [],
      message: '',
      messageType: 'success',
      showForm: false,
      showBulk: false,
      editingUser: {
        id: null,
        name: '',
        loginName: '',
        password: '',
        phone: '',
        department: '',
        workstation: '',
        role: 'employee'
      },
      bulkData: ''
    }
  },
  computed: {
    messageClass() {
      return this.messageType === 'success' ? 'alert alert-success' : 'alert alert-danger'
    },
    hasExistingManager() {
      return this.users.some(u => 
        u.role === 'manager' && (!this.editingUser.id || u.id !== this.editingUser.id)
      )
    },
    hasExistingKitchenChief() {
      return this.users.some(u => 
        u.role === 'kitchen_chief' && (!this.editingUser.id || u.id !== this.editingUser.id)
      )
    }
  },
  created() {
    this.fetchUsers()
  },
  methods: {
    async fetchUsers() {
      try {
        const res = await axios.get('/api/users')
        this.users = res.data || []
      } catch (e) {
        this.message = '加载用户列表失败'
        this.messageType = 'error'
      }
    },
    roleLabel(role) {
      switch (role) {
        case 'manager': return '餐厅经理'
        case 'kitchen_chief': return '厨房主管'
        case 'delivery_staff': return '配餐员'
        case 'finance': return '财务管理'
        case 'employee': return '员工'
        default: return role
      }
    },
    openAdd() {
      this.editingUser = {
        id: null,
        name: '',
        loginName: '',
        password: '',
        phone: '',
        department: '',
        workstation: '',
        role: 'employee'
      }
      this.showForm = true
    },
    openEdit(user) {
      this.editingUser = { ...user, password: '' }
      this.showForm = true
    },
    closeForm() {
      this.showForm = false
    },
    async saveUser() {
      // 前端验证：检查是否已存在餐厅经理或厨房主管
      if (this.editingUser.role === 'manager') {
        const existingManager = this.users.find(u => 
          u.role === 'manager' && (!this.editingUser.id || u.id !== this.editingUser.id)
        )
        if (existingManager) {
          this.message = '系统中已存在餐厅经理，只能有一个餐厅经理'
          this.messageType = 'error'
          return
        }
      } else if (this.editingUser.role === 'kitchen_chief') {
        const existingKitchenChief = this.users.find(u => 
          u.role === 'kitchen_chief' && (!this.editingUser.id || u.id !== this.editingUser.id)
        )
        if (existingKitchenChief) {
          this.message = '系统中已存在厨房主管，只能有一个厨房主管'
          this.messageType = 'error'
          return
        }
      }
      
      try {
        const payload = { ...this.editingUser }
        // 如果编辑时密码为空，可选择在后端保持原密码，这里直接发过去，后端可自行处理
        if (this.editingUser.id) {
          await axios.put(`/api/users/${this.editingUser.id}`, payload)
          this.message = '用户更新成功'
        } else {
          await axios.post('/api/users', payload)
          this.message = '用户添加成功'
        }
        this.messageType = 'success'
        this.showForm = false
        this.fetchUsers()
      } catch (e) {
        // 从后端获取错误信息
        const errorMsg = e.response?.data || '保存用户失败'
        this.message = typeof errorMsg === 'string' ? errorMsg : '保存用户失败'
        this.messageType = 'error'
      }
    },
    async removeUser(user) {
      if (!confirm('确定要删除这个用户吗？')) return
      try {
        await axios.delete(`/api/users/${user.id}`)
        this.message = '用户删除成功'
        this.messageType = 'success'
        this.fetchUsers()
      } catch (e) {
        this.message = '删除用户失败'
        this.messageType = 'error'
      }
    },
    openBulk() {
      this.bulkData = ''
      this.showBulk = true
    },
    closeBulk() {
      this.showBulk = false
    },
    async submitBulk() {
      try {
        const res = await axios.post('/api/users/bulk-import', { bulkData: this.bulkData })
        const { successCount, failCount } = res.data || {}
        this.message = `批量导入完成：成功 ${successCount || 0} 条，失败 ${failCount || 0} 条。`
        this.messageType = 'success'
        this.showBulk = false
        this.fetchUsers()
      } catch (e) {
        this.message = '批量导入失败'
        this.messageType = 'error'
      }
    }
  }
}
</script>

<style scoped>
.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.55);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1050;
}

.modal-dialog {
  max-width: 600px;
  width: 100%;
}

.modal-dialog.modal-lg {
  max-width: 800px;
}
</style>


