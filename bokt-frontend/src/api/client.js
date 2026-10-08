import axios from 'axios';
import { tokenStore } from './tokenStore.js';

// Базовый URL берётся из прокси Vite (/api). При необходимости — VITE_API_BASE.
const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '/api',
});

// Подставляем Bearer в каждый запрос
api.interceptors.request.use((config) => {
  const token = tokenStore.getAccess();
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// Пути, которые не надо пытаться «чинить» refresh-ом
const AUTH_PATHS = ['/auth/login', '/auth/verify-otp', '/auth/refresh'];

let refreshing = null; // single-flight: один refresh на все параллельные 401

async function doRefresh() {
  const refreshToken = tokenStore.getRefresh();
  if (!refreshToken) throw new Error('no refresh token');
  // отдельный axios без интерсептора, чтобы не зациклиться
  const res = await axios.post(
    (import.meta.env.VITE_API_BASE || '/api') + '/auth/refresh',
    { refreshToken },
  );
  tokenStore.set(res.data.accessToken, res.data.refreshToken);
  return res.data.accessToken;
}

api.interceptors.response.use(
  (r) => r,
  async (error) => {
    const { response, config } = error;
    if (!response || response.status !== 401 || config._retried) {
      return Promise.reject(error);
    }
    if (AUTH_PATHS.some((p) => (config.url || '').includes(p))) {
      return Promise.reject(error);
    }
    try {
      config._retried = true;
      if (!refreshing) refreshing = doRefresh().finally(() => { refreshing = null; });
      const newToken = await refreshing;
      config.headers.Authorization = `Bearer ${newToken}`;
      return api(config);
    } catch (e) {
      tokenStore.clear();
      // мягкий редирект на вход
      if (!location.pathname.startsWith('/login')) location.href = '/login';
      return Promise.reject(e);
    }
  },
);

/** Достать человекочитаемое сообщение об ошибке из ApiError бэкенда. */
export function errorMessage(err, fallback = 'Произошла ошибка') {
  return err?.response?.data?.message || err?.message || fallback;
}

export default api;
