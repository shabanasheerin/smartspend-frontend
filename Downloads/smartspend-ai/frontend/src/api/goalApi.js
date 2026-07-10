import apiClient from './apiClient';

export const goalApi = {
  list: () => apiClient.get('/goals'),
  create: (payload) => apiClient.post('/goals', payload),
  update: (id, payload) => apiClient.put(`/goals/${id}`, payload),
  contribute: (id, payload) => apiClient.post(`/goals/${id}/contribute`, payload),
  abandon: (id) => apiClient.post(`/goals/${id}/abandon`),
  remove: (id) => apiClient.delete(`/goals/${id}`),
};
