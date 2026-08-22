package com.salao.agendamento.service;

import com.salao.agendamento.model.Agendamento;
import com.salao.agendamento.model.Profissional;
import com.salao.agendamento.model.Servico;
import com.salao.agendamento.repository.AgendamentoRepository;
import com.salao.agendamento.repository.ProfissionalRepository;
import com.salao.agendamento.repository.ServicoRepository;
import com.salao.cliente.model.Cliente;
import com.salao.cliente.service.ClienteService;
import lombok.RequiredArgsConstructor;
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

   @Transactional
   public Agendamento criarAgendamento(UUID clienteId, UUID profissionalId, UUID servicoId, LocalDateTime dataHoraInicio) {
       Cliente cliente = clienteService.buscarPorId(clienteId);
       Profissional profissional = profissionalRepository.findById(profissionalId)
               .orElseThrow(() -> new RuntimeException("Profissional não encontrado."));
       Servico servico = servicoRepository.findById(servicoId)
               .orElseThrow(() -> new RuntimeException("Serviço não encontrado."));

       LocalDateTime dataHoraFim = dataHoraInicio.plusMinutes(servico.getDuracaoMinutos());

       List<Agendamento> conflitos = agendamentoRepository.findConflitantes(profissionalId, dataHoraInicio, dataHoraFim);
       if (!conflitos.isEmpty()) {
           throw new IllegalStateException("O profissional já possui um agendamento conflitante neste horário.");
       }

       Agendamento agendamento = Agendamento.builder()
               .cliente(cliente)
               .profissional(profissional)
               .servico(servico)
               .dataHoraInicio(dataHoraInicio)
               .dataHoraFim(dataHoraFim)
               .status("PENDENTE")
               .build();

       return agendamentoRepository.save(agendamento);
   }

   @Transactional(readOnly = true)
   public List<Agendamento> listarAgendamentos() {
       return agendamentoRepository.findAll();
   }
}
