import { useEffect, useState } from 'react';
import { Bell, BellOff } from 'lucide-react';
import { notificationApi } from '../api/notificationApi';
import Card from '../components/common/Card';
import EmptyState from '../components/common/EmptyState';
import Skeleton from '../components/common/Skeleton';

const typeColors = {
  BUDGET_EXCEEDED: 'bg-red-50 text-red-600 dark:bg-red-900/30 dark:text-red-300',
  GOAL_ACHIEVED: 'bg-green-50 text-green-600 dark:bg-green-900/30 dark:text-green-300',
  MONTHLY_SUMMARY: 'bg-primary-50 text-primary-600 dark:bg-primary-900/30 dark:text-primary-300',
  RECURRING_REMINDER: 'bg-amber-50 text-amber-600 dark:bg-amber-900/30 dark:text-amber-300',
  PASSWORD_CHANGED: 'bg-gray-100 text-gray-600 dark:bg-gray-800 dark:text-gray-300',
  SYSTEM: 'bg-gray-100 text-gray-600 dark:bg-gray-800 dark:text-gray-300',
};

export default function NotificationsPage() {
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);

  const load = () => {
    setLoading(true);
    notificationApi
      .list({ size: 30 })
      .then(({ data }) => setNotifications(data.data.content))
      .finally(() => setLoading(false));
  };

  useEffect(load, []);

  const handleMarkRead = async (id) => {
    await notificationApi.markRead(id);
    setNotifications((prev) => prev.map((n) => (n.id === id ? { ...n, isRead: true } : n)));
  };

  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold">Notifications</h1>

      <Card className="p-0 overflow-hidden">
        {loading ? (
          <div className="p-5 space-y-3">
            {[...Array(5)].map((_, i) => (
              <Skeleton key={i} className="h-12" />
            ))}
          </div>
        ) : notifications.length === 0 ? (
          <EmptyState title="You're all caught up" description="No notifications yet" />
        ) : (
          <div className="divide-y divide-gray-100 dark:divide-gray-800">
            {notifications.map((n) => (
              <div
                key={n.id}
                className={`flex items-start gap-3 px-5 py-4 ${!n.isRead ? 'bg-primary-50/40 dark:bg-primary-900/10' : ''}`}
              >
                <div className={`w-9 h-9 shrink-0 rounded-lg flex items-center justify-center ${typeColors[n.type] || typeColors.SYSTEM}`}>
                  {n.isRead ? <BellOff size={16} /> : <Bell size={16} />}
                </div>
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium">{n.title}</p>
                  <p className="text-sm text-gray-500 dark:text-gray-400 mt-0.5">{n.message}</p>
                  <p className="text-xs text-gray-400 mt-1">{new Date(n.createdAt).toLocaleString()}</p>
                </div>
                {!n.isRead && (
                  <button
                    onClick={() => handleMarkRead(n.id)}
                    className="text-xs text-primary-600 dark:text-primary-400 hover:underline whitespace-nowrap"
                  >
                    Mark read
                  </button>
                )}
              </div>
            ))}
          </div>
        )}
      </Card>
    </div>
  );
}
