// src/components/RulesPage.tsx
import { useEffect, useState } from 'react';
import {
    Container, Typography, Paper, Stack, Button,
    Table, TableHead, TableRow, TableCell, TableBody, Chip, Switch,
    TextField, MenuItem, Dialog, DialogTitle, DialogContent, DialogActions,
    Alert, Tooltip, IconButton,
} from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import {getRules, getCategories, createRule, testRule, updateRule} from '../api/api';
import type { Rule, Category, } from '../api/api';
import EditIcon from "@mui/icons-material/Edit";

export default function RulesPage() {
    const [rules, setRules] = useState<Rule[]>([]);
    const [loading, setLoading] = useState(true);

    const [form, setForm] = useState({
        pattern: '',
        matchType: 'CONTAINS' as 'CONTAINS'|'REGEX',
        categoryId: '',
        priority: 100,
        enabled: true,
    });

    const [open, setOpen] = useState(false);
    const [cats, setCats] = useState<Category[]>([]);
    const [saving, setSaving] = useState(false);

    const [testInput, setTestInput] = useState('');
    const [testLoading, setTestLoading] = useState(false);
    const [testResult, setTestResult] = useState<{
        matched: boolean;
        ruleId?: string;
        categoryId?: string;
        categoryName?: string;
    } | null>(null);
    const [testError, setTestError] = useState<string | null>(null);

    const [confirmOpen, setConfirmOpen] = useState(false);
    const [ruleToDelete, setRuleToDelete] = useState<Rule | null>(null);
    const [deleting, setDeleting] = useState(false);

    const [editingRule, setEditingRule] = useState<Rule | null>(null);

    const handleCreate = async () => {
        // basic validation
        if (!form.pattern.trim() || !form.categoryId) return;

        try {
            setSaving(true);
            await createRule({
                pattern: form.pattern.trim(),
                matchType: form.matchType,
                categoryId: form.categoryId,
                priority: form.priority,
                enabled: form.enabled,
            });

            // refresh list
            const data = await getRules();
            setRules(data);

            // close + reset
            setOpen(false);
            setForm({ pattern: '', matchType: 'CONTAINS', categoryId: '', priority: 100, enabled: true });
        } catch (e) {
            console.error('Failed to create rule:', e);
        } finally {
            setSaving(false);
        }
    };

    const handleSaveEdit = async () => {
        if (!editingRule) return;
        if (!form.pattern.trim() || !form.categoryId) return;

        try {
            setSaving(true);
            await updateRule(editingRule.id, {
                pattern: form.pattern.trim(),
                matchType: form.matchType,
                categoryId: form.categoryId,
                priority: form.priority,
                enabled: form.enabled,
            });

            // refresh list
            const data = await getRules();
            setRules(data);

            closeDialog();
        } catch (e) {
            console.error('Failed to update rule:', e);
        } finally {
            setSaving(false);
        }
    };

    const handleTestRule = async () => {
        setTestError(null);
        setTestResult(null);
        if (!testInput.trim()) {
            setTestError('Enter a merchant to test.');
            return;
        }
        try {
            setTestLoading(true);
            const res = await testRule(testInput.trim());
            setTestResult(res);
        } catch (e) {
            console.error(e);
            setTestError('Failed to test rule. Check backend logs.');
        } finally {
            setTestLoading(false);
        }
    };

    const handleToggleEnabled = async (rule: Rule, next: boolean) => {
        // optimistic UI update
        setRules(prev =>
            prev.map(r => (r.id === rule.id ? { ...r, enabled: next } : r))
        );

        try {
            await updateRule(rule.id, {
                pattern: rule.pattern,
                matchType: rule.matchType,
                categoryId: rule.categoryId,
                priority: rule.priority,
                enabled: next,
            });
        } catch (e) {
            console.error('Failed to toggle rule', e);
            // rollback on error
            setRules(prev =>
                prev.map(r => (r.id === rule.id ? { ...r, enabled: !next } : r))
            );
        }
    };


    const openDeleteDialog = (rule: Rule) => {
        setRuleToDelete(rule);
        setConfirmOpen(true);
    };

    const closeDeleteDialog = () => {
        if (deleting) return; // avoid closing while in-flight
        setConfirmOpen(false);
        setRuleToDelete(null);
    };

    const confirmDelete = async () => {
        if (!ruleToDelete) return;
        setDeleting(true);

        // optimistic remove
        const prev = rules;
        setRules(prev.filter(r => r.id !== ruleToDelete.id));

        try {
            await import('../api/api').then(m => m.deleteRule(ruleToDelete.id));
            setDeleting(false);
            closeDeleteDialog();
        } catch (e) {
            console.error('Failed to delete rule', e);
            setRules(prev); // rollback
            setDeleting(false);
        }
    };

    const openEdit = (rule: Rule) => {
        setEditingRule(rule);
        setForm({
            pattern: rule.pattern,
            matchType: rule.matchType as 'CONTAINS' | 'REGEX',
            categoryId: rule.categoryId,
            priority: rule.priority,
            enabled: rule.enabled,
        });
        setOpen(true);
    };

    const closeDialog = () => {
        if (saving) return;        // don’t close while saving
        setOpen(false);
        setEditingRule(null);
        setForm({ pattern: '', matchType: 'CONTAINS', categoryId: '', priority: 100, enabled: true });
    };

    useEffect(() => {
        let ignore = false;
        (async () => {
            try {
                const data = await getRules();
                if (!ignore) setRules(data);
            } finally {
                if (!ignore) setLoading(false);
            }
        })();
        return () => { ignore = true; };
    }, []);

// Load categories once
    useEffect(() => {
        let ignore = false;
        (async () => {
            try {
                const data = await getCategories();
                if (!ignore) setCats(data);
            } catch (e) {
                console.error('Failed to load categories:', e);
            }
        })();
        return () => { ignore = true; };
    }, []);

    return (
        <Container sx={{ mt: 4 }}>
            <Stack direction="row" justifyContent="space-between" alignItems="center" mb={2}>
                <Typography variant="h4">Rules</Typography>
                <Button variant="contained" onClick={() => setOpen(true)}>New Rule</Button>
            </Stack>

            <Paper>
                <Stack direction="row" spacing={2} alignItems="center" sx={{ mb: 2 }}>
                    <TextField
                        label="Test merchant"
                        placeholder='e.g. "Harris Teeter #238"'
                        value={testInput}
                        onChange={(e) => setTestInput(e.target.value)}
                        size="small"
                        sx={{ minWidth: 320 }}
                    />
                    <Button
                        variant="outlined"
                        onClick={handleTestRule}
                        disabled={testLoading}
                    >
                        {testLoading ? 'Testing…' : 'Test rule'}
                    </Button>
                    {testError && <Alert severity="error" sx={{ ml: 2 }}>{testError}</Alert>}
                    {testResult && (
                        testResult.matched ? (
                            <Stack direction="row" spacing={1} alignItems="center">
                                <Chip label={`Matched`} color="success" />
                                <Chip label={`Category: ${testResult.categoryName}`} />
                            </Stack>
                        ) : (
                            <Chip label="No match" color="warning" />
                        )
                    )}
                </Stack>
                <Table>
                    <TableHead>
                        <TableRow>
                            <TableCell width={80}>Priority</TableCell>
                            <TableCell>Pattern</TableCell>
                            <TableCell width={120}>Match</TableCell>
                            <TableCell width={200}>Category</TableCell>
                            <TableCell width={120}>Enabled</TableCell>
                            <TableCell width={80} align="right">Actions</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {loading ? (
                            <TableRow>
                                <TableCell colSpan={5}>Loading…</TableCell>
                            </TableRow>
                        ) : rules.length === 0 ? (
                            <TableRow>
                                <TableCell colSpan={5}>No rules yet.</TableCell>
                            </TableRow>
                        ) : (
                            rules.map(r => (
                                <TableRow key={r.id}>
                                    <TableCell>{r.priority}</TableCell>
                                    <TableCell sx={{ fontFamily: 'monospace' }}>{r.pattern}</TableCell>
                                    <TableCell>{r.matchType}</TableCell>
                                    <TableCell>
                                        <Chip
                                            label={r.categoryName}
                                            variant="outlined"
                                            size="small"
                                        />
                                    </TableCell>
                                    <TableCell>
                                        <Switch
                                            checked={r.enabled}
                                            onChange={(e) => handleToggleEnabled(r, e.target.checked)}
                                            size="small"
                                        />
                                    </TableCell>
                                    <TableCell align="right"> {/* NEW */}
                                        <Tooltip title="Edit">
                                          <span>
                                            <IconButton
                                                size="small"
                                                onClick={() => openEdit(r)}
                                            >
                                              {/* add import: import EditIcon from '@mui/icons-material/Edit'; */}
                                                <EditIcon fontSize="small" />
                                            </IconButton>
                                          </span>
                                        </Tooltip>
                                        <Tooltip title="Delete">
                                          <span>
                                            <IconButton
                                                size="small"
                                                color="error"
                                                onClick={() => openDeleteDialog(r)}
                                            >
                                              <DeleteIcon fontSize="small" />
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
            <Dialog open={open} onClose={closeDialog} fullWidth maxWidth="sm">
                <DialogTitle>{editingRule ? 'Edit Rule' : 'New Rule'}</DialogTitle>
                <DialogContent sx={{ pt: 2 }}>
                    <Stack spacing={2} sx={{ mt: 1 }}>
                        <TextField
                            label="Pattern"
                            value={form.pattern}
                            onChange={e => setForm(f => ({ ...f, pattern: e.target.value }))}
                            required
                            helperText={form.matchType === 'CONTAINS'
                                ? 'Example: harris teeter'
                                : 'Regex example: ^starbucks(\\s|#|$)'}
                        />

                        <TextField
                            label="Match Type"
                            select
                            value={form.matchType}
                            onChange={e => setForm(f => ({ ...f, matchType: e.target.value as 'CONTAINS'|'REGEX' }))}
                        >
                            <MenuItem value="CONTAINS">CONTAINS</MenuItem>
                            <MenuItem value="REGEX">REGEX</MenuItem>
                        </TextField>

                        <TextField
                            label="Category"
                            select
                            value={form.categoryId}
                            onChange={e => setForm(f => ({ ...f, categoryId: e.target.value }))}
                            required
                        >
                            {cats.map(c => (
                                <MenuItem key={c.id} value={c.id}>{c.name}</MenuItem>
                            ))}
                        </TextField>

                        <TextField
                            label="Priority"
                            type="number"
                            value={form.priority}
                            onChange={e => setForm(f => ({ ...f, priority: Number(e.target.value || 0) }))}
                            slotProps={{htmlInput: {min: 1, max: 9999}}}
                            helperText={"Lower numbers run first. Default is 100."}
                        />

                        <Stack direction="row" alignItems="center" spacing={1}>
                            <Switch
                                checked={form.enabled}
                                onChange={e => setForm(f => ({ ...f, enabled: e.target.checked }))}
                            />
                            <Typography variant="body2">Enabled</Typography>
                        </Stack>
                    </Stack>
                </DialogContent>
                <DialogActions>
                    <Button onClick={closeDialog} disabled={saving}>
                        Cancel
                    </Button>
                    <Button
                        variant="contained"
                        onClick={editingRule ? handleSaveEdit : handleCreate}
                        disabled={saving || !form.pattern.trim() || !form.categoryId}
                    >
                        {saving
                            ? 'Saving…'
                            : editingRule
                                ? 'Save'
                                : 'Create'}
                    </Button>
                </DialogActions>
            </Dialog>
            <Dialog open={confirmOpen} onClose={closeDeleteDialog}>
                <DialogTitle>Delete rule</DialogTitle>
                <DialogContent>
                    <Typography>
                        Are you sure you want to delete the rule{' '}
                        <strong>{ruleToDelete?.pattern}</strong>?
                    </Typography>
                </DialogContent>
                <DialogActions>
                    <Button onClick={closeDeleteDialog} disabled={deleting}>Cancel</Button>
                    <Button
                        onClick={confirmDelete}
                        color="error"
                        variant="contained"
                        disabled={deleting}
                    >
                        {deleting ? 'Deleting…' : 'Delete'}
                    </Button>
                </DialogActions>
            </Dialog>
        </Container>
    );
}