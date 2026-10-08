import api from './client.js';

export const usersApi = {
  list: (page = 0, size = 20) =>
    api.get('/users', { params: { page, size } }).then((r) => r.data),
  create: (body) => api.post('/users', body).then((r) => r.data),
  disable: (id) => api.post(`/users/${id}/disable`).then((r) => r.data),
  enable: (id) => api.post(`/users/${id}/enable`).then((r) => r.data),
  resetPassword: (id) => api.post(`/users/${id}/reset-password`).then((r) => r.data),
};
