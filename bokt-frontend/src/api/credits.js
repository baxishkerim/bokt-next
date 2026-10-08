import api from './client.js';

export const creditsApi = {
  list: (status, page = 0, size = 20) =>
    api.get('/credits', { params: { status: status || undefined, page, size } }).then((r) => r.data),
  get: (id) => api.get(`/credits/${id}`).then((r) => r.data),
  add: (body) => api.post('/credits', body).then((r) => r.data),
  approve: (id) => api.post(`/credits/${id}/approve`).then((r) => r.data),
  cancel: (id) => api.post(`/credits/${id}/cancel`).then((r) => r.data),
  postpone: (id) => api.post(`/credits/${id}/postpone`).then((r) => r.data),
  byPan: (pan) => api.get('/credits/by-pan', { params: { pan } }).then((r) => r.data),
  loaded: (fileId, page = 0, size = 20) =>
    api.get('/credits/loaded', { params: { fileId, page, size } }).then((r) => r.data),
  archive: (page = 0, size = 20) =>
    api.get('/credits/archive', { params: { page, size } }).then((r) => r.data),
  reversal: (id, body) => api.post(`/credits/${id}/reversal`, body).then((r) => r.data),
};
