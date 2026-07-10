import { useForm } from 'react-hook-form';
import { useDispatch, useSelector } from 'react-redux';
import { Link, useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { register as registerUser } from '../features/auth/authSlice';
import Input from '../components/common/Input';
import Button from '../components/common/Button';

export default function RegisterPage() {
  const {
    register: formRegister,
    handleSubmit,
    watch,
    formState: { errors },
  } = useForm();
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const status = useSelector((state) => state.auth.status);

  const onSubmit = async (formData) => {
    const { confirmPassword, ...payload } = formData;
    const result = await dispatch(registerUser(payload));
    if (registerUser.fulfilled.match(result)) {
      toast.success('Account created! Check your email to verify your address.');
      navigate('/');
    } else {
      toast.error(result.payload || 'Registration failed');
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-950 px-4">
      <div className="w-full max-w-sm bg-white dark:bg-gray-900 rounded-2xl shadow-sm border border-gray-200 dark:border-gray-800 p-8">
        <h1 className="text-2xl font-bold text-primary-600 dark:text-primary-400 mb-1">Create your account</h1>
        <p className="text-sm text-gray-500 dark:text-gray-400 mb-6">Start tracking smarter, today</p>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <Input
            label="Full name"
            placeholder="Jane Doe"
            error={errors.fullName?.message}
            {...formRegister('fullName', { required: 'Full name is required' })}
          />
          <Input
            label="Email"
            type="email"
            placeholder="you@example.com"
            error={errors.email?.message}
            {...formRegister('email', { required: 'Email is required' })}
          />
          <Input
            label="Password"
            type="password"
            placeholder="At least 8 characters"
            error={errors.password?.message}
            {...formRegister('password', {
              required: 'Password is required',
              minLength: { value: 8, message: 'Must be at least 8 characters' },
            })}
          />
          <Input
            label="Confirm password"
            type="password"
            placeholder="Repeat your password"
            error={errors.confirmPassword?.message}
            {...formRegister('confirmPassword', {
              validate: (value) => value === watch('password') || 'Passwords do not match',
            })}
          />

          <Button type="submit" className="w-full" disabled={status === 'loading'}>
            {status === 'loading' ? 'Creating account...' : 'Create Account'}
          </Button>
        </form>

        <p className="text-sm text-center text-gray-500 dark:text-gray-400 mt-6">
          Already have an account?{' '}
          <Link to="/login" className="text-primary-600 dark:text-primary-400 font-medium hover:underline">
            Sign in
          </Link>
        </p>
      </div>
    </div>
  );
}
