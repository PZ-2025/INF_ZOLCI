import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  base: './', 
  plugins: [vue()],
  build: {
    outDir: 'dist', 
    assetsDir: 'assets', 
  },
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    }
  }
})
