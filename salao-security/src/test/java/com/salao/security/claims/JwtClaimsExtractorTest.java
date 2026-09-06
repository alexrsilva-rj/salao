package com.salao.security.claims;

import com.salao.common.security.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtClaimsExtractorTest {

    private final JwtClaimsExtractor extractor = new JwtClaimsExtractor();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldThrowAccessDeniedWhenNotAuthenticated() {
        SecurityContextHolder.clearContext();
        assertThrows(AccessDeniedException.class, extractor::extract);
    }

    @Test
    void shouldThrowAccessDeniedWhenAnonymousUser() {
        AnonymousAuthenticationToken anon = new AnonymousAuthenticationToken(
                "key", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
        SecurityContextHolder.getContext().setAuthentication(anon);

        assertThrows(AccessDeniedException.class, extractor::extract);
    }

    @Test
    void shouldExtractFromJwtToken() {
        Jwt jwt = Jwt.withTokenValue("mock-jwt-token")
                .header("alg", "RS256")
                .claim("sub", "user-uuid-123")
                .claims(claims -> claims.put("realm_access", Map.of("roles", List.of("CUSTOMER"))))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        JwtAuthenticationToken token = new JwtAuthenticationToken(
                jwt, List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
        SecurityContextHolder.getContext().setAuthentication(token);

        UserContext ctx = extractor.extract();
        assertEquals("user-uuid-123", ctx.getKeycloakUserId());
        assertEquals("ROLE_CUSTOMER", ctx.getRole());
        assertTrue(ctx.isCustomer());
    }

    @Test
    void shouldExtractFromApiTokenAuth() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "ApiTokenUser", null, List.of(new SimpleGrantedAuthority("ROLE_RECEPTION")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        UserContext ctx = extractor.extract();
        assertEquals("api-token-user", ctx.getKeycloakUserId());
        assertEquals("ROLE_RECEPTION", ctx.getRole());
        assertTrue(ctx.isReception());
    }
}
