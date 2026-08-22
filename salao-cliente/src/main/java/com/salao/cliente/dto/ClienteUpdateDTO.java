package com.salao.cliente.dto;

import lombok.Data;

/**
 * DTO de entrada para atualização parcial de dados do cliente (Art. 18, IV — Retificação).
 * Apenas os campos aqui presentes podem ser atualizados pelo titular.
 */
@Data
public class ClienteUpdateDTO {
    private String nome;
    private String telefone;
}
