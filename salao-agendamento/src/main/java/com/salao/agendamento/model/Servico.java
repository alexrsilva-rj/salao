package com.salao.agendamento.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "servico")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Servico {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String nome;

    private String descricao;

    @Column(nullable = false)
    private BigDecimal preco;

    @Column(nullable = false)
    private Integer duracaoMinutos; // Wait, let's look at SQLvsNoSQL.txt for field name!
    // In SQLvsNoSQL.txt:
    // Line 406: duracao_minutos INT NOT NULL
    // Line 483: private Integer duracaoMinutos;
    // Let's use duracaoMinutos.

    @Column(nullable = false)
    private boolean ativo = true;
}
