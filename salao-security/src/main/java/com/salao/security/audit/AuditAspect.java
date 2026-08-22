package com.salao.security.audit;

import com.salao.common.audit.Auditavel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Aspecto AOP que intercepta métodos anotados com {@link Auditavel}
 * e delega o registro da trilha ao {@link AuditService}.
 *
 * <p>A auditoria é gravada APÓS a execução bem-sucedida do método.
 * Falhas na auditoria não propagam exceções para não interromper o fluxo principal.</p>
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

    private final AuditService auditService;

    @Around("@annotation(auditavel)")
    public Object auditar(ProceedingJoinPoint pjp, Auditavel auditavel) throws Throwable {
        Object result = pjp.proceed();

        try {
            String entidadeId = extractId(result);
            auditService.registrar(auditavel.acao(), auditavel.entidade(), entidadeId);
        } catch (Exception e) {
            log.warn("[LGPD-Audit] Falha no aspecto de auditoria: {}", e.getMessage());
        }

        return result;
    }

    /**
     * Tenta extrair o ID do objeto retornado pelo método auditado.
     * Funciona para entidades e DTOs que possuam método {@code getId()}.
     */
    private String extractId(Object result) {
        if (result == null) return null;
        try {
            Method getId = result.getClass().getMethod("getId");
            Object id = getId.invoke(result);
            return id != null ? id.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
