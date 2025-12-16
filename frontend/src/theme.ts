import { createTheme } from '@mui/material/styles';

export const theme = createTheme({
  palette: {
    mode: 'dark',
    primary: {
      main: '#2979ff', // FinPilot Blue
    },
    background: {
      default: '#121212', // Deep Gunmetal for app background
      paper: '#1e1e1e', // Slightly lighter for cards/sidebar
    },
  },
});

