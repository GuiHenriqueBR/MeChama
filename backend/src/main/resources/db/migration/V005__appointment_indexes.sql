-- V005__appointment_indexes.sql
-- Índices de performance para Schedule e AppointmentRequest (Tarefa 5)
--
-- A query findAvailableByProvider usa:
--   WHERE provider_profile_id = ? AND available = true AND date >= ?
--
-- A query findByProviderProfile / findByClient usa:
--   WHERE provider_profile_id = ? (ou client_id = ?) ORDER BY created_at DESC
--
-- existsConflict usa:
--   WHERE provider_profile_id = ? AND date = ? AND start_time < ? AND end_time > ?

-- schedules
CREATE INDEX IF NOT EXISTS idx_schedules_provider_date
    ON schedules (provider_profile_id, date);

CREATE INDEX IF NOT EXISTS idx_schedules_available
    ON schedules (available)
    WHERE available = true;

-- appointment_requests
CREATE INDEX IF NOT EXISTS idx_appointment_requests_provider
    ON appointment_requests (provider_profile_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_appointment_requests_client
    ON appointment_requests (client_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_appointment_requests_status
    ON appointment_requests (status);
