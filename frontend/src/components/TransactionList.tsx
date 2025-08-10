import { useState, useEffect } from "react";
import { Transaction, getTransactions, deleteTransaction, createTransaction, updateTransaction} from "../api";
import {
    Container,
    Paper,
    Table,
    TableHead,
    TableRow,
    TableCell,
    TableBody,
    IconButton,
    CircularProgress,
    Alert,
    Stack,
    Typography,
    Box,
    Button,
} from "@mui/material";
import DeleteIcon from "@mui/icons-material/Delete";
import EditIcon from "@mui/icons-material/Edit";
import TransactionForm from "./TransactionForm";

function TransactionList() {
    const [transactions, setTransactions] = useState<Transaction[]>([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null);
    const [isFormOpen, setIsFormOpen] = useState(false);
    const [editingTransaction, setEditingTransaction] = useState<Transaction | null>(null);

    useEffect(() => {
        loadTransactions().catch((error) => {
            console.error("Unexpected error loading transactions:", error);
        })
    }, [])

    const loadTransactions = async () => {
        try {
            setLoading(true);
            setError(null);
            const data = await getTransactions();
            const sorted = [...data].sort(
                (a, b) => new Date(b.date).getTime() - new Date(a.date).getTime()
            );
            setTransactions(sorted);

        } catch (error) {
            console.error("Failed to load transactions:", error);
            setError("Failed to load transactions. Please try again.");

        } finally {
            setLoading(false);
        }

    };

    const handleDelete = async (id: number) => {
        if (window.confirm("Are you sure you want to delete this transaction?")){
            try {
                await deleteTransaction(id);
                setTransactions((prev)=>
                    prev.filter((tx) => tx.id !== id))
            } catch (error) {
                console.error("Failed to delete transaction:", error);
                setError("Failed to delete transaction. Please try again.");
            }
        }
    };

    const handleOpenForm = (transaction: Transaction | null = null) => {
        setEditingTransaction(transaction);
        setIsFormOpen(true);

    }

    const handleCloseForm = () => {
        setIsFormOpen(false);
        setEditingTransaction(null);
    };

    const handleFormSubmit = async (formData: Omit<Transaction, "id">) => {
        try {
            if (editingTransaction) {
                await updateTransaction(editingTransaction.id, formData);
            }
            else {
                await createTransaction(formData);
            }
            handleCloseForm();
            await loadTransactions();
        } catch (error) {
            console.error("Failed to save transaction:", error);
            setError("Failed to save transaction. Please try again.");
        }
    };


    // Format currency for display
    const formatCurrency = (amount: number) => {
        return new Intl.NumberFormat("en-US", {
            style: "currency",
            currency: "USD"
        }).format(amount);
    };

    // Format date for display
    const formatDate = (s: string) => {
        // Interpret YYYY-MM-DD as local date by constructing with year, month-1, day
        const [y, m, d] = s.split('-').map(Number);
        const dt = new Date(y, m - 1, d); // local time
        return dt.toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });
    };

    if (loading) {
        return (
            <Box display="flex" justifyContent="center" alignItems="center" minHeight="80vh">
                <CircularProgress />
            </Box>
        );
    }

    if (error) {
        return (
            <Container sx={{ mt: 4 }}>
                <Alert severity="error" action={
                    <Button color="inherit" size="small" onClick={loadTransactions}>
                        Retry
                    </Button>
                }>
                    {error}
                </Alert>
            </Container>
        );
    }

    return (
        <Container sx={{ mt: 4 }}>
            <Stack direction="row" justifyContent="space-between" alignItems="center" mb={2}>
                <Typography variant='h4' component='h1'>
                    Recent Transactions
                </Typography>
                <Button variant="contained" onClick={() => handleOpenForm()}>Add Transaction</Button>
            </Stack>
            <Paper>
                <Table>
                    <TableHead>
                        <TableRow>
                            <TableCell>Date</TableCell>
                            <TableCell>Merchant</TableCell>
                            <TableCell>Amount</TableCell>
                            <TableCell>Category</TableCell>
                            <TableCell align="right">Actions</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {transactions.map((tx) => (
                            <TableRow key={tx.id}>
                                <TableCell>{formatDate(tx.date)}</TableCell>
                                <TableCell>{tx.merchant}</TableCell>
                                <TableCell sx={{ color: tx.amount < 0 ? 'error.main' : 'success.main' }}>
                                    {formatCurrency(tx.amount)}
                                </TableCell>
                                <TableCell>{tx.category}</TableCell>
                                <TableCell align="right">
                                    <IconButton size="small" onClick={() => handleOpenForm(tx)}>
                                        <EditIcon />
                                    </IconButton>
                                    <IconButton size="small" onClick={() => handleDelete(tx.id)} color="error">
                                        <DeleteIcon />
                                    </IconButton>
                                </TableCell>
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
            </Paper>
            <TransactionForm
                open={isFormOpen}
                onClose={handleCloseForm}
                onSubmit={handleFormSubmit}
                transaction={editingTransaction}
            />
        </Container>
    )


}

export default TransactionList;