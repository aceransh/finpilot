import React, { useState, useEffect, useCallback} from "react";
import {
    getTransactions,
    deleteTransaction,
    createTransaction,
    updateTransaction,
    getCategories,
    Category,
    Transaction,
    listPlaidItems,
    PlaidItemSummary
} from '../api/api';
import {
    Container,
    Paper,
    Table,
    TableHead,
    TableRow,
    TableCell,
    TableBody,
    IconButton,
    // CircularProgress,
    Alert,
    Stack,
    Typography,
    Box,
    Button,
    TablePagination,
    TableSortLabel,
    TextField,
    MenuItem,
    Dialog,
    DialogTitle,
    DialogContent,
    DialogActions,
} from "@mui/material";
import DeleteIcon from "@mui/icons-material/Delete";
import EditIcon from "@mui/icons-material/Edit";
import TransactionForm from "./TransactionForm";
import { syncPlaidItem } from '../api/api';
import { getPlaidAccounts, type PlaidAccount } from '../api/api';

function TransactionList() {
    const [transactions, setTransactions] = useState<Transaction[]>([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null);
    const [isFormOpen, setIsFormOpen] = useState(false);
    const [editingTransaction, setEditingTransaction] = useState<Transaction | null>(null);
    const [page, setPage] = useState(0);           // 0-based page index
    const [rowsPerPage, setRowsPerPage] = useState(10);
    const [sortField, setSortField] = useState<'date'|'merchant'|'amount'|'category'>('date');
    const [sortDir, setSortDir] = useState<'asc'|'desc'>('desc');
    const [total, setTotal] = useState(0);         // totalElements from backend
    // filters
    const [q, setQ] = useState('');
    const [category, setCategory] = useState('');
    const [from, setFrom] = useState(''); // "YYYY-MM-DD"
    const [to, setTo] = useState('');     // "YYYY-MM-DD"
    const [qInput, setQInput] = useState(q);

    const [catOptions, setCatOptions] = useState<Category[]>([]);

    const [deleteOpen, setDeleteOpen] = useState(false);
    const [deleteId, setDeleteId] = useState<number | null>(null);

    //plaid
    const [plaidItems, setPlaidItems] = useState<PlaidItemSummary[]>([]);
    const [selectedItemId, setSelectedItemId] = useState<string>('');
    const [accountId, setAccountId] = useState<string>('');
    const [accountOptions, setAccountOptions] = useState<PlaidAccount[]>([]);

    useEffect(() => {
        let ignore = false;
        (async () => {
            try {
                const cats = await getCategories();
                if (!ignore) setCatOptions(cats);
            } catch (e) {
                console.error("Failed to load categories:", e);
            }
        })();
        return () => { ignore = true; };
    }, []);

    useEffect(() => {
        const t = setTimeout(() => {
            if (q !== qInput) {
                setQ(qInput);
                setPage(0);
            }
        }, 400); // debounce delay

        return () => clearTimeout(t);
    }, [qInput, q]);

    useEffect(() => {
        let ignore = false;
        (async () => {
            try {
                const items = await listPlaidItems();
                if (!ignore) {
                    setPlaidItems(items);
                    // preselect the newest item if any
                    if (items.length && !selectedItemId) {
                        setSelectedItemId(items[0].id);
                    }
                }
            } catch (e) {
                console.error('Failed to load Plaid items:', e);
            }
        })();
        return () => { ignore = true; };
    }, []);

    useEffect(() => {
        let ignore = false;
        (async () => {
            try {
                const accts = await getPlaidAccounts();
                if (!ignore) setAccountOptions(Array.isArray(accts) ? accts : []);
            } catch (e) {
                console.error('Failed to load Plaid accounts:', e);
            }
        })();
        return () => { ignore = true; };
    }, []);


    const loadTransactions = useCallback(async () => {
        try {
            setLoading(true);
            setError(null);

            const sort = `${sortField},${sortDir}`;
            const data = await getTransactions({ page, size: rowsPerPage, sort, q, category, from, to, accountId });

            setTransactions(Array.isArray(data.content) ? data.content : []);
            setTotal(data.totalElements ?? 0);
        } catch (error) {
            console.error("Failed to load transactions:", error);
            setError("Failed to load transactions. Please try again.");
        } finally {
            setLoading(false);
        }
    }, [page, rowsPerPage, sortField, sortDir, q, category, from, to, accountId]);

    useEffect(() => {
        loadTransactions().catch(console.error);
    }, [loadTransactions]);

    const handleOpenForm = (transaction: Transaction | null = null) => {
        setEditingTransaction(transaction);
        setIsFormOpen(true);

    }

    const handleCloseForm = () => {
        setIsFormOpen(false);
        setEditingTransaction(null);
    };

    const handleFormSubmit = async (formData: Omit<Transaction, "id">) => {
        // Let errors propagate to the dialog (TransactionForm) to handle UX.
        if (editingTransaction) {
            await updateTransaction(editingTransaction.id, formData);
        } else {
            await createTransaction(formData);
        }
        handleCloseForm();
        await loadTransactions();
    };

    const handleChangePage = (_: unknown, newPage: number) => {
        setPage(newPage);
    };

    const handleChangeRowsPerPage = (e: React.ChangeEvent<HTMLInputElement>) => {
        setRowsPerPage(parseInt(e.target.value, 10));
        setPage(0); // reset to first page when page size changes
    };

    const handleRequestSort = (field: 'date'|'merchant'|'amount'|'category') => {
        if (sortField === field) {
            setSortDir(prev => (prev === 'asc' ? 'desc' : 'asc'));
        } else {
            setSortField(field);
            setSortDir('desc'); // default to desc on new field
        }
        setPage(0); // reset to first page on sort change
    };

    const handleManualSync = async () => {
        if (!selectedItemId) {
            alert('Pick a bank first.');
            return;
        }
        try {
            const res = await syncPlaidItem(selectedItemId);
            alert(`Sync complete:\ncreated=${res.created}\nupdated=${res.updated}\nremoved=${res.removed}`);
            await loadTransactions();
        } catch (e: any) {
            alert(`Sync failed: ${e?.response?.data?.error || e.message}`);
        }
    };

    const confirmDelete = async () => {
        if (deleteId == null) return;
        try {
            await deleteTransaction(deleteId);
            setTransactions(prev => prev.filter(tx => tx.id !== deleteId)); // optimistic remove
        } catch (err) {
            console.error("Failed to delete transaction:", err);
            setError("Failed to delete transaction. Please try again.");
        } finally {
            setDeleteOpen(false);
            setDeleteId(null);
        }
    };

    const cancelDelete = () => {
        setDeleteOpen(false);
        setDeleteId(null);
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
                <Stack direction="row" spacing={1} alignItems="center">
                    <TextField
                        select
                        label="Bank"
                        size="small"
                        sx={{ minWidth: 260 }}
                        value={selectedItemId}
                        onChange={(e) => setSelectedItemId(e.target.value)}
                    >
                        {plaidItems.map((it) => {
                            const label = it.institutionName
                                ? `${it.institutionName} · ${it.plaidItemId.slice(-6)}`
                                : `Unknown · ${it.plaidItemId.slice(-6)}`;
                            return (
                                <MenuItem key={it.id} value={it.id}>
                                    {label}
                                </MenuItem>
                            );
                        })}
                    </TextField>

                    <Button
                        variant="outlined"
                        onClick={handleManualSync}
                        disabled={!selectedItemId}
                    >
                        Sync
                    </Button>

                    <Button variant="contained" onClick={() => handleOpenForm()}>
                        Add Transaction
                    </Button>
                </Stack>
            </Stack>
            <Paper>
                <Stack direction="row" spacing={2} alignItems="center" sx={{ p: 2 }}>
                    <TextField
                        label="Search"
                        placeholder="Merchant or category"
                        value={qInput}
                        onChange={(e) => setQInput(e.target.value)}
                        size="small"
                    />

                    <TextField
                        label="Category"
                        select
                        value={category}
                        onChange={(e) => { setCategory(e.target.value); setPage(0); }}
                        size="small"
                        sx={{ minWidth: 160 }}
                    >
                        <MenuItem value="">All</MenuItem>
                        {catOptions.map(c => (
                            <MenuItem key={c.id} value={c.name}>
                                {c.name}
                            </MenuItem>
                        ))}
                    </TextField>
                    <TextField
                        label="Account"
                        select
                        value={accountId ?? ''} // always a string
                        onChange={(e) => {
                            const v = (e.target.value as string) ?? '';
                            setAccountId(v);
                            setPage(0);
                            // quick debug:
                            // console.log('Selected accountId=', v);
                        }}
                        size="small"
                        sx={{ minWidth: 260 }}
                    >
                        <MenuItem value="">All</MenuItem>
                        {accountOptions.map(a => {
                            const id = a.accountId ?? ''; // <-- IMPORTANT
                            const inst = a.institutionName || 'Unknown';
                            const name = a.name || a.officialName || 'Account';
                            const mask = a.mask ? ` ••••${a.mask}` : '';
                            const subtype = a.subtype ? ` (${a.subtype})` : '';
                            return (
                                <MenuItem key={id || `${inst}-${name}-${mask}`} value={id}>
                                    {inst} — {name}{mask}{subtype}
                                </MenuItem>
                            );
                        })}
                    </TextField>
                    <TextField
                        label="From"
                        type="date"
                        value={from}
                        onChange={(e) => { setFrom(e.target.value); setPage(0); }}
                        size="small"
                        slotProps={{ inputLabel: { shrink: true } }}
                    />
                    <TextField
                        label="To"
                        type="date"
                        value={to}
                        onChange={(e) => { setTo(e.target.value); setPage(0); }}
                        size="small"
                        slotProps={{ inputLabel: { shrink: true } }}
                    />

                    <Button
                        onClick={() => { setQ(''); setCategory(''); setFrom(''); setTo(''); setAccountId(''); setPage(0); }}
                    >
                        Clear
                    </Button>
                </Stack>
                {loading && (
                    <Box sx={{ px: 2, pt: 2 }}>
                        <Typography variant="body2">Loading…</Typography>
                    </Box>
                )}
                <TablePagination
                    component="div"
                    count={total}
                    page={page}
                    onPageChange={handleChangePage}
                    rowsPerPage={rowsPerPage}
                    onRowsPerPageChange={handleChangeRowsPerPage}
                    rowsPerPageOptions={[5, 10, 25, 50]}
                />
                {transactions.length === 0 ? (
                    <Box sx={{ p: 3 }}>
                        <Typography variant="body1">No transactions found.</Typography>
                    </Box>
                ) : (
                    <Table>
                        <TableHead>
                            <TableRow>
                                <TableCell sortDirection={sortField === 'date' ? sortDir : false}>
                                    <TableSortLabel
                                        active={sortField === 'date'}
                                        direction={sortField === 'date' ? sortDir : 'desc'}
                                        onClick={() => handleRequestSort('date')}
                                    >
                                        Date
                                    </TableSortLabel>
                                </TableCell>

                                <TableCell sortDirection={sortField === 'merchant' ? sortDir : false}>
                                    <TableSortLabel
                                        active={sortField === 'merchant'}
                                        direction={sortField === 'merchant' ? sortDir : 'desc'}
                                        onClick={() => handleRequestSort('merchant')}
                                    >
                                        Merchant
                                    </TableSortLabel>
                                </TableCell>

                                <TableCell sortDirection={sortField === 'amount' ? sortDir : false}>
                                    <TableSortLabel
                                        active={sortField === 'amount'}
                                        direction={sortField === 'amount' ? sortDir : 'desc'}
                                        onClick={() => handleRequestSort('amount')}
                                    >
                                        Amount
                                    </TableSortLabel>
                                </TableCell>

                                <TableCell sortDirection={sortField === 'category' ? sortDir : false}>
                                    <TableSortLabel
                                        active={sortField === 'category'}
                                        direction={sortField === 'category' ? sortDir : 'desc'}
                                        onClick={() => handleRequestSort('category')}
                                    >
                                        Category
                                    </TableSortLabel>
                                </TableCell>

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
                                        <IconButton
                                            size="small"
                                            onClick={() => { setDeleteId(tx.id); setDeleteOpen(true); }}
                                            color="error"
                                        >
                                            <DeleteIcon />
                                        </IconButton>
                                    </TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                )}
            </Paper>
            <TransactionForm
                open={isFormOpen}
                onClose={handleCloseForm}
                onSubmit={handleFormSubmit}
                transaction={editingTransaction}
            />
            <Dialog open={deleteOpen} onClose={cancelDelete} maxWidth="xs" fullWidth>
                <DialogTitle>Delete transaction?</DialogTitle>
                <DialogContent>
                    <Typography variant="body2">
                        This action can’t be undone.
                    </Typography>
                </DialogContent>
                <DialogActions>
                    <Button onClick={cancelDelete}>Cancel</Button>
                    <Button variant="contained" color="error" onClick={confirmDelete}>
                        Delete
                    </Button>
                </DialogActions>
            </Dialog>
        </Container>
    )


}

export default TransactionList;