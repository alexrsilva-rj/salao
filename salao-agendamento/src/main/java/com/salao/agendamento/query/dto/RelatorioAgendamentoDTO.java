package com.salao.agendamento.query.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RelatorioAgendamentoDTO {
   private UUID agendamentoId;
   private String nomeCliente;
   private String nomeProfissional;
   private String nomeServico;
   private BigDecimal precoServico;
   private LocalDateTime dataHoraInicio;
   private String status;
}
