package com.salao.common.annotation;

import java.lang.annotation.*;

/**
 * Marca um campo como dado pessoal (PII - Personally Identifiable Information)
 * conforme a LGPD (Lei nº 13.709/2018).
 * Usada para rastreabilidade, auditoria e geração de relatórios de conformidade.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface PII {
    /** Descrição do dado pessoal e sua finalidade de tratamento. */
    String description() default "";
}
