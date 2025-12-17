import axiosInstance from './axiosConfig';

export interface Transaction {
  id: string;
  plaidTransactionId: string;
  amount: number;
  date: string;
  description: string;
  // --- NEW FIELDS ---
  plaidCategory?: string;
  plaidDetailedCategory?: string;
  // ------------------
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

export interface SyncResponse {
  status: string;
  count: number;
}

export const getTransactions = async (): Promise<Transaction[]> => {
  const response = await axiosInstance.get<Transaction[]>('/transactions');
  return response.data;
};

export const syncTransactions = async (): Promise<SyncResponse> => {
  const response = await axiosInstance.post<SyncResponse>('/transactions/sync');
  // Return data or a safe default if something goes wrong
  return response.data || { status: 'UNKNOWN', count: 0 };
};