import api from './auth';

export type AppointmentStatus = 'PENDING' | 'CONFIRMED' | 'REJECTED' | 'COUNTER_PROPOSED';

export interface ScheduleSlot {
  id: number;
  providerProfileId: number;
  date: string;        // 'YYYY-MM-DD'
  startTime: string;   // 'HH:mm:ss'
  endTime: string;
  available: boolean;
  createdAt: string;
}

export interface AppointmentRequest {
  id: number;
  clientId: number;
  clientName: string;
  providerProfileId: number;
  providerUserId: number;
  providerName: string;
  serviceId: number;
  serviceTitle: string;
  scheduleId: number | null;
  proposedDatetime: string;
  status: AppointmentStatus;
  counterDatetime: string | null;
  notes: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface PagedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

// ─── Agenda (Schedule) ───────────────────────────────────────────────────────

/** Horários disponíveis de um prestador — público, sem token */
export async function getProviderAvailableSlots(providerUserId: number): Promise<ScheduleSlot[]> {
  const res = await api.get<ScheduleSlot[]>(`/schedule/provider/${providerUserId}`);
  return res.data;
}

/** Todos os meus horários — prestador autenticado */
export async function getMyScheduleSlots(): Promise<ScheduleSlot[]> {
  const res = await api.get<ScheduleSlot[]>('/schedule/mine');
  return res.data;
}

/** Adicionar horário disponível — prestador autenticado */
export async function addScheduleSlot(data: {
  date: string;
  startTime: string;
  endTime: string;
}): Promise<ScheduleSlot> {
  const res = await api.post<ScheduleSlot>('/schedule', data);
  return res.data;
}

/** Remover horário — prestador autenticado */
export async function deleteScheduleSlot(scheduleId: number): Promise<void> {
  await api.delete(`/schedule/${scheduleId}`);
}

// ─── Solicitações de Agendamento (AppointmentRequest) ────────────────────────

/** Cliente solicita um agendamento */
export async function createAppointmentRequest(data: {
  providerUserId: number;
  serviceId: number;
  scheduleId?: number;
  proposedDatetime: string;
  notes?: string;
}): Promise<AppointmentRequest> {
  const res = await api.post<AppointmentRequest>('/appointments', data);
  return res.data;
}

/** Cliente lista suas solicitações */
export async function getMyAppointments(
  status?: AppointmentStatus,
  page = 0,
  size = 10,
): Promise<PagedResponse<AppointmentRequest>> {
  const params: Record<string, unknown> = { page, size };
  if (status) params.status = status;
  const res = await api.get<PagedResponse<AppointmentRequest>>('/appointments/mine', { params });
  return res.data;
}

/** Prestador lista solicitações recebidas */
export async function getReceivedAppointments(
  status?: AppointmentStatus,
  page = 0,
  size = 10,
): Promise<PagedResponse<AppointmentRequest>> {
  const params: Record<string, unknown> = { page, size };
  if (status) params.status = status;
  const res = await api.get<PagedResponse<AppointmentRequest>>('/appointments/received', { params });
  return res.data;
}

/** Prestador responde a uma solicitação */
export async function respondToAppointment(
  appointmentId: number,
  data: { action: AppointmentStatus; counterDatetime?: string },
): Promise<AppointmentRequest> {
  const res = await api.patch<AppointmentRequest>(
    `/appointments/${appointmentId}/respond`,
    data,
  );
  return res.data;
}
