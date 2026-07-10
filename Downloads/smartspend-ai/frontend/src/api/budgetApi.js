import apiClient from './apiClient';

export const budgetApi = {
  list: (month, year) => apiClient.get('/budgets', { params: { month, year } }),
  create: (payload) => apiClient.post('/budgets', payload),
  update: (id, payload) => apiClient.put(`/budgets/${id}`, payload),
  remove: (id) => apiClient.delete(`/budgets/${id}`),
};
