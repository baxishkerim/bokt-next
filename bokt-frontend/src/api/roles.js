import api from './client.js';

export const rolesApi = {
  list: () => api.get('/roles').then((r) => r.data),
  create: (body) => api.post('/roles', body).then((r) => r.data),
  updatePermissions: (id, permissions) =>
    api.put(`/roles/${id}/permissions`, permissions).then((r) => r.data),
  remove: (id) => api.delete(`/roles/${id}`).then((r) => r.data),
};
