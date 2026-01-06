const { defineConfig } = require('@vue/cli-service')

// 前端开发环境：将所有 /api 请求代理到 Spring Boot 后端（qyct_system）
module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    proxy: {
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true
      }
    }
  }
})
