package com.salao.security.claims;

import com.salao.common.security.UserContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Extrai o contexto do usuário autenticado a partir do SecurityContext.
 * Suporta autenticação via JWT (Keycloak) e via API Token (filtro estático).
 */
@Component
public class JwtClaimsExtractor {

    /**
     * Extrai o {@link UserContext} do token JWT presente no SecurityContext.
     * Em caso de autenticação via API Token, atribui role ROLE_RECEPTION por padrão.
     */
    public UserContext extract() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            String keycloakUserId = jwtAuth.getToken().getSubject();
            String role = extractPrimaryRole(jwtAuth);
            return UserContext.builder()
                    .keycloakUserId(keycloakUserId)
                    .role(role)
                    .build();
        }

        // Autenticação via x-api-token → trata como acesso administrativo
        return UserContext.builder()
                .keycloakUserId("api-token-user")
                .role("ROLE_RECEPTION")
                .build();
    }

    /**
     * Determina a role principal por ordem de precedência:
     * ROLE_RECEPTION > ROLE_PROFESSIONAL > ROLE_CUSTOMER
     */
    private String extractPrimaryRole(JwtAuthenticationToken jwtAuth) {
        // Tenta extrair de realm_access.roles (padrão Keycloak)
        Map<String, Object> realmAccess = jwtAuth.getToken().getClaim("realm_access");
        if (realmAccess != null && realmAccess.get("roles") instanceof List<?> roles) {
            if (containsRole(roles, "ROLE_RECEPTION")) return "ROLE_RECEPTION";
            if (containsRole(roles, "ROLE_PROFESSIONAL")) return "ROLE_PROFESSIONAL";
            if (containsRole(roles, "ROLE_CUSTOMER")) return "ROLE_CUSTOMER";
        }

        // Fallback: verifica authorities já mapeadas pelo Spring Security
        return jwtAuth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .findFirst()
                .orElse("ROLE_CUSTOMER");
    }

    private boolean containsRole(List<?> roles, String targetRole) {
        return roles.stream()
                .anyMatch(r -> r instanceof String s &&
                        (s.equalsIgnoreCase(targetRole) ||
                         s.equalsIgnoreCase(targetRole.replace("ROLE_", ""))));
    }
}
