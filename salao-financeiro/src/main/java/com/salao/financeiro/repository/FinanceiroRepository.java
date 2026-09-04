package com.salao.financeiro.repository;

import com.salao.financeiro.model.Financeiro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface FinanceiroRepository extends JpaRepository<Financeiro, UUID> {

    /**
     * Verifica se já existe um registro financeiro para o agendamento informado.
     *
     * <p>Usado pelo {@link com.salao.financeiro.service.FinanceiroService} para
     * impedir pagamentos duplicados (Issue 18 / V3 — idempotência de pagamentos).</p>
     *
     * @param agendamentoId ID do agendamento
     * @return {@code true} se já existe um Financeiro para este agendamento
     */
    boolean existsByAgendamentoId(UUID agendamentoId);
}
