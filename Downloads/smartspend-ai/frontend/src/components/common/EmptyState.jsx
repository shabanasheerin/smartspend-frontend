export default function EmptyState({ title, description, action }) {
  return (
    <div className="text-center py-12 text-gray-500 dark:text-gray-400">
      <p className="font-medium text-gray-700 dark:text-gray-200">{title}</p>
      {description && <p className="text-sm mt-1">{description}</p>}
      {action && <div className="mt-4">{action}</div>}
    </div>
  );
}
