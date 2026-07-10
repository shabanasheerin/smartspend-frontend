export default function Select({ label, error, children, className = '', ...rest }) {
  return (
    <div className="space-y-1">
      {label && <label className="text-sm font-medium text-gray-700 dark:text-gray-300">{label}</label>}
      <select
        className={`w-full px-3 py-2 rounded-lg border text-sm bg-white dark:bg-gray-800 border-gray-300 dark:border-gray-700 focus:outline-none focus:ring-2 focus:ring-primary-500 ${
          error ? 'border-red-500' : ''
        } ${className}`}
        {...rest}
      >
        {children}
      </select>
      {error && <p className="text-xs text-red-500">{error}</p>}
    </div>
  );
}
