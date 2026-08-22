package com.salao.cliente.model;

import com.salao.common.annotation.PII;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cliente")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    // ── Dados pessoais (PII) ──────────────────────────────────────

    @PII(description = "Nome completo do titular")
    @Column(nullable = false)
    private String nome;

    @PII(description = "Endereço de e-mail do titular — identificador único")
    @Column(nullable = false, unique = true)
    private String email;

    @PII(description = "Telefone/WhatsApp do titular")
    private String telefone;

    // ── Vínculo com identidade Keycloak ──────────────────────────

    @PII(description = "Subject UUID do usuário no Keycloak — permite isolamento de dados")
    @Column(name = "keycloak_user_id", unique = true)
    private String keycloakUserId;

    // ── Consentimento (Art. 8º LGPD) ─────────────────────────────

    /**
     * Aceite dos Termos de Uso — base legal obrigatória para cadastro.
     * Deve ser {@code true} ao criar o cliente; nunca pode ser revertido para {@code false}.
     */
    @Column(name = "consentimento_termos_aceito", nullable = false)
    @Builder.Default
    private boolean consentimentoTermosAceito = false;

    @Column(name = "data_consentimento_termos")
    private LocalDateTime dataConsentimentoTermos;

    /** Opt-in para notificações transacionais (lembretes de agendamento). */
    @Column(name = "consentimento_notificacoes", nullable = false)
    @Builder.Default
    private boolean consentimentoNotificacoes = false;

    @Column(name = "data_consentimento_notificacoes")
    private LocalDateTime dataConsentimentoNotificacoes;

    /** Opt-in para comunicações de marketing — finalidade separada conforme Art. 8º. */
    @Column(name = "consentimento_marketing", nullable = false)
    @Builder.Default
    private boolean consentimentoMarketing = false;

    @Column(name = "data_consentimento_marketing")
    private LocalDateTime dataConsentimentoMarketing;

    // ── Anonimização / Direito ao Esquecimento (Art. 18 LGPD) ────

    @Column(nullable = false)
    @Builder.Default
    private boolean anonimizado = false;

    @Column(name = "data_anonimizacao")
    private LocalDateTime dataAnonimizacao;

    // ── Ciclo de vida ────────────────────────────────────────────

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    /**
     * Última vez que o titular realizou alguma ação no sistema.
     * Usado pelo job de expurgo para identificar contas inativas há mais de 2 anos.
     */
    @Column(name = "ultima_atividade")
    private LocalDateTime ultimaAtividade;

    @PrePersist
    protected void onCreate() {
        dataCriacao = LocalDateTime.now();
        ultimaAtividade = LocalDateTime.now();
    }
}
