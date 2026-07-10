import apiClient from './apiClient';

export const insightApi = {
  list: () => apiClient.get('/insights'),
};
