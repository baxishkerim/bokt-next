import api from './client.js';

export const auditApi = {
  byCorrelation: (correlationId) =>
    api.get(`/audit/by-correlation/${correlationId}`).then((r) => r.data),
  byEntity: (type, id) => api.get(`/audit/entity/${type}/${id}`).then((r) => r.data),
};
