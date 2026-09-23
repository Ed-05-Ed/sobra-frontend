import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import { VitePWA } from 'vite-plugin-pwa';

export default defineConfig({
  plugins: [
    react(),
    VitePWA({
      registerType: 'autoUpdate',
      includeAssets: ['favicon.ico', 'apple-touch-icon.png'],
      manifest: {
        name: 'SOBRA - Aprovechamiento de Alimentos',
        short_name: 'SOBRA',
        description: 'Inventario inteligente y compartido para evitar el desperdicio de alimentos',
        theme_color: '#064E3B',
        background_color: '#F8F9FA',
        display: 'standalone',
        orientation: 'portrait',
        start_url: '/',
        icons: [
          {
            src: '/pwa-192x192.png',
            sizes: '192x192',
            type: 'image/png'
          },
          {
            src: '/pwa-512x512.png',
            sizes: '512x512',
            type: 'image/png'
          }
        ]
      }
    })
  ],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        // Por defecto redirige al túnel o a localhost:8081 para evitar cualquier bloqueo de CORS en el navegador
        target: process.env.VITE_API_PROXY_TARGET || 'https://enjoyed-senator-procedure-efficiently.trycloudflare.com',
        changeOrigin: true,
        secure: false
      }
    }
  }
});
