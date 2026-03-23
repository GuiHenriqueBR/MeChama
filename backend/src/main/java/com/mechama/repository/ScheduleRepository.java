package com.mechama.repository;

import com.mechama.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    /** Todos os horários disponíveis de um prestador a partir de hoje */
    @Query("""
        SELECT s FROM Schedule s
        WHERE s.providerProfile.id = :profileId
          AND s.available = true
          AND s.date >= :from
        ORDER BY s.date ASC, s.startTime ASC
        """)
    List<Schedule> findAvailableByProvider(
            @Param("profileId") Long profileId,
            @Param("from") LocalDate from);

    /** Todos os horários de um prestador (painel — inclui indisponíveis) */
    List<Schedule> findByProviderProfileIdOrderByDateAscStartTimeAsc(Long providerProfileId);

    /** Verifica se o horário pertence ao prestador (segurança) */
    boolean existsByIdAndProviderProfileId(Long scheduleId, Long providerProfileId);

    /** Verifica conflito de horário no mesmo dia e prestador */
    @Query("""
        SELECT COUNT(s) > 0 FROM Schedule s
        WHERE s.providerProfile.id = :profileId
          AND s.date = :date
          AND s.available = true
          AND s.startTime < :endTime
          AND s.endTime > :startTime
        """)
    boolean existsConflict(
            @Param("profileId") Long profileId,
            @Param("date") java.time.LocalDate date,
            @Param("startTime") java.time.LocalTime startTime,
            @Param("endTime") java.time.LocalTime endTime);
}
