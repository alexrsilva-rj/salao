package com.salao.financeiro.repository;

import com.salao.financeiro.model.Financeiro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface FinanceiroRepository extends JpaRepository<Financeiro, UUID> {
}
