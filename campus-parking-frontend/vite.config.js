import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig(({ mode }) => {
  const isProduction = mode === 'production'

  return {
    plugins: [vue()],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url))
      }
    },
    server: {
      port: 3000, // 前端开发服务器端口改为 3000

      proxy: {
        '/api': {
          target: 'http://localhost:8081', // 统一使用 8081
          changeOrigin: true,
          secure: false,
          configure: (proxy, options) => {
            proxy.on('error', (err, req, res) => {
              console.log('❌ 代理错误:', err);
            });
            proxy.on('proxyReq', (proxyReq, req, res) => {
              console.log('🔄 代理请求:', req.method, req.url);
            });
            proxy.on('proxyRes', (proxyRes, req, res) => {
              console.log('✅ 代理响应:', proxyRes.statusCode, req.url);
            });
          }
        }
      }
    },
    build: {
      outDir: 'dist',
      assetsDir: 'assets',
      sourcemap: !isProduction,
      rollupOptions: {
        output: {
          manualChunks: {
            vendor: ['vue', 'vue-router'],
            utils: ['./src/utils/auth.js']
          }
        }
      }
    },
    base: isProduction ? './' : '/'
  }
})
