/* eslint-disable vue/multi-word-component-names */
<template>
  <div class="login-page">
    <div class="login-overlay" />
    <div class="floating-shapes">
      <div class="shape shape-1"></div>
      <div class="shape shape-2"></div>
      <div class="shape shape-3"></div>
    </div>
    <div class="login-container">
      <div class="login-header">
        <div class="logo-icon">🍽️</div>
        <h2>企业餐厅网络点餐系统</h2>
        <p class="subtitle">Enterprise Catering Order System</p>
      </div>
      <form @submit.prevent="onSubmit" class="login-form">
        <div class="form-group">
          <label for="username">
            <span class="label-icon">👤</span>
            用户名
          </label>
          <div class="input-wrapper">
            <input
              id="username"
              v-model="form.loginName"
              type="text"
              placeholder="请输入用户名"
              required
              autocomplete="username"
            />
          </div>
        </div>
        <div class="form-group">
          <label for="password">
            <span class="label-icon">🔒</span>
            密码
          </label>
          <div class="input-wrapper">
            <input
              id="password"
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              required
              autocomplete="current-password"
            />
          </div>
        </div>
        <div class="form-group remember-me">
          <label class="checkbox-label" for="rememberMe">
            <input
              id="rememberMe"
              v-model="rememberMe"
              type="checkbox"
            />
            <span class="checkbox-custom"></span>
            <span class="checkbox-text">记住用户名</span>
          </label>
        </div>
        <button class="btn btn-login" type="submit" :disabled="loading">
          <span v-if="!loading" class="btn-content">
            <span>登录</span>
            <span class="btn-icon">→</span>
          </span>
          <span v-else class="btn-loading">
            <span class="spinner"></span>
            <span>登录中...</span>
          </span>
        </button>
        <div v-if="errorMessage" class="error-message">
          <span class="error-icon">⚠️</span>
          <span>{{ errorMessage }}</span>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
import axios from '@/utils/axios'

export default {
  name: 'LoginPage',
  data() {
    return {
      form: {
        loginName: localStorage.getItem('rememberLoginName') || '',
        password: ''
      },
      rememberMe: !!localStorage.getItem('rememberLoginName'),
      loading: false,
      errorMessage: ''
    }
  },
  created() {
    // 如果已经登录，访问 /login 时自动跳转到对应控制台，避免出现"登录框 + 侧边栏"的怪异界面
    const userJson = localStorage.getItem('currentUser')
    if (userJson) {
      try {
        const user = JSON.parse(userJson)
        this.redirectByRole(user.role)
      } catch (e) {
        // ignore
      }
    }
  },
  methods: {
    redirectByRole(role) {
      switch (role) {
        case 'manager':
          this.$router.push('/manager/dashboard')
          break
        case 'kitchen_chief':
          this.$router.push('/kitchen/dashboard')
          break
        case 'delivery_staff':
          this.$router.push('/delivery/dashboard')
          break
        case 'finance':
          this.$router.push('/finance/dashboard')
          break
        case 'employee':
          this.$router.push('/employee/menu')
          break
        default:
          this.$router.push('/index')
      }
    },
    async onSubmit() {
      if (!this.form.loginName || !this.form.password) {
        this.errorMessage = '请输入用户名和密码！'
        return
      }
      this.loading = true
      this.errorMessage = ''
      try {
        const res = await axios.post('/api/auth/login', this.form)
        const user = res.data

        // 记住用户名（仅用于下次登录时自动填充）
        if (this.rememberMe) {
          localStorage.setItem('rememberLoginName', this.form.loginName)
        } else {
          localStorage.removeItem('rememberLoginName')
        }

        // 简单保存用户信息（可替换为 Vuex/Pinia）
        localStorage.setItem('currentUser', JSON.stringify(user))

        // 按角色跳转，对应原 JSP 的跳转逻辑
        this.redirectByRole(user.role)
      } catch (e) {
        console.error('登录错误:', e)
        if (e.response) {
          // 服务器返回了响应
          if (e.response.status === 401) {
            this.errorMessage = '用户名或密码错误！'
          } else if (e.response.status === 400) {
            this.errorMessage = '请求参数错误，请检查输入！'
          } else if (e.response.status >= 500) {
            this.errorMessage = '服务器错误，请稍后重试。'
          } else {
            this.errorMessage = `登录失败 (${e.response.status})，请稍后重试。`
          }
        } else if (e.request) {
          // 请求已发出但没有收到响应
          this.errorMessage = '无法连接到服务器，请检查网络连接或确认后端服务是否运行。'
        } else {
          // 其他错误
          this.errorMessage = '登录过程中发生错误，请稍后重试。'
        }
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.login-page {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 50%, #f093fb 100%);
  overflow: hidden;
  animation: gradientShift 20s ease infinite;
  background-size: 200% 200%;
  padding: 20px;
}

@keyframes gradientShift {
  0% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0% 50%;
  }
}

.login-overlay {
  position: absolute;
  inset: 0;
  background-image: 
    radial-gradient(circle at 20% 30%, rgba(255, 255, 255, 0.25), transparent 50%),
    radial-gradient(circle at 80% 70%, rgba(255, 255, 255, 0.2), transparent 50%),
    radial-gradient(circle at 50% 50%, rgba(255, 255, 255, 0.15), transparent 60%);
  opacity: 0.9;
  animation: pulse 10s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 0.9;
  }
  50% {
    opacity: 1;
  }
}

/* 浮动装饰元素 */
.floating-shapes {
  position: absolute;
  inset: 0;
  overflow: hidden;
  z-index: 0;
}

.shape {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  animation: float 15s ease-in-out infinite;
}

.shape-1 {
  width: 200px;
  height: 200px;
  top: 10%;
  left: 10%;
  animation-delay: 0s;
}

.shape-2 {
  width: 150px;
  height: 150px;
  top: 60%;
  right: 15%;
  animation-delay: 5s;
}

.shape-3 {
  width: 100px;
  height: 100px;
  bottom: 20%;
  left: 20%;
  animation-delay: 10s;
}

@keyframes float {
  0%, 100% {
    transform: translate(0, 0) rotate(0deg);
  }
  33% {
    transform: translate(30px, -30px) rotate(120deg);
  }
  66% {
    transform: translate(-20px, 20px) rotate(240deg);
  }
}

.login-container {
  position: relative;
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(30px);
  padding: 48px 40px;
  border-radius: 28px;
  box-shadow: 
    0 25px 80px rgba(0, 0, 0, 0.3),
    0 0 0 1px rgba(255, 255, 255, 0.6) inset,
    0 0 100px rgba(102, 126, 234, 0.2);
  width: 100%;
  max-width: 440px;
  z-index: 1;
  animation: slideIn 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(40px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* 登录头部 */
.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.logo-icon {
  font-size: 4rem;
  margin-bottom: 16px;
  display: block;
  animation: bounce 2s ease-in-out infinite;
  filter: drop-shadow(0 4px 8px rgba(0, 0, 0, 0.2));
}

@keyframes bounce {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

.login-header h2 {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 8px;
  font-weight: 700;
  font-size: 28px;
  letter-spacing: 0.5px;
  line-height: 1.3;
}

.subtitle {
  color: #718096;
  font-size: 13px;
  font-weight: 500;
  letter-spacing: 1px;
  text-transform: uppercase;
  margin: 0;
}

/* 表单样式 */
.login-form {
  width: 100%;
}

.form-group {
  margin-bottom: 24px;
}

.form-group label {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  color: #4a5568;
  font-size: 14px;
  font-weight: 600;
}

.label-icon {
  font-size: 16px;
}

.input-wrapper {
  position: relative;
}

.form-group input[type="text"],
.form-group input[type="password"] {
  width: 100%;
  padding: 14px 18px;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  box-sizing: border-box;
  font-size: 15px;
  transition: all 0.3s ease;
  background: #ffffff;
  color: #1a1a1a;
}

.form-group input::placeholder {
  color: #a0aec0;
}

.form-group input:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1);
  outline: none;
  background: #ffffff;
  transform: translateY(-1px);
}

.form-group input:hover:not(:focus) {
  border-color: #cbd5e0;
  background: #f8f9fa;
}

/* 记住我复选框 */
.remember-me {
  margin-bottom: 28px;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  user-select: none;
}

.checkbox-label input[type="checkbox"] {
  position: absolute;
  opacity: 0;
  width: 0;
  height: 0;
}

.checkbox-custom {
  width: 20px;
  height: 20px;
  border: 2px solid #cbd5e0;
  border-radius: 6px;
  background: #ffffff;
  position: relative;
  transition: all 0.3s ease;
  flex-shrink: 0;
}

.checkbox-label input:checked + .checkbox-custom {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-color: #667eea;
}

.checkbox-label input:checked + .checkbox-custom::after {
  content: '✓';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: #fff;
  font-size: 12px;
  font-weight: 700;
}

.checkbox-text {
  color: #4a5568;
  font-size: 14px;
  font-weight: 500;
}

.checkbox-label:hover .checkbox-custom {
  border-color: #667eea;
  transform: scale(1.1);
}

/* 登录按钮 */
.btn-login {
  width: 100%;
  padding: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.35);
  position: relative;
  overflow: hidden;
  margin-bottom: 16px;
}

.btn-login::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  transform: translate(-50%, -50%);
  transition: width 0.6s, height 0.6s;
}

.btn-login:hover::before {
  width: 400px;
  height: 400px;
}

.btn-login:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none;
}

.btn-login:hover:not(:disabled) {
  background: linear-gradient(135deg, #764ba2 0%, #667eea 100%);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.45);
  transform: translateY(-2px);
}

.btn-login:active:not(:disabled) {
  transform: translateY(0);
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.btn-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  position: relative;
  z-index: 1;
}

.btn-icon {
  font-size: 18px;
  transition: transform 0.3s ease;
}

.btn-login:hover:not(:disabled) .btn-icon {
  transform: translateX(4px);
}

.btn-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  position: relative;
  z-index: 1;
}

.spinner {
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 错误消息 */
.error-message {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: #f5576c;
  text-align: center;
  font-size: 14px;
  padding: 14px 16px;
  background: linear-gradient(135deg, rgba(245, 87, 108, 0.1) 0%, rgba(240, 147, 251, 0.1) 100%);
  border-radius: 12px;
  border: 2px solid rgba(245, 87, 108, 0.2);
  font-weight: 500;
  animation: shake 0.5s ease;
}

.error-icon {
  font-size: 18px;
}

@keyframes shake {
  0%, 100% {
    transform: translateX(0);
  }
  25% {
    transform: translateX(-10px);
  }
  75% {
    transform: translateX(10px);
  }
}

/* 响应式设计 */
@media (max-width: 480px) {
  .login-container {
    padding: 36px 28px;
    border-radius: 24px;
  }

  .login-header h2 {
    font-size: 24px;
  }

  .logo-icon {
    font-size: 3rem;
  }

  .form-group input[type="text"],
  .form-group input[type="password"] {
    padding: 12px 16px;
    font-size: 14px;
  }

  .btn-login {
    padding: 14px;
    font-size: 15px;
  }
}
</style>


