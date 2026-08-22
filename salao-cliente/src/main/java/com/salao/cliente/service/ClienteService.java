package com.salao.cliente.service;

import com.salao.cliente.dto.ClienteUpdateDTO;
import com.salao.cliente.dto.ConsentimentoDTO;
import com.salao.cliente.dto.DadosPessoaisDTO;
import com.salao.cliente.model.Cliente;
import com.salao.cliente.repository.ClienteRepository;
import com.salao.common.security.UserContext;
import com.salao.common.audit.Auditavel;
import lombok.RequiredArgsConstructor;
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
     * Cria um novo cliente.
     * O aceite dos termos de uso é obrigatório (Art. 8º LGPD — base legal).
     */
    @Transactional
    public Cliente criarCliente(Cliente cliente) {
        if (!cliente.isConsentimentoTermosAceito()) {
            throw new IllegalArgumentException(
                    "O aceite dos Termos de Uso é obrigatório para o cadastro (LGPD Art. 8º).");
        }
        if (clienteRepository.findByEmail(cliente.getEmail()).isPresent()) {
            throw new IllegalArgumentException("E-mail já cadastrado.");
        }

        // Registra datas de consentimento no momento do aceite
        cliente.setDataConsentimentoTermos(LocalDateTime.now());
        if (cliente.isConsentimentoNotificacoes()) {
            cliente.setDataConsentimentoNotificacoes(LocalDateTime.now());
        }
        if (cliente.isConsentimentoMarketing()) {
            cliente.setDataConsentimentoMarketing(LocalDateTime.now());
        }

        return clienteRepository.save(cliente);
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
