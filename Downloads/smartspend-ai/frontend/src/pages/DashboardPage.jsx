import { useEffect, useState } from 'react';
import { Line, Doughnut } from 'react-chartjs-2';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  ArcElement,
  Tooltip,
  Legend,
} from 'chart.js';
import { Wallet, TrendingUp, TrendingDown, PiggyBank, Sparkles } from 'lucide-react';
import { dashboardApi } from '../api/dashboardApi';
import { insightApi } from '../api/insightApi';
import StatCard from '../components/common/StatCard';
import Card from '../components/common/Card';
import Skeleton from '../components/common/Skeleton';
import EmptyState from '../components/common/EmptyState';

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, ArcElement, Tooltip, Legend);

const currency = (value) =>
  new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(
    value || 0
  );

export default function DashboardPage() {
  const [summary, setSummary] = useState(null);
  const [insights, setInsights] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([dashboardApi.summary(), insightApi.list()])
      .then(([summaryRes, insightRes]) => {
        setSummary(summaryRes.data.data);
        setInsights(insightRes.data.data);
      })
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return (
      <div className="space-y-6">
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          {[...Array(4)].map((_, i) => (
            <Skeleton key={i} className="h-24" />
          ))}
        </div>
        <Skeleton className="h-80" />
      </div>
    );
  }

  const trendLabels = summary.monthlyExpenseTrend.map((p) => p.month);
  const lineData = {
    labels: trendLabels,
    datasets: [
      {
        label: 'Expenses',
        data: summary.monthlyExpenseTrend.map((p) => p.amount),
        borderColor: '#ef4444',
        backgroundColor: 'rgba(239,68,68,0.1)',
        tension: 0.35,
        fill: true,
      },
      {
        label: 'Income',
        data: summary.monthlyIncomeTrend.map((p) => p.amount),
        borderColor: '#22c55e',
        backgroundColor: 'rgba(34,197,94,0.1)',
        tension: 0.35,
        fill: true,
      },
    ],
  };

  const categoryEntries = Object.entries(summary.categoryDistribution || {});
  const doughnutData = {
    labels: categoryEntries.map(([name]) => name),
    datasets: [
      {
        data: categoryEntries.map(([, amount]) => amount),
        backgroundColor: [
          '#2f5dff', '#22c55e', '#f59e0b', '#ef4444', '#8b5cf6',
          '#06b6d4', '#ec4899', '#84cc16', '#f97316', '#64748b', '#14b8a6',
        ],
        borderWidth: 0,
      },
    ],
  };

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard label="Current Balance" value={currency(summary.currentBalance)} icon={Wallet} tone="primary" />
        <StatCard label="Monthly Income" value={currency(summary.monthlyIncome)} icon={TrendingUp} tone="green" />
        <StatCard label="Monthly Expenses" value={currency(summary.monthlyExpenses)} icon={TrendingDown} tone="red" />
        <StatCard label="Total Savings" value={currency(summary.totalSavings)} icon={PiggyBank} tone="amber" />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <Card className="lg:col-span-2">
          <h3 className="font-semibold mb-4">Income vs Expense Trend</h3>
          <Line data={lineData} options={{ responsive: true, plugins: { legend: { position: 'bottom' } } }} />
        </Card>

        <Card>
          <h3 className="font-semibold mb-4">This Month by Category</h3>
          {categoryEntries.length === 0 ? (
            <EmptyState title="No expenses yet this month" />
          ) : (
            <Doughnut data={doughnutData} options={{ plugins: { legend: { position: 'bottom', labels: { boxWidth: 10 } } } }} />
          )}
        </Card>
      </div>

      <Card>
        <div className="flex items-center gap-2 mb-4">
          <Sparkles size={18} className="text-primary-500" />
          <h3 className="font-semibold">AI Insights</h3>
        </div>
        {insights.length === 0 ? (
          <EmptyState title="Add a few expenses to unlock insights" />
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            {insights.map((insight) => (
              <div
                key={insight.type}
                className="p-4 rounded-lg bg-primary-50 dark:bg-primary-900/20 border border-primary-100 dark:border-primary-900/40"
              >
                <p className="text-sm font-medium text-primary-700 dark:text-primary-300">{insight.title}</p>
                <p className="text-sm text-gray-600 dark:text-gray-300 mt-1">{insight.message}</p>
              </div>
            ))}
          </div>
        )}
      </Card>
    </div>
  );
}
