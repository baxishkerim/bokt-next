import api from './client.js';

export const filesApi = {
  upload: (file) => {
    const form = new FormData();
    form.append('file', file);
    return api
      .post('/credit-files', form, { headers: { 'Content-Type': 'multipart/form-data' } })
      .then((r) => r.data);
  },
};
