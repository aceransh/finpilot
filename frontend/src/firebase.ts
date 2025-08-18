// frontend/src/firebase.ts
import { initializeApp } from "firebase/app";
import { getAuth } from "firebase/auth";

// ⚠️ These values can stay client-side for Firebase Auth,
// but later we’ll move them to env vars before shipping.
const firebaseConfig = {
    apiKey: "AIzaSyDF0tFpzMQUQyVJ9DJ3MQPbdgJUliKg-V4",
    authDomain: "finpilot-15.firebaseapp.com",
    projectId: "finpilot-15",
    storageBucket: "finpilot-15.firebasestorage.app",
    messagingSenderId: "298969540519",
    appId: "1:298969540519:web:c5956e427d6033064c7882",
    // measurementId is optional and not needed now
};

// 1) Initialize the SDK once for the whole app
export const app = initializeApp(firebaseConfig);

// 2) Auth instance we’ll use in Login, guards, headers, etc.
export const auth = getAuth(app);

// DEV helper: expose a function to get the current Firebase ID token
if ((import.meta as any)?.env?.MODE === 'development') {
    (window as any).getIdToken = async (force = true) => {
        const { getAuth } = await import('firebase/auth');
        return getAuth().currentUser?.getIdToken(force);
    };
}