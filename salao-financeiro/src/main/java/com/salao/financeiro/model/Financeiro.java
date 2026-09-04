package com.salao.financeiro.model;

import com.salao.agendamento.model.Agendamento;
import com.salao.agendamento.model.Profissional;
import com.salao.financeiro.model.FormaPagamentoEnum;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "financeiro")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Financeiro {

   @Id
   @GeneratedValue(strategy = GenerationType.AUTO)
   private UUID id;

   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "agendamento_id", nullable = false)
   private Agendamento agendamento;

   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "profissional_id", nullable = false)
   private Profissional profissional;

   @Column(nullable = false)
   private BigDecimal valorTotal;

   @Column(nullable = false)
   private BigDecimal percentualComissao;

   @Column(nullable = false)
   private BigDecimal valorComissao;

   @Column(nullable = false)
   private BigDecimal valorLiquidoSalao;

   @Enumerated(EnumType.STRING)
   @Column(nullable = false)
   private FormaPagamentoEnum formaPagamento;

   @Column(nullable = false)
   private String status; // PAGO, PENDENTE

   @Column(nullable = false)
   private LocalDateTime dataPagamento;

   @PrePersist
   protected void onCreate() {
       if (dataPagamento == null) {
           dataPagamento = LocalDateTime.now();
       }
       if (status == null) {
           status = "PAGO";
       }
   }
}
