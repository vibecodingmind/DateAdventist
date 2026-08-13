import type { AuthUser, ChatMessage, LikeReceived, Match, NotificationItem, Profile, Subscription } from './types';

const TOKEN_KEY = 'ah_access';
const REFRESH_KEY = 'ah_refresh';
const USER_KEY = 'ah_user';

export function apiOrigin(): string {
  return (import.meta.env.VITE_API_BASE_URL || '').replace(/\/$/, '');
}

function apiUrl(path: string): string {
  return `${apiOrigin()}${path}`;
}

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY);
}

export function getStoredUser(): AuthUser | null {
  const raw = localStorage.getItem(USER_KEY);
  if (!raw) return null;
  try {
    return JSON.parse(raw) as AuthUser;
  } catch {
    return null;
  }
}

export function saveSession(user: AuthUser) {
  localStorage.setItem(TOKEN_KEY, user.accessToken);
  localStorage.setItem(REFRESH_KEY, user.refreshToken);
  localStorage.setItem(USER_KEY, JSON.stringify(user));
}

export function clearSession() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(REFRESH_KEY);
  localStorage.removeItem(USER_KEY);
}

type Envelope<T> = { success: boolean; data?: T; error?: { code: string; message: string } };

async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  const headers = new Headers(init.headers);
  if (!(init.body instanceof FormData) && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json');
  }
  headers.set('Accept', 'application/json');
  const token = getToken();
  if (token) headers.set('Authorization', `Bearer ${token}`);

  const res = await fetch(apiUrl(path), { ...init, headers });
  const body = (await res.json().catch(() => null)) as Envelope<T> | null;
  if (!res.ok || !body?.success || body.data === undefined) {
    throw new Error(body?.error?.message || `Request failed (${res.status})`);
  }
  return body.data;
}

export const api = {
  register: (payload: {
    email: string;
    password: string;
    fullName: string;
    age: number;
    gender: string;
    country: string;
    city: string;
    relationshipIntention?: string;
  }) => request<AuthUser>('/api/v1/auth/register', { method: 'POST', body: JSON.stringify(payload) }),

  login: (email: string, password: string) =>
    request<AuthUser>('/api/v1/auth/login', { method: 'POST', body: JSON.stringify({ email, password }) }),

  forgotPassword: (email: string) =>
    request<{ message: string }>('/api/v1/auth/forgot-password', { method: 'POST', body: JSON.stringify({ email }) }),

  resetPassword: (token: string, password: string) =>
    request<{ message: string }>('/api/v1/auth/reset-password', {
      method: 'POST',
      body: JSON.stringify({ token, password }),
    }),

  deleteAccount: () => request<{ message: string }>('/api/v1/auth/delete-account', { method: 'POST', body: '{}' }),

  me: () => request<AuthUser>('/api/v1/auth/me'),
  profile: () => request<Profile>('/api/v1/profile'),
  updateProfile: (payload: Partial<Profile>) =>
    request<Profile>('/api/v1/profile', { method: 'PUT', body: JSON.stringify(payload) }),
  updateFaith: (payload: Partial<Profile>) =>
    request<Profile>('/api/v1/profile/faith', { method: 'PUT', body: JSON.stringify(payload) }),

  uploadPhoto: async (file: File, kind: 'profile' | 'gallery' | 'verification' = 'profile') => {
    const form = new FormData();
    form.append('photo', file);
    return request<{ url: string; kind: string; profile: Profile }>(
      `/api/v1/profile/photo?kind=${encodeURIComponent(kind)}`,
      { method: 'POST', body: form }
    );
  },

  discover: () => request<Profile[]>('/api/v1/discover'),
  like: (toUserId: string, isSuperLike = false) =>
    request<{ isMatch: boolean; matchId?: string; compatibilityScore: number }>('/api/v1/discover/like', {
      method: 'POST',
      body: JSON.stringify({ toUserId, isSuperLike }),
    }),
  pass: (toUserId: string) =>
    request<{ passed: boolean }>('/api/v1/discover/pass', { method: 'POST', body: JSON.stringify({ toUserId }) }),

  likes: () => request<LikeReceived[]>('/api/v1/likes'),
  matches: () => request<Match[]>('/api/v1/matches'),
  messages: (matchId: string) => request<ChatMessage[]>(`/api/v1/matches/${matchId}/messages`),
  sendMessage: (matchId: string, text: string) =>
    request<ChatMessage>(`/api/v1/matches/${matchId}/messages`, { method: 'POST', body: JSON.stringify({ text }) }),
  unmatch: (matchId: string) =>
    request<{ unmatched: boolean }>(`/api/v1/matches/${matchId}`, { method: 'DELETE' }),

  report: (reportedUserId: string, reason: string, details: string) =>
    request<{ message?: string }>('/api/v1/safety/report', {
      method: 'POST',
      body: JSON.stringify({ reportedUserId, reason, details }),
    }),
  block: (blockedUserId: string) =>
    request<{ blocked: boolean }>('/api/v1/safety/block', { method: 'POST', body: JSON.stringify({ blockedUserId }) }),

  notifications: () => request<NotificationItem[]>('/api/v1/notifications'),
  subscription: () => request<Subscription>('/api/v1/subscriptions/current'),
  checkout: (planId: string, paymentProvider = 'stripe') =>
    request<{ checkoutUrl: string; transactionId: string }>('/api/v1/subscriptions/checkout', {
      method: 'POST',
      body: JSON.stringify({ planId, paymentProvider, billingCycle: 'MONTHLY' }),
    }),

  adminLogin: (email: string, password: string) =>
    request<AuthUser>('/api/v1/admin/login', { method: 'POST', body: JSON.stringify({ email, password }) }),
  adminDashboard: () =>
    request<{
      activeUsers: number;
      totalMatches: number;
      pendingVerifications: number;
      openReports: number;
      monthlyRevenue: number;
    }>('/api/v1/admin/dashboard'),

  legal: (kind: 'terms' | 'privacy') =>
    request<{ title: string; text: string }>(`/api/v1/legal/${kind}`),
};
