package com.salao.cliente.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO para gestão e consulta de consentimentos do titular (Art. 8º LGPD).
 */
@Data
public class ConsentimentoDTO {
    private boolean consentimentoTermosAceito;
    private LocalDateTime dataConsentimentoTermos;
    private boolean consentimentoNotificacoes;
    private LocalDateTime dataConsentimentoNotificacoes;
    private boolean consentimentoMarketing;
    private LocalDateTime dataConsentimentoMarketing;
}
