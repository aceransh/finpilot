import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';
import { AuthProvider } from './features/auth/AuthProvider'; // <-- add

const root = ReactDOM.createRoot(document.getElementById('root') as HTMLElement);

root.render(
    <React.StrictMode>
        <AuthProvider> {/* <-- provide auth context to the whole app */}
            <App />
        </AuthProvider>
    </React.StrictMode>
);