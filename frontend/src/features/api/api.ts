import axios from 'axios';

export interface Page<T> {
    content: T[];
    totalPages: number;
    totalElements: number;
    size: number;   // page size
    number: number; // current page index (0-based)
}

export interface Transaction {
    id: number;
    date: string;
    amount: number;
    merchant: string;
    category: string;
    categoryId?: string;        // <-- add
    categoryLocked?: boolean;   // <-- add if you return it
    categoryName?: string;      // <-- if you return it separate from `category`
}

const client = axios.create({ baseURL: '/api/v1' });

// ---- Query types ----
export type SortDir = 'asc' | 'desc';

export interface TxQuery {
    page?: number;          // default 0
    size?: number;          // default 10
    sort?: string;          // e.g. "date,desc" or "amount,asc"
    q?: string;             // search text
    category?: string;
    from?: string;          // "YYYY-MM-DD"
    to?: string;            // "YYYY-MM-DD"
}

// ---- API calls ----
export const getTransactions = async (params: TxQuery = {}) => {
    const { page = 0, size = 10, sort = 'date,desc', q, category, from, to } = params;

    const p: Record<string, unknown> = { page, size, sort };
    if (q) p.q = q;
    if (category) p.category = category;
    if (from) p.from = from;
    if (to) p.to = to;

    const { data } = await client.get<Page<Transaction>>('/transactions', { params: p });
    return data;
};

export type TxUpsert = Omit<Transaction, 'id' | 'categoryName' | 'categoryLocked'>;

export const createTransaction = async (tx: TxUpsert) => {
    const { data } = await client.post<Transaction>('/transactions', tx);
    return data;
};

export const updateTransaction = async (id: number, tx: TxUpsert) => {
    const { data } = await client.put<Transaction>(`/transactions/${id}`, tx);
    return data;
};

export const deleteTransaction = async (id: number) => {
    await client.delete(`/transactions/${id}`);
};

export interface Category {
    id: string;
    name: string;
    type: 'EXPENSE' | 'INCOME';
    color?: string | null; // allow nulls from DB
}

export interface CategoryRequest {
    name: string;
    type: 'EXPENSE' | 'INCOME';
    color?: string;
}

export const getCategories = async () => {
    const { data } = await client.get<Category[]>('/categories');
    return data;
};

export const createCategory = async (body: CategoryRequest) => {
    const { data } = await client.post<Category>('/categories', body);
    return data;
};

// ---- Rules types ----
export type MatchType = 'CONTAINS' | 'REGEX';

export interface Rule {
    id: string;
    pattern: string;
    matchType: MatchType;
    categoryId: string;      // FK
    categoryName: string;    // convenience from backend
    priority: number;
    enabled: boolean;
}

export interface RuleRequest {
    pattern: string;
    matchType: MatchType;
    categoryId: string;
    priority?: number;
    enabled?: boolean;
}

// ---- Rules API ----
export const getRules = async () => {
    const { data } = await client.get<Rule[]>('/rules');
    return data;
};

export const createRule = async (body: RuleRequest) => {
    const { data } = await client.post<Rule>('/rules', body);
    return data;
};

export const updateRule = async (id: string, body: RuleRequest) => {
    const { data } = await client.put<Rule>(`/rules/${id}`, body);
    return data;
};

export const deleteRule = async (id: string) => {
    await client.delete(`/rules/${id}`);
};

export const testRule = async (merchant: string) => {
    const { data } = await client.post<{ matched: boolean; ruleId?: string; categoryId?: string; categoryName?: string }>(
        '/rules/test',
        { merchant }
    );
    return data;
};