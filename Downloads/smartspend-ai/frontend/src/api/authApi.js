import apiClient from './apiClient';

export const authApi = {
  register: (payload) => apiClient.post('/auth/register', payload),
  login: (payload) => apiClient.post('/auth/login', payload),
  logout: () => apiClient.post('/auth/logout'),
  changePassword: (payload) => apiClient.post('/auth/change-password', payload),
  forgotPassword: (payload) => apiClient.post('/auth/forgot-password', payload),
  resetPassword: (payload) => apiClient.post('/auth/reset-password', payload),
  verifyEmail: (token) => apiClient.get(`/auth/verify-email?token=${token}`),
  resendVerification: (payload) => apiClient.post('/auth/resend-verification', payload),
};
