package com.salao.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filtro de autenticação via header {@code x-api-token}.
 * O token é configurado via variável de ambiente {@code API_TOKEN_SECRET}
 * e injetado pelo {@link com.salao.security.config.SecurityConfig}.
 *
 * <p>Autenticações bem-sucedidas recebem a authority {@code ROLE_RECEPTION},
 * concedendo acesso administrativo completo. Nunca use o token padrão em produção.</p>
 */
public class ApiTokenAuthenticationFilter extends OncePerRequestFilter {

    private static final String API_TOKEN_HEADER = "x-api-token";

    private final String validApiToken;

    public ApiTokenAuthenticationFilter(String validApiToken) {
        this.validApiToken = validApiToken;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String apiToken = request.getHeader(API_TOKEN_HEADER);

        if (apiToken != null && !apiToken.isBlank()) {
            if (validApiToken.equals(apiToken)) {
                List<SimpleGrantedAuthority> authorities = List.of(
                        new SimpleGrantedAuthority("ROLE_RECEPTION"),
                        new SimpleGrantedAuthority("SCOPE_agendamento:escrever")
                );
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken("ApiTokenUser", null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token de API inválido.");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
