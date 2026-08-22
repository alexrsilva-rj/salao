package com.salao.common.audit;

import java.lang.annotation.*;

/**
 * Marca um método de serviço para geração automática de trilha de auditoria.
 * Interceptado por {@code AuditAspect} no módulo salao-security.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Auditavel {
    /** Ação realizada (ex: LISTAR_CLIENTES, ANONIMIZAR_CLIENTE). */
    String acao();
    /** Nome da entidade de negócio afetada (ex: Cliente, Agendamento). */
    String entidade();
}
