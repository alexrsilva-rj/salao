package com.salao.security.claims;

import com.salao.common.security.UserContext;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Extrai o contexto do usuário autenticado a partir do SecurityContext.
 * Suporta autenticação via JWT (Keycloak) e via API Token (filtro estático).
 *
 * <p><strong>Fail-Closed:</strong> tokens sem role reconhecida lançam
 * {@link AccessDeniedException} em vez de conceder acesso padrão (Issue 09).</p>
 */
@Component
public class JwtClaimsExtractor {

    /**
     * Extrai o {@link UserContext} do token JWT presente no SecurityContext.
     * Em caso de autenticação via API Token, atribui role ROLE_RECEPTION por padrão.
     *
     * @throws AccessDeniedException se o token JWT não contiver nenhuma role reconhecida
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

        // Autenticação via x-api-token → trata como acesso administrativo de recepção
        return UserContext.builder()
                .keycloakUserId("api-token-user")
                .role("ROLE_RECEPTION")
                .build();
    }

    /**
     * Determina a role principal por ordem de precedência:
     * ROLE_RECEPTION > ROLE_PROFESSIONAL > ROLE_CUSTOMER
     *
     * <p><strong>Fail-Closed:</strong> lança {@link AccessDeniedException} se nenhuma
     * role reconhecida for encontrada no token. Evita que tokens malformados ou de
     * realms incorretos recebam acesso não autorizado (anteriormente concedia ROLE_CUSTOMER).</p>
     *
     * @throws AccessDeniedException se o token não contiver role reconhecida
     */
    private String extractPrimaryRole(JwtAuthenticationToken jwtAuth) {
        // Tenta extrair de realm_access.roles (padrão Keycloak)
        Map<String, Object> realmAccess = jwtAuth.getToken().getClaim("realm_access");
        if (realmAccess != null && realmAccess.get("roles") instanceof List<?> roles) {
            if (containsRole(roles, "RECEPTION")) return "ROLE_RECEPTION";
            if (containsRole(roles, "PROFESSIONAL")) return "ROLE_PROFESSIONAL";
            if (containsRole(roles, "CUSTOMER")) return "ROLE_CUSTOMER";
        }

        // Fallback: verifica authorities já mapeadas pelo Spring Security (sem default fail-open)
        Optional<String> mappedRole = jwtAuth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_") &&
                        (a.equals("ROLE_RECEPTION") || a.equals("ROLE_PROFESSIONAL") || a.equals("ROLE_CUSTOMER")))
                .findFirst();

        return mappedRole.orElseThrow(() ->
                new AccessDeniedException(
                        "Token JWT não contém role reconhecida (RECEPTION, PROFESSIONAL ou CUSTOMER). " +
                        "Verifique o mapeamento de roles no realm Keycloak."));
    }

    private boolean containsRole(List<?> roles, String targetRole) {
        return roles.stream()
                .anyMatch(r -> r instanceof String s &&
                        s.equalsIgnoreCase(targetRole));
    }
}
