-- V004__search_performance_indexes.sql
-- Índices de performance para a query nativa de busca (Tarefa 4)
--
-- A query searchServices() usa:
--   WHERE so.active = true AND pp.available = true AND u.active = true
--   AND LOWER(so.title)       LIKE ...
--   AND LOWER(so.description) LIKE ...
--   AND LOWER(sc.name)        LIKE ...
--   AND LOWER(pp.city)        LIKE ...
--   ORDER BY score DESC
--
-- Nota: índices parciais em colunas boolenas ajudam o planner a descartar
--       linhas inativas antes de avaliar os filtros de texto.

-- service_offerings
CREATE INDEX IF NOT EXISTS idx_service_offerings_active
    ON service_offerings (active)
    WHERE active = true;

CREATE INDEX IF NOT EXISTS idx_service_offerings_category
    ON service_offerings (category_id);

CREATE INDEX IF NOT EXISTS idx_service_offerings_provider
    ON service_offerings (provider_profile_id);

-- provider_profiles
CREATE INDEX IF NOT EXISTS idx_provider_profiles_available
    ON provider_profiles (available)
    WHERE available = true;

CREATE INDEX IF NOT EXISTS idx_provider_profiles_city
    ON provider_profiles (LOWER(city));

CREATE INDEX IF NOT EXISTS idx_provider_profiles_avg_rating
    ON provider_profiles (avg_rating DESC);

-- users
CREATE INDEX IF NOT EXISTS idx_users_active
    ON users (active)
    WHERE active = true;
