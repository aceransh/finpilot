import React from "react";
import { GoogleAuthProvider, signInWithPopup } from "firebase/auth";
import { auth } from "../../firebase";
import { Button, Stack, Typography } from "@mui/material";

export default function Login() {
    const handleGoogleLogin = async () => {
        try {
            const provider = new GoogleAuthProvider();
            const result = await signInWithPopup(auth, provider);

            // Grab ID token from Firebase user
            const token = await result.user.getIdToken();

            console.log("Logged in:", result.user.email);
            console.log("ID token:", token);

            // Later: send token in Authorization header to backend
            // localStorage.setItem("token", token);
        } catch (err) {
            console.error("Login failed", err);
        }
    };

    return (
        <Stack spacing={2} alignItems="center" sx={{ mt: 6 }}>
            <Typography variant="h5">Login to Finpilot</Typography>
            <Button variant="contained" onClick={handleGoogleLogin}>
                Sign in with Google
            </Button>
        </Stack>
    );
}