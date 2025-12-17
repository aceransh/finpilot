import { useEffect, useState } from 'react';
import { Typography, Box } from '@mui/material';
import TransactionTable from '../components/TransactionTable';
import {
  getTransactions,
  updateTransaction,
  createCategory,
  getCategories,
  type Transaction,
  type Category,
} from '../api/transactionService';

const TransactionsPage = () => {
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(true);

  const fetchData = async () => {
    try {
      const [txnsData, catsData] = await Promise.all([
        getTransactions(),
        getCategories(),
      ]);
      setTransactions(Array.isArray(txnsData) ? txnsData : []);
      setCategories(Array.isArray(catsData) ? catsData : []);
    } catch (error) {
      console.error('Error fetching data:', error);
      setTransactions([]);
      setCategories([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleUpdateTransaction = async (
    id: string,
    data: { categoryId?: number; description?: string }
  ) => {
    try {
      await updateTransaction(id, data);
      await fetchData(); // Refresh data
    } catch (error) {
      console.error('Error updating transaction:', error);
      throw error;
    }
  };

  const handleCreateCategory = async (data: { name: string; colorHex?: string }) => {
    try {
      const newCategory = await createCategory(data);
      setCategories((prev) => [...prev, newCategory]);
      return newCategory;
    } catch (error) {
      console.error('Error creating category:', error);
      throw error;
    }
  };

  if (loading) {
    return <Typography>Loading...</Typography>;
  }

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Transactions
      </Typography>
      <TransactionTable
        transactions={transactions}
        categories={categories}
        onUpdate={handleUpdateTransaction}
        onCreateCategory={handleCreateCategory}
      />
    </Box>
  );
};

export default TransactionsPage;

