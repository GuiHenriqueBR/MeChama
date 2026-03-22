import { api } from './auth';

// ─── Tipos ───────────────────────────────────────────────────────────────────

export interface SearchResult {
  // Serviço
  serviceId: number;
  title: string;
  description?: string;
  basePrice: number;
  durationMinutes?: number;
  whatIsIncluded?: string;

  // Prestador
  providerUserId: number;
  providerProfileId: number;
  providerName: string;
  providerAvatarUrl?: string;
  avgRating: number;
  totalReviews: number;
  city?: string;
  neighborhood?: string;
  serviceRadiusKm?: number;

  // Categoria
  categoryId: number;
  categoryName: string;
  categoryIcon?: string;
  categorySlug: string;

  // Ranking
  /** Score final (0.0 a 1.0) — reputação + relevância + destaque + proximidade */
  score: number;
}

export interface SearchParams {
  q?: string;
  categoryId?: number;
  city?: string;
  minRating?: number;
  maxPrice?: number;
  minPrice?: number;
  page?: number;
  size?: number;
}

/** Resposta paginada — mesma estrutura que Spring Data Page<T> */
export interface SearchPage {
  content: SearchResult[];
  totalElements: number;
  totalPages: number;
  number: number;      // página atual (zero-indexed)
  size: number;
  first: boolean;
  last: boolean;
}

// ─── API calls ───────────────────────────────────────────────────────────────

/**
 * Busca serviços com filtros e ordenação por score multi-critério.
 * Endpoint público — não requer autenticação.
 *
 * Exemplo:
 *   searchServices({ q: 'eletricista', city: 'São Paulo', minRating: 4.0 })
 */
export async function searchServices(params: SearchParams = {}): Promise<SearchPage> {
  const { data } = await api.get<SearchPage>('/search', { params });
  return data;
}
