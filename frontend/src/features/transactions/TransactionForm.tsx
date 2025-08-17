import axios from 'axios';
import {getCategories, Category, createTransactionForce, updateTransaction} from '../api/api';
import { useEffect, useState } from 'react';
import {
    Dialog,
    DialogTitle,
    DialogContent,
    DialogActions,
    TextField,
    Button,
    Grid,
    MenuItem,
    Alert,
} from "@mui/material";
import { Transaction } from "../api/api";

interface Props {
    open: boolean;
    onClose: () => void;
    onSubmit: (transaction: Omit<Transaction, "id">) => Promise<void>;
    transaction?: Transaction | null;
}

const emptyForm = {
    date: "",
    merchant: "",
    amount: "",
    category: "",
    categoryId: "",
};

function TransactionForm({ open, onClose, onSubmit, transaction }: Props) {
    const [formData, setFormData] = useState(emptyForm);
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [catOptions, setCatOptions] = useState<Category[]>([]);

    const [dupInfo, setDupInfo] = useState<null | {
        existing?: {
            id: number;
            date: string;
            amount: number;
            merchant: string;
            category?: string;
            categoryName?: string;
        };
        candidate?: {
            date?: string;
            amount?: number;
            merchant?: string;
        };
        detail?: string;
    }>(null);

    useEffect(() => {
        if (!open) return;
        let alive = true;
        (async () => {
            try {
                const cats = await getCategories();
                if (alive) setCatOptions(cats);
            } catch (e) {
                console.error('Failed to load categories', e);
            }
        })();
        return () => { alive = false; };
    }, [open]);

    useEffect(() => {
        if (transaction) {
            setFormData({
                date: transaction.date,
                merchant: transaction.merchant,
                amount: String(transaction.amount),
                category: transaction.category ?? "",
                // transaction doesn't (yet) have categoryId in its TS type, so fall back to ""
                categoryId: (transaction as any).categoryId ?? "",
            });
        } else {
            setFormData(emptyForm);
        }
    }, [transaction, open]);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setDupInfo(null);                 // <-- clear any prior 409 panel
        setFormData(prev => ({ ...prev, [name]: value }));
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
            setDupInfo(null); // clear previous duplicate panel

            // build a normalized payload
            const payload: Omit<Transaction, "id"> = {
                date: formData.date,
                amount: parsedAmount,
                merchant: formData.merchant.trim(),        // <-- trim!
                category: formData.category,
                // only include categoryId if it's a non-empty string
                ...(formData.categoryId ? { categoryId: formData.categoryId } : {}),
            } as any; // 'as any' here keeps TS happy since Transaction doesn't yet include categoryId in its type

            await onSubmit(payload);
            onClose();
        } catch (err: unknown) {
            if (axios.isAxiosError(err)) {
                if (err.response?.status === 409) {
                    const data = err.response.data as any;
                    setDupInfo({
                        existing: data.existing,
                        candidate: data.candidate,
                        detail: data.detail || "Duplicate transaction",
                    });
                    setError(null);
                    return;
                }
                setError(err.response?.data?.message || "Failed to submit. Please try again.");
            } else {
                setError("Failed to submit. Please try again.");
            }
        } finally {
            setSubmitting(false);
        }
    };

    const handleForceAdd = async () => {
        setError(null);

        // basic validation again (same as submit)
        if (!formData.date || !formData.merchant) {
            setError("Date and merchant are required.");
            return;
        }
        const parsedAmount = parseFloat(formData.amount);
        if (Number.isNaN(parsedAmount)) {
            setError("Amount must be a number.");
            return;
        }

        try {
            setSubmitting(true);
            // Build the payload exactly like normal submit
            const payload = {
                date: formData.date,
                amount: parsedAmount,
                merchant: formData.merchant.trim(),
                category: formData.category,
                ...(formData.categoryId ? { categoryId: formData.categoryId } : {}) // <-- key change
            };

            // Force-create directly (temporary: next micro-step will plumb this via parent)
            await createTransactionForce(payload);

            // Close the dialog; parent refresh wiring comes next
            onClose();
        } catch (err) {
            setError("Failed to add anyway. Please try again.");
        } finally {
            setSubmitting(false);
        }
    };

    const handleForceSave = async () => {
        if (!transaction) return; // only valid in edit mode
        try {
            setSubmitting(true);
            // Reuse current form state but send force=true
            await updateTransaction(transaction.id, {
                date: formData.date,
                amount: Number(formData.amount),
                merchant: formData.merchant,
                category: formData.category,
                categoryId: formData.categoryId || undefined,
            }, { force: true });

            setDupInfo(null);
            onClose(); // close dialog; parent will refresh the list in its onClose/onSubmit flow
        } catch (e) {
            setError("Failed to save anyway. Please try again.");
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
            <form onSubmit={handleSubmit}>
                <DialogTitle>{transaction ? "Edit Transaction" : "Add Transaction"}</DialogTitle>
                <DialogContent>

                    {dupInfo && (
                        <div style={{
                            marginBottom: 12,
                            padding: 12,
                            borderRadius: 8,
                            border: '1px solid rgba(0,0,0,0.12)',
                            background: 'rgba(255, 243, 224, 0.6)' // subtle warning tone
                        }}>
                            <div style={{ fontWeight: 600, marginBottom: 6 }}>
                                {dupInfo.detail || "Duplicate transaction"}
                            </div>
                            {dupInfo.existing && (
                                <div style={{ marginBottom: 6 }}>
                                    <div style={{ fontSize: 12, opacity: 0.75 }}>Existing:</div>
                                    <div>
                                        #{dupInfo.existing.id} — {dupInfo.existing.merchant} · {dupInfo.existing.date} · ${dupInfo.existing.amount.toFixed(2)}{dupInfo.existing.categoryName ? ` · ${dupInfo.existing.categoryName}` : ""}
                                    </div>
                                </div>
                            )}
                            {dupInfo.candidate && (
                                <div>
                                    <div style={{ fontSize: 12, opacity: 0.75 }}>Your new one:</div>
                                    <div>
                                        {dupInfo.candidate.merchant} · {dupInfo.candidate.date} · ${Number(dupInfo.candidate.amount).toFixed(2)}
                                    </div>
                                </div>
                            )}
                            <div style={{ display: 'flex', gap: 8, marginTop: 10 }}>
                                {transaction ? (
                                    <Button
                                        variant="contained"
                                        size="small"
                                        onClick={handleForceSave}
                                        disabled={submitting}
                                    >
                                        Save anyway
                                    </Button>
                                ) : (
                                    <Button
                                        variant="contained"
                                        size="small"
                                        onClick={handleForceAdd}
                                        disabled={submitting}
                                    >
                                        Add anyway
                                    </Button>
                                )}
                                <Button
                                    size="small"
                                    onClick={() => setDupInfo(null)}
                                    disabled={submitting}
                                >
                                    Cancel
                                </Button>
                            </div>
                            {error && (
                                <Alert severity="error" sx={{ mb: 2 }}>
                                    {error}
                                </Alert>
                            )}
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
                                name="categoryId"
                                label="Category"
                                select
                                value={formData.categoryId}
                                onChange={(e) => {
                                    const id = e.target.value;
                                    const selected = catOptions.find(c => c.id === id);
                                    setFormData(prev => ({
                                        ...prev,
                                        categoryId: id,
                                        category: selected ? selected.name : prev.category
                                    }));
                                }}
                                fullWidth
                                required
                                disabled={submitting}
                                helperText="Pick from your saved categories"
                            >
                                {catOptions.length === 0 ? (
                                    <MenuItem disabled value="">
                                        {open ? 'Loading…' : 'No categories yet'}
                                    </MenuItem>
                                ) : (
                                    catOptions.map((cat) => (
                                        <MenuItem key={cat.id} value={cat.id}>
                                            {cat.name}
                                        </MenuItem>
                                    ))
                                )}
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