import React, { useState, useEffect } from "react";
import {
    Dialog,
    DialogTitle,
    DialogContent,
    DialogActions,
    TextField,
    Button,
    Grid,
    MenuItem,
} from "@mui/material";
import { Transaction } from "../api";

interface Props {
    open: boolean;
    onClose: () => void;
    onSubmit: (transaction: Omit<Transaction, "id">) => Promise<void>;
    transaction?: Transaction | null;
}

const categories = [
    "Food",
    "Transportation",
    "Shopping",
    "Entertainment",
    "Bills",
    "Income",
    "Other"
];

const emptyForm = {
    date: "",
    merchant: "",
    amount: "",
    category: "",
};

function TransactionForm({ open, onClose, onSubmit, transaction }: Props) {
    const [formData, setFormData] = useState(emptyForm);
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        if (transaction) {
            setFormData({
                date: transaction.date,
                merchant: transaction.merchant,
                amount: String(transaction.amount),
                category: transaction.category,
            });
        } else {
            setFormData(emptyForm);
        }
    }, [transaction, open]);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setFormData((prev) => ({ ...prev, [name]: value }));
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);

        // simple front-end validation
        if (!formData.date || !formData.merchant || !formData.category) {
            setError("All fields are required.");
            return;
        }
        const parsedAmount = parseFloat(formData.amount);
        if (Number.isNaN(parsedAmount)) {
            setError("Amount must be a number.");
            return;
        }

        try {
            setSubmitting(true);
            await onSubmit({ ...formData, amount: parsedAmount });
            // success → close & reset
            onClose();
        } catch (err) {
            setError("Failed to submit. Please try again.");
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
            <form onSubmit={handleSubmit}>
                <DialogTitle>{transaction ? "Edit Transaction" : "Add Transaction"}</DialogTitle>
                <DialogContent>

                    {error && (
                        <div style={{ marginBottom: 8, color: '#d32f2f' }}>
                            {error}
                        </div>
                    )}

                    <Grid container spacing={2} sx={{ mt: 1 }}>
                        <Grid size={{ xs: 12 }}>
                            <TextField
                                name="date"
                                label="Date"
                                type="date"
                                value={formData.date}
                                onChange={handleChange}
                                fullWidth
                                required
                                disabled={submitting}
                                slotProps={{ inputLabel: { shrink: true } }}
                            />
                        </Grid>

                        <Grid size={{ xs: 12 }}>
                            <TextField
                                name="merchant"
                                label="Merchant"
                                value={formData.merchant}
                                onChange={handleChange}
                                fullWidth
                                required
                                disabled={submitting}
                            />
                        </Grid>

                        <Grid size={{ xs: 12 }}>
                            <TextField
                                name="amount"
                                label="Amount"
                                type="number"
                                value={formData.amount}
                                onChange={handleChange}
                                fullWidth
                                required
                                disabled={submitting}
                                slotProps={{ htmlInput: { step: "0.01" } }}
                                helperText="Use negative values for expenses, positive for income"
                            />
                        </Grid>

                        <Grid size={{ xs: 12 }}>
                            <TextField
                                name="category"
                                label="Category"
                                select
                                value={formData.category}
                                onChange={handleChange}
                                fullWidth
                                required
                                disabled={submitting}
                            >
                                {categories.map((cat) => (
                                    <MenuItem key={cat} value={cat}>
                                        {cat}
                                    </MenuItem>
                                ))}
                            </TextField>
                        </Grid>
                    </Grid>
                </DialogContent>

                <DialogActions>
                    <Button onClick={onClose} disabled={submitting}>Cancel</Button>
                    <Button type="submit" variant="contained" disabled={submitting}>
                        {submitting ? "Saving..." : transaction ? "Update" : "Add"}
                    </Button>
                </DialogActions>
            </form>
        </Dialog>
    )
}

export default TransactionForm;