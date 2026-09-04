package com.salao.agendamento.service;

import com.salao.agendamento.model.Agendamento;
import com.salao.agendamento.model.Profissional;
import com.salao.agendamento.model.Servico;
import com.salao.agendamento.repository.AgendamentoRepository;
import com.salao.agendamento.repository.ProfissionalRepository;
import com.salao.agendamento.repository.ServicoRepository;
import com.salao.cliente.model.Cliente;
import com.salao.cliente.service.ClienteService;
import com.salao.common.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AgendamentoService {

    private final AgendamentoRepository agendamentoRepository;
    private final ProfissionalRepository profissionalRepository;
    private final ServicoRepository servicoRepository;
    private final ClienteService clienteService;

    /**
     * Cria um novo agendamento com validação de ownership para clientes (Issue 11 — CWE-639).
     *
     * <p><strong>Regras de segurança:</strong>
     * <ul>
     *   <li>{@code ROLE_CUSTOMER}: o {@code clienteId} é derivado do token JWT via
     *       {@code keycloakUserId} — o valor enviado na request é ignorado para evitar
     *       que um cliente crie agendamentos em nome de outro.</li>
     *   <li>{@code ROLE_RECEPTION}: pode especificar qualquer {@code clienteId}.</li>
     *   <li>{@code ROLE_PROFESSIONAL}: pode especificar qualquer {@code clienteId}.</li>
     * </ul></p>
     *
     * @param clienteIdRequest  ID do cliente enviado na request (ignorado se role=CUSTOMER)
     * @param profissionalId    ID do profissional
     * @param servicoId         ID do serviço
     * @param dataHoraInicio    Data/hora de início do agendamento
     * @param userContext       Contexto do usuário autenticado
     * @throws AccessDeniedException se CUSTOMER tentar criar agendamento para outro cliente
     */
    @Transactional
    public Agendamento criarAgendamento(UUID clienteIdRequest,
                                        UUID profissionalId,
                                        UUID servicoId,
                                        LocalDateTime dataHoraInicio,
                                        UserContext userContext) {

        // Issue 11: se o usuário for CUSTOMER, deriva o clienteId do token
        UUID clienteId = resolverClienteId(clienteIdRequest, userContext);

        Cliente cliente = clienteService.buscarPorId(clienteId);
        Profissional profissional = profissionalRepository.findById(profissionalId)
                .orElseThrow(() -> new RuntimeException("Profissional não encontrado."));
        Servico servico = servicoRepository.findById(servicoId)
                .orElseThrow(() -> new RuntimeException("Serviço não encontrado."));

        LocalDateTime dataHoraFim = dataHoraInicio.plusMinutes(servico.getDuracaoMinutos());

        List<Agendamento> conflitos = agendamentoRepository.findConflitantes(
                profissionalId, dataHoraInicio, dataHoraFim);
        if (!conflitos.isEmpty()) {
            throw new IllegalStateException(
                    "O profissional já possui um agendamento conflitante neste horário.");
        }

        Agendamento agendamento = Agendamento.builder()
                .cliente(cliente)
                .profissional(profissional)
                .servico(servico)
                .dataHoraInicio(dataHoraInicio)
                .dataHoraFim(dataHoraFim)
                .status("PENDENTE")
                .build();

        Agendamento salvo = agendamentoRepository.save(agendamento);

        // Registra atividade do titular para o job de expurgo LGPD
        clienteService.registrarAtividade(clienteId);

        return salvo;
    }

    /**
     * Resolve o clienteId efetivo com base na role do usuário.
     *
     * <ul>
     *   <li>CUSTOMER: ignora o ID da request e busca pelo keycloakUserId do token</li>
     *   <li>RECEPTION/PROFESSIONAL: usa o ID enviado na request (pode agir por terceiros)</li>
     * </ul>
     */
    private UUID resolverClienteId(UUID clienteIdRequest, UserContext userContext) {
        if (userContext.isCustomer()) {
            // Busca o registro do cliente pelo keycloakUserId do token — NUNCA usa o ID da request
            Cliente clienteAutenticado = clienteService.buscarPorKeycloakId(userContext.getKeycloakUserId());
            return clienteAutenticado.getId();
        }
        // RECEPTION e PROFESSIONAL podem criar agendamentos para qualquer cliente
        if (clienteIdRequest == null) {
            throw new IllegalArgumentException("O clienteId é obrigatório para este perfil.");
        }
        return clienteIdRequest;
    }

    /**
     * Lista agendamentos com isolamento por perfil:
     * - ROLE_CUSTOMER: apenas os próprios agendamentos
     * - ROLE_RECEPTION / ROLE_PROFESSIONAL: todos
     */
    @Transactional(readOnly = true)
    public List<Agendamento> listarAgendamentos(UserContext userContext) {
        if (userContext.isCustomer()) {
            return agendamentoRepository.findByClienteKeycloakUserId(
                    userContext.getKeycloakUserId());
        }
        return agendamentoRepository.findAll();
    }
}
