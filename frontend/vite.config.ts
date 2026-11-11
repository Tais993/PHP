import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api/tasks': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/api/boards': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})