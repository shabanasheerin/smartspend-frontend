import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import toast from 'react-hot-toast';
import { Plus, Trash2, PlusCircle, Target } from 'lucide-react';
import { goalApi } from '../api/goalApi';
import Card from '../components/common/Card';
import Button from '../components/common/Button';
import Input from '../components/common/Input';
import Modal from '../components/common/Modal';
import EmptyState from '../components/common/EmptyState';
import Skeleton from '../components/common/Skeleton';

const currency = (v) =>
  new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(v || 0);

export default function GoalsPage() {
  const [goals, setGoals] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [contributeTarget, setContributeTarget] = useState(null);

  const goalForm = useForm();
  const contributeForm = useForm();

  const load = () => {
    setLoading(true);
    goalApi
      .list()
      .then(({ data }) => setGoals(data.data))
      .finally(() => setLoading(false));
  };

  useEffect(load, []);

  const openAdd = () => {
    goalForm.reset({ title: '', targetAmount: '', targetDate: '' });
    setModalOpen(true);
  };

  const onCreateGoal = async (formData) => {
    try {
      await goalApi.create(formData);
      toast.success('Goal created');
      setModalOpen(false);
      load();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Something went wrong');
    }
  };

  const onContribute = async (formData) => {
    try {
      await goalApi.contribute(contributeTarget.id, { amount: Number(formData.amount) });
      toast.success('Contribution added');
      setContributeTarget(null);
      load();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Something went wrong');
    }
  };

  const handleDelete = async (id) => {
    if (!confirm('Delete this goal?')) return;
    try {
      await goalApi.remove(id);
      toast.success('Goal deleted');
      load();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Delete failed');
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-semibold">Savings Goals</h1>
        <Button onClick={openAdd} className="flex items-center gap-1.5">
          <Plus size={16} /> New Goal
        </Button>
      </div>

      {loading ? (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {[...Array(3)].map((_, i) => (
            <Skeleton key={i} className="h-40" />
          ))}
        </div>
      ) : goals.length === 0 ? (
        <Card>
          <EmptyState
            title="No savings goals yet"
            description="Create a goal like 'Buy Laptop' or 'Emergency Fund'"
            action={<Button onClick={openAdd}>New Goal</Button>}
          />
        </Card>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {goals.map((goal) => {
            const pct = Math.min(Number(goal.progressPercentage), 100);
            return (
              <Card key={goal.id}>
                <div className="flex items-start justify-between mb-2">
                  <div className="flex items-center gap-2">
                    <Target size={16} className="text-primary-500" />
                    <p className="font-medium">{goal.title}</p>
                  </div>
                  <button onClick={() => handleDelete(goal.id)} className="text-gray-400 hover:text-red-600">
                    <Trash2 size={15} />
                  </button>
                </div>

                <p className="text-xs text-gray-500 dark:text-gray-400 mb-3">
                  {currency(goal.savedAmount)} of {currency(goal.targetAmount)}
                  {goal.targetDate && ` · by ${goal.targetDate}`}
                </p>

                <div className="w-full h-2 rounded-full bg-gray-100 dark:bg-gray-800 overflow-hidden">
                  <div
                    className={`h-full transition-all ${goal.status === 'ACHIEVED' ? 'bg-green-500' : 'bg-primary-500'}`}
                    style={{ width: `${pct}%` }}
                  />
                </div>

                <div className="flex items-center justify-between mt-3">
                  <span
                    className={`text-xs font-medium ${
                      goal.status === 'ACHIEVED' ? 'text-green-600 dark:text-green-400' : 'text-gray-500 dark:text-gray-400'
                    }`}
                  >
                    {goal.status === 'ACHIEVED' ? 'Achieved 🎉' : `${pct.toFixed(1)}%`}
                  </span>
                  {goal.status === 'IN_PROGRESS' && (
                    <button
                      onClick={() => {
                        contributeForm.reset({ amount: '' });
                        setContributeTarget(goal);
                      }}
                      className="text-xs flex items-center gap-1 text-primary-600 dark:text-primary-400 hover:underline"
                    >
                      <PlusCircle size={14} /> Add funds
                    </button>
                  )}
                </div>
              </Card>
            );
          })}
        </div>
      )}

      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title="New Savings Goal">
        <form onSubmit={goalForm.handleSubmit(onCreateGoal)} className="space-y-4">
          <Input
            label="Title"
            placeholder="e.g. Buy Laptop"
            error={goalForm.formState.errors.title?.message}
            {...goalForm.register('title', { required: 'Title is required' })}
          />
          <Input
            label="Target amount"
            type="number"
            step="0.01"
            error={goalForm.formState.errors.targetAmount?.message}
            {...goalForm.register('targetAmount', { required: 'Target amount is required', min: 0.01 })}
          />
          <Input label="Target date (optional)" type="date" {...goalForm.register('targetDate')} />
          <Button type="submit" className="w-full">
            Create Goal
          </Button>
        </form>
      </Modal>

      <Modal open={!!contributeTarget} onClose={() => setContributeTarget(null)} title={`Add funds to ${contributeTarget?.title || ''}`}>
        <form onSubmit={contributeForm.handleSubmit(onContribute)} className="space-y-4">
          <Input
            label="Amount"
            type="number"
            step="0.01"
            error={contributeForm.formState.errors.amount?.message}
            {...contributeForm.register('amount', { required: 'Amount is required', min: 0.01 })}
          />
          <Button type="submit" className="w-full">
            Contribute
          </Button>
        </form>
      </Modal>
    </div>
  );
}
