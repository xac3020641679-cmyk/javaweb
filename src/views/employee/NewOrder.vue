<template>
  <div class="container mt-4">
    <div class="page-header d-flex justify-content-between align-items-center mb-3">
      <h2 class="mb-0">新建订单</h2>
      <small v-if="orderDate" class="text-muted">
        当前订餐日期：{{ orderDate }}，截止时间：{{ orderDeadline || '未配置' }}
      </small>
    </div>

    <div v-if="message" class="alert" :class="messageClass">
      {{ message }}
    </div>

    <div v-if="loading" class="alert alert-info">加载中...</div>

    <div v-else>
      <div
        class="alert mb-3"
        :class="deadlinePassed || hasOrderedToday ? 'alert-warning' : 'alert-info'"
      >
        <strong>提示：</strong>
        <p class="mb-1">
          请选择菜品和数量后提交订单。系统会自动判断是否超过订餐截止时间，或当天是否已经下单。
        </p>
        <p class="mb-0" v-if="hasOrderedToday">
          您今天已经创建过订单，如需修改请联系管理员。
        </p>
        <p class="mb-0" v-else-if="deadlinePassed">
          已超过当前订餐日期的截止时间（{{ orderDeadline }}），如需订餐请等待下一订餐日。
        </p>
      </div>

      <div class="mb-4 new-order-form">
        <!-- 添加菜品区域 -->
        <div class="card mb-3">
          <div class="card-header">
            <h5 class="mb-0">选择菜品</h5>
          </div>
          <div class="card-body">
            <!-- 菜品选择区域 -->
            <div v-if="menus.length > 0" class="menu-selection-area">
              <div v-for="menu in menus" :key="menu.id" class="menu-group mb-4">
                <h6 class="menu-group-title">
                  {{ menu.name }}（{{ formatDate(menu.date) }}）
                </h6>
                <div class="menu-items-grid">
                  <div
                    v-for="item in getMenuItems(menu.id)"
                    :key="item.id"
                    class="menu-item-select-card"
                    :class="{ 'selected': selectedMenuItemId == item.id }"
                    @click="selectMenuItem(item.id)"
                  >
                    <div class="item-image-container">
                      <div v-if="item.image" class="image-wrapper">
                        <img
                          :src="item.image"
                          class="item-image"
                          :alt="item.name"
                          @error="handleImageError"
                        />
                      </div>
                      <div v-else class="image-placeholder">
                        <span class="placeholder-icon">🍽️</span>
                      </div>
                    </div>
                    <div class="item-info">
                      <h6 class="item-name">{{ item.name }}</h6>
                      <div class="item-price">¥{{ item.price }}/{{ item.unit }}</div>
                    </div>
                    <div v-if="selectedMenuItemId == item.id" class="selected-badge">
                      ✓ 已选择
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <div v-else class="alert alert-info">
              暂无可用菜品
            </div>

            <!-- 数量选择和添加按钮 -->
            <div v-if="selectedMenuItemId" class="add-to-cart-section">
              <div class="row align-items-end">
                <div class="col-md-4 mb-3">
                  <label class="form-label" for="quantity">数量</label>
                  <input
                    id="quantity"
                    v-model.number="selectedQuantity"
                    type="number"
                    min="1"
                    class="form-control"
                  />
                </div>
                <div class="col-md-8 mb-3">
                  <button
                    class="btn btn-success w-100"
                    type="button"
                    @click="addToCart"
                    :disabled="!selectedQuantity || selectedQuantity < 1 || hasOrderedToday || deadlinePassed"
                  >
                    <span>添加到订单</span>
                    <span class="btn-icon">→</span>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 订单列表 -->
        <div class="card mb-3" v-if="cartItems.length > 0">
          <div class="card-header">
            <h5 class="mb-0">订单明细</h5>
          </div>
          <div class="card-body">
            <div class="table-responsive">
              <table class="table table-striped">
                <thead>
                  <tr>
                    <th>菜名</th>
                    <th>单位</th>
                    <th>分量</th>
                    <th>单价</th>
                    <th>小计</th>
                    <th>操作</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(item, index) in cartItems" :key="index">
                    <td>{{ item.name }}</td>
                    <td>{{ item.unit || '-' }}</td>
                    <td>
                      <input
                        v-model.number="item.quantity"
                        type="number"
                        min="1"
                        class="form-control form-control-sm"
                        style="width: 80px;"
                        @change="updateCartItem(index)"
                      />
                    </td>
                    <td>¥{{ item.price.toFixed(2) }}</td>
                    <td>¥{{ (item.price * item.quantity).toFixed(2) }}</td>
                    <td>
                      <button
                        class="btn btn-sm btn-danger"
                        @click="removeFromCart(index)"
                      >
                        删除
                      </button>
                    </td>
                  </tr>
                </tbody>
                <tfoot>
                  <tr>
                    <th colspan="4" class="text-end">总计：</th>
                    <th>¥{{ totalPrice.toFixed(2) }}</th>
                    <th></th>
                  </tr>
                </tfoot>
              </table>
            </div>
          </div>
        </div>

        <div v-if="cartItems.length === 0" class="alert alert-info mb-3">
          <p class="mb-1"><strong>使用说明：</strong></p>
          <p class="mb-0">1. 选择菜品和数量</p>
          <p class="mb-0">2. 点击"添加到订单"按钮</p>
          <p class="mb-0">3. 可以重复步骤1-2添加多道菜品</p>
          <p class="mb-0">4. 在订单明细中可以修改数量或删除菜品</p>
          <p class="mb-0">5. 最后点击"确认点餐"提交订单</p>
        </div>

        <!-- 提交按钮 -->
        <div class="text-center">
          <button
            class="btn btn-primary me-2"
            type="button"
            @click="submitOrder"
            :disabled="submitting || cartItems.length === 0 || hasOrderedToday || deadlinePassed"
          >
            {{ submitting ? '提交中...' : '确认点餐' }}
          </button>
          <button
            class="btn btn-secondary"
            type="button"
            @click="$router.push('/employee/order-list')"
          >
            返回订单列表
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'EmployeeNewOrder',
  data() {
    return {
      loading: false,
      submitting: false,
      menus: [],
      itemsMap: {},
      selectedMenuItemId: '',
      selectedQuantity: 1,
      cartItems: [], // 购物车中的菜品
      message: '',
      messageType: 'success',
      // 订餐可用性相关
      availabilityLoaded: false,
      hasOrderedToday: false,
      deadlinePassed: false,
      orderDate: '',
      orderDeadline: '',
      deliveryStartTime: ''
    }
  },
  computed: {
    messageClass() {
      return this.messageType === 'success' ? 'alert alert-success' : 'alert alert-danger'
    },
    totalPrice() {
      return this.cartItems.reduce((sum, item) => {
        return sum + (item.price * item.quantity)
      }, 0)
    }
  },
  created() {
    this.initPage()
  },
  methods: {
    async initPage() {
      const currentUserJson = localStorage.getItem('currentUser')
      if (!currentUserJson) {
        this.message = '未登录或登录信息已失效，请重新登录'
        this.messageType = 'error'
        this.$router.push('/login')
        return
      }
      const currentUser = JSON.parse(currentUserJson)
      this.loading = true
      try {
        const res = await axios.get('/api/orders/availability', {
          params: { userId: currentUser.id }
        })
        this.hasOrderedToday = !!res.data.hasOrderedToday
        this.deadlinePassed = !!res.data.deadlinePassed
        this.orderDate = res.data.orderDate
        this.orderDeadline = res.data.orderDeadline
        this.deliveryStartTime = res.data.deliveryStartTime
        this.availabilityLoaded = true

        // 无论是否已下单 / 是否超过截止时间，都加载今日菜单，保证下拉可选
        await this.fetchTodayMenus()
        this.applyPreselectedItem()
      } catch (e) {
        this.message = '加载订餐信息失败，请稍后重试'
        this.messageType = 'error'
      } finally {
        this.loading = false
      }
    },
    async fetchTodayMenus() {
      this.loading = true
      try {
        const res = await axios.get('/api/menus/today')
        this.menus = res.data.menus || []
        this.itemsMap = res.data.itemsMap || {}
      } catch (e) {
        this.message = '加载可订菜品失败，请稍后重试'
        this.messageType = 'error'
      } finally {
        this.loading = false
      }
    },
    getMenuItems(menuId) {
      return this.itemsMap[menuId] || []
    },
    formatDate(d) {
      if (!d) return ''
      return String(d).substring(0, 10)
    },
    findSelectedItem() {
      const id = Number(this.selectedMenuItemId)
      if (!id) return null
      for (const key of Object.keys(this.itemsMap)) {
        const list = this.itemsMap[key] || []
        const found = list.find(i => i.id === id)
        if (found) return found
      }
      return null
    },
    selectMenuItem(itemId) {
      if (this.hasOrderedToday || this.deadlinePassed) return
      this.selectedMenuItemId = itemId
      this.selectedQuantity = 1
    },
    handleImageError(event) {
      const img = event.target
      const container = img.closest('.image-wrapper')
      if (container) {
        container.innerHTML = `
          <div class="image-placeholder">
            <span class="placeholder-icon">🍽️</span>
          </div>
        `
      }
    },
    applyPreselectedItem() {
      const itemId = this.$route.query.itemId
      if (itemId && !this.selectedMenuItemId && this.cartItems.length === 0) {
        this.selectedMenuItemId = Number(itemId)
      }
    },
    addToCart() {
      const item = this.findSelectedItem()
      if (!item) {
        this.message = '请选择菜品'
        this.messageType = 'error'
        return
      }
      
      if (!this.selectedQuantity || this.selectedQuantity < 1) {
        this.message = '请输入有效的数量'
        this.messageType = 'error'
        return
      }

      // 检查是否已存在相同菜品
      const existingIndex = this.cartItems.findIndex(cartItem => cartItem.menuItemId === item.id)
      if (existingIndex >= 0) {
        // 如果已存在，增加数量
        this.cartItems[existingIndex].quantity += this.selectedQuantity
        this.message = `已更新 "${item.name}" 的数量`
        this.messageType = 'success'
      } else {
        // 如果不存在，添加新项
        this.cartItems.push({
          menuItemId: item.id,
          name: item.name,
          unit: item.unit,
          price: item.price,
          quantity: this.selectedQuantity
        })
        this.message = `已添加 "${item.name}" 到订单`
        this.messageType = 'success'
      }

      // 清空选择
      this.selectedMenuItemId = ''
      this.selectedQuantity = 1
      
      console.log('当前订单中的菜品:', this.cartItems)
    },
    removeFromCart(index) {
      this.cartItems.splice(index, 1)
    },
    updateCartItem(index) {
      if (this.cartItems[index].quantity < 1) {
        this.cartItems[index].quantity = 1
      }
    },
    async submitOrder() {
      const currentUserJson = localStorage.getItem('currentUser')
      if (!currentUserJson) {
        this.message = '未登录或登录信息已失效，请重新登录'
        this.messageType = 'error'
        this.$router.push('/login')
        return
      }

      const currentUser = JSON.parse(currentUserJson)

      if (this.deadlinePassed) {
        this.message = '订餐截止时间已过，无法下单。'
        this.messageType = 'error'
        return
      }
      if (this.hasOrderedToday) {
        this.message = '您今天已经下过订单，每天只能下单一次。'
        this.messageType = 'error'
        return
      }
      if (this.cartItems.length === 0) {
        this.message = '请至少添加一道菜品'
        this.messageType = 'error'
        return
      }

      this.submitting = true
      this.message = ''
      try {
        // 构建订单项列表
        const orderItems = this.cartItems.map(item => ({
          menuItemId: item.menuItemId,
          quantity: item.quantity
        }))

        const res = await axios.post('/api/orders', {
          userId: currentUser.id,
          userName: currentUser.name,
          phone: currentUser.phone,
          items: orderItems
        })
        this.message = '订单创建成功！'
        this.messageType = 'success'
        const orderId = res.data && (res.data.id || res.data.orderId)
        if (orderId) {
          this.$router.push(`/employee/order-confirmation/${orderId}`)
        } else {
          this.$router.push('/employee/order-list')
        }
      } catch (e) {
        if (e.response && e.response.status === 400) {
          const code = e.response.data
          if (code === 'deadline_passed') {
            this.message = '订餐截止时间已过，无法下单。'
          } else if (code === 'already_ordered_today') {
            this.message = '您今天已经下过订单，每天只能下单一次。'
          } else if (code === 'invalid_menu_item') {
            this.message = '选择的菜品无效，请刷新页面后重试。'
          } else if (code === 'missing_params') {
            this.message = '参数缺失，请重新选择菜品和数量。'
          } else {
            this.message = '下单失败：' + code
          }
        } else {
          this.message = '下单过程中发生错误，请稍后重试。'
        }
        this.messageType = 'error'
      } finally {
        this.submitting = false
      }
    }
  }
}
</script>

<style scoped>
.new-order-form {
  max-width: 1400px;
  margin: 0 auto;
}

/* 菜单选择区域 */
.menu-selection-area {
  margin-bottom: 24px;
}

.menu-group {
  margin-bottom: 32px;
}

.menu-group-title {
  font-size: 1.25rem;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 2px solid rgba(102, 126, 234, 0.1);
}

.menu-items-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}

.menu-item-select-card {
  background: #ffffff;
  border: 2px solid rgba(102, 126, 234, 0.1);
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  display: flex;
  flex-direction: column;
}

.menu-item-select-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  transform: scaleX(0);
  transition: transform 0.3s ease;
  z-index: 1;
}

.menu-item-select-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.2);
  border-color: rgba(102, 126, 234, 0.3);
}

.menu-item-select-card:hover::before {
  transform: scaleX(1);
}

.menu-item-select-card.selected {
  border-color: #667eea;
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.3);
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
}

.menu-item-select-card.selected::before {
  transform: scaleX(1);
}

.item-image-container {
  width: 100%;
  height: 160px;
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

.item-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
  transition: transform 0.4s ease;
  background: #f8f9fa;
}

.menu-item-select-card:hover .item-image {
  transform: scale(1.05);
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
}

.placeholder-icon {
  font-size: 3rem;
  opacity: 0.5;
}

.item-info {
  padding: 12px;
  text-align: center;
}

.item-name {
  font-size: 0.95rem;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 6px;
  line-height: 1.3;
  min-height: 2.6rem;
  display: flex;
  align-items: center;
  justify-content: center;
}

.item-price {
  font-size: 0.9rem;
  color: #667eea;
  font-weight: 700;
}

.selected-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
  z-index: 2;
}

.add-to-cart-section {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 2px solid rgba(102, 126, 234, 0.1);
}

.add-to-cart-section .btn-success {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 24px;
  font-size: 15px;
  font-weight: 600;
}

.add-to-cart-section .btn-icon {
  transition: transform 0.3s ease;
}

.add-to-cart-section .btn-success:hover .btn-icon {
  transform: translateX(4px);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .menu-items-grid {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
    gap: 12px;
  }

  .item-image-container {
    height: 120px;
  }

  .item-name {
    font-size: 0.85rem;
    min-height: 2.4rem;
  }

  .item-price {
    font-size: 0.8rem;
  }
}

@media (max-width: 480px) {
  .menu-items-grid {
    grid-template-columns: repeat(2, 1fr);
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
</style>


