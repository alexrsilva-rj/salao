package com.salao.agendamento.dto;

import com.salao.agendamento.model.Agendamento;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de saída para agendamentos.
 * Evita a exposição direta da entidade JPA e permite
 * controle granular de quais campos são visíveis por perfil.
 */
@Value
@Builder
public class AgendamentoResponseDTO {

    UUID id;
    UUID clienteId;
    String nomeProfissional;
    String nomeServico;
    LocalDateTime dataHoraInicio;
    LocalDateTime dataHoraFim;
    String status;

    public static AgendamentoResponseDTO from(Agendamento a) {
        return AgendamentoResponseDTO.builder()
                .id(a.getId())
                .clienteId(a.getCliente().getId())
                .nomeProfissional(a.getProfissional().getNome())
                .nomeServico(a.getServico().getNome())
                .dataHoraInicio(a.getDataHoraInicio())
                .dataHoraFim(a.getDataHoraFim())
                .status(a.getStatus())
                .build();
    }
}
