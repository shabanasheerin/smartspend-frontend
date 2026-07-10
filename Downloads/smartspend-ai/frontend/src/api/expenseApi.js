import apiClient from './apiClient';

export const expenseApi = {
  list: (params) => apiClient.get('/expenses', { params }),
  create: (payload) => apiClient.post('/expenses', payload),
  update: (id, payload) => apiClient.put(`/expenses/${id}`, payload),
  remove: (id) => apiClient.delete(`/expenses/${id}`),
  uploadReceipt: (expenseId, file) => {
    const formData = new FormData();
    formData.append('file', file);
    return apiClient.post(`/expenses/${expenseId}/receipts`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },
};
