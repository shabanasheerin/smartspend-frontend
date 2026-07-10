import { useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import { Users, ShieldCheck, ShieldOff, Trash2 } from 'lucide-react';
import apiClient from '../api/apiClient';
import Card from '../components/common/Card';
import StatCard from '../components/common/StatCard';
import Skeleton from '../components/common/Skeleton';

const currency = (v) =>
  new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(v || 0);

export default function AdminPage() {
  const [stats, setStats] = useState(null);
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);

  const load = () => {
    setLoading(true);
    Promise.all([apiClient.get('/admin/stats'), apiClient.get('/admin/users', { params: { size: 20 } })])
      .then(([statsRes, usersRes]) => {
        setStats(statsRes.data.data);
        setUsers(usersRes.data.data.content);
      })
      .finally(() => setLoading(false));
  };

  useEffect(load, []);

  const toggleBlock = async (user) => {
    try {
      if (user.enabled) {
        await apiClient.patch(`/admin/users/${user.id}/block`);
        toast.success('User blocked');
      } else {
        await apiClient.patch(`/admin/users/${user.id}/unblock`);
        toast.success('User unblocked');
      }
      load();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Action failed');
    }
  };

  const deleteUser = async (id) => {
    if (!confirm('Delete this user permanently?')) return;
    try {
      await apiClient.delete(`/admin/users/${id}`);
      toast.success('User deleted');
      load();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Delete failed');
    }
  };

  if (loading) {
    return (
      <div className="space-y-6">
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
          {[...Array(4)].map((_, i) => <Skeleton key={i} className="h-24" />)}
        </div>
        <Skeleton className="h-64" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold">Admin Panel</h1>

      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard label="Total Users" value={stats.totalUsers} icon={Users} tone="primary" />
        <StatCard label="Active Users" value={stats.activeUsers} icon={ShieldCheck} tone="green" />
        <StatCard label="Blocked Users" value={stats.blockedUsers} icon={ShieldOff} tone="red" />
        <StatCard label="Total Expense Volume" value={currency(stats.totalExpenseAmountAllUsers)} tone="amber" />
      </div>

      <Card className="p-0 overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-gray-50 dark:bg-gray-800 text-left text-gray-500 dark:text-gray-400">
            <tr>
              <th className="px-5 py-3 font-medium">Name</th>
              <th className="px-5 py-3 font-medium">Email</th>
              <th className="px-5 py-3 font-medium">Status</th>
              <th className="px-5 py-3 font-medium text-right">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100 dark:divide-gray-800">
            {users.map((u) => (
              <tr key={u.id}>
                <td className="px-5 py-3">{u.fullName}</td>
                <td className="px-5 py-3 text-gray-500 dark:text-gray-400">{u.email}</td>
                <td className="px-5 py-3">
                  <span
                    className={`text-xs px-2 py-0.5 rounded-full ${
                      u.enabled
                        ? 'bg-green-50 text-green-600 dark:bg-green-900/30 dark:text-green-300'
                        : 'bg-red-50 text-red-600 dark:bg-red-900/30 dark:text-red-300'
                    }`}
                  >
                    {u.enabled ? 'Active' : 'Blocked'}
                  </span>
                </td>
                <td className="px-5 py-3 text-right whitespace-nowrap">
                  <button onClick={() => toggleBlock(u)} className="p-1.5 text-gray-400 hover:text-primary-600" title={u.enabled ? 'Block' : 'Unblock'}>
                    {u.enabled ? <ShieldOff size={15} /> : <ShieldCheck size={15} />}
                  </button>
                  <button onClick={() => deleteUser(u.id)} className="p-1.5 text-gray-400 hover:text-red-600" title="Delete">
                    <Trash2 size={15} />
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </Card>
    </div>
  );
}
