import React from 'react';
import TransactionList from './components/TransactionList';
import { Container, CssBaseline, Typography } from '@mui/material';

function App() {
    return (
        <>
            <CssBaseline />
            <Container>
                <Typography variant="h2" component="h1" align="center" gutterBottom sx={{ mt: 4 }}>
                    Transaction Tracker
                </Typography>
                <TransactionList />
            </Container>
        </>
    );
}

export default App;