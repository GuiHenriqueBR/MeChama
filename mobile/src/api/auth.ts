import axios from 'axios';
import AsyncStorage from '@react-native-async-storage/async-storage';

/**
 * Instância base do axios para o MeChama.
 * Todos os módulos futuros (serviços, pedidos, pagamentos)
 * devem importar este `api` e adicionar seus próprios endpoints.
 */
export const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

/**
 * Interceptor: injeta o JWT Bearer em todas as requisições autenticadas.
 * Assim, quando implementarmos os próximos módulos, o token já vai
 * junto automaticamente.
 */
api.interceptors.request.use(async (config) => {
  const token = await AsyncStorage.getItem('@mechama_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// ─── Tipos ──────────────────────────────────────────────────────────────────

export type UserType = 'CLIENT' | 'PROVIDER';

export interface UserResponse {
  id: number;
  name: string;
  email: string;
  phone?: string;
  type: UserType;
  active: boolean;
  createdAt: string;
  token: string;
}

// ─── Funções de autenticação ─────────────────────────────────────────────────

/**
 * Cadastra novo usuário e persiste o token localmente.
 */
export async function register(
  name: string,
  email: string,
  password: string,
  type: UserType,
  phone?: string,
): Promise<UserResponse> {
  const { data } = await api.post<UserResponse>('/auth/register', {
    name,
    email,
    password,
    type,
    phone,
  });
  await AsyncStorage.setItem('@mechama_token', data.token);
  await AsyncStorage.setItem('@mechama_user', JSON.stringify(data));
  return data;
}

/**
 * Autentica o usuário e persiste o token localmente.
 */
export async function login(
  email: string,
  password: string,
): Promise<UserResponse> {
  const { data } = await api.post<UserResponse>('/auth/login', {
    email,
    password,
  });
  await AsyncStorage.setItem('@mechama_token', data.token);
  await AsyncStorage.setItem('@mechama_user', JSON.stringify(data));
  return data;
}

/**
 * Remove token e dados do usuário (logout).
 */
export async function logout(): Promise<void> {
  await AsyncStorage.multiRemove(['@mechama_token', '@mechama_user']);
}

/**
 * Retorna o usuário salvo localmente (sem chamada de rede).
 */
export async function getStoredUser(): Promise<UserResponse | null> {
  const raw = await AsyncStorage.getItem('@mechama_user');
  return raw ? JSON.parse(raw) : null;
}
