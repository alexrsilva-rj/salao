package com.salao.cliente.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO de entrada para atualização parcial de dados do cliente (Art. 18, IV — Retificação).
 * Apenas os campos aqui presentes podem ser atualizados pelo titular.
 *
 * <p>Bean Validation aplicada para garantir que apenas dados válidos
 * sejam persistidos no banco (Issue 19 / V4 — CWE-20).</p>
 */
@Data
public class ClienteUpdateDTO {

    /**
     * Nome completo do titular — mínimo 2, máximo 150 caracteres.
     */
    @Size(min = 2, max = 150, message = "O nome deve ter entre 2 e 150 caracteres.")
    private String nome;

    /**
     * Telefone/WhatsApp do titular.
     * Formato aceito: dígitos, espaços, +, ( ) e - com 7 a 20 caracteres.
     */
    @Pattern(
        regexp = "^\\+?[0-9 ()\\-]{7,20}$",
        message = "Formato de telefone inválido. Use apenas dígitos, espaços, +, ( ) ou -."
    )
    private String telefone;
}
