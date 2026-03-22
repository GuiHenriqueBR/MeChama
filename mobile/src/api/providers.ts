import { api } from './auth';

// ─── Tipos ───────────────────────────────────────────────────────────────────

export interface PortfolioItem {
  id: number;
  title: string;
  description?: string;
  photoUrl?: string;
  createdAt: string;
}

export interface ProviderProfileResponse {
  id: number;
  userId: number;
  userName: string;
  userEmail: string;
  bio?: string;
  avatarUrl?: string;
  city?: string;
  neighborhood?: string;
  experienceYears?: number;
  specialties: string[];
  avgRating: number;
  totalReviews: number;
  serviceRadiusKm: number;
  available: boolean;
  portfolioItems: PortfolioItem[];
  updatedAt: string;
}

export interface ProviderProfileRequest {
  bio?: string;
  avatarUrl?: string;
  city?: string;
  neighborhood?: string;
  experienceYears?: number;
  specialties?: string[];
  serviceRadiusKm?: number;
  available?: boolean;
}

export interface PortfolioItemRequest {
  title: string;
  description?: string;
  photoUrl?: string;
}

// ─── API calls ───────────────────────────────────────────────────────────────

/**
 * Busca o perfil público de um prestador pelo userId.
 * Não requer autenticação — usado na tela de detalhe.
 */
export async function getProviderProfile(userId: number): Promise<ProviderProfileResponse> {
  const { data } = await api.get<ProviderProfileResponse>(`/providers/${userId}/profile`);
  return data;
}

/**
 * Busca o portfólio público de um prestador.
 */
export async function getProviderPortfolio(userId: number): Promise<PortfolioItem[]> {
  const { data } = await api.get<PortfolioItem[]>(`/providers/${userId}/portfolio`);
  return data;
}

/**
 * Retorna o perfil do prestador autenticado (para a tela de edição).
 * Requer JWT com ROLE_PROVIDER.
 */
export async function getMyProfile(): Promise<ProviderProfileResponse> {
  const { data } = await api.get<ProviderProfileResponse>('/providers/me/profile');
  return data;
}

/**
 * Cria ou atualiza o perfil do prestador autenticado.
 */
export async function upsertMyProfile(
  request: ProviderProfileRequest,
): Promise<ProviderProfileResponse> {
  const { data } = await api.put<ProviderProfileResponse>('/providers/me/profile', request);
  return data;
}

/**
 * Adiciona um item ao portfólio do prestador autenticado.
 */
export async function addPortfolioItem(
  request: PortfolioItemRequest,
): Promise<PortfolioItem> {
  const { data } = await api.post<PortfolioItem>('/providers/me/portfolio', request);
  return data;
}

/**
 * Remove um item do portfólio.
 */
export async function deletePortfolioItem(itemId: number): Promise<void> {
  await api.delete(`/providers/me/portfolio/${itemId}`);
}
