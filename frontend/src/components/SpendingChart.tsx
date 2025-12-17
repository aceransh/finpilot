import { PieChart, Pie, Cell, ResponsiveContainer, Legend, Tooltip } from 'recharts';
import { Paper, Typography } from '@mui/material';
import type { Transaction } from '../api/transactionService';

interface SpendingChartProps {
  transactions: Transaction[];
}

interface CategoryData {
  name: string;
  value: number;
  color: string;
}

const SpendingChart = ({ transactions }: SpendingChartProps) => {
  // Safety check
  if (!transactions || !Array.isArray(transactions)) {
    return null;
  }

  // Aggregate transactions by category
  const categoryMap = new Map<string, { total: number; color: string }>();

  transactions.forEach((transaction) => {
    if (transaction.amount < 0) {
      // Only count expenses
      const categoryName = transaction.category?.name || 'Uncategorized';
      const categoryColor = transaction.category?.colorHex || '#9e9e9e';

      const current = categoryMap.get(categoryName) || {
        total: 0,
        color: categoryColor,
      };
      categoryMap.set(categoryName, {
        total: current.total + Math.abs(transaction.amount),
        color: categoryColor,
      });
    }
  });

  // Convert to array
  const data: CategoryData[] = Array.from(categoryMap.entries()).map(
      ([name, { total, color }]) => ({
        name,
        value: total,
        color,
      })
  );

  if (data.length === 0) {
    return (
        <Paper sx={{ p: 3, height: 400, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <Typography variant="body1" color="text.secondary">
            No spending data available
          </Typography>
        </Paper>
    );
  }

  return (
      <Paper sx={{ p: 3 }}>
        <Typography variant="h6" gutterBottom>
          Spending by Category
        </Typography>
        <ResponsiveContainer width="100%" height={350}>
          <PieChart>
            <Pie
                // TS FIX: Cast data to 'any' to satisfy Recharts strict typing
                data={data as any}
                cx="50%"
                cy="50%"
                labelLine={false}
                // TS FIX: Use 'any' for render props
                label={({ name, percent }: any) =>
                    `${name} ${((percent || 0) * 100).toFixed(0)}%`
                }
                outerRadius={100}
                fill="#8884d8"
                dataKey="value"
            >
              {data.map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={entry.color} />
              ))}
            </Pie>
            <Tooltip formatter={(value: any) => `$${Number(value).toFixed(2)}`} />
            <Legend />
          </PieChart>
        </ResponsiveContainer>
      </Paper>
  );
};

export default SpendingChart;