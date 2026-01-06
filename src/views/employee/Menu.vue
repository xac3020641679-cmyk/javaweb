<template>
  <div class="menu-page">
    <div class="page-header">
      <h1>今日菜单</h1>
      <p class="text-muted">请选择心仪菜品进行订餐</p>
    </div>

    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p>加载中...</p>
    </div>

    <div v-else>
      <div v-if="menus.length === 0" class="empty-state">
        <div class="empty-icon">🍽️</div>
        <h3>今日暂无可用菜单</h3>
        <p>可能的原因：</p>
        <ul>
          <li>没有菜单被设置为活动状态</li>
          <li>活动菜单的日期不是今天或未来</li>
          <li>活动菜单中没有添加菜品</li>
        </ul>
      </div>

      <div v-for="menu in menus" :key="menu.id" class="menu-card">
        <div class="menu-card-header">
          <div class="menu-header-content">
            <h3>{{ menu.name }}</h3>
            <span class="menu-date">📅 {{ formatDate(menu.date) }}</span>
          </div>
        </div>
        <div class="menu-card-body">
          <div v-if="getMenuItems(menu.id).length" class="menu-items-grid">
            <div
              v-for="item in getMenuItems(menu.id)"
              :key="item.id"
              class="menu-item-card"
            >
              <div class="menu-item-image-container">
                <div v-if="item.image" class="image-wrapper">
                  <img
                    :src="item.image"
                    class="menu-item-image"
                    :alt="item.name"
                    @error="handleImageError"
                  />
                </div>
                <div v-else class="image-placeholder">
                  <span class="placeholder-icon">🍽️</span>
                  <span class="placeholder-text">暂无图片</span>
                </div>
              </div>
              <div class="menu-item-content">
                <h5 class="menu-item-title">{{ item.name }}</h5>
                <div class="menu-item-info">
                  <div class="info-row">
                    <span class="info-label">单价：</span>
                    <span class="info-value price">¥{{ item.price }}</span>
                  </div>
                  <div class="info-row">
                    <span class="info-label">单位：</span>
                    <span class="info-value">{{ item.unit }}</span>
                  </div>
                </div>
                <button class="btn btn-primary order-btn" @click="goNewOrder(item.id)">
                  <span>订购</span>
                  <span class="btn-icon">→</span>
                </button>
              </div>
            </div>
          </div>
          <div v-else class="empty-menu-items">
            <p>该菜单暂无菜品</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'EmployeeMenu',
  data() {
    return {
      loading: false,
      menus: [],
      itemsMap: {} // { menuId: MenuItem[] }
    }
  },
  created() {
    this.fetchTodayMenus()
  },
  methods: {
    async fetchTodayMenus() {
      this.loading = true
      try {
        const res = await axios.get('/api/menus/today')
        this.menus = res.data.menus || []
        this.itemsMap = res.data.itemsMap || {}
      } catch (e) {
        // 忽略详细错误，只在控制台输出
        // eslint-disable-next-line no-console
        console.error(e)
      } finally {
        this.loading = false
      }
    },
    getMenuItems(menuId) {
      return this.itemsMap[menuId] || []
    },
    formatDate(dateStr) {
      if (!dateStr) return ''
      return String(dateStr).substring(0, 10)
    },
    goNewOrder(itemId) {
      this.$router.push({
        path: '/employee/new-order',
        query: { itemId }
      })
    },
    handleImageError(event) {
      // 图片加载失败时，显示占位符
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
    }
  }
}
</script>

<style scoped>
.menu-page {
  padding: 32px;
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  text-align: center;
  margin-bottom: 48px;
  padding-bottom: 24px;
  border-bottom: 2px solid rgba(102, 126, 234, 0.1);
}

.page-header h1 {
  font-size: 2.5rem;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 12px;
}

.page-header .text-muted {
  font-size: 16px;
  color: #718096;
}

/* 加载状态 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  text-align: center;
}

.loading-spinner {
  width: 50px;
  height: 50px;
  border: 4px solid rgba(102, 126, 234, 0.1);
  border-top-color: #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 20px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  border: 2px solid rgba(102, 126, 234, 0.1);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
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
  margin-bottom: 16px;
}

.empty-state p {
  color: #718096;
  margin-bottom: 12px;
}

.empty-state ul {
  display: inline-block;
  text-align: left;
  color: #4a5568;
  line-height: 2;
}

/* 菜单卡片 */
.menu-card {
  margin-bottom: 40px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  transition: all 0.3s ease;
}

.menu-card:hover {
  box-shadow: 0 12px 48px rgba(102, 126, 234, 0.15);
  transform: translateY(-4px);
}

.menu-card-header {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.08) 0%, rgba(118, 75, 162, 0.08) 100%);
  padding: 24px 32px;
  border-bottom: 2px solid rgba(102, 126, 234, 0.1);
}

.menu-header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
}

.menu-card-header h3 {
  font-size: 1.75rem;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0;
}

.menu-date {
  font-size: 14px;
  color: #667eea;
  font-weight: 600;
  background: rgba(102, 126, 234, 0.1);
  padding: 6px 16px;
  border-radius: 20px;
}

.menu-card-body {
  padding: 32px;
}

/* 菜品网格 */
.menu-items-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
}

/* 菜品卡片 */
.menu-item-card {
  background: #ffffff;
  border-radius: 20px;
  border: 2px solid rgba(102, 126, 234, 0.1);
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  flex-direction: column;
  position: relative;
}

.menu-item-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  transform: scaleX(0);
  transition: transform 0.4s ease;
  z-index: 1;
}

.menu-item-card:hover::before {
  transform: scaleX(1);
}

.menu-item-card:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: 0 16px 40px rgba(102, 126, 234, 0.25);
  border-color: rgba(102, 126, 234, 0.3);
}

/* 图片容器 */
.menu-item-image-container {
  width: 100%;
  height: 240px;
  overflow: hidden;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
  position: relative;
}

.image-wrapper {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.menu-item-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
  transition: transform 0.5s cubic-bezier(0.4, 0, 0.2, 1);
  background: #f8f9fa;
}

.menu-item-card:hover .menu-item-image {
  transform: scale(1.08);
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
  font-size: 3rem;
  margin-bottom: 8px;
  opacity: 0.6;
}

.placeholder-text {
  font-size: 14px;
  font-weight: 500;
}

/* 菜品内容 */
.menu-item-content {
  padding: 20px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.menu-item-title {
  font-size: 1.25rem;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 16px;
  line-height: 1.4;
  min-height: 3.5rem;
}

.menu-item-info {
  margin-bottom: 20px;
  flex: 1;
}

.info-row {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  font-size: 14px;
}

.info-label {
  color: #718096;
  font-weight: 500;
  margin-right: 8px;
  min-width: 50px;
}

.info-value {
  color: #1a1a1a;
  font-weight: 600;
}

.info-value.price {
  color: #667eea;
  font-size: 16px;
  font-weight: 700;
}

/* 订购按钮 */
.order-btn {
  width: 100%;
  padding: 12px 20px;
  font-size: 15px;
  font-weight: 600;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.3s ease;
  margin-top: auto;
}

.order-btn .btn-icon {
  transition: transform 0.3s ease;
}

.menu-item-card:hover .order-btn .btn-icon {
  transform: translateX(4px);
}

.empty-menu-items {
  text-align: center;
  padding: 40px 20px;
  color: #718096;
  font-size: 16px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .menu-items-grid {
    grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
    gap: 20px;
  }
}

@media (max-width: 768px) {
  .menu-page {
    padding: 16px;
  }

  .page-header {
    margin-bottom: 32px;
    padding-bottom: 20px;
  }

  .page-header h1 {
    font-size: 2rem;
  }

  .menu-card-header {
    padding: 20px;
  }

  .menu-card-header h3 {
    font-size: 1.5rem;
  }

  .menu-card-body {
    padding: 20px;
  }

  .menu-items-grid {
    grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
    gap: 16px;
  }

  .menu-item-image-container {
    height: 200px;
  }

  .menu-item-title {
    font-size: 1.1rem;
    min-height: auto;
  }
}

@media (max-width: 480px) {
  .menu-items-grid {
    grid-template-columns: 1fr;
  }

  .menu-header-content {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>