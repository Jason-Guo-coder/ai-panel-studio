import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    // 开发/预览:/api 代理到后端(SSE 亦经此代理)
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
})
