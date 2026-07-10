import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import toast from 'react-hot-toast';
import { Plus, Trash2 } from 'lucide-react';
import { budgetApi } from '../api/budgetApi';
import { categoryApi } from '../api/categoryApi';
import Card from '../components/common/Card';
import Button from '../components/common/Button';
import Input from '../components/common/Input';
import Select from '../components/common/Select';
import Modal from '../components/common/Modal';
import EmptyState from '../components/common/EmptyState';
import Skeleton from '../components/common/Skeleton';

const currency = (v) =>
  new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(v || 0);

const now = new Date();

export default function BudgetPage() {
  const [budgets, setBudgets] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [month] = useState(now.getMonth() + 1);
  const [year] = useState(now.getFullYear());

  const { register, handleSubmit, reset, formState: { errors } } = useForm();

  const load = () => {
    setLoading(true);
    Promise.all([budgetApi.list(month, year), categoryApi.list()])
      .then(([budgetRes, catRes]) => {
        setBudgets(budgetRes.data.data);
        setCategories(catRes.data.data);
      })
      .finally(() => setLoading(false));
  };

  useEffect(load, []);

  const openAdd = () => {
    reset({ categoryId: categories[0]?.id || '', limitAmount: '', month, year, alertThresholdPercent: 80 });
    setModalOpen(true);
  };

  const onSubmit = async (formData) => {
    try {
      await budgetApi.create({ ...formData, categoryId: Number(formData.categoryId), month, year });
      toast.success('Budget created');
      setModalOpen(false);
      load();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Something went wrong');
    }
  };

  const handleDelete = async (id) => {
    if (!confirm('Delete this budget?')) return;
    try {
      await budgetApi.remove(id);
      toast.success('Budget deleted');
      load();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Delete failed');
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-semibold">
          Budgets — {now.toLocaleString('default', { month: 'long' })} {year}
        </h1>
        <Button onClick={openAdd} className="flex items-center gap-1.5">
          <Plus size={16} /> New Budget
        </Button>
      </div>

      {loading ? (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {[...Array(3)].map((_, i) => (
            <Skeleton key={i} className="h-32" />
          ))}
        </div>
      ) : budgets.length === 0 ? (
        <Card>
          <EmptyState
            title="No budgets set for this month"
            description="Create a category budget to start tracking limits"
            action={<Button onClick={openAdd}>New Budget</Button>}
          />
        </Card>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {budgets.map((budget) => {
            const pct = Math.min(Number(budget.percentageUsed), 100);
            const barColor = budget.alertTriggered ? 'bg-red-500' : 'bg-primary-500';
            return (
              <Card key={budget.id}>
                <div className="flex items-start justify-between mb-3">
                  <div>
                    <p className="font-medium">{budget.categoryName}</p>
                    <p className="text-xs text-gray-500 dark:text-gray-400">
                      {currency(budget.usedAmount)} of {currency(budget.limitAmount)}
                    </p>
                  </div>
                  <button onClick={() => handleDelete(budget.id)} className="text-gray-400 hover:text-red-600">
                    <Trash2 size={15} />
                  </button>
                </div>
                <div className="w-full h-2 rounded-full bg-gray-100 dark:bg-gray-800 overflow-hidden">
                  <div className={`h-full ${barColor} transition-all`} style={{ width: `${pct}%` }} />
                </div>
                <div className="flex justify-between mt-2 text-xs">
                  <span className={budget.alertTriggered ? 'text-red-500 font-medium' : 'text-gray-500 dark:text-gray-400'}>
                    {Number(budget.percentageUsed).toFixed(1)}% used
                  </span>
                  <span className="text-gray-500 dark:text-gray-400">{currency(budget.remainingAmount)} left</span>
                </div>
              </Card>
            );
          })}
        </div>
      )}

      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title="New Budget">
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <Select label="Category" error={errors.categoryId?.message} {...register('categoryId', { required: true })}>
            {categories.map((c) => (
              <option key={c.id} value={c.id}>
                {c.name}
              </option>
            ))}
          </Select>
          <Input
            label="Monthly limit"
            type="number"
            step="0.01"
            error={errors.limitAmount?.message}
            {...register('limitAmount', { required: 'Limit is required', min: { value: 0.01, message: 'Must be greater than 0' } })}
          />
          <Input
            label="Alert threshold (%)"
            type="number"
            defaultValue={80}
            {...register('alertThresholdPercent')}
          />
          <Button type="submit" className="w-full">
            Create Budget
          </Button>
        </form>
      </Modal>
    </div>
  );
}
