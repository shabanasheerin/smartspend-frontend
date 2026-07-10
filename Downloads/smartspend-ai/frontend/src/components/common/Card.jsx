export default function Card({ children, className = '' }) {
  return (
    <div
      className={`bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl shadow-sm p-5 ${className}`}
    >
      {children}
    </div>
  );
}
