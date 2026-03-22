import { api } from './auth';

// ─── Tipos ───────────────────────────────────────────────────────────────────

export interface ServiceCategory {
  id: number;
  name: string;
  slug: string;
  icon: string;
  minPrice: number;
}

export interface ServiceOfferingResponse {
  id: number;
  providerUserId: number;
  providerName: string;
  providerAvatarUrl?: string;
  providerAvgRating: number;
  providerTotalReviews: number;
  providerCity?: string;
  categoryId: number;
  categoryName: string;
  categoryIcon: string;
  categorySlug: string;
  title: string;
  description?: string;
  basePrice: number;
  durationMinutes?: number;
  whatIsIncluded?: string;
  whatIsNotIncluded?: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface ServiceOfferingRequest {
  categoryId: number;
  title: string;
  description?: string;
  basePrice: number;
  durationMinutes?: number;
  whatIsIncluded?: string;
  whatIsNotIncluded?: string;
  active?: boolean;
}

// ─── API calls ───────────────────────────────────────────────────────────────

/** Lista todas as categorias ativas com preços mínimos — público */
export async function getCategories(): Promise<ServiceCategory[]> {
  const { data } = await api.get<ServiceCategory[]>('/categories');
  return data;
}

/** Serviços ativos de um prestador — público */
export async function getProviderServices(userId: number): Promise<ServiceOfferingResponse[]> {
  const { data } = await api.get<ServiceOfferingResponse[]>(`/providers/${userId}/services`);
  return data;
}

/** Detalhe de um serviço específico — público */
export async function getServiceById(serviceId: number): Promise<ServiceOfferingResponse> {
  const { data } = await api.get<ServiceOfferingResponse>(`/services/${serviceId}`);
  return data;
}

/** Todos os serviços do prestador autenticado (incluindo inativos) */
export async function getMyServices(): Promise<ServiceOfferingResponse[]> {
  const { data } = await api.get<ServiceOfferingResponse[]>('/providers/me/services');
  return data;
}

/** Cria um novo serviço */
export async function createService(
  request: ServiceOfferingRequest,
): Promise<ServiceOfferingResponse> {
  const { data } = await api.post<ServiceOfferingResponse>('/providers/me/services', request);
  return data;
}

/** Atualiza um serviço existente */
export async function updateService(
  serviceId: number,
  request: ServiceOfferingRequest,
): Promise<ServiceOfferingResponse> {
  const { data } = await api.put<ServiceOfferingResponse>(
    `/providers/me/services/${serviceId}`,
    request,
  );
  return data;
}

/** Ativa ou desativa um serviço */
export async function toggleService(serviceId: number): Promise<ServiceOfferingResponse> {
  const { data } = await api.patch<ServiceOfferingResponse>(
    `/providers/me/services/${serviceId}/toggle`,
  );
  return data;
}

/** Exclui permanentemente um serviço */
export async function deleteService(serviceId: number): Promise<void> {
  await api.delete(`/providers/me/services/${serviceId}`);
}
