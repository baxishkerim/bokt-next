import api from './client.js';

export const cardsApi = {
  list: () => api.get('/cards').then((r) => r.data),
  add: (body) => api.post('/cards', body).then((r) => r.data),
  block: (id) => api.post(`/cards/${id}/block`).then((r) => r.data),
  unblock: (id) => api.post(`/cards/${id}/unblock`).then((r) => r.data),
  setBalance: (id, balance) => api.post(`/cards/${id}/balance`, balance,
    { headers: { 'Content-Type': 'application/json' } }).then((r) => r.data),
};
