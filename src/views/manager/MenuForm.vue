<template>
  <div class="container mt-4">
    <h2>{{ isEdit ? '编辑菜单' : '新建菜单' }}</h2>

    <div v-if="message" class="alert" :class="messageClass">
      {{ message }}
    </div>

    <div class="card mb-4">
      <div class="card-body">
        <form @submit.prevent="saveMenu">
          <div class="mb-3">
            <label class="form-label">菜单名称</label>
            <input v-model="menu.name" type="text" class="form-control" required />
          </div>
          <div class="mb-3">
            <label class="form-label">菜单日期</label>
            <input v-model="menu.date" type="date" class="form-control" required />
          </div>
          <div class="form-check mb-3">
            <input
              id="active"
              v-model="menu.active"
              type="checkbox"
              class="form-check-input"
            />
            <label class="form-check-label" for="active">设为当前活动菜单</label>
          </div>
          <button type="submit" class="btn btn-primary me-2" :disabled="saving">
            {{ saving ? '保存中...' : '保存菜单' }}
          </button>
          <button type="button" class="btn btn-secondary" @click="goBack">
            返回菜单列表
          </button>
        </form>
      </div>
    </div>

    <div v-if="isEdit" class="card">
      <div class="card-header d-flex justify-content-between align-items-center">
        <h3 class="mb-0">菜单菜品</h3>
        <button class="btn btn-sm btn-primary" @click="openItemForm()">添加菜品</button>
      </div>
      <div class="card-body">
        <div v-if="items.length === 0" class="alert alert-info text-center">
          该菜单暂无菜品。
        </div>
        <div v-else class="table-responsive">
          <table class="table table-striped">
            <thead>
              <tr>
                <th>菜名</th>
                <th>单位</th>
                <th>单价</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in items" :key="item.id">
                <td>{{ item.name || '未命名' }}</td>
                <td>{{ item.unit || '-' }}</td>
                <td>¥{{ (item.price || 0).toFixed(2) }}</td>
                <td>
                  <button class="btn btn-sm btn-primary me-1" @click="openItemForm(item)">编辑</button>
                  <button class="btn btn-sm btn-danger" @click="removeItem(item)">删除</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- 菜品添加/编辑弹窗 -->
    <div v-if="showItemModal" class="modal-backdrop">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              {{ editingItem.id ? '编辑菜单菜品' : '添加菜单菜品' }}
            </h5>
            <button type="button" class="btn-close" @click="closeItemModal"></button>
          </div>
          <div class="modal-body">
            <div class="mb-3">
              <label class="form-label">从食谱选择（可选）</label>
              <select 
                v-model="selectedRecipeId" 
                class="form-select"
                @change="onRecipeSelected"
              >
                <option value="0">-- 不选择食谱，手动填写 --</option>
                <option v-for="recipe in recipes" :key="recipe.id" :value="recipe.id">
                  {{ recipe.name }} ({{ recipe.category || '未分类' }})
                </option>
              </select>
              <small class="form-text text-muted">选择食谱后会自动填充菜名、单位、单价等信息，您也可以手动修改</small>
              
              <!-- 食谱预览 -->
              <div v-if="selectedRecipeId > 0 && selectedRecipe" class="recipe-preview mt-3">
                <div class="recipe-preview-card">
                  <div class="recipe-image-container">
                    <div v-if="selectedRecipe.image" class="image-wrapper">
                      <img
                        :src="selectedRecipe.image"
                        class="recipe-preview-image"
                        :alt="selectedRecipe.name"
                        @error="handleRecipeImageError"
                      />
                    </div>
                    <div v-else class="image-placeholder">
                      <span class="placeholder-icon">🍽️</span>
                      <span class="placeholder-text">暂无图片</span>
                    </div>
                  </div>
                  <div class="recipe-preview-info">
                    <h6 class="recipe-preview-name">{{ selectedRecipe.name }}</h6>
                    <div class="recipe-preview-details">
                      <span class="detail-item">单位：{{ selectedRecipe.unit || '-' }}</span>
                      <span class="detail-item">单价：¥{{ (selectedRecipe.price || 0).toFixed(2) }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <div class="mb-3">
              <label class="form-label">菜名 <span class="text-danger">*</span></label>
              <input v-model="editingItem.name" type="text" class="form-control" required />
            </div>
            <div class="mb-3">
              <label class="form-label">单位 <span class="text-danger">*</span></label>
              <input v-model="editingItem.unit" type="text" class="form-control" required />
            </div>
            <div class="mb-3">
              <label class="form-label">单价 <span class="text-danger">*</span></label>
              <input
                v-model.number="editingItem.price"
                type="number"
                min="0"
                step="0.01"
                class="form-control"
                required
              />
            </div>
            <div class="mb-3">
              <label class="form-label">图片</label>
              <FileUpload
                v-model="editingItem.image"
                @uploaded="onImageUploaded"
              />
              <div class="form-text mt-2">
                支持 JPG、PNG 等图片格式，最大 5MB。上传后会自动填充图片地址，也可以手动修改。
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" @click="closeItemModal">取消</button>
            <button class="btn btn-primary" @click="saveItem">保存</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import FileUpload from '@/components/FileUpload.vue'

export default {
  name: 'ManagerMenuFormPage',
  components: {
    FileUpload
  },
  props: {
    id: {
      type: String,
      required: false
    }
  },
  data() {
    const today = new Date()
    const y = today.getFullYear()
    const m = String(today.getMonth() + 1).padStart(2, '0')
    const d = String(today.getDate()).padStart(2, '0')
    return {
      menu: {
        id: null,
        name: '',
        date: `${y}-${m}-${d}`,
        active: false
      },
      items: [],
      recipes: [],
      message: '',
      messageType: 'success',
      saving: false,
      showItemModal: false,
      selectedRecipeId: 0,
      editingItem: {
        id: null,
        name: '',
        unit: '',
        price: 0,
        recipeId: 0,
        image: ''
      }
    }
  },
  computed: {
    isEdit() {
      return !!(this.menu.id || this.id || this.$route.params.id)
    },
    messageClass() {
      return this.messageType === 'success' ? 'alert alert-success' : 'alert alert-danger'
    },
    selectedRecipe() {
      if (this.selectedRecipeId > 0) {
        return this.recipes.find(r => r.id === this.selectedRecipeId) || null
      }
      return null
    }
  },
  created() {
    this.loadRecipes()
    if (this.isEdit) {
      this.loadMenu()
    }
  },
  watch: {
    '$route.params.id'(newId) {
      if (newId && this.isEdit) {
        this.loadMenu()
      }
    }
  },
  methods: {
    async loadMenu() {
      // 确保菜单ID是数字类型
      let menuId = this.menu.id || this.id || this.$route.params.id
      if (menuId) {
        menuId = parseInt(menuId, 10)
      }
      if (!menuId || isNaN(menuId)) {
        console.warn('无法加载菜单：缺少菜单ID', { 
          menuId: this.menu.id, 
          id: this.id, 
          routeId: this.$route.params.id 
        })
        return
      }
      try {
        const res = await axios.get(`/api/menus/${menuId}`)
        const { menu, items } = res.data
        this.menu = {
          id: menu.id,
          name: menu.name,
          date: String(menu.date).substring(0, 10),
          active: menu.active
        }
        this.items = items || []
      } catch (e) {
        console.error('加载菜单信息失败:', e)
        this.message = `加载菜单信息失败: ${e.response?.data?.message || e.message || '未知错误'}`
        this.messageType = 'error'
      }
    },
    async saveMenu() {
      this.saving = true
      this.message = ''
      try {
        const payload = {
          name: this.menu.name,
          date: this.menu.date,
          active: this.menu.active
        }
        if (this.isEdit) {
          const menuId = this.id || this.$route.params.id
          await axios.put(`/api/menus/${menuId}`, payload)
          this.message = '菜单更新成功'
          this.messageType = 'success'
          this.loadMenu()
        } else {
          const res = await axios.post('/api/menus', payload)
          const menuId = res.data.id
          this.message = '菜单创建成功，现在可以添加菜品了'
          this.messageType = 'success'
          // 更新菜单ID并切换到编辑模式
          this.menu.id = menuId
          // 更新路由以包含菜单ID
          this.$router.replace(`/manager/menu-form/${menuId}`)
          // 加载菜单数据以显示菜品管理区域
          await this.loadMenu()
        }
      } catch (e) {
        this.message = '保存菜单失败'
        this.messageType = 'error'
      } finally {
        this.saving = false
      }
    },
    goBack() {
      this.$router.push('/manager/menu-management')
    },
    async loadRecipes() {
      try {
        const res = await axios.get('/api/recipes')
        this.recipes = res.data || []
      } catch (e) {
        console.error('加载食谱列表失败:', e)
        this.recipes = []
      }
    },
    onRecipeSelected() {
      if (this.selectedRecipeId > 0) {
        const recipe = this.recipes.find(r => r.id === this.selectedRecipeId)
        if (recipe) {
          // 自动填充信息，但保留用户已输入的内容（如果已输入）
          if (!this.editingItem.name || this.editingItem.name.trim() === '') {
            this.editingItem.name = recipe.name
          }
          if (!this.editingItem.unit || this.editingItem.unit.trim() === '') {
            this.editingItem.unit = recipe.unit || ''
          }
          if (!this.editingItem.price || this.editingItem.price === 0) {
            this.editingItem.price = recipe.price || 0
          }
          if (!this.editingItem.image || this.editingItem.image.trim() === '') {
            this.editingItem.image = recipe.image || ''
          }
          this.editingItem.recipeId = recipe.id
        }
      } else {
        // 取消选择食谱
        this.editingItem.recipeId = 0
      }
    },
    openItemForm(item) {
      if (item) {
        // 编辑模式
        this.editingItem = { 
          ...item,
          recipeId: item.recipeId || 0
        }
        this.selectedRecipeId = item.recipeId || 0
      } else {
        // 新建模式
        this.editingItem = {
          id: null,
          name: '',
          unit: '',
          price: 0,
          recipeId: 0,
          image: ''
        }
        this.selectedRecipeId = 0
      }
      this.showItemModal = true
    },
    onImageUploaded() {
      // 图片上传成功回调
    },
    handleRecipeImageError(event) {
      const img = event.target
      const container = img.closest('.image-wrapper')
      if (container) {
        container.innerHTML = `
          <div class="image-placeholder">
            <span class="placeholder-icon">🍽️</span>
            <span class="placeholder-text">图片加载失败</span>
          </div>
        `
      }
    },
    closeItemModal() {
      this.showItemModal = false
      this.selectedRecipeId = 0
      this.editingItem = {
        id: null,
        name: '',
        unit: '',
        price: 0,
        recipeId: 0,
        image: ''
      }
    },
    async saveItem() {
      // 确保菜单ID是数字类型
      let menuId = this.menu.id || this.id || this.$route.params.id
      if (menuId) {
        menuId = parseInt(menuId, 10)
      }
      if (!menuId || isNaN(menuId)) {
        this.message = '请先保存菜单，再编辑菜品'
        this.messageType = 'error'
        this.showItemModal = false
        return
      }
      // 验证必填字段
      if (!this.editingItem.name || !this.editingItem.name.trim()) {
        this.message = '请输入菜名'
        this.messageType = 'error'
        return
      }
      if (!this.editingItem.unit || !this.editingItem.unit.trim()) {
        this.message = '请输入单位'
        this.messageType = 'error'
        return
      }
      // 允许单价为 0（免费菜品），仅禁止负数或非数字
      const price = Number(this.editingItem.price)
      if (Number.isNaN(price) || price < 0) {
        this.message = '请输入有效的单价（可以为 0 或正数）'
        this.messageType = 'error'
        return
      }
      try {
        if (this.editingItem.id) {
          await axios.put(`/api/menus/items/${this.editingItem.id}`, {
            menuId,
            name: this.editingItem.name,
            unit: this.editingItem.unit,
            price: this.editingItem.price,
            recipeId: this.editingItem.recipeId || 0,
            image: this.editingItem.image || ''
          })
        } else {
          await axios.post(`/api/menus/${menuId}/items`, {
            menuId,
            name: this.editingItem.name,
            unit: this.editingItem.unit,
            price: this.editingItem.price,
            recipeId: this.editingItem.recipeId || 0,
            image: this.editingItem.image || ''
          })
        }
        this.message = '菜单菜品已保存'
        this.messageType = 'success'
        this.showItemModal = false
        
        // 确保菜单ID已设置（使用保存时使用的menuId）
        this.menu.id = menuId
        
        // 清空编辑项
        this.editingItem = {
          id: null,
          name: '',
          unit: '',
          price: 0,
          recipeId: 0,
          image: ''
        }
        this.selectedRecipeId = 0
        
        // 等待一小段时间确保数据库已更新，然后重新加载
        await new Promise(resolve => setTimeout(resolve, 300))
        
        // 直接调用API重新加载菜单数据，确保使用正确的menuId
        try {
          const res = await axios.get(`/api/menus/${menuId}`)
          const { menu, items } = res.data
          this.menu = {
            id: menu.id,
            name: menu.name,
            date: String(menu.date).substring(0, 10),
            active: menu.active
          }
          // 确保items是数组
          if (Array.isArray(items)) {
            this.items = items
          } else if (items) {
            this.items = [items]
          } else {
            this.items = []
          }
          
          // 如果还是没有数据，显示警告
          if (this.items.length === 0) {
            console.warn('警告：保存后菜品列表仍为空，菜单ID:', menuId)
            console.warn('完整响应数据:', JSON.stringify(res.data, null, 2))
            this.message = '菜品已保存，但刷新后未找到数据。请检查后端日志或手动刷新页面。'
            this.messageType = 'warning'
          } else {
            // 成功显示数据
            this.message = `菜品已保存，共 ${this.items.length} 个菜品`
            this.messageType = 'success'
          }
        } catch (e) {
          console.error('保存后重新加载菜单失败:', e)
          this.message = `菜品已保存，但刷新数据失败: ${e.response?.data?.message || e.message}`
          this.messageType = 'error'
        }
      } catch (e) {
        console.error('保存菜单菜品失败:', e)
        const errorMsg = e.response?.data?.message || e.message || '保存菜单菜品失败'
        this.message = `保存菜单菜品失败: ${errorMsg}`
        this.messageType = 'error'
      }
    },
    async removeItem(item) {
      if (!confirm(`确定要删除菜品 "${item.name}" 吗？`)) return
      try {
        await axios.delete(`/api/menus/items/${item.id}`)
        this.message = '菜单菜品已删除'
        this.messageType = 'success'
        this.loadMenu()
      } catch (e) {
        this.message = '删除菜单菜品失败'
        this.messageType = 'error'
      }
    }
  }
}
</script>

<style scoped>
.page {
  padding: 24px;
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
  max-width: 600px;
  width: 100%;
}

/* 食谱预览 */
.recipe-preview {
  margin-top: 16px;
}

.recipe-preview-card {
  background: rgba(255, 255, 255, 0.95);
  border: 2px solid rgba(102, 126, 234, 0.2);
  border-radius: 12px;
  padding: 16px;
  display: flex;
  gap: 16px;
  align-items: center;
  transition: all 0.3s ease;
}

.recipe-preview-card:hover {
  border-color: rgba(102, 126, 234, 0.4);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
}

.recipe-image-container {
  width: 120px;
  height: 120px;
  flex-shrink: 0;
  border-radius: 10px;
  overflow: hidden;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
}

.image-wrapper {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.recipe-preview-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
  background: #f8f9fa;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
  color: #718096;
}

.placeholder-icon {
  font-size: 2rem;
  margin-bottom: 4px;
  opacity: 0.6;
}

.placeholder-text {
  font-size: 12px;
  font-weight: 500;
}

.recipe-preview-info {
  flex: 1;
  min-width: 0;
}

.recipe-preview-name {
  font-size: 1.1rem;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 8px;
  line-height: 1.3;
}

.recipe-preview-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-item {
  font-size: 0.9rem;
  color: #4a5568;
  font-weight: 500;
}

.detail-item:first-child {
  color: #667eea;
  font-weight: 600;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .recipe-preview-card {
    flex-direction: column;
    text-align: center;
  }

  .recipe-image-container {
    width: 100px;
    height: 100px;
  }

  .recipe-preview-details {
    align-items: center;
  }
}
</style>