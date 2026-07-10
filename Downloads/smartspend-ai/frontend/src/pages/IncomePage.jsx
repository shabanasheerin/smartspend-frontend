import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import toast from 'react-hot-toast';
import { Plus, Pencil, Trash2, ArrowUpCircle } from 'lucide-react';
import { incomeApi } from '../api/incomeApi';
import Card from '../components/common/Card';
import Button from '../components/common/Button';
import Input from '../components/common/Input';
import Select from '../components/common/Select';
import Modal from '../components/common/Modal';
import EmptyState from '../components/common/EmptyState';
import Skeleton from '../components/common/Skeleton';

const SOURCES = ['SALARY', 'FREELANCE', 'BUSINESS', 'BONUS', 'INVESTMENTS', 'OTHER'];

const currency = (v) =>
  new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(v || 0);

export default function IncomePage() {
  const [incomes, setIncomes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState(null);

  const { register, handleSubmit, reset, formState: { errors } } = useForm();

  const load = () => {
    setLoading(true);
    incomeApi
      .list({ size: 50 })
      .then(({ data }) => setIncomes(data.data.content))
      .finally(() => setLoading(false));
  };

  useEffect(load, []);

  const openAdd = () => {
    setEditing(null);
    reset({ amount: '', source: 'SALARY', incomeDate: new Date().toISOString().slice(0, 10), notes: '' });
    setModalOpen(true);
  };

  const openEdit = (income) => {
    setEditing(income);
    reset(income);
    setModalOpen(true);
  };

  const onSubmit = async (formData) => {
    try {
      if (editing) {
        await incomeApi.update(editing.id, formData);
        toast.success('Income updated');
      } else {
        await incomeApi.create(formData);
        toast.success('Income added');
      }
      setModalOpen(false);
      load();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Something went wrong');
    }
  };

  const handleDelete = async (id) => {
    if (!confirm('Delete this income record?')) return;
    try {
      await incomeApi.remove(id);
      toast.success('Income deleted');
      load();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Delete failed');
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-semibold">Income</h1>
        <Button onClick={openAdd} className="flex items-center gap-1.5">
          <Plus size={16} /> Add Income
        </Button>
      </div>

      <Card className="p-0 overflow-hidden">
        {loading ? (
          <div className="p-5 space-y-3">
            {[...Array(4)].map((_, i) => (
              <Skeleton key={i} className="h-10" />
            ))}
          </div>
        ) : incomes.length === 0 ? (
          <EmptyState
            title="No income records yet"
            description="Add your first income to start tracking"
            action={<Button onClick={openAdd}>Add Income</Button>}
          />
        ) : (
          <table className="w-full text-sm">
            <thead className="bg-gray-50 dark:bg-gray-800 text-left text-gray-500 dark:text-gray-400">
              <tr>
                <th className="px-5 py-3 font-medium">Date</th>
                <th className="px-5 py-3 font-medium">Source</th>
                <th className="px-5 py-3 font-medium">Notes</th>
                <th className="px-5 py-3 font-medium text-right">Amount</th>
                <th className="px-5 py-3 font-medium text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100 dark:divide-gray-800">
              {incomes.map((income) => (
                <tr key={income.id} className="hover:bg-gray-50 dark:hover:bg-gray-800/50">
                  <td className="px-5 py-3">{income.incomeDate}</td>
                  <td className="px-5 py-3">
                    <span className="inline-flex items-center gap-1 text-green-600 dark:text-green-400">
                      <ArrowUpCircle size={14} /> {income.source}
                    </span>
                  </td>
                  <td className="px-5 py-3 text-gray-500 dark:text-gray-400">{income.notes || '—'}</td>
                  <td className="px-5 py-3 text-right font-medium">{currency(income.amount)}</td>
                  <td className="px-5 py-3 text-right">
                    <button onClick={() => openEdit(income)} className="p-1.5 text-gray-400 hover:text-primary-600">
                      <Pencil size={15} />
                    </button>
                    <button onClick={() => handleDelete(income.id)} className="p-1.5 text-gray-400 hover:text-red-600">
                      <Trash2 size={15} />
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </Card>

      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title={editing ? 'Edit Income' : 'Add Income'}>
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <Input
            label="Amount"
            type="number"
            step="0.01"
            error={errors.amount?.message}
            {...register('amount', { required: 'Amount is required', min: { value: 0.01, message: 'Must be greater than 0' } })}
          />
          <Select label="Source" {...register('source', { required: true })}>
            {SOURCES.map((s) => (
              <option key={s} value={s}>
                {s}
              </option>
            ))}
          </Select>
          <Input
            label="Date"
            type="date"
            error={errors.incomeDate?.message}
            {...register('incomeDate', { required: 'Date is required' })}
          />
          <Input label="Notes (optional)" {...register('notes')} />

          <Button type="submit" className="w-full">
            {editing ? 'Save Changes' : 'Add Income'}
          </Button>
        </form>
      </Modal>
    </div>
  );
}
