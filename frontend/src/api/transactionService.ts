import axiosInstance from './axiosConfig';

export interface Category {
  id: string; // <--- FIXED: UUIDs are strings
  name: string;
  colorHex: string;
}

export interface Transaction {
  id: string;
  plaidTransactionId: string;
  amount: number;
  date: string;
  description: string;
  plaidCategory?: string;
  plaidDetailedCategory?: string;
  category: Category | null; // Reuse the Category interface above
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
  return response.data || { status: 'UNKNOWN', count: 0 };
};

export interface UpdateTransactionRequest {
  categoryId?: string; // <--- FIXED: UUID string
  description?: string;
}

export const updateTransaction = async (
    id: string,
    data: UpdateTransactionRequest
): Promise<Transaction> => {
  const response = await axiosInstance.patch<Transaction>(`/transactions/${id}`, data);
  return response.data;
};

export interface CreateCategoryRequest {
  name: string;
  colorHex?: string;
}

export const createCategory = async (
    data: CreateCategoryRequest
): Promise<Category> => {
  const response = await axiosInstance.post<Category>('/categories', data);
  return response.data;
};

export const getCategories = async (): Promise<Category[]> => {
  const response = await axiosInstance.get<Category[]>('/categories');
  return response.data;
};