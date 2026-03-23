package com.mechama.repository;

import com.mechama.model.AppointmentRequest;
import com.mechama.model.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AppointmentRequestRepository extends JpaRepository<AppointmentRequest, Long> {

    /** Solicitações recebidas pelo prestador (via perfil) */
    @Query("""
        SELECT a FROM AppointmentRequest a
        WHERE a.providerProfile.id = :profileId
          AND (:status IS NULL OR a.status = :status)
        ORDER BY a.createdAt DESC
        """)
    Page<AppointmentRequest> findByProviderProfile(
            @Param("profileId") Long profileId,
            @Param("status") AppointmentStatus status,
            Pageable pageable);

    /** Solicitações feitas pelo cliente */
    @Query("""
        SELECT a FROM AppointmentRequest a
        WHERE a.client.id = :clientId
          AND (:status IS NULL OR a.status = :status)
        ORDER BY a.createdAt DESC
        """)
    Page<AppointmentRequest> findByClient(
            @Param("clientId") Long clientId,
            @Param("status") AppointmentStatus status,
            Pageable pageable);

    /** Verifica se a solicitação pertence ao prestador (segurança) */
    boolean existsByIdAndProviderProfileId(Long appointmentId, Long profileId);

    /** Verifica se a solicitação pertence ao cliente (segurança) */
    boolean existsByIdAndClientId(Long appointmentId, Long clientId);
}
