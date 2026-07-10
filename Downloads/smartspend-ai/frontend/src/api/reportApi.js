import apiClient from './apiClient';

export const reportApi = {
  get: (period, date) => apiClient.get('/reports', { params: { period, date } }),
  exportCsv: (period, date) =>
    apiClient.get('/reports/export/csv', { params: { period, date }, responseType: 'blob' }),
  exportExcel: (period, date) =>
    apiClient.get('/reports/export/excel', { params: { period, date }, responseType: 'blob' }),
  exportPdf: (period, date) =>
    apiClient.get('/reports/export/pdf', { params: { period, date }, responseType: 'blob' }),
};
