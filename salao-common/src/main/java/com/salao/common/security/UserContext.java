package com.salao.common.security;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

/**
 * Representa o contexto do usuário autenticado extraído do JWT ou do filtro de API Token.
 * Encapsula identidade, role e vínculo com o registro no banco de dados.
 */
@Value
@Builder
public class UserContext {

    /** Subject (UUID) do usuário no Keycloak. */
    String keycloakUserId;

    /** Role principal do usuário (ROLE_RECEPTION, ROLE_PROFESSIONAL, ROLE_CUSTOMER). */
    String role;

    /**
     * UUID do cliente no banco de dados — populado somente quando role = ROLE_CUSTOMER
     * e o vínculo já foi estabelecido via keycloakUserId.
     */
    UUID customerDbId;

    public boolean isReception() {
        return "ROLE_RECEPTION".equals(role);
    }

    public boolean isCustomer() {
        return "ROLE_CUSTOMER".equals(role);
    }

    public boolean isProfessional() {
        return "ROLE_PROFESSIONAL".equals(role);
    }
}
