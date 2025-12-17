import { useState } from 'react';
import { Button, CircularProgress } from '@mui/material';
import RefreshIcon from '@mui/icons-material/Refresh';
import { syncTransactions } from '../api/transactionService';

const SyncButton = () => {
  const [loading, setLoading] = useState(false);

  const handleSync = async () => {
    setLoading(true);
    try {
      await syncTransactions();
      // Reload the page to show new data
      window.location.reload();
    } catch (error) {
      console.error('Error syncing transactions:', error);
      alert('Failed to sync transactions. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Button
      variant="contained"
      color="primary"
      startIcon={loading ? <CircularProgress size={20} color="inherit" /> : <RefreshIcon />}
      onClick={handleSync}
      disabled={loading}
    >
      {loading ? 'Syncing...' : 'Sync'}
    </Button>
  );
};

export default SyncButton;

