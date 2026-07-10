import apiClient from './apiClient';

export const dashboardApi = {
  summary: () => apiClient.get('/dashboard/summary'),
};
