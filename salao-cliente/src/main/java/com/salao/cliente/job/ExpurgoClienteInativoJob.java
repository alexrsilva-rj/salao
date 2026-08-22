package com.salao.cliente.job;

import com.salao.cliente.model.Cliente;
import com.salao.cliente.repository.ClienteRepository;
import com.salao.cliente.service.ClienteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Job de expurgo automático conforme LGPD.
 * Executa toda madrugada de domingo às 03:00 e anonimiza clientes
 * que não possuem nenhuma atividade há mais de 2 anos,
 * evitando o acúmulo desnecessário de dados pessoais ("bancos de dados zumbis").
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ExpurgoClienteInativoJob {

    private static final int ANOS_INATIVIDADE = 2;

    private final ClienteRepository clienteRepository;
    private final ClienteService clienteService;

    @Scheduled(cron = "0 0 3 * * SUN")
    @Transactional
    public void executar() {
        LocalDateTime limiteInatividade = LocalDateTime.now().minusYears(ANOS_INATIVIDADE);
        List<Cliente> inativos = clienteRepository
                .findByUltimaAtividadeBeforeAndAnonimizadoFalse(limiteInatividade);

        log.info("[LGPD-Expurgo] Iniciando. {} clientes inativos há mais de {} anos encontrados.",
                inativos.size(), ANOS_INATIVIDADE);

        int anonimizados = 0;
        for (Cliente cliente : inativos) {
            try {
                clienteService.anonimizarCliente(cliente.getId());
                anonimizados++;
                log.info("[LGPD-Expurgo] Cliente {} anonimizado (última atividade: {}).",
                        cliente.getId(), cliente.getUltimaAtividade());
            } catch (Exception e) {
                log.error("[LGPD-Expurgo] Falha ao anonimizar cliente {}: {}",
                        cliente.getId(), e.getMessage());
            }
        }

        log.info("[LGPD-Expurgo] Concluído. {}/{} clientes anonimizados.", anonimizados, inativos.size());
    }
}
