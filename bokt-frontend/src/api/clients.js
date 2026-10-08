import api from './client.js';

export const clientsApi = {
  search: (q = '', page = 0, size = 20) =>
    api.get('/clients', { params: { q: q || undefined, page, size } }).then((r) => r.data),
  get: (id) => api.get(`/clients/${id}`).then((r) => r.data),
  create: (body) => api.post('/clients', body).then((r) => r.data),
  update: (id, body) => api.put(`/clients/${id}`, body).then((r) => r.data),
};
