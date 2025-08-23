// frontend/src/App.tsx
import React from 'react';
import { BrowserRouter, Routes, Route, Link, Navigate, useLocation } from 'react-router-dom';
import {
    Container, CssBaseline, Typography, Stack, Button, CircularProgress, Box
} from '@mui/material';

import TransactionList from './features/transactions/TransactionList';
import CategoriesPage from './features/categories/CategoriesPage';
import RulesPage from './features/rules/RulesPage';
import Login from './features/auth/Login';
import { useAuth } from './features/auth/AuthProvider';
import LinkButton from './features/plaid/LinkButton';

// --- add this tiny guard inside the same file ---
function RequireAuth({ children }: { children: React.ReactElement }) {
    const { user, loading } = useAuth();
    const location = useLocation();

    if (loading) {
        return (
            <Box sx={{ p: 6, display: 'flex', justifyContent: 'center' }}>
                <CircularProgress />
            </Box>
        );
    }
    if (!user) {
        return <Navigate to="/login" replace state={{ from: location }} />;
    }
    return children;
}
// -----------------------------------------------

function App() {
    const { user, loading, signOut } = useAuth();

    React.useEffect(() => {
        const fetchToken = async () => {
            if (user) {
                const token = await user.getIdToken(true);
                console.log("Firebase ID token:", token);
            }
        };
        fetchToken();
    }, [user]);

    return (
        <BrowserRouter>
            <CssBaseline />
            <Container sx={{ pb: 6 }}>
                {/* Header changes slightly: show spinner, user name, or nothing depending on state */}
                <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mt: 3, mb: 2 }}>
                    <Typography variant="h4" component="h1">Transaction Tracker</Typography>
                    <Stack direction="row" spacing={1} alignItems="center">
                        {loading ? (
                            <CircularProgress size={18} />
                        ) : user ? (
                            <>
                                <Typography variant="body2" sx={{ mr: 1 }}>
                                    {user.displayName || user.email}
                                </Typography>
                                <Button size="small" variant="outlined" onClick={signOut}>Sign out</Button>
                            </>
                        ) : null}
                    </Stack>
                </Stack>

                {/* simple nav */}
                <Stack direction="row" spacing={2} sx={{ mb: 3 }} justifyContent="center">
                    <Button component={Link} to="/" variant="outlined">Transactions</Button>
                    <Button component={Link} to="/categories" variant="outlined">Categories</Button>
                    <Button component={Link} to="/rules" variant="outlined">Rules</Button>
                </Stack>

                <Stack direction="row" justifyContent="center" sx={{ mb: 3 }}>
                    <LinkButton />
                </Stack>

                {/* routes */}
                <Routes>
                    {/* Public route */}
                    <Route path="/login" element={<Login />} />

                    {/* Protected routes */}
                    <Route
                        path="/"
                        element={
                            <RequireAuth>
                                <TransactionList />
                            </RequireAuth>
                        }
                    />
                    <Route
                        path="/categories"
                        element={
                            <RequireAuth>
                                <CategoriesPage />
                            </RequireAuth>
                        }
                    />
                    <Route
                        path="/rules"
                        element={
                            <RequireAuth>
                                <RulesPage />
                            </RequireAuth>
                        }
                    />

                    {/* Unknown → home (protected) */}
                    <Route path="*" element={<Navigate to="/" replace />} />
                </Routes>
            </Container>
        </BrowserRouter>
    );
}

export default App;