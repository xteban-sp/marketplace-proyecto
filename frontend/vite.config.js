import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// El frontend corre en :3000 (coincide con las back_urls de Mercado Pago).
export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    host: true,
  },
})
