import { useEffect, useState } from 'react';
import {
    Container, Paper, Stack, Typography, Button,
    Table, TableHead, TableRow, TableCell, TableBody,
    Dialog, DialogTitle, DialogContent, DialogActions,
    TextField, MenuItem
} from '@mui/material';
import { Category, CategoryRequest, getCategories, createCategory } from '../api/api';

export default function CategoriesPage() {
    const [items, setItems] = useState<Category[]>([]);
    const [loading, setLoading] = useState(true);
    const [open, setOpen] = useState(false);

    // form state
    const [name, setName] = useState('');
    const [type, setType] = useState<'EXPENSE' | 'INCOME'>('EXPENSE');
    const [color, setColor] = useState('#2E7D32');

    const load = async () => {
        setLoading(true);
        try {
            const data = await getCategories();
            setItems(data);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        load().catch(console.error);
    }, []);

    const handleCreate = async () => {
        const body: CategoryRequest = { name, type, color };
        await createCategory(body);
        setOpen(false);
        setName('');
        setType('EXPENSE');
        setColor('#2E7D32');
        await load();
    };

    return (
        <Container sx={{ mt: 4 }}>
            <Stack direction="row" justifyContent="space-between" alignItems="center" mb={2}>
                <Typography variant="h4">Categories</Typography>
                <Button variant="contained" onClick={() => setOpen(true)}>New Category</Button>
            </Stack>

            <Paper>
                <Table>
                    <TableHead>
                        <TableRow>
                            <TableCell>Name</TableCell>
                            <TableCell>Type</TableCell>
                            <TableCell>Color</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {loading ? (
                            <TableRow><TableCell colSpan={3}>Loading…</TableCell></TableRow>
                        ) : items.length === 0 ? (
                            <TableRow><TableCell colSpan={3}>No categories yet.</TableCell></TableRow>
                        ) : (
                            items.map(c => (
                                <TableRow key={c.id}>
                                    <TableCell>{c.name}</TableCell>
                                    <TableCell>{c.type}</TableCell>
                                    <TableCell>
                    <span style={{
                        display: 'inline-block',
                        width: 16, height: 16,
                        borderRadius: 4,
                        marginRight: 8,
                        background: c.color || '#999'
                    }} />
                                        {c.color || '—'}
                                    </TableCell>
                                </TableRow>
                            ))
                        )}
                    </TableBody>
                </Table>
            </Paper>

            <Dialog open={open} onClose={() => setOpen(false)} fullWidth maxWidth="sm">
                <DialogTitle>New Category</DialogTitle>
                <DialogContent sx={{ pt: 2 }}>
                    <Stack spacing={2}>
                        <TextField
                            label="Name"
                            value={name}
                            onChange={e => setName(e.target.value)}
                            required
                        />
                        <TextField
                            label="Type"
                            select
                            value={type}
                            onChange={e => setType(e.target.value as 'EXPENSE'|'INCOME')}
                            required
                        >
                            <MenuItem value="EXPENSE">EXPENSE</MenuItem>
                            <MenuItem value="INCOME">INCOME</MenuItem>
                        </TextField>
                        <TextField
                            label="Color (hex)"
                            value={color}
                            onChange={e => setColor(e.target.value)}
                            placeholder="#2E7D32"
                        />
                    </Stack>
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setOpen(false)}>Cancel</Button>
                    <Button variant="contained" onClick={handleCreate} disabled={!name.trim()}>
                        Create
                    </Button>
                </DialogActions>
            </Dialog>
        </Container>
    );
}