import { useEffect, useState } from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import { authApi } from '../api/authApi';

export default function VerifyEmailPage() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');
  const [status, setStatus] = useState('verifying');

  useEffect(() => {
    if (!token) {
      setStatus('error');
      return;
    }
    authApi
      .verifyEmail(token)
      .then(() => setStatus('success'))
      .catch(() => setStatus('error'));
  }, [token]);

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-950 px-4">
      <div className="w-full max-w-sm bg-white dark:bg-gray-900 rounded-2xl shadow-sm border border-gray-200 dark:border-gray-800 p-8 text-center">
        {status === 'verifying' && <p>Verifying your email...</p>}
        {status === 'success' && (
          <>
            <p className="text-green-600 dark:text-green-400 font-medium">Email verified successfully!</p>
            <Link to="/login" className="text-primary-600 dark:text-primary-400 text-sm hover:underline mt-4 inline-block">
              Continue to sign in
            </Link>
          </>
        )}
        {status === 'error' && (
          <>
            <p className="text-red-500 font-medium">This verification link is invalid or expired.</p>
            <Link to="/login" className="text-primary-600 dark:text-primary-400 text-sm hover:underline mt-4 inline-block">
              Back to sign in
            </Link>
          </>
        )}
      </div>
    </div>
  );
}
