package com.salao.cliente.dto;

import com.salao.cliente.model.Cliente;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de saída para dados de clientes.
 * Nunca expõe a entidade JPA diretamente — garante controle
 * granular sobre quais campos são visíveis por perfil.
 */
@Value
@Builder
public class ClienteResponseDTO {

    UUID id;
    String nome;
    String email;
    String telefone;
    LocalDateTime dataCriacao;
    boolean consentimentoNotificacoes;
    boolean consentimentoMarketing;
    boolean anonimizado;

    public static ClienteResponseDTO from(Cliente cliente) {
        return ClienteResponseDTO.builder()
                .id(cliente.getId())
                .nome(cliente.getNome())
                .email(cliente.getEmail())
                .telefone(cliente.getTelefone())
                .dataCriacao(cliente.getDataCriacao())
                .consentimentoNotificacoes(cliente.isConsentimentoNotificacoes())
                .consentimentoMarketing(cliente.isConsentimentoMarketing())
                .anonimizado(cliente.isAnonimizado())
                .build();
    }
}
