<template>
  <div class="file-upload">
    <div class="upload-controls mb-2">
      <input
        ref="fileInput"
        type="file"
        accept="image/*"
        class="file-input-hidden"
        @change="handleFileSelect"
      />
      <button
        type="button"
        class="btn btn-outline-primary"
        @click="triggerFileInput"
        :disabled="uploading"
      >
        <span v-if="uploading">上传中...</span>
        <span v-else>选择图片</span>
      </button>
    </div>
    <div v-if="imageUrl" class="image-preview mb-2">
      <img :src="imageUrl" alt="预览" class="preview-image" />
      <button
        type="button"
        class="btn btn-sm btn-danger mt-2"
        @click="clearImage"
      >
        删除图片
      </button>
    </div>
    <div class="image-url-input mt-2">
      <input
        v-model="imagePath"
        type="text"
        class="form-control form-control-sm"
        placeholder="图片地址（上传后自动填充，也可手动输入）"
        @input="onPathChange"
      />
    </div>
    <div v-if="error" class="text-danger small mt-1">{{ error }}</div>
    <div v-if="uploading" class="text-info small mt-1">正在上传...</div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'FileUpload',
  props: {
    value: {
      type: String,
      default: ''
    },
    maxSize: {
      type: Number,
      default: 5 * 1024 * 1024 // 默认5MB
    }
  },
  data() {
    return {
      uploading: false,
      error: '',
      localImageUrl: '', // 本地预览URL（使用URL.createObjectURL创建）
      serverImagePath: '', // 服务器返回的图片路径
      imagePath: ''
    }
  },
  computed: {
    imageUrl() {
      // 优先使用本地预览URL（文件选择后立即显示，上传成功后也保持显示直到服务器图片加载成功）
      if (this.localImageUrl && this.localImageUrl.startsWith('blob:')) {
        return this.localImageUrl
      }
      // 使用服务器返回的路径（上传成功后，且本地预览已清理）
      if (this.serverImagePath) {
        return this.getImageUrl(this.serverImagePath)
      }
      // 使用传入的value（已保存的图片路径）
      if (this.value) {
        return this.getImageUrl(this.value)
      }
      return ''
    }
  },
  watch: {
    value(newVal) {
      if (!newVal) {
        // 清理本地预览URL
        if (this.localImageUrl && this.localImageUrl.startsWith('blob:')) {
          URL.revokeObjectURL(this.localImageUrl)
        }
        this.localImageUrl = ''
        this.serverImagePath = ''
        this.imagePath = ''
      } else {
        this.imagePath = newVal
        this.serverImagePath = newVal
      }
    }
  },
  mounted() {
    if (this.value) {
      this.imagePath = this.value
      this.serverImagePath = this.value
    }
  },
  beforeUnmount() {
    // 组件销毁前清理本地预览URL
    if (this.localImageUrl && this.localImageUrl.startsWith('blob:')) {
      URL.revokeObjectURL(this.localImageUrl)
    }
  },
  methods: {
    triggerFileInput() {
      this.$refs.fileInput.click()
    },
    // 获取图片URL（处理相对路径和绝对路径）
    getImageUrl(path) {
      if (!path) return ''
      // 如果是完整URL，直接返回
      if (path.startsWith('http://') || path.startsWith('https://')) {
        return path
      }
      // 如果是相对路径，确保以 / 开头
      return path.startsWith('/') ? path : '/' + path
    },
    async handleFileSelect(event) {
      const file = event.target.files[0]
      if (!file) {
        return
      }

      // 验证文件类型
      if (!file.type.startsWith('image/')) {
        this.error = '请选择图片文件'
        return
      }

      // 验证文件大小
      if (file.size > this.maxSize) {
        this.error = `文件大小不能超过 ${(this.maxSize / 1024 / 1024).toFixed(1)}MB`
        return
      }

      this.error = ''
      
      // 保存旧图片路径（用于上传成功后删除）
      const oldImagePath = this.value || this.serverImagePath || this.imagePath
      
      // 立即创建本地预览（使用URL.createObjectURL）
      // 先清理之前的预览URL
      if (this.localImageUrl && this.localImageUrl.startsWith('blob:')) {
        URL.revokeObjectURL(this.localImageUrl)
      }
      this.localImageUrl = URL.createObjectURL(file)
      this.uploading = true

      try {
        const formData = new FormData()
        formData.append('file', file)

        const response = await axios.post('/api/files/upload', formData, {
          headers: {
            'Content-Type': 'multipart/form-data'
          },
          withCredentials: true
        })

        if (response.data.status === 'success') {
          const filePath = response.data.filePath
          
          // 如果存在旧图片且与新图片不同，删除旧图片
          if (oldImagePath && oldImagePath.trim() && oldImagePath !== filePath) {
            this.deleteOldImage(oldImagePath).catch(err => {
              console.warn('删除旧图片失败（不影响新图片上传）:', err)
            })
          }
          
          // 使用服务器返回的路径
          this.serverImagePath = filePath
          this.imagePath = filePath
          
          // 重要：先更新内部状态，再触发事件
          this.$emit('input', filePath)
          this.$emit('uploaded', filePath)
          
          // 测试服务器图片是否可以加载，如果可以则清理本地预览
          this.testAndSwitchToServerImage(filePath)
        } else {
          // 上传失败，清理本地预览
          if (this.localImageUrl && this.localImageUrl.startsWith('blob:')) {
            URL.revokeObjectURL(this.localImageUrl)
            this.localImageUrl = ''
          }
          this.error = response.data.message || '上传失败'
        }
      } catch (error) {
        console.error('上传失败:', error)
        // 上传失败，清理本地预览
        if (this.localImageUrl && this.localImageUrl.startsWith('blob:')) {
          URL.revokeObjectURL(this.localImageUrl)
          this.localImageUrl = ''
        }
        this.error = error.response?.data?.message || '上传失败，请稍后重试'
      } finally {
        this.uploading = false
        // 清空文件输入，允许重复选择同一文件
        event.target.value = ''
      }
    },
    async clearImage() {
      // 如果有已上传的图片，先删除服务器上的文件
      const imagePathToDelete = this.value || this.serverImagePath || this.imagePath
      if (imagePathToDelete && imagePathToDelete.trim()) {
        try {
          await this.deleteOldImage(imagePathToDelete)
        } catch (err) {
          console.warn('删除图片失败（继续清理前端状态）:', err)
        }
      }
      
      // 清理本地预览URL
      if (this.localImageUrl && this.localImageUrl.startsWith('blob:')) {
        URL.revokeObjectURL(this.localImageUrl)
      }
      this.localImageUrl = ''
      this.serverImagePath = ''
      this.imagePath = ''
      this.$emit('input', '')
      this.$emit('cleared')
    },
    // 删除旧图片
    async deleteOldImage(filePath) {
      if (!filePath || !filePath.trim()) {
        return
      }
      
      try {
        const response = await axios.delete('/api/files/delete', {
          params: {
            filePath: filePath
          },
          withCredentials: true
        })
        
        if (response.data.status !== 'success') {
          console.warn('旧图片删除失败:', response.data.message)
        }
      } catch (error) {
        console.error('删除旧图片时发生错误:', error)
        throw error
      }
    },
    onPathChange() {
      // 当用户手动修改图片地址时，更新值
      this.$emit('input', this.imagePath)
      // 如果清空了地址，也清空预览
      if (!this.imagePath) {
        if (this.localImageUrl && this.localImageUrl.startsWith('blob:')) {
          URL.revokeObjectURL(this.localImageUrl)
        }
        this.localImageUrl = ''
        this.serverImagePath = ''
      } else {
        this.serverImagePath = this.imagePath
      }
    },
    // 测试服务器图片是否可以加载，如果可以则切换到服务器图片
    testAndSwitchToServerImage(serverPath) {
      const img = new Image()
      const serverUrl = this.getImageUrl(serverPath)
      
      // 设置超时，如果3秒内无法加载，保持本地预览
      const timeout = setTimeout(() => {
        console.warn('服务器图片加载超时，保持本地预览:', serverUrl)
        img.onload = null
        img.onerror = null
      }, 3000)
      
      img.onload = () => {
        clearTimeout(timeout)
        // 服务器图片加载成功，延迟清理本地预览（给用户一点时间看到切换）
        setTimeout(() => {
          if (this.localImageUrl && this.localImageUrl.startsWith('blob:')) {
            URL.revokeObjectURL(this.localImageUrl)
            this.localImageUrl = ''
          }
        }, 500)
      }
      
      img.onerror = () => {
        clearTimeout(timeout)
        // 服务器图片加载失败，保持本地预览
        console.warn('服务器图片加载失败，保持本地预览:', serverUrl)
      }
      
      // 开始加载测试
      img.src = serverUrl
    }
  }
}
</script>

<style scoped>
.file-upload {
  width: 100%;
}

.upload-controls {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.image-preview {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  margin-bottom: 16px;
  padding: 16px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
  border-radius: 12px;
  border: 2px dashed rgba(102, 126, 234, 0.2);
  transition: all 0.3s ease;
}

.image-preview:hover {
  border-color: rgba(102, 126, 234, 0.4);
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.08) 0%, rgba(118, 75, 162, 0.08) 100%);
}

.preview-image {
  max-width: 300px;
  max-height: 300px;
  object-fit: cover;
  border-radius: 12px;
  border: 2px solid rgba(102, 126, 234, 0.2);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.preview-image:hover {
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.2);
  transform: scale(1.02);
}

.image-url-input {
  width: 100%;
  margin-top: 16px;
}

.image-url-input input {
  width: 100%;
  padding: 12px 16px;
  border: 2px solid #e2e8f0;
  border-radius: 10px;
  font-size: 14px;
  transition: all 0.2s ease;
  background: #ffffff;
}

.image-url-input input:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
  outline: none;
}

.image-url-input input:hover {
  border-color: #cbd5e0;
}

.file-input-hidden {
  position: absolute;
  width: 0;
  height: 0;
  opacity: 0;
  overflow: hidden;
  z-index: -1;
}

.text-danger {
  color: #f5576c;
  font-size: 13px;
  margin-top: 8px;
  padding: 8px 12px;
  background: linear-gradient(135deg, rgba(245, 87, 108, 0.1) 0%, rgba(240, 147, 251, 0.1) 100%);
  border-radius: 8px;
  border-left: 3px solid #f5576c;
}

.text-info {
  color: #4facfe;
  font-size: 13px;
  margin-top: 8px;
  padding: 8px 12px;
  background: linear-gradient(135deg, rgba(79, 172, 254, 0.1) 0%, rgba(0, 242, 254, 0.1) 100%);
  border-radius: 8px;
  border-left: 3px solid #4facfe;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.text-info::before {
  content: '⏳';
  font-size: 16px;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>
