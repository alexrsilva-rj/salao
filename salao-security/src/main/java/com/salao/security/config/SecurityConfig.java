package com.salao.security.config;

import com.salao.security.filter.ApiTokenAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

    @Value("${api.token.secret}")
    private String apiTokenSecret;

    @Bean
    public ApiTokenAuthenticationFilter apiTokenAuthenticationFilter() {
        return new ApiTokenAuthenticationFilter(apiTokenSecret);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(apiTokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                // Relatórios e financeiro: somente RECEPTION
                .requestMatchers("/api/relatorios/**").hasRole("RECEPTION")
                .requestMatchers("/api/financeiro/**").hasRole("RECEPTION")
                // Catálogo: qualquer autenticado
                .requestMatchers(HttpMethod.GET, "/api/catalogo/**").authenticated()
                // Clientes: RECEPTION e CUSTOMER (isolamento feito na camada de serviço)
                .requestMatchers("/api/clientes/**").hasAnyRole("RECEPTION", "CUSTOMER")
                // Agendamentos: todos os perfis autenticados
                .requestMatchers("/api/agendamentos/**").hasAnyRole("RECEPTION", "PROFESSIONAL", "CUSTOMER")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
            );

        return http.build();
    }

    /**
     * Converte claims do JWT Keycloak em authorities Spring Security.
     * Lê roles de realm_access.roles e scopes do campo scope.
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            List<GrantedAuthority> authorities = new ArrayList<>();

            // Extrai roles do realm_access (padrão Keycloak)
            Object realmAccess = jwt.getClaim("realm_access");
            if (realmAccess instanceof Map<?, ?> realmMap) {
                Object rolesClaim = realmMap.get("roles");
                if (rolesClaim instanceof List<?> roles) {
                    for (Object r : roles) {
                        if (r instanceof String roleName) {
                            // Normaliza: "reception" → "ROLE_RECEPTION"
                            String upper = roleName.toUpperCase();
                            String authority = upper.startsWith("ROLE_") ? upper : "ROLE_" + upper;
                            authorities.add(new SimpleGrantedAuthority(authority));
                        }
                    }
                }
            }

            // Mantém compatibilidade com scopes existentes
            Object scopeClaim = jwt.getClaim("scope");
            if (scopeClaim instanceof String scopeStr && !scopeStr.isBlank()) {
                Arrays.stream(scopeStr.split(" "))
                    .filter(s -> !s.isBlank())
                    .forEach(s -> authorities.add(new SimpleGrantedAuthority("SCOPE_" + s)));
            }

            return authorities;
        });
        return converter;
    }
}
