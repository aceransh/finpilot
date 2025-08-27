// frontend/src/features/auth/AuthProvider.tsx
import React, { createContext, useContext, useEffect, useMemo, useState } from 'react';
import { auth } from '../../firebase';                    // uses your existing firebase.ts
import {
    GoogleAuthProvider,
    onAuthStateChanged,
    signInWithPopup,
    signOut as fbSignOut,
    type User,
} from 'firebase/auth';

// 1) Shape of the auth context (what consumers can use)
type AuthCtx = {
    user: User | null;
    loading: boolean;
    signInWithGoogle: () => Promise<void>;
    signOut: () => Promise<void>;
    getIdToken: (force?: boolean) => Promise<string | undefined>;
};

// 2) Create the context (we’ll provide real values in the Provider)
const Ctx = createContext<AuthCtx | undefined>(undefined);

// 3) Provider component that wires Firebase auth state → React state
export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const [user, setUser] = useState<User | null>(null);
    const [loading, setLoading] = useState(true);

    // Subscribe once to auth changes
    useEffect(() => {
        const unsub = onAuthStateChanged(auth, (u) => {
            setUser(u ?? null);
            setLoading(false);
        });
        return () => unsub();
    }, []);

    // Helper: sign in with Google (popup)
    const signInWithGoogle = async () => {
        const provider = new GoogleAuthProvider();
        try {
            await signInWithPopup(auth, provider);
            // user state updates via onAuthStateChanged
        } catch (err: any) {
            // Known popup/COOP-ish issues — fallback to redirect
            const code = err?.code as string | undefined;
            const msg = (err?.message as string | '')?.toLowerCase() ?? '';

            const looksLikePopupBlocked =
                code === 'auth/popup-blocked' ||
                code === 'auth/popup-closed-by-user' ||
                code === 'auth/cancelled-popup-request' ||
                msg.includes('cross-origin-opener-policy') ||
                msg.includes('window.closed');

            if (looksLikePopupBlocked) {
                // Redirect flow is more tolerant in locked-down browsers
                const { signInWithRedirect } = await import('firebase/auth');
                await signInWithRedirect(auth, provider);
                return;
            }

            // If it’s some other error, rethrow for your UI to handle/toast
            throw err;
        }
    };

    // Helper: sign out
    const signOut = async () => {
        await fbSignOut(auth);
    };

    // Helper: get current ID token (used by axios interceptor if you want)
    const getIdToken = async (force = false) => {
        return auth.currentUser?.getIdToken(force);
    };

    // Avoid re-creating object on every render
    const value = useMemo<AuthCtx>(() => ({
        user,
        loading,
        signInWithGoogle,
        signOut,
        getIdToken,
    }), [user, loading]);

    return <Ctx.Provider value={value}>{children}</Ctx.Provider>;
};

// 4) Hook for consumers
export const useAuth = () => {
    const ctx = useContext(Ctx);
    if (!ctx) throw new Error('useAuth must be used within <AuthProvider>');
    return ctx;
};