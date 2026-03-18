import fs from 'node:fs';
import os from 'node:os';
import path from 'node:path';

import vue from '@vitejs/plugin-vue';
import vueJsx from '@vitejs/plugin-vue-jsx';
import type { ConfigEnv, Plugin, UserConfig } from 'vite';
import { loadEnv } from 'vite';
import svgLoader from 'vite-svg-loader';

const CWD = process.cwd();
const BANNER_TEMPLATE_PATH = path.resolve(__dirname, 'scripts', 'templates', 'frontend-startup-banner.txt');

const loadBannerTemplate = () => {
  try {
    return fs.readFileSync(BANNER_TEMPLATE_PATH, 'utf-8');
  } catch {
    return '';
  }
};

const getAccessUrls = (port: number) => {
  const lines: string[] = [];
  lines.push(`  ➜ Local:   http://localhost:${port}/`);
  const nets = os.networkInterfaces();
  const addresses = Object.values(nets)
    .flatMap((items) => items || [])
    .filter((item) => item.family === 'IPv4' && !item.internal)
    .map((item) => item.address)
    .filter((address) => address && !address.startsWith('169.254.'))
    .sort();
  for (const address of Array.from(new Set(addresses))) {
    lines.push(`  ➜ Network: http://${address}:${port}/`);
  }
  return lines.join(os.EOL);
};

const renderBanner = (port: number) => {
  const template = loadBannerTemplate();
  if (!template) return '';
  return template.replaceAll('{{accessUrls}}', getAccessUrls(port));
};

const startupBannerPlugin = (): Plugin => {
  let printed = false;
  return {
    name: 'startup-banner',
    configureServer(server) {
      server.httpServer?.once('listening', () => {
        if (printed) return;
        printed = true;
        const address = server.httpServer?.address();
        const port = typeof address === 'object' && address?.port ? address.port : 0;
        if (!port) return;
        const banner = renderBanner(port);
        if (banner) console.log(banner);
      });
    },
  };
};

// https://vitejs.dev/config/
export default ({ mode }: ConfigEnv): UserConfig => {
  const { VITE_BASE_URL, VITE_API_URL, VITE_API_URL_PREFIX } = loadEnv(mode, CWD);
  return {
    base: VITE_BASE_URL,
    resolve: {
      alias: {
        '@': path.resolve(__dirname, './src'),
      },
    },

    css: {
      preprocessorOptions: {
        less: {
          modifyVars: {
            hack: `true; @import (reference) "${path.resolve('src/style/variables.less')}";`,
          },
          math: 'strict',
          javascriptEnabled: true,
        },
      },
    },

    plugins: [startupBannerPlugin(), vue(), vueJsx(), svgLoader()],

    server: {
      port: 3002,
      host: '0.0.0.0',
      allowedHosts: true,
      proxy: {
        [VITE_API_URL_PREFIX]: {
          target: VITE_API_URL || 'http://127.0.0.1:9999/',
          // Keep original Host so backend can treat proxied dev requests as same-origin.
          changeOrigin: false,
        },
      },
    },
  };
};
