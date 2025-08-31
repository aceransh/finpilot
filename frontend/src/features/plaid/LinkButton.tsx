import React from 'react';
import { Button, CircularProgress } from '@mui/material';
import { usePlaidLink } from 'react-plaid-link';
import { createPlaidLinkToken } from '../api/api'; // adjust path if your api.ts lives elsewhere

const LinkButton: React.FC = () => {
    const [token, setToken] = React.useState<string | null>(null);
    const [loading, setLoading] = React.useState(false);
    const initialized = React.useRef(false);

    React.useEffect(() => {
        if (initialized.current) return;   // prevents double init in dev
        initialized.current = true;

        // your Link setup that calls usePlaidLink/open()
    }, []);

    // 1) Get a fresh link_token from your backend
    const fetchToken = async () => {
        setLoading(true);
        try {
            const t = await createPlaidLinkToken();
            setToken(t);
        } finally {
            setLoading(false);
        }
    };

    // 2) Wire Plaid Link to the token once we have it
    const { open, ready } = usePlaidLink({
        token: token ?? '',

        // Fires when user successfully links an institution
        onSuccess: (public_token, metadata) => {
            console.log('Plaid Link success:', { public_token, metadata });
            // NEXT STEP: POST public_token to /api/v1/plaid/item/public_token/exchange
            // and store the returned access_token (encrypted) + item info.
        },

        // (optional) see why users exited/cancelled
        onExit: (err, metadata) => {
            console.log('Plaid Link exit', { err, metadata });
        },
    });

    // 3) Button behavior
    const handleClick = async () => {
        if (!token) {
            await fetchToken(); // fetch first, then open
            // open() will be ready on next render
            // small delay to ensure hook is ready
            setTimeout(() => open(), 0);
        } else {
            open();
        }
    };

    const disabled = loading || (token ? !ready : false);

    return (
        <Button variant="contained" onClick={handleClick} disabled={disabled}>
            {loading ? <CircularProgress size={18} /> : 'Link a bank account'}
        </Button>
    );
};

export default LinkButton;