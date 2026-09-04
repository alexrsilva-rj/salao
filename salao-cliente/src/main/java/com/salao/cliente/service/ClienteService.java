package com.salao.cliente.service;

import com.salao.cliente.dto.ClienteCreateDTO;
import com.salao.cliente.dto.ClienteUpdateDTO;
import com.salao.cliente.dto.ConsentimentoDTO;
import com.salao.cliente.dto.DadosPessoaisDTO;
import com.salao.cliente.model.Cliente;
import com.salao.cliente.repository.ClienteRepository;
import com.salao.common.security.UserContext;
import com.salao.common.audit.Auditavel;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;

    // ── Criação ──────────────────────────────────────────────────

    /**
     * Cria um novo cliente a partir de um DTO de entrada seguro.
     *
     * <p>O {@code keycloakUserId} é extraído <strong>exclusivamente</strong> do
     * {@link UserContext} (token JWT autenticado), nunca do payload da request.
     * Isso elimina o risco de Mass Assignment (CWE-915) e sequestro de vínculo de
     * identidade em violação aos Arts. 8º e 9º da LGPD.</p>
     *
     * @param dto DTO validado com apenas os campos permitidos para input
     * @param ctx contexto do usuário autenticado — fonte do keycloakUserId
     */
    @Transactional
    public Cliente criarCliente(ClienteCreateDTO dto, UserContext ctx) {
        if (!Boolean.TRUE.equals(dto.getConsentimentoTermosAceito())) {
            throw new IllegalArgumentException(
                    "O aceite dos Termos de Uso é obrigatório para o cadastro (LGPD Art. 8º).");
        }
        if (clienteRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("E-mail já cadastrado.");
        }

        LocalDateTime agora = LocalDateTime.now();

        Cliente cliente = Cliente.builder()
                .nome(dto.getNome())
                .email(dto.getEmail())
                .telefone(dto.getTelefone())
                // keycloakUserId SEMPRE vem do token JWT, nunca do payload da request
                .keycloakUserId(ctx.getKeycloakUserId())
                .consentimentoTermosAceito(true)
                .dataConsentimentoTermos(agora)
                .consentimentoNotificacoes(dto.isConsentimentoNotificacoes())
                .dataConsentimentoNotificacoes(dto.isConsentimentoNotificacoes() ? agora : null)
                .consentimentoMarketing(dto.isConsentimentoMarketing())
                .dataConsentimentoMarketing(dto.isConsentimentoMarketing() ? agora : null)
                .build();

        return clienteRepository.save(cliente);
    }

    // ── Validação de Ownership (IDOR) ────────────────────────────

    /**
     * Valida que o usuário autenticado tem permissão para acessar o cliente indicado.
     *
     * <p>Usuários com {@code ROLE_CUSTOMER} só podem acessar o próprio registro.
     * Usuários com {@code ROLE_RECEPTION} ou {@code ROLE_PROFESSIONAL} podem
     * acessar qualquer registro.</p>
     *
     * @param cliente entidade a ser verificada
     * @param ctx contexto do usuário autenticado
     * @throws AccessDeniedException se o cliente CUSTOMER tentar acessar registro de terceiro
     */
    public void validarOwnership(Cliente cliente, UserContext ctx) {
        if (ctx.isCustomer() &&
                !ctx.getKeycloakUserId().equals(cliente.getKeycloakUserId())) {
            throw new AccessDeniedException(
                    "Acesso negado: você não tem permissão para acessar dados de outro cliente.");
        }
    }

    // ── Consultas ────────────────────────────────────────────────

    /**
     * Lista clientes respeitando o isolamento por perfil:
     * - ROLE_RECEPTION: retorna todos os clientes.
     * - ROLE_CUSTOMER: retorna apenas o próprio registro.
     */
    @Transactional(readOnly = true)
    @Auditavel(acao = "LISTAR_CLIENTES", entidade = "Cliente")
    public List<Cliente> listarClientes(UserContext userContext) {
        if (userContext.isCustomer()) {
            return clienteRepository.findByKeycloakUserId(userContext.getKeycloakUserId())
                    .map(List::of)
                    .orElse(List.of());
        }
        return clienteRepository.findAll();
    }

    @Transactional(readOnly = true)
    @Auditavel(acao = "BUSCAR_CLIENTE", entidade = "Cliente")
    public Cliente buscarPorId(UUID id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado."));
    }

    @Transactional(readOnly = true)
    public Cliente buscarPorKeycloakId(String keycloakUserId) {
        return clienteRepository.findByKeycloakUserId(keycloakUserId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado para este usuário."));
    }

    // ── Retificação (Art. 18, IV LGPD) ──────────────────────────

    @Transactional
    @Auditavel(acao = "ATUALIZAR_CLIENTE", entidade = "Cliente")
    public Cliente atualizarCliente(UUID id, ClienteUpdateDTO dto) {
        Cliente cliente = buscarPorId(id);
        if (cliente.isAnonimizado()) {
            throw new IllegalStateException("Não é possível atualizar um cliente anonimizado.");
        }
        if (dto.getNome() != null && !dto.getNome().isBlank()) {
            cliente.setNome(dto.getNome());
        }
        if (dto.getTelefone() != null) {
            cliente.setTelefone(dto.getTelefone());
        }
        return clienteRepository.save(cliente);
    }

    // ── Consentimento (Art. 8º LGPD) ────────────────────────────

    @Transactional(readOnly = true)
    public ConsentimentoDTO buscarConsentimento(UUID id) {
        Cliente cliente = buscarPorId(id);
        ConsentimentoDTO dto = new ConsentimentoDTO();
        dto.setConsentimentoTermosAceito(cliente.isConsentimentoTermosAceito());
        dto.setDataConsentimentoTermos(cliente.getDataConsentimentoTermos());
        dto.setConsentimentoNotificacoes(cliente.isConsentimentoNotificacoes());
        dto.setDataConsentimentoNotificacoes(cliente.getDataConsentimentoNotificacoes());
        dto.setConsentimentoMarketing(cliente.isConsentimentoMarketing());
        dto.setDataConsentimentoMarketing(cliente.getDataConsentimentoMarketing());
        return dto;
    }

    @Transactional
    @Auditavel(acao = "ATUALIZAR_CONSENTIMENTO", entidade = "Cliente")
    public ConsentimentoDTO atualizarConsentimento(UUID id, ConsentimentoDTO dto) {
        Cliente cliente = buscarPorId(id);
        if (cliente.isAnonimizado()) {
            throw new IllegalStateException("Não é possível alterar consentimento de cliente anonimizado.");
        }

        // Notificações
        if (dto.isConsentimentoNotificacoes() != cliente.isConsentimentoNotificacoes()) {
            cliente.setConsentimentoNotificacoes(dto.isConsentimentoNotificacoes());
            cliente.setDataConsentimentoNotificacoes(dto.isConsentimentoNotificacoes() ? LocalDateTime.now() : null);
        }

        // Marketing
        if (dto.isConsentimentoMarketing() != cliente.isConsentimentoMarketing()) {
            cliente.setConsentimentoMarketing(dto.isConsentimentoMarketing());
            cliente.setDataConsentimentoMarketing(dto.isConsentimentoMarketing() ? LocalDateTime.now() : null);
        }

        clienteRepository.save(cliente);
        return buscarConsentimento(id);
    }

    // ── Direito ao Esquecimento (Art. 18, VI LGPD) ──────────────

    /**
     * Anonimiza os dados pessoais do cliente.
     * O registro permanece no banco para preservar o histórico financeiro
     * (obrigação legal — Art. 10º LGPD c/c legislação fiscal).
     * Os dados de PII são sobrescritos de forma irreversível.
     */
    @Transactional
    @Auditavel(acao = "ANONIMIZAR_CLIENTE", entidade = "Cliente")
    public Cliente anonimizarCliente(UUID id) {
        Cliente cliente = buscarPorId(id);
        if (cliente.isAnonimizado()) {
            return cliente; // Idempotente
        }

        cliente.setNome("Cliente Anonimizado");
        cliente.setEmail("anonimizado-" + cliente.getId() + "@removido.local");
        cliente.setTelefone(null);
        cliente.setKeycloakUserId(null);
        cliente.setConsentimentoMarketing(false);
        cliente.setDataConsentimentoMarketing(null);
        cliente.setConsentimentoNotificacoes(false);
        cliente.setDataConsentimentoNotificacoes(null);
        cliente.setAnonimizado(true);
        cliente.setDataAnonimizacao(LocalDateTime.now());

        return clienteRepository.save(cliente);
    }

    // ── Portabilidade de Dados (Art. 18, V LGPD) ─────────────────

    @Transactional(readOnly = true)
    @Auditavel(acao = "EXPORTAR_DADOS_PESSOAIS", entidade = "Cliente")
    public DadosPessoaisDTO exportarDadosPessoais(UUID id) {
        return DadosPessoaisDTO.from(buscarPorId(id));
    }

    // ── Uso interno ──────────────────────────────────────────────

    /**
     * Atualiza o campo {@code ultimaAtividade} do cliente.
     * Chamado pelo módulo de agendamento ao criar um novo agendamento.
     */
    @Transactional
    public void registrarAtividade(UUID clienteId) {
        clienteRepository.findById(clienteId).ifPresent(c -> {
            c.setUltimaAtividade(LocalDateTime.now());
            clienteRepository.save(c);
        });
    }
}
