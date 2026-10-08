import api from './client.js';

export const organisationsApi = {
  register: (body) => api.post('/organisations', body).then((r) => r.data),
  list: () => api.get('/organisations').then((r) => r.data),
  activate: (id) => api.post(`/organisations/${id}/activate`).then((r) => r.data),
  deactivate: (id) => api.post(`/organisations/${id}/deactivate`).then((r) => r.data),
};
