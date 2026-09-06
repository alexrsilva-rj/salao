package com.salao.security.config;

import com.salao.security.filter.ApiTokenAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

    @Value("${api.token.secret:}")
    private String apiTokenSecret;

    @Value("${swagger.enabled:false}")
    private boolean swaggerEnabled;

    @Bean
    public ApiTokenAuthenticationFilter apiTokenAuthenticationFilter() {
        return new ApiTokenAuthenticationFilter(apiTokenSecret);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())
            .headers(headers -> headers
                .contentTypeOptions(Customizer.withDefaults())
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000))
                .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'; frame-ancestors 'none'"))
                .referrerPolicy(referrer -> referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(apiTokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> {
                if (swaggerEnabled) {
                    auth.requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll();
                } else {
                    auth.requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").hasRole("RECEPTION");
                }
                auth
                    // Relatórios e financeiro: somente RECEPTION
                    .requestMatchers("/api/relatorios/**").hasRole("RECEPTION")
                    .requestMatchers("/api/financeiro/**").hasRole("RECEPTION")
                    // Catálogo: qualquer autenticado
                    .requestMatchers(HttpMethod.GET, "/api/catalogo/**").authenticated()
                    // Clientes: RECEPTION e CUSTOMER (isolamento feito na camada de serviço)
                    .requestMatchers("/api/clientes/**").hasAnyRole("RECEPTION", "CUSTOMER")
                    // Agendamentos: todos os perfis autenticados
                    .requestMatchers("/api/agendamentos/**").hasAnyRole("RECEPTION", "PROFESSIONAL", "CUSTOMER")
                    .anyRequest().authenticated();
            })
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
                            String upper = roleName.toUpperCase();
                            String authority = upper.startsWith("ROLE_") ? upper : "ROLE_" + upper;
                            authorities.add(new SimpleGrantedAuthority(authority));

                            // Mapeia sinônimos / roles legadas do Keycloak para garantir compatibilidade
                            mapRoleAliases(upper, authorities);
                        }
                    }
                }
            }

            // Extrai roles de resource_access (client roles) caso existam
            Object resourceAccess = jwt.getClaim("resource_access");
            if (resourceAccess instanceof Map<?, ?> resourceMap) {
                for (Object clientEntry : resourceMap.values()) {
                    if (clientEntry instanceof Map<?, ?> clientMap) {
                        Object clientRolesClaim = clientMap.get("roles");
                        if (clientRolesClaim instanceof List<?> clientRoles) {
                            for (Object r : clientRoles) {
                                if (r instanceof String roleName) {
                                    String upper = roleName.toUpperCase();
                                    String authority = upper.startsWith("ROLE_") ? upper : "ROLE_" + upper;
                                    authorities.add(new SimpleGrantedAuthority(authority));

                                    mapRoleAliases(upper, authorities);
                                }
                            }
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

    private static void mapRoleAliases(String roleName, List<GrantedAuthority> authorities) {
        String clean = roleName.startsWith("ROLE_") ? roleName.substring(5) : roleName;
        switch (clean) {
            case "ADMIN", "GERENTE" -> authorities.add(new SimpleGrantedAuthority("ROLE_RECEPTION"));
            case "CLIENTE" -> authorities.add(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
            case "PROFISSIONAL" -> authorities.add(new SimpleGrantedAuthority("ROLE_PROFESSIONAL"));
            default -> {}
        }
    }
}
