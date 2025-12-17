import {
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Typography,
} from '@mui/material';
import { format } from 'date-fns';
// FIX: Ensure "type" is here
import type { Transaction } from '../api/transactionService';

interface RecentTransactionsProps {
  transactions: Transaction[];
}

const RecentTransactions = ({ transactions }: RecentTransactionsProps) => {
  return (
    <TableContainer component={Paper}>
      <Typography variant="h6" sx={{ p: 2 }}>
        Recent Transactions
      </Typography>
      <Table>
        <TableHead>
          <TableRow>
            <TableCell>Date</TableCell>
            <TableCell>Description</TableCell>
            <TableCell align="right">Amount</TableCell>
            <TableCell>Category</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {transactions.length === 0 ? (
            <TableRow>
              <TableCell colSpan={4} align="center">
                No transactions found
              </TableCell>
            </TableRow>
          ) : (
            transactions.map((transaction) => (
              <TableRow key={transaction.id}>
                <TableCell>
                  {format(new Date(transaction.date), 'MM/dd/yyyy')}
                </TableCell>
                <TableCell>{transaction.description || 'N/A'}</TableCell>
                <TableCell
                  align="right"
                  sx={{
                    color: transaction.amount < 0 ? 'error.main' : 'success.main',
                    fontWeight: 'bold',
                  }}
                >
                  ${transaction.amount.toFixed(2)}
                </TableCell>
                <TableCell>
                  {transaction.category ? (
                    <span
                      style={{
                        color: transaction.category.colorHex,
                        fontWeight: 'bold',
                      }}
                    >
                      {transaction.category.name}
                    </span>
                  ) : (
                    'Uncategorized'
                  )}
                </TableCell>
              </TableRow>
            ))
          )}
        </TableBody>
      </Table>
    </TableContainer>
  );
};

export default RecentTransactions;

