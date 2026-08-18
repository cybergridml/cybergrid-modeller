import react from '@vitejs/plugin-react';
import { defineConfig, loadEnv } from 'vite';

export default defineConfig(({ mode }) => ({
  plugins: [react()],
  build: {
    minify: mode !== 'development',
  },
  define: {
    'process.env': { ...process.env, ...loadEnv(mode, process.cwd()) },
  },
}));
