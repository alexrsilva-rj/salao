package com.salao.agendamento.repository;

import com.salao.agendamento.model.Agendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, UUID> {

    @Query("""
           SELECT a FROM Agendamento a
           WHERE a.profissional.id = :profissionalId
             AND a.status <> 'CANCELADO'
             AND a.dataHoraInicio < :fim
             AND a.dataHoraFim > :inicio
           """)
    List<Agendamento> findConflitantes(
            @Param("profissionalId") UUID profissionalId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim);

    /**
     * Busca agendamentos pertencentes ao titular identificado pelo keycloakUserId.
     * Usado para isolamento de dados quando role = ROLE_CUSTOMER.
     */
    @Query("""
           SELECT a FROM Agendamento a
           JOIN a.cliente c
           WHERE c.keycloakUserId = :keycloakUserId
           """)
    List<Agendamento> findByClienteKeycloakUserId(@Param("keycloakUserId") String keycloakUserId);
}
