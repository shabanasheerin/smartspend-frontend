import apiClient from './apiClient';

export const categoryApi = {
  list: () => apiClient.get('/categories'),
};
