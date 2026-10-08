import api from './client.js';

export const authApi = {
  login: (orgLogin, username, password) =>
    api.post('/auth/login', { orgLogin, username, password }).then((r) => r.data),
  verifyOtp: (reference, code) =>
    api.post('/auth/verify-otp', { reference, code }).then((r) => r.data),
  logout: () => api.post('/auth/logout').then((r) => r.data),
  me: () => api.get('/auth/me').then((r) => r.data),
};
