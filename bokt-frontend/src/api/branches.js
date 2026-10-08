import api from './client.js';

export const branchesApi = {
  list: () => api.get('/branches').then((r) => r.data),
  add: (body) => api.post('/branches', body).then((r) => r.data),
};
