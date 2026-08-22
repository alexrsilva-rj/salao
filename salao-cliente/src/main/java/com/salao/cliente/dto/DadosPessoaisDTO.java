package com.salao.cliente.dto;

import com.salao.cliente.model.Cliente;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO completo para portabilidade de dados (Art. 18, V LGPD).
 * Contém todos os dados pessoais do titular em formato exportável.
 */
@Value
@Builder
public class DadosPessoaisDTO {

    UUID id;
    String nome;
    String email;
    String telefone;
    LocalDateTime dataCriacao;
    LocalDateTime ultimaAtividade;

    // Consentimentos
    boolean consentimentoTermosAceito;
    LocalDateTime dataConsentimentoTermos;
    boolean consentimentoNotificacoes;
    LocalDateTime dataConsentimentoNotificacoes;
    boolean consentimentoMarketing;
    LocalDateTime dataConsentimentoMarketing;

    // Status de anonimização
    boolean anonimizado;
    LocalDateTime dataAnonimizacao;

    public static DadosPessoaisDTO from(Cliente c) {
        return DadosPessoaisDTO.builder()
                .id(c.getId())
                .nome(c.getNome())
                .email(c.getEmail())
                .telefone(c.getTelefone())
                .dataCriacao(c.getDataCriacao())
                .ultimaAtividade(c.getUltimaAtividade())
                .consentimentoTermosAceito(c.isConsentimentoTermosAceito())
                .dataConsentimentoTermos(c.getDataConsentimentoTermos())
                .consentimentoNotificacoes(c.isConsentimentoNotificacoes())
                .dataConsentimentoNotificacoes(c.getDataConsentimentoNotificacoes())
                .consentimentoMarketing(c.isConsentimentoMarketing())
                .dataConsentimentoMarketing(c.getDataConsentimentoMarketing())
                .anonimizado(c.isAnonimizado())
                .dataAnonimizacao(c.getDataAnonimizacao())
                .build();
    }
}
