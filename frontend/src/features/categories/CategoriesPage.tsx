import { useEffect, useState } from 'react';
import {
    Container, Paper, Stack, Typography, Button,
    Table, TableHead, TableRow, TableCell, TableBody,
    Dialog, DialogTitle, DialogContent, DialogActions,
    TextField, MenuItem, Tooltip, IconButton,
} from '@mui/material';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import { Category, CategoryRequest, getCategories, createCategory, deleteCategory, updateCategory } from '../api/api';

export default function CategoriesPage() {
    const [items, setItems] = useState<Category[]>([]);
    const [loading, setLoading] = useState(true);
    const [open, setOpen] = useState(false);
    const [editing, setEditing] = useState<Category | null>(null);

    // form state
    const [name, setName] = useState('');
    const [type, setType] = useState<'EXPENSE' | 'INCOME'>('EXPENSE');
    const [color, setColor] = useState('#2E7D32');

    const [confirmOpen, setConfirmOpen] = useState(false);
    const [toDelete, setToDelete] = useState<Category | null>(null);
    const [actionError, setActionError] = useState<string | null>(null);

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

    // helpers
    const openNew = () => {
        setEditing(null);
        setName('');
        setType('EXPENSE');
        setColor('#2E7D32');
        setOpen(true);
    };

    const openEdit = (c: Category) => {
        setEditing(c);
        setName(c.name);
        setType(c.type);
        setColor(c.color ?? '#2E7D32');
        setOpen(true);
    };

    const askDelete = (c: Category) => {
        setToDelete(c);
        setActionError(null);
        setConfirmOpen(true);
    };

    const closeConfirm = () => {
        setConfirmOpen(false);
        setToDelete(null);
    };

    const handleDelete = async () => {
        if (!toDelete) return;
        try {
            await deleteCategory(toDelete.id);
            closeConfirm();
            await load();                 // refresh list
        } catch (err: any) {
            // Axios style: err.response?.status
            if (err?.response?.status === 409) {
                setActionError('This category is used by existing transactions. Remove or reassign them first.');
            } else {
                setActionError('Failed to delete category. Please try again.');
            }
        }
    };

    const handleSave = async () => {
        if (!editing) return;                                // safety: must be editing
        const body: CategoryRequest = { name, type, color }; // payload for PUT

        await updateCategory(editing.id, body);              // PUT /categories/{id}
        setOpen(false);                                      // close dialog
        setEditing(null);                                    // clear editing state
        setName('');                                         // reset form state
        setType('EXPENSE');
        setColor('#2E7D32');
        await load();                                        // refresh table
    };

    return (
        <Container sx={{ mt: 4 }}>
            <Stack direction="row" justifyContent="space-between" alignItems="center" mb={2}>
                <Typography variant="h4">Categories</Typography>
                <Button variant="contained" onClick={openNew}>New Category</Button>
            </Stack>

            <Paper>
                <Table>
                    <TableHead>
                        <TableRow>
                            <TableCell>Name</TableCell>
                            <TableCell>Type</TableCell>
                            <TableCell>Color</TableCell>
                            <TableCell align="right">Actions</TableCell> {/* NEW */}
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {loading ? (
                            <TableRow><TableCell colSpan={4}>Loading...</TableCell></TableRow>
                            ) : items.length === 0 ? (
                            <TableRow><TableCell colSpan={4}>No categories yet.</TableCell></TableRow>
                        ) : (
                            items.map(c => (
                                <TableRow key={c.id}>
                                    <TableCell>{c.name}</TableCell>
                                    <TableCell>{c.type}</TableCell>
                                    <TableCell>
                                        <span style={{
                                            display: 'inline-block',
                                            width: 16,
                                            height: 16,
                                            borderRadius: 4,
                                            marginRight: 8,
                                            background: c.color || '#999'
                                        }} />
                                        {c.color || '—'}
                                    </TableCell>
                                    <TableCell align = "right">
                                        <Tooltip title="Edit">
                                            <span>
                                              <IconButton size="small" onClick={() => openEdit(c)}>
                                                <EditIcon />
                                              </IconButton>
                                            </span>
                                        </Tooltip>
                                        <Tooltip title="Delete">
                                              <span>
                                                <IconButton size="small" color="error" onClick={() => askDelete(c)}>
                                                  <DeleteIcon />
                                                </IconButton>
                                              </span>
                                        </Tooltip>
                                    </TableCell>
                                </TableRow>
                            ))
                        )}
                    </TableBody>
                </Table>
            </Paper>

            <Dialog open={open} onClose={() => setOpen(false)} fullWidth maxWidth="sm">
                <DialogTitle>{editing ? 'Edit Category' : 'New Category'}</DialogTitle>
                <DialogContent>
                    <Stack spacing={2} sx={{ mt: 1 }}>
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
                    {editing ? (
                        <Button
                            variant="contained"
                            onClick={handleSave}
                            disabled={!name.trim()}          // simple validation
                        >
                            Save
                        </Button>
                    ) : (
                        <Button
                            variant="contained"
                            onClick={handleCreate}
                            disabled={!name.trim()}
                        >
                            Create
                        </Button>
                    )}
                </DialogActions>
            </Dialog>
            <Dialog open={confirmOpen} onClose={closeConfirm}>
                <DialogTitle>Delete category?</DialogTitle>
                <DialogContent>
                    <Typography variant="body2" sx={{ mt: 1 }}>
                        {toDelete
                            ? `“${toDelete.name}” will be removed. This can’t be undone.`
                            : 'This category will be removed.'}
                    </Typography>

                    {actionError && (
                        <Typography variant="body2" color="error" sx={{ mt: 1 }}>
                            {actionError}
                        </Typography>
                    )}
                </DialogContent>
                <DialogActions>
                    <Button onClick={closeConfirm}>Cancel</Button>
                    <Button color="error" variant="contained" onClick={handleDelete}>
                        Delete
                    </Button>
                </DialogActions>
            </Dialog>
        </Container>
    );
}