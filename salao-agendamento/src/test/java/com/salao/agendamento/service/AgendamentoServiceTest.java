package com.salao.agendamento.service;

import com.salao.agendamento.model.Agendamento;
import com.salao.agendamento.model.Profissional;
import com.salao.agendamento.model.Servico;
import com.salao.agendamento.repository.AgendamentoRepository;
import com.salao.agendamento.repository.ProfissionalRepository;
import com.salao.agendamento.repository.ServicoRepository;
import com.salao.cliente.model.Cliente;
import com.salao.cliente.service.ClienteService;
import com.salao.common.exception.RegraNegocioException;
import com.salao.common.security.UserContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgendamentoServiceTest {

    @Mock
    private AgendamentoRepository agendamentoRepository;

    @Mock
    private ProfissionalRepository profissionalRepository;

    @Mock
    private ServicoRepository servicoRepository;

    @Mock
    private ClienteService clienteService;

    @InjectMocks
    private AgendamentoService agendamentoService;

    private UUID clienteId;
    private UUID profId;
    private UUID servId;
    private Cliente cliente;
    private Profissional prof;
    private Servico serv;
    private UserContext userContext;

    @BeforeEach
    void setUp() {
        clienteId = UUID.randomUUID();
        profId = UUID.randomUUID();
        servId = UUID.randomUUID();

        cliente = Cliente.builder().id(clienteId).nome("João").email("joao@test.com").anonimizado(false).build();
        prof = Profissional.builder().id(profId).nome("Ana").ativo(true).build();
        serv = Servico.builder().id(servId).nome("Corte").preco(new BigDecimal("50.00")).duracaoMinutos(30).ativo(true).build();

        userContext = UserContext.builder()
                .keycloakUserId("kc-user-1")
                .role("ROLE_RECEPTION")
                .build();
    }

    @Test
    void shouldCreateAppointmentSuccessfully() {
        LocalDateTime inicio = LocalDateTime.now().plusDays(1);
        when(clienteService.buscarPorId(clienteId)).thenReturn(cliente);
        when(profissionalRepository.findById(profId)).thenReturn(Optional.of(prof));
        when(servicoRepository.findById(servId)).thenReturn(Optional.of(serv));
        when(agendamentoRepository.findConflitantes(any(), any(), any())).thenReturn(Collections.emptyList());
        when(agendamentoRepository.save(any(Agendamento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Agendamento resultado = agendamentoService.criarAgendamento(clienteId, profId, servId, inicio, userContext);

        assertNotNull(resultado);
        assertEquals("PENDENTE", resultado.getStatus());
        verify(agendamentoRepository).save(any(Agendamento.class));
    }

    @Test
    void shouldRejectAnonymizedCustomer() {
        cliente.setAnonimizado(true);
        LocalDateTime inicio = LocalDateTime.now().plusDays(1);
        when(clienteService.buscarPorId(clienteId)).thenReturn(cliente);

        assertThrows(RegraNegocioException.class, () ->
                agendamentoService.criarAgendamento(clienteId, profId, servId, inicio, userContext));
    }

    @Test
    void shouldRejectInactiveProfessional() {
        prof.setAtivo(false);
        LocalDateTime inicio = LocalDateTime.now().plusDays(1);
        when(clienteService.buscarPorId(clienteId)).thenReturn(cliente);
        when(profissionalRepository.findById(profId)).thenReturn(Optional.of(prof));

        assertThrows(RegraNegocioException.class, () ->
                agendamentoService.criarAgendamento(clienteId, profId, servId, inicio, userContext));
    }

    @Test
    void shouldRejectInactiveService() {
        serv.setAtivo(false);
        LocalDateTime inicio = LocalDateTime.now().plusDays(1);
        when(clienteService.buscarPorId(clienteId)).thenReturn(cliente);
        when(profissionalRepository.findById(profId)).thenReturn(Optional.of(prof));
        when(servicoRepository.findById(servId)).thenReturn(Optional.of(serv));

        assertThrows(RegraNegocioException.class, () ->
                agendamentoService.criarAgendamento(clienteId, profId, servId, inicio, userContext));
    }

    @Test
    void shouldRejectConflictingTime() {
        LocalDateTime inicio = LocalDateTime.now().plusDays(1);
        when(clienteService.buscarPorId(clienteId)).thenReturn(cliente);
        when(profissionalRepository.findById(profId)).thenReturn(Optional.of(prof));
        when(servicoRepository.findById(servId)).thenReturn(Optional.of(serv));
        when(agendamentoRepository.findConflitantes(any(), any(), any()))
                .thenReturn(List.of(Agendamento.builder().id(UUID.randomUUID()).build()));

        assertThrows(RegraNegocioException.class, () ->
                agendamentoService.criarAgendamento(clienteId, profId, servId, inicio, userContext));
    }
}
