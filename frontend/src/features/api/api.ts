import axios, { AxiosHeaders } from 'axios';
import { getAuth } from 'firebase/auth';
import '../../firebase';

/* ----------------------------- HTTP client ----------------------------- */
export const client = axios.create({ baseURL: '/api/v1' });

/* ------------------------------ Shared types --------------------------- */
export interface Page<T> {
    content: T[];
    totalPages: number;
    totalElements: number;
    size: number;
    number: number; // 0-based page index
}

/* ============================== Transactions =========================== */
export interface Transaction {
    id: number;
    date: string;          // YYYY-MM-DD
    amount: number;
    merchant: string;
    category: string;      // legacy label kept for now
    categoryId?: string;   // FK to categories
    categoryLocked?: boolean;
    categoryName?: string; // resolved name from FK
}

export type SortDir = 'asc' | 'desc';

export interface TxQuery {
    page?: number;
    size?: number;
    sort?: string;   // e.g. "date,desc"
    q?: string;
    category?: string;
    from?: string;   // YYYY-MM-DD
    to?: string;     // YYYY-MM-DD
    accountId?: string;
}

export type TxUpsert = Omit<Transaction, 'id' | 'categoryName' | 'categoryLocked'>;

export const getTransactions = async (params: TxQuery = {}) => {
    const { page = 0, size = 10, sort = 'date,desc', q, category, from, to, accountId } = params;
    const p: Record<string, unknown> = { page, size, sort };
    if (q) p.q = q;
    if (category) p.category = category;
    if (from) p.from = from;
    if (to) p.to = to;
    if (accountId) p.accountId = accountId; // NEW

    const { data } = await client.get<Page<Transaction>>('/transactions', { params: p });
    return data;
};

export const createTransaction = async (tx: TxUpsert) => {
    const { data } = await client.post<Transaction>('/transactions', tx);
    return data;
};

export const createTransactionForce = async (tx: TxUpsert) => {
    const { data } = await client.post<Transaction>('/transactions', tx, {
        params: { force: true },
    });
    return data;
};

export const updateTransaction = async (
    id: number,
    tx: TxUpsert,
    opts?: { force?: boolean }
) => {
    const params = opts?.force ? { force: true } : undefined;
    const { data } = await client.put<Transaction>(`/transactions/${id}`, tx, { params });
    return data;
};

export const deleteTransaction = async (id: number) => {
    await client.delete(`/transactions/${id}`);
};

/* ================================ Categories =========================== */
export interface Category {
    id: string;
    name: string;
    type: 'EXPENSE' | 'INCOME';
    color?: string | null;
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

export const updateCategory = async (id: string, body: CategoryRequest) => {
    const { data } = await client.put<Category>(`/categories/${id}`, body);
    return data;
};

export const deleteCategory = async (id: string) => {
    await client.delete(`/categories/${id}`);
};

/* ================================= Rules =============================== */
export type MatchType = 'CONTAINS' | 'REGEX';

export interface Rule {
    id: string;
    pattern: string;
    matchType: MatchType;
    categoryId: string;
    categoryName: string;
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
    const { data } = await client.post<{
        matched: boolean;
        ruleId?: string;
        categoryId?: string;
        categoryName?: string;
    }>('/rules/test', { merchant });
    return data;
};

/* ================================= Auth =============================== */
client.interceptors.request.use(async (config) => {
    // make sure headers is the right type
    const headers = AxiosHeaders.from(config.headers);

    try {
        const auth = getAuth();
        const user = auth.currentUser;

        if (user) {
            const token = await user.getIdToken();
            headers.set('Authorization', `Bearer ${token}`);
            headers.delete('X-Demo-User');
        } else {
            headers.set('X-Demo-User', 'demo');
            headers.delete('Authorization');
        }
    } catch {
        // safe fallback for local/dev
        headers.set('X-Demo-User', 'demo');
        headers.delete('Authorization');
    }

    // assign the correctly typed headers back
    config.headers = headers;
    return config;
});

/* ================================= Plaid =============================== */
type LinkTokenRes = { link_token: string } | { linkToken: string };

export type PlaidSyncResponse = {
    created: number;
    updated: number;
    removed: number;
    skipped: number;
};

export const createPlaidLinkToken = async (): Promise<string> => {
    const { data } = await client.post<LinkTokenRes>('/plaid/link/token/create', {});
    const token = (data as any).link_token ?? (data as any).linkToken;
    if (!token) throw new Error('Missing link_token in response');
    return token;
};

export async function syncPlaidItem(itemId: string): Promise<PlaidSyncResponse> {
    // Use axios client, not fetch
    const { data } = await client.post<PlaidSyncResponse>(`/plaid/items/${itemId}/sync`);
    return data;
}

export interface PlaidItemSummary {
    id: string;                 // DB UUID
    plaidItemId: string;        // Plaid item_id
    institutionId?: string|null;
    institutionName?: string|null;
    createdAt: string;          // ISO timestamp
}

export const listPlaidItems = async (): Promise<PlaidItemSummary[]> => {
    const { data } = await client.get<PlaidItemSummary[]>('/plaid/items');
    return data;
};

export interface PlaidAccount {
    accountId: string;      // ✅ matches backend
    name?: string;
    officialName?: string;
    mask?: string;
    subtype?: string;
    institutionName?: string;
    plaidItemId?: string;
    itemDbId?: string;
}

export const getPlaidAccounts = async (): Promise<PlaidAccount[]> => {
    const { data } = await client.get<PlaidAccount[]>('/plaid/accounts');
    return data;
};

