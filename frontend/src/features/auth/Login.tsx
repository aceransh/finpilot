// frontend/src/features/auth/Login.tsx
import React, { useEffect } from 'react';
import { Button, Container, Stack, Typography } from '@mui/material';
import { GoogleAuthProvider, signInWithPopup } from 'firebase/auth';
import { auth } from '../../firebase';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from './AuthProvider';

export default function Login() {
    const navigate = useNavigate();
    const location = useLocation() as { state?: { from?: Location } };
    const { user } = useAuth();

    // where to go after login: the page the guard sent us from, or "/"
    const goTo =
        (location.state?.from as any)?.pathname || '/';

    // already signed in? bounce immediately
    useEffect(() => {
        if (user) {
            navigate(goTo, { replace: true });
        }
    }, [user, goTo, navigate]);

    const handleGoogleLogin = async () => {
        await signInWithPopup(auth, new GoogleAuthProvider());
        // after popup completes, go where we intended
        navigate(goTo, { replace: true });
    };

    return (
        <Container sx={{ mt: 6 }}>
            <Typography variant="h5" align="center" gutterBottom>
                Login to Finpilot
            </Typography>
            <Stack alignItems="center" sx={{ mt: 2 }}>
                <Button variant="contained" onClick={handleGoogleLogin}>
                    Sign in with Google
                </Button>
            </Stack>
        </Container>
    );
}