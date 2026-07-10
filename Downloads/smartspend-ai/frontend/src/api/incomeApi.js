import apiClient from './apiClient';

export const incomeApi = {
  list: (params) => apiClient.get('/incomes', { params }),
  create: (payload) => apiClient.post('/incomes', payload),
  update: (id, payload) => apiClient.put(`/incomes/${id}`, payload),
  remove: (id) => apiClient.delete(`/incomes/${id}`),
};
