<template>
  <div class="container mt-4">
    <div class="page-header d-flex justify-content-between align-items-center mb-3">
      <div>
        <h2 class="mb-1">食谱管理</h2>
        <p class="text-muted mb-0">维护菜品基础信息，可在菜单中复用</p>
      </div>
      <div class="d-flex gap-2">
        <input
          v-model="keyword"
          type="text"
          class="form-control form-control-sm"
          style="width: 220px"
          placeholder="按名称或分类搜索"
        />
        <button class="btn btn-primary btn-sm" @click="openCreate">添加食谱</button>
      </div>
    </div>

    <div v-if="message" class="alert" :class="messageClass">
      {{ message }}
    </div>

    <div class="card">
      <div class="card-body">
        <div v-if="loading" class="alert alert-info text-center">
          加载中...
        </div>
        <div v-else>
          <div v-if="!filteredRecipes.length" class="alert alert-info text-center">
            暂无食谱数据。
          </div>
          <div v-else class="table-responsive">
            <table class="table table-striped align-middle">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>名称</th>
                  <th>分类</th>
                  <th>图片</th>
                  <th>单位</th>
                  <th>单价</th>
                  <th style="width: 150px">操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="r in filteredRecipes" :key="r.id">
                  <td>{{ r.id }}</td>
                  <td>{{ r.name }}</td>
                  <td>{{ r.category }}</td>
                  <td>
                    <div v-if="r.image" class="recipe-thumb">
                      <img 
                        :src="getImageUrl(r.image)" 
                        alt="图片" 
                        @error="handleImageError($event, r)"
                      />
                    </div>
                    <span v-else class="text-muted">无</span>
                  </td>
                  <td>{{ r.unit }}</td>
                  <td>¥{{ r.price.toFixed(2) }}</td>
                  <td>
                    <button class="btn btn-sm btn-outline-primary me-1" @click="openEdit(r)">编辑</button>
                    <button class="btn btn-sm btn-outline-danger" @click="remove(r)">删除</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>

    <!-- 添加/编辑 食谱弹窗 -->
    <div v-if="showForm" class="modal-backdrop">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              {{ editingRecipe.id ? '编辑食谱' : '添加食谱' }}
            </h5>
            <button type="button" class="btn-close" @click="closeForm"></button>
          </div>
          <div class="modal-body">
            <div class="mb-3">
              <label class="form-label">名称</label>
              <input v-model="editingRecipe.name" type="text" class="form-control" required>
            </div>
            <div class="mb-3">
              <label class="form-label">分类</label>
              <input v-model="editingRecipe.category" type="text" class="form-control">
            </div>
            <div class="mb-3">
              <label class="form-label">图片</label>
              <FileUpload
                v-model="editingRecipe.image"
                @uploaded="onImageUploaded"
              />
            </div>
            <div class="mb-3">
              <label class="form-label">单位</label>
              <input v-model="editingRecipe.unit" type="text" class="form-control" placeholder="份 / 碗 / 两 等">
            </div>
            <div class="mb-3">
              <label class="form-label">单价（元）</label>
              <input v-model.number="editingRecipe.price" type="number" min="0" step="0.01" class="form-control">
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" @click="closeForm">取消</button>
            <button class="btn btn-primary" @click="save" :disabled="saving">
              {{ saving ? '保存中...' : '保存' }}
            </button>
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
  name: 'ManagerRecipeManagementPage',
  components: {
    FileUpload
  },
  data() {
    return {
      recipes: [],
      loading: false,
      message: '',
      messageType: 'success',
      showForm: false,
      saving: false,
      editingRecipe: {
        id: null,
        name: '',
        category: '',
        image: '',
        unit: '',
        price: 0
      },
      keyword: '',
      originalImagePath: '' // 保存编辑前的原始图片路径，用于更新时删除旧图片
    }
  },
  computed: {
    messageClass() {
      return this.messageType === 'success' ? 'alert alert-success' : 'alert alert-danger'
    },
    filteredRecipes() {
      const k = this.keyword.trim().toLowerCase()
      if (!k) return this.recipes
      return this.recipes.filter(r =>
        (r.name && r.name.toLowerCase().includes(k)) ||
        (r.category && r.category.toLowerCase().includes(k))
      )
    }
  },
  created() {
    this.fetchRecipes()
  },
  methods: {
    // 获取图片URL（处理相对路径和绝对路径）
    getImageUrl(path) {
      if (!path) return ''
      path = path.trim()
      // 如果是完整URL，直接返回
      if (path.startsWith('http://') || path.startsWith('https://')) {
        return path
      }
      // 如果是相对路径，确保以 / 开头
      return path.startsWith('/') ? path : '/' + path
    },
    // 图片加载错误处理
    handleImageError(event, recipe) {
      console.error('图片加载失败 - 食谱:', recipe.name, '路径:', recipe.image)
      event.target.alt = '图片加载失败'
      event.target.style.border = '2px solid red'
    },
    async fetchRecipes() {
      this.loading = true
      try {
        const res = await axios.get('/api/recipes')
        this.recipes = res.data || []
      } catch (e) {
        this.message = '加载食谱列表失败'
        this.messageType = 'error'
      } finally {
        this.loading = false
      }
    },
    openCreate() {
      this.editingRecipe = {
        id: null,
        name: '',
        category: '',
        image: '',
        unit: '',
        price: 0
      }
      this.originalImagePath = ''
      this.showForm = true
    },
    openEdit(recipe) {
      this.editingRecipe = { ...recipe }
      this.originalImagePath = recipe.image || '' // 保存原始图片路径
      this.showForm = true
    },
    closeForm() {
      this.showForm = false
    },
    onImageUploaded(filePath) {
      // 图片上传成功回调
      console.log('图片上传成功:', filePath)
      this.editingRecipe.image = filePath
    },
    async save() {
      if (!this.editingRecipe.name) {
        this.message = '请填写食谱名称'
        this.messageType = 'error'
        return
      }
      this.saving = true
      try {
        const payload = { ...this.editingRecipe }
        
        // 确保图片路径正确
        if (payload.image) {
          payload.image = payload.image.trim()
          if (payload.image && !payload.image.startsWith('/') && !payload.image.startsWith('http://') && !payload.image.startsWith('https://')) {
            payload.image = '/' + payload.image
          }
        } else {
          payload.image = null
        }
        
        if (this.editingRecipe.id) {
          // 更新食谱：如果图片路径改变了，删除旧图片
          const oldImagePath = this.originalImagePath
          const newImagePath = payload.image
          
          if (oldImagePath && oldImagePath.trim() && oldImagePath !== newImagePath) {
            console.log('图片路径已更改，准备删除旧图片:', oldImagePath)
            try {
              await this.deleteImageFile(oldImagePath)
            } catch (err) {
              console.warn('删除旧图片失败（不影响更新操作）:', err)
            }
          }
          
          await axios.put(`/api/recipes/${this.editingRecipe.id}`, payload)
          this.message = '食谱更新成功'
        } else {
          await axios.post('/api/recipes', payload)
          this.message = '食谱添加成功'
        }
        this.messageType = 'success'
        this.showForm = false
        this.originalImagePath = ''
        this.fetchRecipes()
      } catch (e) {
        console.error('保存食谱失败:', e)
        this.message = '保存食谱失败: ' + (e.response?.data?.message || e.message)
        this.messageType = 'error'
      } finally {
        this.saving = false
      }
    },
    async remove(recipe) {
      if (!confirm(`确定要删除食谱 "${recipe.name}" 吗？`)) return
      try {
        // 先删除食谱对应的图片文件
        if (recipe.image && recipe.image.trim()) {
          console.log('删除食谱时，同时删除图片文件:', recipe.image)
          try {
            await this.deleteImageFile(recipe.image)
          } catch (err) {
            console.warn('删除图片文件失败（继续删除食谱）:', err)
          }
        }
        
        // 删除食谱记录
        await axios.delete(`/api/recipes/${recipe.id}`)
        this.message = '食谱删除成功'
        this.messageType = 'success'
        this.fetchRecipes()
      } catch (e) {
        console.error('删除食谱失败:', e)
        this.message = '删除食谱失败: ' + (e.response?.data?.message || e.message)
        this.messageType = 'error'
      }
    },
    // 删除图片文件
    async deleteImageFile(filePath) {
      if (!filePath || !filePath.trim()) {
        return
      }
      
      try {
        const response = await axios.delete('/api/files/delete', {
          params: {
            filePath: filePath
          }
        })
        
        if (response.data.status === 'success') {
          console.log('✓ 图片文件删除成功:', filePath)
        } else {
          console.warn('图片文件删除失败:', response.data.message)
        }
      } catch (error) {
        console.error('删除图片文件时发生错误:', error)
        throw error
      }
    }
  }
}
</script>

<style scoped>
.page {
  padding: 24px;
}

.recipe-thumb img {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid #eee;
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
</style>