import { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import { Menu, Sun, Moon, Bell, LogOut } from 'lucide-react';
import { toggleSidebar, toggleTheme } from '../../features/auth/uiSlice';
import { logout } from '../../features/auth/authSlice';
import { notificationApi } from '../../api/notificationApi';

export default function Navbar() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const theme = useSelector((state) => state.ui.theme);
  const user = useSelector((state) => state.auth.user);
  const [unreadCount, setUnreadCount] = useState(0);

  useEffect(() => {
    notificationApi
      .unreadCount()
      .then(({ data }) => setUnreadCount(data.data.unreadCount))
      .catch(() => {});
  }, []);

  const handleLogout = async () => {
    await dispatch(logout());
    navigate('/login');
  };

  return (
    <header className="h-16 flex items-center justify-between px-4 lg:px-6 border-b border-gray-200 dark:border-gray-800 bg-white dark:bg-gray-900">
      <button className="lg:hidden" onClick={() => dispatch(toggleSidebar())}>
        <Menu size={22} />
      </button>

      <div className="hidden lg:block text-sm text-gray-500 dark:text-gray-400">
        Welcome back{user?.fullName ? `, ${user.fullName.split(' ')[0]}` : ''} 👋
      </div>

      <div className="flex items-center gap-4">
        <button
          onClick={() => dispatch(toggleTheme())}
          className="p-2 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors"
          aria-label="Toggle theme"
        >
          {theme === 'light' ? <Moon size={18} /> : <Sun size={18} />}
        </button>

        <button
          onClick={() => navigate('/notifications')}
          className="relative p-2 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors"
          aria-label="Notifications"
        >
          <Bell size={18} />
          {unreadCount > 0 && (
            <span className="absolute -top-0.5 -right-0.5 bg-red-500 text-white text-[10px] rounded-full w-4 h-4 flex items-center justify-center">
              {unreadCount > 9 ? '9+' : unreadCount}
            </span>
          )}
        </button>

        <div className="flex items-center gap-2">
          <div className="w-8 h-8 rounded-full bg-primary-500 text-white flex items-center justify-center text-sm font-semibold">
            {user?.fullName?.charAt(0)?.toUpperCase() || 'U'}
          </div>
          <button
            onClick={handleLogout}
            className="p-2 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors"
            aria-label="Log out"
            title="Log out"
          >
            <LogOut size={18} />
          </button>
        </div>
      </div>
    </header>
  );
}
