import { useState } from 'react';
import toast from 'react-hot-toast';
import { Download } from 'lucide-react';
import { reportApi } from '../api/reportApi';
import Card from '../components/common/Card';
import Button from '../components/common/Button';
import Select from '../components/common/Select';
import EmptyState from '../components/common/EmptyState';

const currency = (v) =>
  new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(v || 0);

export default function ReportsPage() {
  const [period, setPeriod] = useState('MONTHLY');
  const [rows, setRows] = useState(null);
  const [loading, setLoading] = useState(false);

  const runReport = async () => {
    setLoading(true);
    try {
      const { data } = await reportApi.get(period);
      setRows(data.data);
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to load report');
    } finally {
      setLoading(false);
    }
  };

  const downloadCsv = async () => {
    try {
      const { data } = await reportApi.exportCsv(period);
      triggerDownload(data, `smartspend-report-${period.toLowerCase()}.csv`);
    } catch {
      toast.error('Export failed');
    }
  };

  const downloadExcel = async () => {
    try {
      const { data } = await reportApi.exportExcel(period);
      triggerDownload(data, `smartspend-report-${period.toLowerCase()}.xlsx`);
    } catch {
      toast.error('Export failed');
    }
  };

  const downloadPdf = async () => {
    try {
      const { data } = await reportApi.exportPdf(period);
      triggerDownload(data, `smartspend-report-${period.toLowerCase()}.pdf`);
    } catch {
      toast.error('Export failed');
    }
  };

  const triggerDownload = (data, filename) => {
    const url = window.URL.createObjectURL(new Blob([data]));
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    link.click();
    window.URL.revokeObjectURL(url);
  };

  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold">Reports</h1>

      <Card>
        <div className="flex flex-wrap items-end gap-4">
          <Select label="Period" value={period} onChange={(e) => setPeriod(e.target.value)} className="w-48">
            <option value="DAILY">Daily</option>
            <option value="WEEKLY">Weekly</option>
            <option value="MONTHLY">Monthly</option>
            <option value="YEARLY">Yearly</option>
          </Select>
          <Button onClick={runReport} disabled={loading}>
            {loading ? 'Loading...' : 'Run Report'}
          </Button>
          <Button variant="secondary" onClick={downloadCsv} className="flex items-center gap-1.5">
            <Download size={15} /> CSV
          </Button>
          <Button variant="secondary" onClick={downloadExcel} className="flex items-center gap-1.5">
            <Download size={15} /> Excel
          </Button>
          <Button variant="secondary" onClick={downloadPdf} className="flex items-center gap-1.5">
            <Download size={15} /> PDF
          </Button>
        </div>
      </Card>

      {rows && (
        <Card className="p-0 overflow-hidden">
          {rows.length === 0 ? (
            <EmptyState title="No transactions in this period" />
          ) : (
            <table className="w-full text-sm">
              <thead className="bg-gray-50 dark:bg-gray-800 text-left text-gray-500 dark:text-gray-400">
                <tr>
                  <th className="px-5 py-3 font-medium">Date</th>
                  <th className="px-5 py-3 font-medium">Type</th>
                  <th className="px-5 py-3 font-medium">Category</th>
                  <th className="px-5 py-3 font-medium">Notes</th>
                  <th className="px-5 py-3 font-medium text-right">Amount</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100 dark:divide-gray-800">
                {rows.map((row, i) => (
                  <tr key={i}>
                    <td className="px-5 py-3">{row.date}</td>
                    <td className="px-5 py-3">
                      <span className={row.type === 'INCOME' ? 'text-green-600' : 'text-red-500'}>{row.type}</span>
                    </td>
                    <td className="px-5 py-3">{row.category}</td>
                    <td className="px-5 py-3 text-gray-500 dark:text-gray-400">{row.notes || '—'}</td>
                    <td className="px-5 py-3 text-right font-medium">{currency(row.amount)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </Card>
      )}
    </div>
  );
}
