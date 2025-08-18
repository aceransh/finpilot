import React from 'react';
import { BrowserRouter, Routes, Route, Link } from 'react-router-dom';
import { Container, CssBaseline, Typography, Stack, Button } from '@mui/material';

import TransactionList from './features/transactions/TransactionList';
import CategoriesPage from './features/categories/CategoriesPage';
import RulesPage from './features/rules/RulesPage';
import Login from "./features/auth/Login";

function App() {
    return (
        <BrowserRouter>
            <CssBaseline />
            <Container sx={{ pb: 6 }}>
                <Typography variant="h2" component="h1" align="center" gutterBottom sx={{ mt: 4 }}>
                    Transaction Tracker
                </Typography>

                {/* simple nav */}
                <Stack direction="row" spacing={2} sx={{ mb: 3 }} justifyContent="center">
                    <Button component={Link} to="/" variant="outlined">Transactions</Button>
                    <Button component={Link} to="/categories" variant="outlined">Categories</Button>
                    <Button component={Link} to="/rules" variant="outlined">Rules</Button>
                    <Button component={Link} to="/login" variant="outlined">Login</Button> {/* NEW */}
                </Stack>

                {/* routes */}
                <Routes>
                    <Route path="/" element={<TransactionList />} />
                    <Route path="/categories" element={<CategoriesPage />} />
                    <Route path="/rules" element={<RulesPage />} />
                    <Route path="/login" element={<Login />} /> {/* NEW */}
                </Routes>
            </Container>
        </BrowserRouter>
    );
}

export default App;