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
import type { Transaction } from '../api/transactionService';

interface RecentTransactionsProps {
  transactions: Transaction[];
}

const RecentTransactions = ({ transactions }: RecentTransactionsProps) => {
  return (
      // FIX 1: Add fixed height and scroll
      <TableContainer component={Paper} sx={{ maxHeight: 400, overflow: 'auto' }}>
        <Typography variant="h6" sx={{ p: 2, position: 'sticky', top: 0, bgcolor: 'background.paper', zIndex: 1 }}>
          Recent Transactions
        </Typography>

        {/* FIX 2: Sticky Header so you don't lose context while scrolling */}
        <Table stickyHeader>
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
                // FIX 3: Slice to show only the last 50 items
                transactions.slice(0, 50).map((transaction) => (
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
                        {/* LOGIC: Check Custom Category -> Plaid Category -> Uncategorized */}
                        {transaction.category ? (
                            <span
                                style={{
                                  color: transaction.category.colorHex,
                                  fontWeight: 'bold',
                                }}
                            >
                      {transaction.category.name}
                    </span>
                        ) : transaction.plaidCategory ? (
                            <span
                                style={{
                                  color: '#2979ff', // FinPilot Blue for auto-categories
                                  fontWeight: 'bold',
                                  textTransform: 'capitalize',
                                }}
                            >
                      {transaction.plaidCategory.replace(/_/g, ' ').toLowerCase()}
                    </span>
                        ) : (
                            <span style={{ color: '#9e9e9e' }}>Uncategorized</span>
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