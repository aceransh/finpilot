import { useEffect, useState } from 'react';
import { usePlaidLink } from 'react-plaid-link';
import { Button } from '@mui/material';
import axiosInstance from '../api/axiosConfig';

const LinkButton = () => {
  const [linkToken, setLinkToken] = useState<string | null>(null);

  // Fetch link token on mount
  useEffect(() => {
    const fetchLinkToken = async () => {
      try {
        const response = await axiosInstance.post('/plaid/link-token');
        setLinkToken(response.data.link_token);
      } catch (error) {
        console.error('Error fetching link token:', error);
      }
    };
    fetchLinkToken();
  }, []);

  const onSuccess = async (publicToken: string) => {
    try {
      // Send public_token to backend
      await axiosInstance.post('/plaid/public-token', {
        public_token: publicToken,
      });
      console.log('Bank connected successfully!');
      // TODO: Show success message or refresh data
    } catch (error) {
      console.error('Error exchanging public token:', error);
      // TODO: Show error message
    }
  };

  const config = {
    token: linkToken,
    onSuccess,
    onExit: (err: any, metadata: any) => {
      if (err) {
        console.error('Plaid Link error:', err);
      }
    },
  };

  const { open, ready } = usePlaidLink(config);

  return (
    <Button
      variant="contained"
      onClick={() => open()}
      disabled={!ready || !linkToken}
      sx={{ mt: 2 }}
    >
      Connect a Bank
    </Button>
  );
};

export default LinkButton;

