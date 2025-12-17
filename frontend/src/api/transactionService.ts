import axiosInstance from './axiosConfig';

export interface Transaction {
  id: string;
  plaidTransactionId: string;
  amount: number;
  date: string;
  description: string;
  category: {
    id: number;
    name: string;
    colorHex: string;
  } | null;
  account: {
    id: string;
    name: string;
  };
}

export const getTransactions = async (): Promise<Transaction[]> => {
  const response = await axiosInstance.get('/transactions');
  return response.data;
};

