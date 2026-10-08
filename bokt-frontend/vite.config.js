import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// Прокси /api и /actuator на бэкенд, чтобы в dev не упираться в CORS.
// Адрес бэкенда можно переопределить переменной VITE_BACKEND_URL.
const backend = process.env.VITE_BACKEND_URL || 'http://localhost:8080';

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': { target: backend, changeOrigin: true },
      '/actuator': { target: backend, changeOrigin: true },
    },
  },
});
