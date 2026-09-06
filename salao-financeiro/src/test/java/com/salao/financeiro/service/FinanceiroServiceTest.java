package com.salao.financeiro.service;

import com.salao.agendamento.model.Agendamento;
import com.salao.agendamento.model.Profissional;
import com.salao.agendamento.model.Servico;
import com.salao.agendamento.repository.AgendamentoRepository;
import com.salao.common.exception.PagamentoDuplicadoException;
import com.salao.common.exception.RegraNegocioException;
import com.salao.financeiro.model.Financeiro;
import com.salao.financeiro.model.FormaPagamentoEnum;
import com.salao.financeiro.repository.FinanceiroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinanceiroServiceTest {

    @Mock
    private FinanceiroRepository financeiroRepository;

    @Mock
    private AgendamentoRepository agendamentoRepository;

    @InjectMocks
    private FinanceiroService financeiroService;

    private UUID agendamentoId;
    private Agendamento agendamento;

    @BeforeEach
    void setUp() {
        agendamentoId = UUID.randomUUID();
        Profissional prof = Profissional.builder().id(UUID.randomUUID()).nome("Maria").build();
        Servico serv = Servico.builder().id(UUID.randomUUID()).nome("Corte").preco(new BigDecimal("100.00")).build();
        agendamento = Agendamento.builder()
                .id(agendamentoId)
                .profissional(prof)
                .servico(serv)
                .status("PENDENTE")
                .build();
    }

    @Test
    void shouldRegisterPaymentSuccessfully() {
        when(agendamentoRepository.findById(agendamentoId)).thenReturn(Optional.of(agendamento));
        when(financeiroRepository.existsByAgendamentoId(agendamentoId)).thenReturn(false);
        when(financeiroRepository.save(any(Financeiro.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Financeiro financeiro = financeiroService.registrarPagamento(
                agendamentoId, new BigDecimal("20.00"), FormaPagamentoEnum.PIX);

        assertNotNull(financeiro);
        assertEquals(new BigDecimal("100.00"), financeiro.getValorTotal());
        assertEquals(new BigDecimal("20.00"), financeiro.getValorComissao());
        assertEquals(new BigDecimal("80.00"), financeiro.getValorLiquidoSalao());
        assertEquals("PAGO", agendamento.getStatus());
        verify(agendamentoRepository).save(agendamento);
    }

    @Test
    void shouldRejectPaymentForCanceledAppointment() {
        agendamento.setStatus("CANCELADO");
        when(agendamentoRepository.findById(agendamentoId)).thenReturn(Optional.of(agendamento));

        assertThrows(RegraNegocioException.class, () ->
                financeiroService.registrarPagamento(agendamentoId, new BigDecimal("20.00"), FormaPagamentoEnum.PIX));
        verify(financeiroRepository, never()).save(any());
    }

    @Test
    void shouldRejectDuplicatePayment() {
        when(agendamentoRepository.findById(agendamentoId)).thenReturn(Optional.of(agendamento));
        when(financeiroRepository.existsByAgendamentoId(agendamentoId)).thenReturn(true);

        assertThrows(PagamentoDuplicadoException.class, () ->
                financeiroService.registrarPagamento(agendamentoId, new BigDecimal("20.00"), FormaPagamentoEnum.PIX));
        verify(financeiroRepository, never()).save(any());
    }
}
