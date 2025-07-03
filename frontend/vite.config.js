import {fileURLToPath, URL} from 'node:url'

import {defineConfig} from 'vite'
import vue from '@vitejs/plugin-vue'

// 引入crypto模块作为polyfill
import {webcrypto} from 'node:crypto'

// 为Node.js环境提供crypto polyfill
if (!globalThis.crypto) {
    globalThis.crypto = webcrypto
}

// https://vitejs.dev/config/
export default defineConfig({
    plugins: [
        vue(),
        //vueDevTools(),
    ],
    resolve: {
        alias: {
            '@': fileURLToPath(new URL('./src', import.meta.url))
        }
    },
    server: {
        proxy: {
          '/api': {
            target: 'http://localhost:8080',
            changeOrigin: true
          }
        }
        // host: '0.0.0.0',  // 允许所有IP访问
        // port: 5173
    }
})
