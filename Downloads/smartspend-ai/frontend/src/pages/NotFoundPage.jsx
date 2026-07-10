import { Link } from 'react-router-dom';

export default function NotFoundPage() {
  return (
    <div className="min-h-screen flex flex-col items-center justify-center gap-3 text-center px-4">
      <h1 className="text-4xl font-bold text-primary-600">404</h1>
      <p className="text-gray-500 dark:text-gray-400">This page doesn't exist.</p>
      <Link to="/" className="text-primary-600 dark:text-primary-400 hover:underline text-sm">
        Back to dashboard
      </Link>
    </div>
  );
}
