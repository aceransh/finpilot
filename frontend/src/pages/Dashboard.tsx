import { useEffect, useState } from 'react';
import { Box, Typography } from '@mui/material';
// NOTICE: No Grid imports here!
import LinkButton from '../components/LinkButton';
import SyncButton from '../components/SyncButton';
import RecentTransactions from '../components/RecentTransactions';
import SpendingChart from '../components/SpendingChart';
import { getTransactions } from '../api/transactionService';
import type { Transaction } from '../api/transactionService';

const Dashboard = () => {
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchTransactions = async () => {
      try {
        const data = await getTransactions();
        setTransactions(Array.isArray(data) ? data : []);
      } catch (error) {
        console.error('Error fetching transactions:', error);
        setTransactions([]);
      } finally {
        setLoading(false);
      }
    };

    fetchTransactions();
  }, []);

  return (
      <Box>
        <Typography variant="h4" gutterBottom>
          Dashboard
        </Typography>

        <Box sx={{ mb: 3, display: 'flex', gap: 2 }}>
          <LinkButton />
          <SyncButton />
        </Box>

        {loading ? (
            <Typography>Loading...</Typography>
        ) : (
            // BULLETPROOF LAYOUT: Uses simple Flexbox
            // This works on EVERY version of Material UI
            <Box sx={{ display: 'flex', flexDirection: { xs: 'column', md: 'row' }, gap: 3 }}>

              {/* Left Column (Chart) - 40% width on desktop */}
              <Box sx={{ flex: { xs: '1 1 100%', md: '0 0 40%' } }}>
                <SpendingChart transactions={transactions} />
              </Box>

              {/* Right Column (Table) - Takes remaining space */}
              <Box sx={{ flex: 1 }}>
                <RecentTransactions transactions={transactions} />
              </Box>

            </Box>
        )}
      </Box>
  );
};

export default Dashboard;