package com.salao.cliente.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO de entrada para criação de cliente.
 *
 * <p>Expõe <strong>apenas</strong> os campos autorizados para input do chamador.
 * Campos sensíveis como {@code keycloakUserId}, {@code id}, {@code anonimizado},
 * {@code dataConsentimentoTermos} e {@code dataAnonimizacao} são preenchidos
 * exclusivamente pelo backend — eliminando o risco de Mass Assignment (CWE-915).</p>
 *
 * <p>O {@code keycloakUserId} é extraído do token JWT autenticado via
 * {@link com.salao.common.security.UserContext}, nunca do payload da request.</p>
 *
 * @see com.salao.cliente.service.ClienteService#criarCliente(ClienteCreateDTO, com.salao.common.security.UserContext)
 */
@Data
public class ClienteCreateDTO {

    /**
     * Nome completo do titular (PII — Art. 5º, I, LGPD).
     */
    @NotBlank(message = "O nome é obrigatório.")
    @Size(min = 2, max = 150, message = "O nome deve ter entre 2 e 150 caracteres.")
    private String nome;

    /**
     * E-mail do titular — identificador único no sistema.
     */
    @NotBlank(message = "O e-mail é obrigatório.")
    @Email(message = "Formato de e-mail inválido.")
    @Size(max = 255)
    private String email;

    /**
     * Telefone/WhatsApp do titular (opcional).
     */
    @Pattern(
        regexp = "^\\+?[0-9 ()\\-]{7,20}$",
        message = "Formato de telefone inválido. Use apenas dígitos, espaços, +, ( ) ou -."
    )
    private String telefone;

    /**
     * Aceite dos Termos de Uso — base legal obrigatória (Art. 8º, LGPD).
     * Deve ser {@code true} para que o cadastro seja aceito.
     */
    @NotNull(message = "O campo consentimentoTermosAceito é obrigatório.")
    private Boolean consentimentoTermosAceito;

    /**
     * Opt-in para notificações transacionais (lembretes de agendamento).
     */
    private boolean consentimentoNotificacoes = false;

    /**
     * Opt-in para comunicações de marketing — finalidade separada (Art. 8º, LGPD).
     */
    private boolean consentimentoMarketing = false;
}
