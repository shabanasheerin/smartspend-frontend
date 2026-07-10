import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import toast from 'react-hot-toast';
import { Plus, Pencil, Trash2, ArrowDownCircle, Paperclip } from 'lucide-react';
import { expenseApi } from '../api/expenseApi';
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

export default function ExpensePage() {
  const [expenses, setExpenses] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState(null);

  const { register, handleSubmit, watch, reset, formState: { errors } } = useForm();
  const isRecurring = watch('recurring');

  const load = () => {
    setLoading(true);
    Promise.all([expenseApi.list({ size: 50 }), categoryApi.list()])
      .then(([expRes, catRes]) => {
        setExpenses(expRes.data.data.content);
        setCategories(catRes.data.data);
      })
      .finally(() => setLoading(false));
  };

  useEffect(load, []);

  const openAdd = () => {
    setEditing(null);
    reset({
      categoryId: categories[0]?.id || '',
      amount: '',
      expenseDate: new Date().toISOString().slice(0, 10),
      notes: '',
      recurring: false,
      recurrenceFrequency: 'MONTHLY',
    });
    setModalOpen(true);
  };

  const openEdit = (expense) => {
    setEditing(expense);
    reset({
      categoryId: expense.categoryId,
      amount: expense.amount,
      expenseDate: expense.expenseDate,
      notes: expense.notes,
      recurring: expense.recurring,
      recurrenceFrequency: expense.recurrenceFrequency || 'MONTHLY',
    });
    setModalOpen(true);
  };

  const onSubmit = async (formData) => {
    const payload = { ...formData, categoryId: Number(formData.categoryId) };
    try {
      if (editing) {
        await expenseApi.update(editing.id, payload);
        toast.success('Expense updated');
      } else {
        await expenseApi.create(payload);
        toast.success('Expense added');
      }
      setModalOpen(false);
      load();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Something went wrong');
    }
  };

  const handleDelete = async (id) => {
    if (!confirm('Delete this expense?')) return;
    try {
      await expenseApi.remove(id);
      toast.success('Expense deleted');
      load();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Delete failed');
    }
  };

  const handleUploadReceipt = async (expenseId, file) => {
    if (!file) return;
    try {
      await expenseApi.uploadReceipt(expenseId, file);
      toast.success('Receipt uploaded');
    } catch (err) {
      toast.error(err.response?.data?.message || 'Upload failed');
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-semibold">Expenses</h1>
        <Button onClick={openAdd} className="flex items-center gap-1.5">
          <Plus size={16} /> Add Expense
        </Button>
      </div>

      <Card className="p-0 overflow-hidden">
        {loading ? (
          <div className="p-5 space-y-3">
            {[...Array(4)].map((_, i) => (
              <Skeleton key={i} className="h-10" />
            ))}
          </div>
        ) : expenses.length === 0 ? (
          <EmptyState
            title="No expenses yet"
            description="Add your first expense to start tracking"
            action={<Button onClick={openAdd}>Add Expense</Button>}
          />
        ) : (
          <table className="w-full text-sm">
            <thead className="bg-gray-50 dark:bg-gray-800 text-left text-gray-500 dark:text-gray-400">
              <tr>
                <th className="px-5 py-3 font-medium">Date</th>
                <th className="px-5 py-3 font-medium">Category</th>
                <th className="px-5 py-3 font-medium">Notes</th>
                <th className="px-5 py-3 font-medium text-right">Amount</th>
                <th className="px-5 py-3 font-medium text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100 dark:divide-gray-800">
              {expenses.map((expense) => (
                <tr key={expense.id} className="hover:bg-gray-50 dark:hover:bg-gray-800/50">
                  <td className="px-5 py-3">{expense.expenseDate}</td>
                  <td className="px-5 py-3">
                    <span className="inline-flex items-center gap-1 text-red-500 dark:text-red-400">
                      <ArrowDownCircle size={14} /> {expense.categoryName}
                      {expense.recurring && (
                        <span className="ml-1 text-[10px] px-1.5 py-0.5 rounded bg-amber-100 text-amber-700 dark:bg-amber-900/40 dark:text-amber-300">
                          {expense.recurrenceFrequency}
                        </span>
                      )}
                    </span>
                  </td>
                  <td className="px-5 py-3 text-gray-500 dark:text-gray-400">{expense.notes || '—'}</td>
                  <td className="px-5 py-3 text-right font-medium">{currency(expense.amount)}</td>
                  <td className="px-5 py-3 text-right whitespace-nowrap">
                    <label className="p-1.5 text-gray-400 hover:text-primary-600 cursor-pointer inline-block" title="Attach receipt">
                      <Paperclip size={15} />
                      <input
                        type="file"
                        accept="image/jpeg,image/png,image/webp,application/pdf"
                        className="hidden"
                        onChange={(e) => handleUploadReceipt(expense.id, e.target.files[0])}
                      />
                    </label>
                    <button onClick={() => openEdit(expense)} className="p-1.5 text-gray-400 hover:text-primary-600">
                      <Pencil size={15} />
                    </button>
                    <button onClick={() => handleDelete(expense.id)} className="p-1.5 text-gray-400 hover:text-red-600">
                      <Trash2 size={15} />
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </Card>

      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title={editing ? 'Edit Expense' : 'Add Expense'}>
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <Select label="Category" error={errors.categoryId?.message} {...register('categoryId', { required: 'Category is required' })}>
            {categories.map((c) => (
              <option key={c.id} value={c.id}>
                {c.name}
              </option>
            ))}
          </Select>
          <Input
            label="Amount"
            type="number"
            step="0.01"
            error={errors.amount?.message}
            {...register('amount', { required: 'Amount is required', min: { value: 0.01, message: 'Must be greater than 0' } })}
          />
          <Input
            label="Date"
            type="date"
            error={errors.expenseDate?.message}
            {...register('expenseDate', { required: 'Date is required' })}
          />
          <Input label="Notes (optional)" {...register('notes')} />

          <label className="flex items-center gap-2 text-sm">
            <input type="checkbox" {...register('recurring')} />
            Recurring expense
          </label>

          {isRecurring && (
            <Select label="Frequency" {...register('recurrenceFrequency')}>
              <option value="DAILY">Daily</option>
              <option value="WEEKLY">Weekly</option>
              <option value="MONTHLY">Monthly</option>
              <option value="YEARLY">Yearly</option>
            </Select>
          )}

          <Button type="submit" className="w-full">
            {editing ? 'Save Changes' : 'Add Expense'}
          </Button>
        </form>
      </Modal>
    </div>
  );
}
