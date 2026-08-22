package com.salao.security.audit;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Registro de trilha de auditoria para acesso a dados pessoais (PII).
 * Persiste todas as operações realizadas por usuários autenticados
 * que envolvam leitura ou alteração de dados de titulares.
 */
@Entity
@Table(name = "audit_log", indexes = {
        @Index(name = "idx_audit_log_usuario_id", columnList = "usuario_id"),
        @Index(name = "idx_audit_log_data_hora", columnList = "data_hora")
})
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    /** Identificador do usuário no Keycloak (sub) ou "ApiTokenUser". */
    @Column(name = "usuario_id", nullable = false)
    private String usuarioId;

    /** Role do usuário no momento da operação. */
    @Column(nullable = false)
    private String role;

    /** Ação realizada (ex: LISTAR_CLIENTES, BUSCAR_CLIENTE). */
    @Column(nullable = false)
    private String acao;

    /** Entidade de domínio afetada (ex: Cliente). */
    @Column(nullable = false)
    private String entidade;

    /** ID do registro específico acessado, quando aplicável. */
    @Column(name = "entidade_id")
    private String entidadeId;

    /** Endereço IP de origem da requisição. */
    @Column(name = "ip_origem")
    private String ipOrigem;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @PrePersist
    protected void onCreate() {
        if (dataHora == null) {
            dataHora = LocalDateTime.now();
        }
    }
}
