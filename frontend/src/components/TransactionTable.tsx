import { useState, useMemo } from 'react';
import {
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  TextField,
  Select,
  MenuItem,
  FormControl,
  Chip,
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Box,
  InputAdornment,
  Tooltip,
} from '@mui/material';
// FIXED: Aliased 'Search' to 'SearchIcon' to match the code usage
import { Check, Close, Search as SearchIcon, Edit } from '@mui/icons-material';
import { format } from 'date-fns';
import type { Transaction, Category } from '../api/transactionService';

interface TransactionTableProps {
  transactions: Transaction[];
  categories: Category[];
  onUpdate: (id: string, data: { categoryId?: string; description?: string }) => Promise<void>;
  onCreateCategory: (data: { name: string; colorHex?: string }) => Promise<Category>;
}

type SortKey = 'date' | 'amount';
type SortDirection = 'asc' | 'desc';

interface SortConfig {
  key: SortKey | null;
  direction: SortDirection;
}

const TransactionTable = ({
                            transactions,
                            categories,
                            onUpdate,
                            onCreateCategory,
                          }: TransactionTableProps) => {
  const [searchTerm, setSearchTerm] = useState('');
  const [sortConfig, setSortConfig] = useState<SortConfig>({ key: null, direction: 'asc' });
  const [editingId, setEditingId] = useState<string | null>(null);
  const [editDescription, setEditDescription] = useState('');
  const [editCategoryId, setEditCategoryId] = useState<string | null>(null);
  const [createCategoryDialogOpen, setCreateCategoryDialogOpen] = useState(false);
  const [newCategoryName, setNewCategoryName] = useState('');
  const [newCategoryColor, setNewCategoryColor] = useState('#2979ff');

  // Filter and sort transactions
  const filteredAndSortedTransactions = useMemo(() => {
    let filtered = transactions.filter((txn) => {
      const searchLower = searchTerm.toLowerCase();
      return (
          (txn.description || '').toLowerCase().includes(searchLower) ||
          txn.amount.toString().includes(searchLower)
      );
    });

    if (sortConfig.key) {
      filtered = [...filtered].sort((a, b) => {
        let aValue: number | string;
        let bValue: number | string;

        if (sortConfig.key === 'date') {
          aValue = new Date(a.date).getTime();
          bValue = new Date(b.date).getTime();
        } else {
          aValue = a.amount;
          bValue = b.amount;
        }

        if (aValue < bValue) {
          return sortConfig.direction === 'asc' ? -1 : 1;
        }
        if (aValue > bValue) {
          return sortConfig.direction === 'asc' ? 1 : -1;
        }
        return 0;
      });
    }

    return filtered;
  }, [transactions, searchTerm, sortConfig]);

  const handleSort = (key: SortKey) => {
    setSortConfig((prev) => ({
      key,
      direction: prev.key === key && prev.direction === 'asc' ? 'desc' : 'asc',
    }));
  };

  const handleEditClick = (transaction: Transaction) => {
    setEditingId(transaction.id);
    setEditDescription(transaction.description || '');
    setEditCategoryId(transaction.category ? String(transaction.category.id) : null);
  };

  const handleCancel = () => {
    setEditingId(null);
    setEditDescription('');
    setEditCategoryId(null);
  };

  const handleSave = async (id: string) => {
    await onUpdate(id, {
      description: editDescription,
      categoryId: editCategoryId || undefined,
    });
    handleCancel();
  };

  const handleCategoryChange = async (value: string) => {
    if (value === 'new') {
      setCreateCategoryDialogOpen(true);
    } else {
      setEditCategoryId(value);
    }
  };

  const handleCreateCategory = async () => {
    try {
      const newCategory = await onCreateCategory({
        name: newCategoryName,
        colorHex: newCategoryColor,
      });
      setEditCategoryId(String(newCategory.id));
      setCreateCategoryDialogOpen(false);
      setNewCategoryName('');
      setNewCategoryColor('#2979ff');
    } catch (error) {
      console.error('Error creating category:', error);
      alert('Failed to create category');
    }
  };

  return (
      <>
        <Box sx={{ mb: 2 }}>
          <TextField
              fullWidth
              placeholder="Search by description or amount..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              // FIXED: Use slotProps instead of deprecated InputProps
              slotProps={{
                input: {
                  startAdornment: (
                      <InputAdornment position="start">
                        <SearchIcon />
                      </InputAdornment>
                  ),
                },
              }}
          />
        </Box>

        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell sx={{ cursor: 'pointer' }} onClick={() => handleSort('date')}>
                  Date {sortConfig.key === 'date' && (sortConfig.direction === 'asc' ? '↑' : '↓')}
                </TableCell>
                <TableCell>Description</TableCell>
                <TableCell sx={{ cursor: 'pointer' }} onClick={() => handleSort('amount')} align="right">
                  Amount {sortConfig.key === 'amount' && (sortConfig.direction === 'asc' ? '↑' : '↓')}
                </TableCell>
                <TableCell>Category</TableCell>
                <TableCell align="right">Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {filteredAndSortedTransactions.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={5} align="center">
                      No transactions found
                    </TableCell>
                  </TableRow>
              ) : (
                  filteredAndSortedTransactions.map((transaction) => {
                    const isEditing = editingId === transaction.id;

                    return (
                        <TableRow key={transaction.id} hover>
                          <TableCell>
                            {format(new Date(transaction.date), 'MM/dd/yyyy')}
                          </TableCell>

                          <TableCell>
                            {isEditing ? (
                                <TextField
                                    fullWidth
                                    size="small"
                                    value={editDescription}
                                    onChange={(e) => setEditDescription(e.target.value)}
                                />
                            ) : (
                                transaction.description || 'N/A'
                            )}
                          </TableCell>

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
                            {isEditing ? (
                                <FormControl fullWidth size="small">
                                  <Select
                                      value={editCategoryId || ''}
                                      onChange={(e) => handleCategoryChange(e.target.value as string)}
                                  >
                                    <MenuItem value="">
                                      <em>None</em>
                                    </MenuItem>
                                    {categories.map((cat) => (
                                        <MenuItem key={cat.id} value={String(cat.id)}>
                                          {cat.name}
                                        </MenuItem>
                                    ))}
                                    <MenuItem value="new" sx={{ color: 'primary.main', fontWeight: 'bold' }}>
                                      + Create New Category
                                    </MenuItem>
                                  </Select>
                                </FormControl>
                            ) : transaction.category ? (
                                <Chip
                                    label={transaction.category.name}
                                    sx={{
                                      backgroundColor: transaction.category.colorHex,
                                      color: 'white',
                                      fontWeight: 'bold',
                                    }}
                                />
                            ) : transaction.plaidCategory ? (
                                <Chip
                                    label={transaction.plaidCategory.replace(/_/g, ' ')}
                                    variant="outlined"
                                    color="primary"
                                    size="small"
                                />
                            ) : (
                                <span style={{ color: '#9e9e9e', fontSize: '0.875rem' }}>Uncategorized</span>
                            )}
                          </TableCell>

                          <TableCell align="right">
                            {isEditing ? (
                                <Box sx={{ display: 'flex', gap: 1, justifyContent: 'flex-end' }}>
                                  <IconButton size="small" color="primary" onClick={() => handleSave(transaction.id)}>
                                    <Check />
                                  </IconButton>
                                  <IconButton size="small" color="error" onClick={handleCancel}>
                                    <Close />
                                  </IconButton>
                                </Box>
                            ) : (
                                <Tooltip title="Edit Transaction">
                                  <IconButton size="small" onClick={() => handleEditClick(transaction)}>
                                    <Edit fontSize="small" />
                                  </IconButton>
                                </Tooltip>
                            )}
                          </TableCell>
                        </TableRow>
                    );
                  })
              )}
            </TableBody>
          </Table>
        </TableContainer>

        {/* Create Category Dialog */}
        <Dialog open={createCategoryDialogOpen} onClose={() => setCreateCategoryDialogOpen(false)}>
          <DialogTitle>Create New Category</DialogTitle>
          <DialogContent>
            <TextField
                autoFocus
                margin="dense"
                label="Category Name"
                fullWidth
                variant="standard"
                value={newCategoryName}
                onChange={(e) => setNewCategoryName(e.target.value)}
                sx={{ mb: 2 }}
            />
            <TextField
                margin="dense"
                label="Color (Hex)"
                fullWidth
                variant="standard"
                value={newCategoryColor}
                onChange={(e) => setNewCategoryColor(e.target.value)}
                placeholder="#2979ff"
            />
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setCreateCategoryDialogOpen(false)}>Cancel</Button>
            <Button onClick={handleCreateCategory} variant="contained" disabled={!newCategoryName.trim()}>
              Create
            </Button>
          </DialogActions>
        </Dialog>
      </>
  );
};

export default TransactionTable;