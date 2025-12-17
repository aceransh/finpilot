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

// Simple color palette for the auto-generated categories
const COLORS = ['#0088FE', '#00C49F', '#FFBB28', '#FF8042', '#8884d8', '#82ca9d', '#ffc658'];

const SpendingChart = ({ transactions }: SpendingChartProps) => {
    // Safety check
    if (!transactions || !Array.isArray(transactions)) {
        return null;
    }

    // Aggregate transactions by category
    const categoryMap = new Map<string, number>();

    transactions.forEach((transaction) => {
        // PLAID LOGIC: Positive amounts are typically expenses.
        // We filter > 0 to capture your Uber/McDonalds/Starbucks charges.
        if (transaction.amount > 0) {

            // 1. Try Custom Category -> 2. Try Plaid Category -> 3. Default
            let categoryName = transaction.category?.name
                || transaction.plaidCategory
                || 'Uncategorized';

            // Clean up string: "FOOD_AND_DRINK" -> "FOOD AND DRINK"
            categoryName = categoryName.replace(/_/g, ' ');

            const currentTotal = categoryMap.get(categoryName) || 0;
            categoryMap.set(categoryName, currentTotal + transaction.amount);
        }
    });

    // Convert to array
    const data: CategoryData[] = Array.from(categoryMap.entries()).map(
        ([name, value], index) => ({
            name,
            value,
            // Cycle through our color palette
            color: COLORS[index % COLORS.length],
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
                        label={false} // <--- CHANGED: Removes the messy text labels
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