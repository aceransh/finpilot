import axios from 'axios';

export interface Transaction {
    id: number;
    date:   string;
    amount: number;
    merchant: string;
    category: string;
}

const client = axios.create({ baseURL: '/api/v1' });

// api.ts (optional refactor)
export const getTransactions = async () =>
    (await client.get<Transaction[]>('/transactions')).data;

export const createTransaction = async (tx: Omit<Transaction,'id'>) =>
    (await client.post<Transaction>('/transactions', tx)).data;

export const updateTransaction = async (id: number, tx: Omit<Transaction,'id'>) =>
    (await client.put<Transaction>(`/transactions/${id}`, tx)).data;

export const deleteTransaction = async (id: number) => {
    await client.delete(`/transactions/${id}`);
};