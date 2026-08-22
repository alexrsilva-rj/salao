package com.salao.security.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Serviço responsável por persistir trilhas de auditoria de acesso a dados pessoais.
 * Executa em transação separada para garantir que o log seja gravado mesmo
 * em caso de rollback na transação principal.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(String acao, String entidade, String entidadeId) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String usuarioId = auth != null ? auth.getName() : "anonymous";
            String role = auth != null ? auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(a -> a.startsWith("ROLE_"))
                    .findFirst()
                    .orElse("UNKNOWN") : "UNKNOWN";

            String ipOrigem = resolveIpOrigem();

            AuditLog auditLog = AuditLog.builder()
                    .usuarioId(usuarioId)
                    .role(role)
                    .acao(acao)
                    .entidade(entidade)
                    .entidadeId(entidadeId)
                    .ipOrigem(ipOrigem)
                    .build();

            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.warn("[LGPD-Audit] Falha ao gravar trilha de auditoria: {}", e.getMessage());
        }
    }

    private String resolveIpOrigem() {
        try {
            var attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                String ip = attrs.getRequest().getHeader("X-Forwarded-For");
                return (ip != null && !ip.isBlank()) ? ip.split(",")[0].trim()
                        : attrs.getRequest().getRemoteAddr();
            }
        } catch (Exception ignored) { }
        return null;
    }
}
