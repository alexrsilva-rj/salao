package com.salao.agendamento.query;

import com.salao.agendamento.query.dto.RelatorioAgendamentoDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AgendamentoQueryService {

   @PersistenceContext
   private EntityManager entityManager;

   @Transactional(readOnly = true)
   public List<RelatorioAgendamentoDTO> buscarRelatorioPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
       String jpql = "SELECT new com.salao.agendamento.query.dto.RelatorioAgendamentoDTO(" +
                     "a.id, c.nome, p.nome, s.nome, s.preco, a.dataHoraInicio, a.status) " +
                     "FROM Agendamento a " +
                     "JOIN a.cliente c " +
                     "JOIN a.profissional p " +
                     "JOIN a.servico s " +
                     "WHERE a.dataHoraInicio BETWEEN :inicio AND :fim";

       return entityManager.createQuery(jpql, RelatorioAgendamentoDTO.class)
               .setParameter("inicio", inicio)
               .setParameter("fim", fim)
               .getResultList();
   }
}
