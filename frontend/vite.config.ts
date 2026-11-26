import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'
import istanbul from 'vite-plugin-istanbul'

export default defineConfig(({ command }) => ({
  plugins: [
    react(),
    command === 'serve' && process.env.VITE_COVERAGE === 'true' && istanbul({
      include: 'src/**',
      extension: ['.ts', '.tsx'],
    }),
  ],
  server: { port: 5173, strictPort: true },
}))
